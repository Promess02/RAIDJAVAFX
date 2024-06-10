package pk.wieik.raidjavafx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class RAID5App extends Application {

    private TextField inputDataField;
    private TextField numOfDiscsField;
    private TextField inputFilePathField;
    private TextField outputFilePathField;
    private VBox discContainer;
    private List<List<Bit>> listOfDiscs;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("RAID 5 Data Storage");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(20);
        grid.setHgap(10);
        grid.setStyle("-fx-background-color: #95c3be;");
        grid.setAlignment(Pos.CENTER);

        Label inputDataLabel = new Label("Input Data:");
        GridPane.setConstraints(inputDataLabel, 0, 0);
        inputDataField = new TextField();
        inputDataField.setMinWidth(300);
        GridPane.setConstraints(inputDataField, 1, 0);

        Label numOfDiscsLabel = new Label("Number of Discs:");
        GridPane.setConstraints(numOfDiscsLabel, 0, 1);
        numOfDiscsField = new TextField();
        numOfDiscsLabel.setMaxWidth(200);
        GridPane.setConstraints(numOfDiscsField, 1, 1);

        Label inputFilePathLabel = new Label("Input File Path:");
        GridPane.setConstraints(inputFilePathLabel, 0, 2);
        inputFilePathField = new TextField();
        inputFilePathField.setMinWidth(300);
        inputFilePathField.setPromptText("Enter file path from resource folder");
        GridPane.setConstraints(inputFilePathField, 1, 2);

        Label outputFilePathLabel = new Label("Output File Path:");
        GridPane.setConstraints(outputFilePathLabel, 0, 3);
        outputFilePathField = new TextField();
        outputFilePathField.setMinWidth(300);
        outputFilePathField.setPromptText("Enter file path from resource folder");
        GridPane.setConstraints(outputFilePathField, 1, 3);

        Button saveDataButton = new Button("Save Data to Discs");
        saveDataButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        saveDataButton.setOnAction(e -> saveDataToDiscs());
        GridPane.setConstraints(saveDataButton, 1, 4);

        Button readDataButton = new Button("Read Data from Discs");
        readDataButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        readDataButton.setOnAction(e -> readDataFromDiscs());
        GridPane.setConstraints(readDataButton, 1, 5);

        discContainer = new VBox(15);
        GridPane.setConstraints(discContainer, 0, 6, 2, 1);

        grid.getChildren().addAll(inputDataLabel, inputDataField, numOfDiscsLabel,
                numOfDiscsField, inputFilePathLabel, inputFilePathField, outputFilePathLabel, outputFilePathField, saveDataButton, readDataButton, discContainer);

        Scene scene = new Scene(grid, 1000, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void saveDataToDiscs() {
        String inputData = inputDataField.getText();
        String inputFilePath = getResourceAbsolutePath(inputFilePathField.getText());
        String numOfDiscs = numOfDiscsField.getText();

        if(inputFilePath!=null){
            if(!inputFilePathField.getText().isEmpty()){
                // if bad file format
                if (isFilePath(inputFilePath)) {
                    try {
                        inputData = new String(Files.readAllBytes(Paths.get(inputFilePath)));
                        inputDataField.setText(inputData);
                    } catch (IOException e) {
                        showErrorMessage("Error reading input file.");
                        return;
                    }
                }else{
                    showErrorMessage("Invalid input file path. Please enter a valid path that ends in '.txt'.");
                    clearTextField(inputFilePathField);
                }
            }
        }
        // if empty then don't check the file

        if (!isValidBinary(inputData)) {
            showErrorMessage("Invalid input data. Please enter binary values (0 or 1) only.");
            clearTextField(inputDataField);
            return;
        }

        if (!isValidInteger(numOfDiscs)) {
            showErrorMessage("Invalid number of discs. Please enter an integer number in range 2-6.");
            clearTextField(numOfDiscsField);
            return;
        }
        int numberOfDiscs = Integer.parseInt(numOfDiscs);

        Disc disc = new Disc(numberOfDiscs, inputData);
        List<Bit> bitList = Cluster.toList(disc);
        listOfDiscs = Cluster.createDiscList(disc);
        Cluster.saveData(bitList, listOfDiscs);

        updateDiscDisplay("Destroy Data",-1);
    }

    private HBox createDiscBox(List<Bit> discData) {
        HBox discBox = new HBox(8);
        for (Bit bit : discData) {
            Rectangle bitRect = new Rectangle(25, 25);
            bitRect.setFill(bit.isParityBit() ? Color.GREEN : Color.WHITE);
            bitRect.setStroke(Color.BLACK);
            StackPane bitPane = new StackPane(bitRect, new Text(bit.getBit() ? "1" : "0"));
            discBox.getChildren().add(bitPane);
        }
        discBox.setAlignment(Pos.CENTER);
        return discBox;
    }

    private void toggleDiscData(int discIndex, Button button) {
        String btnText = button.getText();
        if (btnText.equals("Destroy Data")) {
            Cluster.simulateDamage(new Disc(discIndex), listOfDiscs);
            button.setText("Restore Data");
            btnText = button.getText();
        } else {
            List<Bit> restoredData = Cluster.recoverData(listOfDiscs, discIndex, inputDataField.getText().length());
            listOfDiscs.set(discIndex, restoredData);
            button.setText("Destroy Data");
            btnText = button.getText();
        }
        updateDiscDisplay(btnText, discIndex);
    }

    private void updateDiscDisplay(String btnText, int toggleIndex) {
        discContainer.getChildren().clear();
        for (int i = 0; i < listOfDiscs.size(); i++) {
            HBox discBox = createDiscBox(listOfDiscs.get(i));
            Button destroyRestoreButton = new Button("Destroy Data");
            if(i==toggleIndex)
                destroyRestoreButton.setText(btnText);
            destroyRestoreButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            int discIndex = i;
            destroyRestoreButton.setOnAction(e -> toggleDiscData(discIndex, destroyRestoreButton));
            VBox discWithButton = new VBox(5, discBox, destroyRestoreButton);
            discWithButton.setAlignment(Pos.CENTER);
            discContainer.getChildren().add(discWithButton);
        }
    }


    private void readDataFromDiscs() {
        if(listOfDiscs==null){
            showErrorMessage("No data to read from.");
            return;
        }
        List<Bit> recoveredData = Cluster.readData(listOfDiscs, inputDataField.getText().length());
        StringBuilder dataString = new StringBuilder();
        for (Bit bit : recoveredData) {
            dataString.append(bit.getBit() ? "1" : "0");
        }
        String outputFilePath = getResourceAbsolutePath(outputFilePathField.getText());
        String savedDataMessage = "";
        if(outputFilePath!=null){
            if (isFilePath(outputFilePath)) {
                try {
                    Path path = Paths.get(outputFilePath);
                    if(!Files.exists(path)){
                        Files.createFile(path);
                    }
                    Files.write(path, dataString.toString().getBytes());
                    savedDataMessage = "\nSaved data to " + outputFilePath;
                } catch (IOException e) {
                    showErrorMessage("Error writing to output file.");
                    return;
                }
            } else {
                showErrorMessage("Invalid output file path. Please enter a valid path that ends in '.txt'.");
                clearTextField(outputFilePathField);
            }
        }else{
            showErrorMessage("incorrect file path provided. Please enter a valid file path.");
            clearTextField(outputFilePathField);
        }
        new AlertBox().display("Recovered Data", dataString+ savedDataMessage);
    }
    private boolean isValidBinary(String input) {
        return input.matches("[01]+");
    }

    private boolean isValidInteger(String input) {
        return input.matches("[2-6]");
    }

    private boolean isFilePath(String input){
        return input.endsWith(".txt");
    }

    private void showErrorMessage(String message) {
        new AlertBox().display("Error", message);
    }

    private void clearTextField(TextField textField) {
        textField.setText("");
    }

    public static String getResourceAbsolutePath(String resourceName) {
        // Get the class loader for this class
        ClassLoader classLoader = RAID5App.class.getClassLoader();

        // Get the URL of the resource
        java.net.URL resourceUrl = classLoader.getResource(resourceName);

        // Convert URL to Path and then to absolute path
        Path resourcePath = null;
        try {
            if (resourceUrl != null) {
                resourcePath = Paths.get(resourceUrl.toURI());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return resourcePath != null ? resourcePath.toAbsolutePath().toString() : null;
    }
}

class AlertBox {

    public void display(String title, String message) {
        Stage window = new Stage();
        window.setTitle(title);
        window.setMinWidth(250);
        window.setMinHeight(150);

        Label label = new Label();
        label.setText(message);
        label.setStyle("-fx-font-size: 16px;");

        VBox layout = new VBox(10);
        layout.getChildren().addAll(label);
        layout.setPadding(new Insets(10, 10, 10, 10));
        layout.setStyle("-fx-alignment: center");

        Scene scene = new Scene(layout);
        window.setScene(scene);
        window.showAndWait();
    }



}
