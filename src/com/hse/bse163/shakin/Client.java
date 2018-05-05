package com.hse.bse163.shakin;

import java.io.*;
import java.net.*;
import java.lang.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

class Client extends JFrame implements ActionListener, MouseListener {
    JPanel panel;
    JLabel title, subT, msg, error, servFiles;
    Font font, labelfont;
    JTextField txt;
    JButton up, down, folderLocation;
    String dirName;
    Socket clientSocket;
    InputStream inFromServer;
    OutputStream outToServer;
    BufferedInputStream bis;
    PrintWriter pw;
    DataInputStream inputData;


    String name, file, path;
    String hostAddr;
    int portNumber;
    int c;
    int size = 9022386;
    JList<String> filelist;
    HashSet<String> names;
    HashSet<String> clientFileNames;
    int len; // number of files on the server retrieved


    BufferedReader in;


    public Client(String dir, String host, int port) {
        super("Torrent");

        // set dirName to the one that's entered by the user
        dirName = dir.charAt(dir.length() - 1) == '/' ? dir : dir + "/";

        // set hostAddr to the one that's passed by the user
        hostAddr = host;

        // set portNumber to the one that's passed by the user
        portNumber = port;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        panel = new JPanel(null);

        font = new Font("Roboto", Font.BOLD, 60);
        title = new JLabel("Torrent");
        title.setFont(font);
        title.setBounds(300, 50, 400, 50);
        panel.add(title);

        folderLocation = new JButton("Show client files");
        folderLocation.setBounds(550, 130, 150, 40);
        panel.add(folderLocation);



        labelfont = new Font("Roboto", Font.PLAIN, 20);
        subT = new JLabel("Enter File Name :");
        subT.setFont(labelfont);
        subT.setBounds(100, 450, 200, 50);
        panel.add(subT);



        txt = new JTextField();
        txt.setBounds(400, 450, 500, 50);
        panel.add(txt);

        up = new JButton("Upload");
        up.setBounds(250, 550, 200, 50);
        panel.add(up);

        down = new JButton("Download");
        down.setBounds(550, 550, 200, 50);
        panel.add(down);

        error = new JLabel("");
        error.setFont(labelfont);
        error.setBounds(200, 650, 600, 50);
        panel.add(error);

        up.addActionListener(this);
        down.addActionListener(this);
        folderLocation.addActionListener(this);

        try {
            clientSocket = new Socket(hostAddr, portNumber);
            inFromServer = clientSocket.getInputStream();
            pw = new PrintWriter(clientSocket.getOutputStream(), true);
            outToServer = clientSocket.getOutputStream();
            ObjectInputStream oin = new ObjectInputStream(inFromServer);
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

            servFiles = new JLabel("Files in the Server Directory :");
            servFiles.setBounds(350, 125, 400, 50);
            panel.add(servFiles);

            filelist = new JList<>(temp_names);
            JScrollPane scroll = new JScrollPane(filelist);
            scroll.setBounds(300, 200, 400, 200);

            panel.add(scroll);
            filelist.addMouseListener(this);

            File clientFiles = new File(dirName);
            clientFileNames = new HashSet<>(Arrays.asList(clientFiles.list()));

        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            error.setText("Exception:" + e.getMessage());
            error.setBounds(300, 125, 600, 50);
            panel.revalidate();
        }

        getContentPane().add(panel);
    }


    public void mouseClicked(MouseEvent click) {
        if (click.getClickCount() == 2) {
            String selectedItem = filelist.getSelectedValue();
            txt.setText(selectedItem);
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
            servFiles.setText("Files in the Client Directory :");
        } else {
            temp_names = names.toArray(new String[names.size()]);
            folderLocation.setText("Show client files");
            servFiles.setText("Files in the Server Directory :");
        }

        Arrays.sort(temp_names);
        filelist.setListData(temp_names);
    }

