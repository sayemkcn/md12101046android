package digital.edgelabs.bdbnnewsedgelabs.entity;

import java.util.Date;

/**
 * Created by sayemkcn on 8/10/16.
 */
public class NewsEntity {
    private Long id;
    private Long sourceId;
    private String title;
    private String details;
    private String imageUrl;
    private String author;
    private Date lastUpdated;
    private NewsSourceEntity newsSourceEntity;

    public NewsEntity() {
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

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public NewsSourceEntity getNewsSourceEntity() {
        return newsSourceEntity;
    }

    public void setNewsSourceEntity(NewsSourceEntity newsSourceEntity) {
        this.newsSourceEntity = newsSourceEntity;
    }

    @Override
    public String toString() {
        return "NewsEntity{" +
                "id=" + id +
                ", sourceId=" + sourceId +
                ", title='" + title + '\'' +
                ", details='" + details + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", author='" + author + '\'' +
                ", lastUpdated=" + lastUpdated +
                ", newsSourceEntity=" + newsSourceEntity +
                '}';
    }
}
