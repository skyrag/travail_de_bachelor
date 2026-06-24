package model.repositories;

import model.entities.User;
import org.junit.Test;
import org.junit.*;
import org.testcontainers.containers.PostgreSQLContainer;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.Helpers;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestLoginRepositories {
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    ) .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("Login.sql");


    loginRepository repo;
    Application app;

    @BeforeClass
    public static void beforeAll() {
        postgres.start();
    }

    @AfterClass
    public static void afterAll() {
        postgres.stop();
    }

    @Before
    public void setUp() {
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

        app = new GuiceApplicationBuilder()
                        .configure(config)
                        .build();
        repo = app.injector().instanceOf(loginRepository.class);
    }

    @After
    public void shutdown() {
        if (app != null) {
            Helpers.stop(app);
        }
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
