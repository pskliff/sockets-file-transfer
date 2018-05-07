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
    private JLabel errorLabel;
    private JLabel filesFolderLabel;
    private JTextField fileNameText;
    private JButton downloadButton, folderLocation;
    private File clientDirectory;
    private PrintWriter pw;
    private DataInputStream inputData;


    private String name;
    private JList<String> filesList;
    private HashSet<String> names;
    private HashSet<String> clientFileNames;


    private BufferedReader in;

    private final JFileChooser fileChooser = new JFileChooser();



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

        Font titleFont = new Font("Helvetica", Font.BOLD, 30);
        JLabel titleLabel = new JLabel("Simple Torrent");
        titleLabel.setFont(titleFont);
        titleLabel.setBounds(300, 50, 700, 40);
        panel.add(titleLabel);


        folderLocation = new JButton("Show client files");
        folderLocation.setBounds(550, 130, 150, 40);
        panel.add(folderLocation);


        Font labelFont = new Font("Helvetica Neue", Font.PLAIN, 20);
        JLabel fNameInpLabel = new JLabel("Enter File Name :");
        fNameInpLabel.setFont(labelFont);
        fNameInpLabel.setBounds(100, 650, 200, 50);
        panel.add(fNameInpLabel);


        fileNameText = new JTextField();
        fileNameText.setBounds(310, 650, 500, 50);
        panel.add(fileNameText);


        downloadButton = new JButton("Download");
        downloadButton.setBounds(550, 750, 200, 50);
        panel.add(downloadButton);


        errorLabel = new JLabel("");
        errorLabel.setFont(labelFont);
        errorLabel.setBounds(200, 750, 600, 50);
        panel.add(errorLabel);


        downloadButton.addActionListener(this);
        folderLocation.addActionListener(this);
    }



    /**
     * Connects to the server and starts the client GUI
     */
    private void run() {

        clientDirectory = fileChooser.getCurrentDirectory();

        System.out.println("At Start ClientDirectory = " + clientDirectory.toString());

        StringBuilder hostBuild = new StringBuilder();
        int portNumber = Utility.getHostPort(hostBuild);
        String hostAddr = hostBuild.toString();

        initGUI();

        try {
            Socket clientSocket = new Socket(hostAddr, portNumber);
            InputStream inFromServer = clientSocket.getInputStream();
            pw = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(inFromServer));
            inputData = new DataInputStream(inFromServer);

            String successIndicator = in.readLine();
            System.out.println(successIndicator);

            int len = Integer.parseInt(in.readLine());
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
            scroll.setBounds(300, 200, 600, 400);

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
    private void runProgress(String successIndicator, long fileSize, File directory, Utility.ProgressDialog progress) throws IOException {


        byte[] data = new byte[4096];

        if (successIndicator.equals("Success")) {
            File f = new File(directory, name);



            FileOutputStream fileOut = new FileOutputStream(f);
            DataOutputStream dataOut = new DataOutputStream(fileOut);

            System.out.println("File to save directory = " + f);
            long totalDown = 0;
            long remaining = fileSize;
            System.out.println("File size = " + fileSize);
            int cnt;

            while ((cnt = inputData.read(data, 0, (int)Math.min((long)data.length, remaining))) > 0) {


                System.out.println("Cnt = " + cnt);
                totalDown += cnt;
                remaining -= cnt;
                final int percent = (int) (totalDown * 100 / fileSize);

                System.out.println("Total = " + totalDown);
                System.out.println("Remain = " + remaining);
                System.out.println("Percent = " + percent);

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

            System.out.println("client directory before updating = " + clientDirectory);
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
                        bufDir = fileChooser.getSelectedFile();//fileChooser.getCurrentDirectory();
                        System.out.println("file chooser curr dir after choosing directory = " + fileChooser.getCurrentDirectory());
                        System.out.println("file chooser select file after choosing directory = " + fileChooser.getSelectedFile());
                        System.out.println("Buf Dir after choosing directory = " + bufDir);
                        clientDirectory = bufDir;
                    }

                final File directory = bufDir;

                if (!directory.exists())
                    directory.mkdir();

                System.out.println("directory after choosing directory = " + directory);

                name = fileNameText.getText();
                String file = "*" + name + "*";

                inputData.skipBytes(inputData.available());
                pw.println(file);


                String s = in.readLine();
                long fileSize = Long.parseLong(in.readLine());

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