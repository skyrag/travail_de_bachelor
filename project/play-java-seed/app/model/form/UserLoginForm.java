package model.form;


import model.groupConstraints.LoginCheck;
import play.data.validation.Constraints;
import play.data.validation.Constraints.Validate;
import play.data.validation.ValidationError;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Form object used for user login.
 * <p>
 * This class is used by Play Framework to bind and validate
 * login form data coming from HTTP requests.
 * <p>
 * The user can authenticate using either
 * a username or an email address
 */
@Validate(groups = {LoginCheck.class})
public class UserLoginForm implements Constraints.Validatable<ValidationError> {

    @Constraints.Required(groups = {LoginCheck.class}) private String usernameOrMail;

    @Constraints.Required(groups = {LoginCheck.class}) private String password;


    public UserLoginForm() {

    }

    /**
     * Custom validation logic for login input.
     * <p>
     * If the input contains an "@", it is treated as an email and validated
     * against an email regex pattern. Otherwise, it is treated as a username.
     *
     * @return a ValidationError if the email format is invalid, otherwise null
     */
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
