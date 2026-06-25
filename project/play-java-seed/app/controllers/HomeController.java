package controllers;

import model.form.UserLoginForm;
import model.form.UserRegisterForm;
import model.repositories.LoginRepository;
import model.service.HashService;
import play.mvc.*;
import model.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import model.groupConstraints.LoginCheck;
import model.groupConstraints.RegisterCheck;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static play.libs.Scala.asScala;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home, login and register page.
 */
@Singleton
public class HomeController extends Controller {

    private final FormFactory formFactory;
    private final MessagesApi messagesApi;
    private final LoginRepository loginRepo;
    private final HashService hashService;

    private final Logger logger = LoggerFactory.getLogger(getClass()) ;


    @Inject
    public HomeController(FormFactory formFactory, MessagesApi messagesApi, LoginRepository loginRepository, HashService hashService) {
        this.formFactory = formFactory;
        this.messagesApi = messagesApi;
        this.loginRepo = loginRepository;
        this.hashService = hashService;

    }

    /**
     * This is the function that is called when we GET /
     *
     * @return the html page
     */
    public Result index() {
        return ok(views.html.index.render());
    }

    /**
     * This is the function that is called when we GET /login
     *
     * @return the html page
     */
    public Result login(Http.Request request) {
        return ok(views.html.login.render(formFactory.form(UserLoginForm.class, LoginCheck.class), request, messagesApi.preferred(request)));
    }

    /**
     * This is the function that is called when we GET /register
     *
     * @return the html page
     */
    public Result register(Http.Request request) {
        return ok(views.html.register.render(formFactory.form(UserRegisterForm.class, RegisterCheck.class), request, messagesApi.preferred(request)));
    }

    /**
     * This is the function that is called when we Post /register
     * <p>
     * it determines where to send the user based on his answer to the form
     *
     * @return the html page
     */
    public Result registerIn(Http.Request request) {
        final Form<UserRegisterForm> registerForm = formFactory.form(UserRegisterForm.class, RegisterCheck.class).bindFromRequest(request);

        if (registerForm.hasErrors()) {
            logger.error("errors = {}", registerForm.errors());
            return badRequest(views.html.register.render(registerForm, request, messagesApi.preferred(request)));
        } else {
            UserRegisterForm data = registerForm.get();

            if (loginRepo.getByEmail(data.getEmail()).toCompletableFuture().join() != null){
                return badRequest(views.html.register.render(formFactory.form(UserRegisterForm.class, RegisterCheck.class)
                                .withError("email", "this email is already used"),
                        request,
                        messagesApi.preferred(request)));
            } else {
                if (loginRepo.getByUsername(data.getUsername()).toCompletableFuture().join() != null){
                    return badRequest(views.html.register.render(formFactory.form(UserRegisterForm.class, RegisterCheck.class)
                                    .withError("username", "this username is already used"),
                            request,
                            messagesApi.preferred(request)));
                } else {
                    User newUser = new User(data.getFirstName(),
                            data.getLastName(),
                            data.getUsername(),
                            data.getEmail(),
                            hashService.hash(data.getPassword().toCharArray())
                    );
                    loginRepo.add(newUser);
                    return redirect(routes.HomeController.login());
                }
            }
        }
    }

    /**
     * This is the function that is called when we Post /login
     * <p>
     * it determines where to send the user based on his answer to the form
     *
     * @return the html page
     */
    public Result authenticate(Http.Request request) {
        final Form<UserLoginForm> loginForm = formFactory.form(UserLoginForm.class, LoginCheck.class).bindFromRequest(request);

        if (loginForm.hasErrors()) {
            logger.error("errors = {}", loginForm.errors());
            return badRequest(views.html.login.render( loginForm, request, messagesApi.preferred(request)));
        } else {
            UserLoginForm data = loginForm.get();

            // we determine whether he used his email ou username
            Pattern pattern = Pattern.compile("@", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(data.getUsernameOrMail());
            User user;

            // we look for the user
            if (matcher.find()){
                user = loginRepo.getByEmail(data.getUsernameOrMail()).toCompletableFuture().join();
            } else {
                user = loginRepo.getByUsername(data.getUsernameOrMail()).toCompletableFuture().join();
            }

            // we compare his password with the hash found if some user is found
            if (user != null && hashService.verify(user.getPasswordHash(), data.getPassword().toCharArray())){
                // c'est bon maintenant TODO faut crée la session
                return redirect(routes.HomeController.index());
            } else {
                return badRequest(views.html.login.render(loginForm.withError("login", "Invalid email or password."), request, messagesApi.preferred(request)));
            }

        }
    }
}
