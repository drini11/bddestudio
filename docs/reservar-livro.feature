# language: pt
@prioridade-media
Funcionalidade: Reservar livro
  Como estudante
  Quero reservar um livro indisponível
  Para garantir o acesso

  Cenário: Reserva de livro disponível
    Dado que o livro "Clean Code" está disponível
    Quando o estudante solicita a reserva
    Então a reserva é confirmada

  Cenário: Limite de reservas atingido
    Dado que o estudante já possui 3 reservas
    Quando ele tenta reservar outro livro
    Então o sistema recusa a reserva
    E informa o limite de 3 reservas

  @sugestao-ia
  Cenário: Cancelamento de reserva
    Dado que o estudante tem uma reserva ativa
    Quando ele cancela a reserva
    Então o livro volta para a fila

  @sugestao-ia
  Cenário: Reserva expira se o livro não for retirado
    Dado que o estudante reservou um livro que acabou de ficar disponível
    E o prazo de retirada é de 48 horas
    Quando passam 48 horas sem o estudante retirar o livro
    Então a reserva é cancelada
    E o livro é oferecido ao próximo estudante da fila
