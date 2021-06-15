package Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

// lector doctor 1 time himself +
// announce died  in night & muted +
// shuffle role Invulnerable +
// cant hill or kill already dead players +
// cant vote dead people +
// out dead people failed +
// nobody is dead in night 2 ! professional if he wrong for ex +
// muted can vote +
// lector himself bug +
// chat file -
// player can vote at night -
// can skip day +
// show voters +
// Invulnerable result for all +
// ready! +
// username +
// vote end early if someone dead logout +


/**
 * The type Game manager.
 * this manage the whole game
 * this decide what server should do
 */
public class GameManager {

    private int numberOfPlayers, numberOfMafias, numberOfCitizens;
    private ArrayList<Server.ClientThread> clientThreads;
    private ArrayList<Server.ClientThread> mafiaClients = new ArrayList<>();
    private ArrayList<Player> players = new ArrayList<>();
    private HashMap<Server.ClientThread, Player> connectClientToRole = new HashMap<>();
    private ArrayList<Server.ClientThread> deadClients = new ArrayList<>();
    //private HashMap<Server.ClientThread, MafiaTeam> mafiaTeam = new HashMap<>();
    //private HashMap<Server.ClientThread, CitizenTeam> citizenTeam = new HashMap<>();
    private Server server;
    private int readyToGo = 0;
    private int targetToGo = 0;

    private final int firstDayChatTime = 10, mafiaNightTime = 20, citizenNightTime = 20, dayChatTime = 300;

    private boolean isGodFatherShot = false, isLectorDoctorHill = false, isDoctorHill = false,
            isDetectiveAttempt = false, isProfessionalShot = false,
            isPsychologistMuted = false, isInvulnerableAttempt  =false,
            isMayorAttempt = false, isLectorHillItself = false, isDoctorHillItself = false;
    private int numberOfInvulnerableAttempt = 0;
    private Server.ClientThread protectedByLector = null;
    private boolean votingHasBeenCanceled = false, keepGoing = true;

    /**
     * The constant BLACK color.
     */
    public static final String	BLACK				= "\u001B[30m";
    /**
     * The constant RED color.
     */
    public static final String	RED					= "\u001B[31m";
    /**
     * The constant GREEN color.
     */
    public static final String	GREEN				= "\u001B[32m";
    /**
     * The constant YELLOW color.
     */
    public static final String	YELLOW				= "\u001B[33m";
    /**
     * The constant BLUE color.
     */
    public static final String	BLUE				= "\u001B[34m";
    /**
     * The constant MAGENTA color.
     */
    public static final String	MAGENTA				= "\u001B[35m";
    /**
     * The constant CYAN color.
     */
    public static final String	CYAN				= "\u001B[36m";
    /**
     * The constant WHITE color.
     */
    public static final String	WHITE				= "\u001B[37m";
    /**
     * The constant RESET color.
     */
    public static final String  RESET               = "\u001B[0m";


    /**
     * Instantiates a new Game manager.
     *
     * @param numberOfPlayers the number of players
     * @param clientThreads   the client threads
     * @param server          the server
     */
    public GameManager(int numberOfPlayers, ArrayList<Server.ClientThread> clientThreads, Server server) {
        this.numberOfPlayers = numberOfPlayers;
        this.clientThreads = clientThreads;
        this.server = server;
        numberOfMafias = numberOfPlayers / 3;
        numberOfCitizens = numberOfPlayers - numberOfMafias;
    }

    /**
     * Gets ready to go clients.
     *
     * @return the ready to go clients
     */
    public int getReadyToGo() {
        return readyToGo;
    }

    /**
     * Create players.
     * this create players at the start of the game
     * depend on how many players are in the game
     */
    public void createPlayers() {

        if (numberOfMafias == 1) {
            players.add(new GodFather());
        } else {
            players.add(new LectorDoctor());
            players.add(new GodFather());

            for (int i = 0; i < numberOfMafias - 2; i++) {
                players.add(new SimpleMafia());
            }
        }

        switch (numberOfCitizens) {
            case 7: {
                players.add(new SimpleCitizen());
            }
            case 6: {
                players.add(new Mayor());
            }
            case 5: {
                players.add(new Professional());
            }
            case 4: {
                players.add(new Psychologist());
            }
            case 3: {
                players.add(new Invulnerable());
            }
            case 2: {
                players.add(new Detective());
            }
            case 1: {
                players.add(new Doctor());
                break;
            }
            default:{
                players.add(new Mayor());
                players.add(new Professional());
                players.add(new Psychologist());
                players.add(new Invulnerable());
                players.add(new Detective());
                players.add(new Doctor());
                for (int i = 0; i < numberOfCitizens - 6; i++) {
                    players.add(new SimpleCitizen());
                }

            }
        }

    }

