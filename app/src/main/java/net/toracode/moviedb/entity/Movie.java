package net.toracode.moviedb.entity;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Created by sayemkcn on 10/17/2016.
 */

public class Movie implements Serializable {
    private int id;
    private String name;
    private String imageUrl;
    private String detailsUrl;
    private String releaseDate;
    private String directorName;
    private String producerName;
    private String detailsResponse;
    private String[] casts;
    private String rating;
    private String category;
    private String[] thumbnailUrls;

    public Movie() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getDirectorName() {
        return directorName;
    }

    public void setDirectorName(String directorName) {
        this.directorName = directorName;
    }


    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDetailsUrl() {
        return detailsUrl;
    }

    public void setDetailsUrl(String detailsUrl) {
        this.detailsUrl = detailsUrl;
    }

    public String[] getCasts() {
        return casts;
    }

    public void setCasts(String[] casts) {
        this.casts = casts;
    }

    public String getDetailsResponse() {
        return detailsResponse;
    }

    public void setDetailsResponse(String detailsResponse) {
        this.detailsResponse = detailsResponse;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getProducerName() {
        return producerName;
    }

    public void setProducerName(String producerName) {
        this.producerName = producerName;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", detailsUrl='" + detailsUrl + '\'' +
                ", releaseDate='" + releaseDate + '\'' +
                ", directorName='" + directorName + '\'' +
                ", producerName='" + producerName + '\'' +
                ", detailsResponse='" + detailsResponse + '\'' +
                ", casts=" + Arrays.toString(casts) +
                ", rating='" + rating + '\'' +
                ", category='" + category + '\'' +
                '}';
    }

    public String[] getThumbnailUrls() {
        return thumbnailUrls;
    }

    public void setThumbnailUrls(String[] thumbnailUrls) {
        this.thumbnailUrls = thumbnailUrls;
    }
}
