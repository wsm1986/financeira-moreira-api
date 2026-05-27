package com.financeira.api.application.usecase.payslip;

import com.financeira.api.application.dto.PayslipRequest;
import com.financeira.api.application.dto.PayslipResponse;
import com.financeira.api.domain.model.Payslip;
import com.financeira.api.domain.model.PayslipItem;
import com.financeira.api.domain.repository.PayslipRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SavePayslipUseCase {

    private final PayslipRepository repository;

    public SavePayslipUseCase(PayslipRepository repository) {
        this.repository = repository;
    }

    public PayslipResponse execute(String userUid, PayslipRequest request) {
        Optional<Payslip> existing = repository.findByUserUidAndCompetencia(userUid, request.competencia());
        Payslip payslip = existing.orElseGet(() ->
                new Payslip(userUid, request.competencia(), request.salarioBase(),
                        request.totalProventos(), request.totalDescontos(), request.liquido())
        );
        payslip.setSalarioBase(request.salarioBase());
        payslip.setInss(request.inss());
        payslip.setIrrf(request.irrf());
        payslip.setPensaoAlimenticia(request.pensaoAlimenticia());
        payslip.setEmprestimoConsignado(request.emprestimoConsignado());
        payslip.setAssistenciaMedica(request.assistenciaMedica());
        payslip.setCoparticipacao(request.coparticipacao());
        payslip.setPgbl(request.pgbl());
        payslip.setSeguroVida(request.seguroVida());
        payslip.setValeTransporte(request.valeTransporte());
        payslip.setValeRefeicao(request.valeRefeicao());
        payslip.setFgts(request.fgts());
        payslip.setTotalProventos(request.totalProventos());
        payslip.setTotalDescontos(request.totalDescontos());
        payslip.setLiquido(request.liquido());
        payslip.setObservacoes(request.observacoes());
        payslip.setExtras(toItems(request.extras()));
        payslip.setOutrosDescontos(toItems(request.outrosDescontos()));
        return PayslipResponse.from(repository.save(payslip));
    }

    private List<PayslipItem> toItems(List<com.financeira.api.application.dto.PayslipItemDto> dtos) {
        if (dtos == null) return List.of();
        return dtos.stream()
                .map(d -> new PayslipItem(null, d.descricao(), d.valor()))
                .collect(Collectors.toList());
    }
}
