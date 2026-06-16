package controllers;

import play.data.validation.Constraints;
import play.data.validation.Constraints.Validate;
import play.data.validation.Constraints.Validatable;
import play.data.validation.Constraints;
import play.data.validation.ValidationError;
import model.groupConstraints.LoginCheck;
import model.groupConstraints.RegisterCheck;


@Validate(groups = {RegisterCheck.class})
public class UserData implements Validatable<ValidationError>{

    @Constraints.Required private String firstName;

    @Constraints.Required private String lastName;

    @Constraints.Required(groups = {RegisterCheck.class, LoginCheck.class})
    @Constraints.Email(groups = {RegisterCheck.class})
    private String email;

    @Constraints.Required(groups = {RegisterCheck.class, LoginCheck.class})
    private String password;

    @Constraints.Required(groups = {RegisterCheck.class})
    private String repeatPassword;

    public UserData (){
    }

    public UserData (String email){
        this.email = email;
    }

    @Override
    public ValidationError validate() {
        if (password != repeatPassword) {
            return new ValidationError("repeatPassword", "Passwords do not match");
        }
        return null;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public String getRepeatPassword() {
        return repeatPassword;
    }

    public void setRepeatPassword(String repeatPassword) {
        this.repeatPassword = repeatPassword;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}