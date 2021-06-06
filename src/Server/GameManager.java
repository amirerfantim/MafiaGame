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
    //private HashMap<Server.ClientThread, MafiaTeam> mafiaTeam = new HashMap<>();
    //private HashMap<Server.ClientThread, CitizenTeam> citizenTeam = new HashMap<>();
    private Server server;
    private int readyToGo = 0;
    private int targetToGo = 0;

    private final int dayChatTime = 25, mafiaNight = 25;
    private boolean isGodFatherShot = false, isLectorDoctorhill = false;


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

    public void mafiaShot(String usernameToFind) {

        Player player = null;

        if (!isGodFatherShot) {
            for (Server.ClientThread ct : clientThreads) {
                if (usernameToFind.equals(ct.getUsername())) {
                    player = connectClientToRole.get(ct);
                    server.broadcast("God: Nice Shot! sleep!", server.getActiveClients());
                    player.setAlive(false);
                }
            }if(player == null) {
                server.broadcast("God: there isn't any client with this username", server.getActiveClients());
            }
        }else{
        }
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

    public boolean ready() {
        readyToGo++;
        return readyToGo == targetToGo;
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
        server.broadcast("God: say [!ready] to continue", server.getClientThreads());
        targetToGo = server.getMaxCapacity();
        server.setWaitingToGo(true);
        firstDayChat();

        while(true){
            sleep(1);
            if (readyToGo == targetToGo) {

                readyToGo = 0;
                server.setWaitingToGo(false);
                break;
            }
        }
        //day chat
        server.broadcast("God: chat for " + dayChatTime + " seconds!", server.getClientThreads());
        sleep(dayChatTime);
        waitAllClients();
        server.broadcast("God: Day ends" , server.getClientThreads());
        sleep(5);




        //mafia night shot & chat & lectorDoctor
        server.broadcast("God: Night begins" , server.getClientThreads());
        server.broadcast("God: Mafia Team chat & kill some one within " + mafiaNight + " seconds!"
                , server.getClientThreads());
        server.setActiveClients(mafiaClients);
        server.broadcast("God: to kill someone, GodFather send -> [ @<username> ] ", server.getActiveClients());
        server.broadcast("God: to hill someone, LectorDoctor send -> [ @<username> ] ", server.getActiveClients());
        wakeMafiaUp();
        sleep(mafiaNight);
        waitAllClients();






    }
}