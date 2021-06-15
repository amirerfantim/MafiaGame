package Client;

import java.net.*;
import java.io.*;
import java.util.*;


/**
 * The type Client.
 * client side of the multi-threading application
 */
//The Client that can be run as a console
public class Client  {

    // notification
    private final String notification = " *** ";

    // for I/O
    private DataInputStream sInput;		// to read from the socket
    private DataOutputStream sOutput;		// to write on the socket
    private Socket socket;					// socket object

    private String server, username;	// server and username
    private int port;					//port

    /*
     *  Constructor to set below things
     *  server: the server address
     *  port: the port number
     *  username: the username
     */

    /**
     * Instantiates a new Client.
     *
     * @param server   the server
     * @param port     the port
     * @param username the username
     */
    Client(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

    /**
     * Start boolean.
     *
     * @return the boolean
     */
    /*
     * To start the chat
     */
    public boolean start() {
        // try to connect to the server
        try {
            socket = new Socket(server, port);
        }
        // exception handler if it failed
        catch(Exception ec) {
            display("Error connecting to server:" + ec);
            return false;
        }

        String msg = "Connection accepted " + socket.getInetAddress() + ":" + socket.getPort()
                + "\n please wait until start...";
        display(msg);

        /* Creating both Data Stream */
        try
        {
            sInput  = new DataInputStream(socket.getInputStream());
            sOutput = new DataOutputStream(socket.getOutputStream());
        }
        catch (IOException eIO) {
            display("Exception creating new Input/output Streams: " + eIO);
            return false;
        }

        // creates the Thread to listen from the server
        new ListenFromServer().start();
        // Send our username to the server this is the only message that we
        // will send as a String. All other messages will be ChatMessage objects
        try
        {
            sOutput.writeUTF(username);

        }
        catch (IOException eIO) {
            display("Exception doing login : " + eIO);
            disconnect();
            return false;
        }
        // success we inform the caller that it worked
        return true;
    }

    /*
     * To send a message to the console
     */
    private void display(String msg) {

        System.out.println(msg);

    }

    /**
     * Send message to server
     *
     * @param message the message
     */
    /*
     * To send a message to the server
     */
    void sendMessage(String message) {
        try {
            sOutput.writeUTF(message);
        }
        catch(IOException e) {
            display("Exception writing to server: " + e);
        }
    }

    /*
     * When something goes wrong
     * Close the Input/Output streams and disconnect
     */
    private void disconnect() {
        try {
            if(sInput != null) sInput.close();
        }
        catch(Exception e) {
            System.out.println("Error closing data input stream");
        }
        try {
            if(sOutput != null) sOutput.close();
        }
        catch(Exception e) {
            System.out.println("Error closing data output stream");
        }
        try{
            if(socket != null) socket.close();
        }
        catch(Exception e) {
            System.out.println("Error closing socket");
        }

    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    /*
     * To start the Client in console mode use one of the following command
     * > java Client
     * > java Client username
     * > java Client username portNumber
     * > java Client username portNumber serverAddress
     * at the console prompt
     * If the portNumber is not specified 1500 is used
     * If the serverAddress is not specified "localHost" is used
     * If the username is not specified "Anonymous" is used
     */
    public static void main(String[] args) {
        // default values if not entered
        int portNumber;
        String serverAddress = "localhost";
        String userName ;
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the username: ");
        userName = scanner.nextLine();

        System.out.print("Enter the port number: ");
        portNumber = Integer.parseInt(scanner.nextLine());

        Client client = new Client(serverAddress, portNumber, userName);
        // try to connect to the server and return if not connected
        if(!client.start())
            return;

        // infinite loop to get the input from the user
        while(true) {

            System.out.print("> ");
            // read message from user
            String message = scanner.nextLine();

            client.sendMessage(message);

            if(message.equalsIgnoreCase("!LOGOUT")){
                break;
            }

        }

        // close resource
        scanner.close();
        // client completed its job. disconnect client.
        client.disconnect();
    }

    /**
     * The type Listen from server.
     * this listen from server all-the-time
     */
    /*
     * a class that waits for the message from the server
     */
    class ListenFromServer extends Thread {

        public void run() {
            while(true) {
                try {
                    // read the message form the input dataStream
                    String message = sInput.readUTF();
                    // print the message
                    System.out.println(message);
/*
                    if(message.equals("choose another username: ")) {
                        Scanner scanner = new Scanner(System.in);
                        username = scanner.nextLine();
                        sendMessage(username);
                    }

 */

                    System.out.print("> ");
                }
                catch(IOException e) {
                    display(notification + "Server has closed the connection: " + e + notification);
                    break;
                }
            }
        }
    }
}