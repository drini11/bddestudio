import { requisitar } from './cliente'

export const criarHistoria = (projetoId, dados) =>
    requisitar('POST', `/api/projetos/${projetoId}/historias`, dados)

export const buscarHistoria = (id) => requisitar('GET', `/api/historias/${id}`)

export const atualizarHistoria = (id, dados) => requisitar('PUT', `/api/historias/${id}`, dados)

export const excluirHistoria = (id) => requisitar('DELETE', `/api/historias/${id}`)