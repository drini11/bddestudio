export const PRIORIDADES = [
    { valor: 'ALTA', rotulo: 'Alta' },
    { valor: 'MEDIA', rotulo: 'Média' },
    { valor: 'BAIXA', rotulo: 'Baixa' },
]

export const rotuloPrioridade = (valor) =>
    PRIORIDADES.find((p) => p.valor === valor)?.rotulo ?? valor

export const ORIGENS = {
    HUMANO: 'Humano',
    SUGESTAO_IA: 'Sugestão da IA',
}

export const DECISOES = {
    PENDENTE: 'Pendente',
    ACEITA: 'Aceita',
    REJEITADA: 'Rejeitada',
}

export const MOTIVOS = {
    INCORRETA: 'Incorreta',
    GENERICA: 'Genérica',
    DUPLICADA: 'Duplicada',
    FORA_DO_ESCOPO: 'Fora do escopo',
    OUTRO: 'Outro',
}