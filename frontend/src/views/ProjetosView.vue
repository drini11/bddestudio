<script setup>
import { ref, onMounted } from 'vue'
import { listarProjetos } from '../api/projetos'

const projetos = ref([])
const carregando = ref(true)
const erro = ref('')

onMounted(async () => {
  try {
    projetos.value = await listarProjetos()
  } catch (e) {
    erro.value = e.message
  } finally {
    carregando.value = false
  }
})
</script>

<template>
  <section>
    <h1>Projetos</h1>

    <p v-if="carregando">Carregando...</p>
    <p v-else-if="erro" class="erro">{{ erro }}</p>
    <p v-else-if="projetos.length === 0">Nenhum projeto cadastrado.</p>

    <ul v-else class="cartoes">
      <li v-for="p in projetos" :key="p.id">
        <div>
          <strong>{{ p.nome }}</strong><br />
          <small>{{ p.descricao }}</small>
        </div>
        <div class="acoes">
          <RouterLink :to="`/projetos/${p.id}`">Histórias</RouterLink>
          <RouterLink :to="`/projetos/${p.id}/resumo`">Resumo</RouterLink>
        </div>
      </li>
    </ul>
  </section>
</template>