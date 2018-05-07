package com.hse.bse163.shakin;


import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;

public class Utility {

    public static class ProgressDialog {

        private final JFrame frame = new JFrame();
        private final JDialog dialog = new JDialog(frame, "Download Progress", false);
        private final JProgressBar progressBar = new JProgressBar();


        /**
         * Конструктор progressBar и диалога, в котором он содержится
         */
        public ProgressDialog() {

            frame.setBounds(0, 0, 1000, 100);
            dialog.setBounds(0, 0, 800, 80);
            frame.setUndecorated(true);


            progressBar.setMinimum(0);
            progressBar.setMaximum(100);
            progressBar.setValue(0);
            progressBar.setStringPainted(true);

            progressBar.setForeground(new Color(210, 105, 030));

            progressBar.setPreferredSize(new Dimension(500, 20));

            progressBar.setUI(new ProgressUI());

            dialog.setUndecorated(true);


            dialog.getContentPane().add(progressBar);
            dialog.pack();
            dialog.setDefaultCloseOperation(0);


            Toolkit kit = dialog.getToolkit();
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] gs = ge.getScreenDevices();
            Insets in = kit.getScreenInsets(gs[0].getDefaultConfiguration());
            Dimension d = kit.getScreenSize();
            int max_width = (d.width - in.left - in.right);
            int max_height = (d.height - in.top - in.bottom);
            dialog.setLocation((int) (max_width - dialog.getWidth()) / 2, (int) (max_height - dialog.getHeight()) / 2);



            dialog.setVisible(true);
            progressBar.setVisible(true);
            dialog.setAlwaysOnTop(true);
        }



        public void showDialog() {
            dialog.setVisible(true);
            dialog.setAlwaysOnTop(true);
        }



        public void closeDialog() {
            if (dialog.isVisible()) {
                dialog.getContentPane().remove(progressBar);
                dialog.getContentPane().validate();
                dialog.setVisible(false);
            }
        }


        public void updateBar(int val) {
            progressBar.setValue(val);
        }



        public static class ProgressUI extends BasicProgressBarUI {
            private Rectangle r = new Rectangle();


            protected void paintIndeterminate(Graphics g, JComponent c) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                r = getBox(r);
                g.setColor(progressBar.getForeground());
                g.fillOval(r.x, r.y, r.height, r.height);
            }
        }
    }


    public static void configureFileChooser(
            final JFileChooser fileChooser) {

        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Select directory to save file");
    }


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
     * Dialog that asks if user wants to download file
     */
    public static boolean hasPermission(long fileSize) {

        JPanel hpPanel = new JPanel();
        JLabel askLabel = new JLabel(String.format("Do you want to download a file of size = %.4f MB ?", fileSize / 1e6));

        hpPanel.add(askLabel);
        String message = "Do you??";
        int result = JOptionPane.showConfirmDialog(null, hpPanel,
                message, JOptionPane.OK_CANCEL_OPTION);

        return result == JOptionPane.OK_OPTION;
    }


    /**
     * Sends file
     *
     * @param in  stream to read file from
     * @param out stream to write file in
     */
    public static void sendBytes(BufferedInputStream in, DataOutputStream out) throws Exception {

        int count;
        byte[] buffer = new byte[4096];
        while ((count = in.read(buffer)) > 0)
            out.write(buffer, 0, count);

    }
}
