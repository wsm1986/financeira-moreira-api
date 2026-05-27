-- schema/02-validate.sql
-- Queries de validação pós-migração — Financeira Moreira
-- Substitua :user_id pelo Firebase UID do usuário

-- ─── Contagem de registros por tabela ────────────────────────────────
SELECT
  (SELECT COUNT(*) FROM users       WHERE uid      = :'user_id')                       AS users,
  (SELECT COUNT(*) FROM categories  WHERE user_uid = :'user_id')                       AS categories,
  (SELECT COUNT(*) FROM banks       WHERE user_uid = :'user_id' AND deleted_at IS NULL) AS banks,
  (SELECT COUNT(*) FROM credit_cards WHERE user_uid = :'user_id' AND deleted_at IS NULL) AS credit_cards,
  (SELECT COUNT(*) FROM entries      WHERE user_uid = :'user_id' AND deleted_at IS NULL) AS entries,
  (SELECT COUNT(*) FROM recurrences  WHERE user_uid = :'user_id' AND deleted_at IS NULL) AS recurrences,
  (SELECT COUNT(*) FROM bills        WHERE user_uid = :'user_id' AND deleted_at IS NULL) AS bills,
  (SELECT COUNT(*) FROM investments  WHERE user_uid = :'user_id' AND deleted_at IS NULL) AS investments,
  (SELECT COUNT(*) FROM goals        WHERE user_uid = :'user_id' AND deleted_at IS NULL) AS goals,
  (SELECT COUNT(*) FROM payslips     WHERE user_uid = :'user_id' AND deleted_at IS NULL) AS payslips;

-- ─── Entries órfãs (category_id sem correspondência) ─────────────────
SELECT e.id, e.name, e.category_id
FROM entries e
WHERE e.user_uid = :'user_id'
  AND e.category_id NOT IN (SELECT id FROM categories WHERE user_uid = :'user_id')
  AND e.deleted_at IS NULL
LIMIT 20;

-- ─── Bills órfãs ─────────────────────────────────────────────────────
SELECT b.id, b.name, b.category_id
FROM bills b
WHERE b.user_uid = :'user_id'
  AND b.category_id NOT IN (SELECT id FROM categories WHERE user_uid = :'user_id')
  AND b.deleted_at IS NULL
LIMIT 20;

-- ─── Recurrences órfãs ───────────────────────────────────────────────
SELECT r.id, r.name, r.category_id
FROM recurrences r
WHERE r.user_uid = :'user_id'
  AND r.category_id NOT IN (SELECT id FROM categories WHERE user_uid = :'user_id')
  AND r.deleted_at IS NULL
LIMIT 20;

-- ─── Entries com account_id inválido ─────────────────────────────────
SELECT e.id, e.name, e.account_id
FROM entries e
WHERE e.user_uid = :'user_id'
  AND e.account_id IS NOT NULL
  AND e.account_id NOT IN (SELECT id FROM banks WHERE user_uid = :'user_id')
LIMIT 20;

-- ─── Entries com card_id inválido ────────────────────────────────────
SELECT e.id, e.name, e.card_id
FROM entries e
WHERE e.user_uid = :'user_id'
  AND e.card_id IS NOT NULL
  AND e.card_id NOT IN (SELECT id FROM credit_cards WHERE user_uid = :'user_id')
LIMIT 20;

-- ─── Consistência monetária por mês ──────────────────────────────────
SELECT
  month_key,
  kind,
  COUNT(*)       AS qty,
  SUM(amount)    AS total
FROM entries
WHERE user_uid   = :'user_id'
  AND deleted_at IS NULL
GROUP BY month_key, kind
ORDER BY month_key DESC, kind;

-- ─── Categorias com lançamentos (quantidade) ─────────────────────────
SELECT
  c.name,
  COUNT(e.id) AS total_entries,
  SUM(e.amount) AS total_amount
FROM categories c
LEFT JOIN entries e ON e.category_id = c.id AND e.deleted_at IS NULL
WHERE c.user_uid = :'user_id'
GROUP BY c.id, c.name
ORDER BY total_entries DESC;
