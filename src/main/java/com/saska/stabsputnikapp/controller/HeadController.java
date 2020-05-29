package com.saska.stabsputnikapp.controller;

import com.jfoenix.controls.JFXTextArea;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import com.jfoenix.validation.RequiredFieldValidator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jssc.*;
import othertasks.CodeWars.SimplyPID;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

public class HeadController {

    public static final String FILE = "src/main/resources/txtfiles/SerialReceive.txt";
    public static final String FILESET = "src/main/resources/txtfiles/ReceiveSetpoint.txt";
    public static final String LOGFILESET = "src/main/resources/txtfiles/LogSetpoint.txt";
    public static final String LOGFILE = "src/main/resources/txtfiles/LogReceiveData.txt";
    public static SerialPort serialPort;
    public static String data;

    @FXML
    private JFXTextArea dataComPort;

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
    private Button buildGraph;

    @FXML
    private Button copy;

    @FXML
    private JFXToggleButton openclose;

    public StringBuilder fileReader() throws IOException {
        FileReader rFile = new FileReader(FILE);
        Scanner scan = new Scanner(rFile);
        StringBuilder fullFile = new StringBuilder();
        while (scan.hasNextInt()) {
            fullFile.append(scan.nextInt());
        }
        rFile.close();
        return fullFile;
    }

    public String readFile() throws IOException {
        return Files.lines(Paths.get(FILE)).reduce("",(a, b) -> a + "\n" + b).trim();
    }

    public List<Double> readerOutput() throws FileNotFoundException {
        File file = new File(FILE);
        Scanner scanner = new Scanner(file);
        List<Double> doublesInput = new ArrayList<>();
        while (scanner.hasNext()) {
            if (scanner.hasNextDouble()) {
                doublesInput.add(scanner.nextDouble());
            } else {
                scanner.next();
            }
        }
        return doublesInput;
    }

    public List<Double> readerSetpoint() throws FileNotFoundException {
        File file = new File(FILESET);
        Scanner scanner = new Scanner(file);
        List<Double> doublesOutput = new ArrayList<>();
        while (scanner.hasNext()) {
            if (scanner.hasNextDouble()) {
                doublesOutput.add(scanner.nextDouble());
            } else {
                scanner.next();
            }
        }
        return doublesOutput;
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

    private void validationSetpoint() {
        Pattern p = Pattern.compile("(\\d+\\.?\\d*)?");
        inputSetPoint.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!p.matcher(newValue).matches()) inputSetPoint.setText(oldValue);
        });
    }

    private void validationkP() {
        Pattern p = Pattern.compile("(\\d+\\.?\\d*)?");
        kP.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!p.matcher(newValue).matches()) kP.setText(oldValue);
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

    public void resultInputWrite(Boolean resultInput) {
        Platform.runLater(() ->
                resultWrite.appendText(String.valueOf(resultInput)));
    }

    public class EventListener implements SerialPortEventListener {

        public void serialEvent(SerialPortEvent event) {
            if (event.isRXCHAR() && event.getEventValue() > 0) {
                try {
                    data = serialPort.readString(event.getEventValue());
                    outputDataInTextField(data);
                    Thread.sleep(100);
                    fileWriter(data, FILE);
                    logFileWriter(data, LOGFILE);
                } catch (SerialPortException | IOException | InterruptedException ex) {
                    System.out.println(ex);
                }
            }
        }

        public void outputDataInTextField(String data) {
            Platform.runLater(() ->
                    dataComPort.appendText(String.valueOf(data)));
        }
    }

    private void fileWriter(String buffer, String path) throws IOException {
        FileWriter wFile = new FileWriter(path, true);
        wFile.write(buffer);
        wFile.close();
    }

    private void logFileWriter(String buffer, String path) {
        try {
            FileWriter fw = new FileWriter(path, true);
            PrintWriter out = new PrintWriter(fw);
            DateFormat dateF = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            String time = dateF.format(cal.getTime());
            out.print(time + ": ");
            fw.write(buffer.toUpperCase() + "\n");
            out.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void calculatePID(double currentValue) {
        double output = 0;

        SimplyPID pid = new SimplyPID(0, 1.2, 0, 0.025);

        System.out.printf("Time\tSet Point\tCurrent value\tOutput\tError\n");

        for (int i = 0; i < 30; i++){
            System.out.printf("%d\t%3.2f\t%3.2f\t%3.2f\t%3.2f\n", i, pid.getSetPoint(), currentValue, output, (pid.getSetPoint()-currentValue));
            if (i == 15)
                pid.setSetpoint(50);
            // Compute the output (assuming 1 unit of time passed between each measurement)
            output = pid.getOutput(1,currentValue);
            currentValue += output*1.3 + (Math.random()-.5)*3;
        }
    }

    private void loadStage() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/graphic.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.getIcons().add(new Image("/images/planets_Earth.jpg"));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void initialize() {
        RequiredFieldValidator requiredInputValidator = new RequiredFieldValidator();
        requiredInputValidator.setMessage("Cannot be empty");
        validationkP();
        validationSetpoint();
        requestPort();
        //calculatePID(Double.parseDouble(data)); //----------------
        inputSetPoint.getValidators().add(requiredInputValidator);
        inputSetPoint.focusedProperty().addListener((o, oldVal, newVal) -> {
            if (!newVal) {
                inputSetPoint.validate();
            }
        });

        setPoint.setOnAction(action -> {
            if (inputSetPoint.getText().isEmpty()) {
                warningMessage();
            } else {
                try {
                    resultWrite.setText("setpoint: ");
                    resultInputWrite(serialPort.writeString("set_point" + inputSetPoint.getText()));
                    logFileWriter(inputSetPoint.getText(), LOGFILESET);
                    fileWriter(inputSetPoint.getText(), FILESET);
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
        buildGraph.setOnAction(plot -> loadStage());

        openclose.setOnAction(event -> {

            if(openclose.isSelected()){
                initializeComPort();
                openclose.setText("Open port");
                openclose.setTooltip(new Tooltip("Click and the serial\n" + "port will be closed"));
            }
            else{
                stop();
                openclose.setText("Close port");
                openclose.setTooltip(new Tooltip("Click and the serial\n" + "port will be opened"));
            }
        });
    }
}


