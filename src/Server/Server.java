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
    protected ArrayList<ClientThread> clientThreads;
    // to display time
    private SimpleDateFormat simpleDateFormat;
    // the port number to listen for connection
    private final int port;
    // to check if server is running
    private boolean keepGoing;
    private final int maxCapacity = 6;
    // notification
    private final String notification = " *** ";
    private final GameManager gameManager;
    private boolean waitingToGo = false;
    private ArrayList<ClientThread> activeClients;




    //constructor that receive the port to listen to for connection as parameter

    public Server(int port) {
        // the port
        this.port = port;
        // to display hh:mm:ss
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        // an ArrayList to keep the list of the Client
        clientThreads = new ArrayList<ClientThread>();
        gameManager = new GameManager(maxCapacity, clientThreads, this);
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public ArrayList<ClientThread> getClientThreads() {
        return clientThreads;
    }

    public ArrayList<ClientThread> getActiveClients() {
        return activeClients;
    }

    public void setActiveClients(ArrayList<ClientThread> activeClients) {
        this.activeClients = activeClients;
    }

    public void setWaitingToGo(boolean waitingToGo) {
        this.waitingToGo = waitingToGo;
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
                // break if server stopped
                if(!keepGoing)
                    break;
                // if client is connected, create its thread
                ClientThread t = new ClientThread(this, socket);
                //add this client to arraylist
                clientThreads.add(t);

                //t.start();
            }

            broadcast("Server is full -> Let's go", getClientThreads());

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
                        } catch (IOException e) {
                            e.printStackTrace();
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
    public synchronized boolean broadcast(String message, ArrayList<ClientThread> toSendMsg) {
        // add timestamp to the message
        String time = simpleDateFormat.format(new Date());

        String messageLf = time + " | " + message + "\n";
        // display message
        System.out.print(messageLf);

        // we loop in reverse order in case we would have to remove a Client
        // because it has disconnected
        for(int i = toSendMsg.size(); --i >= 0;) {
            ClientThread ct = toSendMsg.get(i);
            // try to write to the Client if it fails remove it from the list
            if(!ct.writeMsg(messageLf)) {
                clientThreads.remove(ct);
                display("Disconnected Client " + ct.username + " removed from list.");
            }
        }

        return true;

    }

    public synchronized boolean sendMsgToClient(String message, ClientThread clientThread){
        String time = simpleDateFormat.format(new Date());
        String messageLf = time + " | " + message + "\n";
        System.out.print(messageLf);

        if(!clientThread.writeMsg(messageLf)) {
            clientThreads.remove(clientThread);
            display("Disconnected Client " + clientThread.username + " removed from list.");
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
        broadcast(notification + disconnectedClient + " has left the chat room." + notification, clientThreads);
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
        DataInputStream sInput;
        DataOutputStream sOutput;
        // my unique id (easier for deconnection)
        int id;
        // the Username of the Client
        String username;
        // message object to recieve message and its type
        String message;
        // timestamp
        String date;
        private boolean isWait = false;

        // Constructor
        ClientThread(Server server, Socket socket) {
            this.server = server;
            // a unique id
            id = ++uniqueId;
            this.socket = socket;
            //Creating both Data Stream
            System.out.println("Thread trying to create Object Input/Output Streams");
            try {

                sOutput = new DataOutputStream(socket.getOutputStream());
                sInput = new DataInputStream(socket.getInputStream());
                // read the username
                username = (String) sInput.readUTF();

                for(ClientThread clientThread : clientThreads){
                    if(username.equals(clientThread.getUsername())){
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(username);
                        stringBuilder.append(uniqueId);
                        username = stringBuilder.toString();
                    }
                }

                /*
                for(int i = 0 ; i < clientThreads.size() ; i++){
                    if(username.equals(clientThreads.get(i).getUsername())){
                        writeMsg("choose another username: ");
                        cm =(ChatMessage) sInput.readObject();
                        username = cm.getMessage();
                        i = 0;
                    }
                }
                 */


                server.broadcast(notification + username + " has joined the chat room." + notification, getClientThreads());
            } catch (IOException e) {
                server.display("Exception creating new Input/output Streams: " + e);
                return;
            }
            date = new Date().toString() + "\n";
        }

        public boolean isWait() {
            return isWait;
        }

        public String getUsername() {
            return username;
        }

        public void setWait(boolean wait) {
            isWait = wait;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public synchronized void run() {

            boolean keepGoing = true;

            while (keepGoing) {

                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                int count = 0;

                try {

                    count = sInput.available();

                    if (count > 0) {
                        message = sInput.readUTF();
                    }

                    if (isWait) {
                        synchronized (this) {
                            try {
                                wait();

                            } catch (InterruptedException e) {
                                System.out.println("Error in waiting");
                                e.printStackTrace();
                            }
                        }
                    }

                } catch (IOException e) {
                    display(username + " Exception reading Streams: " + e);
                    e.printStackTrace();
                }

                if(count == 0){
                    continue;
                }

                if(message.equalsIgnoreCase("!LOGOUT")){
                    //display(username + " disconnected with a LOGOUT message.");
                    keepGoing = false;
                    break;
                }else if(message.equalsIgnoreCase("!WHOISIN")){
                    writeMsg("List of the users connected at " + simpleDateFormat.format(new Date()) + "\n");
                    // send list of active clients
                    for (int i = 0; i < clientThreads.size(); ++i) {
                        Server.ClientThread ct = clientThreads.get(i);
                        writeMsg((i + 1) + ") " + ct.username + " since " + ct.date);
                    }
                }

                if (!waitingToGo) {
                    if(message.charAt(0) == '@'){
                        Player curPlayer = gameManager.getPlayer(this);
                        //String[] decodedMsg = message.split(" ");
                        if( curPlayer instanceof GodFather){
                           gameManager.godFatherShot(message.substring(1));
                        }else if(curPlayer instanceof LectorDoctor){
                            gameManager.lectorHill(message.substring(1));
                        }

                    } else{
                        boolean confirmation = broadcast(username + ": " + message, activeClients);

                        if (!confirmation) {
                            String msg = notification + "Sorry. No such user exists." + notification;
                            writeMsg(msg);
                        }
                    }

                } else {
                     if (message.equalsIgnoreCase("!READY")) {
                        gameManager.ready();
                        writeMsg(gameManager.getReadyToGo() + " number of players are ready so far");
                    }
                }

            }
            // if out of the loop then disconnected and remove from client list
            remove(id);
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

        private int indexOfUsername(String userN){
            for(int i = 0; i < clientThreads.size(); i++){
                if(clientThreads.get(i).getUsername().equals(userN)){
                    return i;
                }
            }
            return -1;
        }

        public boolean writeMsg(String msg) {
            // if Client is still connected send the message to it
            if (!socket.isConnected()) {
                close();
                return false;
            }
            // write the message to the stream
            try {
                sOutput.writeUTF(msg);
            }
            // if an error occurs, do not abort just inform the user
            catch (IOException e) {
                display(notification + "Error sending message to " + username + notification);
                clientThreads.remove(this);
                broadcast("Disconnected Client " + username + " removed from list.", clientThreads);
                display(e.toString());
            }

            return true;
        }
    }


}


