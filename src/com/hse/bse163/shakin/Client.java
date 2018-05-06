package com.hse.bse163.shakin;

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
    private JButton uploadButton, downloadButton, folderLocation;
    private String clientDirectory;
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
    private int len; // number of files on the server retrieved


    private BufferedReader in;




    public Client() {
        super("Simple Torrent");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    private int getHostPort(StringBuilder host) {
        JTextField hostField = new JTextField(15);
        JTextField portField = new JTextField(15);

        JPanel hpPanel = new JPanel();
        JLabel hostLabel = new JLabel("Host:");
//        hostLabel.setBounds(10, 10, 50, 50);
        hpPanel.add(hostLabel);
        hpPanel.add(hostField);
        hpPanel.add(Box.createHorizontalStrut(50));
        hpPanel.add(new JLabel("Port:"));
        hpPanel.add(portField);

        boolean ok = false;
        String message = "Please Enter Host and Port";
        while(!ok)
        {
            int result = JOptionPane.showConfirmDialog(null, hpPanel,
                    message, JOptionPane.OK_CANCEL_OPTION);
            ok = result == JOptionPane.OK_OPTION;
            try
            {
                Integer.parseInt(portField.getText());
            }
            catch (NumberFormatException e)
            {
                ok = false;
                message = "Please Enter Host and Port ( PORT MUST BE INT )";
            }
        }

        host.append( hostField.getText());
        return Integer.parseInt(portField.getText());
    }

    private void run(String dir, String host, int port)
    {
        // set clientDirectory to the one that's entered by the user
        clientDirectory = dir.charAt(dir.length() - 1) == '/' ? dir : dir + "/";

//        getHost();

//
        StringBuilder hostBuild = new StringBuilder();
        portNumber = getHostPort(hostBuild);
        hostAddr = hostBuild.toString();




        //         set hostAddr to the one that's passed by the user
//        hostAddr = host;

        // set portNumber to the one that's passed by the user
//        portNumber = port;



        panel = new JPanel(null);



        titleFont = new Font("Helvetica", Font.BOLD, 30);
        titleLabel = new JLabel("Simple Torrent");
        titleLabel.setFont(titleFont);
        titleLabel.setBounds(300, 50, 700, 40);
        panel.add(titleLabel);



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

        uploadButton = new JButton("Upload");
        uploadButton.setBounds(250, 550, 200, 50);
        panel.add(uploadButton);

        downloadButton = new JButton("Download");
        downloadButton.setBounds(550, 550, 200, 50);
        panel.add(downloadButton);

        errorLabel = new JLabel("");
        errorLabel.setFont(labelFont);
        errorLabel.setBounds(200, 650, 600, 50);
        panel.add(errorLabel);

        uploadButton.addActionListener(this);
        downloadButton.addActionListener(this);
        folderLocation.addActionListener(this);

        try {
            clientSocket = new Socket(hostAddr, portNumber);
            inFromServer = clientSocket.getInputStream();
            pw = new PrintWriter(clientSocket.getOutputStream(), true);
            outToServer = clientSocket.getOutputStream();
            in = new BufferedReader(new InputStreamReader(inFromServer));
            inputData = new DataInputStream(inFromServer);

            String s = in.readLine(); //(String) oin.readObject();
            System.out.println(s);

            len = Integer.parseInt(in.readLine());//Integer.parseInt((String) oin.readObject());
            System.out.println(len);

            String[] temp_names = new String[len];
            names = new HashSet<>();

            for (int i = 0; i < len; i++) {
                String filename = in.readLine();//(String) oin.readObject();
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

            File clientFiles = new File(clientDirectory);
            clientFileNames = new HashSet<>(Arrays.asList(clientFiles.list()));

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

    void updateFileList(boolean showServerFiles){
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
        JLabel askLabel = new JLabel("Do you want to download a file of size = " + fileSize + "?");

        hpPanel.add(askLabel);
        String message = "Do you??";
            int result = JOptionPane.showConfirmDialog(null, hpPanel,
                    message, JOptionPane.OK_CANCEL_OPTION);

            return result == JOptionPane.OK_OPTION;
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == folderLocation) {
            updateFileList("Show server files".equals(folderLocation.getText()));

        } else if (event.getSource() == downloadButton) {
            try {
                File directory = new File(clientDirectory);

                if (!directory.exists()) {
                    directory.mkdir();
                }
                boolean complete = true;

                name = fileNameText.getText();
                file = new String("*" + name + "*");

                inputData.skipBytes(inputData.available());
                pw.println(file); //lets the server know which file is to be downloaded

//                ObjectInputStream oin = new ObjectInputStream(inFromServer);
                String s = in.readLine();//(String) oin.readObject();
                int fileSize = Integer.parseInt(in.readLine());



                byte[] data = new byte[4096];

                if (s.equals("Success")) {
                    File f = new File(directory, name);
                    FileOutputStream fileOut = new FileOutputStream(f);
                    DataOutputStream dataOut = new DataOutputStream(fileOut);

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

                    int totalDown = 0;
                    int remaining = fileSize;
                    while ((c = inputData.read(data, 0, Math.min(data.length, remaining))) > 0)
                    {
                        totalDown += c;
                        remaining -= c;
                        dataOut.write(data, 0, c);
                    }

                    System.out.println("Completed");
                    errorLabel.setText("Completed");

                    if(!clientFileNames.contains(name))
                        clientFileNames.add(name);

                    updateFileList(false);
                    fileOut.close();
                } else {
                    System.out.println("Requested file not found on the server.");
                    errorLabel.setText("Requested file not found on the server.");
                    panel.revalidate();
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
