<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { buscarHistoria, atualizarHistoria, excluirHistoria } from '../api/historias'
import { listarCenarios, criarCenario, atualizarCenario, excluirCenario } from '../api/cenarios'
import { usuarioAtual } from '../estado/usuario'
import { rotuloPrioridade, rotuloTipo, ORIGENS } from '../utils/rotulos'
import FormularioHistoria from '../components/FormularioHistoria.vue'
import EditorCenario from '../components/EditorCenario.vue'
import MensagemErro from '../components/MensagemErro.vue'

const route = useRoute()
const router = useRouter()
const historiaId = route.params.id

const historia = ref(null)
const cenarios = ref([])
const carregando = ref(true)
const erroCarga = ref(null)

const editandoHistoria = ref(false)
const salvandoHistoria = ref(false)
const erroHistoria = ref(null)

// null = nenhum aberto; 'novo' = criando; número = id do cenário em edição
const cenarioAberto = ref(null)
const salvandoCenario = ref(false)
const erroCenario = ref(null)

const erroAcao = ref(null) // erro ao excluir

// só informativo: quem decide é a API
const naoEAutor = computed(
    () => usuarioAtual.value && historia.value && usuarioAtual.value.id !== historia.value.autorId,
)

const inicialCenario = computed(() => {
  if (cenarioAberto.value === 'novo') {
    return {
      titulo: '',
      origem: 'HUMANO',
      passos: [
        { tipo: 'DADO', texto: '' },
        { tipo: 'QUANDO', texto: '' },
        { tipo: 'ENTAO', texto: '' },
      ],
    }
  }
  const c = cenarios.value.find((x) => x.id === cenarioAberto.value)
  return {
    titulo: c.titulo,
    origem: c.origem,
    passos: c.passos.map(({ tipo, texto }) => ({ tipo, texto })),
  }
})

onMounted(async () => {
  try {
    historia.value = await buscarHistoria(historiaId)
    cenarios.value = await listarCenarios(historiaId)
  } catch (e) {
    erroCarga.value = e
  } finally {
    carregando.value = false
  }
})

async function salvarHistoria(dados) {
  salvandoHistoria.value = true
  erroHistoria.value = null
  try {
    historia.value = await atualizarHistoria(historiaId, dados)
    editandoHistoria.value = false
  } catch (e) {
    erroHistoria.value = e
  } finally {
    salvandoHistoria.value = false
  }
}

async function excluirEstaHistoria() {
  if (!confirm('Excluir esta história, seus cenários e suas sugestões?')) return
  erroAcao.value = null
  try {
    await excluirHistoria(historiaId)
    router.push(`/projetos/${historia.value.projetoId}`)
  } catch (e) {
    erroAcao.value = e
  }
}

function abrirCenario(id) {
  erroCenario.value = null
  cenarioAberto.value = id
}

async function salvarCenario(dados) {
  salvandoCenario.value = true
  erroCenario.value = null
  try {
    if (cenarioAberto.value === 'novo') {
      await criarCenario(historiaId, dados)
    } else {
      await atualizarCenario(cenarioAberto.value, dados)
    }
    // recarrega da API: o que aparece é o que foi gravado, na ordem gravada
    cenarios.value = await listarCenarios(historiaId)
    cenarioAberto.value = null
  } catch (e) {
    erroCenario.value = e
  } finally {
    salvandoCenario.value = false
  }
}

async function excluirEsteCenario(cenario) {
  if (!confirm(`Excluir o cenário "${cenario.titulo}"?`)) return
  erroAcao.value = null
  try {
    await excluirCenario(cenario.id)
    cenarios.value = await listarCenarios(historiaId)
  } catch (e) {
    erroAcao.value = e
  }
}
</script>

<template>
  <section>
    <p v-if="carregando">Carregando...</p>
    <MensagemErro :erro="erroCarga" />

    <template v-if="historia">
      <p><RouterLink :to="`/projetos/${historia.projetoId}`">← Histórias do projeto</RouterLink></p>

      <p v-if="!usuarioAtual" class="caixa-aviso">
        Nenhum usuário selecionado: escolha um no menu "Usuário" para alterar esta história.
      </p>
      <p v-else-if="naoEAutor" class="caixa-aviso">
        Você está como {{ usuarioAtual.nome }}, mas o autor é {{ historia.autorNome }}.
        A API só aceita alterações do autor.
      </p>

      <div class="cabecalho">
        <h1>{{ historia.titulo }}</h1>
        <span :class="['etiqueta', 'prioridade-' + historia.prioridade.toLowerCase()]">
          {{ rotuloPrioridade(historia.prioridade) }}
        </span>
      </div>

      <FormularioHistoria
          v-if="editandoHistoria"
          :inicial="historia"
          :salvando="salvandoHistoria"
          :erro="erroHistoria"
          @salvar="salvarHistoria"
          @cancelar="editandoHistoria = false"
      />

      <template v-else>
        <p class="narrativa">
          Como <strong>{{ historia.papel }}</strong>,
          quero <strong>{{ historia.objetivo }}</strong>,
          para <strong>{{ historia.beneficio }}</strong>.
        </p>
        <p class="aviso">Autor: {{ historia.autorNome }}</p>
        <div class="botoes">
          <button class="secundario" @click="editandoHistoria = true">Editar história</button>
          <button class="perigo" @click="excluirEstaHistoria">Excluir história</button>
        </div>
      </template>

      <MensagemErro :erro="erroAcao" />

      <div class="cabecalho secao">
        <h2>Cenários</h2>
        <button v-if="cenarioAberto === null" @click="abrirCenario('novo')">+ Novo cenário</button>
      </div>

      <EditorCenario
          v-if="cenarioAberto !== null"
          :key="cenarioAberto"
          :inicial="inicialCenario"
          :salvando="salvandoCenario"
          :erro="erroCenario"
          @salvar="salvarCenario"
          @cancelar="cenarioAberto = null"
      />

      <p v-if="cenarios.length === 0 && cenarioAberto === null">Esta história ainda não tem cenários.</p>

      <article v-for="c in cenarios" :key="c.id" class="cenario">
        <header>
          <div>
            <strong>{{ c.titulo }}</strong>
            <span class="etiqueta">{{ ORIGENS[c.origem] ?? c.origem }}</span>
          </div>
          <div class="botoes">
            <button class="secundario pequeno" @click="abrirCenario(c.id)">Editar</button>
            <button class="perigo pequeno" @click="excluirEsteCenario(c)">Excluir</button>
          </div>
        </header>
        <ol class="passos">
          <li v-for="p in c.passos" :key="p.id">
            <span class="tipo">{{ rotuloTipo(p.tipo) }}</span> {{ p.texto }}
          </li>
        </ol>
      </article>
    </template>
  </section>
</template>