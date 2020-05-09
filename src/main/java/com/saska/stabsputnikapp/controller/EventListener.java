//package com.saska.stabsputnikapp.controller;
//
//import jssc.SerialPort;
//import jssc.SerialPortEvent;
//import jssc.SerialPortEventListener;
//import jssc.SerialPortException;
//
//import java.io.IOException;
//
//public class EventListener implements SerialPortEventListener {
//
//    public void serialEvent(SerialPortEvent event) {
//        if (event.isRXCHAR() && event.getEventValue() > 0) {
//            try {
//                SerialPort serialPort;
//                byte[] buffer = serialPort.readBytes(event.getEventValue());
////                  System.out.println("bufferByte - " + Arrays.toString(buffer));
//                String bufferStr = serialPort.readString(event.getEventValue());
//                System.out.println("bufferStr - " + bufferStr);
//                String[] parse = bufferStr.split("/n");
////                  String string = new String(buffer);
////                  System.out.println("String" + string);
//                fileWriter(buffer);
//                setDataInTextField(buffer[0], buffer[1]);
//                drawBeforeLine(buffer[0], buffer[1]);
//                progressData.setProgress(buffer[0]);
//                serialPort.closePort();
//            } catch (SerialPortException | IOException ex) {
//                System.out.println(ex);
//            }
//        }
//    }
//
//}