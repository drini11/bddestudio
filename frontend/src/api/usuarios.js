import { requisitar } from './cliente'

export const listarUsuarios = () => requisitar('GET', '/api/usuarios')