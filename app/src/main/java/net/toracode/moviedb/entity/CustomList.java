package net.toracode.moviedb.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by sayemkcn on 11/2/16.
 */

public class CustomList implements Serializable {
    private Long uniqueId;
    private Date lastUpdated;
    private Date created;
    private String title;
    private String description;
    private String type;
    private List<Movie> movieList;
    private User user;
    private List<User> followerList;

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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Movie> getMovieList() {
        return movieList;
    }

    public void setMovieList(List<Movie> movieList) {
        this.movieList = movieList;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public List<User> getFollowerList() {
        return followerList;
    }

    public void setFollowerList(List<User> followerList) {
        this.followerList = followerList;
    }

    @Override
    public String toString() {
        return "CustomList{" +
                "uniqueId=" + uniqueId +
                ", lastUpdated=" + lastUpdated +
                ", created=" + created +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", type='" + type + '\'' +
                ", movieList=" + movieList +
                ", user=" + user +
                ", followerList=" + followerList +
                '}';
    }
}

