package org.juanitolaguna.controller;

import com.sun.javafx.tk.TKClipboard;
import com.sun.javafx.tk.Toolkit;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.DataFormat;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import org.juanitolaguna.downloader.FileLoader;
import org.juanitolaguna.factories.ParserFactory;
import org.juanitolaguna.parser.Parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;


public class Controller {
    @FXML
    private Button downloadButton;
    @FXML
    private TextField urlField, destField, usrField, passField, regExField;
    private List<TextField> textFields;
    @FXML
    private ProgressIndicator progressIndicator;
    @FXML
    private Label downloadingLabel, supportLabel;
    private List<String> domainList = ParserFactory.generateDomainList();
    private Pattern pattern = Pattern.compile("(.*\\.pdf\"|.*\\.zip\"|.*\\.docx\"|.*\\.doc\")");

    public void initialize() {
        downloadButton.setDisable(true);
    }


    @FXML
    public void handleKeyReleased() {
        generateList();
        boolean disableButtons = true;
        if (textFields.stream().noneMatch(e -> e.getText().equals(""))) {
            disableButtons = textFields.stream().map(TextInputControl::getText)
                    .anyMatch(field -> field.isEmpty() || field.trim().isEmpty());
        }
        downloadButton.setDisable(disableButtons);
    }

    private void generateList() {
        textFields = new ArrayList<>();
        textFields.add(urlField);
        textFields.add(destField);
        textFields.add(usrField);
        textFields.add(passField);
    }


    @FXML
    public void downloadClicked(){
        try {
            if (supportLabel.isVisible()) {
                showPageNotSupported();
            } else {
                if (!regExField.getText().equals("")) {
                    pattern = Pattern.compile(parseFieldToRegEx());
                }
                String url = urlField.getText();
                String dest = destField.getText();
                String username = usrField.getText();
                String password = passField.getText();

                downloadingLabel.setText("Downloading, please wait...");
                downloadingLabel.setStyle("-fx-text-fill: orange");
                Task task = new Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        progressIndicator.setVisible(true);
                        downloadingLabel.setVisible(true);
                        FileLoader fileLoader = new FileLoader(dest);
                        Parser parser = ParserFactory.makeParser(url, username, password, pattern);
                        fileLoader.writeAllFiles(parser.getUrlList(url));
                        progressIndicator.setVisible(false);
                        return null;
                    }

                    @Override
                    protected void succeeded() {
                        super.succeeded();
                        downloadingLabel.setText("Download complete!");
                        downloadingLabel.setStyle("-fx-text-fill: green");
                    }

                };
                progressIndicator.progressProperty().bind(task.progressProperty());
                new Thread(task).start();
            }
        } catch (FilenameFormatException e){
            Alert alert = new Alert(Alert.AlertType.INFORMATION, e.getMessage(), ButtonType.OK);
            alert.showAndWait();
        }


    }

    private void showPageNotSupported() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "This page is not supported.", ButtonType.OK);
        alert.showAndWait();
    }


    public void chooseFile() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose destination Folder");
        File defaultDirectory = new File(System.getProperty("user.home"));
        directoryChooser.setInitialDirectory(defaultDirectory);
        File selectedFile = directoryChooser.showDialog(new Stage());
        if (selectedFile != null) {
            destField.setText(selectedFile.getAbsolutePath());
        }

    }


    public void pasteFromClipboard() {
        TKClipboard clipboard = Toolkit.getToolkit().getSystemClipboard();
        urlField.setText(clipboard.getContent(DataFormat.PLAIN_TEXT).toString());
    }

    @FXML
    public void handleParser() {
        if (!urlField.getText().equals("")) {
            boolean urlSupported = domainList.stream()
                    .anyMatch(e -> urlField.getText().contains(e));
            supportLabel.setVisible(!urlSupported);
        }
    }

    private String parseFieldToRegEx() throws FilenameFormatException {
        String filter = regExField.getText().replace(" ", "");
        String[] fileEndings = filter.split(",");
        checkFilterFormat(fileEndings);

        StringBuilder sb = new StringBuilder("");
        Stream.of(fileEndings).forEach(e -> sb.append(".*\\" + e + "\"|"));
        filter = sb.substring(0, sb.length() - 1);
        filter = "(" + filter + ")";
        return filter;
    }

    private void checkFilterFormat(String[] ary) throws FilenameFormatException {
        boolean correctFormatting = Stream.of(ary).allMatch(e -> e.matches("\\..*"));
        if (!correctFormatting) {
            throw new FilenameFormatException("Wrong Filename Formatting");
        }
    }
}
