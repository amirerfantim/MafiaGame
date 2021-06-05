package Client;



import Server.ChatMessage;

import java.net.*;
import java.io.*;
import java.util.*;


//The Client that can be run as a console
public class Client  {

    // notification
    private String notification = " *** ";

    // for I/O
    private ObjectInputStream sInput;		// to read from the socket
    private ObjectOutputStream sOutput;		// to write on the socket
    private Socket socket;					// socket object

    private String server, username;	// server and username
    private int port;					//port

    /*
     *  Constructor to set below things
     *  server: the server address
     *  port: the port number
     *  username: the username
     */

    Client(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

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
            sInput  = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
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
            sOutput.writeObject(username);

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

    /*
     * To send a message to the server
     */
    void sendMessage(ChatMessage chatMessage) {
        try {
            sOutput.writeObject(chatMessage);
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
        catch(Exception e) {}
        try {
            if(sOutput != null) sOutput.close();
        }
        catch(Exception e) {}
        try{
            if(socket != null) socket.close();
        }
        catch(Exception e) {}

    }
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
        int portNumber = 8888;
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


            if(message.equalsIgnoreCase("!LOGOUT")) {
                client.sendMessage(new ChatMessage(ChatMessage.LOGOUT, ""));
                break;
            }
            // message to check who are present in chatroom
            else if(message.equalsIgnoreCase("!WHOISIN")) {
                client.sendMessage(new ChatMessage(ChatMessage.WHOISIN, ""));
            }
            else if(message.equalsIgnoreCase("!READY") || message.equalsIgnoreCase("!GO")){
                client.sendMessage(new ChatMessage(ChatMessage.READY, ""));
            }
            // regular text message
            else {
                client.sendMessage(new ChatMessage(ChatMessage.MESSAGE, message));
            }

        }

        // close resource
        scanner.close();
        // client completed its job. disconnect client.
        client.disconnect();
    }

    /*
     * a class that waits for the message from the server
     */
    class ListenFromServer extends Thread {

        public void run() {
            while(true) {
                try {
                    // read the message form the input datastream
                    String message = (String) sInput.readObject();
                    // print the message
                    System.out.println(message);
                    /*
                    if(message.equals("choose another username: ")) {
                        Scanner scanner = new Scanner(System.in);
                        username = scanner.nextLine();
                        ChatMessage chatMessage = new ChatMessage(ChatMessage.MESSAGE, username );
                        sendMessage(chatMessage);
                    }

                     */

                    System.out.print("> ");
                }
                catch(IOException e) {
                    display(notification + "Server has closed the connection: " + e + notification);
                    break;
                }
                catch(ClassNotFoundException e2) {
                }
            }
        }
    }
}
