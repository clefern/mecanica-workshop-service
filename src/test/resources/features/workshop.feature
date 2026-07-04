# language: pt
Funcionalidade: Workshop — execução física do reparo
  Como sistema de oficina da mecânica
  Quero registrar e executar reparos atribuídos a mecânicos
  Para finalizar a Saga e entregar o veículo ao cliente

  Contexto:
    Dado que tenho um token de autenticação válido

  Cenário: Iniciar execução com mecânico disponível
    Quando a saga solicita início de execução para uma OS
    Então o evento ExecucaoFinalizada é publicado pelo workshop
    E a execução é salva no MongoDB com status FINALIZADA

  Cenário: Comando duplicado é ignorado por idempotência
    Dado que uma execução já foi processada para uma saga
    Quando a saga solicita início de execução novamente com o mesmo sagaId
    Então o evento ExecucaoFinalizada é publicado pelo workshop exatamente 1 vez

  Cenário: Falha quando não há mecânico disponível
    Dado que não há mecânicos cadastrados no workshop
    Quando a saga solicita início de execução para uma OS
    Então o evento FalhaNaExecucao é publicado pelo workshop

  Cenário: Buscar execução por OS via HTTP
    Dado que uma execução foi registrada para uma OS
    Quando busco a execução pela OS via endpoint
    Então recebo status HTTP 200
