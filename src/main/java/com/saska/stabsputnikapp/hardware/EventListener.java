package com.saska.stabsputnikapp.hardware;

import com.jfoenix.controls.JFXTextArea;
import com.saska.stabsputnikapp.receivingdata.CommunicateFile;
import javafx.application.Platform;
import javafx.fxml.FXML;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.io.IOException;

public class EventListener implements SerialPortEventListener {

    public static final String FILE = "src/main/resources/txtfiles/SerialReceive.txt";
    public static final String LOGFILE = "src/main/resources/txtfiles/LogReceiveData.txt";

    public static String data;
    public static SerialPort serialPort;
    public static String[] parseData;

    @FXML
    public JFXTextArea dataComPort;

    CommunicateFile communicate = new CommunicateFile();

    public void serialEvent(SerialPortEvent event) {
        if (event.isRXCHAR() && event.getEventValue() > 0) {
            try {
                Thread.sleep(300);
                data = serialPort.readString(event.getEventValue());
                parseData = data.split(" ");
                outputDataInTextField("inp: " + parseData[0] + " set: " + parseData[1] + " out: " + parseData[2] + "\n");
                communicate.fileWriter(data, FILE);
                communicate.logFileWriter(data, LOGFILE);
            } catch (SerialPortException | IOException | InterruptedException ex) {
                System.out.println(ex);
            }
        }
    }

    public void outputDataInTextField(String data) {
        Platform.runLater(() ->
                dataComPort.appendText(String.valueOf(data)));
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

}
