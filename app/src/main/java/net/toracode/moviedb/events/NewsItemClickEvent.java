package net.toracode.moviedb.events;

import net.toracode.moviedb.entity.NewsEntity;

/**
 * Created by sayemkcn on 8/23/16.
 */
public class NewsItemClickEvent {
    private NewsEntity news;

    public NewsItemClickEvent(NewsEntity news) {
        this.news = news;
    }

    public NewsEntity getNews() {
        return news;
    }

    public void setNews(NewsEntity news) {
        this.news = news;
    }
}
