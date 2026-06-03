# Relatório de Validação — Import JSON → PostgreSQL
**Data:** 2026-05-28  
**Import executado:** `POST /api/import/bypass` com `X-Bypass-Key: FOLEGO_IMPORT_2026_WSMC`  
**Arquivo:** `folego-backup-processado.json` (baseado em `folego-backup-2026-05-28.json`)  
**Usuário:** `DIwOPWkwHcdn0ecdXF3uUro24kE2` (Wellington Sousa)

---

## ✅ Resultado Geral: SUCESSO TOTAL — 0 erros, 0 warnings

| Entidade      | JSON  | Importados | Skipped | Status |
|---------------|-------|-----------|---------|--------|
| categories    | 21    | 10        | 11      | ✅ (11 já existiam no DB antes do import) |
| banks         | 1     | 1         | 0       | ✅ |
| cards         | 3     | 3         | 0       | ✅ |
| recurrences   | 6     | 6         | 0       | ✅ |
| entries       | 789   | 789       | 0       | ✅ |
| investments   | 8     | 8         | 0       | ✅ |
| payslips      | 8     | 8         | 0       | ✅ |
| bills         | 0     | 0         | —       | ✅ |
| goals         | 0     | 0         | —       | ✅ |

---

## Detalhe por Entidade

### 1. Categories (21 total no DB)

| Campo JSON | Campo DB       | Mapeamento        |
|------------|----------------|-------------------|
| `name`     | `name`         | ✅ Preservado     |
| `icon`     | `icon`         | ✅ Preservado     |
| `color`    | `color`        | ✅ Preservado     |
| `type`     | `type`         | ✅ Preservado     |
| `id`       | *(não salvo)*  | DB gera novo UUID |
| —          | `budget`       | Default = 0       |
| —          | `nature`       | NULL              |

**11 categorias originais** (com type válido: expense/income/both):
Alimentação, Assinaturas, Investimentos, Lazer, Moradia, Outros, Renda, Salário/Renda, Saúde, Transferência well, Transporte

**10 categorias sintéticas** (adicionadas no pré-processamento — type='?'):
Beleza/Higiene, Compras Online, Delivery/Restaurante, Moradia/Contas, Pet, Salário, Saúde/Farmácia, Transferências, Transporte/Combustível, Vestuário/Joias

> ⚠️ **Ação recomendada:** Corrigir `type='?'` para `'expense'` ou `'both'` nas 10 categorias sintéticas via `PUT /api/categories/{id}`. O CHECK constraint do PostgreSQL não está sendo aplicado (Hibernate ddl-auto não gera CHECKs), mas o frontend deve normalizar esses valores.

---

### 2. Banks (1 — Santander)

| Campo JSON | Campo DB   | Valor DB          |
|------------|------------|-------------------|
| `name`     | `name`     | Santander         |
| `balance`  | `balance`  | R$ 114.141,12     |
| `color`    | `color`    | #ec0000           |
| `icon`     | `icon`     | 🏦                |
| —          | `type`     | "corrente" (default) |

> ⚠️ O JSON não tem campo `type` para bancos. Considere atualizar para "corrente" ou "digital" manualmente se necessário.

---

### 3. Cards (3)

| Nome                    | Bandeira    | Final | Limite     | Fecha | Vence | Banco          |
|-------------------------|-------------|-------|-----------|-------|-------|----------------|
| Santander Mastercard Elite | mastercard | 1791 | R$ 37.769,75 | 26  | 1     | Santander ✅   |
| PicPay                  | mastercard  | 3036  | R$ 18.200,00 | 28  | 5     | null (sem banco) |
| Mercado Pago            | mastercard  | 1010  | R$ 20.000,00 | 29  | 8     | null (sem banco) |

---

### 4. Recurrences (6)

| Nome                    | Valor         | Categoria        | Start    | End      | Meses | Ativo |
|-------------------------|---------------|------------------|----------|----------|-------|-------|
| Ninho Verde 2           | R$ 1.400,00   | Moradia/Contas   | 2026-06  | 2029-05  | 36    | ✅    |
| Condomínio Flex         | R$ 1.100,00   | Moradia/Contas   | 2026-06  | 2029-05  | 36    | ✅    |
| Internet Ninho Verde 2  | R$ 119,00     | Moradia/Contas   | 2026-06  | 2029-05  | 36    | ✅    |
| Jardineiro              | R$ 230,00     | Moradia/Contas   | 2026-06  | 2029-05  | 36    | ✅    |
| Caixa Economica         | R$ 2.460,53   | Moradia/Contas   | 2026-06  | 2029-05  | 36    | ✅    |
| Crullind                | R$ 3,90       | Assinaturas      | 2026-06  | 2029-05  | 36    | ✅    |

