/**
 * migrate-firebase-to-postgres.ts
 * Migrador Firebase/Zustand JSON → PostgreSQL — Financeira Moreira
 *
 * Uso:
 *   npx ts-node migrate-firebase-to-postgres.ts <firebase-export.json> <firebase-uid>
 *
 * Variáveis de ambiente:
 *   DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASS
 *
 * Como obter o firebase-export.json:
 *   No portal, vá em Configurações → Dados → Exportar JSON
 *   O arquivo gerado tem o formato do estado Zustand (salvo no Firestore)
 */

import * as fs   from 'fs'
import * as path from 'path'
import { Pool, PoolClient } from 'pg'

// ─── Tipos (espelho do frontend TypeScript) ───────────────────────────────────

interface FirebaseExport {
  appVersion?: string
  version?: number
  config: {
    currentMonthKey: string
    defaultAccountId?: string
    defaultCardId?:   string
    currency:         string
    userName:         string
    lastBackupAt?:    string
  }
  entries: FbEntry[]
  cards:   FbCard[]
  banks:   FbBank[]
  categories:  FbCategory[]
  investments: FbInvestment[]
  goals:       FbGoal[]
  recurrences: FbRecurrence[]
  payslips:    FbPayslip[]
  bills:       FbBill[]
  trash?:      unknown[]
}

interface FbEntry {
  id: string; monthKey: string; kind: string; name: string; category: string
  amount: number; date: string; icon: string
  accountId?: string; installmentTotal?: number; installmentCurrent?: number
  installmentGroupId?: string; recurrenceId?: string; recurrenceMonths?: number
  cardId?: string; billingMonth?: string; invoiceRef?: string; toAccountId?: string
  isPaid?: boolean; isReconciled?: boolean; notes?: string; tags?: string[]
}
interface FbCard {
  id: string; name: string; brand: string; lastDigits: string; limit: number
  closingDay: number; dueDay: number; color: string; icon: string; bankId?: string
}
interface FbBank {
  id: string; name: string; type: string; balance: number; color: string; icon: string
}
interface FbCategory {
  id: string; name: string; icon: string; budget: number; color: string
  type: string; nature?: string
}
interface FbInvestment {
  id: string; name: string; type: string; amount: number; currentValue: number
  rate?: number; maturity?: string; bankId?: string; isEmergencyReserve?: boolean
  icon: string; color: string
}
interface FbGoal {
  id: string; name: string; icon: string; targetAmount: number; currentAmount: number
  deadline: string; color: string; status: string; notes?: string
}
interface FbRecurrence {
  id: string; name: string; icon: string; category: string; kind: string
  amount: number; cardId?: string; accountId?: string; startMonth: string
  endMonth?: string; months: number; active: boolean
}
interface FbPayslip {
  id: string; competencia: string; salarioBase: number
  extras?: Array<{ descricao: string; valor: number }>
  inss: number; irrf: number; pensaoAlimenticia: number; emprestimoConsignado: number
  assistenciaMedica: number; coparticipacao: number; pgbl: number; seguroVida: number
  valeTransporte: number; valeRefeicao: number
  outrosDescontos?: Array<{ descricao: string; valor: number }>
  fgts: number; totalProventos: number; totalDescontos: number
  liquido: number; observacoes?: string
}
interface FbBill {
  id: string; name: string; amount: number; dueDate: string; category: string
  paid: boolean; paidDate?: string; bankId?: string; notes?: string; type?: string
}

interface MigrationReport {
  success:   boolean
  timestamp: string
  userId:    string
  summary: Record<string, number>
  errors:   Array<{ entity: string; id: string; error: string }>
  warnings: Array<{ entity: string; id: string; message: string }>
  categoryMappings: Record<string, string>
  bankMappings:     Record<string, string>
  cardMappings:     Record<string, string>
}

// ─── Migrator ────────────────────────────────────────────────────────────────

class FirebaseToPostgresMigrator {
  private pool:   Pool
  private client: PoolClient | null = null
  private report: MigrationReport
  private data:   FirebaseExport | null = null

  // firebase id / name → db uuid
  private catNameMap = new Map<string, string>()
  private bankMap    = new Map<string, string>()
  private cardMap    = new Map<string, string>()

