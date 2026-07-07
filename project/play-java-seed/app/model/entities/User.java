package model.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a user of the application.
 * <p>
 * A user can authenticate either through a local account
 * using a password hash or through an external OAuth provider.
 * The entity is persisted in the {@code users} table.
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String username;
    private String surname;
    private String name;
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    /**
     * OAuth provider name (e.g. Google, GitHub).
     */
    @Column(name = "oauth_provider")
    private String oauthProvider;

    /**
     * Unique identifier provided by the OAuth provider.
     */
    @Column(name = "oauth_sub")
    private String oauthSub;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Team> teams = new ArrayList<>();

    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private LocalDateTime createdAt;

    public User(String name, String surname, String username, String email, String passwordHash) {
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    public User(String name, String surname,String username, String email, String oauthProvider, String oauthSub) {
        this.name = name;
        this.surname = surname;
        this.username = username;
        this.email = email;
        this.oauthProvider = oauthProvider;
        this.oauthSub = oauthSub;
    }

    public User() {

    }
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getOauth_provider() {
        return oauthProvider;
    }

    public void setOauth_provider(String oauth_provider) {
        this.oauthProvider = oauth_provider;
    }

    public String getOauth_sub() {
        return oauthSub;
    }

    public void setOauth_sub(String oauth_sub) {
        this.oauthSub = oauth_sub;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof User u) {
            return Objects.equals(u.id, this.id);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Long.hashCode(id);
    }
}
