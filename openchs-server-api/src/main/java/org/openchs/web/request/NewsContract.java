package org.openchs.web.request;

import org.joda.time.DateTime;
import org.openchs.domain.News;

import java.util.Date;

public class NewsContract extends CHSRequest {
    private String title;
    private Date publishedDate;
    private DateTime createdDateTime;
    private String heroImage;
    private String content;
    private String contentHtml;

    public static NewsContract fromEntity(News news) {
        NewsContract newsContract = new NewsContract();
        newsContract.setId(news.getId());
        newsContract.setUuid(news.getUuid());
        newsContract.setVoided(news.isVoided());
        newsContract.setTitle(news.getTitle());
        newsContract.setPublishedDate(news.getPublishedDate());
        newsContract.setHeroImage(news.getHeroImage());
        newsContract.setContent(news.getContent());
        newsContract.setContentHtml(news.getContentHtml());
        newsContract.setCreatedDateTime(news.getCreatedDateTime());
        return newsContract;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(Date publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getHeroImage() {
        return heroImage;
    }

    public void setHeroImage(String heroImage) {
        this.heroImage = heroImage;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentHtml() {
        return contentHtml;
    }

    public void setContentHtml(String contentHtml) {
        this.contentHtml = contentHtml;
    }

    public DateTime getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(DateTime createdDateTime) {
        this.createdDateTime = createdDateTime;
    }
}
