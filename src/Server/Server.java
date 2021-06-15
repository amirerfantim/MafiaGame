package Server;

import java.io.*;
import java.net.*;
import java.security.cert.CertificateNotYetValidException;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * The type Server.
 * server side of the multi-thread application
 */
// the server that can be run as a console
public class Server {
    // a unique ID for each connection
    private static int uniqueId;
    /**
     * The Client threads.
     */
// an ArrayList to keep the list of the Client
    protected ArrayList<ClientThread> clientThreads;
    // to display time
    private SimpleDateFormat simpleDateFormat;
    // the port number to listen for connection
    private final int port;
    // to check if server is running
    private boolean keepGoing;
    private int maxCapacity;
    // notification
    private final String notification = " *** ";
    private final GameManager gameManager;
    private boolean waitingToGo = false;
    private ArrayList<ClientThread> activeClients;

    /**
     * The constant RED.
     */
    public static final String	RED					= "\u001B[31m";
    /**
     * The constant BLUE.
     */
    public static final String	BLUE				= "\u001B[34m";
    /**
     * The constant CYAN.
     */
    public static final String	CYAN				= "\u001B[36m";
    /**
     * The constant RESET.
     */
    public static final String  RESET               = "\u001B[0m";

    //constructor that receive the port to listen to for connection as parameter

    /**
     * Instantiates a new Server.
     *
     * @param port        the port
     * @param maxCapacity the max capacity
     */
    public Server(int port, int maxCapacity) {
        // the port
        this.port = port;
        // to display hh:mm:ss
        simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        // an ArrayList to keep the list of the Client
        clientThreads = new ArrayList<>();
        this.maxCapacity = maxCapacity;
        gameManager = new GameManager(this.maxCapacity, clientThreads, this);
    }

    /**
     * Gets max capacity.
     *
     * @return the max capacity
     */
    public int getMaxCapacity() {
        return maxCapacity;
    }

    /**
     * Gets client threads.
     *
     * @return the client threads
     */
    public ArrayList<ClientThread> getClientThreads() {
        return clientThreads;
    }

    /**
     * Gets active clients.
     *
     * @return the active clients
     */
    public ArrayList<ClientThread> getActiveClients() {
        return activeClients;
    }

    /**
     * Sets active clients.
     *
     * @param activeClients the active clients
     */
    public void setActiveClients(ArrayList<ClientThread> activeClients) {
        this.activeClients = activeClients;
    }

    /**
     * Sets waiting to go.
     *
     * @param waitingToGo the waiting to go
     */
    public void setWaitingToGo(boolean waitingToGo) {
        this.waitingToGo = waitingToGo;
    }

    /**
     * Start the server
     */
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

            broadcast(CYAN + "Server is full -> Let's go" +RESET, getClientThreads());

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

