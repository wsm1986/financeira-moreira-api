# Prompt de Desenvolvimento — Folego Financeiro Frontend v3

## Contexto

Você está desenvolvendo o **Folego Financeiro**, um app de gestão financeira pessoal para uso do Wellington Sousa. O backend (Spring Boot 3.2 / Java 21 / PostgreSQL) já está em produção em `https://financeira-moreira-api.onrender.com`. O frontend atual (React 19 + Zustand 5 + Vite 8) funcionava offline com estado local; agora precisa se integrar ao backend.

A migração de dados já foi executada: **789 lançamentos, 21 categorias, 1 banco, 3 cartões, 6 recorrências, 8 investimentos, 8 holerites** estão no PostgreSQL em produção.

---

## Tech Stack

### Frontend (manter / usar)
- **React 19** (Server Components opcionais)
- **Zustand 5** — manter para estado de UI (mês ativo, modais, loading); não mais fonte de dados
- **Vite 8** — bundler
- **TypeScript** — obrigatório em todos os arquivos
- **Tailwind CSS** — estilização
- **React Query / TanStack Query v5** — **adicionar** para cache e sincronização com a API

### Autenticação
- **Firebase Auth** (Google + Email/Password)
- Token JWT via `auth.currentUser.getIdToken()` — renovar automaticamente (expira em 1h)

### Backend
- **Base URL:** `https://financeira-moreira-api.onrender.com`
- **Render Free Tier:** hiberna após 15min sem requests → primeiro request demora ~15-80s (cold start)
- Fazer wake-up call em `GET /api/ping` na inicialização do app antes de qualquer request autenticado

---

## Autenticação — Fluxo Completo

```
1. Usuário loga com Firebase Auth (Google ou email)
2. Obter token: const token = await auth.currentUser.getIdToken(true)
3. Passar em TODAS as requests autenticadas:
   Authorization: Bearer <token>
4. Firebase renova o token automaticamente se expirado (getIdToken(true) força refresh)
5. Interceptor Axios/fetch: se 401, chamar getIdToken(true) e retry
```

### Axios interceptor recomendado:
```typescript
axiosInstance.interceptors.request.use(async (config) => {
  const token = await auth.currentUser?.getIdToken();
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

axiosInstance.interceptors.response.use(
  (res) => res,
  async (error) => {
    if (error.response?.status === 401 && !error.config._retry) {
      error.config._retry = true;
      const token = await auth.currentUser?.getIdToken(true); // force refresh
      error.config.headers.Authorization = `Bearer ${token}`;
      return axiosInstance(error.config);
    }
    return Promise.reject(error);
  }
);
```

---

## API Contract Completo

### Endpoints Públicos (sem token)
```
GET  /api/ping     → { status: "alive", ts: number, build: string }
GET  /api/version  → "build:..."
GET  /api/sync     → "Sincronizado"
```

---

### Categories — GET /api/categories

**Request:** `GET /api/categories`  
**Auth:** Bearer token  

**Response:** `CategoryResponse[]`
```typescript
interface CategoryResponse {
  id: string;           // UUID
  name: string;
  icon: string;         // emoji
  budget: number;       // R$ orçamento mensal (0 = sem orçamento)
  color: string;        // hex #RRGGBB
  type: "expense" | "income" | "both";
  nature: "essencial" | "desejo" | "investimento" | null;
}
```

**Criar:** `POST /api/categories`
```typescript
interface CategoryRequest {
  name: string;          // required
  icon?: string;
  budget?: number;
  color?: string;
  type: "expense" | "income" | "both";  // required
  nature?: "essencial" | "desejo" | "investimento";
}
```

**Atualizar:** `PUT /api/categories/{id}`  
**Deletar:** `DELETE /api/categories/{id}` → 204

**Dados atuais no DB (21 categorias):**
- 11 originais com type correto (expense/income/both)
- 10 sintéticas com type='?' — **filtrar ou normalizar no frontend até serem corrigidas**

---

### Banks — GET /api/banks

**Response:** `BankResponse[]`
```typescript
interface BankResponse {
  id: string;
  name: string;
  type: "corrente" | "poupanca" | "investimento" | "digital";
  balance: number;
  color: string;
  icon: string;
}
```

