<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { buscarProjeto, consultarProjeto } from '../api/projetos'
import { PRIORIDADES, rotuloPrioridade } from '../utils/rotulos'

const projetoId = useRoute().params.id

const projeto = ref(null)
const historias = ref([])
const prioridade = ref('') // '' = todas
const carregando = ref(true)
const erro = ref('')

async function carregarHistorias() {
  carregando.value = true
  erro.value = ''
  try {
    historias.value = await consultarProjeto(projetoId, prioridade.value)
  } catch (e) {
    erro.value = e.message
  } finally {
    carregando.value = false
  }
}

onMounted(async () => {
  try {
    projeto.value = await buscarProjeto(projetoId)
  } catch (e) {
    erro.value = e.message
    carregando.value = false
    return
  }
  await carregarHistorias()
})

// toda vez que o filtro muda, busca de novo na API
watch(prioridade, carregarHistorias)
</script>

<template>
  <section>
    <p><RouterLink to="/projetos">← Projetos</RouterLink></p>

    <p v-if="erro && !projeto" class="erro">{{ erro }}</p>

    <template v-if="projeto">
      <div class="cabecalho">
        <h1>{{ projeto.nome }}</h1>
        <RouterLink :to="`/projetos/${projeto.id}/historias/nova`">+ Nova história</RouterLink>
        <RouterLink :to="`/projetos/${projeto.id}/resumo`">Ver resumo</RouterLink>
      </div>
      <p class="aviso">{{ projeto.descricao }}</p>

      <label class="filtro">
        Prioridade
        <select v-model="prioridade">
          <option value="">Todas</option>
          <option v-for="p in PRIORIDADES" :key="p.valor" :value="p.valor">{{ p.rotulo }}</option>
        </select>
      </label>

      <p v-if="carregando">Carregando...</p>
      <p v-else-if="erro" class="erro">{{ erro }}</p>
      <p v-else-if="historias.length === 0">
        Nenhuma história{{ prioridade ? ' com essa prioridade' : '' }}.
      </p>

      <table v-else class="tabela">
        <thead>
        <tr>
          <th>História</th>
          <th>Prioridade</th>
          <th>Autor</th>
          <th>Cenários</th>
          <th>Sugestões pendentes</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="h in historias" :key="h.id">
          <td>
            <RouterLink :to="`/historias/${h.id}`"><strong>{{ h.titulo }}</strong></RouterLink><br />
            <small>Como {{ h.papel }}, quero {{ h.objetivo }}, para {{ h.beneficio }}</small>
          </td>
          <td>
              <span :class="['etiqueta', 'prioridade-' + h.prioridade.toLowerCase()]">
                {{ rotuloPrioridade(h.prioridade) }}
              </span>
          </td>
          <td>{{ h.autorNome }}</td>
          <td>{{ h.quantidadeCenarios }}</td>
          <td :class="{ pendente: h.sugestoesPendentes > 0 }">{{ h.sugestoesPendentes }}</td>
        </tr>
        </tbody>
      </table>
    </template>
  </section>
</template>