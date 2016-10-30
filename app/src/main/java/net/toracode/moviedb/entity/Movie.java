package net.toracode.moviedb.entity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by sayemkcn on 10/17/2016.
 */

public class Movie extends BaseEntity implements Serializable {
    private String name;
    private String storyLine;
    private String type;
    private String language;
    private String imageUrl;
    private String industry;
    private String genere;
    private String trailerUrl;
    private Date releaseDate;
    private String duration;
    private String budget;
    private char rated;
    private String productionHouse;
    private List<Person> castAndCrewList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStoryLine() {
        return storyLine;
    }

    public void setStoryLine(String storyLine) {
        this.storyLine = storyLine;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getGenere() {
        return genere;
    }

    public void setGenere(String genere) {
        this.genere = genere;
    }

    public String getTrailerUrl() {
        return trailerUrl;
    }

    public void setTrailerUrl(String trailerUrl) {
        this.trailerUrl = trailerUrl;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public char getRated() {
        return rated;
    }

    public void setRated(char rated) {
        this.rated = rated;
    }

    public String getProductionHouse() {
        return productionHouse;
    }

    public void setProductionHouse(String productionHouse) {
        this.productionHouse = productionHouse;
    }

    public List<Person> getCastAndCrewList() {
        return castAndCrewList;
    }

    public void setCastAndCrewList(List<Person> castAndCrewList) {
        this.castAndCrewList = castAndCrewList;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "name='" + name + '\'' +
                ", storyLine='" + storyLine + '\'' +
                ", type='" + type + '\'' +
                ", language='" + language + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", industry='" + industry + '\'' +
                ", genere='" + genere + '\'' +
                ", trailerUrl='" + trailerUrl + '\'' +
                ", releaseDate=" + releaseDate +
                ", duration='" + duration + '\'' +
                ", budget='" + budget + '\'' +
                ", rated=" + rated +
                ", productionHouse='" + productionHouse + '\'' +
                ", castAndCrewList=" + castAndCrewList +
                '}';
    }
}
