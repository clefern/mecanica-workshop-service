package com.fiap.mecanica.workshop.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fiap.mecanica.workshop.application.messaging.ExecucaoFinalizadaEvent;
import com.fiap.mecanica.workshop.application.messaging.FalhaNaExecucaoEvent;
import com.fiap.mecanica.workshop.application.messaging.IniciarExecucaoCommand;
import com.fiap.mecanica.workshop.domain.enums.StatusExecucao;
import com.fiap.mecanica.workshop.infra.messaging.publisher.WorkshopEventPublisher;
import com.fiap.mecanica.workshop.infra.persistence.document.ExecucaoDocument;
import com.fiap.mecanica.workshop.infra.persistence.document.MecanicoDocument;
import com.fiap.mecanica.workshop.infra.persistence.repository.ExecucaoMongoRepository;
import com.fiap.mecanica.workshop.infra.persistence.repository.MecanicoMongoRepository;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WorkshopServiceTest {

  @Mock ExecucaoMongoRepository execucaoRepository;
  @Mock MecanicoMongoRepository mecanicoRepository;
  @Mock WorkshopEventPublisher publisher;

  @InjectMocks WorkshopService workshopService;

  @Test
  void executarReparo_comMecanicoDisponivel_deveSalvarExecucaoEPublicarSucesso() {
    UUID osId = UUID.randomUUID();
    UUID sagaId = UUID.randomUUID();
    UUID mecanicoId = UUID.randomUUID();
    IniciarExecucaoCommand command = new IniciarExecucaoCommand(sagaId, osId);

    MecanicoDocument mecanico = MecanicoDocument.builder()
        .id(mecanicoId)
        .nome("Carlos Silva")
        .especialidade("Motor")
        .build();

    when(mecanicoRepository.findFirstBy()).thenReturn(Optional.of(mecanico));
    when(execucaoRepository.save(any())).thenAnswer(i -> i.getArgument(0));

    workshopService.executarReparo(command);

    ArgumentCaptor<ExecucaoDocument> execCaptor = ArgumentCaptor.forClass(ExecucaoDocument.class);
    verify(execucaoRepository).save(execCaptor.capture());
    ExecucaoDocument execucao = execCaptor.getValue();
    assertThat(execucao.getOsId()).isEqualTo(osId);
    assertThat(execucao.getSagaId()).isEqualTo(sagaId);
    assertThat(execucao.getMecanicoId()).isEqualTo(mecanicoId);
    assertThat(execucao.getStatus()).isEqualTo(StatusExecucao.FINALIZADA);

    ArgumentCaptor<ExecucaoFinalizadaEvent> eventCaptor =
        ArgumentCaptor.forClass(ExecucaoFinalizadaEvent.class);
    verify(publisher).publicarSucesso(eventCaptor.capture());
    assertThat(eventCaptor.getValue().sagaId()).isEqualTo(sagaId);
    assertThat(eventCaptor.getValue().osId()).isEqualTo(osId);
    assertThat(eventCaptor.getValue().execucaoId()).isEqualTo(execucao.getId());
  }

  @Test
  void executarReparo_semMecanico_devePublicarFalhaSemSalvarExecucao() {
    UUID osId = UUID.randomUUID();
    UUID sagaId = UUID.randomUUID();
    IniciarExecucaoCommand command = new IniciarExecucaoCommand(sagaId, osId);

    when(mecanicoRepository.findFirstBy()).thenReturn(Optional.empty());

    workshopService.executarReparo(command);

    verify(execucaoRepository, never()).save(any());

    ArgumentCaptor<FalhaNaExecucaoEvent> eventCaptor =
        ArgumentCaptor.forClass(FalhaNaExecucaoEvent.class);
    verify(publisher).publicarFalha(eventCaptor.capture());
    assertThat(eventCaptor.getValue().sagaId()).isEqualTo(sagaId);
    assertThat(eventCaptor.getValue().osId()).isEqualTo(osId);
    assertThat(eventCaptor.getValue().motivo()).containsIgnoringCase("mecânico");
  }
}
