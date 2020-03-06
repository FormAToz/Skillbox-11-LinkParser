import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;

public class Main {
    private static final String INPUT_EXAMPLE = "https://lenta.ru";
    private static final String REGEX = "(https://).+\\.\\D{2,3}";

    private static String domain;
    private static Set<String> visitedLinks = ConcurrentHashMap.newKeySet();
    private static Queue<String> queueLinks = new ConcurrentLinkedQueue<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String input;
        Boolean action = true;

        while (action) {
            System.out.printf("Введите адрес страницы, например - %s \n", INPUT_EXAMPLE);
            input = scanner.nextLine();

            if (!input.matches(REGEX)) {
                System.out.println("Неверный формат ввода. Попробуйте еще раз...");
                continue;
            }

            String[] tmpArray = input.split("/");
            domain = tmpArray[2];

            queueLinks.add(input);
            action = false;
        }

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        List<LinkParser> linkParsers = new ArrayList<>();

        for (int threads = 0; threads < Runtime.getRuntime().availableProcessors(); threads++) {
            LinkParser parser = new LinkParser(queueLinks, visitedLinks);
            linkParsers.add(parser);
        }

        linkParsers.forEach(forkJoinPool::invoke);

        while (true) {
            for (LinkParser parser : linkParsers) {
                if (parser.getStatus().equals("working")) {
                    try {
                        Thread.sleep(1000);
                        continue;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            break;
        }
    }

    public static String getDomain() {
        return domain;
    }
}
