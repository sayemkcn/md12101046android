package digital.edgelabs.bdbnnewsedgelabs.entity;

import android.graphics.Bitmap;

import java.util.List;

/**
 * Created by SAyEM on 19-Aug-16.
 */
public class NewsSourceEntity {
    private Long id;
    private String name;
    private String iconUrl;
    private List<NewsEntity> newsList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public List<NewsEntity> getNewsList() {
        return newsList;
    }

    public void setNewsList(List<NewsEntity> newsList) {
        this.newsList = newsList;
    }

    @Override
    public String toString() {
        return "NewsSourceEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", newsList=" + newsList +
                '}';
    }
}
