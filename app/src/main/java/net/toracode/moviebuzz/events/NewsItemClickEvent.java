package net.toracode.moviebuzz.events;

import net.toracode.moviebuzz.entity.NewsEntity;

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
