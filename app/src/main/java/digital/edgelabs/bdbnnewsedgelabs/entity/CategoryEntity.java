package digital.edgelabs.bdbnnewsedgelabs.entity;

import java.util.List;

/**
 * Created by SAyEM on 19-Aug-16.
 */
public class CategoryEntity {
    private Long id;
    private String name;
    private String iconUrl;
    private String accentColorCode;
    List<NewsSourceEntity> newsSourceList;

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


    public List<NewsSourceEntity> getNewsSourceList() {
        return newsSourceList;
    }

    public void setNewsSourceList(List<NewsSourceEntity> newsSourceList) {
        this.newsSourceList = newsSourceList;
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

    @Override
    public String toString() {
        return "CategoryEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", accentColorCode='" + accentColorCode + '\'' +
                ", newsSourceList=" + newsSourceList +
                '}';
    }
}
