package com.saska.stabsputnikapp.controller;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import com.jfoenix.validation.RequiredFieldValidator;
import com.saska.stabsputnikapp.hardware.EventListener;
import com.saska.stabsputnikapp.pid.SimplyPID;
import com.saska.stabsputnikapp.receivingdata.CommunicateFile;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import jssc.SerialPort;
import jssc.SerialPortException;
import jssc.SerialPortList;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import static com.saska.stabsputnikapp.hardware.EventListener.parseData;
import static com.saska.stabsputnikapp.hardware.EventListener.serialPort;

public class HeadController {

    public static final String FILESET = "src/main/resources/txt/ReceiveSetpoint.txt";
    public static final String LOGFILESET = "src/main/resources/txt/LogSetpoint.txt";
    private static final String FILE_OUTPUT = "src/main/resources/txt/Output.txt";
    final int WINDOW_SIZE = 77;
    EventListener eventListener = new EventListener();
    CommunicateFile communicate = new CommunicateFile();
    RequiredFieldValidator requiredInputValidator = new RequiredFieldValidator();

    @FXML
    private LineChart<String, Double> lineChart;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private Button setKoeffP;

    @FXML
    private Button setKoeffI;

    @FXML
    private Button setKoeffD;

    @FXML
    private JFXTextField kP;

    @FXML
    private JFXTextField kI;

    @FXML
    private JFXTextField kD;

    @FXML
    private JFXTextArea resultWrite;

    @FXML
    private Button setPoint;

    @FXML
    private JFXTextField inputSetPoint;

    @FXML
    private Button buildChart;

    @FXML
    private Button copy;

    @FXML
    private JFXToggleButton openOrClose;

    @FXML
    private JFXButton clearChart;

    public void initializeComPort() {
        serialPort = new SerialPort(getPort());
        try {
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
            serialPort.addEventListener(new EventListener());
        } catch (SerialPortException ex) {
            ex.printStackTrace();
        }
    }

    public String getPort() {
        String[] portNames = SerialPortList.getPortNames();
        return portNames[0];
    }

    @SuppressWarnings("unchecked")
    private void requestPort() {
        JComboBox<String> portNameSelector = new JComboBox<>();
        portNameSelector.setModel(new DefaultComboBoxModel<>());
        String[] portNames;
        portNames = SerialPortList.getPortNames();
        for (String portName : portNames) {
            portNameSelector.addItem(portName);
        }
        if (portNameSelector.getItemCount() == 0) {
            JOptionPane.showMessageDialog(null, "Cannot find any serial port", "Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.add(new JLabel("Port "));
        panel.add(portNameSelector);
        if (JOptionPane.showConfirmDialog(null, panel, "Select the port", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            portNameSelector.getSelectedItem().toString();
        } else {
            System.exit(0);
        }
    }

    public void warningMessage() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText("Empty field");
        alert.setContentText("Enter the number in input field");
        alert.showAndWait();
    }

    public void errorMessage() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("No receive data");
        alert.showAndWait();
    }

    private void validationSetpoint() {
        Pattern p = Pattern.compile("(\\d+\\.?\\d*)?");
        inputSetPoint.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!p.matcher(newValue).matches()) inputSetPoint.setText(oldValue);
        });
        requiredInputValidator.setMessage("Cannot be empty");
        inputSetPoint.getValidators().add(requiredInputValidator);
        inputSetPoint.focusedProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal) {
                inputSetPoint.validate();
            }
        });
    }

    private void validationkP() {
        Pattern p = Pattern.compile("(\\d+\\.?\\d*)?");
        kP.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!p.matcher(newValue).matches()) kP.setText(oldValue);
        });
    }

    private void validationkI() {
        Pattern p = Pattern.compile("(\\d+\\.?\\d*)?");
        kI.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!p.matcher(newValue).matches()) kI.setText(oldValue);
        });
    }

    private void validationkD() {
        Pattern p = Pattern.compile("(\\d+\\.?\\d*)?");
        kD.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!p.matcher(newValue).matches()) kD.setText(oldValue);
        });
    }

    public void countPID() throws IOException {
        double output = 0;
        double currentValue = 25;
        SimplyPID pid = new SimplyPID(0, 1.2, 0, 0.025);
        System.out.print("Time\tSet Point\tCurrent value\tOutput\tError\n");

        for (int i = 0; i < 30; i++) {
            System.out.printf("%d\t%3.2f\t%3.2f\t%3.2f\t%3.2f\n", i, pid.getSetPoint(), currentValue, output, (pid.getSetPoint() - currentValue));
            communicate.fileWriter(String.valueOf(output), FILE_OUTPUT);
            if (i == 15)
                pid.setSetpoint(50);

            output = pid.getOutput(1, currentValue);

            currentValue += output * 1.3 + (Math.random() - .5) * 3;
        }
    }

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
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> {
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
            });
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    public void openConnectionReadSerialPort() {
        initializeComPort();
        openOrClose.setText("Open port");
        openOrClose.setTooltip(new Tooltip("Click and the serial\n" + "port will be closed"));
    }

    public void closeConnectionReadSerialPort() {
        eventListener.stop();
        openOrClose.setText("Close port");
        openOrClose.setTooltip(new Tooltip("Click and the serial\n" + "port will be opened"));
    }

    @FXML
    private void onCopy() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(eventListener.dataComPort.getText().trim());
        clipboard.setContent(content);
    }

    public void resultInputWrite(Boolean resultInput) {
        Platform.runLater(() ->
                resultWrite.appendText(String.valueOf(resultInput)));
    }

    public void validateAllField() {
        validationkP();
        validationkI();
        validationkD();
        validationSetpoint();
    }

    @FXML
    void initialize() throws IOException {
        validateAllField();
        //requestPort();
        countPID();

        setPoint.setOnAction(action -> {
            if (inputSetPoint.getText().isEmpty()) {
                warningMessage();
            } else {
                try {
                    resultWrite.setText("setpoint: ");
                    resultInputWrite(serialPort.writeString("set_point" + inputSetPoint.getText()));
                    communicate.logFileWriter(inputSetPoint.getText(), LOGFILESET);
                    communicate.fileWriter(inputSetPoint.getText(), FILESET);
                } catch (SerialPortException | IOException e) {
                    e.printStackTrace();
                }
            }
        });

        setKoeffP.setOnAction(action -> {
            if (kP.getText().isEmpty()) {
                warningMessage();
            } else {
                try {
                    resultWrite.setText("new koeffP: ");
                    resultInputWrite(serialPort.writeString("new_koefP" + kP.getText()));
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
            }
        });

        buildChart.setOnAction(build ->  {
            if (parseData == null) {
                errorMessage();
            } else {
                buildChart();
            }
        });

        setKoeffI.setOnAction(action -> {
            if (kI.getText().isEmpty()) {
                warningMessage();
            } else {
                try {
                    resultWrite.setText("new koeffI: ");
                    resultInputWrite(serialPort.writeString("new_koefI" + kI.getText()));
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
            }
        });

        setKoeffD.setOnAction(action -> {
            if (kD.getText().isEmpty()) {
                warningMessage();
            } else {
                try {
                    resultWrite.setText("new koeffD: ");
                    resultInputWrite(serialPort.writeString("new_koefD" + kD.getText()));
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
            }
        });

        openOrClose.setOnAction(event -> {
            if (openOrClose.isSelected()) {
                openConnectionReadSerialPort();
            } else {
                closeConnectionReadSerialPort();
            }
        });

        clearChart.setOnAction(clear -> lineChart.getData().clear());
        copy.setOnAction(copy -> onCopy());

    }
}


