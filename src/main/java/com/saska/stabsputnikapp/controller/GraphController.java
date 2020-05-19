package com.saska.stabsputnikapp.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class GraphController {

    final int WINDOW_SIZE = 30;
    @FXML
    private LineChart<String, Number> lineChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    public void plotGraph() {
        xAxis.setLabel("time");
        xAxis.setAnimated(false);
        yAxis.setLabel("frequency");
        yAxis.setAnimated(false);
        lineChart.setAnimated(false);
        XYChart.Series<String, Number> inputLine = new XYChart.Series<>();
        XYChart.Series<String, Number> outputLine = new XYChart.Series<>();
        inputLine.setName("input");
        outputLine.setName("output");
        lineChart.getData().add(inputLine);
        lineChart.getData().add(outputLine);
        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            Integer randomInput = ThreadLocalRandom.current().nextInt(10);
            Integer randomOutput = ThreadLocalRandom.current().nextInt(20);
            Platform.runLater(() -> {
                Date now = new Date();
                inputLine.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), randomInput));
                outputLine.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), randomOutput));
                if (inputLine.getData().size() > WINDOW_SIZE)
                    inputLine.getData().remove(0);
                if (outputLine.getData().size() > WINDOW_SIZE)
                    outputLine.getData().remove(0);
            });
        }, 0, 1, TimeUnit.SECONDS);
    }

    @FXML
    void initialize() {
        plotGraph();
    }

}
