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
        // TODO: if no argument — default path is current dir
        if(args.length == 0) {
            System.out.println("Please enter the server directory address as first argument while running from command line.");
        }
        else {

            serverDirectory = args[0];
            System.out.println("Server has started");
            System.out.println("Waiting for clients");

            ServerSocket listenerSocket;

            if(args.length >= 2){
                listenerSocket = new ServerSocket(Integer.parseInt(args[1]));
            }
            else{
                listenerSocket = new ServerSocket(8888);
            }


            int id = 1;
            while (true) {
                Socket clientSocket = listenerSocket.accept();

                //TODO: make logging
                System.out.println("Client with ID " + id + " connected from " + clientSocket.getInetAddress().getHostName() + "...");
                Thread server = new Handler(clientSocket, id, serverDirectory);
                id++;
                server.start();
            }
        }
    }
}

class Handler extends Thread {
//    int n;

    String name, f, fileData;

    Socket clientSocket;
    int counter;
    String serverDirectory;

    public Handler(Socket socket, int cnt, String directory) {
        clientSocket = socket;
        counter = cnt;
        serverDirectory = directory.charAt(directory.length() - 1) == '/' ? directory : directory + "/";
    }

    public void run() {
        try {
            InputStream clientInpStream = clientSocket.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(clientInpStream));
            OutputStream output = clientSocket.getOutputStream();
            PrintWriter clientPW = new PrintWriter(output, true);

            ObjectOutputStream objectOutput = new ObjectOutputStream(output);
//            objectOutput.writeObject("Connection Established");
            clientPW.println("Connection Established");

            File severDirFiles = new File(serverDirectory);

            ArrayList<String> fileNames = new ArrayList<>(Arrays.asList(severDirFiles.list()));
            int filesNumber = fileNames.size();
//            objectOutput.writeObject(String.valueOf(filesNumber));
            clientPW.println(String.valueOf(filesNumber));

            for(String name: fileNames) {
//                objectOutput.writeObject(name);
                clientPW.println(name);
            }

            name = in.readLine();
            char ch = name.charAt(0);//name.substring(0, 1);


            int fileLength = 0;

            if (ch == '*') {
                int n = name.length() - 1;
                String fileName = name.substring(1, n);

                FileInputStream fileReader = null;
                BufferedInputStream bufFileReader = null;

                boolean fileExists = true;
                System.out.println("Request to download file " + fileName + " received from " + clientSocket.getInetAddress().getHostName() + "...");
                fileName = serverDirectory + fileName;
                //System.out.println(fileName);

                File fileToDown = new File(fileName);


                try {
                    fileLength =  (int)fileToDown.length();
                    fileReader = new FileInputStream(fileName);
                    bufFileReader = new BufferedInputStream(fileReader);
                }
                catch (FileNotFoundException e) {
                    fileExists = false;
                    System.out.println("FileNotFoundException:" + e.getMessage());
                }
                if (fileExists) {
//                    objectOutput = new ObjectOutputStream(output);
//                    objectOutput.writeObject("Success");
                    clientPW.println("Success");
                    clientPW.println(fileLength);
                    System.out.println("Download begins");

                    sendBytes(bufFileReader, output, fileLength);
                    System.out.println("Completed");

                    bufFileReader.close();
                    fileReader.close();
//                    objectOutput.close();
                    output.close();
                }
                else {
//                    objectOutput = new ObjectOutputStream(output);
//                    objectOutput.writeObject("FileNotFound");
                    clientPW.print("FileNotFound");
                    bufFileReader.close();
                    fileReader.close();
//                    objectOutput.close();
                    output.close();
                }
            }
            else{
                try {
                    boolean isEnded = true;
                    System.out.println("Request to upload file " + name + " received from " + clientSocket.getInetAddress().getHostName() + "...");

                    File directory = new File(serverDirectory);
                    if (!directory.exists()) {
                        System.out.println("Directory made");
                        directory.mkdir();
                    }

                    fileLength =  in.read();
//                    int size = 9022386;
                    byte[] data = new byte[fileLength];
                    File fileToUp = new File(directory, name);
                    FileOutputStream fileOut = new FileOutputStream(fileToUp);
                    DataOutputStream dataOut = new DataOutputStream(fileOut);

                    while (isEnded) {
                        int buf;
                        buf = clientInpStream.read(data, 0, data.length);
                        if (buf == -1) {
                            isEnded = false;
                            System.out.println("Completed");
                        } else {
                            dataOut.write(data, 0, buf);
                            dataOut.flush();
                        }
                    }
                    fileOut.close();
                } catch (Exception exc) {
                    System.out.println(exc.getMessage());
                }
            }
        }
        catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private static void sendBytes(BufferedInputStream in , OutputStream out, int fileLength) throws Exception {
//        int size = 9022386;
        byte[] data = new byte[fileLength];
        int bytes = 0;
        int c = in.read(data, 0, data.length);
        out.write(data, 0, c);
        out.flush();
    }
}

