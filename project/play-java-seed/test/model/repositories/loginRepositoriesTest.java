package model.repositories;

import model.entities.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class loginRepositoriesTest {
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    ) .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("Login.sql");


    loginRepository repo;

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @BeforeEach
    void setUp() {
        System.out.println("on est la");
        Map<String, Object> config = new HashMap<>();
        config.put(
                "db.default.url",
                postgres.getJdbcUrl()
        );
        config.put(
                "db.default.password",
                postgres.getPassword()
        );
        config.put(
                "db.default.username",
                postgres.getUsername()
        );

        Application app =
                new GuiceApplicationBuilder()
                        .configure(config)
                        .build();
        repo = app.injector().instanceOf(loginRepository.class);
    }

    @Test
    public void shouldAddUser() {

        User user = new User();
        user.username = "alice";
        user.email = "alice@test.ch";
        user.name = "alice";
        user.surname = "alice";
        user.oauth_provider = null;
        user.oauth_sub = null;
        user.password_hash = "alice";

        User saved =
                repo.add(user)
                        .toCompletableFuture()
                        .join();

        assertNotNull(saved);
    }

}
