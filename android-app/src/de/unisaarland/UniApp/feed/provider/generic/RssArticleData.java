package de.unisaarland.UniApp.feed.provider.generic;

import java.io.Serializable;
import java.util.Date;

public class RssArticleData implements Serializable {
    public String contentText;
    public String heading;
    public Date pubDate;
    public String url;

    public RssArticleData(String contentText, String heading, Date pubDate, String url) {
        this.contentText = contentText;
        this.heading = heading;
        this.pubDate = pubDate;
        this.url = url;
    }
}