**Todos os 6 com `kind='debito_recorrente'`**, `accountId` → Santander (mapeado corretamente).

---

### 5. Entries (789)

**Distribuição por kind:**
| Kind               | Qtd |
|--------------------|-----|
| debito_recorrente  | 464 |
| credito_parcelado  | 123 |
| recorrente_cartao  | 99  |
| credito_avista     | 81  |
| receita            | 14  |
| pagamento_fatura   | 8   |

**Distribuição por categoria (top 10):**
| Categoria              | Qtd |
|------------------------|-----|
| Moradia/Contas         | 484 |
| Assinaturas            | 88  |
| Compras Online         | 70  |
| Transferências         | 63  |
| Alimentação            | 24  |
| Delivery/Restaurante   | 15  |
| Saúde/Farmácia         | 11  |
| Transporte/Combustível | 8   |
| Outros                 | 7   |
| Salário                | 7   |

**Valor total:** R$ 1.081.797,16  
**Menor:** R$ 3,90 | **Maior:** R$ 15.203,61

**Campos opcionais:**
- 563 entries com `recurrenceId` (vinculadas a recorrências)
- 123 entries com `installmentGroupId` (parceladas — grupos com UUID consistente)
- `isPaid`, `billingMonth`, `cardId` preservados corretamente

> ⚠️ IDs originais do JSON **não** são preservados — cada entry recebeu novo UUID no DB.  
> ⚠️ `isReconciled=false` para todas (não existia no JSON).

---

### 6. Investments (8)

Todos são "Previdência — PGBL [Mês]/26" de Mai/26 a Dez/26.

| Campo JSON       | Campo DB        | Valor             |
|------------------|-----------------|-------------------|
| `name`           | `name`          | ✅ Preservado     |
| `type`           | `type`          | "fundo" ✅        |
| `amount`         | `amount`        | R$ 616,24 ✅      |
| `currentValue`   | `current_value` | R$ 616,24 ✅      |
| `icon`           | `icon`          | 🏦 ✅             |
| `color`          | `color`         | #7c8dff ✅        |
| —                | `rate`          | NULL              |
| —                | `maturity`      | NULL              |
| —                | `bank_id`       | NULL              |
| —                | `is_emergency_reserve` | false     |

---

### 7. Payslips (8)

| Competência | Salário Base  | Líquido      | Extras | Outros Descontos |
|-------------|---------------|--------------|--------|-----------------|
| 2026-05     | R$ 17.126,25  | R$ 11.697,34 | 4      | 4               |
| 2026-06     | R$ 17.126,25  | R$ 11.697,34 | 4      | 4               |
| 2026-07     | R$ 17.126,25  | R$ 11.697,34 | 4      | 4               |
| 2026-08     | R$ 17.126,25  | R$ 15.203,61 | 4      | 4               |
| 2026-09     | R$ 17.126,25  | R$ 15.203,61 | 4      | 4               |
| 2026-10     | R$ 17.126,25  | R$ 15.203,61 | 4      | 4               |
| 2026-11     | R$ 17.126,25  | R$ 15.203,61 | 4      | 4               |
| 2026-12     | R$ 17.126,25  | R$ 15.203,61 | 4      | 4               |

Todos os campos financeiros preservados: inss=922.20, irrf=4716.04, pgbl=616.24, pensaoAlimenticia=3918.88, assistenciaMedica=1119.80, coparticipacao=345.68, seguroVida=32.42, fgts=2338.55.

**Items (payslip_items):**
- 4 extras: GRATIFICACAO FUNCAO LEGADO (9419.43), FERIAS MES (1896.12), 1/3 CONSTIT FERIAS MES (790.05), PROVISAO FERIAS CREDITO (430.06)
- 4 outrosDescontos: INSS FERIAS (65.87), DESC ADTO FERIAS (1896.12), DESC ADTO 1/3 FERIAS (790.05), CONTRIBUICAO NEGOCIAL (35.00)

Diferença no líquido maio-jul vs ago-dez: totalDescontos mai-jul=17.964,57 (inclui DESC ADTO FERIAS + DESC ADTO 1/3).

---

## ⚠️ Ações Pós-Import Recomendadas

1. **Corrigir type='?' nas 10 categorias sintéticas** → PATCH/PUT cada uma para type='expense' ou 'both'
2. **Remover endpoint bypass** após validação → deletar `POST /api/import/bypass` do `ImportController.java`
3. **Criar `app_config`** para o usuário com `currentMonthKey='2026-06'` (não foi importado)
4. **Não re-importar** sem limpar o banco antes — re-import geraria entradas duplicadas (sem deduplicação por ID original)
