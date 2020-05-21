package com.saska.stabsputnikapp.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class GraphController  {

    HeadController headController = new HeadController();

    final int WINDOW_SIZE = 15;
    @FXML
    private LineChart<String, Double> lineChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    public List<Double> dataPlot() throws IOException {
        return headController.reader();
    }

    public void plotGraph() throws IOException {
        xAxis.setLabel("time");
        xAxis.setAnimated(false);
        yAxis.setLabel("frequency");
        yAxis.setAnimated(false);
        lineChart.setAnimated(false);

        XYChart.Series<String, Double> inputLine = new XYChart.Series<>();
        XYChart.Series<String, Double> outputLine = new XYChart.Series<>();

        inputLine.setName("input");
        outputLine.setName("output");

        lineChart.getData().add(inputLine);
        lineChart.getData().add(outputLine);
        List<Double> list = dataPlot();
        Iterator<Double> iterationData = list.iterator();

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            Integer randomInput = ThreadLocalRandom.current().nextInt(10);
            Integer randomOutput = ThreadLocalRandom.current().nextInt(20);
            Platform.runLater(() -> {
                Date now = new Date();
                if (iterationData.hasNext()) {
                    inputLine.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), iterationData.next()));
                    if (inputLine.getData().size() > WINDOW_SIZE)
                        inputLine.getData().remove(0);
                } else {
                    inputLine.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), 0.0));
                    if (inputLine.getData().size() > WINDOW_SIZE)
                        inputLine.getData().remove(0);
                }
//              outputLine.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), randomOutput));

//              if (outputLine.getData().size() > WINDOW_SIZE)
//                    outputLine.getData().remove(0);
            });
        }, 0, 1, TimeUnit.SECONDS);
    }

    @FXML
    void initialize() throws IOException {
        plotGraph();
    }

}
