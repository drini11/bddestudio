# Registro de uso de IA

Ferramenta usada: **Claude Code** (claude.ai), como assistente de desenvolvimento. A decisão e a
responsabilidade pelo código são do autor do projeto. Os dados usados nos prompts e nos testes são fictícios e gerados por IA, e nenhuma
credencial foi enviada.

Cada interação tem: problema, ferramenta e prompt, proposta da IA, decisão, mudanças realizadas e forma de verificação.

## API

### API-1. Arquitetura em camadas e ArchUnit

- **Problema:** aplicar Clean Architecture (`domain`, `application`, `infrastructure`) e cumprir as cinco camadas do
  enunciado, com um teste que garanta que o controller não acessa o repositório.
- **Ferramenta e prompt:** Claude. Ajuda com a arquitetura, com commits e testes unitários.
- **Proposta da IA:** mapa entre as camadas do enunciado e os pacotes, entidades JPA como modelo de domínio, três regras
  ArchUnit, `docker-compose` e `application.yml`.
- **Mudanças:** `ArquiteturaTest` corrigido, `allowEmptyShould` removido depois dos primeiros serviços e controllers, e
  uma entrada em "Erros recorrentes da IA" no `AGENTS.md`.
- **Verificação:** um teste temporário de diagnóstico listou as classes importadas (13 com filtro). Depois da correção,
  `mvn test` passou com as três regras analisando classes de verdade.

### API-2. Tratamento de erros com ProblemDetail

- **Problema:** erro de compilação no `@Override` do handler de exceções.
- **Ferramenta e prompt:** Claude. Mensagem de erro do IntelliJ sobre o `@Override` e o tipo do `request`.
- **Erro da IA:** a causa real era um import errado de `ResponseEntityExceptionHandler`. Reescrito antes de
  pedir a mensagem exata do erro.
- **Decisão:** manter o handler original, com o `extends` e o import correto.
- **Mudanças:** import corrigido. A versão sem `extends` foi descartada.
- **Verificação:** os erros do framework (JSON malformado, cabeçalho ausente, método não suportado) saem no mesmo
  formato `ProblemDetail`; o 405 de um `DELETE` sem mapeamento mostrou isso na prática.

### API-3. @Transactional somente onde há escrita

- **Problema:** o professor orientou usar `@Transactional` só nos métodos de atualização.
- **Ferramenta e prompt:** Claude. Explicação sobre o funcionamento do @Transactional e debate sobre onde manter e onde remover.
- **Proposta da IA:** tirar das leituras e da classe; manter em criar, atualizar e excluir. Explicou que o `atualizar`
  depende da transação por causa do dirty checking (sem ela, nada é gravado e não há erro) e que excluir uma história sem
  transação fica frágil.
- **Decisão:** `@Transactional` nos métodos de escrita e nenhum nas leituras.
- **Mudanças:** anotações movidas em `ProjetoService` e `HistoriaService`; convenção no `AGENTS.md`.
- **Verificação:** `requests/transactional.http`: o `GET` depois de cada `PUT` mostra o valor novo, e a exclusão de
  história responde 204. Os testes com Mockito não detectam transação faltando, por isso essa verificação foi manual.

### API-4. Cenário e Passo (regra 2)

- **Problema:** validar a ordem Dado, Quando, Então, o comportamento de E/Mas e a obrigatoriedade de Quando e Então, e
  manter os passos ordenados na edição.
- **Ferramenta e prompt:** Claude. Pedido do passo "Cenário e Passo" e do primeiro teste de regras.
- **Proposta da IA:** `ValidadorPassos` estático e sem Spring; `PUT` que substitui a sequência de passos com
  `clear()` e `add` na mesma lista (por causa de `orphanRemoval`); 422 para violação de regra e 409 para título repetido.
- **Intercorrência:** ao acrescentar o novo construtor de `Cenario`, o antigo foi substituído e o `DadosIniciais` deixou
  de compilar. Os dois construtores foram mantidos.
- **Decisão:** a regra fica em um lugar só, e o formato é verificado sobre a sequência inteira.
- **Mudanças:** `ValidadorPassos`, `CenarioService`, `CenarioController` e DTOs.
- **Verificação:** 10 testes do validador e 8 do serviço; `requests/cenarios.http` (422 sem Então, fora de ordem e
  iniciado por E; ordem preservada depois de editar).

### API-5. Exportação e resumo

- **Problema:** implementar a regra de exportação do .feature
- **Ferramenta e prompt:** Claude. Pedido de explicação do funcionamneto dos passos "exportação .feature" e "consulta e resumo".
- **Proposta da IA:** `Sugestao` sem setters, com `aceitar` e `rejeitar`; rejeição sem motivo como regra de negócio no
  serviço (422); `CascadeType.REMOVE` para excluir as sugestões junto com a história; `GeradorFeature` estático, com
  quebras de linha reduzidas a uma linha para não injetar linhas de Gherkin; contagens por `group by` em projeções, sem
  consultar história por história.
