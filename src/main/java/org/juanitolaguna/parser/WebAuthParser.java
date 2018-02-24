package org.juanitolaguna.parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class WebAuthParser implements Parser {
    private Document document;
    private String globalLink;
    private Pattern pattern;
    public static final String domain = "http://users.informatik.haw-hamburg.de/";
    public final boolean authentication = true;


    public WebAuthParser(String link, final String username, final String password, Pattern pattern) throws IOException {
        Authenticator.setDefault(new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password.toCharArray());
            }
        });
        document = Jsoup.connect(link).get();
        globalLink = link;
        this.pattern = pattern;
    }

    public HashMap<String, String> getLinks() {
        Elements links = document.getElementsByTag("a");
        HashMap<String, String> hashMap = new HashMap<>();
        links.stream().filter(e -> pattern.matcher(e.toString()).find()).forEach(e -> {
            hashMap.put(e.text(), e.attr("abs:href"));

        });
        return hashMap;
    }

    // returns Hashmap<Link, Hashmap<Filename, FileLink>>
    public HashMap<String, HashMap<String, String>> getUrlList(String link) throws IOException {
        //link (global) - link custom

        HashMap<String, HashMap<String, String>> globalMap = new HashMap<>();
        Document document = Jsoup.connect(link).get();
        Elements links = document.getElementsByTag("a");

        HashMap<String, String> mapOfLinks = new HashMap<>();
        links.stream().parallel().filter(e -> pattern.matcher(e.toString()).find()).forEach(e -> {
            mapOfLinks.put(e.text(), e.attr("abs:href"));
            //get relative path
            globalMap.put(e.baseUri().substring(globalLink.length() - 1), mapOfLinks);
        });
        links.stream()
                .filter(e -> e.text().endsWith("/") && e.baseUri().contains(link))
                .forEach(e -> {
                    try {
                        globalMap.putAll(getUrlList(e.attr("abs:href")));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                });
        return globalMap;
    }


    public void echoPage() {
        System.out.println(document.body());
    }


//    public static void main(String[] args) {
//        String url = "http://users.informatik.haw-hamburg.de/~wendholt/pub/pm2-java/";
//        String username = "abz823";
//        String password = "1Syncmaster";
//        try {
//            WebAuthParser parser = new WebAuthParser(url, username, password);
//            Document document = Jsoup.connect(url).get();
//            Elements links = document.getElementsByTag("a");
//            links.stream().forEach(e -> System.out.println(e.attr("href")));
////            parser.echoPage();
//            System.out.println(parser.getLinks());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
}
