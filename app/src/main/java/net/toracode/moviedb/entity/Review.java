package net.toracode.moviedb.entity;

import java.io.Serializable;

/**
 * Created by sayemkcn on 10/31/16.
 */

public class Review extends BaseEntity implements Serializable{
    private String title;
    private String message;
    private float rating;
    private User user;
    private Movie movie;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    @Override
    public String toString() {
        return "Review{" +
                "title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", rating=" + rating +
                ", user=" + user +
                ", movie=" + movie +
                '}';
    }
}
