package controllers;

import play.mvc.*;
import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.i18n.MessagesApi;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

import static play.libs.Scala.asScala;

/**
 * This controller contains an action to handle HTTP requests
 * to the application's home page.
 */
public class HomeController extends Controller {

    /**
     * An action that renders an HTML page with a welcome message.
     * The configuration in the <code>routes</code> file means that
     * this method will be called when the application receives a
     * <code>GET</code> request with a path of <code>/</code>.
     */
    private final Form<UserData> form;
    private MessagesApi messagesApi;
    private final List<User> users;

    private final Logger logger = LoggerFactory.getLogger(getClass()) ;


    @Inject
    public HomeController(FormFactory formFactory, MessagesApi messagesApi) {
        this.form = formFactory.form(UserData.class);
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

    public Result login(Http.Request request) {
        return ok(views.html.login.render(asScala(users), form, request, messagesApi.preferred(request)));
    }

    public Result authenticate(Http.Request request) {
        final Form<UserData> loginForm = form.bindFromRequest(request);

        if (loginForm.hasErrors()) {
            logger.error("errors = {}", loginForm.errors());
            return badRequest(views.html.login.render(asScala(users), loginForm, request, messagesApi.preferred(request)));
        } else {
            UserData data = loginForm.get();
            users.add(new User(data.getEmail(), data.getPassword()));
            return redirect(routes.HomeController.login())
                    .flashing("info", "User added!");
        }
    }
}
