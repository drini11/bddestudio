<script setup>
import { reactive } from 'vue'
import { ORIGENS, TIPOS_PASSO } from '../utils/rotulos'
import MensagemErro from './MensagemErro.vue'

const props = defineProps({
  inicial: { type: Object, required: true },
  salvando: Boolean,
  erro: Object,
})
const emit = defineEmits(['salvar', 'cancelar'])

// "chave" só serve para o Vue reconhecer cada linha ao reordenar; não vai para a API
let proximaChave = 0
const novoPasso = (tipo, texto) => ({ chave: proximaChave++, tipo, texto })

const form = reactive({
  titulo: props.inicial.titulo,
  origem: props.inicial.origem,
  passos: props.inicial.passos.map((p) => novoPasso(p.tipo, p.texto)),
})

function adicionarPasso() {
  form.passos.push(novoPasso(form.passos.length > 0 ? 'E' : 'DADO', ''))
}

function removerPasso(i) {
  form.passos.splice(i, 1)
}

function mover(i, delta) {
  const j = i + delta
  if (j < 0 || j >= form.passos.length) return
  const [passo] = form.passos.splice(i, 1)
  form.passos.splice(j, 0, passo)
}

function enviar() {
  emit('salvar', {
    titulo: form.titulo,
    origem: form.origem,
    passos: form.passos.map(({ tipo, texto }) => ({ tipo, texto })),
  })
}
</script>

<template>
  <form class="formulario" @submit.prevent="enviar">
    <label>Título do cenário <input v-model="form.titulo" maxlength="150" /></label>

    <label>
      Origem
      <select v-model="form.origem">
        <option v-for="(rotulo, valor) in ORIGENS" :key="valor" :value="valor">{{ rotulo }}</option>
      </select>
    </label>

    <fieldset>
      <legend>Passos (a ordem desta lista é a ordem do cenário)</legend>

      <div v-for="(passo, i) in form.passos" :key="passo.chave" class="passo-linha">
        <select v-model="passo.tipo" class="tipo-passo">
          <option v-for="t in TIPOS_PASSO" :key="t.valor" :value="t.valor">{{ t.rotulo }}</option>
        </select>
        <input v-model="passo.texto" maxlength="255" placeholder="Texto do passo" />
        <button type="button" class="secundario pequeno" :disabled="i === 0" title="Subir" @click="mover(i, -1)">↑</button>
        <button type="button" class="secundario pequeno" :disabled="i === form.passos.length - 1" title="Descer" @click="mover(i, 1)">↓</button>
        <button type="button" class="perigo pequeno" title="Remover" @click="removerPasso(i)">✕</button>
      </div>

      <div><button type="button" class="secundario" @click="adicionarPasso">+ Adicionar passo</button></div>
    </fieldset>

    <MensagemErro :erro="erro" />

    <div class="botoes">
      <button type="submit" :disabled="salvando">{{ salvando ? 'Salvando...' : 'Salvar cenário' }}</button>
      <button type="button" class="secundario" @click="emit('cancelar')">Cancelar</button>
    </div>
  </form>
</template>