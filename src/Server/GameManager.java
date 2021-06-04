package Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class GameManager {

    private int numberOfPlayers, numberOfMafias, numberOfCitizens;
    private ArrayList<Server.ClientThread> clientThreads;
    private ArrayList<Player> players = new ArrayList<>();
    private HashMap<Server.ClientThread, Player> connectClientToRole = new HashMap<>();
    //private HashMap<Server.ClientThread, MafiaTeam> mafiaTeam = new HashMap<>();
    //private HashMap<Server.ClientThread, CitizenTeam> citizenTeam = new HashMap<>();
    private Server server;
    private int readyToGo = 0;

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
            players.add(new LecterDoctor());
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
            } else if (curPlayer instanceof LecterDoctor) {
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
                clientThread.notify();
            }
        }
    }

    public boolean ready() {
        readyToGo++;
        return readyToGo == server.getMaxCapacity();
    }


    public synchronized void game() {
        createPlayers();
        giveRoles();
        firstNight();

        sleep(5);
        server.broadcast("say [ready] to continue");
        server.setWaitingToGo(true);
        firstDayChat();
        while(true){
            sleep(1);
            if (readyToGo == server.getMaxCapacity()) {
                server.broadcast("Lets go!");
                break;
            }
        }


    }
}