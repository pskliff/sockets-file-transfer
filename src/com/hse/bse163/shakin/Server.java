package com.hse.bse163.shakin;

import java.io.*;
import java.net.*;
import java.util.*;



public class Server {

    /**
     * @param args first argument — directory path (if no directory path — throws error)
     *             second argument — port number (default 8888)
     */
    public static void main(String args[]) throws Exception {
        String serverDirectory;
        if (args.length == 0) {
            System.out.println("Enter directory as the first command line argument and port as the second (Default port: 8888)");
            return;
        } else {

            ServerSocket listenerSocket;
            serverDirectory = args[0];
            System.out.println("Server has started");
            System.out.println("Waiting for clients");



            if (args.length >= 2)
                listenerSocket = new ServerSocket(Integer.parseInt(args[1]));
            else
                listenerSocket = new ServerSocket(8888);

            try {
                int id = 1;
                while (true) {
                    Socket clientSocket = listenerSocket.accept();

                    System.out.println("Client with ID " + id + " connected from " + clientSocket.getInetAddress().getHostName());
                    Thread server = new Handler(clientSocket, serverDirectory);
                    id++;
                    server.start();
                }
            } catch (Exception ex) {
                try {
                    listenerSocket.close();
                } catch (IOException e) {
                    System.out.println("SocketClose: " + e.getMessage());
                }
            }


        }
    }
}


class Handler extends Thread {

    private Socket clientSocket;
    private File serverDirectory;


    public Handler(Socket socket, String directory) {
        clientSocket = socket; //directory.charAt(directory.length() - 1) == File.separatorChar ? directory : directory + File.separator
//        File someDirectory = new File(directory);
//        System.out.println("Started ServerDirectory = " + someDirectory.getAbsoluteFile());

        serverDirectory = new File(directory);
        System.out.println("Next Started ServerDirectory = " + serverDirectory);
    }


    /**
     * Works with client (Allows to download files)
     */
    public void run() {
        try {
            InputStream clientInpStream = clientSocket.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(clientInpStream));
            DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream());
            PrintWriter clientPW = new PrintWriter(output, true);

            clientPW.println("Connection Established");

            File severDirFiles = serverDirectory;

            ArrayList<String> fileNames = new ArrayList<>(Arrays.asList(severDirFiles.list()));
            int filesNumber = fileNames.size();
            clientPW.println(String.valueOf(filesNumber));

            for (String name : fileNames)
                clientPW.println(name);

            while (true) {
                String name = in.readLine();
                char ch = name.charAt(0);

                long fileLength = 0;

                if (ch == '*') {
                    int n = name.length() - 1;
                    String fileName = name.substring(1, n);

                    FileInputStream fileReader;
                    BufferedInputStream bufFileReader = null;

                    boolean fileExists = true;
                    System.out.println("Request to download file " + fileName + " received from Client: " + clientSocket.getInetAddress().getHostName());
                    fileName = serverDirectory + File.separator + fileName;

                    File fileToDown = new File(fileName);

                    System.out.println("File to download full name = " + fileName + "; fileToString = " + fileToDown.toString());

                    try {
                        fileLength = fileToDown.length();
                        fileReader = new FileInputStream(fileName);
                        bufFileReader = new BufferedInputStream(fileReader);
                    } catch (FileNotFoundException e) {
                        fileExists = false;
                        System.out.println("FileNotFoundException:" + e.getMessage());
                    }
                    if (fileExists) {
                        clientPW.println("Success");
                        clientPW.println(fileLength);
                        System.out.println("Download begins");

                        Utility.sendBytes(bufFileReader, output);
                        System.out.println("Completed");

                    } else
                        clientPW.print("FileNotFound");
                }
            }


        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e) {
                System.out.println("SocketClose: " + e.getMessage());
            }
        }
    }


}



//        int size = 9022386;
//        byte[] data = new byte[fileLength];
//        int bytes = 0;
//        int cnt = in.read(data, 0, data.length);
//        out.write(data, 0, fileLength);
//        out.flush();


//else{
//        try {
//        boolean isEnded = true;
//        System.out.println("Request to upload file " + name + " received from " + clientSocket.getInetAddress().getHostName() + "...");
//
//        File directory = new File(serverDirectory);
//        if (!directory.exists()) {
//        System.out.println("Directory made");
//        directory.mkdir();
//        }
//
//        fileLength =  in.read();
////                    int size = 9022386;
//        byte[] data = new byte[fileLength];
//        File fileToUp = new File(directory, name);
//        FileOutputStream fileOut = new FileOutputStream(fileToUp);
//        DataOutputStream dataOut = new DataOutputStream(fileOut);
//
//        while (isEnded) {
//        int buf;
//        buf = clientInpStream.read(data, 0, data.length);
//        if (buf == -1) {
//        isEnded = false;
//        System.out.println("Completed");
//        } else {
//        dataOut.write(data, 0, buf);
//        dataOut.flush();
//        }
//        }
//        fileOut.close();
//        } catch (Exception exc) {
//        System.out.println(exc.getMessage());
//        }
//        }
