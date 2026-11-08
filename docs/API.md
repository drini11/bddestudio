# API do Estúdio BDD

Base: `http://localhost:8080`. As respostas são JSON, exceto a exportação `.feature`, que é texto UTF-8.

## Identidade simulada (limitação)

As operações que dependem de quem as executa recebem o usuário atual no cabeçalho `X-Usuario-Id`
(o `id` de um usuário cadastrado, listado em `GET /api/usuarios`).

**Isso não é autenticação.** Não há senha nem token: qualquer cliente pode enviar o id de outro usuário.
O cabeçalho serve apenas para registrar a autoria e aplicar a regra "somente o autor edita ou exclui a
história e seus cenários". Autenticação real (por exemplo, Spring Security) está fora do escopo.

## Formato de erro

Todo erro devolve um `ProblemDetail` (RFC 9457) com `title`, `status`, `detail` e `instance`.
Erros de validação (400) trazem também `erros`, um objeto `{ "campo": "mensagem" }`.

| Status | Quando |
|---|---|
| 400 | JSON malformado, campo ausente ou inválido, valor de enum inexistente, cabeçalho ausente ou mal formado, parâmetro com tipo errado |
| 401 | `X-Usuario-Id` não corresponde a nenhum usuário |
| 403 | O usuário não é o autor da história |
| 404 | Recurso inexistente |
| 409 | Conflito com o estado atual: nome ou título repetido, projeto com histórias, sugestão já decidida, exportação bloqueada |
| 422 | Regra de negócio violada: ordem ou obrigatoriedade dos passos, rejeição de sugestão sem motivo |

Ordem das verificações: identificar (401), localizar (404), autorizar (403), regra de negócio (422), conflito (409).

## Usuários

| Rota | Cabeçalho | Sucesso | Erros |
|---|---|---|---|
| `GET /api/usuarios` | | 200, lista de usuários | |

## Projetos

Corpo (`POST` e `PUT`): `nome` (obrigatório, até 150), `descricao` (até 255), `contextoDominio` (até 10000).

| Rota | Cabeçalho | Sucesso | Erros |
|---|---|---|---|
| `POST /api/projetos` | | 201 + `Location` | 400, 409 (nome repetido) |
| `GET /api/projetos` | | 200 | |
| `GET /api/projetos/{id}` | | 200 | 404 |
| `PUT /api/projetos/{id}` | | 200 | 400, 404, 409 (nome repetido) |
| `DELETE /api/projetos/{id}` | | 204 | 404, 409 (projeto com histórias) |

## Histórias

Corpo (`POST` e `PUT`): `titulo` (até 150), `papel`, `objetivo`, `beneficio` (até 255 cada; todos obrigatórios) e
`prioridade` (`ALTA`, `MEDIA` ou `BAIXA`). O título é único dentro do projeto.

| Rota | Cabeçalho | Sucesso | Erros |
|---|---|---|---|
| `POST /api/projetos/{projetoId}/historias` | `X-Usuario-Id` | 201 + `Location`; o usuário vira o autor | 400, 401, 404 (projeto), 409 (título repetido) |
| `GET /api/projetos/{projetoId}/historias?prioridade=` | | 200; o filtro é opcional | 400 (prioridade inválida), 404 |
| `GET /api/historias/{id}` | | 200 | 404 |
| `PUT /api/historias/{id}` | `X-Usuario-Id` | 200 | 400, 401, 403, 404, 409 |
| `DELETE /api/historias/{id}` | `X-Usuario-Id` | 204; remove também cenários, passos e sugestões | 400, 401, 403, 404 |

## Cenários e passos

Corpo (`POST` e `PUT`):

```json
{
  "titulo": "Reserva de livro emprestado",
  "origem": "HUMANO",
  "passos": [
    { "tipo": "DADO", "texto": "que o livro está emprestado" },
    { "tipo": "QUANDO", "texto": "o estudante solicita a reserva" },
    { "tipo": "ENTAO", "texto": "o estudante entra na fila" }
  ]
}
```

