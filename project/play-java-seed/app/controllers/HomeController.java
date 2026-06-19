package controllers;

import model.form.UserLoginForm;
import model.form.UserRegisterForm;
import play.mvc.*;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;
import model.groupConstraints.LoginCheck;
import model.groupConstraints.RegisterCheck;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static play.libs.Scala.asScala;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
@Singleton
public class HomeController extends Controller {

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    private final FormFactory formFactory;
    private MessagesApi messagesApi;
    private final List<User> users;

    private final Logger logger = LoggerFactory.getLogger(getClass()) ;


    @Inject
    public HomeController(FormFactory formFactory, MessagesApi messagesApi) {
        this.formFactory = formFactory;
        this.messagesApi = messagesApi;
        this.users = com.google.common.collect.Lists.newArrayList(
                new User("mail 1", "456"),
                new User("mail 2", "123"),
                new User("mail 3", "789")
        );

    }

    public Result index() {
        return ok(views.html.index.render());
    }

    // la fonction pour get le formulaire de login
    public Result login(Http.Request request) {
        return ok(views.html.login.render(asScala(users), formFactory.form(UserLoginForm.class, LoginCheck.class), request, messagesApi.preferred(request)));
    }

    // la fonction pour get le formulaire d'enregistrement
    public Result register(Http.Request request) {
        return ok(views.html.register.render(asScala(users), formFactory.form(UserRegisterForm.class, RegisterCheck.class), request, messagesApi.preferred(request)));
    }

    // la fonction a appeler a la fin du formulaire d'enregistrement pour enregistrer ou non la personne sur la db
    public Result registerIn(Http.Request request) {
        final Form<UserRegisterForm> registerForm = formFactory.form(UserRegisterForm.class, RegisterCheck.class).bindFromRequest(request);

        if (registerForm.hasErrors()) {
            logger.error("errors = {}", registerForm.errors());
            return badRequest(views.html.register.render(asScala(users), registerForm, request, messagesApi.preferred(request)));
        } else {
            UserRegisterForm data = registerForm.get();
            //faire l'appel a la DB pour checker si il y a déjà quelqu'un dans la DB avec la même addresse mail
            // Si il y a renoyer une erreur
            // Si il y a pas enregistrer la personne et la logger automatiquement
            if (false) { // TODO a changer quand on aura la DB
                return badRequest(views.html.register.render(asScala(users), formFactory.form(UserRegisterForm.class, RegisterCheck.class).fill(new UserRegisterForm(data.getEmail())), request, messagesApi.preferred(request)));
            } else {
                // appel a la DB et redirect sur la page principale
                users.add(new User(data.getEmail(), data.getPassword()));
                return redirect(routes.HomeController.login());
            }

        }

    }

    // la fonction a appeler a la fin du formulaire de login pour log la personne ou non.
    public Result authenticate(Http.Request request) {
        final Form<UserLoginForm> loginForm = formFactory.form(UserLoginForm.class, LoginCheck.class).bindFromRequest(request);

        if (loginForm.hasErrors()) {
            logger.error("errors = {}", loginForm.errors());
            return badRequest(views.html.login.render(asScala(users), loginForm, request, messagesApi.preferred(request)));
        } else {
            UserLoginForm data = loginForm.get();
            // faire un appel a la DB pour checker si les identifiant sont similaires
            // Si non on lève une erreur
            // Si oui alors on loggue la personne a son compte.
            if (false) { // TODO a changer lorsque l'on aura l'appel a la DB
                return badRequest(views.html.login.render(asScala(users), loginForm, request, messagesApi.preferred(request)));
            } else {
                //TODO- remplacer par le login lorsque l'on aura la DB
                return redirect(routes.HomeController.login());
            }
        }
    }
}
