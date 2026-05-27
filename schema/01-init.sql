-- schema/01-init.sql
-- Schema completo PostgreSQL — Financeira Moreira
-- Gerado em: 2026-05-27
-- Executar apenas uma vez em banco limpo (dev/staging/prod)

BEGIN;

-- ─── users ───────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS users (
  uid        VARCHAR(255) PRIMARY KEY,
  email      VARCHAR(255) NOT NULL UNIQUE,
  name       VARCHAR(255) NOT NULL,
  currency   VARCHAR(3)   NOT NULL DEFAULT 'BRL',
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT valid_currency CHECK (currency IN ('BRL'))
);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- ─── categories ──────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS categories (
  id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  user_uid   VARCHAR(255) NOT NULL,
  name       VARCHAR(255) NOT NULL,
  icon       VARCHAR(10)  NOT NULL DEFAULT '📁',
  budget     NUMERIC(19,2) NOT NULL DEFAULT 0,
  color      VARCHAR(7)   NOT NULL DEFAULT '#CCCCCC',
  type       VARCHAR(20)  NOT NULL CHECK (type IN ('expense', 'income', 'both')),
  nature     VARCHAR(20)  CHECK (nature IN ('essencial', 'desejo', 'investimento')),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_uid) REFERENCES users(uid) ON DELETE CASCADE,
  UNIQUE(user_uid, name)
);
CREATE INDEX IF NOT EXISTS idx_categories_user_uid ON categories(user_uid);
CREATE INDEX IF NOT EXISTS idx_categories_name     ON categories(name);

