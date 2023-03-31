package com.example.demo;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class BenforduvZakonController extends Application {
    private TextArea outputTextArea;
    private PieChart pieChart;
    private ComboBox<String> userComboBox;
    private Map<String, List<Double>> userTransactions = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("BenfordMoment");

        Button chooseFileButton = new Button("Vyber fajlu");
        chooseFileButton.setOnAction(e -> chooseFile(primaryStage));

        outputTextArea = new TextArea();
        outputTextArea.setEditable(false);

        pieChart = new PieChart();
        pieChart.setTitle("Honza Koláček zastoupení procent");

        userComboBox = new ComboBox<>();
        userComboBox.setPromptText("Vyber uživatele");
        userComboBox.setOnAction(e -> updateChart());

        GridPane grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(25, 25, 25, 25));
        grid.add(new Label("Vyber CSV file:"), 0, 0);
        grid.add(chooseFileButton, 1, 0);
        grid.add(new Label("Podezřelý uživatel:"), 0, 1);
        grid.add(userComboBox, 1, 1);
        grid.add(new Label("Transakšns"), 2, 0);
        grid.add(outputTextArea, 2, 1, 1, 2);
        grid.add(pieChart, 0, 2, 2, 1);

        Scene scene = new Scene(grid, 800, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void chooseFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Otevřít CSV fajl");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV fajla", "*.csv")
        );
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            processFile(file);
        }
    }

    private void processFile(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] transaction = line.split(";");
                String user = transaction[1] + transaction[2];
                double amount = Double.parseDouble(transaction[3]);
                userTransactions.computeIfAbsent(user, k -> new ArrayList<>()).add(amount);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Errorus", "Objevil se error: " + e.getMessage());
            return;
        }

        userComboBox.getItems().clear();
        userComboBox.getItems().addAll(userTransactions.keySet());

        if (!userTransactions.isEmpty()) {
            userComboBox.getSelectionModel().selectFirst();
            updateChart();
        }
    }

    private void updateChart() {
        String selectedUser = userComboBox.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            return;
        }

        List<Double> userAmounts = userTransactions.get(selectedUser);

        Map<Integer, Long> firstDigitCounts = new HashMap<>();
        for (Double amount : userAmounts) {
            int firstDigit = Integer.parseInt(Integer.toString((int) Math.abs(amount)).substring(0, 1));
            firstDigitCounts.put(firstDigit, firstDigitCounts.getOrDefault(firstDigit, 0L) + 1L);
        }

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (int i = 1; i <= 9; i++) {
            long count = firstDigitCounts.getOrDefault(i, 0L);
            double percentage = (double) count / userAmounts.size() * 100.0;
            pieChartData.add(new PieChart.Data(Integer.toString(i), percentage));
        }

        pieChart.setData(pieChartData);

        outputTextArea.setText(String.format("Celkový počet transakcí uživatele %s: %d%n%n", selectedUser, userAmounts.size()));

        outputTextArea.appendText("Zastoupení prvních číslic:%n");
        for (int i = 1; i <= 9; i++) {
            long count = firstDigitCounts.getOrDefault(i, 0L);
            double percentage = (double) count / userAmounts.size() * 100.0;
            outputTextArea.appendText(String.format("%d: %.2f%%%n", i, percentage));
        }
    }


    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
