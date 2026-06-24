package model.repositories;

import model.entities.User;
import org.junit.Test;
import org.junit.*;
import org.testcontainers.containers.PostgreSQLContainer;
import play.Application;
import play.inject.guice.GuiceApplicationBuilder;
import play.test.Helpers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * These tests are testing the basic fonctionnalities of the login repositorie by creating
 * a postgreSQL database in a container with the help of testcontainer
 */
public class TestLoginRepositories {

    // the testcontainer that simulate our DB
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    ) .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("Login.sql");


    LoginRepository repo;
    Application app;

    //starting the DB before doing those tests
    @BeforeClass
    public static void beforeAll() {
        postgres.start();
    }

    // stopping the Db after doing those tests
    @AfterClass
    public static void afterAll() {
        postgres.stop();
    }

    // setting up the config of our play app to be linked with our DB
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
        repo = app.injector().instanceOf(LoginRepository.class);
    }

    // stopping the app in case it is not after each test
    @After
    public void cleanup() {
        if (app != null) {
            Helpers.stop(app);
        }
    }

    @Test
    public void shouldExistUser() {

        // setup of a user to add
        User alice = new User();
        alice.setUsername("userAlice");
        alice.setEmail("alice@test.ch");
        alice.setName("alice");
        alice.setSurname("surAlice");
        alice.setOauth_provider(null);
        alice.setOauth_sub(null);
        alice.setPasswordHash("hashAlice");

        // adding a user
        repo.add(alice);

        // asserting that we get our user back
        Boolean resEmail = repo.existsByEmail(alice.getEmail())
                .toCompletableFuture()
                .join();
        Boolean resUsername = repo.existsByUsername(alice.getUsername())
                .toCompletableFuture()
                .join();

        assertEquals(resEmail, resUsername);
        assertTrue(resUsername);

        // cleaning up
        repo.remove(alice).toCompletableFuture().join();

    }

    @Test
    public void shouldGetUser() {

        // setup of a user to add
        User bob = new User();
        bob.setName("bob");
        bob.setSurname("surBob");
        bob.setUsername("userBob");
        bob.setEmail("bob@test.ch");
        bob.setOauth_provider("google");
        bob.setOauth_sub("bob'sid");
        bob.setPasswordHash("hashBob");

        // adding the user
        User saved = repo.add(bob)
                .toCompletableFuture()
                .join();

        User getted = repo.get(bob)
                .toCompletableFuture()
                .join();

        // asserting that we get the same user and that it gets rightfully added
        assertNotNull(saved);
        assertEquals(saved, getted);
        assertEquals(bob, getted);

        //cleaning up
        repo.remove(bob)
                .toCompletableFuture()
                .join();

    }

    @Test
    public void shouldGetAll() {

        // setup of users to get
        User bob = new User();
        bob.setName("bob");
        bob.setSurname("surBob");
        bob.setUsername("userBob");
        bob.setEmail("bob@test.ch");
        bob.setOauth_provider("google");
        bob.setOauth_sub("bob'sid");
        bob.setPasswordHash("hashBob");

        User alice = new User();
        alice.setUsername("userAlice");
        alice.setEmail("alice@test.ch");
        alice.setName("alice");
        alice.setSurname("surAlice");
        alice.setOauth_provider(null);
        alice.setOauth_sub(null);
        alice.setPasswordHash("hashAlice");

        // adding the users
        repo.add(bob);
        repo.add(alice);

        List<User> list = repo.getAll()
                .toCompletableFuture()
                .join();

        assertTrue(list.contains(alice));
        assertTrue(list.contains(bob));

        //cleaning up
        repo.remove(alice)
                .toCompletableFuture()
                .join();
        repo.remove(bob)
                .toCompletableFuture()
                .join();
    }
}
