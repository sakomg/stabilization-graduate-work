package com.saska.stabsputnikapp.hardware;

import jssc.SerialPortList;

import javax.swing.*;
import java.awt.*;

public class CheckIncludePort {

    public void requestPort() {
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
            portNameSelector.getSelectedItem();
        } else {
            System.exit(0);
        }
    }

}