**Criar:** `POST /api/banks`
```typescript
interface BankRequest {
  name: string;      // required
  type: string;      // required: corrente|poupanca|investimento|digital
  balance?: number;
  color?: string;
  icon?: string;
}
```

**Atualizar:** `PUT /api/banks/{id}`  
**Deletar:** `DELETE /api/banks/{id}` → 204 (soft delete)  
**Ajustar saldo:** `PUT /api/banks/{id}/balance` — body: `{ balance: number }`

**Dados atuais:** 1 banco — Santander, saldo=114.141,12, type='corrente'

---

### Cards — GET /api/cards

**Response:** `CardResponse[]`
```typescript
interface CardResponse {
  id: string;
  name: string;
  brand: "visa" | "mastercard" | "elo" | "amex" | "hipercard";
  lastDigits: string;   // 4 chars
  cardLimit: number;
  closingDay: number;   // 1-28
  dueDay: number;       // 1-28
  color: string;
  icon: string;
  bankId: string | null;
}
```

**Criar:** `POST /api/cards`
```typescript
interface CardRequest {
  name: string;
  brand: string;        // visa|mastercard|elo|amex|hipercard
  lastDigits: string;   // exatamente 4 chars
  cardLimit: number;    // positive
  closingDay: number;   // 1-28
  dueDay: number;       // 1-28
  color?: string;
  icon?: string;
  bankId?: string;      // UUID banco vinculado
}
```

**Atualizar:** `PUT /api/cards/{id}`  
**Deletar:** `DELETE /api/cards/{id}` → 204

**Dados atuais:** 3 cartões
- Santander Mastercard Elite — final 1791, limite 37.769,75, fecha 26, vence 1
- PicPay — final 3036, limite 18.200,00, fecha 28, vence 5
- Mercado Pago — final 1010, limite 20.000,00, fecha 29, vence 8

---

### Entries — GET /api/entries?monthKey=YYYY-MM

**Request:** `GET /api/entries?monthKey=2026-05`  
**Response:** `EntryResponse[]`

```typescript
type EntryKind =
  | "receita"
  | "debito_avista"
  | "debito_recorrente"
  | "credito_avista"
  | "credito_parcelado"
  | "recorrente_cartao"
  | "pagamento_fatura"
  | "transferencia";

interface EntryResponse {
  id: string;
  monthKey: string;            // YYYY-MM
  kind: EntryKind;
  name: string;
  categoryId: string;          // UUID da categoria
  amount: number;
  entryDate: string;           // YYYY-MM-DD (LocalDate serializa assim)
  icon: string;
  // Opcional por kind:
  accountId: string | null;    // receita, debito_*, pagamento_fatura, transferencia
  installmentTotal: number | null;   // credito_parcelado
  installmentCurrent: number | null; // credito_parcelado
  installmentGroupId: string | null; // credito_parcelado — UUID do grupo
  recurrenceId: string | null;       // debito_recorrente, recorrente_cartao
  recurrenceMonths: number | null;
  cardId: string | null;             // credito_*, recorrente_cartao, pagamento_fatura
  billingMonth: string | null;       // YYYY-MM — mês da fatura para crédito
  invoiceRef: string | null;         // YYYY-MM — mês referência do pagamento de fatura
  toAccountId: string | null;        // transferencia — conta destino
  isPaid: boolean;
  isReconciled: boolean;
  notes: string | null;
  tags: string[] | null;
}
```

**Criar:** `POST /api/entries` → 201
```typescript
interface EntryRequest {
  monthKey: string;      // required
  kind: EntryKind;       // required
  name: string;          // required
  categoryId: string;    // required UUID
  amount: number;        // required, positive
  entryDate: string;     // required YYYY-MM-DD
  icon?: string;
  accountId?: string;
  installmentTotal?: number;
  installmentCurrent?: number;
  installmentGroupId?: string;
  recurrenceId?: string;
  recurrenceMonths?: number;
  cardId?: string;
  billingMonth?: string;
  invoiceRef?: string;
  toAccountId?: string;
  isPaid?: boolean;
  isReconciled?: boolean;
  notes?: string;
  tags?: string[];
}
```

