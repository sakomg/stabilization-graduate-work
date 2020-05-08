package com.saska.stabsputnikapp.receivingdata;

import jssc.*;

import java.util.Arrays;

class ReceiveDataComPort {
    private static SerialPort serialPort;


    public static void main(String[] args) {
        serialPort = new SerialPort("COM3");
        try {
            serialPort.openPort();
            serialPort.setParams(SerialPort.BAUDRATE_9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
            serialPort.addEventListener(new EventListener());
        } catch (SerialPortException ex) {
            ex.printStackTrace();
        }
    }


    public static class EventListener implements SerialPortEventListener {

        public void serialEvent(SerialPortEvent event) {
            if (event.isRXCHAR() && event.getEventValue() > 0) {
                try {
                    byte[] buffer = serialPort.readBytes();


                    System.out.println(Arrays.toString(buffer));
                    serialPort.closePort();

                } catch (SerialPortException ex) {
                    System.out.println(ex);

                }
            }
        }
    }

}