- **Decisão:** exportação bloqueada devolve 409 (estado), e a rejeição sem motivo devolve 422 (regra).
- **Mudanças:** entidade, repositórios, `SugestaoService`, `ExportacaoService`, `ConsultaProjetoService`, controllers e DTOs.
- **Verificação:** os dois testes de regra exigidos (`rejeitar_semMotivo` e `exportar_comSugestaoPendente`), 61 testes no
  total, os roteiros `sugestoes.http`, `exportacao.http` e `consulta.http`, e a comparação do resumo com consultas ao banco.

### API-6. Remoção do Spring AI

- **Problema:** não haveria tempo para a integração com a API de IA (opcional).
- **Decisão:** remover o Spring AI e a configuração de IA. O projeto passou a não usar nenhuma variável de ambiente.
- **Mudanças:** `pom.xml`, `application.yml` e `AGENTS.md`.
- **Verificação:** `./mvnw clean test` com os 61 testes passando.

## SPA

### SPA-1. Escolha do framework

- **Problema:** pouca experiência com um framework de frontend, com poucos dias para a SPA.
- **Ferramenta e prompt:** Claude. "Posso fazer com React com Vite, mas eu tenho pouca experiência com frameworks frontend, com
  Angular seria mais simples?"
- **Proposta da IA:** comparou Vue, React e Angular pela quantidade de conceitos até a primeira tela com dados, e
  recomendou Vue 3 com Vite.
- **Decisão:** usar Vue 3 com Vite, em JavaScript.
- **Mudanças:** projeto criado em `frontend/` com o gerador oficial, só com o Vue Router.
- **Verificação:** a tela de seleção de usuário carregou os dados reais da API.

### SPA-2. Estrutura, CORS e cliente HTTP

- **Problema:** liberar o CORS só para a origem da SPA, centralizar as chamadas HTTP e acrescentar o cabeçalho
  `X-Usuario-Id` em um lugar só.
- **Ferramenta e prompt:** Claude. Pedido da estrutura da SPA e, depois, a mensagem do Vite "Failed to resolve import
  '../estado/usuario'" e como funcionaria as chamadas à API.
- **Proposta da IA:** `CorsConfig`, `cliente.js` (que converte `ProblemDetail` em `ErroApi`), `usuario.js` (estado do
  usuário) e a lista de causas prováveis do erro de import. A causa real era uma pasta criada como `estados`.
- **Decisão:** toda chamada HTTP passa por `cliente.js`.
- **Mudanças:** pasta renomeada para `estado`; porta do Vite fixada em 5173 com `strictPort`.
- **Verificação:** `curl.exe` com a origem permitida (200 e `Access-Control-Allow-Origin`) e com outra origem (403); a lista
  de usuários apareceu na tela.

### SPA-3. Telas de projetos, histórias e cenários

- **Problema:** construir as telas sem duplicar regra de negócio no frontend, e permitir reordenar os passos de um
  cenário.
- **Ferramenta e prompt:** Claude. Pedidos do funcionamento das telas de projetos, histórias com filtro, resumo, criação e edição de
  histórias e do editor de cenários.
- **Proposta da IA:** formulários sem validação própria, que enviam o que foi digitado e mostram o erro da API
  (`MensagemErro`); botões para subir e descer cada passo; recarregar os cenários da API depois de salvar.
- **Decisão:** a SPA só exibe o que a API decide.
- **Mudanças:** views, componentes `FormularioHistoria`, `EditorCenario` e `MensagemErro`.
- **Verificação:** roteiro nas telas, com print das mensagens 422 (sem Então, fora de ordem, iniciado por E), 409 (título
  repetido) e 403 (usuário que não é o autor).

### SPA-4. Sugestões e exportação do .feature

- **Problema:** mostrar as sugestões ao lado da história e baixar o `.feature` com o nome vindo da API.
- **Ferramenta e prompt:** Claude. Pedido do painel de sugestões e da exportação.
- **Proposta da IA:** `PainelSugestoes` com estado próprio; a rejeição sem motivo é enviada como `motivo: null`, para a API
  responder 422; `baixarTexto` lê o `Content-Disposition` (exposto pelo CORS) e o download usa `Blob`.
- **Decisão:** a tela não bloqueia a rejeição sem motivo.
- **Mudanças:** componente, `cliente.js` dividido em `chamar` e `requisitar`, `exportacao.js` e `utils/arquivo.js`.
- **Verificação:** rejeição sem motivo (422), exportação bloqueada (409), prova de "sugestão já decidida" com duas abas
  (409) e o `.feature` baixado.