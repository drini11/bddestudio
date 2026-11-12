<script setup>
import { RouterLink, RouterView } from 'vue-router'
import { usuarioAtual } from './estado/usuario'
</script>

<template>
  <header class="topo">
    <strong>Estúdio BDD</strong>
    <nav>
      <RouterLink to="/">Usuário</RouterLink>
      <RouterLink to="/projetos">Projetos</RouterLink>
    </nav>
    <span class="usuario-atual">
      <template v-if="usuarioAtual">Usuário: {{ usuarioAtual.nome }}</template>
      <template v-else>Nenhum usuário selecionado</template>
    </span>
  </header>

  <main>
    <RouterView />
  </main>
</template>