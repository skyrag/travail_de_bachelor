package model.form;


import model.groupConstraints.LoginCheck;
import model.groupConstraints.RegisterCheck;
import play.data.validation.Constraints;
import play.data.validation.Constraints.Validate;
import play.data.validation.ValidationError;

import javax.validation.Constraint;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Validate(groups = {LoginCheck.class})
public class UserLoginForm implements Constraints.Validatable<ValidationError> {

    @Constraints.Required(groups = {LoginCheck.class}) private String usernameOrMail;

    @Constraints.Required(groups = {LoginCheck.class}) private String password;


    public UserLoginForm() {

    }

    @Override
    public ValidationError validate() {
        Pattern pattern = Pattern.compile("@", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(usernameOrMail);
        if (matcher.find()){
            pattern = Pattern.compile("^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$", Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(usernameOrMail);
            if (matcher.find()){
                return null;
            } else {
                return new ValidationError("usernameOrMail", "is not a valid email ");
            }
        } else {
            return null;
        }
        // check un premier regex pour savoir si le string a un @
        // ensuite si c'est un mail on check avec notre regex
        // sinon all good ?
    }

    public void setUsernameOrMail(String usernameOrMail) {
        this.usernameOrMail = usernameOrMail;
    }

    public String getUsernameOrMail() {
        return usernameOrMail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
