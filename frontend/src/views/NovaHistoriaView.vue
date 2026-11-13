<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { criarHistoria } from '../api/historias'
import { usuarioAtual } from '../estado/usuario'
import FormularioHistoria from '../components/FormularioHistoria.vue'

const route = useRoute()
const router = useRouter()
const projetoId = route.params.id

const salvando = ref(false)
const erro = ref(null)

async function salvar(dados) {
  salvando.value = true
  erro.value = null
  try {
    const criada = await criarHistoria(projetoId, dados)
    router.push(`/historias/${criada.id}`)
  } catch (e) {
    erro.value = e
  } finally {
    salvando.value = false
  }
}
</script>

<template>
  <section>
    <p><RouterLink :to="`/projetos/${projetoId}`">← Histórias do projeto</RouterLink></p>
    <h1>Nova história</h1>

    <p v-if="!usuarioAtual" class="caixa-aviso">
      Selecione um usuário no menu "Usuário" antes de criar uma história.
    </p>

    <FormularioHistoria
        :salvando="salvando"
        :erro="erro"
        @salvar="salvar"
        @cancelar="router.push(`/projetos/${projetoId}`)"
    />
  </section>
</template>