  constructor(
    private userId: string,
    private dbConfig: { host: string; port: number; database: string; user: string; password: string }
  ) {
    this.pool = new Pool(dbConfig)
    this.report = {
      success: false, timestamp: new Date().toISOString(), userId,
      summary: { users:0, categories:0, banks:0, creditCards:0, entries:0,
                 recurrences:0, bills:0, investments:0, goals:0, payslips:0, errors:0 },
      errors: [], warnings: [], categoryMappings: {}, bankMappings: {}, cardMappings: {},
    }
  }

  async migrate(exportPath: string): Promise<MigrationReport> {
    try {
      log('Iniciando migração Firebase → PostgreSQL')
      log(`Arquivo : ${exportPath}`)
      log(`UserID  : ${this.userId}`)

      this.data = this.load(exportPath)
      this.validate()

      this.client = await this.pool.connect()
      log('✓ Conectado ao PostgreSQL')

      await this.client.query('BEGIN')

      await this.migrateUsers()
      await this.migrateCategories()
      await this.migrateBanks()
      await this.migrateCreditCards()
      await this.migrateEntries()
      await this.migrateRecurrences()
      await this.migrateBills()
      await this.migrateInvestments()
      await this.migrateGoals()
      await this.migratePayslips()
      await this.migrateAppConfig()
      await this.validateIntegrity()

      await this.client.query('COMMIT')
      log('✓ COMMIT — migração concluída')
      this.report.success = true
    } catch (err) {
      log(`✗ Erro: ${err}`)
      if (this.client) {
        await this.client.query('ROLLBACK').catch(() => {})
        log('✓ ROLLBACK — nenhum dado foi gravado')
      }
      throw err
    } finally {
      this.client?.release()
      await this.pool.end()
    }
    return this.report
  }

  // ── Helpers ────────────────────────────────────────────────────────

  private load(filePath: string): FirebaseExport {
    const raw = fs.readFileSync(filePath, 'utf-8')
    const json = JSON.parse(raw)
    if (!json.entries || !json.config) throw new Error('JSON inválido: faltam entries ou config')
    return json as FirebaseExport
  }

  private validate() {
    const d = this.data!
    const fields = ['entries','cards','banks','categories','investments','goals','recurrences','payslips','bills']
    for (const f of fields) {
      if (!Array.isArray(d[f as keyof FirebaseExport])) {
        throw new Error(`Campo obrigatório ausente ou inválido: ${f}`)
      }
    }
    log(`✓ Schema validado — ${d.entries.length} lançamentos, ${d.categories.length} categorias`)
  }

  private err(entity: string, id: string, e: unknown) {
    const msg = e instanceof Error ? e.message : String(e)
    this.report.errors.push({ entity, id, error: msg })
    this.report.summary.errors++
    log(`  ✗ [${entity}] ${id}: ${msg}`)
  }

  private warn(entity: string, id: string, message: string) {
    this.report.warnings.push({ entity, id, message })
    log(`  ⚠ [${entity}] ${id}: ${message}`)
  }

  private uuid(): string {
    return crypto.randomUUID()
  }

  // ── Entidades ──────────────────────────────────────────────────────

  private async migrateUsers() {
    log('[Users] Verificando usuário...')
    try {
      const existing = await this.client!.query('SELECT uid FROM users WHERE uid = $1', [this.userId])
      if (existing.rows.length > 0) { log('  → já existe, pulando'); return }

      await this.client!.query(
        `INSERT INTO users (uid, email, name, currency) VALUES ($1, $2, $3, 'BRL')`,
        [this.userId, `${this.userId}@migrated.local`, this.data!.config.userName || 'Usuário']
      )
      this.report.summary.users = 1
      log('  ✓ Usuário criado')
    } catch (e) { this.err('user', this.userId, e) }
  }

  private async migrateCategories() {
    log(`[Categories] Migrando ${this.data!.categories.length} categorias...`)
    for (const c of this.data!.categories) {
      try {
        const id = this.uuid()
        await this.client!.query(
          `INSERT INTO categories (id, user_uid, name, icon, budget, color, type, nature)
           VALUES ($1,$2,$3,$4,$5,$6,$7,$8)
           ON CONFLICT (user_uid, name) DO UPDATE SET icon=$4, budget=$5, color=$6, type=$7, nature=$8
           RETURNING id`,
          [id, this.userId, c.name, c.icon||'📁', c.budget||0, c.color||'#CCCCCC', c.type||'both', c.nature||null]
        )
        this.catNameMap.set(c.name, id)
        this.report.categoryMappings[c.name] = id
        this.report.summary.categories++
      } catch (e) { this.err('category', c.id, e) }
    }
    log(`  ✓ ${this.report.summary.categories} categorias`)
  }

