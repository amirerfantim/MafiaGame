package Server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;

public class GameManager {

    private int numberOfPlayers, numberOfMafias, numberOfCitizens;
    private ArrayList<Server.ClientThread> clientThreads = new ArrayList<>();
    private ArrayList<Player> players = new ArrayList<>();
    private HashMap<Server.ClientThread, Player> connectClientToRole = new HashMap<>();
    private Server server;

    public GameManager(int numberOfPlayers, ArrayList<Server.ClientThread> clientThreads, Server server) {
        this.numberOfPlayers = numberOfPlayers;
        this.clientThreads = clientThreads;
        this.server = server;
        numberOfMafias = numberOfPlayers / 3;
        numberOfCitizens = numberOfPlayers - numberOfMafias;
    }

    public void createPlayers(){

        if (numberOfMafias == 1) {
            players.add(new GodFather());
        } else {
            players.add(new LecterDoctor());
            players.add(new GodFather());

            for (int i = 0; i < numberOfMafias - 2; i++) {
                players.add(new SimpleMafia());
            }
        }

        switch (numberOfCitizens){
            case 7:{
                players.add(new Mayor());
            }
            case 6:{
                players.add(new Professional());
            }
            case 5:{
                players.add(new Psychologist());
            }
            case 4:{
                players.add(new Invulnerable());
            }
            case 3:{
                players.add(new Detective());
            }
            case 2:{
                players.add(new Doctor());
            }
            case 1:{
                players.add(new SimpleCitizen());
            }
        }
        for(int i = 0 ; i < numberOfCitizens - 7 ; i++){
            players.add(new SimpleCitizen());
        }

    }

    public void giveRoles(){
        Collections.shuffle(clientThreads);
        createPlayers();
        int i = 0;

        for(Server.ClientThread clientThread : clientThreads){
                connectClientToRole.put(clientThread, players.get(i));
                i++;
        }
        for(Server.ClientThread clientThread : clientThreads){
            System.out.println(clientThread.getUsername() + " -> " + connectClientToRole.get(clientThread) );
        }


    }

    public void oneDay(){

        for (Server.ClientThread clientThread : clientThreads) {
            clientThread.start();
        }

    }

}
