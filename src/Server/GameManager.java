package Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

// lector doctor 1 time himself +
// ready!
// username
// cant hill or kill already dead players +
// muted can vote +
// announce died  in night & muted +
// shuffle role inverulabele +
// cant vote dead people +
// player can vote at night
// chat file
// can skip day


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
    private int readyToGo = 0, votedSoFar = 0;
    private int targetToGo = 0;

    private final int firstDayChatTime = 20, mafiaNightTime = 30, citizenNightTime = 20, dayChatTime = 100;

    private boolean isGodFatherShot = false, isLectorDoctorHill = false, isDoctorHill = false,
                    isDetectiveAttempt = false, isProfessionalShot = false,
                    isPsychologistMuted = false, isInvulnerableAttempt  =false,
                    isMayorAttempt = false, isLectorHillItself = false;
    private int numberOfInvulnerableAttempt = 0;
    private Server.ClientThread protectedByLector = null;
    private boolean votingHasBeenCanceled = false, keepGoing = true;


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
        for (Server.ClientThread clientThread : server.getClientThreads()) {
            synchronized (clientThread) {
                clientThread.setWait(false);
                clientThread.notify();
            }
        }
    }

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

    public boolean isClientDied(Server.ClientThread ct){
            for(Server.ClientThread obj : deadClients){
                if(obj.equals(ct)){
                    return true;
                }
           }
            return false;
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
                    if(!isClientDied(ct)) {
                        if (player instanceof CitizenTeam) {
                            server.broadcast("God: Nice Shot!", server.getActiveClients());
                            isGodFatherShot = true;
                            player.setAlive(false);

                            if (player instanceof Invulnerable) {
                                if (!((Invulnerable) player).isHasBeenShot()) {
                                    ((Invulnerable) player).setHasBeenShot(true);
                                    player.setAlive(true);
                                }
                            }

                        } else {
                            server.broadcast("God: you cant shot a mafia!", server.getActiveClients());
                        }
                    }else {
                        server.broadcast("God: Player is already dead!", server.getActiveClients());
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
                    if(!isClientDied(ct)) {
                        if (player instanceof MafiaTeam) {
                            if(player instanceof LectorDoctor ){
                                if(!isLectorHillItself) {
                                    isLectorHillItself = true;
                                    server.broadcast("God: you protected yourself",
                                            server.getActiveClients());
                                    isLectorDoctorHill = true;
                                    protectedByLector = ct;
                                }else{
                                    server.broadcast("God: you hill yourself once",
                                            server.getActiveClients());
                                }
                            }else{
                                server.broadcast("God: you protected " + ct.getUsername(),
                                        server.getActiveClients());
                                isLectorDoctorHill = true;
                                protectedByLector = ct;
                            }
                        } else {
                            server.broadcast("God: you cant hill a citizen!", server.getActiveClients());
                        }
                    }else{
                        server.broadcast("God: Player is already dead!", server.getActiveClients());
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
                    if(!isClientDied(ct)) {
                        player = connectClientToRole.get(ct);
                        player.setAlive(true);
                        server.broadcast("God: you protected " + ct.getUsername(), server.getActiveClients());
                        isLectorDoctorHill = true;
                    }else{
                        server.broadcast("God: Player is already dead!", server.getActiveClients());
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
                            server.broadcast("God: " + ct.getUsername() + " is in Citizen's team"
                                    , server.getActiveClients());
                        } else {
                            server.broadcast("God: " + ct.getUsername() + " is in Mafia's team"
                                    , server.getActiveClients());
                        }
                        isDetectiveAttempt = true;
                    }else{
                        server.broadcast("God: Player is already dead!", server.getActiveClients());
                    }
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
                    if(!isClientDied(ct)) {
                        server.broadcast("God: Nice Shot!", server.getActiveClients());
                        isProfessionalShot = true;
                        if (player instanceof MafiaTeam) {
                            player.setAlive(false);
                            if (ct.equals(protectedByLector)) {
                                player.setAlive(true);
                            }
                        } else {
                            connectClientToRole.get(professionalCT).setAlive(false);
                        }
                    }else{
                        server.broadcast("God: Player is already dead!", server.getActiveClients());
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
                        server.broadcast("God: Well Done!", server.getActiveClients());
                        server.broadcast("God: " + ct.getUsername() + " muted!", server.getClientThreads());
                        isPsychologistMuted = true;
                        ct.setCanTalk(false);
                    }else{
                        server.broadcast("God: Player is already dead!", server.getActiveClients());
                    }
                }
            }if(!found) {
                server.broadcast("God: there isn't any client with this username", server.getActiveClients());
            }
        }else{
            server.broadcast("God: you attempt before", server.getActiveClients());
        }

        server.setActiveClients(activeClients);

    }

    public void invulnerableAttempt(Server.ClientThread invulnerableCT) {

        ArrayList<Server.ClientThread> invulnerable = new ArrayList<>();
        ArrayList<Server.ClientThread> activeClients = server.getActiveClients();
        invulnerable.add(invulnerableCT);
        server.setActiveClients(invulnerable);

        ArrayList<Server.ClientThread> shuffledDead  = deadClients;
        Collections.shuffle(shuffledDead);

        if(numberOfInvulnerableAttempt < 2) {
            if (!isInvulnerableAttempt) {
                if (shuffledDead.size() > 0) {
                    for (Server.ClientThread deadClient : shuffledDead) {
                        server.broadcast("-> " + connectClientToRole.get(deadClient).toString(),
                                server.getActiveClients());
                    }
                    numberOfInvulnerableAttempt += 1;
                } else {
                    server.broadcast("God: no one died yet!",
                            server.getActiveClients());
                }

                isInvulnerableAttempt = true;

            } else {
                server.broadcast("God: you attempt before", server.getActiveClients());
            }
        }else{
            server.broadcast("God: you have reached your limit ( 2times )", server.getActiveClients());
        }

        server.setActiveClients(activeClients);

    }

    public void mayorAttempt(Server.ClientThread mayorCT) {

        ArrayList<Server.ClientThread> mayor = new ArrayList<>();
        ArrayList<Server.ClientThread> activeClients = server.getActiveClients();
        mayor.add(mayorCT);
        server.setActiveClients(mayor);

        if (!isMayorAttempt) {
            votingHasBeenCanceled = true;
            isMayorAttempt = true;

        } else {
            server.broadcast("God: you attempt before", server.getActiveClients());
        }


        server.setActiveClients(activeClients);

    }

    public void vote(String usernameToFind, Server.ClientThread currentCt) {

        ArrayList<Server.ClientThread> psychologist = new ArrayList<>();
        ArrayList<Server.ClientThread> activeClients = server.getActiveClients();
        psychologist.add(currentCt);
        server.setActiveClients(psychologist);
        boolean found = false;

        if (!currentCt.isVoted()) {
            for (Server.ClientThread ct : clientThreads) {
                if (usernameToFind.equals(ct.getUsername())) {
                    found = true;
                    if(!isClientDied(ct)) {
                        server.broadcast("God: Well Done!", server.getActiveClients());
                        ct.setVotes(ct.getVotes() + 1);
                        votedSoFar += 1;
                        currentCt.setVoted(true);
                    }else{
                        server.broadcast("God: this client is dead!", server.getActiveClients());
                    }
                }
            }if(!found) {
                server.broadcast("God: there isn't any client with this username", server.getActiveClients());
            }
        }else{
            server.broadcast("God: you voted before", server.getActiveClients());
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

        server.broadcast("God: Detective wakeUp -> you have " + citizenNightTime + " seconds!"
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
        isDetectiveAttempt = false;
        server.setActiveClients(server.getClientThreads());
    }

    public void professionalNight(){

        server.broadcast("God: Professional wakeUp you have -> " + citizenNightTime + " seconds!"
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

        server.broadcast("God: Psychologist wakeUp you have -> " + citizenNightTime + " seconds!"
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

    public void invulnerableNight(){

        server.broadcast("God: invulnerable Up wakeUp -> you have " + citizenNightTime + " seconds!"
                , server.getClientThreads());

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
            server.broadcast("God: if you want to know the result, send -> [ @!result ] ",
                    server.getActiveClients());
        }

        sleep(citizenNightTime);
        server.broadcast("God: Invulnerable go to sleep", server.getClientThreads());
        waitAllClients();
        isInvulnerableAttempt = false;
        server.setActiveClients(server.getClientThreads());
    }

    public void mayorTime(){

        server.broadcast("God: Mayor, do yo wanna cancel the voting ? you have "
                + citizenNightTime + " seconds!", server.getClientThreads());

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
            server.broadcast("God: if you want to cancel the result, send -> [ @!cancel ] ",
                    server.getActiveClients());
        }

        sleep(citizenNightTime);
        server.broadcast("God: End of mayor time", server.getClientThreads());
        waitAllClients();
        isMayorAttempt = false;
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

        boolean isHereBefore = false;

        for(Server.ClientThread ct : server.getClientThreads()){
            Player player = connectClientToRole.get(ct);
            if(!player.isAlive()){
                for(Server.ClientThread clientThread : deadClients){
                    if(clientThread.equals(ct)){
                        isHereBefore = true;
                    }
                }
                if(!isHereBefore) {
                    deadClients.add(ct);
                }
            }
        }
    }

    public synchronized void presentLastMoment(ArrayList<Server.ClientThread> players){

        for(Server.ClientThread ct : players){
            if(!ct.isLastMoment()) {
                ct.setLastMoment(true);
                ct.setWait(false);
                synchronized (ct) {
                    ct.notify();
                }
                ct.writeMsg("you are watching the game -> To logout enter [ !logout ]");
            }
        }

    }

    public void dayTime(){

        long start = System.currentTimeMillis();
        long end = start + dayChatTime * 1000;

        server.broadcast("God: it's day again talk for " + dayChatTime + " seconds!",
                server.getClientThreads());
        server.broadcast("God: to VOTE send -> [ !vote<space>@username ] " + dayChatTime + " seconds!",
                server.getClientThreads());
        notifyAllClients();

        while(System.currentTimeMillis() < end){
            sleep(1);
            if(votedSoFar == server.getClientThreads().size() - deadClients.size()){
                server.broadcast("God: Day ended early" , server.getClientThreads());
                break;
            }
        }
        server.broadcast("God: Day ends" , server.getClientThreads());
        waitAllClients();

    }

    public void firstDay(){
        firstDayChat();
        //waitUntilFull(server.getClientThreads().size());

        //day chat
        server.broadcast("God: chat for " + firstDayChatTime + " seconds!", server.getClientThreads());
        sleep(firstDayChatTime);
        waitAllClients();
        server.broadcast("God: Day ends" , server.getClientThreads());
    }

    public void applyVoting(){

        Server.ClientThread deadClient = server.getClientThreads().get(0);
        boolean noOneIsOut = false;

        for(Server.ClientThread ct : server.getClientThreads()){
            if(ct.getVotes() > deadClient.getVotes()){
                deadClient = ct;
            }
        }

        connectClientToRole.get(deadClient).setAlive(false);

        for(Server.ClientThread ct : server.getClientThreads()){
            if(ct.getVotes() == deadClient.getVotes() && !ct.equals(deadClient)){
                connectClientToRole.get(deadClient).setAlive(true);
                noOneIsOut = true;
            }
        }

        if(!noOneIsOut){
            server.broadcast("God: " + deadClient.getUsername() + " is out of the game",
                    server.getClientThreads());
        }


    }

    public void checkEnd(){
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
            server.broadcast("God: Citizen's Won! THE END!", server.getClientThreads());
        }
        if(mafiaCount == citizenCount){
            keepGoing = false;
            server.broadcast("God: Mafia's Won! THE END!", server.getClientThreads());
        }
    }

    public void announceDeadClients(){
        server.broadcast("God : List of players witch are out of the game from start: "
                , server.getClientThreads());
        for(Server.ClientThread ct : deadClients){
            server.broadcast("-> " + ct.getUsername(), server.getClientThreads());
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
/*
            psychologistNight();
            sleep(5);

 */

            collectDeadClients();
            presentLastMoment(deadClients);
            announceDeadClients();

            checkEnd();

            dayTime();
            sleep(5);

            mayorTime();
            sleep(5);

            if(!votingHasBeenCanceled){
                applyVoting();
            }

            collectDeadClients();
            presentLastMoment(deadClients);

            checkEnd();

            votedSoFar = 0;
            votingHasBeenCanceled = false;
            protectedByLector = null;

            waitAllClients();

            for(Server.ClientThread ct : server.getClientThreads()){
                ct.setVoted(false);
                ct.setVotes(0);
                if(connectClientToRole.get(ct).isAlive()) {
                    ct.setCanTalk(true);
                }
            }

        }

    }

}