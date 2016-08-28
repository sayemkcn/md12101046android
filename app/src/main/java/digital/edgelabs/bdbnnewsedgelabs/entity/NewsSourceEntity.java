package digital.edgelabs.bdbnnewsedgelabs.entity;

import java.io.Serializable;

/**
 * Created by SAyEM on 19-Aug-16.
 */
public class NewsSourceEntity implements Serializable {
    private Long id;
    private String name;
    private String iconUrl;
    private String accentColorCode;

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

    public String getAccentColorCode() {
        return accentColorCode;
    }

    public void setAccentColorCode(String accentColorCode) {
        this.accentColorCode = accentColorCode;
    }

    @Override
    public String toString() {
        return "NewsSourceEntity{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", iconUrl='" + iconUrl + '\'' +
                ", accentColorCode='" + accentColorCode + '\'' +
                '}';
    }
}
