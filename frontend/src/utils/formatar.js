export function formatarData(iso) {
    if (!iso) return ''
    return new Date(iso).toLocaleString('pt-BR', { dateStyle: 'short', timeStyle: 'short' })
}