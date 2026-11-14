# Roteiro de verificação

Cada item da seção 13 do enunciado tem a entrada, o resultado esperado, o resultado observado e a evidência.

**Legenda da coluna "Observado":** o texto já preenchido vem das telas capturadas durante o desenvolvimento.
`☐` marca o que ainda precisa ser conferido e anotado ao executar o roteiro. Os prints ficam em `docs/evidencias/`.

## Preparação

1. Banco de pé: `docker compose -f docker/docker-compose.yml up -d`.
2. API em `http://localhost:8080` e SPA em `http://localhost:5173`.
3. Para uma execução limpa, recriar o banco só com o seed: `docker compose -f docker/docker-compose.yml down -v` e
   depois `up -d`, e subir a API de novo.
4. Na SPA, menu **Usuário**: Ana Souza é a autora de todas as histórias do seed, e Bruno Lima não é.

Verificação automática: `./mvnw test` (61 testes, nenhuma falha) e a execução completa do `requests/verificacao.http`
no IntelliJ ("Run all requests in file", todos os testes verdes).

## 1. História válida, título repetido e narrativa incompleta

| # | Entrada | Esperado | Observado | Evidência |
|---|---|---|---|---|
| 1.1 | SPA, Ana, projeto Biblioteca, "+ Nova história": título `Consultar histórico de empréstimos`, papel `estudante`, objetivo `consultar os livros que já peguei emprestados`, benefício `acompanhar minhas leituras e os prazos`, prioridade Média | História criada (201); a tela mostra a narrativa e o autor Ana | ☐ | `01-historia-valida.png` |
| 1.2 | Criar outra história com o mesmo título no mesmo projeto | 409 "Já existe uma história com o título '...' neste projeto" | ☐ | `02-titulo-repetido.png` |
| 1.3 | Criar história com o papel (Como) em branco | 400 com o campo `papel`: "O papel (Como...) é obrigatório" | ☐ | `03-narrativa-incompleta.png` |

## 2. Cenário válido e inválido

| # | Entrada | Esperado | Observado | Evidência |
|---|---|---|---|---|
| 2.1 | Na história criada em 1.1, "+ Novo cenário" `Histórico com empréstimos`: Dado, Quando e Então com texto | Cenário criado (201), com os passos na ordem informada | ☐ | `04-cenario-valido.png` |
| 2.2 | Cenário só com Dado e Quando (sem Então) | 422 "O cenário precisa ter ao menos um passo Então" | 422 "Regra de negócio violada: O cenário precisa ter ao menos um passo Então" | `05-cenario-sem-entao.png` |
| 2.3 | Cenário só com Dado e Então (sem Quando) | 422 "O cenário precisa ter ao menos um passo Quando" | ☐ | `06-cenario-sem-quando.png` |
| 2.4 | Cenário com o Quando acima do Dado | 422 sobre a ordem | 422 "Passo 2: Dado não pode vir depois de Quando (a ordem é Dado, Quando, Então)" | `07-cenario-fora-de-ordem.png` |
| 2.5 | Cenário com o primeiro passo do tipo E | 422 "O cenário não pode começar com um passo E" | 422 "Regra de negócio violada: O cenário não pode começar com um passo E" | `08-cenario-comeca-com-e.png` |
| 2.6 | Dois cenários com o mesmo título na mesma história | 409 "Já existe um cenário com o título '...' nesta história" | 409 "Conflito: Já existe um cenário com o título 'dddasd' nesta história" | `09-cenario-titulo-repetido.png` |

## 3. Passos mantidos na ordem correta após edição

| # | Entrada | Esperado | Observado | Evidência |
|---|---|---|---|---|
| 3.1 | Criar `Histórico sem empréstimos` com Dado, E, Quando, Então, Mas. Editar e deixar a ordem Dado, Quando, E, Então, Mas. Salvar e recarregar a página (`F5`) | Os passos aparecem na ordem editada, e a API devolve as posições de 1 a 5 (`GET /api/cenarios/{id}`) | ☐ | `10-ordem-apos-edicao.png` |

## 4. Bloqueio de edição e exclusão por quem não é o autor

| # | Entrada | Esperado | Observado | Evidência |
|---|---|---|---|---|
| 4.1 | Como Bruno, editar a história `Reservar livro` (da Ana) | 403 | 403 "Acesso negado: Somente o autor pode alterar ou excluir esta história" | `11-editar-historia-nao-autor.png` |
| 4.2 | Como Bruno, editar um cenário dessa história | 403 | 403 "Acesso negado: Somente o autor da história pode alterar ou excluir seus cenários" | `12-editar-cenario-nao-autor.png` |
| 4.3 | Como Bruno, excluir um cenário dessa história | 403 | 403 "Acesso negado: Somente o autor da história pode alterar ou excluir seus cenários" | `13-excluir-cenario-nao-autor.png` |
| 4.4 | Como Bruno, excluir a história | 403 | ☐ | `14-excluir-historia-nao-autor.png` |

## 5. Sugestões: registro, aceite, rejeição e rejeição sem motivo

