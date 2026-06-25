package com.fiap.mecanica.workshop.infra.messaging.listener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.workshop.application.messaging.IniciarExecucaoCommand;
import com.fiap.mecanica.workshop.domain.service.WorkshopService;
import com.fiap.mecanica.workshop.infra.messaging.publisher.WorkshopEventPublisher;
import com.fiap.mecanica.workshop.infra.persistence.repository.ProcessedCommandMongoRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class IniciarExecucaoListenerTest {

  @Mock WorkshopService workshopService;
  @Mock WorkshopEventPublisher publisher;
  @Mock ProcessedCommandMongoRepository processedCommandRepo;

  @InjectMocks IniciarExecucaoListener listener;

  @Test
  void onIniciarExecucao_novoComando_deveProcessar() {
    UUID sagaId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    IniciarExecucaoCommand command = new IniciarExecucaoCommand(sagaId, osId);

    when(processedCommandRepo.existsBySagaId(sagaId)).thenReturn(false);
    when(processedCommandRepo.save(any())).thenAnswer(i -> i.getArgument(0));

    listener.onIniciarExecucao(command);

    verify(workshopService).executarReparo(command);
    verify(publisher, never()).publicarFalha(any());
  }

  @Test
  void onIniciarExecucao_comandoDuplicado_deveIgnorar() {
    UUID sagaId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    IniciarExecucaoCommand command = new IniciarExecucaoCommand(sagaId, osId);

    when(processedCommandRepo.existsBySagaId(sagaId)).thenReturn(true);

    listener.onIniciarExecucao(command);

    verify(workshopService, never()).executarReparo(any());
    verify(publisher, never()).publicarFalha(any());
  }

  @Test
  void onIniciarExecucao_erroNoServico_devePublicarFalha() {
    UUID sagaId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    IniciarExecucaoCommand command = new IniciarExecucaoCommand(sagaId, osId);

    when(processedCommandRepo.existsBySagaId(sagaId)).thenReturn(false);
    when(processedCommandRepo.save(any())).thenAnswer(i -> i.getArgument(0));
    doThrow(new RuntimeException("erro inesperado"))
        .when(workshopService).executarReparo(command);

    listener.onIniciarExecucao(command);

    verify(publisher).publicarFalha(argThat(e ->
        e.sagaId().equals(sagaId) && e.osId().equals(osId)
            && e.motivo().contains("Erro interno")));
  }
}