-- ─── banks ───────────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS banks (
  id         UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  user_uid   VARCHAR(255) NOT NULL,
  name       VARCHAR(255) NOT NULL,
  type       VARCHAR(20)  NOT NULL CHECK (type IN ('corrente', 'poupanca', 'investimento', 'digital')),
  balance    NUMERIC(19,2) NOT NULL DEFAULT 0,
  color      VARCHAR(7)   NOT NULL DEFAULT '#CCCCCC',
  icon       VARCHAR(10)  NOT NULL DEFAULT '🏦',
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP WITH TIME ZONE,
  FOREIGN KEY (user_uid) REFERENCES users(uid) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_banks_user_uid   ON banks(user_uid);
CREATE INDEX IF NOT EXISTS idx_banks_deleted_at ON banks(deleted_at);

-- ─── credit_cards ────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS credit_cards (
  id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  user_uid    VARCHAR(255) NOT NULL,
  name        VARCHAR(255) NOT NULL,
  brand       VARCHAR(20)  NOT NULL CHECK (brand IN ('visa', 'mastercard', 'elo', 'amex', 'hipercard')),
  last_digits VARCHAR(4)   NOT NULL,
  card_limit  NUMERIC(19,2) NOT NULL,
  closing_day INTEGER      NOT NULL CHECK (closing_day BETWEEN 1 AND 28),
  due_day     INTEGER      NOT NULL CHECK (due_day BETWEEN 1 AND 28),
  color       VARCHAR(7)   NOT NULL DEFAULT '#CCCCCC',
  icon        VARCHAR(10)  NOT NULL DEFAULT '💳',
  bank_id     UUID,
  created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at  TIMESTAMP WITH TIME ZONE,
  FOREIGN KEY (user_uid) REFERENCES users(uid)   ON DELETE CASCADE,
  FOREIGN KEY (bank_id)  REFERENCES banks(id)    ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_credit_cards_user_uid   ON credit_cards(user_uid);
CREATE INDEX IF NOT EXISTS idx_credit_cards_bank_id    ON credit_cards(bank_id);
CREATE INDEX IF NOT EXISTS idx_credit_cards_deleted_at ON credit_cards(deleted_at);

-- ─── entries (lançamentos) ───────────────────────────────────────────
CREATE TABLE IF NOT EXISTS entries (
  id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  user_uid             VARCHAR(255) NOT NULL,
  month_key            VARCHAR(7)   NOT NULL CHECK (month_key ~ '^\d{4}-\d{2}$'),
  kind                 VARCHAR(30)  NOT NULL CHECK (kind IN (
    'receita', 'debito_avista', 'debito_recorrente',
    'credito_avista', 'credito_parcelado', 'recorrente_cartao',
    'pagamento_fatura', 'transferencia'
  )),
  name                 VARCHAR(255) NOT NULL,
  category_id          UUID         NOT NULL,          -- FK → categories (não mais string)
  amount               NUMERIC(19,2) NOT NULL CHECK (amount > 0),
  entry_date           DATE         NOT NULL,
  icon                 VARCHAR(10)  NOT NULL DEFAULT '💰',

  -- débito/crédito à vista
  account_id           UUID,

  -- parcelado
  installment_total    INTEGER CHECK (installment_total IS NULL OR installment_total > 0),
  installment_current  INTEGER CHECK (installment_current IS NULL OR installment_current > 0),
  installment_group_id UUID,

  -- recorrente
  recurrence_id        UUID,
  recurrence_months    INTEGER CHECK (recurrence_months IS NULL OR recurrence_months > 0),

  -- cartão de crédito
  card_id              UUID,
  billing_month        VARCHAR(7) CHECK (billing_month IS NULL OR billing_month ~ '^\d{4}-\d{2}$'),

  -- pagamento de fatura
  invoice_ref          VARCHAR(7) CHECK (invoice_ref IS NULL OR invoice_ref ~ '^\d{4}-\d{2}$'),

  -- transferência entre contas
  to_account_id        UUID,

  -- flags
  is_paid              BOOLEAN NOT NULL DEFAULT FALSE,
  is_reconciled        BOOLEAN NOT NULL DEFAULT FALSE,
  notes                TEXT,
  tags                 TEXT[],

  created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at  TIMESTAMP WITH TIME ZONE,

  FOREIGN KEY (user_uid)      REFERENCES users(uid)         ON DELETE CASCADE,
  FOREIGN KEY (category_id)   REFERENCES categories(id)     ON DELETE RESTRICT,
  FOREIGN KEY (account_id)    REFERENCES banks(id)          ON DELETE SET NULL,
  FOREIGN KEY (card_id)       REFERENCES credit_cards(id)   ON DELETE SET NULL,
  FOREIGN KEY (to_account_id) REFERENCES banks(id)          ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_entries_user_uid            ON entries(user_uid);
CREATE INDEX IF NOT EXISTS idx_entries_month_key           ON entries(month_key);
CREATE INDEX IF NOT EXISTS idx_entries_category_id         ON entries(category_id);
CREATE INDEX IF NOT EXISTS idx_entries_account_id          ON entries(account_id);
CREATE INDEX IF NOT EXISTS idx_entries_card_id             ON entries(card_id);
CREATE INDEX IF NOT EXISTS idx_entries_recurrence_id       ON entries(recurrence_id);
CREATE INDEX IF NOT EXISTS idx_entries_installment_group   ON entries(installment_group_id);
CREATE INDEX IF NOT EXISTS idx_entries_deleted_at          ON entries(deleted_at);
CREATE INDEX IF NOT EXISTS idx_entries_user_month          ON entries(user_uid, month_key);
CREATE INDEX IF NOT EXISTS idx_entries_is_paid             ON entries(is_paid);

-- ─── recurrences (contratos de recorrência) ──────────────────────────
CREATE TABLE IF NOT EXISTS recurrences (
  id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  user_uid    VARCHAR(255) NOT NULL,
  name        VARCHAR(255) NOT NULL,
  icon        VARCHAR(10)  NOT NULL DEFAULT '🔄',
  category_id UUID         NOT NULL,
  kind        VARCHAR(30)  NOT NULL CHECK (kind IN (
    'receita', 'debito_avista', 'debito_recorrente',
    'credito_avista', 'credito_parcelado', 'recorrente_cartao',
    'pagamento_fatura', 'transferencia'
  )),
  amount      NUMERIC(19,2) NOT NULL CHECK (amount > 0),
  card_id     UUID,
  account_id  UUID,
  start_month VARCHAR(7)   NOT NULL CHECK (start_month ~ '^\d{4}-\d{2}$'),
  end_month   VARCHAR(7)   CHECK (end_month IS NULL OR end_month ~ '^\d{4}-\d{2}$'),
  months      INTEGER      NOT NULL CHECK (months > 0),
  active      BOOLEAN      NOT NULL DEFAULT TRUE,
  created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at  TIMESTAMP WITH TIME ZONE,
  FOREIGN KEY (user_uid)    REFERENCES users(uid)       ON DELETE CASCADE,
  FOREIGN KEY (category_id) REFERENCES categories(id)   ON DELETE RESTRICT,
  FOREIGN KEY (card_id)     REFERENCES credit_cards(id) ON DELETE SET NULL,
  FOREIGN KEY (account_id)  REFERENCES banks(id)        ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_recurrences_user_uid   ON recurrences(user_uid);
CREATE INDEX IF NOT EXISTS idx_recurrences_active     ON recurrences(active);
CREATE INDEX IF NOT EXISTS idx_recurrences_deleted_at ON recurrences(deleted_at);

-- ─── bills (contas a pagar/receber) ──────────────────────────────────
CREATE TABLE IF NOT EXISTS bills (
  id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  user_uid    VARCHAR(255) NOT NULL,
  name        VARCHAR(255) NOT NULL,
  amount      NUMERIC(19,2) NOT NULL CHECK (amount > 0),
  due_date    DATE         NOT NULL,
  category_id UUID         NOT NULL,
  paid        BOOLEAN      NOT NULL DEFAULT FALSE,
  paid_date   DATE,
  bank_id     UUID,
  notes       TEXT,
  type        VARCHAR(20)  NOT NULL DEFAULT 'pagar' CHECK (type IN ('pagar', 'receber')),
  created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at  TIMESTAMP WITH TIME ZONE,
  FOREIGN KEY (user_uid)    REFERENCES users(uid)     ON DELETE CASCADE,
  FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT,
  FOREIGN KEY (bank_id)     REFERENCES banks(id)      ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_bills_user_uid   ON bills(user_uid);
CREATE INDEX IF NOT EXISTS idx_bills_due_date   ON bills(due_date);
CREATE INDEX IF NOT EXISTS idx_bills_paid       ON bills(paid);
CREATE INDEX IF NOT EXISTS idx_bills_deleted_at ON bills(deleted_at);

-- ─── investments ─────────────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS investments (
  id                   UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  user_uid             VARCHAR(255) NOT NULL,
  name                 VARCHAR(255) NOT NULL,
  type                 VARCHAR(30)  NOT NULL CHECK (type IN ('renda_fixa', 'renda_variavel', 'fundo', 'cripto', 'imovel', 'outro')),
  amount               NUMERIC(19,2) NOT NULL,
  current_value        NUMERIC(19,2) NOT NULL,
  rate                 NUMERIC(10,2),
  maturity             DATE,
  bank_id              UUID,
  is_emergency_reserve BOOLEAN NOT NULL DEFAULT FALSE,
  icon                 VARCHAR(10)  NOT NULL DEFAULT '📊',
  color                VARCHAR(7)   NOT NULL DEFAULT '#CCCCCC',
  created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at  TIMESTAMP WITH TIME ZONE,
  FOREIGN KEY (user_uid) REFERENCES users(uid) ON DELETE CASCADE,
  FOREIGN KEY (bank_id)  REFERENCES banks(id)  ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_investments_user_uid            ON investments(user_uid);
CREATE INDEX IF NOT EXISTS idx_investments_is_emergency_reserve ON investments(is_emergency_reserve);
CREATE INDEX IF NOT EXISTS idx_investments_deleted_at          ON investments(deleted_at);

-- ─── goals (metas financeiras) ───────────────────────────────────────
CREATE TABLE IF NOT EXISTS goals (
  id             UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  user_uid       VARCHAR(255) NOT NULL,
  name           VARCHAR(255) NOT NULL,
  icon           VARCHAR(10)  NOT NULL DEFAULT '🎯',
  target_amount  NUMERIC(19,2) NOT NULL,
  current_amount NUMERIC(19,2) NOT NULL DEFAULT 0,
  deadline       VARCHAR(7)   NOT NULL CHECK (deadline ~ '^\d{4}-\d{2}$'),
  color          VARCHAR(50)  NOT NULL DEFAULT '#CCCCCC',
  status         VARCHAR(20)  NOT NULL DEFAULT 'on-track' CHECK (status IN ('on-track', 'at-risk', 'great', 'completed')),
  notes          TEXT,
  created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at  TIMESTAMP WITH TIME ZONE,
  FOREIGN KEY (user_uid) REFERENCES users(uid) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_goals_user_uid   ON goals(user_uid);
CREATE INDEX IF NOT EXISTS idx_goals_status     ON goals(status);
CREATE INDEX IF NOT EXISTS idx_goals_deleted_at ON goals(deleted_at);

-- ─── payslips (holerites) ────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS payslips (
  id                    UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  user_uid              VARCHAR(255) NOT NULL,
  competencia           VARCHAR(7)   NOT NULL CHECK (competencia ~ '^\d{4}-\d{2}$'),
  salario_base          NUMERIC(19,2) NOT NULL,
  inss                  NUMERIC(19,2) NOT NULL DEFAULT 0,
  irrf                  NUMERIC(19,2) NOT NULL DEFAULT 0,
  pensao_alimenticia    NUMERIC(19,2) NOT NULL DEFAULT 0,
  emprestimo_consignado NUMERIC(19,2) NOT NULL DEFAULT 0,
  assistencia_medica    NUMERIC(19,2) NOT NULL DEFAULT 0,
  coparticipacao        NUMERIC(19,2) NOT NULL DEFAULT 0,
  pgbl                  NUMERIC(19,2) NOT NULL DEFAULT 0,
  seguro_vida           NUMERIC(19,2) NOT NULL DEFAULT 0,
  vale_transporte       NUMERIC(19,2) NOT NULL DEFAULT 0,
  vale_refeicao         NUMERIC(19,2) NOT NULL DEFAULT 0,
  fgts                  NUMERIC(19,2) NOT NULL DEFAULT 0,
  total_proventos       NUMERIC(19,2) NOT NULL,
  total_descontos       NUMERIC(19,2) NOT NULL,
  liquido               NUMERIC(19,2) NOT NULL,
  observacoes           TEXT,
  created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at  TIMESTAMP WITH TIME ZONE,
  FOREIGN KEY (user_uid) REFERENCES users(uid) ON DELETE CASCADE,
  UNIQUE(user_uid, competencia)
);
CREATE INDEX IF NOT EXISTS idx_payslips_user_uid   ON payslips(user_uid);
CREATE INDEX IF NOT EXISTS idx_payslips_competencia ON payslips(competencia);
CREATE INDEX IF NOT EXISTS idx_payslips_deleted_at ON payslips(deleted_at);

-- ─── payslip_items (extras e outros descontos livres) ─────────────────
CREATE TABLE IF NOT EXISTS payslip_items (
  id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
  payslip_id  UUID         NOT NULL,
  type        VARCHAR(20)  NOT NULL CHECK (type IN ('extra', 'desconto')),
  descricao   VARCHAR(255) NOT NULL,
  valor       NUMERIC(19,2) NOT NULL,
  FOREIGN KEY (payslip_id) REFERENCES payslips(id) ON DELETE CASCADE
);
CREATE INDEX IF NOT EXISTS idx_payslip_items_payslip_id ON payslip_items(payslip_id);

-- ─── app_config (configurações por usuário) ──────────────────────────
CREATE TABLE IF NOT EXISTS app_config (
  user_uid           VARCHAR(255) PRIMARY KEY,
  current_month_key  VARCHAR(7)   NOT NULL CHECK (current_month_key ~ '^\d{4}-\d{2}$'),
  default_account_id UUID,
  default_card_id    UUID,
  currency           VARCHAR(3)   NOT NULL DEFAULT 'BRL',
  user_name          VARCHAR(255),
  last_backup_at     TIMESTAMP WITH TIME ZONE,
  created_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_uid)           REFERENCES users(uid)       ON DELETE CASCADE,
  FOREIGN KEY (default_account_id) REFERENCES banks(id)        ON DELETE SET NULL,
  FOREIGN KEY (default_card_id)    REFERENCES credit_cards(id) ON DELETE SET NULL
);

COMMIT;
