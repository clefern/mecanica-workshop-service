package com.fiap.mecanica.workshop.bdd;

import com.fiap.mecanica.workshop.infra.persistence.document.MecanicoDocument;
import com.fiap.mecanica.workshop.infra.persistence.repository.ExecucaoMongoRepository;
import com.fiap.mecanica.workshop.infra.persistence.repository.MecanicoMongoRepository;
import com.fiap.mecanica.workshop.infra.persistence.repository.ProcessedCommandMongoRepository;
import io.cucumber.java.Before;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CucumberHooks {

  private final ExecucaoMongoRepository execucaoRepo;
  private final ProcessedCommandMongoRepository processedRepo;
  private final MecanicoMongoRepository mecanicoRepo;

  @Before
  public void limparCollections() {
    execucaoRepo.deleteAll();
    processedRepo.deleteAll();
    // Garante que mecânicos estão disponíveis (cenário de falha pode ter removido)
    if (mecanicoRepo.count() == 0) {
      mecanicoRepo.saveAll(List.of(
          MecanicoDocument.builder()
              .id(UUID.fromString("a1b2c3d4-0001-0001-0001-000000000001"))
              .nome("Carlos Silva").especialidade("Motor").build(),
          MecanicoDocument.builder()
              .id(UUID.fromString("a1b2c3d4-0002-0002-0002-000000000002"))
              .nome("Ana Souza").especialidade("Suspensão").build()
      ));
    }
  }
}
