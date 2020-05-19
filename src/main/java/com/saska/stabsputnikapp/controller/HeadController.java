package com.saska.stabsputnikapp.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jssc.*;

import javax.swing.*;
import java.awt.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class HeadController {

    public static final String FILE = "src/main/resources/txtfiles/SerialReceive.txt";
    public static SerialPort serialPort;

    @FXML
    private TextArea dataComPort;

    @FXML
    private Button setNewSpeed;

    @FXML
    private TextField inputNewSpeed;

    @FXML
    private TextArea resultWrite;

    @FXML
    private Button setPoint;

    @FXML
    private TextField inputSetPoint;

    @FXML
    private Button openPort;

    @FXML
    private Button closePort;

    @FXML
    private Button buildGraph;

    @FXML
    private Button copy;

    public HeadController() throws IOException {
    }

    public Integer fileReader() throws IOException {
        FileReader rFile = new FileReader(FILE);
        Scanner scan = new Scanner(rFile);
        StringBuilder fullFile = new StringBuilder();
        while (scan.hasNext()) {
            fullFile.append(scan.next());
        }
        rFile.close();
        return Integer.parseInt(String.valueOf(fullFile));
    }

    @FXML
    private void onCopy() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(dataComPort.getText().trim());
        clipboard.setContent(content);
    }

    public void initializeComPort() {
        serialPort = new SerialPort(getPort());
        try {
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
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
        portNameSelector.setModel(new DefaultComboBoxModel<String>());
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


    private void fileWriter(String buffer) throws IOException {
        FileWriter wFile = new FileWriter(FILE, true);
        wFile.write(buffer);
        wFile.close();
    }

    public void warningMessage() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText("Empty field");
        alert.setContentText("Enter the number in input field");
        alert.showAndWait();
    }

    private void validationSetpoint() {
        Pattern p = Pattern.compile("(\\d+\\.?\\d*)?");
        inputSetPoint.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!p.matcher(newValue).matches()) inputSetPoint.setText(oldValue);
        });
    }

    private void validationSpeed() {
        Pattern p = Pattern.compile("(\\d+\\.?\\d*)?");
        inputNewSpeed.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!p.matcher(newValue).matches()) inputNewSpeed.setText(oldValue);
        });
    }

    public void stop() {
        if (serialPort == null) return;
        if (serialPort.isOpened()) {
            try {
                serialPort.closePort();
            } catch (SerialPortException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    public void outputDataInTextField(String data) {
        Platform.runLater(() ->
                dataComPort.appendText(String.valueOf(data)));
    }

    public void resultInputWrite(Boolean resultInput) {
        Platform.runLater(() ->
                resultWrite.appendText(String.valueOf(resultInput)));
    }

    public class EventListener implements SerialPortEventListener {
        public void serialEvent(SerialPortEvent event) {
            if (event.isRXCHAR() && event.getEventValue() > 0) {
                try {
                    String data = serialPort.readString(event.getEventValue());
                    fileWriter(data);
                    outputDataInTextField(data);
                } catch (SerialPortException | IOException ex) {
                    System.out.println(ex);
                }
            }
        }
    }

    public void algorithmPID() {
        MiniPID miniPID;

        miniPID = new MiniPID(0.25, 0.01, 0.4);
        miniPID.setOutputLimits(10);
        //miniPID.setMaxIOutput(2);
        //miniPID.setOutputRampRate(3);
        //miniPID.setOutputFilter(.3);
        miniPID.setSetpointRange(40);

        double target=100;

        double actual=0;
        double output=0;

        miniPID.setSetpoint(0);
        miniPID.setSetpoint(target);

        System.err.printf("Target\tActual\tOutput\tError\n");
        //System.err.printf("Output\tP\tI\tD\n");

        // Position based test code
        for (int i = 0; i < 100; i++){

            //if(i==50)miniPID.setI(.05);

            if (i == 60)
                target = 50;

            //if(i==75)target=(100);
            //if(i>50 && i%4==0)target=target+(Math.random()-.5)*50;

            output = miniPID.getOutput(actual, target);
            actual = actual + output;

            //System.out.println("==========================");
            //System.out.printf("Current: %3.2f , Actual: %3.2f, Error: %3.2f\n",actual, output, (target-actual));
            System.err.printf("%3.2f\t%3.2f\t%3.2f\t%3.2f\n", target, actual, output, (target-actual));

            //if(i>80 && i%5==0)actual+=(Math.random()-.5)*20;
        }
    }

    private void loadStage() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/graphic.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.getIcons().add(new Image("/images/planets_Earth.jpg"));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void initialize() {
        validationSpeed();
        validationSetpoint();
        closePort.setTooltip(new Tooltip("Click the button and the\n" + "serial port will be closed"));
        openPort.setTooltip(new Tooltip("Click the button and the\n" + "serial port will be opened"));
        algorithmPID();
        requestPort();
        setPoint.setOnAction(action -> {
            if (inputSetPoint.getText().isEmpty()) {
                warningMessage();
            } else {
                try {
                    resultWrite.setText("setpoint: ");
                    resultInputWrite(serialPort.writeString("set_point" + inputSetPoint.getText()));
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
            }
        });

        setNewSpeed.setOnAction(action -> {
            if (inputNewSpeed.getText().isEmpty()) {
                warningMessage();
            } else {
                try {
                    resultWrite.setText("new revolution: ");
                    resultInputWrite(serialPort.writeString("new_speed" + inputNewSpeed.getText()));
                } catch (SerialPortException e) {
                    e.printStackTrace();
                }
            }
        });

        openPort.setOnAction(actionEvent -> initializeComPort());

        closePort.setOnAction(close -> stop());

        buildGraph.setOnAction(plot -> loadStage());
    }
}


