package digital.edgelabs.bdbnnewsedgelabs.entity;

import java.util.Date;

/**
 * Created by sayemkcn on 8/10/16.
 */
public class NewsEntity {
    private Long id;
    private String title;
    private String details;
    private Date lastUpdated;

    public NewsEntity(Long id, String title, String details, Date lastUpdated) {
        this.id = id;
        this.title = title;
        this.details = details;
        this.lastUpdated = lastUpdated;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "NewsEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", details='" + details + '\'' +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