- `titulo`: obrigatório, até 150, único na história. `origem` é opcional: `HUMANO` (padrão na criação) ou `SUGESTAO_IA`.
- `tipo`: `DADO`, `QUANDO`, `ENTAO`, `E` ou `MAS`. `texto`: obrigatório, até 255.
- Os passos são numerados pelo servidor na ordem em que chegam. O `PUT` **substitui** a sequência inteira.
- Regra 2 (422): a ordem é Dado, Quando, Então; `E` e `MAS` continuam o tipo anterior e não iniciam o cenário; é obrigatório ao menos um Quando e um Então.
- Quem altera cenários é o autor da história a que eles pertencem.

| Rota | Cabeçalho | Sucesso | Erros |
|---|---|---|---|
| `POST /api/historias/{historiaId}/cenarios` | `X-Usuario-Id` | 201 + `Location` | 400, 401, 403, 404, 409 (título repetido), 422 |
| `GET /api/historias/{historiaId}/cenarios` | | 200, com os passos em ordem | 404 |
| `GET /api/cenarios/{id}` | | 200 | 404 |
| `PUT /api/cenarios/{id}` | `X-Usuario-Id` | 200 | 400, 401, 403, 404, 409, 422 |
| `DELETE /api/cenarios/{id}` | `X-Usuario-Id` | 204 | 400, 401, 403, 404 |

## Sugestões de IA

Uma sugestão registra o que uma ferramenta de IA propôs. Ela nasce `PENDENTE` e, depois de decidida, não muda mais.
Aceitar não altera os cenários: o analista ajusta e pode marcar o cenário com `origem: SUGESTAO_IA`.
Qualquer usuário identificado pode registrar e decidir.

| Rota | Cabeçalho | Corpo | Sucesso | Erros |
|---|---|---|---|---|
| `POST /api/historias/{historiaId}/sugestoes` | `X-Usuario-Id` | `ferramenta` (até 100), `prompt` e `conteudo` (até 20000), todos obrigatórios | 201 + `Location` | 400, 401, 404 |
| `GET /api/historias/{historiaId}/sugestoes?decisao=` | | | 200; filtro opcional (`PENDENTE`, `ACEITA`, `REJEITADA`) | 400, 404 |
| `GET /api/sugestoes/{id}` | | | 200 | 404 |
| `POST /api/sugestoes/{id}/aceitar` | `X-Usuario-Id` | opcional: `justificativa` (até 2000) | 200 | 400, 401, 404, 409 (já decidida) |
| `POST /api/sugestoes/{id}/rejeitar` | `X-Usuario-Id` | `motivo` (`INCORRETA`, `GENERICA`, `DUPLICADA`, `FORA_DO_ESCOPO`, `OUTRO`) e `justificativa` (até 2000) | 200 | 400, 401, 404, 409 (já decidida), 422 (sem motivo; `OUTRO` sem justificativa) |

## Exportação

| Rota | Sucesso | Erros |
|---|---|---|
| `GET /api/historias/{id}/feature` | 200, `text/plain; charset=UTF-8` com `Content-Disposition: attachment; filename="<titulo>.feature"` | 404; 409 (história sem cenários ou com sugestão pendente) |

O arquivo é Gherkin em português, começando por `# language: pt`. A funcionalidade leva a tag
`@prioridade-<alta|media|baixa>`, e os cenários de origem `SUGESTAO_IA` levam a tag `@sugestao-ia`.

## Consulta e resumo do projeto

| Rota | Sucesso | Erros |
|---|---|---|
| `GET /api/projetos/{projetoId}/consulta?prioridade=` | 200; histórias com `quantidadeCenarios` e `sugestoesPendentes`; filtro opcional por prioridade | 400, 404 |
| `GET /api/projetos/{projetoId}/resumo` | 200; ver abaixo | 404 |

Resposta do resumo:

```json
{
  "projetoId": 1,
  "projetoNome": "Biblioteca Universitária",
  "totalHistorias": 3,
  "cenariosPorOrigem": { "HUMANO": 4, "SUGESTAO_IA": 1 },
  "sugestoesPorDecisao": { "PENDENTE": 0, "ACEITA": 1, "REJEITADA": 2 },
  "motivosDeRejeicao": [
    { "motivo": "GENERICA", "quantidade": 1 },
    { "motivo": "DUPLICADA", "quantidade": 1 }
  ]
}
```

`motivosDeRejeicao` lista só os motivos que ocorreram, do mais frequente ao menos frequente (empate por ordem do enum).