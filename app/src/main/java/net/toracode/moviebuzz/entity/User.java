package net.toracode.moviebuzz.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by sayemkcn on 10/31/16.
 */

public class User implements Serializable {
    private Long uniqueId;
    private Date lastUpdated;
    private Date created;
    private String name;
    private String email;
    private String accountId;
    private String phone;

    public Long getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(Long uniqueId) {
        this.uniqueId = uniqueId;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
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

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "User{" +
                "uniqueId=" + uniqueId +
                ", lastUpdated=" + lastUpdated +
                ", created=" + created +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", accountId='" + accountId + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
