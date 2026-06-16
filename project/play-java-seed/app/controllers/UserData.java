package controllers;


import play.data.validation.Constraints;


public class UserData {

    @Constraints.Required protected String email;

    protected String password;

    public UserData() {

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
}