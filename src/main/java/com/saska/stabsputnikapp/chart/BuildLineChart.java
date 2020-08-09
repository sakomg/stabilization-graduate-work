package com.saska.stabsputnikapp.chart;

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
import java.util.concurrent.TimeUnit;

import static com.saska.stabsputnikapp.hardware.EventListener.parseData;

public class BuildLineChart {

    final int WINDOW_SIZE = 77;
    @FXML
    public LineChart<String, Double> lineChart;
    @FXML
    public CategoryAxis xAxis;
    @FXML
    public NumberAxis yAxis;

    public void buildChart() {
        xAxis.setLabel("time");
        xAxis.setAnimated(false);
        yAxis.setLabel("frequency");
        yAxis.setAnimated(false);
        lineChart.setAnimated(false);

        XYChart.Series<String, Double> inputLine = new XYChart.Series<>();
        XYChart.Series<String, Double> outputLine = new XYChart.Series<>();
        XYChart.Series<String, Double> setLine = new XYChart.Series<>();

        inputLine.setName("input");
        inputLine.getClass().getResource("stylesheet.css");
        outputLine.setName("output");
        outputLine.getClass().getResource("stylesheet.css");
        setLine.setName("setpoint");

        lineChart.getData().add(inputLine);
        lineChart.getData().add(outputLine);
        lineChart.getData().add(setLine);
        lineChart.setCreateSymbols(false);

        final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ss:SSS");
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(() -> Platform.runLater(() -> {
            Date now = new Date();

            inputLine.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), Double.parseDouble(parseData[0].trim())));
            if (inputLine.getData().size() > WINDOW_SIZE)
                inputLine.getData().remove(0);

            setLine.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), Double.parseDouble(parseData[1].trim())));
            if (setLine.getData().size() > WINDOW_SIZE)
                setLine.getData().remove(0);

            outputLine.getData().add(new XYChart.Data<>(simpleDateFormat.format(now), Double.parseDouble(parseData[2].trim())));
            if (outputLine.getData().size() > WINDOW_SIZE)
                outputLine.getData().remove(0);
        }), 0, 100, TimeUnit.MILLISECONDS);
    }

    public void clearChart() {
        if (lineChart != null)
            lineChart.getData().clear();
    }
}
