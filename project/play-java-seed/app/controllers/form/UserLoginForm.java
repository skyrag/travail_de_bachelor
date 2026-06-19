package controllers.form;


import model.groupConstraints.LoginCheck;
import model.groupConstraints.RegisterCheck;
import play.data.validation.Constraints;
import play.data.validation.Constraints.Validate;
import play.data.validation.ValidationError;

import javax.validation.Constraint;
import java.util.Objects;

@Validate(groups = {LoginCheck.class})
public class UserLoginForm implements Constraints.Validatable<ValidationError> {

    @Constraints.Required private String usernameOrMail;

    @Constraints.Required private String password;


    public UserLoginForm() {

    }

    @Override
    public ValidationError validate() {
        // ^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$
        // check un premier regex pour savoir si le string a un @
        // ensuite si c'est un mail on check avec notre regex
        // sinon all good ?
        return null;
    }
}