    /**
     * Give roles.
     * give clients their roles
     */
    public void giveRoles() {
        Collections.shuffle(clientThreads);
        createPlayers();
        int i = 0;

        for (Server.ClientThread clientThread : clientThreads) {
            connectClientToRole.put(clientThread, players.get(i));
            i++;
        }
        for (Server.ClientThread clientThread : clientThreads) {
            System.out.println(clientThread.getUsername() + " -> " + connectClientToRole.get(clientThread));
        }
/*
        for(Server.ClientThread clientThread : clientThreads){
            Player curPlayer = connectClientToRole.get(clientThread);
            if(curPlayer instanceof MafiaTeam){
                mafiaTeam.put(clientThread, (MafiaTeam) curPlayer);
            }else{
                citizenTeam.put(clientThread, (CitizenTeam) curPlayer);
            }
        }
 */
        for (Server.ClientThread clientThread : server.clientThreads) {
            if (connectClientToRole.get(clientThread) instanceof MafiaTeam) {
                mafiaClients.add(clientThread);
            }
        }

    }

    /**
     * First day chat.
     * this handle first day of chatroom
     */
    public void firstDayChat() {

        for (Server.ClientThread clientThread : clientThreads) {
            clientThread.start();
        }

    }

    /**
     * First night.
     * this handle first night of the game
     */
    public void firstNight() {

        for (Server.ClientThread clientThread : clientThreads) {
            Player curPlayer = connectClientToRole.get(clientThread);

            if (curPlayer instanceof SimpleMafia) {
                server.sendMsgToClient(BLACK + "God : You are Simple Mafia" + RESET, clientThread);
                showMafiaTeam(clientThread);
            } else if (curPlayer instanceof GodFather) {
                server.sendMsgToClient(BLACK + "God : You are God Father" + RESET, clientThread);
                showMafiaTeam(clientThread);
            } else if (curPlayer instanceof LectorDoctor) {
                server.sendMsgToClient(BLACK + "God : You are Lector Doctor" + RESET, clientThread);
                showMafiaTeam(clientThread);
            } else if (curPlayer instanceof SimpleCitizen) {
                server.sendMsgToClient(BLUE + "God : You are Simple citizen" + RESET, clientThread);
            } else if (curPlayer instanceof Doctor) {
                server.sendMsgToClient(BLUE + "God : You are Doctor" + RESET, clientThread);
            } else if (curPlayer instanceof Detective) {
                server.sendMsgToClient(BLUE + "God : You are Detective" + RESET, clientThread);
            } else if (curPlayer instanceof Invulnerable) {
                server.sendMsgToClient(BLUE + "God : You are Invulnerable" + RESET, clientThread);
            } else if (curPlayer instanceof Mayor) {
                server.sendMsgToClient(BLUE + "God : You are Mayor" + RESET, clientThread);
                for(Server.ClientThread ct : server.getClientThreads()){
                    if(connectClientToRole.get(ct) instanceof Doctor){
                        server.sendMsgToClient("God : " + ct.getUsername() + " is Doctor!", clientThread);
                    }
                }
            } else if (curPlayer instanceof Professional) {
                server.sendMsgToClient(BLUE + "God : You are Professional" + RESET, clientThread);
            } else if (curPlayer instanceof Psychologist) {
                server.sendMsgToClient(BLUE + "God : You are Psychologist" + RESET, clientThread);
            }


        }

    }

    /**
     * Show mafia team.
     * @param ctToSend the ct to send
     */
    public void showMafiaTeam(Server.ClientThread ctToSend) {
        int row = 1;

        server.sendMsgToClient(YELLOW + "The Mafia Team is: " + RESET, ctToSend);

        for (Server.ClientThread clientThread : clientThreads) {
            if (connectClientToRole.get(clientThread) instanceof MafiaTeam) {
                server.sendMsgToClient(YELLOW + row + ": " + clientThread.getUsername()
                        + " -> " + connectClientToRole.get(clientThread) + RESET, ctToSend);
                row++;
            }
        }
    }

    /**
     * Gets player.
     * enter the client, get the player!
     * @param clientThread the client thread
     * @return the player
     */
    public Player getPlayer(Server.ClientThread clientThread) {
        return connectClientToRole.get(clientThread);
    }

    /**
     * Sleep.
     * sleep a thread for a certain time
     * @param seconds the seconds
     */
    public void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            System.out.println("error during sleep");
            e.printStackTrace();
        }
    }

    /**
     * Notify all clients.
     */
    public synchronized void notifyAllClients() {
        for (Server.ClientThread clientThread : server.getClientThreads()) {
            synchronized (clientThread) {
                clientThread.setWait(false);
                clientThread.notify();
            }
        }
    }

    /**
     * Disconnected.
     * handle the works after a client diconnect
     * @param ct the ct
     */
