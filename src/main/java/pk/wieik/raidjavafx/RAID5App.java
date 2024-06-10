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

import java.util.ArrayList;
import java.util.List;

public class RAID5App extends Application {

    private TextField inputDataField;
    private TextField numOfDiscsField;
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
        GridPane.setConstraints(inputDataField, 1, 0);

        Label numOfDiscsLabel = new Label("Number of Discs:");
        GridPane.setConstraints(numOfDiscsLabel, 0, 1);
        numOfDiscsField = new TextField();
        GridPane.setConstraints(numOfDiscsField, 1, 1);

        Button saveDataButton = new Button("Save Data to Discs");
        saveDataButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        saveDataButton.setOnAction(e -> saveDataToDiscs());
        GridPane.setConstraints(saveDataButton, 1, 2);

        Button readDataButton = new Button("Read Data from Discs");
        readDataButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
        readDataButton.setOnAction(e -> readDataFromDiscs());
        GridPane.setConstraints(readDataButton, 1, 3);

        discContainer = new VBox(15);
        GridPane.setConstraints(discContainer, 0, 4, 2, 1);

        grid.getChildren().addAll(inputDataLabel, inputDataField, numOfDiscsLabel, numOfDiscsField, saveDataButton, readDataButton, discContainer);

        Scene scene = new Scene(grid, 1000, 800);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void saveDataToDiscs() {
        String inputData = inputDataField.getText();
        String numOfDiscs = numOfDiscsField.getText();

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

        updateDiscDisplay("Destroy Data");
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
        } else {
            List<Bit> restoredData = Cluster.recoverData(listOfDiscs, discIndex, inputDataField.getText().length());
            listOfDiscs.set(discIndex, restoredData);
            button.setText("Destroy Data");
        }
        updateDiscDisplay(button.getText());
    }

    private void updateDiscDisplay(String btnText) {
        discContainer.getChildren().clear();
        for (int i = 0; i < listOfDiscs.size(); i++) {
            HBox discBox = createDiscBox(listOfDiscs.get(i));
            Button destroyRestoreButton = new Button(btnText);
            destroyRestoreButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
            int discIndex = i;
            destroyRestoreButton.setOnAction(e -> toggleDiscData(discIndex, destroyRestoreButton));
            VBox discWithButton = new VBox(5, discBox, destroyRestoreButton);
            discWithButton.setAlignment(Pos.CENTER);
            discContainer.getChildren().add(discWithButton);
        }
    }

    private void readDataFromDiscs() {
        List<Bit> recoveredData = Cluster.readData(listOfDiscs, inputDataField.getText().length());
        StringBuilder dataString = new StringBuilder();
        for (Bit bit : recoveredData) {
            dataString.append(bit.getBit() ? "1" : "0");
        }
        new AlertBox().display("Recovered Data", dataString.toString());
    }
    private boolean isValidBinary(String input) {
        return input.matches("[01]+");
    }

    private boolean isValidInteger(String input) {
        return input.matches("[2-6]");
    }

    private void showErrorMessage(String message) {
        new AlertBox().display("Error", message);
    }

    private void clearTextField(TextField textField) {
        textField.setText("");
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
