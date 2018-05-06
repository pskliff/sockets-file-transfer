package com.hse.bse163.shakin;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import javafx.stage.FileChooser;

import java.io.*;
import java.net.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.TimerTask;

class Client extends JFrame implements ActionListener, MouseListener {
    private JPanel panel;
    private JLabel titleLabel, fNameInpLabel, errorLabel, filesFolderLabel;
    private Font titleFont, labelFont;
    private JTextField fileNameText;
    private JButton downloadButton, folderLocation;
    private File clientDirectory;
    private Socket clientSocket;
    private InputStream inFromServer;
    private OutputStream outToServer;
    private PrintWriter pw;
    private DataInputStream inputData;


    private String name, file, path;
    private String hostAddr;
    private int portNumber;
    int c;
    private JList<String> filesList;
    private HashSet<String> names;
    private HashSet<String> clientFileNames;
    private int len;


    private BufferedReader in;

    final JFileChooser fileChooser = new JFileChooser();



    public Client() {
        super("Simple Torrent");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }


    private int getHostPort(StringBuilder host) {
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


    private void run(String dir, String host, int port) {
        // set clientDirectory to the one that's entered by the user
//        clientDirectory = dir.charAt(dir.length() - 1) == '/' ? dir : dir + "/";

        clientDirectory = fileChooser.getCurrentDirectory();

        StringBuilder hostBuild = new StringBuilder();
        portNumber = getHostPort(hostBuild);
        hostAddr = hostBuild.toString();



        panel = new JPanel(null);



        titleFont = new Font("Helvetica", Font.BOLD, 30);
        titleLabel = new JLabel("Simple Torrent");
        titleLabel.setFont(titleFont);
        titleLabel.setBounds(300, 50, 700, 40);
        panel.add(titleLabel);


        folderLocation = new JButton("Show client files");
        folderLocation.setBounds(550, 130, 150, 40);
        panel.add(folderLocation);



        labelFont = new Font("Helvetica Neue", Font.PLAIN, 20);
        fNameInpLabel = new JLabel("Enter File Name :");
        fNameInpLabel.setFont(labelFont);
        fNameInpLabel.setBounds(100, 450, 200, 50);
        panel.add(fNameInpLabel);



        fileNameText = new JTextField();
        fileNameText.setBounds(400, 450, 500, 50);
        panel.add(fileNameText);

        downloadButton = new JButton("Download");
        downloadButton.setBounds(550, 550, 200, 50);
        panel.add(downloadButton);

        errorLabel = new JLabel("");
        errorLabel.setFont(labelFont);
        errorLabel.setBounds(200, 650, 600, 50);
        panel.add(errorLabel);



        downloadButton.addActionListener(this);
        folderLocation.addActionListener(this);

        try {
            clientSocket = new Socket(hostAddr, portNumber);
            inFromServer = clientSocket.getInputStream();
            pw = new PrintWriter(clientSocket.getOutputStream(), true);
            outToServer = clientSocket.getOutputStream();
            in = new BufferedReader(new InputStreamReader(inFromServer));
            inputData = new DataInputStream(inFromServer);

            String successIndicator = in.readLine();
            System.out.println(successIndicator);

            len = Integer.parseInt(in.readLine());
            System.out.println(len);

            String[] temp_names = new String[len];
            names = new HashSet<>();

            for (int i = 0; i < len; i++) {
                String filename = in.readLine();
                System.out.println(filename);
                names.add(filename);
                temp_names[i] = filename;
            }

            // sort the array of strings that's going to get displayed in the scrollpane
            Arrays.sort(temp_names);

            filesFolderLabel = new JLabel("Files in the Server Directory :");
            filesFolderLabel.setBounds(350, 125, 400, 50);
            panel.add(filesFolderLabel);

            filesList = new JList<>(temp_names);
            JScrollPane scroll = new JScrollPane(filesList);
            scroll.setBounds(300, 200, 400, 200);

            panel.add(scroll);
            filesList.addMouseListener(this);


            updateFileNames();

        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            errorLabel.setText("Exception:" + e.getMessage());
            errorLabel.setBounds(300, 125, 600, 50);
            panel.revalidate();
        }

        getContentPane().add(panel);
        setVisible(true);
    }


    public void mouseClicked(MouseEvent click) {
        if (click.getClickCount() == 2) {
            String selectedItem = filesList.getSelectedValue();
            fileNameText.setText(selectedItem);
            panel.revalidate();
        }
    }


    public void mousePressed(MouseEvent e) {
    }


    public void mouseEntered(MouseEvent e) {
    }


    public void mouseExited(MouseEvent e) {
    }


    public void mouseReleased(MouseEvent e) {
    }

    void updateFileNames()
    {
        clientFileNames = new HashSet<>(Arrays.asList(clientDirectory.list()));
    }

    void updateFileList(boolean showServerFiles) {
        String[] temp_names;
        // "Show client files".equals(folderLocation.getText())
        if (!showServerFiles) {
            temp_names = clientFileNames.toArray(new String[clientFileNames.size()]);
            folderLocation.setText("Show server files");
            filesFolderLabel.setText("Files in the Client Directory :");
        } else {
            temp_names = names.toArray(new String[names.size()]);
            folderLocation.setText("Show client files");
            filesFolderLabel.setText("Files in the Server Directory :");
        }

        Arrays.sort(temp_names);
        filesList.setListData(temp_names);
    }


    private boolean hasPermission(int fileSize) {

        JPanel hpPanel = new JPanel();
        JLabel askLabel = new JLabel(String.format("Do you want to download a file of size = %.4f kb ?", fileSize / 1024.0));

        hpPanel.add(askLabel);
        String message = "Do you??";
        int result = JOptionPane.showConfirmDialog(null, hpPanel,
                message, JOptionPane.OK_CANCEL_OPTION);

        return result == JOptionPane.OK_OPTION;
    }


    public static class ProgressDialog {

        private final JFrame frame = new JFrame();
        private final JDialog dialog = new JDialog(frame, "Download Progress", false);
        private final JProgressBar progressBar = new JProgressBar();


        /**
         * Конструктор progreeBar и диалога, в котором он содержится
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



    private void runProgress(String s, int fileSize, File directory, ProgressDialog progress) throws IOException {

//        inputData.skipBytes(inputData.available());

        byte[] data = new byte[4096];

        if (s.equals("Success")) {
            File f = new File(directory, name);
            FileOutputStream fileOut = new FileOutputStream(f);
            DataOutputStream dataOut = new DataOutputStream(fileOut);



            long totalDown = 0;
            int remaining = fileSize;
            System.out.println("File size = " + fileSize);
            while ((c = inputData.read(data, 0, Math.min(data.length, remaining))) > 0) {


                totalDown += c;
                remaining -= c;
                final int percent = (int) (totalDown * 100 / fileSize);

                SwingUtilities.invokeLater(() ->  progress.updateBar(percent));
//                progress.updateBar(percent);

                if (fileSize < 1e3)
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                else if (fileSize < 1e6)
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }


                dataOut.write(data, 0, c);
            }
            System.out.println("Completed");
            errorLabel.setText("Completed");

            if (!clientFileNames.contains(name))
                clientFileNames.add(name);

            updateFileNames();
            updateFileList(false);
            fileOut.close();
        } else {
            System.out.println("Requested file not found on the server.");
            errorLabel.setText("Requested file not found on the server.");
            panel.revalidate();
        }
    }


    private static void configureFileChooser(
            final JFileChooser fileChooser) {

        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Select directory to save file");
//        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
    }


    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == folderLocation) {
            updateFileList("Show server files".equals(folderLocation.getText()));

        } else if (event.getSource() == downloadButton) {
            try {

                String fileChooseMsg = "Select directory to save file";

                File bufDir;
                configureFileChooser(fileChooser);

                    int ret = fileChooser.showDialog(null, fileChooseMsg);
                     bufDir = clientDirectory;
                    if (ret == JFileChooser.APPROVE_OPTION)
                    {
                        bufDir = fileChooser.getCurrentDirectory();
                        clientDirectory = bufDir;
                    }

                final File directory = bufDir;

//                if (!directory.exists()) {
//                    directory.mkdir();
//                }

                name = fileNameText.getText();
                file = new String("*" + name + "*");

                inputData.skipBytes(inputData.available());
                pw.println(file); //lets the server know which file is to be downloaded


                String s = in.readLine();
                int fileSize = Integer.parseInt(in.readLine());

                if (hasPermission(fileSize)) {
                    final ProgressDialog progress = new ProgressDialog();

                    java.util.Timer timer = new java.util.Timer();
                    final TimerTask task = new TimerTask() {
                        public void run() {
                            progress.showDialog();
                        }
                    };


                    timer.schedule(task, 1);
                    Thread someThread = new Thread(new Runnable() {
                        public void run() {

                            try {
                                runProgress(s, fileSize, directory, progress);
                            } catch (IOException e) {

                            }
                            SwingUtilities.invokeLater(new Runnable() {
                                public void run() {
                                    task.cancel();
                                    progress.closeDialog();
                                }
                            });
                        }
                    });
                    someThread.start();

                }


            } catch (Exception exc) {
                System.out.println("Exception: " + exc.getMessage());
                errorLabel.setText("Exception:" + exc.getMessage());
                panel.revalidate();
            }
        }
    }


    private static void sendBytes(BufferedInputStream in, OutputStream out, int fileLength) throws Exception {
//        int size = 9022386;
        byte[] data = new byte[fileLength];
        int bytes = 0;
        int c = in.read(data, 0, data.length);
        out.write(data, 0, c);
        out.flush();
    }



    public static void main(String args[]) {

        // if at least three argument are passed, consider the first one as directory path,
        // the second one as host address and the third one as port number
        // If host address is not present, default it to "localhost"
        // If port number is not present, default it to 3333
        // If directory path is not present, show errorLabel
        if (args.length >= 3) {
            Client tcp = new Client();
            tcp.setSize(1000, 900);
//            tcp.setVisible(true);
            tcp.run(args[0], args[1], Integer.parseInt(args[2]));
        } else if (args.length == 2) {
            Client tcp = new Client();
            tcp.setSize(1000, 900);
//            tcp.setVisible(true);
            tcp.run(args[0], args[1], 8888);
        } else if (args.length == 1) {
            Client tcp = new Client();
            tcp.setSize(1000, 900);
//            tcp.setVisible(true);
            tcp.run(args[0], "localhost", 8888);
        } else {
            System.out.println("Please enter the client directory address as first argument while running from command line.");
        }
    }
}



//        JLabel hostLabel = new JLabel("Host:");
//        hostLabel.setBounds(100, 100, 50, 20);
//        panel.add(hostLabel);
//
//        JTextField hostField = new JTextField();
//        hostField.setBounds(170, 100, 150, 20);
//        panel.add(hostField);
//
//
//
//        JLabel portLabel = new JLabel("Port:");
//        portLabel.setBounds(350, 100, 100, 20);
//        panel.add(portLabel);
//
//        JTextField portField = new JTextField();
//        portField.setBounds(420, 100, 100, 20);
//        panel.add(portField);



//
//
//else if (event.getSource() == uploadButton) {
//        try {
//        name = fileNameText.getText();
//
//        FileInputStream file = null;
//        BufferedInputStream bis = null;
//
//        boolean fileExists = true;
//        path = clientDirectory + name;
//
//        File fileToUpload = new File(path);
//        int fileLength = 0;
//
//        try {
//        fileLength = (int) fileToUpload.length();
//        file = new FileInputStream(path);
//        bis = new BufferedInputStream(file);
//        } catch (FileNotFoundException e) {
//        fileExists = false;
//        System.out.println("FileNotFoundException:" + e.getMessage());
//        errorLabel.setText("FileNotFoundException:" + e.getMessage());
//        panel.revalidate();
//        }
//
//        if (fileExists) {
//        // send file name to server
//        pw.println(name);
//        pw.println(fileLength);
//
//        System.out.println("Upload begins");
//        errorLabel.setText("Upload begins");
//        panel.revalidate();
//
//        // send file data to server
//        sendBytes(bis, outToServer, fileLength);
//
//        System.out.println("Completed");
//        errorLabel.setText("Completed");
//        panel.revalidate();
//
//        boolean exists = names.contains(name);
//
//        if (!exists) {
//        names.add(name);
//        len++;
//        }
//
////                    String[] temp_names = (String[])names.toArray();
////                    for (int i = 0; i < len; i++) {
////                        temp_names[i] = names[i];
////                    }
//
//        // sort the array of strings that's going to get displayed in the scrollpane
////                    Arrays.sort(temp_names);
//
//        // update the contents of the list in scroll pane
////                    filesList.setListData(temp_names);
//
//        updateFileList(true);
//
//        // close all file buffers
//        bis.close();
//        file.close();
//        outToServer.close();
//        }
//        } catch (Exception e) {
//        System.out.println("Exception: " + e.getMessage());
//        errorLabel.setText("Exception:" + e.getMessage());
//        panel.revalidate();
//        }



//empty file case
//                    while (complete) {
//                        c = inputData.read(data, 0, data.length); //data.length
//                        if (c <= 0) {
//                            complete = false;
//                            System.out.println("Completed");
//                            errorLabel.setText("Completed");
//                            panel.revalidate();
//
//
//                        } else {
//                            dataOut.write(data, 0, c);
//                            dataOut.flush();
//
//                            if(!clientFileNames.contains(name))
//                                clientFileNames.add(name);
//                        }
//                    }



//        final DownloadProgressBarPanel progressBar = new DownloadProgressBarPanel(0, 100);
//                    String message = "Progress";
//                    int result = JOptionPane.showConfirmDialog(null, progressBar,
//                            message, JOptionPane.OK_CANCEL_OPTION);

//        JFrame frame = new JFrame("Progress Bar Example");
//        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
//        frame.setContentPane(progressBar);
//        frame.pack();
//        frame.setVisible(true);
//        JDialog dialog = new JDialog(this, "Progress");
//        dialog.getContentPane().add(progressBar);
//        dialog.setVisible(true);



//public static class DownloadProgressBarPanel extends JPanel {
//
//    JProgressBar pbar;
//
//    private int minValue = 0;
//
//    private int maxValue = 100;
//
//    public DownloadProgressBarPanel(int min, int max) {
//
//        minValue = min;
//        maxValue = max;
//
//        // initialize Progress Bar
//        pbar = new JProgressBar();
//        pbar.setMinimum(minValue);
//        pbar.setMaximum(maxValue);
////            pbar.setIndeterminate(true);
//
//        // add to JPanel
//        add(pbar);
//    }
//
//    public void updateBar(int newValue) {
//        pbar.setValue(newValue);
//    }
//}


//                try {
//
////                    java.lang.Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    System.out.println("progress Bar Interrupted exception: " + e.getMessage() );
//                }

//                progressBar.setValue(percent);
//


//                try {
//                    SwingUtilities.invokeLater(() ->  progressBar.setValue(percent));
//                    java.lang.Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    System.out.println("progress Bar Interrupted exception: " + e.getMessage() );
//                }



//SwingUtilities.invokeLater(() -> {
//        try{
//        runProgress(s, fileSize, directory);
//        }
//        catch (IOException e)
//        {
//        System.out.println("progress bar IOExc = " + e.getMessage());
//        }
//
//        });