    /**
     * Stop.
     */
// to stop the server
    protected void stop() {
        keepGoing = false;
        try {
            new Socket("localhost", port);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    // Display an event to the console
    private void display(String msg) {
        String time = simpleDateFormat.format(new Date()) + " | " + msg;
        System.out.println(time);
    }

    /**
     * Broadcast boolean.
     *
     * @param message   the message
     * @param toSendMsg the clients which get the message
     * @return the boolean if it was successful or not
     */
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

    /**
     * Send msg to client.
     *
     * @param message      the message
     * @param clientThread the client thread
     */
    public synchronized void sendMsgToClient(String message, ClientThread clientThread){
        String time = simpleDateFormat.format(new Date());
        String messageLf = time + " | " + message + "\n";
        System.out.print(messageLf);

        if(!clientThread.writeMsg(messageLf)) {
            clientThreads.remove(clientThread);
            display("Disconnected Client " + clientThread.username + " removed from list.");
        }

        //return true;
    }

    /**
     * Remove.
     * remove a ct from the clientThreads
     * @param id the id
     */
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

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    /*
     *  To run as a console application
     * > java Server
     * > java Server portNumber
     * If the port number is not specified 1500 is used
     *
     */
    public static void main(String[] args) {
        // start server on port 1500 unless a PortNumber is specified

        Scanner scanner = new Scanner(System.in);
        int portNumber, serverCapacity;

        System.out.print("enter max capacity of server: ");
        serverCapacity = scanner.nextInt();

        System.out.print("enter port number of server: ");
        portNumber = scanner.nextInt();


        // create a server object and start it
        Server server = new Server(portNumber, serverCapacity);
        server.start();
    }

    /**
     * The type Client thread.
     * this makes a thread for each client
     */
// One instance of this thread will run for each client
    class ClientThread extends Thread {
        /**
         * The Socket.
         */
// the socket to get messages from client
        Socket socket;
        /**
         * The S input stream.
         */
        DataInputStream sInput;
        /**
         * The S output stream.
         */
        DataOutputStream sOutput;
        /**
         * The Id of the client.
         */
// my unique id (easier for disconnection)
        int id;
        /**
         * The Username of the client.
         */
// the Username of the Client
        String username;
        /**
         * The Message.
         */
// message object to receive message and its type
        String message;
        /**
         * The Date.
         */
// timestamp
        String date;
        private boolean isWait = false, isLastMoment = false,isReady = false, canTalk = true ;
        private ClientThread vote = null;
        private ArrayList<ClientThread> votes = new ArrayList<>();
        private int deActiveInARow = 0;

        /**
         * Instantiates a new Client thread.
         *
         * @param server the server
         * @param socket the socket
         */
// Constructor
        ClientThread(Server server, Socket socket) {
            // a unique id
            id = ++uniqueId;
            this.socket = socket;
            //Creating both Data Stream
            System.out.println("Thread trying to create Object Input/Output Streams");
            try {

                sOutput = new DataOutputStream(socket.getOutputStream());
                sInput = new DataInputStream(socket.getInputStream());
                // read the username
                username = sInput.readUTF();

                for(ClientThread clientThread : clientThreads){
                    if(username.equals(clientThread.getUsername())){
                        username = username +
                                uniqueId +
                                uniqueId / 2 +
                                uniqueId * 3;
                    }
                }


/*
                for(int i = 0 ; i < clientThreads.size() ; i++){
                    if(username.equals(clientThreads.get(i).getUsername())){
                        writeMsg("choose another username: ");
                        username = sInput.readUTF();
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

        /**
         * Is last moment boolean.
         * is client dead or not?
         *
         * @return the boolean
         */
        public boolean isLastMoment() {
            return isLastMoment;
        }


        /**
         * Gets vote.
         *
         * @return the vote
         */
        public ClientThread getVote() {
            return vote;
        }

        /**
         * Gets votes.
         *
         * @return the votes
         */
        public ArrayList<ClientThread> getVotes() {
            return votes;
        }

        /**
         * Is ready boolean.
         *
         * @return the boolean
         */
        public boolean isReady() {
            return isReady;
        }

        /**
         * Gets de active in a row.
         *
         * @return the de active in a row
         */
        public int getDeActiveInARow() {
            return deActiveInARow;
        }

        /**
         * Sets can talk.
         *
         * @param canTalk the can talk
         */
        public void setCanTalk(boolean canTalk) {
            this.canTalk = canTalk;
        }

        /**
         * Sets votes.
         *
         * @param votes the votes
         */
        public void setVotes(ArrayList<ClientThread> votes) {
            this.votes = votes;
        }

        /**
         * Gets username.
         *
         * @return the username
         */
        public String getUsername() {
            return username;
        }

        /**
         * Sets wait.
         *
         * @param wait the wait
         */
        public void setWait(boolean wait) {
            isWait = wait;
        }

        /**
         * Sets ready.
         *
         * @param ready the ready
         */
        public void setReady(boolean ready) {
            isReady = ready;
        }

        /**
         * Sets de active in a row.
         *
         * @param deActiveInARow the de active in a row
         */
        public void setDeActiveInARow(int deActiveInARow) {
            this.deActiveInARow = deActiveInARow;
        }


        /**
         * Sets vote.
         *
         * @param vote the vote
         */
        public void setVote(ClientThread vote) {
            this.vote = vote;
        }

        /**
         * Sets last moment.
         *
         * @param lastMoment the last moment
         */
        public void setLastMoment(boolean lastMoment) {
            isLastMoment = lastMoment;
        }

        /**
         * Add a vote.
         *
         * @param ct the ct
         */
        public void addAVote(Server.ClientThread ct){
            votes.add(ct);
        }

        /**
         * Add de activeness by 1.
         */
        public void addDeActiveness(){
            deActiveInARow +=1;
        }

        // this manage whole input & output streams of the client
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

                String[] splitMessage = message.split(" ");

                if(message.equalsIgnoreCase("!LOGOUT")){
                    //display(username + " disconnected with a LOGOUT message.");
                    gameManager.disconnected(this);
                    //keepGoing = false;
                    break;
                }else if(message.equalsIgnoreCase("!WhoIsIn")){
                    writeMsg("List of the users connected at " + simpleDateFormat.format(new Date()) + "\n");
                    // send list of active clients
                    for (int i = 0; i < clientThreads.size(); ++i) {
                        Server.ClientThread ct = clientThreads.get(i);
                        writeMsg((i + 1) + ") " + ct.username + " since " + ct.date);
                    }
                }else if(canTalk) {
                    if (message.equalsIgnoreCase("!READY") && !isLastMoment) {
                        if(!isReady) {
                            gameManager.ready();
                            isReady = true;
                            broadcast(BLUE + gameManager.getReadyToGo()
                                            + " number of players are ready so far" + RESET, getClientThreads());
                        }else{
                            writeMsg(RED + "you said you are ready before!" + RESET);
                        }
                    }else if (!waitingToGo && !isLastMoment) {
                        if (message.length() > 0 && message.charAt(0) == '@') {
                            Player curPlayer = gameManager.getPlayer(this);
                            //String[] decodedMsg = message.split(" ");
                            if (curPlayer instanceof GodFather) {
                                gameManager.godFatherShot(message.substring(1), this);
                            } else if (curPlayer instanceof LectorDoctor) {
                                gameManager.lectorHill(message.substring(1), this);
                            } else if (curPlayer instanceof Doctor) {
                                gameManager.doctorHill(message.substring(1), this);
                            } else if (curPlayer instanceof Detective) {
                                gameManager.detectiveAttempt(message.substring(1), this);
                            } else if (curPlayer instanceof Professional) {
                                gameManager.professionalShot(message.substring(1), this);
                            } else if (curPlayer instanceof Psychologist) {
                                gameManager.psychologistAttempt(message.substring(1), this);
                            } else if (curPlayer instanceof Invulnerable) {
                                if (message.substring(1).equalsIgnoreCase("!RESULT")) {
                                    gameManager.invulnerableAttempt(this);
                                }
                            } else if (curPlayer instanceof Mayor) {
                                if (message.substring(1).equalsIgnoreCase("!CANCEL")) {
                                    gameManager.mayorAttempt(this);
                                }
                            }
                        }
                        else if (splitMessage[0].equalsIgnoreCase("!VOTE")) {
                            if (splitMessage[1].charAt(0) == '@') {
                                gameManager.vote(splitMessage[1].substring(1), this);
                            }
                        } else {
                            boolean confirmation = broadcast(username + ": " + message, activeClients);
                            if (!confirmation) {
                                String msg = RED + notification + "Sorry. No such user exists." + notification + RESET;
                                writeMsg(msg);
                            }
                        }

                    }
                }else if (splitMessage[0].equalsIgnoreCase("!VOTE") && !isLastMoment && !waitingToGo) {
                    if (splitMessage[1].charAt(0) == '@') {
                        gameManager.vote(splitMessage[1].substring(1), this);
                    }
                }else if (message.equalsIgnoreCase("!READY") && !isLastMoment) {
                    if(!isReady) {
                        gameManager.ready();
                        isReady = true;
                        broadcast(BLUE + gameManager.getReadyToGo() + " number of players are ready so far"
                                        + RESET, getClientThreads());
                    }else{
                        writeMsg("you said you are ready before!");
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
                System.out.println("Error closing data output stream");
            }
            try {
                if (sInput != null) sInput.close();
            } catch (Exception e) {
                System.out.println("Error closing data input stream");
            }

            try {
                if (socket != null) socket.close();
            } catch (Exception e) {
                System.out.println("Error closing socket");
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

        /**
         * Write msg boolean.
         *
         * @param msg the msg
         * @return the boolean
         */
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
                gameManager.disconnected(this);
                clientThreads.remove(this);
                broadcast(RED + "Disconnected Client " + username + " removed from list." + RESET
                        , clientThreads);
                display(e.toString());
            }

            return true;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClientThread that = (ClientThread) o;
            return Objects.equals(getUsername(), that.getUsername());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getUsername());
        }
    }


}