**Atualizar:** `PUT /api/entries/{id}`  
**Deletar:** `DELETE /api/entries/{id}` → 204 (soft delete)  
**Marcar pago/pendente:** `PATCH /api/entries/{id}/paid`

**Dados atuais (789 entries):**
- 464 debito_recorrente | 123 credito_parcelado | 99 recorrente_cartao | 81 credito_avista | 14 receita | 8 pagamento_fatura
- 563 com recurrenceId | 123 com installmentGroupId
- Total: R$ 1.081.797,16 | min: R$ 3,90 | max: R$ 15.203,61

---

### Recurrences — GET /api/recurrences

**Response:** `RecurrenceResponse[]`
```typescript
interface RecurrenceResponse {
  id: string;
  name: string;
  icon: string;
  categoryId: string | null;
  kind: EntryKind;
  amount: number;
  cardId: string | null;
  accountId: string | null;
  startMonth: string;   // YYYY-MM
  endMonth: string | null;
  months: number;
  active: boolean;
}
```

**Criar:** `POST /api/recurrences` → 201
```typescript
interface RecurrenceRequest {
  name: string;
  icon?: string;
  categoryId?: string;
  kind: string;          // required — aceita também "frequency" como alias
  amount: number;        // required, positive
  cardId?: string;
  accountId?: string;
  startMonth: string;    // required YYYY-MM
  endMonth?: string;
  months: number;        // required
  active?: boolean;
}
```

**Atualizar:** `PUT /api/recurrences/{id}`  
**Deletar:** `DELETE /api/recurrences/{id}` → 204  
**Cancelar:** `PATCH /api/recurrences/{id}/cancel` → active=false

**Dados atuais (6 recorrências):**
Todas `kind=debito_recorrente`, accountId=Santander, start=2026-06, end=2029-05, months=36:
- Ninho Verde 2: R$ 1.400 | Condomínio Flex: R$ 1.100 | Internet NV2: R$ 119
- Jardineiro: R$ 230 | Caixa Economica: R$ 2.460,53 | Crullind: R$ 3,90 (Assinaturas)

---

### Investments — GET /api/investments

**Response:** `InvestmentResponse[]`
```typescript
type InvestmentType = "renda_fixa" | "renda_variavel" | "fundo" | "cripto" | "imovel" | "outro";

interface InvestmentResponse {
  id: string;
  name: string;
  type: InvestmentType;
  amount: number;        // valor investido
  currentValue: number;  // valor atual de mercado
  rate: number | null;   // % taxa (ex: 120 = 120% CDI)
  maturity: string | null; // YYYY-MM-DD
  bankId: string | null;
  isEmergencyReserve: boolean;
  icon: string;
  color: string;
}
```

**Criar:** `POST /api/investments` → 201
```typescript
interface InvestmentRequest {
  name: string;
  type: string;          // InvestmentType
  amount: number;        // required
  currentValue?: number; // aceita também "returns" como alias
  rateStr?: string;      // string descritiva ex: "CDI 120%" (não calculado)
  maturity?: string;     // YYYY-MM-DD — aceita também "date" e "startDate" como aliases
  bankId?: string;
  isEmergencyReserve?: boolean;
  icon?: string;
  color?: string;
}
```

**Atualizar:** `PUT /api/investments/{id}`  
**Deletar:** `DELETE /api/investments/{id}` → 204

**Dados atuais (8 investimentos):**
Todos: Previdência — PGBL [Mês]/26, type=fundo, amount=currentValue=616,24, icon=🏦, color=#7c8dff

---

### Payslips — GET /api/payslips

**Response:** `PayslipResponse[]`
```typescript
interface PayslipItemDto {
  descricao: string;
  valor: number;
}

interface PayslipResponse {
  id: string;
  competencia: string;      // YYYY-MM
  salarioBase: number;
  extras: PayslipItemDto[];  // proventos variáveis
  inss: number;
  irrf: number;
  pensaoAlimenticia: number;
  emprestimoConsignado: number;
  assistenciaMedica: number;
  coparticipacao: number;
  pgbl: number;
  seguroVida: number;
  valeTransporte: number;
  valeRefeicao: number;
  outrosDescontos: PayslipItemDto[]; // descontos variáveis
  fgts: number;
  totalProventos: number;
  totalDescontos: number;
  liquido: number;
  observacoes: string | null;
}
```

