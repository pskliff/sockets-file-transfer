package com.hse.bse163.shakin;

import javafx.concurrent.Task;

import java.io.*;
import java.net.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Arrays;
import java.util.HashSet;

class Client extends JFrame implements ActionListener, MouseListener {
    private JPanel panel;
    private JLabel titleLabel, fNameInpLabel, errorLabel, filesFolderLabel;
    private Font titleFont, labelFont;
    private JTextField fileNameText;
    private JButton downloadButton, folderLocation;
    private File clientDirectory;
    private Socket clientSocket;
    private InputStream inFromServer;
    private PrintWriter pw;
    private DataInputStream inputData;


    private String name, file;
    private String hostAddr;
    private int portNumber;
    int cnt;
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


    /**
     * initialize GUI
     */
    void initGUI()
    {
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
    }



    /**
     * Connects to the server and starts the client GUI
     */
    private void run() {

        clientDirectory = fileChooser.getCurrentDirectory();

        StringBuilder hostBuild = new StringBuilder();
        portNumber = Utility.getHostPort(hostBuild);
        hostAddr = hostBuild.toString();

        initGUI();

        try {
            clientSocket = new Socket(hostAddr, portNumber);
            inFromServer = clientSocket.getInputStream();
            pw = new PrintWriter(clientSocket.getOutputStream(), true);
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


            Arrays.sort(temp_names);

            filesFolderLabel = new JLabel("Files in the SERVER Directory :");
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


    /**
     * Handles double mouse click on the scrollpane
     * @param click
     */
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


    /**
     * Updates list of file names of the last client directory
     */
    void updateFileNames()
    {
        clientFileNames = new HashSet<>(Arrays.asList(clientDirectory.list()));
    }


    /**
     * Switches scroll pane contents between server files and client files
     */
    void updateFileList(boolean showServerFiles) {
        String[] temp_names;
        if (!showServerFiles) {
            temp_names = clientFileNames.toArray(new String[clientFileNames.size()]);
            folderLocation.setText("Show server files");
            filesFolderLabel.setText("Files in the CLIENT Directory :");
        } else {
            temp_names = names.toArray(new String[names.size()]);
            folderLocation.setText("Show client files");
            filesFolderLabel.setText("Files in the SERVER Directory :");
        }

        Arrays.sort(temp_names);
        filesList.setListData(temp_names);
    }



    /**
     * Runs Download of the file with showing it in the progress bar
     */
    private void runProgress(String successIndicator, int fileSize, File directory, Utility.ProgressDialog progress) throws IOException {


        byte[] data = new byte[4096];

        if (successIndicator.equals("Success")) {
            File f = new File(directory, name);
            FileOutputStream fileOut = new FileOutputStream(f);
            DataOutputStream dataOut = new DataOutputStream(fileOut);


            long totalDown = 0;
            int remaining = fileSize;
            System.out.println("File size = " + fileSize);
            while ((cnt = inputData.read(data, 0, Math.min(data.length, remaining))) > 0) {


                totalDown += cnt;
                remaining -= cnt;
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


                dataOut.write(data, 0, cnt);
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



    /**
     * handles all button click events
     */
    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == folderLocation) {
            updateFileList("Show server files".equals(folderLocation.getText()));

        } else if (event.getSource() == downloadButton) {
            try {

                String fileChooseMsg = "Select directory to save file";

                File bufDir;
                Utility.configureFileChooser(fileChooser);

                    int ret = fileChooser.showDialog(null, fileChooseMsg);
                     bufDir = clientDirectory;
                    if (ret == JFileChooser.APPROVE_OPTION)
                    {
                        bufDir = fileChooser.getCurrentDirectory();
                        clientDirectory = bufDir;
                    }

                final File directory = bufDir;


                name = fileNameText.getText();
                file = "*" + name + "*";

                inputData.skipBytes(inputData.available());
                pw.println(file);


                String s = in.readLine();
                int fileSize = Integer.parseInt(in.readLine());

                if (Utility.hasPermission(fileSize)) {
                    final Utility.ProgressDialog progress = new Utility.ProgressDialog();


                    final Task task = new Task() {
                        @Override
                        protected Object call() throws Exception {
                            progress.showDialog();
                            return 0;
                        }
                    };


                    Thread someThread = new Thread(() -> {

                        try {
                            runProgress(s, fileSize, directory, progress);
                        } catch (IOException e) {

                        }
                        SwingUtilities.invokeLater(() -> {
                            progress.closeDialog();
                        });
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




    public static void main(String args[]) {
        Client tcp = new Client();
        tcp.setSize(1000, 900);
        tcp.run();
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
//                        cnt = inputData.read(data, 0, data.length); //data.length
//                        if (cnt <= 0) {
//                            complete = false;
//                            System.out.println("Completed");
//                            errorLabel.setText("Completed");
//                            panel.revalidate();
//
//
//                        } else {
//                            dataOut.write(data, 0, cnt);
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
