<script setup>
defineProps({ erro: Object })
</script>

<template>
  <div v-if="erro" class="caixa-erro" role="alert">
    <strong v-if="erro.titulo">{{ erro.titulo }}: </strong>{{ erro.message }}
    <ul v-if="erro.erros">
      <li v-for="(mensagem, campo) in erro.erros" :key="campo">{{ campo }}: {{ mensagem }}</li>
    </ul>
  </div>
</template>