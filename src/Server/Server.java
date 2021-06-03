package Server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;

// the server that can be run as a console
public class Server {
    // a unique ID for each connection
    private static int uniqueId;
    // an ArrayList to keep the list of the Client
    private ArrayList<ClientThread> clientThreads;
    // to display time
    private SimpleDateFormat simpleDateFormat;
    // the port number to listen for connection
    private int port;
    // to check if server is running
    private boolean keepGoing;
    private int maxCapacity = 3;
    // notification
    private String notification = " *** ";

    //constructor that receive the port to listen to for connection as parameter

    public Server(int port) {
        // the port
        this.port = port;
        // to display hh:mm:ss
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        // an ArrayList to keep the list of the Client
        clientThreads = new ArrayList<ClientThread>();
    }

    public void start() {
        keepGoing = true;

        //create socket server and wait for connection requests
        try
        {
            // the socket used by the server
            ServerSocket serverSocket = new ServerSocket(port);

            // infinite loop to wait for connections ( till server is active )
            for(int i = 0 ; i < maxCapacity; i++){
                display("Server waiting for Clients on port " + port + ".");

                // accept connection if requested from client
                Socket socket = serverSocket.accept();
                // break if server stoped
                if(!keepGoing)
                    break;
                // if client is connected, create its thread
                ClientThread t = new ClientThread(this, socket);
                //add this client to arraylist
                clientThreads.add(t);

                //t.start();
            }

            broadcast("Server is full -> Let's go");

            GameManager gameManager = new GameManager(maxCapacity, clientThreads, this);
            gameManager.game();
            /*
            for (ClientThread clientThread : clientThreads) {
                clientThread.start();
            }

             */

            // try to stop the server
            if(!keepGoing) {
                try {
                    serverSocket.close();
                    for (ClientThread tc : clientThreads) {
                        try {
                            // close all data streams and socket
                            tc.sInput.close();
                            tc.sOutput.close();
                            tc.socket.close();
                        } catch (IOException ioE) {
                        }
                    }
                } catch (Exception e) {
                    display("Exception closing the server and clients: " + e);
                }
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
        String time = simpleDateFormat.format(new Date()) + " | " + msg;
        System.out.println(time);
    }

    // to broadcast a message to all Clients
    public synchronized boolean broadcast(String message) {
        // add timestamp to the message
        String time = simpleDateFormat.format(new Date());

        String messageLf = time + " | " + message + "\n";
        // display message
        System.out.print(messageLf);

        // we loop in reverse order in case we would have to remove a Client
        // because it has disconnected
        for(int i = clientThreads.size(); --i >= 0;) {
            ClientThread ct = clientThreads.get(i);
            // try to write to the Client if it fails remove it from the list
            if(!ct.writeMsg(messageLf)) {
                clientThreads.remove(i);
                display("Disconnected Client " + ct.username + " removed from list.");
            }
        }

    return true;


}

    // if client sent LOGOUT message to exit
    synchronized void remove(int id) {

        String disconnectedClient = "";
        // scan the array list until we found the Id
        for(int i = 0; i < clientThreads.size(); ++i) {
            ClientThread ct = clientThreads.get(i);
            // if found remove it
            if(ct.id == id) {
                disconnectedClient = ct.getUsername();
                clientThreads.remove(i);
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
        int portNumber = 8686;
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
        private final Server server;
        // the socket to get messages from client
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        // my unique id (easier for deconnection)
        int id;
        // the Username of the Client
        String username;
        // message object to recieve message and its type
        ChatMessage cm;
        // timestamp
        String date;

        // Constructor
        ClientThread(Server server, Socket socket) {
            this.server = server;
            // a unique id
            id = ++uniqueId;
            this.socket = socket;
            //Creating both Data Stream
            System.out.println("Thread trying to create Object Input/Output Streams");
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                // read the username
                username = (String) sInput.readObject();
                server.broadcast(server.notification + username + " has joined the chat room." + server.notification);
            } catch (IOException e) {
                server.display("Exception creating new Input/output Streams: " + e);
                return;
            } catch (ClassNotFoundException e) {
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
        public synchronized void run() {

            boolean keepGoing = true;
            int dayTime = 60;
            long start = System.currentTimeMillis();
            long end = start + dayTime*1000;

            while (keepGoing) {


                // read a String (which is an object)
                try {
                    cm = (ChatMessage) sInput.readObject();

                    if(System.currentTimeMillis() > end){
                        synchronized (this){
                            try {
                                wait();
                                start = System.currentTimeMillis();
                                end = start + dayTime*1000;
                                continue;
                            } catch (InterruptedException e) {
                                System.out.println("Error in waiting");
                                e.printStackTrace();
                            }
                        }
                    }

                } catch (IOException e) {
                    server.display(username + " Exception reading Streams: " + e);
                    break;
                } catch (ClassNotFoundException e2) {
                    break;
                }
                // get the message from the ChatMessage object received
                String message = cm.getMessage();

                // different actions based on type message
                switch (cm.getType()) {

                    case ChatMessage.MESSAGE:
                        boolean confirmation = server.broadcast(username + ": " + message);
                        if (!confirmation) {
                            String msg = server.notification + "Sorry. No such user exists." + server.notification;
                            writeMsg(msg);
                        }
                        break;
                    case ChatMessage.LOGOUT:
                        server.display(username + " disconnected with a LOGOUT message.");
                        keepGoing = false;
                        break;
                    case ChatMessage.WHOISIN:
                        writeMsg("List of the users connected at " + server.simpleDateFormat.format(new Date()) + "\n");
                        // send list of active clients
                        for (int i = 0; i < server.clientThreads.size(); ++i) {
                            Server.ClientThread ct = server.clientThreads.get(i);
                            writeMsg((i + 1) + ") " + ct.username + " since " + ct.date);
                        }
                        break;
                }
            }
            // if out of the loop then disconnected and remove from client list
            server.remove(id);
            close();
        }

        // close everything
        private void close() {
            try {
                if (sOutput != null) sOutput.close();
            } catch (Exception e) {
            }
            try {
                if (sInput != null) sInput.close();
            } catch (Exception e) {
            }
            ;
            try {
                if (socket != null) socket.close();
            } catch (Exception e) {
            }
        }

        // write a String to the Client output stream
        private boolean writeMsg(String msg) {
            // if Client is still connected send the message to it
            if (!socket.isConnected()) {
                close();
                return false;
            }
            // write the message to the stream
            try {
                sOutput.writeObject(msg);
            }
            // if an error occurs, do not abort just inform the user
            catch (IOException e) {
                server.display(server.notification + "Error sending message to " + username + server.notification);
                server.display(e.toString());
            }
            return true;
        }
    }
}

