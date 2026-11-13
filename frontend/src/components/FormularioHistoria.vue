<script setup>
import { reactive } from 'vue'
import { PRIORIDADES } from '../utils/rotulos'
import MensagemErro from './MensagemErro.vue'

const props = defineProps({
  inicial: {
    type: Object,
    default: () => ({ titulo: '', papel: '', objetivo: '', beneficio: '', prioridade: 'MEDIA' }),
  },
  salvando: Boolean,
  erro: Object,
})
const emit = defineEmits(['salvar', 'cancelar'])

const form = reactive({
  titulo: props.inicial.titulo,
  papel: props.inicial.papel,
  objetivo: props.inicial.objetivo,
  beneficio: props.inicial.beneficio,
  prioridade: props.inicial.prioridade,
})
</script>

<template>
  <form class="formulario" @submit.prevent="emit('salvar', { ...form })">
    <label>Título <input v-model="form.titulo" maxlength="150" /></label>
    <label>Como (papel) <input v-model="form.papel" maxlength="255" /></label>
    <label>Quero (objetivo) <input v-model="form.objetivo" maxlength="255" /></label>
    <label>Para (benefício) <input v-model="form.beneficio" maxlength="255" /></label>
    <label>
      Prioridade
      <select v-model="form.prioridade">
        <option v-for="p in PRIORIDADES" :key="p.valor" :value="p.valor">{{ p.rotulo }}</option>
      </select>
    </label>

    <MensagemErro :erro="erro" />

    <div class="botoes">
      <button type="submit" :disabled="salvando">{{ salvando ? 'Salvando...' : 'Salvar' }}</button>
      <button type="button" class="secundario" @click="emit('cancelar')">Cancelar</button>
    </div>
  </form>
</template>