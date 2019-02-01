package me.lkp111138.mysupercutebot.api.handlers;

import com.sun.net.httpserver.HttpExchange;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.HttpsURLConnection;
import java.net.URL;

public class ForumThreadListHandler extends AbstractHandler {
    @Override
    public HttpResponse realhandle(HttpExchange exchange) throws Exception {
        String[] segments = exchange.getRequestURI().getPath().split("/");
        String fid = segments[3];
        String page = "";
        if (segments.length > 4) {
            page = "/page-" + segments[4];
        }
        String url = "https://forums.wynncraft.com/forums/" + fid + page;
//        Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.75 Safari/537.36").get();
        HttpsURLConnection conn = (HttpsURLConnection) new URL(url).openConnection();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.75 Safari/537.36");
        Document doc = Jsoup.parse(conn.getInputStream(), "utf-8", url);
        // get thread list
        Elements elements = doc.select("#content > div > div > div.discussionList.section.sectionMain > form.DiscussionList.InlineModForm > ol > li");
        JSONArray threads = new JSONArray();
        String subforum = doc.selectFirst("#content > div > div > div.titleBar > h1").text();
        for (Element elem : elements) {
            String title = elem.selectFirst("div.main > div.titleText > h3.title > a.PreviewTooltip").text();
            String tid = elem.id().substring(7);
            Element stats = elem.selectFirst("div.stats");
            String replies = stats.selectFirst("dl.major > dd").text();
            String views = stats.selectFirst("dl.minor > dd").text();
            Element block3 = elem.selectFirst("div.lastPost > dl");
            Element last_post = block3.selectFirst("dt > a");
            String by_username = last_post.text();
            String[] _by_uid = last_post.attr("href").split("\\.");
            String by_uid = _by_uid[_by_uid.length - 1].split("/")[0];
            Element abbr = block3.selectFirst("dd > a > span");
//            System.out.println(block3);
            String last_post_time = "";
            try {
                last_post_time = abbr.attr("data-time");
            } catch (NullPointerException e) {
                //fuck you
            }
            if (last_post_time.equals("")) {
                abbr = block3.selectFirst("dd > a > abbr");
//            System.out.println(block3);
                try {
                    last_post_time = abbr.attr("data-time");
                } catch (NullPointerException e) {
                    //fuck you
                }
            }
            threads.put(new JSONObject().put("title", title).put("id", tid).put("replies", replies).put("views", views).put("last_post", new JSONObject().put("username", by_username).put("uid", by_uid).put("at", last_post_time)));
        }
        return new HttpResponse().setResponse(new JSONObject().put("success", true).put("threads", threads).put("name", subforum).toString()).setRcode(200);
    }
}
