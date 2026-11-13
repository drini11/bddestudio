<script setup>
import { ref, onMounted } from 'vue'
import { listarUsuarios } from '../api/usuarios'
import { usuarioAtual, escolherUsuario } from '../estado/usuario'

const usuarios = ref([])
const carregando = ref(true)
const erro = ref('')

onMounted(async () => {
  try {
    usuarios.value = await listarUsuarios()
    // se o usuário salvo não existe mais (banco recriado), esquece dele
    if (usuarioAtual.value && !usuarios.value.some((u) => u.id === usuarioAtual.value.id)) {
      escolherUsuario(null)
    }
  } catch (e) {
    erro.value = e.message
  } finally {
    carregando.value = false
  }
})
</script>

<template>
  <section>
    <h1>Usuário de demonstração</h1>
    <p v-if="carregando">Carregando...</p>
    <p v-else-if="erro" class="erro">{{ erro }}</p>

    <ul v-else class="cartoes">
      <li v-for="u in usuarios" :key="u.id" :class="{ ativo: usuarioAtual?.id === u.id }">
        <div>
          <strong>{{ u.nome }}</strong><br />
          <small>{{ u.email }}</small>
        </div>
        <button @click="escolherUsuario(u)" :disabled="usuarioAtual?.id === u.id">
          {{ usuarioAtual?.id === u.id ? 'Selecionado' : 'Usar este usuário' }}
        </button>
      </li>
    </ul>
  </section>
</template>