package net.toracode.moviedb.entity;

import java.util.Date;

/**
 * Created by sayemkcn on 10/30/16.
 */

public abstract class BaseEntity {
    private Long uniqueId;
    private Date lastUpdated;
    private Date created;

    public Long getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(Long uniqueId) {
        this.uniqueId = uniqueId;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return "BaseEntity{" +
                "uniqueId=" + uniqueId +
                ", lastUpdated=" + lastUpdated +
                ", created=" + created +
                '}';
    }
}
