import { usuarioAtual } from '../estado/usuario'

const URL_BASE = 'http://localhost:8080'

export class ErroApi extends Error {
    constructor(status, titulo, detalhe, erros) {
        super(detalhe || titulo)
        this.status = status
        this.titulo = titulo
        this.erros = erros || null // erros por campo, quando a API devolve 400 de validação
    }
}

export async function requisitar(metodo, caminho, corpo) {
    const cabecalhos = {}
    if (corpo !== undefined) {
        cabecalhos['Content-Type'] = 'application/json'
    }
    if (usuarioAtual.value) {
        cabecalhos['X-Usuario-Id'] = String(usuarioAtual.value.id)
    }

    let resposta
    try {
        resposta = await fetch(URL_BASE + caminho, {
            method: metodo,
            headers: cabecalhos,
            body: corpo !== undefined ? JSON.stringify(corpo) : undefined,
        })
    } catch {
        throw new ErroApi(0, 'API indisponível',
            'Não foi possível falar com a API. Ela está rodando em ' + URL_BASE + '?')
    }

    if (!resposta.ok) {
        let problema = {}
        try {
            problema = await resposta.json()
        } catch {
            // corpo vazio ou que não é JSON
        }
        throw new ErroApi(resposta.status, problema.title || 'Erro',
            problema.detail || 'Erro ' + resposta.status, problema.erros)
    }

    if (resposta.status === 204) {
        return null
    }
    const tipo = resposta.headers.get('Content-Type') || ''
    return tipo.includes('application/json') ? resposta.json() : resposta.text()
}