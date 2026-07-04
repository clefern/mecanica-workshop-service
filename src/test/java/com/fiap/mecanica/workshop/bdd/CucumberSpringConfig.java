package com.fiap.mecanica.workshop.bdd;

import com.fiap.mecanica.workshop.infra.messaging.publisher.WorkshopEventPublisher;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.MongoDBContainer;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@ContextConfiguration(initializers = CucumberSpringConfig.MongoInitializer.class)
public class CucumberSpringConfig {

  static MongoDBContainer mongo = new MongoDBContainer("mongo:7.0");

  static {
    mongo.start();
  }

  public static class MongoInitializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext ctx) {
      TestPropertyValues.of(
          "spring.data.mongodb.uri=" + mongo.getConnectionString() + "/workshop_service"
      ).applyTo(ctx.getEnvironment());
    }
  }

  @MockBean
  WorkshopEventPublisher workshopEventPublisher;
}
