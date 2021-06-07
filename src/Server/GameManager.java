package Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class GameManager {

    private int numberOfPlayers, numberOfMafias, numberOfCitizens;
    private ArrayList<Server.ClientThread> clientThreads;
    private ArrayList<Server.ClientThread> mafiaClients = new ArrayList<>();
    private ArrayList<Player> players = new ArrayList<>();
    private HashMap<Server.ClientThread, Player> connectClientToRole = new HashMap<>();
    private HashMap<Server.ClientThread , Player> deadClients = new HashMap<>();
    //private HashMap<Server.ClientThread, MafiaTeam> mafiaTeam = new HashMap<>();
    //private HashMap<Server.ClientThread, CitizenTeam> citizenTeam = new HashMap<>();
    private Server server;
    private int readyToGo = 0;
    private int targetToGo = 0;

    private final int dayChatTime = 25, mafiaNightTime = 45, citizenNightTime = 45;
    private boolean isGodFatherShot = false, isLectorDoctorHill = false, isDoctorHill = false,
                    isDetectiveAttempt = false, isProfessionalShot = false,
                    isPsychologistMuted = false;
    private Server.ClientThread protectedByLector = null;


    public GameManager(int numberOfPlayers, ArrayList<Server.ClientThread> clientThreads, Server server) {
        this.numberOfPlayers = numberOfPlayers;
        this.clientThreads = clientThreads;
        this.server = server;
        numberOfMafias = numberOfPlayers / 3;
        numberOfCitizens = numberOfPlayers - numberOfMafias;
    }

    public int getReadyToGo() {
        return readyToGo;
    }

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
                players.add(new Mayor());
            }
            case 6: {
                players.add(new Professional());
            }
            case 5: {
                players.add(new Psychologist());
            }
            case 4: {
                players.add(new Invulnerable());
            }
            case 3: {
                players.add(new Detective());
            }
            case 2: {
                players.add(new Doctor());
            }
            case 1: {
                players.add(new SimpleCitizen());
            }
        }
        for (int i = 0; i < numberOfCitizens - 7; i++) {
            players.add(new SimpleCitizen());
        }

    }

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

    public void firstDayChat() {

        for (Server.ClientThread clientThread : clientThreads) {
            clientThread.start();
        }

    }

    public void firstNight() {

        for (Server.ClientThread clientThread : clientThreads) {
            Player curPlayer = connectClientToRole.get(clientThread);

            if (curPlayer instanceof SimpleMafia) {
                server.sendMsgToClient("God : You are Simple Mafia", clientThread);
                showMafiaTeam(clientThread);
            } else if (curPlayer instanceof GodFather) {
                server.sendMsgToClient("God : You are God Father", clientThread);
                showMafiaTeam(clientThread);
            } else if (curPlayer instanceof LectorDoctor) {
                server.sendMsgToClient("God : You are Lecter Doctor", clientThread);
                showMafiaTeam(clientThread);
            } else if (curPlayer instanceof SimpleCitizen) {
                server.sendMsgToClient("God : You are Simple citizen", clientThread);
            } else if (curPlayer instanceof Doctor) {
                server.sendMsgToClient("God : You are Doctor", clientThread);
            } else if (curPlayer instanceof Detective) {
                server.sendMsgToClient("God : You are Detective", clientThread);
            } else if (curPlayer instanceof Invulnerable) {
                server.sendMsgToClient("God : You are Invulnerable", clientThread);
            } else if (curPlayer instanceof Mayor) {
                server.sendMsgToClient("God : You are Mayor", clientThread);
                for(Server.ClientThread ct : server.getClientThreads()){
                    if(connectClientToRole.get(ct) instanceof Doctor){
                        server.sendMsgToClient("God : " + ct.getUsername() + " is Doctor!", clientThread);
                    }
                }
            } else if (curPlayer instanceof Professional) {
                server.sendMsgToClient("God : You are Professional", clientThread);
            } else if (curPlayer instanceof Psychologist) {
                server.sendMsgToClient("God : You are Psychologist", clientThread);
            }


        }

    }

    public void showMafiaTeam(Server.ClientThread ctToSend) {
        int row = 1;

        server.sendMsgToClient("The Mafia Team is: ", ctToSend);

        for (Server.ClientThread clientThread : clientThreads) {
            if (connectClientToRole.get(clientThread) instanceof MafiaTeam) {
                server.sendMsgToClient(row + ": " + clientThread.getUsername()
                        + " -> " + connectClientToRole.get(clientThread), ctToSend);
                row++;
            }
        }
    }

    public Player getPlayer(Server.ClientThread clientThread) {
        return connectClientToRole.get(clientThread);
    }

    public void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            System.out.println("error during sleep");
            e.printStackTrace();
        }
    }

    public synchronized void notifyAllClients() {
        for (Server.ClientThread clientThread : server.clientThreads) {
            synchronized (clientThread) {
                clientThread.setWait(false);
                clientThread.notify();
            }
        }
    }

    public synchronized void notifySomeClients(ArrayList<Server.ClientThread> cts) {
        for (Server.ClientThread clientThread : cts) {
            if(!clientThread.isDead())
            synchronized (clientThread) {
                clientThread.setWait(false);
                clientThread.notify();
            }
        }
    }

    public synchronized void waitAllClients() {
        for (Server.ClientThread clientThread : server.clientThreads) {
            clientThread.setWait(true);
        }
    }

    public synchronized void wakeMafiaUp() {
        for (Server.ClientThread clientThread : server.clientThreads) {
            if (connectClientToRole.get(clientThread) instanceof MafiaTeam) {
                synchronized (clientThread) {
                    clientThread.setWait(false);
                    clientThread.notify();
                }
            }
        }
    }

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
                    if(player instanceof CitizenTeam) {
                        server.broadcast("God: Nice Shot!", server.getActiveClients());
                        isGodFatherShot = true;
                        player.setAlive(false);

                        if(player instanceof Invulnerable ){
                            if(!((Invulnerable) player).isHasBeenShot()) {
                                ((Invulnerable) player).setHasBeenShot(true);
                                player.setAlive(true);
                            }
                        }

                    }else{
                        server.broadcast("God: you cant shot a mafia!", server.getActiveClients());
                    }
                }
            }if(player == null) {
                server.broadcast("God: there isn't any client with this username", server.getActiveClients());
            }
        }else{
            server.broadcast("God: you shot before", server.getActiveClients());
        }

        server.setActiveClients(activeClients);

    }

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
                    if(player instanceof MafiaTeam) {
                        server.broadcast("God: you protected " + ct.getUsername(), server.getActiveClients());
                        isLectorDoctorHill = true;
                        protectedByLector = ct;
                    }else{
                        server.broadcast("God: you cant hill a citizen!", server.getActiveClients());
                    }
                }
            }if(player == null) {
                server.broadcast("God: there isn't any client with this username", server.getActiveClients());
            }
        }else{
            server.broadcast("God: you hill before", server.getActiveClients());
        }

        server.setActiveClients(activeClients);

    }

    public void doctorHill(String usernameToFind, Server.ClientThread doctorCt){
        Player player = null;
        ArrayList<Server.ClientThread> doctor = new ArrayList<>();
        ArrayList<Server.ClientThread> activeClients = server.getActiveClients();
        doctor.add(doctorCt);
        server.setActiveClients(doctor);

        if (!isDoctorHill) {
            for (Server.ClientThread ct : clientThreads) {
                if (usernameToFind.equals(ct.getUsername())) {
                    player = connectClientToRole.get(ct);
                    server.broadcast("God: you protected " + ct.getUsername(), server.getActiveClients());
                    isLectorDoctorHill = true;

                }
            }if(player == null) {
                server.broadcast("God: there isn't any client with this username", server.getActiveClients());
            }
        }else{
            server.broadcast("God: you hill before", server.getActiveClients());
        }

        server.setActiveClients(activeClients);

    }

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
                    if(player instanceof CitizenTeam || player instanceof GodFather) {
                        server.broadcast("God: " + ct.getUsername() + " is in Citizen's team"
                                , server.getActiveClients());
                    }else {
                        server.broadcast("God: " + ct.getUsername() + " is in Mafia's team"
                                , server.getActiveClients());
                    }

                    isDetectiveAttempt = true;
                }
            }if(player == null) {
                server.broadcast("God: there isn't any client with this username", server.getActiveClients());
            }
        }else{
            server.broadcast("God: you attempted before", server.getActiveClients());
        }

        server.setActiveClients(activeClients);

    }

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
                    server.broadcast("God: Nice Shot!", server.getActiveClients());
                    isProfessionalShot = true;
                    if(player instanceof MafiaTeam) {
                        player.setAlive(false);
                        if(ct.equals(protectedByLector)){
                            player.setAlive(true);
                        }
                    }else{
                        connectClientToRole.get(professionalCT).setAlive(false);
                    }
                }
            }if(player == null) {
                server.broadcast("God: there isn't any client with this username", server.getActiveClients());
            }
        }else{
            server.broadcast("God: you shot before", server.getActiveClients());
        }

        server.setActiveClients(activeClients);

    }

    public void psychologistAttempt(String usernameToFind, Server.ClientThread psychologistCT) {

        Player player = null;
        ArrayList<Server.ClientThread> psychologist = new ArrayList<>();
        ArrayList<Server.ClientThread> activeClients = server.getActiveClients();
        psychologist.add(psychologistCT);
        server.setActiveClients(psychologist);

        if (!isPsychologistMuted) {
            for (Server.ClientThread ct : clientThreads) {
                if (usernameToFind.equals(ct.getUsername())) {
                    player = connectClientToRole.get(ct);
                    server.broadcast("God: Well Done!", server.getActiveClients());
                    isPsychologistMuted = true;
                    player.setCanTalk(false);
                }
            }if(player == null) {
                server.broadcast("God: there isn't any client with this username", server.getActiveClients());
            }
        }else{
            server.broadcast("God: you attempt before", server.getActiveClients());
        }

        server.setActiveClients(activeClients);

    }

    public void mafiaNight(){

        server.broadcast("God: Night begins" , server.getClientThreads());
        server.broadcast("God: Mafia Team chat & kill some one within " + mafiaNightTime + " seconds!"
                , server.getClientThreads());

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
            server.broadcast("God: to kill someone, GodFather send -> [ @<username> ] ",
                    server.getActiveClients());
            server.broadcast("God: to hill someone, LectorDoctor send -> [ @<username> ] ",
                    server.getActiveClients());
        }

        sleep(mafiaNightTime);
        server.broadcast("God: Mafias go to sleep", server.getClientThreads());
        waitAllClients();
        isGodFatherShot = false;
        isLectorDoctorHill =false;
        //protectedByLector = null;
        server.setActiveClients(server.getClientThreads());
    }

    public void doctorNight(){

        server.broadcast("God: Doctor wakeUp & hill someone within " + citizenNightTime + " seconds!"
                , server.getClientThreads());

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
            server.broadcast("God: to hill someone, send -> [ @<username> ] ", server.getActiveClients());
        }

        sleep(citizenNightTime);
        server.broadcast("God: Doctor go to sleep", server.getClientThreads());
        waitAllClients();
        isDoctorHill = false;
        server.setActiveClients(server.getClientThreads());
    }

    public void detectiveNight(){

        server.broadcast("God: Detective wakeUp & you have " + citizenNightTime + " seconds!"
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
            server.broadcast("God: to know a role of someone, send -> [ @<username> ] ",
                    server.getActiveClients());
        }

        sleep(citizenNightTime);
        server.broadcast("God: Detective go to sleep", server.getClientThreads());
        waitAllClients();
        isDoctorHill = false;
        server.setActiveClients(server.getClientThreads());
    }

    public void professionalNight(){

        server.broadcast("God: Professional wakeUp " + citizenNightTime + " seconds!"
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
            server.broadcast("God: if you want to shoot someone, send -> [ @<username> ] ",
                    server.getActiveClients());
        }

        sleep(citizenNightTime);
        server.broadcast("God: Professional go to sleep", server.getClientThreads());
        waitAllClients();
        isProfessionalShot = false;
        server.setActiveClients(server.getClientThreads());
    }

    public void psychologistNight(){

        server.broadcast("God: Psychologist wakeUp " + citizenNightTime + " seconds!"
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
            server.broadcast("God: if you want to mute someone, send -> [ @<username> ] ",
                    server.getActiveClients());
        }

        sleep(citizenNightTime);
        server.broadcast("God: Psychologist go to sleep", server.getClientThreads());
        waitAllClients();
        isPsychologistMuted = false;
        server.setActiveClients(server.getClientThreads());
    }

    public Server.ClientThread findClient(String usernameToFind){

        for(Server.ClientThread ct :clientThreads){
            if(usernameToFind.equals(ct.getUsername())){
                return ct;
            }
        }
        server.broadcast( "God: Player not found", server.getClientThreads());
        return null;
    }

    public void ready() {
        readyToGo++;
    }

    public void waitUntilFull(int targetToGo){

        server.broadcast("God: say [!ready] to continue", server.getClientThreads());
        server.setWaitingToGo(true);

        while(true){
            sleep(1);
            if (readyToGo == targetToGo) {

                readyToGo = 0;
                server.setWaitingToGo(false);
                break;
            }
        }
    }

    public void collectDeadClients(){
        for(Server.ClientThread ct : server.getClientThreads()){
            Player player = connectClientToRole.get(ct);
            if(!player.isAlive()){
                deadClients.put(ct, player);
            }
        }
    }

    public synchronized void presentLastMoment(HashMap<Server.ClientThread, Player> players){

        for(Server.ClientThread ct : players.keySet()){
            ct.setLastMoment(true);
            ct.setWait(false);
            synchronized (ct){
                ct.notify();
            }
            ct.writeMsg("To Watch the game enter [ !watch ]\nTo logout enter [ !logout ]");
        }

    }


    /*
    public void getReady(int target){
        server.broadcast("God: say [ready] to continue");
        targetToGo = target;
        server.setWaitingToGo(true);
        firstDayChat();

        while(true){
            sleep(1);
            if (readyToGo == server.getMaxCapacity()) {
                readyToGo = 0;
                server.setWaitingToGo(false);
                break;
            }
        }
    }

     */

    public synchronized void game() {

        server.setActiveClients(server.getClientThreads());

        createPlayers();
        giveRoles();
        firstNight();
        sleep(5);

        firstDayChat();
        waitUntilFull(server.getClientThreads().size());

        //day chat
        server.broadcast("God: chat for " + dayChatTime + " seconds!", server.getClientThreads());
        sleep(dayChatTime);
        waitAllClients();
        server.broadcast("God: Day ends" , server.getClientThreads());
        sleep(5);


        //mafia night shot & chat & lectorDoctor
        mafiaNight();
        sleep(5);

        doctorNight();
        sleep(5);

        detectiveNight();
        sleep(5);

        professionalNight();
        sleep(5);

        psychologistNight();
        sleep(5);

        collectDeadClients();
        presentLastMoment(deadClients);

        for(Server.ClientThread ct : deadClients.keySet()){
            System.out.println(ct.getUsername() + " -> " + connectClientToRole.get(ct).toString());
        }


    }

}