import { requisitar } from './cliente'

export const listarSugestoes = (historiaId, decisao) =>
    requisitar('GET', `/api/historias/${historiaId}/sugestoes` + (decisao ? `?decisao=${decisao}` : ''))

export const registrarSugestao = (historiaId, dados) =>
    requisitar('POST', `/api/historias/${historiaId}/sugestoes`, dados)

export const aceitarSugestao = (id, dados) => requisitar('POST', `/api/sugestoes/${id}/aceitar`, dados)

export const rejeitarSugestao = (id, dados) => requisitar('POST', `/api/sugestoes/${id}/rejeitar`, dados)