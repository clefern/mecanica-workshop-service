package com.fiap.mecanica.workshop.bdd.steps;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.fiap.mecanica.workshop.application.messaging.ExecucaoFinalizadaEvent;
import com.fiap.mecanica.workshop.application.messaging.FalhaNaExecucaoEvent;
import com.fiap.mecanica.workshop.application.messaging.IniciarExecucaoCommand;
import com.fiap.mecanica.workshop.bdd.ScenarioContext;
import com.fiap.mecanica.workshop.domain.enums.StatusExecucao;
import com.fiap.mecanica.workshop.infra.messaging.listener.IniciarExecucaoListener;
import com.fiap.mecanica.workshop.infra.messaging.publisher.WorkshopEventPublisher;
import com.fiap.mecanica.workshop.infra.persistence.repository.ExecucaoMongoRepository;
import com.fiap.mecanica.workshop.infra.persistence.repository.MecanicoMongoRepository;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

@RequiredArgsConstructor
public class WorkshopSteps {

  private final ScenarioContext ctx;
  private final IniciarExecucaoListener listener;
  private final ExecucaoMongoRepository execucaoRepo;
  private final MecanicoMongoRepository mecanicoRepo;
  private final WorkshopEventPublisher workshopEventPublisher;

  @Autowired
  private TestRestTemplate restTemplate;

  @LocalServerPort
  private int port;

  private String baseUrl() {
    return "http://localhost:" + port;
  }

  private IniciarExecucaoCommand buildCommand(UUID sagaId, UUID osId) {
    return new IniciarExecucaoCommand(sagaId, osId);
  }

  @Quando("a saga solicita início de execução para uma OS")
  public void iniciarExecucao() {
    UUID sagaId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    ctx.setSagaId(sagaId);
    ctx.setOsId(osId);
    listener.onIniciarExecucao(buildCommand(sagaId, osId));
  }

  @Então("o evento ExecucaoFinalizada é publicado pelo workshop")
  public void verificarEventoExecucaoFinalizada() {
    verify(workshopEventPublisher, times(1)).publicarSucesso(any(ExecucaoFinalizadaEvent.class));
  }

  @E("a execução é salva no MongoDB com status FINALIZADA")
  public void verificarExecucaoSalva() {
    var execucao = execucaoRepo.findByOsId(ctx.getOsId());
    assertThat(execucao).isPresent();
    assertThat(execucao.get().getStatus()).isEqualTo(StatusExecucao.FINALIZADA);
    ctx.setExecucaoId(execucao.get().getId());
  }

  @Dado("que uma execução já foi processada para uma saga")
  public void execucaoJaProcessada() {
    UUID sagaId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    ctx.setSagaId(sagaId);
    ctx.setOsId(osId);
    listener.onIniciarExecucao(buildCommand(sagaId, osId));
    execucaoRepo.findByOsId(osId).ifPresent(e -> ctx.setExecucaoId(e.getId()));
  }

  @Quando("a saga solicita início de execução novamente com o mesmo sagaId")
  public void iniciarExecucaoDuplicada() {
    listener.onIniciarExecucao(buildCommand(ctx.getSagaId(), ctx.getOsId()));
  }

  @E("o evento ExecucaoFinalizada é publicado pelo workshop exatamente 1 vez")
  public void verificarIdempotencia() {
    verify(workshopEventPublisher, times(1)).publicarSucesso(any(ExecucaoFinalizadaEvent.class));
  }

  @Dado("que não há mecânicos cadastrados no workshop")
  public void removerMecanicos() {
    mecanicoRepo.deleteAll();
  }

  @Então("o evento FalhaNaExecucao é publicado pelo workshop")
  public void verificarEventoFalha() {
    verify(workshopEventPublisher, times(1)).publicarFalha(any(FalhaNaExecucaoEvent.class));
  }

  @Dado("que uma execução foi registrada para uma OS")
  public void criarExecucaoParaOs() {
    UUID sagaId = UUID.randomUUID();
    UUID osId = UUID.randomUUID();
    ctx.setSagaId(sagaId);
    ctx.setOsId(osId);
    listener.onIniciarExecucao(buildCommand(sagaId, osId));
    execucaoRepo.findByOsId(osId).ifPresent(e -> ctx.setExecucaoId(e.getId()));
  }

  @Quando("busco a execução pela OS via endpoint")
  public void buscarExecucaoPorOs() {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + ctx.getToken());
    ResponseEntity<String> resp = restTemplate.exchange(
        baseUrl() + "/api/execucoes/os/" + ctx.getOsId(),
        HttpMethod.GET,
        new HttpEntity<>(headers),
        String.class);
    ctx.setLastStatusCode(resp.getStatusCode().value());
  }

  @Então("recebo status HTTP {int}")
  public void verificarStatusHttp(int statusEsperado) {
    assertThat(ctx.getLastStatusCode()).isEqualTo(statusEsperado);
  }
}
