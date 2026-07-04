package com.fiap.mecanica.workshop.bdd.steps;

import com.fiap.mecanica.workshop.bdd.ScenarioContext;
import io.cucumber.java.pt.Dado;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthSteps {

  private static final String SECRET =
      "ZGV2U2VjcmV0S2V5Rm9yTG9jYWxEZXZlbG9wbWVudE9ubHkzMkJ5dGVz";

  private final ScenarioContext ctx;

  @Dado("que tenho um token de autenticação válido")
  public void gerarToken() {
    byte[] key = Decoders.BASE64.decode(SECRET);
    String token = Jwts.builder()
        .subject("test@mecanica.com")
        .issuedAt(new Date())
        .expiration(new Date(System.currentTimeMillis() + 86_400_000))
        .signWith(Keys.hmacShaKeyFor(key))
        .compact();
    ctx.setToken(token);
  }
}
