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
import java.util.concurrent.TimeUnit;

public class GraphController  {

    HeadController headController = new HeadController();

    final int WINDOW_SIZE = 55;
    @FXML
    private LineChart<String, Double> lineChart;
    @FXML
    private CategoryAxis xAxis;
    @FXML
    private NumberAxis yAxis;

    public List<Double> dataPlotOutput() throws IOException {
        return headController.readerOutput();
    }

    public void plotGraph() throws IOException {
        xAxis.setLabel("time");
        xAxis.setAnimated(false);
        yAxis.setLabel("frequency");
        yAxis.setAnimated(false);
        lineChart.setAnimated(false);

       // XYChart.Series<String, Double> inputLine = new XYChart.Series<>();
        XYChart.Series<String, Double> outputLine = new XYChart.Series<>();
      //  XYChart.Series<String, Double> setLine = new XYChart.Series<>();

        //inputLine.setName("input");
        //inputLine.getClass().getResource("stylesheet.css");
        outputLine.setName("output");
        outputLine.getClass().getResource("stylesheet.css");
        //setLine.setName("setpoint");

       // lineChart.getData().add(inputLine);
        lineChart.getData().add(outputLine);
       // lineChart.getData().add(setLine);
        List<Double> list = dataPlotOutput();
        Iterator<Double> iterationData = list.iterator();

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
                Date now = new Date();
                if (iterationData.hasNext()) {
                    outputLine.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), iterationData.next()));
                    if (outputLine.getData().size() > WINDOW_SIZE)
                        outputLine.getData().remove(0);
                } else {
                    outputLine.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), 0.0));
                }
//                    setLine.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), 0.0));
//                    if (setLine.getData().size() > WINDOW_SIZE)
//                        setLine.getData().remove(0);
            });
        }, 0, 1, TimeUnit.SECONDS);
    }

    @FXML
    void initialize() throws IOException {
        plotGraph();
    }
}