/*
    public synchronized void notifySomeClients(ArrayList<Server.ClientThread> cts) {
        for (Server.ClientThread clientThread : cts) {
            if(!clientThread.isDead())
            synchronized (clientThread) {
                clientThread.setWait(false);
                clientThread.notify();
            }
        }
    }
 */
    public void disconnected(Server.ClientThread ct){
        if(connectClientToRole.get(ct).isAlive()){
            deadClients.add(ct);
        }
    }

    /**
     * Wait all clients.
     */
    public synchronized void waitAllClients() {
        for (Server.ClientThread clientThread : server.clientThreads) {
            clientThread.setWait(true);
        }
    }

    /**
     * Is client died boolean.
     * is client dead return true else false
     *
     * @param ct the ct
     * @return the boolean
     */
    public boolean isClientDied(Server.ClientThread ct){
        for(Server.ClientThread obj : deadClients){
            if(obj.equals(ct)){
                return true;
            }
        }
        return false;
    }

    /**
     * God father shot.
     * kill citizens
     *
     * @param usernameToFind the username to find
     * @param godFatherCT    the god father client
     */
    public void godFatherShot(String usernameToFind, Server.ClientThread godFatherCT) {

        Player player = null;
        ArrayList<Server.ClientThread> godFather = new ArrayList<>();
        ArrayList<Server.ClientThread> activeClients = server.getActiveClients();
        godFather.add(godFatherCT);
        server.setActiveClients(godFather);

        if (!isGodFatherShot) {
            for (Server.ClientThread ct : clientThreads) {
                if (usernameToFind.equals(ct.getUsername())) {
                    player = connectClientToRole.get(ct);
                    if(!isClientDied(ct)) {
                        if (player instanceof CitizenTeam) {
                            server.broadcast(GREEN + "God: Nice Shot!" + RESET, server.getActiveClients());
                            isGodFatherShot = true;
                            player.setAlive(false);

                            if (player instanceof Invulnerable) {
                                if (!((Invulnerable) player).isHasBeenShot()) {
                                    ((Invulnerable) player).setHasBeenShot(true);
                                    player.setAlive(true);
                                }
                            }

                        } else {
                            server.broadcast(RED + "God: you cant shot a mafia!" + RESET
                                    , server.getActiveClients());
                        }
                    }else {
                        server.broadcast(RED + "God: Player is already dead!" + RESET
                                , server.getActiveClients());
                    }
                }
            }if(player == null) {
                server.broadcast(RED + "God: there isn't any client with this username" + RESET
                        , server.getActiveClients());
            }
        }else{
            server.broadcast(RED + "God: you shot before" + RESET, server.getActiveClients());
        }

        server.setActiveClients(activeClients);

    }

    /**
     * Lector hill.
     * hill a mafia
     *
     * @param usernameToFind the username to find
     * @param lectorCT       the lector client
     */
    public void lectorHill(String usernameToFind, Server.ClientThread lectorCT){
        Player player = null;
        ArrayList<Server.ClientThread> lectorDoctor = new ArrayList<>();
        ArrayList<Server.ClientThread> activeClients = server.getActiveClients();
        lectorDoctor.add(lectorCT);
        server.setActiveClients(lectorDoctor);

        if (!isLectorDoctorHill) {
            for (Server.ClientThread ct : clientThreads) {
                if (usernameToFind.equals(ct.getUsername())) {
                    player = connectClientToRole.get(ct);
                    if(!isClientDied(ct)) {
                        if (player instanceof MafiaTeam) {
                            if(player instanceof LectorDoctor ){
                                if(!isLectorHillItself) {
                                    isLectorHillItself = true;
                                    server.broadcast(GREEN + "God: you protected yourself" + RESET,
                                            server.getActiveClients());
                                    isLectorDoctorHill = true;
                                    protectedByLector = ct;
                                }else{
                                    server.broadcast(RED + "God: you hill yourself once" + RESET,
                                            server.getActiveClients());
                                }
                            }else{
                                server.broadcast(GREEN + "God: you protected " + ct.getUsername() + RESET,
                                        server.getActiveClients());
                                isLectorDoctorHill = true;
                                protectedByLector = ct;
                            }
                        } else {
                            server.broadcast(RED + "God: you cant hill a citizen!" + RESET
                                    , server.getActiveClients());
                        }
                    }else{
                        server.broadcast(RED + "God: Player is already dead!" + RESET
                                , server.getActiveClients());
                    }
                }
            }if(player == null) {
                server.broadcast(RED + "God: there isn't any client with this username" + RESET
                        , server.getActiveClients());
            }
        }else{
            server.broadcast(RED + "God: you hill before" + RESET, server.getActiveClients());
        }

        server.setActiveClients(activeClients);

    }

    /**
     * Doctor hill.
     * hill a player
     *
     * @param usernameToFind the username to find
     * @param doctorCt       the doctor client
     */
    public void doctorHill(String usernameToFind, Server.ClientThread doctorCt){
        Player player = null;
        ArrayList<Server.ClientThread> doctor = new ArrayList<>();
        ArrayList<Server.ClientThread> activeClients = server.getActiveClients();
        doctor.add(doctorCt);
        server.setActiveClients(doctor);

        if (!isDoctorHill) {
            for (Server.ClientThread ct : clientThreads) {
                if (usernameToFind.equals(ct.getUsername())) {
                    if(!isClientDied(ct)) {
                        player = connectClientToRole.get(ct);
                        if(player instanceof Doctor){
                            if(!isDoctorHillItself){
                                isDoctorHillItself = true;
                                server.broadcast(GREEN + "God: you protected yourself" + RESET,
                                        server.getActiveClients());
                                isDoctorHill = true;
                                player.setAlive(true);
                            }else{
                                server.broadcast(RED + "God: you hill yourself once" + RESET,
                                        server.getActiveClients());
                            }

                        }else {
                            player.setAlive(true);
                            server.broadcast(GREEN + "God: you protected " + ct.getUsername() + RESET
                                    , server.getActiveClients());
                            isDoctorHill = true;
                        }
                    }else{
                        server.broadcast(RED + "God: Player is already dead!" + RESET
                                , server.getActiveClients());
                    }
                }
            }if(player == null) {
                server.broadcast(RED + "God: there isn't any client with this username" + RESET
                        , server.getActiveClients());
            }
        }else{
            server.broadcast(RED + "God: you hill before" + RESET, server.getActiveClients());
        }

        server.setActiveClients(activeClients);

    }

    /**
     * Detective attempt.
     * know the team of the client -> mafia or citizen
     *
     * @param usernameToFind the username to find
     * @param detectiveCT    the detective client
     */
    public void detectiveAttempt(String usernameToFind, Server.ClientThread detectiveCT){
        Player player = null;
        ArrayList<Server.ClientThread> detective = new ArrayList<>();
        ArrayList<Server.ClientThread> activeClients = server.getActiveClients();
        detective.add(detectiveCT);
        server.setActiveClients(detective);

        if (!isDetectiveAttempt) {
            for (Server.ClientThread ct : clientThreads) {
                if (usernameToFind.equals(ct.getUsername())) {
                    player = connectClientToRole.get(ct);

                    if(!isClientDied(ct)) {
                        if (player instanceof CitizenTeam || player instanceof GodFather) {
                            server.broadcast(GREEN + "God: " + ct.getUsername()
                                            + " is in Citizen's team" + RESET, server.getActiveClients());
                        } else {
                            server.broadcast(GREEN + "God: " + ct.getUsername() + " is in Mafia's team" + RESET
                                    , server.getActiveClients());
                        }
                        isDetectiveAttempt = true;
                    }else{
                        server.broadcast(RED + "God: Player is already dead!" + RESET
                                , server.getActiveClients());
                    }
                }
            }if(player == null) {
                server.broadcast(RED + "God: there isn't any client with this username" + RESET
                        , server.getActiveClients());
            }
        }else{
            server.broadcast(RED + "God: you attempted before" + RESET, server.getActiveClients());
        }

        server.setActiveClients(activeClients);

    }

    /**
     * Professional shot.
     * shot a client
     *
     * @param usernameToFind the username to find
     * @param professionalCT the professional ct
     */
    public void professionalShot(String usernameToFind, Server.ClientThread professionalCT) {

        Player player = null;
        ArrayList<Server.ClientThread> professional = new ArrayList<>();
        ArrayList<Server.ClientThread> activeClients = server.getActiveClients();
        professional.add(professionalCT);
        server.setActiveClients(professional);

        if (!isProfessionalShot) {
            for (Server.ClientThread ct : clientThreads) {
                if (usernameToFind.equals(ct.getUsername())) {
                    player = connectClientToRole.get(ct);
                    if(!isClientDied(ct)) {
                        server.broadcast(GREEN + "God: Nice Shot!" + RESET, server.getActiveClients());
                        isProfessionalShot = true;
                        if (player instanceof MafiaTeam) {
                            player.setAlive(false);
                            if (ct.equals(protectedByLector) || player instanceof GodFather) {
                                player.setAlive(true);
                            }
                        } else {
                            connectClientToRole.get(professionalCT).setAlive(false);
                        }
                    }else{
                        server.broadcast(RED + "God: Player is already dead!" + RESET
                                , server.getActiveClients());
                    }
                }
            }if(player == null) {
                server.broadcast(RED + "God: there isn't any client with this username" + RESET
                        , server.getActiveClients());
            }
        }else{
            server.broadcast(RED + "God: you shot before" + RESET, server.getActiveClients());
        }

        server.setActiveClients(activeClients);

    }

    /**
     * Psychologist attempt.
     * mute a client
     *
     * @param usernameToFind the username to find
     * @param psychologistCT the psychologist client
     */
    public void psychologistAttempt(String usernameToFind, Server.ClientThread psychologistCT) {

        ArrayList<Server.ClientThread> psychologist = new ArrayList<>();
        ArrayList<Server.ClientThread> activeClients = server.getActiveClients();
        psychologist.add(psychologistCT);
        server.setActiveClients(psychologist);
        boolean found = false;

        if (!isPsychologistMuted) {
            for (Server.ClientThread ct : clientThreads) {
                if (usernameToFind.equals(ct.getUsername())) {
                    found = true;
                    if(!isClientDied(ct)) {
                        server.broadcast(GREEN + "God: Well Done!" + RESET, server.getActiveClients());
                        server.broadcast(CYAN + "God: " + ct.getUsername() + " muted!" + RESET
                                , server.getClientThreads());
                        isPsychologistMuted = true;
                        ct.setCanTalk(false);
                    }else{
                        server.broadcast(RED + "God: Player is already dead!" + RESET
                                , server.getActiveClients());
                    }
                }
            }if(!found) {
                server.broadcast(RED + "God: there isn't any client with this username" + RESET
                        , server.getActiveClients());
            }
        }else{
            server.broadcast(RED + "God: you attempt before" + RESET, server.getActiveClients());
        }

        server.setActiveClients(activeClients);

    }

    /**
     * Invulnerable attempt.
     * know the roles of the clients which are dead
     *
     * @param invulnerableCT the invulnerable client
     */
    public void invulnerableAttempt(Server.ClientThread invulnerableCT) {

        ArrayList<Server.ClientThread> invulnerable = new ArrayList<>();
        ArrayList<Server.ClientThread> activeClients = server.getActiveClients();
        invulnerable.add(invulnerableCT);
        server.setActiveClients(invulnerable);

        if(numberOfInvulnerableAttempt < 2) {
            if (!isInvulnerableAttempt) {
                server.broadcast(GREEN + "God: Done! God will say the roles tomorrow" + RESET,
                        server.getActiveClients());
                numberOfInvulnerableAttempt += 1;
                isInvulnerableAttempt = true;

            } else {
                server.broadcast(RED + "God: you attempt before" + RESET, server.getActiveClients());
            }
        }else{
            server.broadcast(RED + "God: you have reached your limit ( 2times )" + RESET
                    , server.getActiveClients());
        }

        server.setActiveClients(activeClients);

    }

    /**
     * Mayor attempt.
     * can cancel the voting
     *
     * @param mayorCT the mayor client
     */
    public void mayorAttempt(Server.ClientThread mayorCT) {

        ArrayList<Server.ClientThread> mayor = new ArrayList<>();
        ArrayList<Server.ClientThread> activeClients = server.getActiveClients();
        mayor.add(mayorCT);
        server.setActiveClients(mayor);

        if (!isMayorAttempt) {
            votingHasBeenCanceled = true;
            isMayorAttempt = true;
            server.broadcast(CYAN + "God: Voting canceled" + RESET, server.getClientThreads());

        } else {
            server.broadcast(RED + "God: you attempt before" + RESET, server.getActiveClients());
        }


        server.setActiveClients(activeClients);

    }

    /**
     * Vote.
     * voting process of a client
     *
     * @param usernameToFind the username to find
     * @param currentCt      the current client
     */
    public void vote(String usernameToFind, Server.ClientThread currentCt) {

        ArrayList<Server.ClientThread> psychologist = new ArrayList<>();
        ArrayList<Server.ClientThread> activeClients = server.getActiveClients();
        psychologist.add(currentCt);
        server.setActiveClients(psychologist);
        boolean found = false;

        if (!currentCt.isReady()) {
            for (Server.ClientThread ct : clientThreads) {
                if (usernameToFind.equals(ct.getUsername())) {
                    found = true;
                    if(!isClientDied(ct)) {
                        server.broadcast(BLUE + "God: " + currentCt.getUsername()
                                        + " voted " + ct.getUsername() + RESET, server.getClientThreads());
                        currentCt.setVote(ct);
                    }else{
                        server.broadcast(RED + "God: this client is dead!" + RESET, server.getActiveClients());
                    }
                }
            }if(!found) {
                server.broadcast(RED + "God: there isn't any client with this username" + RESET
                        , server.getActiveClients());
            }
        }else{
            server.broadcast(RED + "God: you passed the voting" + RESET, server.getActiveClients());
        }

        server.setActiveClients(activeClients);

    }

    /**
     * Collect votes of the clients.
     */
    public void collectVotes(){
        for(Server.ClientThread ct : server.getClientThreads()){
            for(Server.ClientThread ctVote : server.getClientThreads()){
                if(ctVote.equals(ct.getVote())){
                    ctVote.addAVote(ct);
                }
            }
        }
    }

    /**
     * Announce votes of each client.
     */
    public void announceVotes(){
        StringBuilder string = new StringBuilder();

        for(Server.ClientThread clientThread : server.getClientThreads()){
            server.broadcast(CYAN + "People who voted " + clientThread.getUsername() + ": " + RESET
                    , server.getClientThreads());
            for(Server.ClientThread vote : clientThread.getVotes()){
                string.append(vote.getUsername()).append(", ");
            }
            server.broadcast(BLUE + string.toString() + RESET, server.getClientThreads());
            string = new StringBuilder();
            sleep(1);
        }
    }

    /**
     * Collect de actives clients. " clients that didn't vote "
     */
    public void collectDeActives(){
        int deActiveDaysToKick = 3;

        for(Server.ClientThread ct : server.getClientThreads()){
            Player player = connectClientToRole.get(ct);
            if(player.isAlive()){
                if(ct.getVote() == null){
                    ct.addDeActiveness();
                    ct.writeMsg(YELLOW + "God: you were deActive for " + ct.getDeActiveInARow() + " day(s)" + RESET);
                }else{
                    ct.setDeActiveInARow(0);
                }
                if(ct.getDeActiveInARow() >= deActiveDaysToKick){
                    ct.writeMsg(RED + "God: you are kicked from game for because you were deActive for 3 days in a row"
                            + RESET);
                    player.setAlive(false);
                    presentLastMoment(ct);
                }
            }
        }
    }

    /**
     * Mafia night.
     * wake mafia up then wait them
     */
    public void mafiaNight(){

        server.broadcast(CYAN + "God: Night begins" + RESET , server.getClientThreads());
        server.broadcast(CYAN + "God: Mafia Team chat & kill some one within " + mafiaNightTime + " seconds!"
                        + RESET, server.getClientThreads());

        mafiaClients = new ArrayList<>();

        for(Server.ClientThread ct : server.getClientThreads()){
            if(connectClientToRole.get(ct) instanceof MafiaTeam){
                mafiaClients.add(ct);
                synchronized (ct) {
                    ct.setWait(false);
                    ct.notify();
                }
            }
        }

        if(mafiaClients.size() > 0) {
            server.setActiveClients(mafiaClients);
            server.broadcast(MAGENTA + "God: to kill someone, GodFather send -> [ @<username> ] " + RESET,
                    server.getActiveClients());
            server.broadcast(MAGENTA + "God: to hill someone, LectorDoctor send -> [ @<username> ] " + RESET,
                    server.getActiveClients());
        }

        sleep(mafiaNightTime);
        server.broadcast(BLUE + "God: Mafias go to sleep" + RESET, server.getClientThreads());
        waitAllClients();
        isGodFatherShot = false;
        isLectorDoctorHill =false;
        //protectedByLector = null;
        server.setActiveClients(server.getClientThreads());
    }

    /**
     * Doctor night.
     * wake doctor up then wait it
     */
    public void doctorNight(){

        server.broadcast(CYAN + "God: Doctor wakeUp & hill someone within " + citizenNightTime + " seconds!"
                        + RESET, server.getClientThreads());

        ArrayList<Server.ClientThread> doctor = new ArrayList<>();

        for(Server.ClientThread ct : server.getClientThreads()){
            if(connectClientToRole.get(ct) instanceof Doctor){
                doctor.add(ct);
                synchronized (ct) {
                    ct.setWait(false);
                    ct.notify();
                }
            }
        }

        if(doctor.size() > 0) {
            server.setActiveClients(doctor);
            server.broadcast(MAGENTA + "God: to hill someone, send -> [ @<username> ] " + RESET
                    , server.getActiveClients());
        }

        sleep(citizenNightTime);
        server.broadcast(BLUE + "God: Doctor go to sleep" + RESET, server.getClientThreads());
        waitAllClients();
        isDoctorHill = false;
        server.setActiveClients(server.getClientThreads());
    }

    /**
     * Detective night.
     * wake detective up then wait it
     */
    public void detectiveNight(){

        server.broadcast(CYAN + "God: Detective wakeUp -> you have " + citizenNightTime + " seconds!" + RESET
                , server.getClientThreads());

        ArrayList<Server.ClientThread> detective = new ArrayList<>();

        for(Server.ClientThread ct : server.getClientThreads()){
            if(connectClientToRole.get(ct) instanceof Detective){
                detective.add(ct);
                synchronized (ct) {
                    ct.setWait(false);
                    ct.notify();
                }
            }
        }

        if(detective.size() > 0) {
            server.setActiveClients(detective);
            server.broadcast(MAGENTA + "God: to know a role of someone, send -> [ @<username> ] " + RESET,
                    server.getActiveClients());
        }

        sleep(citizenNightTime);
        server.broadcast(BLUE + "God: Detective go to sleep" + RESET, server.getClientThreads());
        waitAllClients();
        isDetectiveAttempt = false;
        server.setActiveClients(server.getClientThreads());
    }

    /**
     * Professional night.
     * wake Professional up then wait it
     */
    public void professionalNight(){

        server.broadcast(CYAN + "God: Professional wakeUp you have -> " + citizenNightTime + " seconds!" + RESET
                , server.getClientThreads());

        ArrayList<Server.ClientThread> professional = new ArrayList<>();

        for(Server.ClientThread ct : server.getClientThreads()){
            if(connectClientToRole.get(ct) instanceof Professional){
                professional.add(ct);
                synchronized (ct) {
                    ct.setWait(false);
                    ct.notify();
                }
            }
        }

        if(professional.size() > 0) {
            server.setActiveClients(professional);
            server.broadcast(MAGENTA + "God: if you want to shoot someone, send -> [ @<username> ] " + RESET,
                    server.getActiveClients());
        }

        sleep(citizenNightTime);
        server.broadcast(BLUE + "God: Professional go to sleep" + RESET, server.getClientThreads());
        waitAllClients();
        isProfessionalShot = false;
        server.setActiveClients(server.getClientThreads());
    }

    /**
     * Psychologist night.
     * wake Psychologist up then wait it
     */
    public void psychologistNight(){

        server.broadcast(CYAN + "God: Psychologist wakeUp you have -> " + citizenNightTime + " seconds!" + RESET
                , server.getClientThreads());

        ArrayList<Server.ClientThread> psychologist = new ArrayList<>();

        for(Server.ClientThread ct : server.getClientThreads()){
            if(connectClientToRole.get(ct) instanceof Psychologist){
                psychologist.add(ct);
                synchronized (ct) {
                    ct.setWait(false);
                    ct.notify();
                }
            }
        }

        if(psychologist.size() > 0) {
            server.setActiveClients(psychologist);
            server.broadcast(MAGENTA + "God: if you want to mute someone, send -> [ @<username> ] " + RESET,
                    server.getActiveClients());
        }

        sleep(citizenNightTime);
        server.broadcast(BLUE + "God: Psychologist go to sleep" + RESET, server.getClientThreads());
        waitAllClients();
        isPsychologistMuted = false;
        server.setActiveClients(server.getClientThreads());
    }

    /**
     * Invulnerable night.
     * wake Invulnerable up then wait it
     */
    public void invulnerableNight(){

        server.broadcast(CYAN + "God: invulnerable Up wakeUp -> you have " + citizenNightTime + " seconds!"
                        + RESET, server.getClientThreads());

        ArrayList<Server.ClientThread> invulnerable = new ArrayList<>();

        for(Server.ClientThread ct : server.getClientThreads()){
            if(connectClientToRole.get(ct) instanceof Invulnerable){
                invulnerable.add(ct);
                synchronized (ct) {
                    ct.setWait(false);
                    ct.notify();
                }
            }
        }

        if(invulnerable.size() > 0) {
            server.setActiveClients(invulnerable);
            server.broadcast(MAGENTA + "God: if you want to know the result, send -> [ @!result ] " + RESET,
                    server.getActiveClients());
        }

        sleep(citizenNightTime);
        server.broadcast(BLUE + "God: Invulnerable go to sleep" + RESET, server.getClientThreads());
        waitAllClients();
        //isInvulnerableAttempt = false;
        server.setActiveClients(server.getClientThreads());
    }

    /**
     * Invulnerable act.
     * if Invulnerable attempted last night this method do the work.
     * announce the role's of dead clients
     */
    public void invulnerableAct(){


        if(isInvulnerableAttempt){
            ArrayList<Server.ClientThread> shuffledDead  = deadClients;
            Collections.shuffle(shuffledDead);
            if(shuffledDead.size() > 0) {
                server.broadcast(BLACK + "God: Dead Roles: " + RESET, server.getClientThreads());
                for (Server.ClientThread deadClient : shuffledDead) {
                    server.broadcast("-> " + WHITE + connectClientToRole.get(deadClient).toString() + RESET,
                            server.getClientThreads());
                    sleep(1);
                }
            }else{
                server.broadcast(GREEN + "God: no one died" + RESET, server.getClientThreads());
            }
        }
        isInvulnerableAttempt = false;
    }

    /**
     * Mayor time.
     * wake mayor up then wait it
     */
    public void mayorTime(){

        server.broadcast(CYAN + "God: Mayor, do yo wanna cancel the voting ? you have "
                + citizenNightTime + " seconds!" + RESET, server.getClientThreads());

        ArrayList<Server.ClientThread> mayor
                = new ArrayList<>();

        for(Server.ClientThread ct : server.getClientThreads()){
            if(connectClientToRole.get(ct) instanceof Mayor){
                mayor.add(ct);
                synchronized (ct) {
                    ct.setWait(false);
                    ct.notify();
                }
            }
        }

        if(mayor.size() > 0) {
            server.setActiveClients(mayor);
            server.broadcast(MAGENTA + "God: if you want to cancel the result, send -> [ @!cancel ] " + RESET,
                    server.getActiveClients());
        }

        sleep(citizenNightTime);
        server.broadcast(BLUE + "God: End of mayor time" + RESET, server.getClientThreads());
        waitAllClients();
        isMayorAttempt = false;
        server.setActiveClients(server.getClientThreads());
    }

    /**
     * Ready.
     * increase readyToGo by 1
     */
    public void ready() {
        readyToGo += 1;
    }

    /**
     * Wait until full.
     * wait the game till all clients are ready
     */
    public void waitUntilFull(){

        server.broadcast(CYAN + "God: say [!ready] to continue" + RESET, server.getClientThreads());
        server.setWaitingToGo(true);

        while(true){
            sleep(2);
            if (readyToGo >= server.getMaxCapacity() - deadClients.size() ) {
                readyToGo = 0;
                server.setWaitingToGo(false);
                break;
            }
        }

        for(Server.ClientThread ct : server.getClientThreads()){
            ct.setReady(false);
        }
    }

    /**
     * Collect dead clients.
     */
    public void collectDeadClients(){

        boolean isHereBefore = false;

        for(Server.ClientThread ct : server.getClientThreads()){
            Player player = connectClientToRole.get(ct);
            if(!player.isAlive()){
                for(Server.ClientThread clientThread : deadClients){
                    if (clientThread.equals(ct)) {
                        isHereBefore = true;
                        break;
                    }
                }
                if(!isHereBefore) {
                    deadClients.add(ct);
                    presentLastMoment(ct);
                }
                isHereBefore = false;
            }
        }
    }

    /**
     * Present last moment.
     * client can decide to watch the game or logout
     * @param ct the ct
     */
    public synchronized void presentLastMoment(Server.ClientThread ct){
        if(!ct.isLastMoment()) {
            ct.setLastMoment(true);
            ct.setWait(false);
            ct.setCanTalk(false);
            synchronized (ct) {
                ct.notify();
            }
            ct.writeMsg(YELLOW + "you are watching the game -> To logout enter [ !logout ]" + RESET);
        }
    }

    /**
     * Day time.
     * day time chatroom & voting
     */
    public void dayTime(){

        long start = System.currentTimeMillis();
        long end = start + dayChatTime * 1000;

        server.broadcast(CYAN + "God: it's day again talk for " + dayChatTime + " seconds!" + RESET,
                server.getClientThreads());
        server.broadcast(CYAN + "God: to VOTE send -> [ !vote<space>@username ] " + RESET,
                server.getClientThreads());
        server.broadcast(CYAN + "God: to Skip Day send -> [ !READY ] " + RESET,
                server.getClientThreads());
        notifyAllClients();

        while(System.currentTimeMillis() < end){
            sleep(1);
            if(readyToGo >= server.getMaxCapacity() - deadClients.size()){
                server.broadcast(BLUE + "God: Day ended early" + RESET , server.getClientThreads());
                sleep(2);
                break;
            }

        }
        server.broadcast(BLUE + "God: Day ends" + RESET, server.getClientThreads());
        waitAllClients();

    }

    /**
     * First day.
     */
    public void firstDay(){
        firstDayChat();
        waitUntilFull();

        //day chat
        server.broadcast(CYAN + "God: chat for " + firstDayChatTime + " seconds!" + RESET
                , server.getClientThreads());
        sleep(firstDayChatTime);
        waitAllClients();
        server.broadcast(BLUE + "God: Day ends" + RESET, server.getClientThreads());
    }

    /**
     * Apply voting.
     * if mayor didn't canceled voting this method apply voting and the votes
     */
    public void applyVoting(){

        Server.ClientThread deadClient = server.getClientThreads().get(0);
        boolean noOneIsOut = false;

        for(Server.ClientThread ct : server.getClientThreads()){
            if(ct.getVotes().size() > deadClient.getVotes().size()){
                deadClient = ct;
            }
        }

        connectClientToRole.get(deadClient).setAlive(false);

        for(Server.ClientThread ct : server.getClientThreads()){
            if(ct.getVotes().size() == deadClient.getVotes().size() && !ct.equals(deadClient)){
                connectClientToRole.get(deadClient).setAlive(true);
                noOneIsOut = true;
            }
        }

        if(noOneIsOut){
            server.broadcast(GREEN + "God: no one is out of the game" + RESET,
                    server.getClientThreads());
        }else{
            server.broadcast(YELLOW + "God: " + deadClient.getUsername() + " is out of the game" + RESET,
                    server.getClientThreads());
            deadClients.add(deadClient);
            presentLastMoment(deadClient);
            announceDeadClients();
        }

    }

    /**
     * Check end boolean.
     * check if game reach end or not
     * @return the boolean
     */
    public boolean checkEnd(){
        Player player;
        int mafiaCount = 0, citizenCount = 0;
        for(Server.ClientThread ct : server.clientThreads){
            player = connectClientToRole.get(ct);
            if(player.isAlive()){
                if(player instanceof MafiaTeam){
                    mafiaCount++;
                }else{
                    citizenCount++;
                }
            }
        }

        if(mafiaCount == 0){
            keepGoing = false;
            server.broadcast(GREEN + "God: Citizen's Won! THE END!" + RESET, server.getClientThreads());
            return true;
        }
        if(mafiaCount == citizenCount){
            keepGoing = false;
            server.broadcast(RED + "God: Mafia's Won! THE END!" + RESET, server.getClientThreads());
            return true;
        }
        return false;
    }

    /**
     * Announce dead clients.
     */
    public void announceDeadClients(){
        server.broadcast(BLACK + "God : List of players witch are out of the game from start: " + RESET
                , server.getClientThreads());
        for(Server.ClientThread ct : deadClients){
            server.broadcast("-> " + WHITE + ct.getUsername() + RESET, server.getClientThreads());
            sleep(1);
        }
    }

    /**
     * Game.
     * this manage the whole game procedure
     */
    public synchronized void game() {

        server.setActiveClients(server.getClientThreads());

        createPlayers();
        giveRoles();

        firstNight();
        sleep(5);

        firstDay();
        sleep(5);

        while(keepGoing) {

            mafiaNight();
            sleep(5);

            doctorNight();
            sleep(5);

            detectiveNight();
            sleep(5);

            professionalNight();
            sleep(5);

            invulnerableNight();
            sleep(5);

            psychologistNight();
            sleep(5);


            collectDeadClients();
            announceDeadClients();
            waitAllClients();

            if(checkEnd()){
                continue;
            }

            invulnerableAct();

            dayTime();
            sleep(5);

            collectVotes();
            announceVotes();

            mayorTime();
            sleep(5);

            if(!votingHasBeenCanceled){
                applyVoting();
            }

            collectDeActives();
            waitAllClients();

            if(checkEnd()){
                continue;
            }
            readyToGo = 0;
            votingHasBeenCanceled = false;
            protectedByLector = null;

            waitAllClients();

            for(Server.ClientThread ct : server.getClientThreads()){
                ct.setVote(null);
                ct.setVotes(new ArrayList<>());
                ct.setReady(false);
                if(connectClientToRole.get(ct).isAlive()) {
                    ct.setCanTalk(true);
                }
            }

        }
/*
        sleep(3);
        for(Server.ClientThread ct : server.getClientThreads()){
            ct.setKeepGoing(false);
        }
        server.setKeepGoing(false);

 */

    }

}