  private async migrateBanks() {
    log(`[Banks] Migrando ${this.data!.banks.length} contas...`)
    for (const b of this.data!.banks) {
      try {
        const id = this.uuid()
        await this.client!.query(
          `INSERT INTO banks (id, user_uid, name, type, balance, color, icon)
           VALUES ($1,$2,$3,$4,$5,$6,$7)`,
          [id, this.userId, b.name, b.type||'corrente', b.balance||0, b.color||'#CCCCCC', b.icon||'🏦']
        )
        this.bankMap.set(b.id, id)
        this.report.bankMappings[b.id] = id
        this.report.summary.banks++
      } catch (e) { this.err('bank', b.id, e) }
    }
    log(`  ✓ ${this.report.summary.banks} contas`)
  }

  private async migrateCreditCards() {
    log(`[CreditCards] Migrando ${this.data!.cards.length} cartões...`)
    for (const c of this.data!.cards) {
      try {
        const id     = this.uuid()
        const bankId = c.bankId ? this.bankMap.get(c.bankId) : null
        await this.client!.query(
          `INSERT INTO credit_cards (id, user_uid, name, brand, last_digits, card_limit, closing_day, due_day, color, icon, bank_id)
           VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11)`,
          [id, this.userId, c.name, c.brand||'mastercard', c.lastDigits||'0000',
           c.limit||0, c.closingDay||1, c.dueDay||10, c.color||'#CCCCCC', c.icon||'💳', bankId||null]
        )
        this.cardMap.set(c.id, id)
        this.report.cardMappings[c.id] = id
        this.report.summary.creditCards++
      } catch (e) { this.err('credit_card', c.id, e) }
    }
    log(`  ✓ ${this.report.summary.creditCards} cartões`)
  }

  private async migrateEntries() {
    log(`[Entries] Migrando ${this.data!.entries.length} lançamentos...`)
    for (const e of this.data!.entries) {
      try {
        const catId = this.catNameMap.get(e.category)
        if (!catId) { this.warn('entry', e.id, `categoria não encontrada: "${e.category}"`); continue }

        const id          = this.uuid()
        const accountId   = e.accountId   ? this.bankMap.get(e.accountId)   : null
        const cardId      = e.cardId      ? this.cardMap.get(e.cardId)      : null
        const toAccountId = e.toAccountId ? this.bankMap.get(e.toAccountId) : null

        await this.client!.query(
          `INSERT INTO entries (
             id, user_uid, month_key, kind, name, category_id, amount, entry_date, icon,
             account_id, installment_total, installment_current, installment_group_id,
             recurrence_id, recurrence_months, card_id, billing_month, invoice_ref,
             to_account_id, is_paid, is_reconciled, notes, tags
           ) VALUES (
             $1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$14,$15,$16,$17,$18,$19,$20,$21,$22,$23
           )`,
          [
            id, this.userId, e.monthKey, e.kind, e.name, catId, e.amount, e.date, e.icon||'💰',
            accountId, e.installmentTotal||null, e.installmentCurrent||null, e.installmentGroupId||null,
            e.recurrenceId||null, e.recurrenceMonths||null, cardId, e.billingMonth||null, e.invoiceRef||null,
            toAccountId, e.isPaid||false, e.isReconciled||false, e.notes||null,
            e.tags?.length ? e.tags : null,
          ]
        )
        this.report.summary.entries++
      } catch (e2) { this.err('entry', (e as FbEntry).id, e2) }
    }
    log(`  ✓ ${this.report.summary.entries} lançamentos`)
  }

  private async migrateRecurrences() {
    log(`[Recurrences] Migrando ${this.data!.recurrences.length} recorrências...`)
    for (const r of this.data!.recurrences) {
      try {
        const catId = this.catNameMap.get(r.category)
        if (!catId) { this.warn('recurrence', r.id, `categoria não encontrada: "${r.category}"`); continue }

        const cardId    = r.cardId    ? this.cardMap.get(r.cardId)    : null
        const accountId = r.accountId ? this.bankMap.get(r.accountId) : null

        await this.client!.query(
          `INSERT INTO recurrences (id, user_uid, name, icon, category_id, kind, amount, card_id, account_id, start_month, end_month, months, active)
           VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13)`,
          [r.id, this.userId, r.name, r.icon||'🔄', catId, r.kind, r.amount,
           cardId, accountId, r.startMonth, r.endMonth||null, r.months, r.active]
        )
        this.report.summary.recurrences++
      } catch (e) { this.err('recurrence', r.id, e) }
    }
    log(`  ✓ ${this.report.summary.recurrences} recorrências`)
  }

