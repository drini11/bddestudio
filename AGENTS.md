# AGENTS.md

## Arquitetura (Clean Architecture)
- domain.model: entidades JPA e enums
- domain.repository: interfaces Spring Data, sem regra de negócio
- application.service: regras de negócio e @Transactional
- application.port: interfaces exigidas pela aplicação (ex.: gerador de sugestões por IA)
- infrastructure.web: controllers, DTOs, @RestControllerAdvice
- infrastructure.ai e infrastructure.config: adaptadores e configuração

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
