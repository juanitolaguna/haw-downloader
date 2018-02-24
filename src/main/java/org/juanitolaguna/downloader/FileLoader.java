package org.juanitolaguna.downloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class FileLoader {
    private String dirAsString;

    public FileLoader(String usrDir) {
        this.dirAsString = usrDir;
    }


    public void writeAllFiles(HashMap<String, HashMap<String, String>> map) {
        map.entrySet().stream().parallel().forEach(entry -> {
            Path path = Paths.get(dirAsString + entry.getKey());
            if (!Files.exists(path)) {
                try {
                    Files.createDirectories(path);
                } catch (IOException e) {
                    System.out.println(e);
                }
            }
            writeFiles(entry.getValue(), entry.getKey());
        });
    }

    private void writeFiles(HashMap<String, String> links, String relativePath) {
        String sep = File.separator;
        links.entrySet().stream().parallel().forEach((Map.Entry<String, String> entry) -> {
            URL url;
            try {
                url = new URL(entry.getValue());
                InputStream in = url.openStream();

                Path path = Paths.get(dirAsString + sep + relativePath + sep + entry.getKey());
                Files.copy(in, path, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println(e);
            }
        });
    }
}