**Criar/Atualizar (upsert por competencia):** `POST /api/payslips` → 200/201
```typescript
interface PayslipRequest {
  competencia: string;    // required YYYY-MM
  salarioBase: number;    // required
  extras?: PayslipItemDto[];
  inss?: number;
  irrf?: number;
  pensaoAlimenticia?: number;
  emprestimoConsignado?: number;
  assistenciaMedica?: number;
  coparticipacao?: number;
  pgbl?: number;
  seguroVida?: number;
  valeTransporte?: number;
  valeRefeicao?: number;
  outrosDescontos?: PayslipItemDto[];
  fgts?: number;
  totalProventos: number;   // required
  totalDescontos: number;   // required
  liquido: number;          // required
  observacoes?: string;
}
```

**Deletar:** `DELETE /api/payslips/{id}` → 204

**Dados atuais (8 holerites 2026-05 a 2026-12):**
salarioBase=17.126,25 para todos; liquido: mai-jul=11.697,34, ago-dez=15.203,61

---

### Bills — GET /api/bills

**Response:** `BillResponse[]`
```typescript
interface BillResponse {
  id: string;
  name: string;
  amount: number;
  dueDate: string;       // YYYY-MM-DD
  categoryId: string | null;
  paid: boolean;
  paidDate: string | null; // YYYY-MM-DD
  bankId: string | null;
  notes: string | null;
  type: "pagar" | "receber";
}
```

**Criar:** `POST /api/bills` → 201  
**Atualizar:** `PUT /api/bills/{id}`  
**Deletar:** `DELETE /api/bills/{id}` → 204  
**Pagar:** `PATCH /api/bills/{id}/pay` → paid=true, paidDate=hoje

---

### Goals — GET /api/goals

**Response:** `GoalResponse[]`
```typescript
interface GoalResponse {
  id: string;
  name: string;
  icon: string;
  targetAmount: number;
  currentAmount: number;
  deadline: string;      // YYYY-MM
  color: string;
  status: "on-track" | "at-risk" | "great" | "completed";
  notes: string | null;
}
```

**Criar:** `POST /api/goals` → 201  
**Atualizar:** `PUT /api/goals/{id}`  
**Deletar:** `DELETE /api/goals/{id}` → 204  
**Adicionar progresso:** `POST /api/goals/{id}/progress` — body: `{ amount: number }`

---

### WhatsApp/Notificações
```
POST /api/whatsapp/test          → testa envio WhatsApp (precisa token)
POST /api/whatsapp/send          → body: { mensagem: string }
POST /api/notificacoes/teste     → alias
POST /api/notificacoes/enviar    → alias
Response: { ok: boolean, status: string, message: string }
```

---

## Regras de Negócio Críticas

### Entry Kinds — regras de campos obrigatórios
```
receita              → accountId (conta que recebe)
debito_avista        → accountId
debito_recorrente    → accountId, recurrenceId (opcional)
credito_avista       → cardId, billingMonth
credito_parcelado    → cardId, billingMonth, installmentTotal, installmentCurrent, installmentGroupId
recorrente_cartao    → cardId, billingMonth, recurrenceId
pagamento_fatura     → accountId (débito da conta), cardId, invoiceRef (mês da fatura)
transferencia        → accountId (origem), toAccountId (destino)
```

### Cálculo de Saldo Bancário
- `debito_*` e `pagamento_fatura`: saldo diminui quando `isPaid=true`
- `receita` e `transferencia` (destino): saldo aumenta quando `isPaid=true`
- `credito_*`: NÃO afeta saldo imediatamente — afeta quando `pagamento_fatura` é marcado como pago

### monthKey vs billingMonth
- `monthKey`: mês em que a entry aparece no extrato/dashboard
- `billingMonth`: mês da fatura do cartão (para crédito) — pode diferir de monthKey (compra em 25/mai fecha em 26, billing=2026-06)