| # | Entrada | Esperado | Observado | Evidência |
|---|---|---|---|---|
| 5.1 | No painel de sugestões, "+ Registrar": ferramenta `ChatGPT`, prompt e conteúdo preenchidos | Sugestão criada (201), com decisão Pendente e "Registrada por" o usuário atual | ☐ | `15-sugestao-registrada.png` |
| 5.2 | Aceitar uma sugestão pendente, com justificativa | Decisão Aceita; a lista de cenários não muda | ☐ | `16-sugestao-aceita.png` |
| 5.3 | Rejeitar outra com o motivo Genérica e uma justificativa | Decisão Rejeitada, com o motivo exibido | ☐ | `17-sugestao-rejeitada.png` |
| 5.4 | Rejeitar uma pendente sem escolher o motivo | 422 "A rejeição exige um motivo: ..."; a sugestão continua Pendente | ☐ | `18-rejeicao-sem-motivo.png` |
| 5.5 | Rejeitar com o motivo Outro e sem justificativa | 422 "O motivo OUTRO exige uma justificativa" | ☐ | `19-motivo-outro-sem-justificativa.png` |
| 5.6 | Criar um cenário à mão com a origem "Sugestão da IA" | Cenário criado com a etiqueta "Sugestão da IA" | ☐ | `20-cenario-origem-ia.png` |

## 6. Sugestão já decidida não pode ser alterada

| # | Entrada | Esperado | Observado | Evidência |
|---|---|---|---|---|
| 6.1 | Abrir a mesma história em duas abas, com uma sugestão pendente. Decidir na aba A. Na aba B (ainda desatualizada), tentar decidir a mesma sugestão | 409 "A sugestão já foi decidida (...) e não pode ser alterada" | ☐ | `21-sugestao-ja-decidida.png` |

## 7. Exportação

| # | Entrada | Esperado | Observado | Evidência |
|---|---|---|---|---|
| 7.1 | "Exportar .feature" em uma história sem cenários | 409 "A história não tem cenários e não pode ser exportada" | ☐ | `22-exportacao-sem-cenarios.png` |
| 7.2 | "Exportar .feature" em uma história com sugestão pendente | 409 "A história tem sugestões pendentes: aceite ou rejeite todas antes de exportar" | ☐ | `23-exportacao-com-pendente.png` |
| 7.3 | Decidir todas as sugestões e exportar `Reservar livro` | Baixa o arquivo; começa com `# language: pt`; cenários `@sugestao-ia` com a tag; passos Dado, Quando, Então, E e Mas na ordem | ☐ | `24-exportacao-ok.png` e `docs/exemplo-reservar-livro.feature` |

## 8. Valores do resumo coerentes com os dados cadastrados

| # | Entrada | Esperado | Observado | Evidência |
|---|---|---|---|---|
| 8.1 | Abrir o resumo do projeto na SPA e comparar com as consultas ao banco (comandos abaixo) | Total de histórias, cenários por origem, sugestões por decisão e motivos de rejeição iguais aos do banco | ☐ | `25-resumo-spa.png` e `26-resumo-banco.png` |

Consultas ao banco (projeto 1):

```
docker compose -f docker/docker-compose.yml exec db psql -U estudio -d estudio_bdd -c "select count(*) as historias from historia_usuario where projeto_id = 1;"

docker compose -f docker/docker-compose.yml exec db psql -U estudio -d estudio_bdd -c "select c.origem, count(*) from cenario c join historia_usuario h on h.id = c.historia_id where h.projeto_id = 1 group by c.origem order by 1;"

docker compose -f docker/docker-compose.yml exec db psql -U estudio -d estudio_bdd -c "select s.decisao, count(*) from sugestao s join historia_usuario h on h.id = s.historia_id where h.projeto_id = 1 group by s.decisao order by 1;"

docker compose -f docker/docker-compose.yml exec db psql -U estudio -d estudio_bdd -c "select s.motivo_rejeicao, count(*) from sugestao s join historia_usuario h on h.id = s.historia_id where h.projeto_id = 1 and s.motivo_rejeicao is not null group by s.motivo_rejeicao order by 2 desc;"
```

Origens, decisões ou motivos que não aparecem nas consultas aparecem como 0 no resumo (ou não aparecem, no caso dos motivos).

## 9. Manutenção dos dados após reiniciar a aplicação

| # | Entrada | Esperado | Observado | Evidência |
|---|---|---|---|---|
| 9.1 | Contar os registros (comando abaixo), parar a API, subir de novo e contar outra vez | Os números são exatamente os mesmos, sem duplicar o seed | ☐ | `27-contagem-antes.png` e `28-contagem-depois.png` |

```
docker compose -f docker/docker-compose.yml exec db psql -U estudio -d estudio_bdd -c "select (select count(*) from usuario) as usuarios, (select count(*) from projeto) as projetos, (select count(*) from historia_usuario) as historias, (select count(*) from cenario) as cenarios, (select count(*) from passo) as passos, (select count(*) from sugestao) as sugestoes;"
```

## Atividade bônus

Não implementada.

## Evidências adicionais

- **CORS somente para a origem da SPA:** `curl.exe -i -H "Origin: http://localhost:5173" http://localhost:8080/api/usuarios` devolve 200 com
  `Access-Control-Allow-Origin: http://localhost:5173`; com `-H "Origin: http://outro-site.exemplo"` devolve 403
  (`Invalid CORS request`). Prints: `29-cors-permitida.png` e `30-cors-bloqueada.png`.
- **Testes automáticos:** saída do `./mvnw clean test` com 61 testes e nenhuma falha (`31-mvn-test.png`).
