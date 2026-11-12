<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { resumirProjeto } from '../api/projetos'
import { ORIGENS, DECISOES, MOTIVOS } from '../utils/rotulos'

const projetoId = useRoute().params.id

const resumo = ref(null)
const carregando = ref(true)
const erro = ref('')

onMounted(async () => {
  try {
    resumo.value = await resumirProjeto(projetoId)
  } catch (e) {
    erro.value = e.message
  } finally {
    carregando.value = false
  }
})
</script>

<template>
  <section>
    <p><RouterLink :to="`/projetos/${projetoId}`">← Histórias do projeto</RouterLink></p>

    <p v-if="carregando">Carregando...</p>
    <p v-else-if="erro" class="erro">{{ erro }}</p>

    <template v-else>
      <h1>Resumo: {{ resumo.projetoNome }}</h1>

      <div class="indicador">
        <span class="numero">{{ resumo.totalHistorias }}</span>
        <span>histórias</span>
      </div>

      <h2>Cenários por origem</h2>
      <ul class="lista-simples">
        <li v-for="(qtd, origem) in resumo.cenariosPorOrigem" :key="origem">
          {{ ORIGENS[origem] ?? origem }}: <strong>{{ qtd }}</strong>
        </li>
      </ul>

      <h2>Sugestões por decisão</h2>
      <ul class="lista-simples">
        <li v-for="(qtd, decisao) in resumo.sugestoesPorDecisao" :key="decisao">
          {{ DECISOES[decisao] ?? decisao }}: <strong>{{ qtd }}</strong>
        </li>
      </ul>

      <h2>Motivos de rejeição mais frequentes</h2>
      <p v-if="resumo.motivosDeRejeicao.length === 0">Nenhuma sugestão rejeitada.</p>
      <ol v-else class="lista-simples">
        <li v-for="m in resumo.motivosDeRejeicao" :key="m.motivo">
          {{ MOTIVOS[m.motivo] ?? m.motivo }}: <strong>{{ m.quantidade }}</strong>
        </li>
      </ol>
    </template>
  </section>
</template>