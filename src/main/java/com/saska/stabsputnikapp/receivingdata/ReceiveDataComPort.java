package com.saska.stabsputnikapp.receivingdata;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.util.Arrays;

public class ReceiveDataComPort {
    public static SerialPort serialPort;
    public static class EventListener implements SerialPortEventListener {

        @Override
        public void serialEvent(SerialPortEvent event) {
            if (event.isRXCHAR() && event.getEventValue() == 8) {
                try {
                    byte[] buffer = serialPort.readBytes(8);
                    System.out.println(buffer[0]);
                    System.out.println(buffer[1]);
                    serialPort.closePort();
                    System.out.println(Arrays.toString(buffer));
                    //fileWriter(buffer);
                } catch (SerialPortException ex) {
                    System.out.println(ex);
                }
            }
        }

    }

}

