# Endpoint: POST /api/scheduler/resumo

Aciona manualmente o resumo financeiro diário que normalmente roda às 08h (horário de Brasília).
O backend monta a mensagem e envia via WhatsApp usando o mesmo fluxo do agendamento automático.

---

## Contrato

```
POST /api/scheduler/resumo
Authorization: Bearer <firebase-id-token>
Content-Type: application/json
Body: (vazio)
```

### Respostas

| Status | Significado |
|--------|-------------|
| `200`  | Resumo enviado com sucesso |
| `200`  | `NOTIFICACAO_USER_UID` não configurado no backend — sem envio, sem erro |
| `500`  | Erro interno ao gerar ou enviar o resumo |

**Corpo 200 (sucesso):**
```json
{ "ok": true, "message": "Resumo diário acionado com sucesso" }
```

**Corpo 500 (erro):**
```json
{ "ok": false, "message": "Erro ao acionar resumo: <detalhe>" }
```

---

## Implementação no frontend

### 1. Adicionar função em `src/services/api.ts`

```ts
export async function acionarResumoDiario(): Promise<{ ok: boolean; message: string }> {
  return request<{ ok: boolean; message: string }>('/api/scheduler/resumo', {
    method: 'POST',
  })
}
```

> O `request()` já injeta o `Authorization: Bearer <token>` automaticamente.
> Não passar body — o endpoint não espera nenhum payload.

### 2. Adicionar tipo em `src/services/apiTypes.ts` (opcional)

```ts
export interface SchedulerResumoResponse {
  ok: boolean
  message: string
}
```

Se preferir manter simples, o inline `{ ok: boolean; message: string }` já basta.

### 3. Usar em um componente ou hook

```tsx
import { acionarResumoDiario } from '../services/api'

function BotaoEnviarResumo() {
  const [loading, setLoading] = useState(false)
  const [feedback, setFeedback] = useState<string | null>(null)

  async function handleClick() {
    setLoading(true)
    setFeedback(null)
    try {
      const res = await acionarResumoDiario()
      setFeedback(res.ok ? 'Resumo enviado!' : res.message)
    } catch (err) {
      setFeedback('Erro ao enviar resumo.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <>
      <button onClick={handleClick} disabled={loading}>
        {loading ? 'Enviando...' : 'Enviar Resumo Agora'}
      </button>
      {feedback && <p>{feedback}</p>}
    </>
  )
}
```

---

## Observações

- **Retry:** o `request()` do projeto **não faz retry em POST** (`canRetry = method !== 'POST'`). Isso é correto — evita enviar o WhatsApp duplicado.
- **Cold start Render:** se o backend estiver dormindo, a chamada pode levar até 80 s. Considere mostrar um loading persistente ou um aviso ao usuário.
- **Sem feedback de entrega:** o endpoint retorna `200` assim que o backend tenta enviar. Não confirma se a mensagem chegou no WhatsApp — apenas se o backend processou sem erro.
- **Onde colocar o botão:** sugerido na tela de Configurações, próximo às opções de notificação/WhatsApp.
