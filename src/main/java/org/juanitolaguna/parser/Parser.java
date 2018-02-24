package org.juanitolaguna.parser;

import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;

public interface Parser {
     HashMap<String, HashMap<String, String>> getUrlList(String link) throws IOException;
     void echoPage();

}
