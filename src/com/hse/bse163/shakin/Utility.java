package com.hse.bse163.shakin;


import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;

public class Utility {

    public static int getHostPort(StringBuilder host) {
        JTextField hostField = new JTextField(15);
        JTextField portField = new JTextField(15);

        JPanel hpPanel = new JPanel();
        JLabel hostLabel = new JLabel("Host:");
        hpPanel.add(hostLabel);
        hpPanel.add(hostField);
        hpPanel.add(Box.createHorizontalStrut(50));
        hpPanel.add(new JLabel("Port:"));
        hpPanel.add(portField);

        boolean ok = false;
        String message = "Please Enter Host and Port";
        while (!ok) {
            int result = JOptionPane.showConfirmDialog(null, hpPanel,
                    message, JOptionPane.OK_CANCEL_OPTION);
            ok = result == JOptionPane.OK_OPTION;
            try {
                Integer.parseInt(portField.getText());
            } catch (NumberFormatException e) {
                ok = false;
                message = "Please Enter Host and Port ( PORT MUST BE INT )";
            }
        }

        host.append(hostField.getText());
        return Integer.parseInt(portField.getText());
    }


    /**
     * Sends file
     * @param in stream to read file from
     * @param out stream to write file in
     */
    public static void sendBytes(BufferedInputStream in , DataOutputStream out) throws Exception {

        int count;
        byte[] buffer = new byte[4096];
        while ((count = in.read(buffer)) > 0)
            out.write(buffer, 0, count);

    }
}
