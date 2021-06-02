package Server;


import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;

// the server that can be run as a console
public class Server {
    // an ArrayList to keep the list of the Client
    private ArrayList<ClientThread> players;
    // to display time
    private SimpleDateFormat simpleDateFormat;
    // the port number to listen for connection
    private int port;
    // to check if server is running
    private boolean keepGoing;

    private String message;
    // notification
    private String notification = " *** ";

    //constructor that receive the port to listen to for connection as parameter

    public Server(int port) {
        // the port
        this.port = port;
        // to display hh:mm:ss
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        // an ArrayList to keep the list of the Client
        players = new ArrayList<ClientThread>();
    }

    public void start() {
        keepGoing = true;
        //create socket server and wait for connection requests
        try
        {
            // the socket used by the server
            ServerSocket serverSocket = new ServerSocket(port);

            // infinite loop to wait for connections ( till server is active )
            while(keepGoing) {
                display("Server waiting for Clients on port " + port + ".");

                // accept connection if requested from client
                Socket socket = serverSocket.accept();
                // break if server stoped
                if(!keepGoing)
                    break;
                // if client is connected, create its thread
                ClientThread t = new ClientThread(socket);
                //add this client to arraylist
                players.add(t);

                t.start();
            }
            // try to stop the server
            try {
                serverSocket.close();
                for(int i = 0; i < players.size(); ++i) {
                    ClientThread tc = players.get(i);
                    try {
                        // close all data streams and socket
                        tc.sInput.close();
                        tc.sOutput.close();
                        tc.socket.close();
                    }
                    catch(IOException ioE) {
                    }
                }
            }
            catch(Exception e) {
                display("Exception closing the server and clients: " + e);
            }
        }
        catch (IOException e) {
            String msg = simpleDateFormat.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
            display(msg);
        }
    }

    // to stop the server
    protected void stop() {
        keepGoing = false;
        try {
            new Socket("localhost", port);
        }
        catch(Exception e) {
        }
    }

    // Display an event to the console
    private void display(String msg) {
        String time = simpleDateFormat.format(new Date()) + " " + msg;
        System.out.println(time);
    }

    // to broadcast a message to all Clients
    private synchronized boolean broadcast(String message) {
        // add timestamp to the message
        String time = simpleDateFormat.format(new Date()) , messageToSend = "";

        messageToSend = time + " | " + message + "\n";
        System.out.print(messageToSend);

        // we loop in reverse order in case we would have to remove a Client
        // because it has disconnected
        for(int i = players.size(); --i >= 0;) {
            ClientThread clientThread = players.get(i);
            // try to write to the Client if it fails remove it from the list
            if(!clientThread.writeMsg(messageToSend)) {
                players.remove(i);
                display("Disconnected Client " + clientThread.username + " removed from list.");
            }
        }

        return true;
    }

    // if client sent LOGOUT message to exit
    synchronized void remove(int id) {

        String disconnectedClient = "";
        // scan the array list until we found the Id
        for(int i = 0; i < players.size(); ++i) {
            ClientThread ct = players.get(i);
            // if found remove it
            if(ct.id == id) {
                disconnectedClient = ct.getUsername();
                players.remove(i);
                break;
            }
        }
        broadcast(notification + disconnectedClient + " has left the chat room." + notification);
    }

    /*
     *  To run as a console application
     * > java Server
     * > java Server portNumber
     * If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        // start server on port 1500 unless a PortNumber is specified
        int portNumber = 8888;
        switch(args.length) {
            case 1:
                try {
                    portNumber = Integer.parseInt(args[0]);
                }
                catch(Exception e) {
                    System.out.println("Invalid port number.");
                    System.out.println("Usage is: > java Server [portNumber]");
                    return;
                }
            case 0:
                break;
            default:
                System.out.println("Usage is: > java Server [portNumber]");
                return;

        }
        // create a server object and start it
        Server server = new Server(portNumber);
        server.start();
    }

    // One instance of this thread will run for each client
    class ClientThread extends Thread {
        // the socket to get messages from client
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        // my unique id (easier for deconnection)
        int id;
        // the Username of the Client
        String username;
        // message object to recieve message and its type
        // timestamp
        String date;

        // Constructor
        ClientThread(Socket socket) {
            // a unique id
            this.socket = socket;
            //Creating both Data Stream
            System.out.println("Thread trying to create Object Input/Output Streams");
            try
            {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput  = new ObjectInputStream(socket.getInputStream());
                // read the username
                username = (String) sInput.readObject();
                broadcast(notification + username + " has joined the chat room." + notification);
            }
            catch (IOException e) {
                display("Exception creating new Input/output Streams: " + e);
                return;
            }
            catch (ClassNotFoundException e) {
            }
            date = new Date().toString() + "\n";
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        // infinite loop to read and forward message
        public void run() {
            // to loop until LOGOUT
            boolean keepGoing = true;
            while(keepGoing) {
                // read a String (which is an object)
                try {
                    message = (String) sInput.readObject();
                }
                catch (IOException e) {
                    display(username + " Exception reading Streams: " + e);
                    break;
                }
                catch(ClassNotFoundException e2) {
                    break;
                }
                // get the message from the ChatMessage object received
                String message = Server.this.message;

                if(message == null){
                    continue;
                }

                boolean confirmation =  broadcast(username + ": " + message);
                if(!confirmation){
                    String msg = notification + "Sorry. No such user exists." + notification;
                    writeMsg(msg);
                }

                if(message.equals("logout")){
                    keepGoing = false;
                    break;
                }

            }
            // if out of the loop then disconnected and remove from client list
            remove(id);
            close();
        }

        // close everything
        private void close() {
            try {
                if(sOutput != null) sOutput.close();
            }
            catch(Exception e) {}
            try {
                if(sInput != null) sInput.close();
            }
            catch(Exception e) {};
            try {
                if(socket != null) socket.close();
            }
            catch (Exception e) {}
        }

        // write a String to the Client output stream
        private boolean writeMsg(String msg) {
            // if Client is still connected send the message to it
            if(!socket.isConnected()) {
                close();
                return false;
            }
            // write the message to the stream
            try {
                sOutput.writeObject(msg);
            }
            // if an error occurs, do not abort just inform the user
            catch(IOException e) {
                display(notification + "Error sending message to " + username + notification);
                display(e.toString());
            }
            return true;
        }
    }
}
