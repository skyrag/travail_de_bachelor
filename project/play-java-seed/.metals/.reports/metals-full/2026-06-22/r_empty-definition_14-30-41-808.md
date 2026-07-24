error id: file:///mnt/c/Users/Vivobook%20Pro/Documents/HEIG-VD/TB/travail_de_bachelor/project/play-java-seed/test/model/repositories/loginRepositoriesTest.java:org/junit/jupiter/api/BeforeAll#
file:///mnt/c/Users/Vivobook%20Pro/Documents/HEIG-VD/TB/travail_de_bachelor/project/play-java-seed/test/model/repositories/loginRepositoriesTest.java
empty definition using pc, found symbol in pc: org/junit/jupiter/api/BeforeAll#
empty definition using semanticdb
empty definition using fallback
non-local guesses:

offset: 165
uri: file:///mnt/c/Users/Vivobook%20Pro/Documents/HEIG-VD/TB/travail_de_bachelor/project/play-java-seed/test/model/repositories/loginRepositoriesTest.java
text:

```scala
package model.repositories;

import model.entities.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.@@

BeforeAll;

import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;

import java.util.HashMap;
import java.util.Map;

import static

org.junit.jupiter.api.Assertions.assertNotNull;

public

class loginRepositoriesTest {
  static PostgreSQLContainer <?> postgres = new PostgreSQLContainer <> (
    "postgres:16-alpine"
    ).withDatabaseName("testdb")
    .withUsername("test")
    .withPassword("test");

  LoginRepository repo;

  @BeforeAll
  static void beforeAll() {
    postgres.start();
  }

  @AfterAll
  static void afterAll() {
    postgres.stop();
  }

  @BeforeEach
  void setUp () {
    System.out.println("on est la");
    Map < String
    , Object > config = new HashMap <> ();
    config.put(
      "db.default.url",
      postgres.getJdbcUrl()
    );
    config.put(
      "db.default.username",
      postgres.getUsername()
    );
    config.put(
      "db.default.password",
      postgres.getPassword()
    );

    Application app =
      new GuiceApplicationBuilder()
        .configure(config)
        .build();
    repo = app.injector().instanceOf(LoginRepository.
    class);
  }

  @Test
  public void shouldAddUser() {

    User user = new User();
    user.username = "alice";
    user.email = "alice@test.ch";

    User saved =
      repo.add(user)
        .toCompletableFuture()
        .join();

    assertNotNull(saved);
  }

}

```


#### Short summary: 

empty definition using pc, found symbol in pc: org/junit/jupiter/api/BeforeAll#