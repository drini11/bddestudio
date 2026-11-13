import { createRouter, createWebHistory } from 'vue-router'
import UsuariosView from '../views/UsuariosView.vue'
import ProjetosView from '../views/ProjetosView.vue'
import ProjetoView from '../views/ProjetoView.vue'
import ResumoView from '../views/ResumoView.vue'
import NovaHistoriaView from '../views/NovaHistoriaView.vue'
import HistoriaView from '../views/HistoriaView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [{ path: '/', name: 'usuarios', component: UsuariosView },
    { path: '/projetos', name: 'projetos', component: ProjetosView },
    { path: '/projetos/:id', name: 'projeto', component: ProjetoView },
    { path: '/projetos/:id/resumo', name: 'resumo', component: ResumoView },
    { path: '/projetos/:id/historias/nova', name: 'nova-historia', component: NovaHistoriaView },
    { path: '/historias/:id', name: 'historia', component: HistoriaView }],

})

export default router