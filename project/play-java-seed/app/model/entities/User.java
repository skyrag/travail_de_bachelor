package model.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    public long id;

    public String username;
    public String surname;
    public String name;
    public String email;

    public String oauth_provider;
    public String oauth_sub;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOauth_provider() {
        return oauth_provider;
    }

    public void setOauth_provider(String oauth_provider) {
        this.oauth_provider = oauth_provider;
    }

    public String getOauth_sub() {
        return oauth_sub;
    }

    public void setOauth_sub(String oauth_sub) {
        this.oauth_sub = oauth_sub;
    }
}