### Installment Group
- Todas as parcelas de uma compra parcelada compartilham o mesmo `installmentGroupId`
- installmentCurrent: 1, 2, 3... installmentTotal
- Ao criar: gerar UUID único para o grupo e usar em todas as parcelas

---

## Estrutura de State Management (Zustand → React Query)

### Antes (Zustand local)
```typescript
// tudo no store, sem backend
const entries = useStore(s => s.entries);
const addEntry = useStore(s => s.addEntry);
```

### Depois (React Query + Zustand para UI)
```typescript
// Dados remotos: React Query
const { data: entries, isLoading } = useQuery({
  queryKey: ['entries', monthKey],
  queryFn: () => api.entries.listByMonth(monthKey),
  staleTime: 5 * 60 * 1000, // 5 min cache
});

const createEntry = useMutation({
  mutationFn: (req: EntryRequest) => api.entries.create(req),
  onSuccess: () => queryClient.invalidateQueries({ queryKey: ['entries'] }),
});

// Estado de UI: Zustand (sem dados do servidor)
const { activeMonth, setActiveMonth, openModal, closeModal } = useUIStore();
```

---

## Serviço de API — Estrutura Recomendada

```typescript
// src/services/api.ts
const BASE = "https://financeira-moreira-api.onrender.com";

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const token = await auth.currentUser?.getIdToken();
  const res = await fetch(`${BASE}${path}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${token}`,
      ...options?.headers,
    },
  });
  if (!res.ok) {
    const err = await res.json().catch(() => ({}));
    throw new ApiError(res.status, err.message || "Erro na API");
  }
  return res.status === 204 ? undefined as T : res.json();
}

export const api = {
  categories: {
    list: () => request<CategoryResponse[]>("/api/categories"),
    create: (req: CategoryRequest) => request<CategoryResponse>("/api/categories", { method: "POST", body: JSON.stringify(req) }),
    update: (id: string, req: CategoryRequest) => request<CategoryResponse>(`/api/categories/${id}`, { method: "PUT", body: JSON.stringify(req) }),
    delete: (id: string) => request<void>(`/api/categories/${id}`, { method: "DELETE" }),
  },
  banks: {
    list: () => request<BankResponse[]>("/api/banks"),
    create: (req: BankRequest) => request<BankResponse>("/api/banks", { method: "POST", body: JSON.stringify(req) }),
    update: (id: string, req: BankRequest) => request<BankResponse>(`/api/banks/${id}`, { method: "PUT", body: JSON.stringify(req) }),
    delete: (id: string) => request<void>(`/api/banks/${id}`, { method: "DELETE" }),
    updateBalance: (id: string, balance: number) => request<BankResponse>(`/api/banks/${id}/balance`, { method: "PUT", body: JSON.stringify({ balance }) }),
  },
  cards: {
    list: () => request<CardResponse[]>("/api/cards"),
    create: (req: CardRequest) => request<CardResponse>("/api/cards", { method: "POST", body: JSON.stringify(req) }),
    update: (id: string, req: CardRequest) => request<CardResponse>(`/api/cards/${id}`, { method: "PUT", body: JSON.stringify(req) }),
    delete: (id: string) => request<void>(`/api/cards/${id}`, { method: "DELETE" }),
  },
  entries: {
    listByMonth: (monthKey: string) => request<EntryResponse[]>(`/api/entries?monthKey=${monthKey}`),
    create: (req: EntryRequest) => request<EntryResponse>("/api/entries", { method: "POST", body: JSON.stringify(req) }),
    update: (id: string, req: EntryRequest) => request<EntryResponse>(`/api/entries/${id}`, { method: "PUT", body: JSON.stringify(req) }),
    delete: (id: string) => request<void>(`/api/entries/${id}`, { method: "DELETE" }),
    togglePaid: (id: string) => request<EntryResponse>(`/api/entries/${id}/paid`, { method: "PATCH" }),
  },
  recurrences: {
    list: () => request<RecurrenceResponse[]>("/api/recurrences"),
    create: (req: RecurrenceRequest) => request<RecurrenceResponse>("/api/recurrences", { method: "POST", body: JSON.stringify(req) }),
    update: (id: string, req: RecurrenceRequest) => request<RecurrenceResponse>(`/api/recurrences/${id}`, { method: "PUT", body: JSON.stringify(req) }),
    delete: (id: string) => request<void>(`/api/recurrences/${id}`, { method: "DELETE" }),
    cancel: (id: string) => request<RecurrenceResponse>(`/api/recurrences/${id}/cancel`, { method: "PATCH" }),
  },
  investments: {
    list: () => request<InvestmentResponse[]>("/api/investments"),
    create: (req: InvestmentRequest) => request<InvestmentResponse>("/api/investments", { method: "POST", body: JSON.stringify(req) }),
    update: (id: string, req: InvestmentRequest) => request<InvestmentResponse>(`/api/investments/${id}`, { method: "PUT", body: JSON.stringify(req) }),
    delete: (id: string) => request<void>(`/api/investments/${id}`, { method: "DELETE" }),
  },
  payslips: {
    list: () => request<PayslipResponse[]>("/api/payslips"),
    save: (req: PayslipRequest) => request<PayslipResponse>("/api/payslips", { method: "POST", body: JSON.stringify(req) }),
    delete: (id: string) => request<void>(`/api/payslips/${id}`, { method: "DELETE" }),
  },
  bills: {
    list: () => request<BillResponse[]>("/api/bills"),
    create: (req: BillRequest) => request<BillResponse>("/api/bills", { method: "POST", body: JSON.stringify(req) }),
    update: (id: string, req: BillRequest) => request<BillResponse>(`/api/bills/${id}`, { method: "PUT", body: JSON.stringify(req) }),
    delete: (id: string) => request<void>(`/api/bills/${id}`, { method: "DELETE" }),
    pay: (id: string) => request<BillResponse>(`/api/bills/${id}/pay`, { method: "PATCH" }),
  },
  goals: {
    list: () => request<GoalResponse[]>("/api/goals"),
    create: (req: GoalRequest) => request<GoalResponse>("/api/goals", { method: "POST", body: JSON.stringify(req) }),
    update: (id: string, req: GoalRequest) => request<GoalResponse>(`/api/goals/${id}`, { method: "PUT", body: JSON.stringify(req) }),
    delete: (id: string) => request<void>(`/api/goals/${id}`, { method: "DELETE" }),
    addProgress: (id: string, amount: number) => request<GoalResponse>(`/api/goals/${id}/progress`, { method: "POST", body: JSON.stringify({ amount }) }),
  },
};
```

