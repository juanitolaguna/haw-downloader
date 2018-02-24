package org.juanitolaguna.factories;

import org.juanitolaguna.parser.Parser;
import org.juanitolaguna.parser.WebAuthParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class ParserFactory {
    private static Map<String, Parser> parserMap = new HashMap<>();
    private static List<String> domainList = new ArrayList<>();

    private ParserFactory() {
    }


    public static Parser makeParser(String link, String username, String password, Pattern pattern) throws IOException {
        generateParserMap(link, username, password, pattern);
        Parser parser = parserMap.entrySet().stream()
                .filter(entry -> link.contains(entry.getKey()))
                .map(entry -> entry.getValue())
                .findFirst()
                .orElse(null);
        return parser;


    }

    public static void makeParser(String link, Pattern pattern) throws IOException {
        //Is it possible to enter empty string if no authentification required??
        makeParser(link, "", "", pattern);
    }

    private static void generateParserMap(String link, String username, String password, Pattern pattern) throws IOException {
        parserMap.put(WebAuthParser.domain, new WebAuthParser(link, username, password, pattern));
    }

    public static List<String> generateDomainList() {
        domainList.add(WebAuthParser.domain);
        return domainList;
    }
}
