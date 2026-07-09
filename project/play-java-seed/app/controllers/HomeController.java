package controllers;

import model.actor.BridgeActor;
import model.actor.ConnexionActor;
import model.actor.GameActor;
import model.form.UserLoginForm;
import model.form.UserRegisterForm;
import model.monitor.ConnexionMonitor;
import model.repositories.GameRepository;
import model.repositories.LoginRepository;
import model.service.HashService;
import model.service.MatchmakingService;
import model.service.SeedMakerService;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.javadsl.Adapter;
import org.apache.pekko.stream.Materializer;
import org.apache.pekko.stream.OverflowStrategy;
import play.libs.streams.ActorFlow;
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

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static play.libs.Scala.asScala;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home, login and register page.
 */
@Singleton
public class HomeController extends Controller {

    private final FormFactory formFactory;
    private final MessagesApi messagesApi;
    private final ActorSystem actorSystem;
    private final Materializer materializer;


    private final LoginRepository loginRepo;
    private final GameRepository gameRepo;

    private final HashService hashService;
    private final SeedMakerService seedGenerator;
    private final MatchmakingService matchmakingService;

    private final ConnexionMonitor connexions;


    private final Object lock = new Object();


    private final Logger logger = LoggerFactory.getLogger(getClass()) ;


    @Inject
    public HomeController(FormFactory formFactory,
                          MessagesApi messagesApi,
                          LoginRepository loginRepository,
                          GameRepository gameRepo,
                          HashService hashService,
                          ActorSystem actorSystem,
                          Materializer materializer,
                          SeedMakerService seedGenerator,
                          ConnexionMonitor connexions,
                          MatchmakingService matchmakingService) {
        this.formFactory = formFactory;
        this.messagesApi = messagesApi;
        this.loginRepo = loginRepository;
        this.hashService = hashService;
        this.actorSystem = actorSystem;
        this.materializer = materializer;
        this.seedGenerator = seedGenerator;
        this.gameRepo = gameRepo;
        this.connexions = connexions;
        this.matchmakingService = matchmakingService;
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

    public CompletionStage<Result> game(Http.Request request) {
        String userId = request.session().get("userId")
                .orElseThrow(() -> new RuntimeException("Unauthorized"));
        CompletionStage<ActorRef<GameActor.Message>> gameActor;
        synchronized (lock) {
            gameActor = matchmakingService.addPlayer(userId);
        }
        return gameActor.thenApply(actor -> {
           if (actor == null) {
               return ok(views.html.index.render());
           }
           //TODO c'est ici qu'on peut faire quelque chose avec ce gameActor si besoin
            // TODO replace with the game screen because the game is OOOOOONNNNN!!!
            return ok(views.html.game.render(request));
        });
    }

    /**
     * This is the function that is called when we Post /register
     * <p>
     * it determines where to send the user based on his answer to the form
     *
     * @return the html page
     */
    public CompletionStage<Result> registerIn(Http.Request request) {
        final Form<UserRegisterForm> registerForm = formFactory.form(UserRegisterForm.class, RegisterCheck.class).bindFromRequest(request);

        if (registerForm.hasErrors()) {
            logger.error("errors = {}", registerForm.errors());
            return CompletableFuture.completedFuture(badRequest(views.html.register.render(registerForm, request, messagesApi.preferred(request))));
        }

        UserRegisterForm data = registerForm.get();

        return loginRepo.getByEmail(data.getEmail()).thenCompose(existingEmail -> {
            if (existingEmail != null) {
                return CompletableFuture.completedFuture(badRequest(views.html.register.render(formFactory.form(UserRegisterForm.class, RegisterCheck.class)
                                .withError("email", "this email is already used"),
                        request,
                        messagesApi.preferred(request))));
            }

            return loginRepo.getByUsername(data.getUsername()).thenApply(existingUsername -> {
                if (existingUsername != null) {
                    return badRequest(views.html.register.render(formFactory.form(UserRegisterForm.class, RegisterCheck.class)
                                    .withError("username", "this username is already used"),
                            request,
                            messagesApi.preferred(request)));
                }
                User newUser = new User(data.getFirstName(),
                        data.getLastName(),
                        data.getUsername(),
                        data.getEmail(),
                        hashService.hash(data.getPassword().toCharArray())
                );
                loginRepo.add(newUser);
                return redirect(routes.HomeController.login());

            });
        });

    }

    /**
     * This is the function that is called when we Post /login
     * <p>
     * it determines where to send the user based on his answer to the form
     *
     * @return the html page
     */
    public CompletionStage<Result> authenticate(Http.Request request) {
        final Form<UserLoginForm> loginForm = formFactory.form(UserLoginForm.class, LoginCheck.class).bindFromRequest(request);

        if (loginForm.hasErrors()) {
            logger.error("errors = {}", loginForm.errors());
            return CompletableFuture.completedFuture(badRequest(views.html.login.render( loginForm, request, messagesApi.preferred(request))));
        }

        UserLoginForm data = loginForm.get();
        boolean isEmail = data.getUsernameOrMail().contains("@");

        CompletionStage<User> userLookup = isEmail
                ? loginRepo.getByEmail(data.getUsernameOrMail())
                : loginRepo.getByUsername(data.getUsernameOrMail());

        return userLookup.thenApply(existingUser -> {
            if (existingUser != null && hashService.verify(existingUser.getPasswordHash(), data.getPassword().toCharArray())) {
                return redirect(routes.HomeController.game()).addingToSession(request, "userId", existingUser.getStringId());
            }
            return badRequest(views.html.login.render(
                    loginForm.withError("login", "Invalid email or password."), request, messagesApi.preferred(request)));
        });
    }

    public WebSocket webSocket() {
        return WebSocket.Json.accept(request -> {
            String userId = request.session().get("userId")
                    .orElseThrow(() -> new RuntimeException("Unauthorized"));
            System.out.println("banger");
            return ActorFlow.actorRef(out ->
                            BridgeActor.create(out, getUserActor(userId, out)),
                    256,
                    OverflowStrategy.dropHead(),
                    actorSystem,
                    materializer);
        });
    }

    private ActorRef<ConnexionActor.Message> getUserActor(String userId, org.apache.pekko.actor.ActorRef ws){
        ActorRef<ConnexionActor.Message> user = connexions.getActorFromId(userId);

        if (user != null){
            return user;
        }

        user = Adapter.spawn(
                actorSystem,
                ConnexionActor.create(ws, Long.parseLong(userId)),
                userId
        );
        connexions.addConnexion(userId, user);
        return user;
    }
}
