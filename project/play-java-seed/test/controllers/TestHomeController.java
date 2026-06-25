package controllers;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import model.entities.User;
import model.repositories.LoginRepository;
import model.service.HashService;
import org.junit.Test;
import play.Application;
import play.api.test.CSRFTokenHelper;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Http;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;

import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static play.mvc.Http.Status.OK;
import static play.test.Helpers.*;

/**
 * This class of test tests the fonctionnalities of our homeController that handles forms
 * and where to make the user go based on his answer
 * routes tested :
 *  - index
 *  - login
 *  - register
 */
public class TestHomeController extends WithApplication {

    private LoginRepository repo;
    private HashService hashService;

    @Override
    protected Application provideApplication() {

        User user = new User();
        user.setUsername("bob");
        user.setPasswordHash("hash");

        repo = mock(LoginRepository.class);
        hashService = mock(HashService.class);

        when(repo.getByUsername(any()))
                .thenReturn(CompletableFuture.completedFuture(null));
        when(repo.getByEmail(any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        when(repo.add(any())).thenReturn(CompletableFuture.completedFuture(user));

        when(hashService.verify(any(), any()))
                .thenReturn(true);

        return new GuiceApplicationBuilder()
                .overrides(new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(LoginRepository.class).toInstance(repo);
                        bind(HashService.class).toInstance(hashService);
                    }
                })
                .build();
    }

    @Test
    public void testIndex() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/");

        Result result = route(app, request);
        assertEquals(OK, result.status());
    }


    @Test
    public void testRegisterFormOk() {

        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyForm(ImmutableMap.of(
                        "email","bob@test.ch",
                        "username", "xXBoBSlayer62Xx",
                        "firstName", "Bob",
                        "lastName", "TheBuilder",
                        "password", "bobzabest",
                        "repeatPassword", "bobzabest"
                ))
                .uri("/register");

        request = CSRFTokenHelper.addCSRFToken(request);
        Result result = route(app, request);
        assertEquals(SEE_OTHER, result.status());
    }

    @Test
    public void testRegisterFormPasswordDifference() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyForm(ImmutableMap.of(
                        "email","bob@test.ch",
                        "username", "xXBoBSlayer62Xx",
                        "firstName", "Bob",
                        "lastName", "TheBuilder",
                        "password", "bobzabest",
                        "repeatPassword", "alice"
                ))
                .uri("/register");

        request = CSRFTokenHelper.addCSRFToken(request);
        Result result = route(app, request);
        assertEquals(BAD_REQUEST, result.status());
    }


    @Test
    public void testRegisterFormNotValidEmail() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyForm(ImmutableMap.of(
                        "email","test@@gmail.com",
                        "username", "xXBoBSlayer62Xx",
                        "firstName", "Bob",
                        "lastName", "TheBuilder",
                        "password", "bobzabest",
                        "repeatPassword", "bobzabest"
                ))
                .uri("/register");

        request = CSRFTokenHelper.addCSRFToken(request);
        Result result = route(app, request);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void testRegisterFormNoFields() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyForm(ImmutableMap.of(
                        "email","",
                        "username", "",
                        "firstName", "",
                        "lastName", "",
                        "password", "",
                        "repeatPassword", ""
                ))
                .uri("/register");

        request = CSRFTokenHelper.addCSRFToken(request);
        Result result = route(app, request);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void testLoginFormValidEmail() {
        User user = new User();
        user.setEmail("bob@test.ch");
        user.setPasswordHash("hash");

        when(repo.getByEmail(user.getEmail()))
                .thenReturn(CompletableFuture.completedFuture(user));

        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyForm(ImmutableMap.of(
                        "usernameOrMail","bob@test.ch",
                        "password", "bobzabest"
                ))
                .uri("/login");

        request = CSRFTokenHelper.addCSRFToken(request);
        Result result = route(app, request);
        assertEquals(SEE_OTHER, result.status());
    }

    @Test
    public void testLoginFormValidUsername() {
        User user = new User();
        user.setUsername("xXBoBSlayer62Xx");
        user.setPasswordHash("hash");

        when(repo.getByUsername(user.getUsername()))
                .thenReturn(CompletableFuture.completedFuture(user));

        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyForm(ImmutableMap.of(
                        "usernameOrMail","xXBoBSlayer62Xx",
                        "password", "bobzabest"
                ))
                .uri("/login");

        request = CSRFTokenHelper.addCSRFToken(request);
        Result result = route(app, request);
        assertEquals(SEE_OTHER, result.status());
    }

    @Test
    public void testLoginFormNotValidEmail() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyForm(ImmutableMap.of(
                        "usernameOrMail","test@@gmail.com",
                        "password", "bobzabest"
                ))
                .uri("/login");

        request = CSRFTokenHelper.addCSRFToken(request);
        Result result = route(app, request);
        assertEquals(BAD_REQUEST, result.status());
    }

    @Test
    public void testLoginFormNoFields() {
        Http.RequestBuilder request = Helpers.fakeRequest()
                .method(POST)
                .bodyForm(ImmutableMap.of(
                        "usernameOrMail","",
                        "password", ""
                ))
                .uri("/login");

        request = CSRFTokenHelper.addCSRFToken(request);
        Result result = route(app, request);
        assertEquals(BAD_REQUEST, result.status());
    }
}
