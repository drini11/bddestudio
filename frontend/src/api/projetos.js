import { requisitar } from './cliente'

export const listarProjetos = () => requisitar('GET', '/api/projetos')

export const buscarProjeto = (id) => requisitar('GET', `/api/projetos/${id}`)

export const consultarProjeto = (id, prioridade) =>
    requisitar('GET', `/api/projetos/${id}/consulta` + (prioridade ? `?prioridade=${prioridade}` : ''))

export const resumirProjeto = (id) => requisitar('GET', `/api/projetos/${id}/resumo`)