---

## Wake-up do Render na Inicialização

```typescript
// Em App.tsx ou no provider de auth, antes de qualquer request autenticado:
async function wakeUpRender() {
  try {
    const res = await fetch(`${BASE}/api/ping`);
    const data = await res.json();
    console.log("API alive:", data.build);
  } catch {
    // ignorar — retry automático pelo React Query
  }
}

// Chamar assim que o app monta (não precisa de auth):
useEffect(() => { wakeUpRender(); }, []);
```

**UX para cold start:** Mostrar skeleton loader ou spinner com mensagem "Conectando ao servidor..." se a primeira request demorar mais de 3s.

---

## Telas Prioritárias

### 1. Dashboard / Visão Mensal
- Seletor de mês (← mês → , botão "Hoje")
- Cards: Receitas / Despesas / Saldo do Mês / Saldo na Conta
- Lista de entries filtrada por `monthKey` com grouping por categoria ou data
- Toggle isPaid por entry (PATCH /api/entries/{id}/paid)
- FAB para adicionar entry

### 2. Lançamentos
- Lista paginada ou virtualizada (789+ items)
- Filtros: categoria, kind, isPaid, período
- Ações: editar, excluir, marcar pago
- Suporte a todos os 6 kinds

### 3. Categorias
- Grid com ícone + nome + tipo + budget
- CRUD completo
- Corrigir type='?' das 10 sintéticas

### 4. Cartões
- Card visual com gradiente por cor, bandeira, final
- Mostrar fatura corrente (entries credito_* do billingMonth ativo)
- Botão "Pagar fatura" → criar entry pagamento_fatura

