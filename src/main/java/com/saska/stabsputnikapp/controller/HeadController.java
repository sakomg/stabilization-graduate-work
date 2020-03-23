package com.saska.stabsputnikapp.controller;


import com.saska.stabsputnikapp.receivingdata.ReceiveDataComPort;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import jssc.*;

import javax.swing.*;
import java.awt.*;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.regex.Pattern;

public class HeadController extends ReceiveDataComPort {

    public static SerialPort serialPort;
    @FXML
    private ResourceBundle resources;
    @FXML
    private URL location;
    @FXML
    private Line line;
    @FXML
    private Button changesButton;
    @FXML
    private TextField angleInput;
    @FXML
    private TextArea dataComPort;
    @FXML
    private Button writeDataInFile;
    @FXML
    private ProgressBar progressBar;

    public static StringBuilder fileReader() throws IOException {
        FileReader rFile = new FileReader("ReceiveData.txt");
        Scanner scan = new Scanner(rFile);
        StringBuilder fullFile = new StringBuilder();
        while (scan.hasNext()) {
            fullFile.append(scan.next());
        }
        rFile.close();
        return fullFile;
    }

    public static void initializeComPort() {
        serialPort = new SerialPort(getPort());
        try {
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
            serialPort.addEventListener(new EventListener());
        } catch (SerialPortException ex) {
        }
    }

    static String getPort() {
        String[] portNames = SerialPortList.getPortNames();
        return portNames[0];
    }

    @SuppressWarnings("unchecked")
    private static String requestPort() {
        JComboBox<String> portNameSelector = new JComboBox<>();
        portNameSelector.setModel(new DefaultComboBoxModel<String>());
        String[] portNames;
        if (SerialNativeInterface.getOsType() == SerialNativeInterface.OS_MAC_OS_X) {
            portNames = SerialPortList.getPortNames("/dev/", Pattern.compile("tty\\..*"));
        } else {
            portNames = SerialPortList.getPortNames();
        }
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
            return portNameSelector.getSelectedItem().toString();
        } else {
            System.exit(0);
        }
        return "";
    }

    private static void fileWriter(byte[] buffer) throws IOException {
        FileWriter wFile = new FileWriter("ReceiveData.txt");
        wFile.write(Arrays.toString(buffer));
        wFile.close();
    }

    @FXML
    void initialize() throws IOException {

        validationAngleInput();
        fileReader();
        requestPort();

        double beforeBx = ElementBx(parseFileReader(fileReader()));
        double beforeBy = ElementBy(parseFileReader(fileReader()));


        writeDataInFile.setOnAction(actionEvent -> {
            initializeComPort();
            progressBar.setProgress(beforeBx);
            progressBar.setProgress(beforeBy);
            try {
                parseFileReader(fileReader());
            } catch (IOException e) {
                e.printStackTrace();
            }
            setDataInTextField(beforeBx, beforeBy);
        });

        line.setStartX(0);
        line.setStartY(0);
        line.setEndX(beforeBx);
        line.setEndY(beforeBy);
        line.setStrokeWidth(5);
        line.setStroke(Color.MEDIUMAQUAMARINE);

        changesButton.setOnAction(actionEvent -> {
            if (angleInput.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Уведомление");
                alert.setHeaderText("Поле Rotation angle пустое");
                alert.setContentText("Введите в поле Rotation angle число, чтобы продолжить");
                alert.showAndWait();
            } else {
                double alpha = Math.atan(beforeBy / beforeBx);
                double beta = Double.parseDouble(angleInput.getText());
                double radius = Math.sqrt(Math.pow(beforeBx, 2) + Math.pow(beforeBy, 2));

                double afterBx = radius * Math.cos(alpha + beta * Math.PI / 180);
                double afterBy = radius * Math.sin(alpha + beta * Math.PI / 180);

                line.setStartX(0);
                line.setStartY(0);
                line.setEndX(afterBx);
                line.setEndY(afterBy);
                line.setStrokeWidth(5);
                line.setStroke(Color.MEDIUMBLUE);
            }
        });
    }

    private String[] parseFileReader(StringBuilder fullFile) {
        String[] numbers = fullFile.toString().replace("[", "").replace("]", "").split(",");
        return numbers;
    }

    public double ElementBx(String[] numbers) {
        double Bx = Double.parseDouble(numbers[0]);
        return Bx;
    }

    public double ElementBy(String[] numbers) {
        double By = Double.parseDouble(numbers[1]);
        return By;
    }

    public void setDataInTextField(double Bx, double By) {
        dataComPort.setText("Bx: " + Bx + "\n" + "By: " + By);
    }

    private void validationAngleInput() {
        Pattern p = Pattern.compile("(\\d+\\.?\\d*)?");
        angleInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!p.matcher(newValue).matches()) angleInput.setText(oldValue);
        });
    }

    @Override
    public String toString() {
        return "MainController{" +
                "resources=" + resources +
                ", location=" + location +
                ", line=" + line +
                ", changesButton=" + changesButton +
                ", angleInput=" + angleInput +
                ", dataComPort=" + dataComPort +
                '}';
    }

    public static class EventListener implements SerialPortEventListener {

        public void serialEvent(SerialPortEvent event) {
            if (event.isRXCHAR() && event.getEventValue() == 16) {
                try {
                    byte[] buffer = serialPort.readBytes(16);
                    // System.out.println(buffer[0]);
                    // System.out.println(buffer[1]);
                    serialPort.closePort();
                    // System.out.println(Arrays.toString(buffer));
                    fileWriter(buffer);

                } catch (SerialPortException | IOException ex) {
                    System.out.println(ex);
                }
            }
        }
    }
}
