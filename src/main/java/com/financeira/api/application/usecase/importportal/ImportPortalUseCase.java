package com.financeira.api.application.usecase.importportal;

import com.financeira.api.application.dto.ImportPortalRequest;
import com.financeira.api.application.dto.ImportPortalRequest.*;
import com.financeira.api.application.dto.ImportPortalResponse;
import com.financeira.api.domain.model.*;
import com.financeira.api.domain.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ImportPortalUseCase {

    private final CategoryRepository categoryRepository;
    private final BankRepository bankRepository;
    private final CreditCardRepository cardRepository;
    private final EntryRepository entryRepository;
    private final RecurrenceRepository recurrenceRepository;
    private final BillRepository billRepository;
    private final InvestmentRepository investmentRepository;
    private final GoalRepository goalRepository;
    private final PayslipRepository payslipRepository;

    public ImportPortalUseCase(CategoryRepository categoryRepository,
                               BankRepository bankRepository,
                               CreditCardRepository cardRepository,
                               EntryRepository entryRepository,
                               RecurrenceRepository recurrenceRepository,
                               BillRepository billRepository,
                               InvestmentRepository investmentRepository,
                               GoalRepository goalRepository,
                               PayslipRepository payslipRepository) {
        this.categoryRepository = categoryRepository;
        this.bankRepository = bankRepository;
        this.cardRepository = cardRepository;
        this.entryRepository = entryRepository;
        this.recurrenceRepository = recurrenceRepository;
        this.billRepository = billRepository;
        this.investmentRepository = investmentRepository;
        this.goalRepository = goalRepository;
        this.payslipRepository = payslipRepository;
    }

    public ImportPortalResponse execute(String userUid, ImportPortalRequest request) {
        List<String> warnings = new ArrayList<>();

        // 1. Categories
        Map<String, UUID> categoryNameMap = new HashMap<>();
        for (Category existing : categoryRepository.findAllByUserUid(userUid)) {
            categoryNameMap.put(existing.getName().toLowerCase(), existing.getId());
        }
        int catImported = 0, catSkipped = 0;
        for (RawCategory raw : request.resolvedCategories()) {
            if (raw.name() == null) continue;
            if (categoryNameMap.containsKey(raw.name().toLowerCase())) {
                catSkipped++;
                continue;
            }
            Category cat = new Category(userUid,
                    raw.name(),
                    raw.icon() != null ? raw.icon() : "📁",
                    raw.budget() != null ? BigDecimal.valueOf(raw.budget()) : BigDecimal.ZERO,
                    raw.color() != null ? raw.color() : "#CCCCCC",
                    raw.type() != null ? raw.type() : "both",
                    raw.nature());
            Category saved = categoryRepository.save(cat);
            categoryNameMap.put(saved.getName().toLowerCase(), saved.getId());
            catImported++;
        }

        // 2. Banks
        Map<String, UUID> bankIdMap = new HashMap<>();
        int bankImported = 0;
        for (RawBank raw : request.resolvedBanks()) {
            Bank bank = new Bank(userUid,
                    raw.name(),
                    raw.type() != null ? raw.type() : "corrente",
                    raw.balance() != null ? BigDecimal.valueOf(raw.balance()) : BigDecimal.ZERO,
                    raw.color() != null ? raw.color() : "#CCCCCC",
                    raw.icon() != null ? raw.icon() : "🏦");
            Bank saved = bankRepository.save(bank);
            if (raw.id() != null) bankIdMap.put(raw.id(), saved.getId());
            bankImported++;
        }

        // 3. Cards
        Map<String, UUID> cardIdMap = new HashMap<>();
        int cardImported = 0;
        for (RawCard raw : request.resolvedCards()) {
            UUID mappedBankId = raw.bankId() != null ? bankIdMap.get(raw.bankId()) : null;
            CreditCard card = new CreditCard(userUid,
                    raw.name(),
                    raw.brand() != null ? raw.brand() : "mastercard",
                    raw.lastDigits() != null ? raw.lastDigits() : "0000",
                    raw.limit() != null ? BigDecimal.valueOf(raw.limit()) : BigDecimal.ZERO,
                    raw.closingDay() != null ? raw.closingDay() : 1,
                    raw.dueDay() != null ? raw.dueDay() : 1,
                    raw.color() != null ? raw.color() : "#CCCCCC",
                    raw.icon() != null ? raw.icon() : "💳",
                    mappedBankId);
            CreditCard saved = cardRepository.save(card);
            if (raw.id() != null) cardIdMap.put(raw.id(), saved.getId());
            cardImported++;
        }

        // 4. Recurrences — importadas ANTES das entries para montar o recurrenceIdMap
        Map<String, UUID> recurrenceIdMap = new HashMap<>();  // old portal id → new DB UUID
        int recImported = 0, recSkipped = 0;
        for (RawRecurrence raw : request.resolvedRecurrences()) {
            UUID categoryId = raw.category() != null
                    ? categoryNameMap.get(raw.category().toLowerCase()) : null;
            if (categoryId == null) {
                warnings.add("Categoria '" + raw.category() + "' não encontrada, recorrência '" + raw.name() + "' ignorada");
                recSkipped++;
                continue;
            }
            Recurrence rec = new Recurrence(userUid, raw.name(), raw.icon(), categoryId,
                    raw.kind() != null ? raw.kind() : "debito_recorrente",
                    raw.amount() != null ? BigDecimal.valueOf(raw.amount()) : BigDecimal.ZERO,
                    raw.cardId() != null ? cardIdMap.getOrDefault(raw.cardId(), null) : null,
                    raw.accountId() != null ? bankIdMap.getOrDefault(raw.accountId(), null) : null,
                    raw.startMonth() != null ? raw.startMonth() : "2026-01",
                    raw.endMonth(), raw.months() != null ? raw.months() : 12,
                    raw.active() != null ? raw.active() : true);
            Recurrence saved = recurrenceRepository.save(rec);
            if (raw.id() != null) recurrenceIdMap.put(raw.id(), saved.getId());
            recImported++;
        }

        // 5. Entries — após recurrences para resolver recurrenceId e installmentGroupId
        // installmentGroupIdMap: garante que todas as parcelas do mesmo grupo compartilham o mesmo UUID
        Map<String, UUID> installmentGroupIdMap = new HashMap<>();
        int entryImported = 0, entrySkipped = 0;
        for (RawEntry raw : request.resolvedEntries()) {
            UUID categoryId = raw.category() != null
                    ? categoryNameMap.get(raw.category().toLowerCase()) : null;
            if (categoryId == null) {
                warnings.add("Categoria '" + raw.category() + "' não encontrada, lançamento '" + raw.name() + "' ignorado");
                entrySkipped++;
                continue;
            }
            LocalDate entryDate = raw.date() != null ? LocalDate.parse(raw.date().substring(0, 10)) : LocalDate.now();
            String monthKey = raw.monthKey() != null ? raw.monthKey() : entryDate.toString().substring(0, 7);
            Entry entry = new Entry(userUid, monthKey, raw.kind() != null ? raw.kind() : "debito_avista",
                    raw.name(), categoryId,
                    raw.amount() != null ? BigDecimal.valueOf(raw.amount()) : BigDecimal.ZERO,
                    entryDate, raw.icon() != null ? raw.icon() : "💰");

            // Campos opcionais
            entry.setIsPaid(raw.isPaid() != null ? raw.isPaid() : false);
            entry.setIsReconciled(raw.isReconciled() != null ? raw.isReconciled() : false);
            entry.setNotes(raw.notes());
            entry.setTags(raw.tags());
            entry.setBillingMonth(raw.billingMonth());
            entry.setInvoiceRef(raw.invoiceRef());

            // FKs resolvidos pelos maps
            if (raw.accountId() != null)
                entry.setAccountId(bankIdMap.getOrDefault(raw.accountId(), null));
            if (raw.toAccountId() != null)
                entry.setToAccountId(bankIdMap.getOrDefault(raw.toAccountId(), null));
            if (raw.cardId() != null)
                entry.setCardId(cardIdMap.getOrDefault(raw.cardId(), null));

            // Recorrência: resolve portal string id → novo DB UUID
            if (raw.recurrenceId() != null) {
                UUID mappedRecId = recurrenceIdMap.get(raw.recurrenceId());
                // Se não encontrado no map (recorrência veio sem contrato), tenta UUID direto
                if (mappedRecId == null) {
                    try { mappedRecId = UUID.fromString(raw.recurrenceId()); } catch (IllegalArgumentException ignored) {}
                }
                entry.setRecurrenceId(mappedRecId);
            }
            entry.setRecurrenceMonths(raw.recurrenceMonths());

            // Parcelas: garante UUID consistente por grupo dentro desta importação
            if (raw.installmentGroupId() != null) {
                UUID groupUuid = installmentGroupIdMap.computeIfAbsent(
                        raw.installmentGroupId(), k -> UUID.randomUUID());
                entry.setInstallmentGroupId(groupUuid);
            }
            entry.setInstallmentTotal(raw.installmentTotal());
            entry.setInstallmentCurrent(raw.installmentCurrent());

            entryRepository.save(entry);
            entryImported++;
        }

        // 6. Bills
        int billImported = 0, billSkipped = 0;
        for (RawBill raw : request.resolvedBills()) {
            UUID categoryId = raw.category() != null
                    ? categoryNameMap.get(raw.category().toLowerCase()) : null;
            if (categoryId == null) {
                warnings.add("Categoria '" + raw.category() + "' não encontrada, conta '" + raw.name() + "' ignorada");
                billSkipped++;
                continue;
            }
            LocalDate dueDate = raw.dueDate() != null ? LocalDate.parse(raw.dueDate().substring(0, 10)) : LocalDate.now();
            LocalDate paidDate = raw.paidDate() != null ? LocalDate.parse(raw.paidDate().substring(0, 10)) : null;
            Bill bill = new Bill(userUid, raw.name(),
                    raw.amount() != null ? BigDecimal.valueOf(raw.amount()) : BigDecimal.ZERO,
                    dueDate, categoryId, raw.paid(),
                    paidDate,
                    raw.bankId() != null ? bankIdMap.getOrDefault(raw.bankId(), null) : null,
                    raw.notes(), raw.type() != null ? raw.type() : "pagar");
            billRepository.save(bill);
            billImported++;
        }

        // 7. Investments
        int invImported = 0;
        for (RawInvestment raw : request.resolvedInvestments()) {
            Investment inv = new Investment(userUid, raw.name(), raw.type() != null ? raw.type() : "outro",
                    raw.amount() != null ? BigDecimal.valueOf(raw.amount()) : BigDecimal.ZERO,
                    raw.currentValue() != null ? BigDecimal.valueOf(raw.currentValue()) : BigDecimal.ZERO,
                    raw.rate() != null ? BigDecimal.valueOf(raw.rate()) : null,
                    raw.maturity() != null ? LocalDate.parse(raw.maturity().substring(0, 10)) : null,
                    raw.bankId() != null ? bankIdMap.getOrDefault(raw.bankId(), null) : null,
                    raw.isEmergencyReserve(),
                    raw.icon() != null ? raw.icon() : "📊",
                    raw.color() != null ? raw.color() : "#CCCCCC");
            investmentRepository.save(inv);
            invImported++;
        }

        // 8. Goals
        int goalImported = 0;
        for (RawGoal raw : request.resolvedGoals()) {
            Goal goal = new Goal(userUid, raw.name(), raw.icon(),
                    raw.targetAmount() != null ? BigDecimal.valueOf(raw.targetAmount()) : BigDecimal.ZERO,
                    raw.currentAmount() != null ? BigDecimal.valueOf(raw.currentAmount()) : BigDecimal.ZERO,
                    raw.deadline() != null ? raw.deadline() : "2027-12",
                    raw.color() != null ? raw.color() : "#CCCCCC",
                    raw.status() != null ? raw.status() : "on-track",
                    raw.notes());
            goalRepository.save(goal);
            goalImported++;
        }

        // 9. Payslips
        int payslipImported = 0;
        for (RawPayslip raw : request.resolvedPayslips()) {
            Optional<Payslip> existing = payslipRepository.findByUserUidAndCompetencia(userUid, raw.competencia());
            Payslip payslip = existing.orElseGet(() ->
                    new Payslip(userUid, raw.competencia(),
                            raw.salarioBase() != null ? BigDecimal.valueOf(raw.salarioBase()) : BigDecimal.ZERO,
                            raw.totalProventos() != null ? BigDecimal.valueOf(raw.totalProventos()) : BigDecimal.ZERO,
                            raw.totalDescontos() != null ? BigDecimal.valueOf(raw.totalDescontos()) : BigDecimal.ZERO,
                            raw.liquido() != null ? BigDecimal.valueOf(raw.liquido()) : BigDecimal.ZERO)
            );
            payslip.setSalarioBase(raw.salarioBase() != null ? BigDecimal.valueOf(raw.salarioBase()) : BigDecimal.ZERO);
            payslip.setInss(raw.inss() != null ? BigDecimal.valueOf(raw.inss()) : null);
            payslip.setIrrf(raw.irrf() != null ? BigDecimal.valueOf(raw.irrf()) : null);
            payslip.setPensaoAlimenticia(raw.pensaoAlimenticia() != null ? BigDecimal.valueOf(raw.pensaoAlimenticia()) : null);
            payslip.setEmprestimoConsignado(raw.emprestimoConsignado() != null ? BigDecimal.valueOf(raw.emprestimoConsignado()) : null);
            payslip.setAssistenciaMedica(raw.assistenciaMedica() != null ? BigDecimal.valueOf(raw.assistenciaMedica()) : null);
            payslip.setCoparticipacao(raw.coparticipacao() != null ? BigDecimal.valueOf(raw.coparticipacao()) : null);
            payslip.setPgbl(raw.pgbl() != null ? BigDecimal.valueOf(raw.pgbl()) : null);
            payslip.setSeguroVida(raw.seguroVida() != null ? BigDecimal.valueOf(raw.seguroVida()) : null);
            payslip.setValeTransporte(raw.valeTransporte() != null ? BigDecimal.valueOf(raw.valeTransporte()) : null);
            payslip.setValeRefeicao(raw.valeRefeicao() != null ? BigDecimal.valueOf(raw.valeRefeicao()) : null);
            payslip.setFgts(raw.fgts() != null ? BigDecimal.valueOf(raw.fgts()) : null);
            payslip.setTotalProventos(raw.totalProventos() != null ? BigDecimal.valueOf(raw.totalProventos()) : BigDecimal.ZERO);
            payslip.setTotalDescontos(raw.totalDescontos() != null ? BigDecimal.valueOf(raw.totalDescontos()) : BigDecimal.ZERO);
            payslip.setLiquido(raw.liquido() != null ? BigDecimal.valueOf(raw.liquido()) : BigDecimal.ZERO);
            payslip.setObservacoes(raw.observacoes());
            if (raw.extras() != null) {
                payslip.setExtras(raw.extras().stream()
                        .map(i -> new PayslipItem("extra", i.descricao(), i.valor() != null ? BigDecimal.valueOf(i.valor()) : BigDecimal.ZERO))
                        .collect(Collectors.toList()));
            }
            if (raw.outrosDescontos() != null) {
                payslip.setOutrosDescontos(raw.outrosDescontos().stream()
                        .map(i -> new PayslipItem("desconto", i.descricao(), i.valor() != null ? BigDecimal.valueOf(i.valor()) : BigDecimal.ZERO))
                        .collect(Collectors.toList()));
            }
            payslipRepository.save(payslip);
            payslipImported++;
        }

        Map<String, ImportPortalResponse.EntityStats> summary = new LinkedHashMap<>();
        summary.put("categories",  new ImportPortalResponse.EntityStats(catImported,   catSkipped));
        summary.put("banks",       new ImportPortalResponse.EntityStats(bankImported,   0));
        summary.put("cards",       new ImportPortalResponse.EntityStats(cardImported,   0));
        summary.put("recurrences", new ImportPortalResponse.EntityStats(recImported,    recSkipped));
        summary.put("entries",     new ImportPortalResponse.EntityStats(entryImported,  entrySkipped));
        summary.put("bills",       new ImportPortalResponse.EntityStats(billImported,   billSkipped));
        summary.put("investments", new ImportPortalResponse.EntityStats(invImported,    0));
        summary.put("goals",       new ImportPortalResponse.EntityStats(goalImported,   0));
        summary.put("payslips",    new ImportPortalResponse.EntityStats(payslipImported,0));

        return new ImportPortalResponse(summary, warnings);
    }
}