### 5. Contas Bancárias
- Lista com saldo atual
- Botão ajustar saldo manual
- Histórico de movimentações

### 6. Recorrências
- Lista de contratos ativos/inativos
- Cancelar recorrência
- Ver entries vinculadas

### 7. Investimentos
- Total investido vs. valor atual
- Lista com atualização de currentValue
- Tipos: fundo, renda_fixa, etc.

### 8. Holerites
- Accordion por competência
- Detalhe: todos campos financeiros + extras/descontos
- Cálculo: totalProventos - totalDescontos = liquido

### 9. Metas
- Progresso visual (barra)
- Adicionar aporte
- Status: on-track / at-risk / great / completed

---

## Tratamento de Erros

```typescript
// Formatos de erro da API:
// 401: { error: "UNAUTHORIZED", message: "Token ausente ou inválido..." }
// 400: { status: 400, message: "...", violations: [{field, message}] }
// 500: { status: 500, message: "Erro interno", detail: "...", timestamp }
// 403: (apenas /import/bypass com key errada) — sem body

// React Query onError global:
queryClient.setDefaultOptions({
  mutations: {
    onError: (error) => {
      if (error instanceof ApiError && error.status === 401) {
        // redirecionar para login
      } else {
        toast.error(error.message);
      }
    },
  },
});
```

---

## Configuração do Usuário (app_config)

> ⚠️ **Atenção:** A tabela `app_config` não foi populada pelo import. O endpoint ainda não existe no backend. Por enquanto, armazenar `currentMonthKey` e preferências no localStorage ou Zustand persist.

Dados do usuário importado:
- `userId`: `DIwOPWkwHcdn0ecdXF3uUro24kE2`
- `userName`: `Wellington Sousa`
- `currentMonthKey`: `2026-06`
- `currency`: `BRL`

---

## Checklist de Implementação

### Fase 1 — Infraestrutura (1-2 dias)
- [ ] Instalar React Query v5: `npm i @tanstack/react-query`
- [ ] Criar `src/services/api.ts` com todos os endpoints
- [ ] Criar `src/services/auth.ts` com Firebase + interceptor token
- [ ] Wake-up call no App.tsx
- [ ] QueryClientProvider no root
- [ ] Zustand UI store (apenas estado de UI: activeMonth, modals, loadings)

### Fase 2 — Dados Read-Only (2-3 dias)
- [ ] `useCategories()` hook
- [ ] `useBanks()` hook
- [ ] `useCards()` hook
- [ ] `useEntries(monthKey)` hook
- [ ] `useRecurrences()` hook
- [ ] `useInvestments()` hook
- [ ] `usePayslips()` hook
- [ ] Exibir dados nas telas existentes substituindo dados do Zustand

### Fase 3 — CRUD Mutations (3-4 dias)
- [ ] Create/Update/Delete entries (invalidate cache após mutação)
- [ ] Create/Update/Delete categories
- [ ] Create/Update/Delete banks + updateBalance
- [ ] Create/Update/Delete cards
- [ ] Create/Update/Delete recurrences + cancel
- [ ] Create/Update/Delete investments
- [ ] Save/Delete payslips
- [ ] Create/Update/Delete bills + pay
- [ ] Create/Update/Delete goals + addProgress

### Fase 4 — Polimento (2-3 dias)
- [ ] Loading states / skeletons
- [ ] Error boundaries
- [ ] Cold start UX (spinner + mensagem)
- [ ] Corrigir type='?' das 10 categorias sintéticas via PUT
- [ ] Remover endpoint `/api/import/bypass` do backend

---

## Notas de Deploy

- **CORS**: Backend está configurado para aceitar qualquer origem (`*`) em dev. Em produção, configurar para o domínio do frontend.
- **Environment vars:** Backend no Render usa env vars para DB, Firebase, etc. Nunca commitar `.env` com secrets.
- **Firebase Config (frontend):** Usar variáveis de ambiente Vite (`VITE_FIREBASE_*`).
- **Swagger UI:** `https://financeira-moreira-api.onrender.com/swagger-ui/index.html` — útil para testar endpoints durante desenvolvimento.
