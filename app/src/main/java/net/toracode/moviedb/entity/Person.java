package net.toracode.moviedb.entity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by sayemkcn on 10/30/16.
 */

public class Person implements Serializable {
    private Long uniqueId;
    private Date lastUpdated;
    private Date created;
    private String name;
    private String[] designations;
    private Date birthDate;
    private String bio;
    private String[] awards;
    private String[] socialLinks;

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

    public String[] getDesignations() {
        return designations;
    }

    public void setDesignations(String[] designations) {
        this.designations = designations;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String[] getAwards() {
        return awards;
    }

    public void setAwards(String[] awards) {
        this.awards = awards;
    }

    public String[] getSocialLinks() {
        return socialLinks;
    }

    public void setSocialLinks(String[] socialLinks) {
        this.socialLinks = socialLinks;
    }

    @Override
    public String toString() {
        return "Person{" +
                "uniqueId=" + uniqueId +
                ", lastUpdated=" + lastUpdated +
                ", created=" + created +
                ", name='" + name + '\'' +
                ", designations=" + Arrays.toString(designations) +
                ", birthDate=" + birthDate +
                ", bio='" + bio + '\'' +
                ", awards=" + Arrays.toString(awards) +
                ", socialLinks=" + Arrays.toString(socialLinks) +
                '}';
    }
}
