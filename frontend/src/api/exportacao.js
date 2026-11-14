import { baixarTexto } from './cliente'

export const exportarFeature = (historiaId) => baixarTexto(`/api/historias/${historiaId}/feature`)