  private async migrateBills() {
    log(`[Bills] Migrando ${this.data!.bills.length} contas a pagar...`)
    for (const b of this.data!.bills) {
      try {
        const catId  = this.catNameMap.get(b.category)
        if (!catId) { this.warn('bill', b.id, `categoria não encontrada: "${b.category}"`); continue }

        const bankId = b.bankId ? this.bankMap.get(b.bankId) : null

        await this.client!.query(
          `INSERT INTO bills (id, user_uid, name, amount, due_date, category_id, paid, paid_date, bank_id, notes, type)
           VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11)`,
          [b.id, this.userId, b.name, b.amount, b.dueDate, catId,
           b.paid||false, b.paidDate||null, bankId, b.notes||null, b.type||'pagar']
        )
        this.report.summary.bills++
      } catch (e) { this.err('bill', b.id, e) }
    }
    log(`  ✓ ${this.report.summary.bills} contas`)
  }

  private async migrateInvestments() {
    log(`[Investments] Migrando ${this.data!.investments.length} investimentos...`)
    for (const inv of this.data!.investments) {
      try {
        const bankId = inv.bankId ? this.bankMap.get(inv.bankId) : null
        await this.client!.query(
          `INSERT INTO investments (id, user_uid, name, type, amount, current_value, rate, maturity, bank_id, is_emergency_reserve, icon, color)
           VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12)`,
          [inv.id, this.userId, inv.name, inv.type, inv.amount, inv.currentValue,
           inv.rate||null, inv.maturity||null, bankId, inv.isEmergencyReserve||false,
           inv.icon||'📊', inv.color||'#CCCCCC']
        )
        this.report.summary.investments++
      } catch (e) { this.err('investment', inv.id, e) }
    }
    log(`  ✓ ${this.report.summary.investments} investimentos`)
  }

  private async migrateGoals() {
    log(`[Goals] Migrando ${this.data!.goals.length} metas...`)
    for (const g of this.data!.goals) {
      try {
        await this.client!.query(
          `INSERT INTO goals (id, user_uid, name, icon, target_amount, current_amount, deadline, color, status, notes)
           VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10)`,
          [g.id, this.userId, g.name, g.icon||'🎯', g.targetAmount,
           g.currentAmount||0, g.deadline, g.color||'#CCCCCC', g.status||'on-track', g.notes||null]
        )
        this.report.summary.goals++
      } catch (e) { this.err('goal', g.id, e) }
    }
    log(`  ✓ ${this.report.summary.goals} metas`)
  }

  private async migratePayslips() {
    log(`[Payslips] Migrando ${this.data!.payslips.length} holerites...`)
    for (const p of this.data!.payslips) {
      try {
        const id = this.uuid()
        await this.client!.query(
          `INSERT INTO payslips (
             id, user_uid, competencia, salario_base, inss, irrf, pensao_alimenticia,
             emprestimo_consignado, assistencia_medica, coparticipacao, pgbl, seguro_vida,
             vale_transporte, vale_refeicao, fgts, total_proventos, total_descontos, liquido, observacoes
           ) VALUES ($1,$2,$3,$4,$5,$6,$7,$8,$9,$10,$11,$12,$13,$14,$15,$16,$17,$18,$19)`,
          [
            id, this.userId, p.competencia, p.salarioBase, p.inss, p.irrf,
            p.pensaoAlimenticia, p.emprestimoConsignado, p.assistenciaMedica,
            p.coparticipacao, p.pgbl, p.seguroVida, p.valeTransporte, p.valeRefeicao,
            p.fgts, p.totalProventos, p.totalDescontos, p.liquido, p.observacoes||null,
          ]
        )
        // payslip_items: extras e outrosDescontos
        for (const item of (p.extras || [])) {
          await this.client!.query(
            `INSERT INTO payslip_items (payslip_id, type, descricao, valor) VALUES ($1,'extra',$2,$3)`,
            [id, item.descricao, item.valor]
          )
        }
        for (const item of (p.outrosDescontos || [])) {
          await this.client!.query(
            `INSERT INTO payslip_items (payslip_id, type, descricao, valor) VALUES ($1,'desconto',$2,$3)`,
            [id, item.descricao, item.valor]
          )
        }
        this.report.summary.payslips++
      } catch (e) { this.err('payslip', p.id, e) }
    }
    log(`  ✓ ${this.report.summary.payslips} holerites`)
  }

