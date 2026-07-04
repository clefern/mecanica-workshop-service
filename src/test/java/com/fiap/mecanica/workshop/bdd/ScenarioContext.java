package com.fiap.mecanica.workshop.bdd;

import io.cucumber.spring.ScenarioScope;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
@ScenarioScope
public class ScenarioContext {

  private String token;
  private UUID sagaId;
  private UUID osId;
  private UUID execucaoId;
  private int lastStatusCode;

  public String getToken() { return token; }
  public void setToken(String token) { this.token = token; }

  public UUID getSagaId() { return sagaId; }
  public void setSagaId(UUID sagaId) { this.sagaId = sagaId; }

  public UUID getOsId() { return osId; }
  public void setOsId(UUID osId) { this.osId = osId; }

  public UUID getExecucaoId() { return execucaoId; }
  public void setExecucaoId(UUID execucaoId) { this.execucaoId = execucaoId; }

  public int getLastStatusCode() { return lastStatusCode; }
  public void setLastStatusCode(int lastStatusCode) { this.lastStatusCode = lastStatusCode; }
}
