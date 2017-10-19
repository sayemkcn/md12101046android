package net.toracode.moviebuzz.entity;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by sayemkcn on 11/15/16.
 */

public class Comment implements Serializable{
    private Long uniqueId;
    private Date lastUpdated;
    private Date created;
    private String commentBody;
    private User user;

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

    public String getCommentBody() {
        return commentBody;
    }

    public void setCommentBody(String commentBody) {
        this.commentBody = commentBody;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "uniqueId=" + uniqueId +
                ", lastUpdated=" + lastUpdated +
                ", created=" + created +
                ", commentBody='" + commentBody + '\'' +
                ", user=" + user +
                '}';
    }
}