  private async migrateAppConfig() {
    log('[AppConfig] Migrando configurações...')
    try {
      const cfg           = this.data!.config
      const defaultAccId  = cfg.defaultAccountId ? this.bankMap.get(cfg.defaultAccountId) : null
      const defaultCardId = cfg.defaultCardId    ? this.cardMap.get(cfg.defaultCardId)    : null

      await this.client!.query(
        `INSERT INTO app_config (user_uid, current_month_key, default_account_id, default_card_id, currency, user_name, last_backup_at)
         VALUES ($1,$2,$3,$4,$5,$6,$7)
         ON CONFLICT (user_uid) DO UPDATE
           SET current_month_key=$2, default_account_id=$3, default_card_id=$4,
               currency=$5, user_name=$6, last_backup_at=$7, updated_at=NOW()`,
        [this.userId, cfg.currentMonthKey, defaultAccId, defaultCardId,
         cfg.currency||'BRL', cfg.userName, cfg.lastBackupAt ? new Date(cfg.lastBackupAt) : null]
      )
      log('  ✓ AppConfig migrado')
    } catch (e) { this.err('app_config', this.userId, e) }
  }

  private async validateIntegrity() {
    log('[Validate] Verificando integridade...')
    const checks = [
      { name: 'entries-cat-orphan',  sql: `SELECT COUNT(*) FROM entries WHERE user_uid=$1 AND deleted_at IS NULL AND category_id NOT IN (SELECT id FROM categories WHERE user_uid=$1)` },
      { name: 'bills-cat-orphan',    sql: `SELECT COUNT(*) FROM bills   WHERE user_uid=$1 AND deleted_at IS NULL AND category_id NOT IN (SELECT id FROM categories WHERE user_uid=$1)` },
      { name: 'rec-cat-orphan',      sql: `SELECT COUNT(*) FROM recurrences WHERE user_uid=$1 AND deleted_at IS NULL AND category_id NOT IN (SELECT id FROM categories WHERE user_uid=$1)` },
    ]
    for (const c of checks) {
      const r = await this.client!.query(c.sql, [this.userId])
      const n = parseInt(r.rows[0].count)
      if (n > 0) this.warn('validate', c.name, `${n} registros órfãos detectados`)
    }
    log('  ✓ Validação concluída')
  }
}

// ─── Utilitário ───────────────────────────────────────────────────────────────

function log(msg: string) {
  console.log(`[${new Date().toISOString().slice(11,19)}] ${msg}`)
}

// ─── Execução ─────────────────────────────────────────────────────────────────

async function main() {
  const exportPath = process.argv[2]
  const userId     = process.argv[3]

  if (!exportPath || !userId) {
    console.error('Uso: npx ts-node migrate-firebase-to-postgres.ts <firebase-export.json> <firebase-uid>')
    process.exit(1)
  }

  if (!fs.existsSync(exportPath)) {
    console.error(`Arquivo não encontrado: ${exportPath}`)
    process.exit(1)
  }

  const dbConfig = {
    host:     process.env.DB_HOST     || 'localhost',
    port:     parseInt(process.env.DB_PORT || '5432'),
    database: process.env.DB_NAME     || 'financeira',
    user:     process.env.DB_USER     || 'postgres',
    password: process.env.DB_PASS     || '',
  }

  const migrator = new FirebaseToPostgresMigrator(userId, dbConfig)

  try {
    const report = await migrator.migrate(exportPath)

    console.log('\n' + '═'.repeat(60))
    console.log('RELATÓRIO DE MIGRAÇÃO')
    console.log('═'.repeat(60))
    console.log(JSON.stringify(report, null, 2))

    const outFile = path.join(process.cwd(), `migration-report-${Date.now()}.json`)
    fs.writeFileSync(outFile, JSON.stringify(report, null, 2))
    console.log(`\nRelatório salvo em: ${outFile}`)

    const hasErrors = report.summary.errors > 0
    process.exit(report.success && !hasErrors ? 0 : 1)
  } catch (err) {
    console.error('Erro fatal na migração:', err)
    process.exit(1)
  }
}

main()
