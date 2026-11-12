import { ref } from 'vue'

const CHAVE = 'estudiobdd.usuario'

function lerSalvo() {
    try {
        return JSON.parse(localStorage.getItem(CHAVE))
    } catch {
        return null
    }
}

// ref = valor reativo: quando muda, tudo que o usa na tela é redesenhado
export const usuarioAtual = ref(lerSalvo())

export function escolherUsuario(usuario) {
    usuarioAtual.value = usuario
    try {
        localStorage.setItem(CHAVE, JSON.stringify(usuario))
    } catch {
        // sem armazenamento disponível: a seleção vale só até recarregar a página
    }
}