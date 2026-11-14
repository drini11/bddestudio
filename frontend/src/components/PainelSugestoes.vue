<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { listarSugestoes, registrarSugestao, aceitarSugestao, rejeitarSugestao } from '../api/sugestoes'
import { usuarioAtual } from '../estado/usuario'
import { DECISOES, MOTIVOS } from '../utils/rotulos'
import { formatarData } from '../utils/formatar'
import MensagemErro from './MensagemErro.vue'

const props = defineProps({ historiaId: { type: [String, Number], required: true } })
const emit = defineEmits(['mudou'])

const sugestoes = ref([])
const filtro = ref('') // '' = todas
const carregando = ref(true)
const erroCarga = ref(null)

async function carregar() {
  carregando.value = true
  erroCarga.value = null
  try {
    sugestoes.value = await listarSugestoes(props.historiaId, filtro.value)
  } catch (e) {
    erroCarga.value = e
  } finally {
    carregando.value = false
  }
}

onMounted(carregar)
watch(filtro, carregar)

// ---- registrar uma sugestão ----
const registrando = ref(false)
const salvandoRegistro = ref(false)
const erroRegistro = ref(null)
const novo = reactive({ ferramenta: '', prompt: '', conteudo: '' })

function abrirRegistro() {
  erroRegistro.value = null
  registrando.value = true
}

async function registrar() {
  salvandoRegistro.value = true
  erroRegistro.value = null
  try {
    await registrarSugestao(props.historiaId, { ...novo })
    novo.ferramenta = ''
    novo.prompt = ''
    novo.conteudo = ''
    registrando.value = false
    await carregar()
    emit('mudou')
  } catch (e) {
    erroRegistro.value = e
  } finally {
    salvandoRegistro.value = false
  }
}

// ---- aceitar ou rejeitar ----
const decisaoAberta = reactive({ id: null, tipo: '', motivo: '', justificativa: '' })
const salvandoDecisao = ref(false)
const erroDecisao = ref(null)

function abrirDecisao(sugestao, tipo) {
  decisaoAberta.id = sugestao.id
  decisaoAberta.tipo = tipo
  decisaoAberta.motivo = ''
  decisaoAberta.justificativa = ''
  erroDecisao.value = null
}

function fecharDecisao() {
  decisaoAberta.id = null
}

async function confirmarDecisao() {
  salvandoDecisao.value = true
  erroDecisao.value = null
  try {
    const justificativa = decisaoAberta.justificativa || null
    if (decisaoAberta.tipo === 'aceitar') {
      await aceitarSugestao(decisaoAberta.id, { justificativa })
    } else {
      // sem motivo escolhido vai null, e quem responde 422 é a API
      await rejeitarSugestao(decisaoAberta.id, { motivo: decisaoAberta.motivo || null, justificativa })
    }
    fecharDecisao()
    await carregar()
    emit('mudou')
  } catch (e) {
    erroDecisao.value = e
  } finally {
    salvandoDecisao.value = false
  }
}
</script>

<template>
  <aside class="painel-sugestoes">
    <div class="cabecalho">
      <h2>Sugestões da IA</h2>
      <button v-if="!registrando" class="pequeno" @click="abrirRegistro">+ Registrar</button>
    </div>
    <p class="aviso">O sistema não chama nenhuma IA: você registra o que a ferramenta respondeu e decide aqui.</p>

    <p v-if="!usuarioAtual" class="caixa-aviso">Selecione um usuário para registrar ou decidir sugestões.</p>

    <form v-if="registrando" class="formulario" @submit.prevent="registrar">
      <label>Ferramenta <input v-model="novo.ferramenta" maxlength="100" placeholder="ChatGPT, Claude..." /></label>
      <label>Prompt usado <textarea v-model="novo.prompt" rows="3"></textarea></label>
      <label>Conteúdo recebido <textarea v-model="novo.conteudo" rows="5"></textarea></label>
      <MensagemErro :erro="erroRegistro" />
      <div class="botoes">
        <button type="submit" :disabled="salvandoRegistro">
          {{ salvandoRegistro ? 'Salvando...' : 'Registrar sugestão' }}
        </button>
        <button type="button" class="secundario" @click="registrando = false">Cancelar</button>
      </div>
    </form>

    <label class="filtro">
      Mostrar
      <select v-model="filtro">
        <option value="">Todas</option>
        <option v-for="(rotulo, valor) in DECISOES" :key="valor" :value="valor">{{ rotulo }}</option>
      </select>
    </label>

    <p v-if="carregando">Carregando...</p>
    <MensagemErro :erro="erroCarga" />
    <p v-if="!carregando && !erroCarga && sugestoes.length === 0">
      Nenhuma sugestão{{ filtro ? ' nesse filtro' : '' }}.
    </p>

    <article v-for="s in sugestoes" :key="s.id" class="sugestao">
      <header>
        <strong>{{ s.ferramenta }}</strong>
        <span :class="['etiqueta', 'decisao-' + s.decisao.toLowerCase()]">{{ DECISOES[s.decisao] }}</span>
      </header>
      <small class="aviso">Registrada por {{ s.registradaPorNome }} em {{ formatarData(s.registradaEm) }}</small>

      <details>
        <summary>Prompt usado</summary>
        <p class="texto-longo">{{ s.prompt }}</p>
      </details>
      <p class="texto-longo">{{ s.conteudo }}</p>

      <template v-if="s.decisao === 'PENDENTE'">
        <div v-if="decisaoAberta.id !== s.id" class="botoes">
          <button class="pequeno" @click="abrirDecisao(s, 'aceitar')">Aceitar</button>
          <button class="perigo pequeno" @click="abrirDecisao(s, 'rejeitar')">Rejeitar</button>
        </div>

        <form v-else class="formulario compacto" @submit.prevent="confirmarDecisao">
          <label v-if="decisaoAberta.tipo === 'rejeitar'">
            Motivo da rejeição
            <select v-model="decisaoAberta.motivo">
              <option value="">— escolha —</option>
              <option v-for="(rotulo, valor) in MOTIVOS" :key="valor" :value="valor">{{ rotulo }}</option>
            </select>
          </label>
          <label>
            Justificativa
            <small v-if="decisaoAberta.tipo === 'rejeitar'">(obrigatória se o motivo for Outro)</small>
            <textarea v-model="decisaoAberta.justificativa" rows="2"></textarea>
          </label>
          <MensagemErro :erro="erroDecisao" />
          <div class="botoes">
            <button type="submit" :disabled="salvandoDecisao">
              {{ decisaoAberta.tipo === 'aceitar' ? 'Confirmar aceite' : 'Confirmar rejeição' }}
            </button>
            <button type="button" class="secundario" @click="fecharDecisao">Cancelar</button>
          </div>
        </form>
      </template>

      <p v-else class="decisao-info">
        {{ DECISOES[s.decisao] }} por {{ s.decididaPorNome }} em {{ formatarData(s.decididaEm) }}
        <template v-if="s.motivoRejeicao"> — motivo: {{ MOTIVOS[s.motivoRejeicao] }}</template>
        <template v-if="s.justificativa"><br />“{{ s.justificativa }}”</template>
        <template v-if="s.decisao === 'ACEITA'">
          <br /><small>Aceitar não altera os cenários: ajuste-os à mão e marque a origem "Sugestão da IA".</small>
        </template>
      </p>
    </article>
  </aside>
</template>