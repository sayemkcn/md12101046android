package net.toracode.moviebuzz.entity;

import java.util.List;

/**
 * Created by SAyEM on 19-Aug-16.
 */
public class CategoryEntity {
    private Long id;
    private String name;
    private String iconUrl;
    private String accentColorCode;
    private List<NewsEntity> newsEntityList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccentColorCode() {
        return accentColorCode;
    }

    public void setAccentColorCode(String accentColorCode) {
        this.accentColorCode = accentColorCode;
    }


    public List<NewsEntity> getNewsEntityList() {
        return newsEntityList;
    }

    public void setNewsEntityList(List<NewsEntity> newsEntityList) {
        this.newsEntityList = newsEntityList;
    }

    @Override
    public String toString() {
        return "CategoryEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", accentColorCode='" + accentColorCode + '\'' +
                ", newsEntityList=" + newsEntityList +
                '}';
    }
}
