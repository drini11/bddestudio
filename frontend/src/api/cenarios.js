import { requisitar } from './cliente'

export const listarCenarios = (historiaId) =>
    requisitar('GET', `/api/historias/${historiaId}/cenarios`)

export const criarCenario = (historiaId, dados) =>
    requisitar('POST', `/api/historias/${historiaId}/cenarios`, dados)

export const atualizarCenario = (id, dados) => requisitar('PUT', `/api/cenarios/${id}`, dados)

export const excluirCenario = (id) => requisitar('DELETE', `/api/cenarios/${id}`)