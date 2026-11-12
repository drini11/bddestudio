# AGENTS.md

## Arquitetura (Clean Architecture)
- domain.model: entidades JPA e enums
- domain.repository: interfaces Spring Data, sem regra de negócio
- application.service: regras de negócio e @Transactional
- infrastructure.web: controllers, DTOs, @RestControllerAdvice

## Regras de dependência (verificadas pelo ArchUnit)
- domain não depende de application nem de infrastructure
- application não depende de infrastructure
- infrastructure.web não acessa domain.repository

## Convenções
- Injeção por construtor; nunca @Autowired em campo
- Não usar @Data em entidades
- Associações sempre LAZY; open-in-view desligado
- Toda regra de negócio fica em application.service
- @Transactional só em métodos de escrita; nunca na classe
- Regra de formato de cenário (ordem, E/Mas, Quando e Então) fica só em ValidadorPassos; violação lança RegraNegocioException (422)
- Passos são editados pelo cenário: o PUT substitui a sequência. Nunca trocar a coleção `passos` por uma nova lista (orphanRemoval); usar clear() + add
- Autoria de cenário usa HistoriaUsuario.foiEscritaPor
- Sugestão decidida é imutável (409). Não existe PUT nem DELETE de sugestão; o estado só muda por Sugestao.aceitar/rejeitar, chamados pelo SugestaoService
- Regras de sugestão (motivo obrigatório na rejeição, OUTRO exige justificativa, já decidida) ficam em SugestaoService; o DTO não valida o motivo
- Ordem de erros: 401, 404, 403, 422, 409
- Agregações (contagens por história, por origem, por decisão, por motivo) usam group by em consultas de projeção; nunca consultar história por história
- Resumo preenche com zero os valores de enum sem ocorrência; ConsultaProjetoService não usa @Transactional (só leituras)

## Comandos
- Banco: docker compose -f docker/docker-compose.yml up -d
- Rodar: ./mvnw spring-boot:run
- Testar: ./mvnw test

## Frontend (frontend/, Vue 3 + Vite, JavaScript)
- Sem regra de negócio: a SPA exibe o que a API devolve, inclusive as mensagens de erro (ProblemDetail)
- Toda chamada HTTP passa por src/api/cliente.js (requisitar); as views não chamam fetch direto
- Usuário atual fica em src/estado/usuario.js e vai no cabeçalho X-Usuario-Id
- Comandos: cd frontend; npm install; npm run dev (http://localhost:5173)
- CORS liberado só para http://localhost:5173 (CorsConfig); a porta é fixa no vite.config.js
- Rótulos de enums (Prioridade, Origem, Decisão, Motivo) ficam em src/utils/rotulos.js; a tela nunca mostra o valor cru da API
- Filtros são enviados à API como parâmetro; a SPA não filtra listas por conta própria