    public void actionPerformed(ActionEvent event) {
        if (event.getSource() == folderLocation) {
            updateFileList("Show server files".equals(folderLocation.getText()));

        } else if (event.getSource() == up) {
            try {
                name = txt.getText();

                FileInputStream file = null;
                BufferedInputStream bis = null;

                boolean fileExists = true;
                path = dirName + name;

                File fileToUpload = new File(path);
                int fileLength = 0;

                try {
                    fileLength = (int) fileToUpload.length();
                    file = new FileInputStream(path);
                    bis = new BufferedInputStream(file);
                } catch (FileNotFoundException e) {
                    fileExists = false;
                    System.out.println("FileNotFoundException:" + e.getMessage());
                    error.setText("FileNotFoundException:" + e.getMessage());
                    panel.revalidate();
                }

                if (fileExists) {
                    // send file name to server
                    pw.println(name);
                    pw.println(fileLength);

                    System.out.println("Upload begins");
                    error.setText("Upload begins");
                    panel.revalidate();

                    // send file data to server
                    sendBytes(bis, outToServer, fileLength);

                    System.out.println("Completed");
                    error.setText("Completed");
                    panel.revalidate();

                    boolean exists = names.contains(name);

                    if (!exists) {
                        names.add(name);
                        len++;
                    }

//                    String[] temp_names = (String[])names.toArray();
//                    for (int i = 0; i < len; i++) {
//                        temp_names[i] = names[i];
//                    }

                    // sort the array of strings that's going to get displayed in the scrollpane
//                    Arrays.sort(temp_names);

                    // update the contents of the list in scroll pane
//                    filelist.setListData(temp_names);

                    updateFileList(true);

                    // close all file buffers
                    bis.close();
                    file.close();
                    outToServer.close();
                }
            } catch (Exception e) {
                System.out.println("Exception: " + e.getMessage());
                error.setText("Exception:" + e.getMessage());
                panel.revalidate();
            }
        } else if (event.getSource() == down) {
            try {
                File directory = new File(dirName);

                if (!directory.exists()) {
                    directory.mkdir();
                }
                boolean complete = true;

                name = txt.getText();
                file = new String("*" + name + "*");
                pw.println(file); //lets the server know which file is to be downloaded

//                ObjectInputStream oin = new ObjectInputStream(inFromServer);
                String s = in.readLine();//(String) oin.readObject();
                int fileSize = Integer.parseInt(in.readLine());

                byte[] data = new byte[fileSize];

                if (s.equals("Success")) {
                    File f = new File(directory, name);
                    FileOutputStream fileOut = new FileOutputStream(f);
                    DataOutputStream dataOut = new DataOutputStream(fileOut);

                    //empty file case
//                    while (complete) {
                        c = inputData.read(data, 0, data.length); //data.length
//                        if (c <= 0) {
                            complete = false;
                            System.out.println("Completed");
                            error.setText("Completed");
                            panel.revalidate();

//                        } else {
                            dataOut.write(data, 0, c);
                            dataOut.flush();
//                        }
//                    }

                    updateFileList(false);
                    fileOut.close();
                } else {
                    System.out.println("Requested file not found on the server.");
                    error.setText("Requested file not found on the server.");
                    panel.revalidate();
                }
            } catch (Exception exc) {
                System.out.println("Exception: " + exc.getMessage());
                error.setText("Exception:" + exc.getMessage());
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
        // If directory path is not present, show error
        if (args.length >= 3) {
            Client tcp = new Client(args[0], args[1], Integer.parseInt(args[2]));
            tcp.setSize(1000, 900);
            tcp.setVisible(true);
        } else if (args.length == 2) {
            Client tcp = new Client(args[0], args[1], 8888);
            tcp.setSize(1000, 900);
            tcp.setVisible(true);
        } else if (args.length == 1) {
            Client tcp = new Client(args[0], "localhost", 8888);
            tcp.setSize(1000, 900);
            tcp.setVisible(true);
        } else {
            System.out.println("Please enter the client directory address as first argument while running from command line.");
        }
    }
}
