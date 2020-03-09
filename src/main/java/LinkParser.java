import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.RecursiveAction;

public class LinkParser extends RecursiveAction {
    private static final Logger logger = LogManager.getLogger(LinkParser.class);
    private static final Marker marker = MarkerManager.getMarker("LINK");

    private Queue<String> queueLinks;
    private Set<String> visitedLinks;
    private String status = null;

    public LinkParser(Queue<String> queueLinks, Set<String> visitedLinks) {
        this.queueLinks = queueLinks;
        this.visitedLinks = visitedLinks;
    }

    public String getStatus() {
        return status;
    }

    @Override
    protected void compute() {
        while (true) {
            String link = queueLinks.poll();

            //спим, если ссылка null
            if (link == null) {
                status = "sleeping";
                break;
            }

            //если ссылка уже пропарсена, то берем ссылку из очереди снова
            if (visitedLinks.contains(link)) {
                continue;
            }

            status = "working";
            visitedLinks.add(link);
            logger.info(marker, link);

            try {
                Document doc = Jsoup.connect(link).get();
                Elements urls = doc.getElementsByTag("a");

                for (Element innerLink : urls) {
                    String linkString;

                    if (innerLink.attr("href").contains(Main.getDomain())) {
                        linkString = innerLink.attr("abs:href");

                    }else {
                        continue;
                    }

                    //проверяем, что ссылка содержит имя домена и ее нет в посещенных ссылках
                    if (linkString.contains(Main.getDomain()) & !visitedLinks.contains(linkString)) {
                        queueLinks.add(linkString);
                        LinkParser parser = new LinkParser(queueLinks, visitedLinks);
                        parser.fork();
                    }
                }

            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
