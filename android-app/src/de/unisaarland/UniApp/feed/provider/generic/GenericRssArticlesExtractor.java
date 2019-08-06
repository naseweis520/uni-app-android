package de.unisaarland.UniApp.feed.provider.generic;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.unisaarland.UniApp.utils.ContentExtractor;

public class GenericRssArticlesExtractor implements ContentExtractor {
    private final String baseUrl;

    public GenericRssArticlesExtractor(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public List<RssArticleData> extract(InputStream data) throws IOException {
        Document doc = Jsoup.parse(data, null, baseUrl);
        List<RssArticleData> outList = new ArrayList<>();

        Elements elements = doc.getElementsByTag("item");
        for (Element ele : elements) {
            String content = ele.getElementsByTag("content:encoded") != null && ele.getElementsByTag("content:encoded").hasText() ?
                    html2text(ele.getElementsByTag("content:encoded").text()) : null;
            String description = ele.getElementsByTag("description") != null && ele.getElementsByTag("description").hasText() ?
                    html2text(ele.getElementsByTag("description").text()) : null;
            String heading = ele.getElementsByTag("title") != null && ele.getElementsByTag("title").hasText() ?
                    ele.getElementsByTag("title").text() : null;
            Long pubDate = null;
            if(ele.getElementsByTag("pubDate") != null && ele.getElementsByTag("pubDate").hasText()) {
                try {
                    String pubDateString = ele.getElementsByTag("pubDate") != null && ele.getElementsByTag("pubDate").hasText() ?
                            ele.getElementsByTag("pubDate").text() : null;
                    pubDate = Date.parse(pubDateString);
                } catch (Exception ignored) {}
            }
            String url = ele.getElementsByTag("link") != null && ele.getElementsByTag("link").hasText() ?
                    ele.getElementsByTag("link").text() : null;

            outList.add(new RssArticleData(content == null ? description : content, heading,
                    pubDate == null ? null : new Date(pubDate), url));
        }

        return outList;
    }

    public static String html2text(String html) {
        return Jsoup.parse(html).text();
    }
}
