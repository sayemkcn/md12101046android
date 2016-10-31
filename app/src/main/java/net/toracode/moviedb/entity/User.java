package net.toracode.moviedb.entity;

import java.io.Serializable;

/**
 * Created by sayemkcn on 10/31/16.
 */

public class User extends BaseEntity implements Serializable{
    private String name;
    private String email;
    private String accountId;

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

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", accountId='" + accountId + '\'' +
                '}';
    }
}
