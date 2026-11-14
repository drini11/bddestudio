# Diagrama de classes

Modelo de domínio (`domain.model`). As associações e a multiplicidade seguem o mapeamento JPA.

```mermaid
classDiagram
    class Usuario {
        +Long id
        +String nome
        +String email  «único»
    }

    class Projeto {
        +Long id
        +String nome  «único»
        +String descricao
        +String contextoDominio
        +LocalDateTime criadoEm
    }

    class HistoriaUsuario {
        +Long id
        +String titulo  «único no projeto»
        +String papel
        +String objetivo
        +String beneficio
        +Prioridade prioridade
        +LocalDateTime criadaEm
        +LocalDateTime atualizadaEm
        +foiEscritaPor(usuarioId) boolean
    }

    class Cenario {
        +Long id
        +String titulo  «único na história»
        +int posicao
        +OrigemCenario origem
    }

    class Passo {
        +Long id
        +TipoPasso tipo
        +String texto
        +int posicao
    }

    class Sugestao {
        +Long id
        +String ferramenta
        +String prompt
        +String conteudo
        +DecisaoSugestao decisao
        +MotivoRejeicao motivoRejeicao
        +String justificativa
        +LocalDateTime registradaEm
        +LocalDateTime decididaEm
        +aceitar(usuario, justificativa)
        +rejeitar(usuario, motivo, justificativa)
    }

    class Prioridade {
        <<enumeration>>
        ALTA
        MEDIA
        BAIXA
    }

    class OrigemCenario {
        <<enumeration>>
        HUMANO
        SUGESTAO_IA
    }

    class TipoPasso {
        <<enumeration>>
        DADO
        QUANDO
        ENTAO
        E
        MAS
    }

    class DecisaoSugestao {
        <<enumeration>>
        PENDENTE
        ACEITA
        REJEITADA
    }

    class MotivoRejeicao {
        <<enumeration>>
        INCORRETA
        GENERICA
        DUPLICADA
        FORA_DO_ESCOPO
        OUTRO
    }

    HistoriaUsuario "*" --> "1" Projeto : pertence a
    HistoriaUsuario "*" --> "1" Usuario : autor
    HistoriaUsuario "1" *-- "*" Cenario : cenários ordenados por posição
    Cenario "1" *-- "*" Passo : passos ordenados por posição
    HistoriaUsuario "1" *-- "*" Sugestao : sugestões
    Sugestao "*" --> "1" Usuario : registradaPor
    Sugestao "*" --> "0..1" Usuario : decididaPor

    HistoriaUsuario --> Prioridade
    Cenario --> OrigemCenario
    Passo --> TipoPasso
    Sugestao --> DecisaoSugestao
    Sugestao --> MotivoRejeicao
```

## Notas

- O losango cheio (`*--`) indica composição: cenários, passos e sugestões são removidos junto com a história, e os
  passos junto com o cenário (`cascade` e `orphanRemoval`).
- `Projeto` não guarda a lista de histórias: a associação é unidirecional, da história para o projeto.
- Os passos e os cenários voltam sempre ordenados por `posicao` (`@OrderBy`).
- `motivoRejeicao`, `decididaPor` e `decididaEm` só são preenchidos quando a sugestão deixa de ser `PENDENTE`.
- Restrições de unicidade no banco: e-mail do usuário, nome do projeto, `(projeto, título)` na história e
  `(história, título)` no cenário.

## Camadas

```mermaid
flowchart LR
    SPA["Apresentação<br/>frontend/ (Vue)"] -->|HTTP e JSON| C["Controller<br/>infrastructure.web"]
    C --> S["Serviço<br/>application.service"]
    S --> R["Repositório<br/>domain.repository"]
    R --> B[("PostgreSQL")]
    M["Domínio<br/>domain.model"] -.-> S
    M -.-> R
```
