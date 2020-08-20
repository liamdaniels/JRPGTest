package com.mygdx.game;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * IDEA:
 * manages all the players
 * can load them from files
 * "currently in party"?
 */
public class PlayerManager {


    public final String INFOFILE = "battle/humanstats.csv";
    //party
    private BattlePlayer p1;
    private BattlePlayer p2;
    private BattlePlayer p3;
    private BattlePlayer p4;


    private BattlePlayer[] allPlayers;

    public static String[] playerNames;


    public PlayerManager(ItemManager itemManager, SpellManager spellManager){
        //loading all possibly players that game could have

        try {
            BufferedReader buff = new BufferedReader(new FileReader(INFOFILE));

            //first line is header
            buff.readLine();

            //prob not more than 12 playable characters
            int MAX_PLAYERS = 12;

            allPlayers = new BattlePlayer[MAX_PLAYERS];
            playerNames = new String[MAX_PLAYERS];

            //load new players until can't
            String currentPlayerLine = buff.readLine();
            for(int index = 0; currentPlayerLine != null; index++){

                String[] current = currentPlayerLine.split(",");

                //Here is the order of things in the file:
                //NAME, Imgfile, option1, option2, option3, option4, level, exp, vital, big brain, sharp, hard, Chunk, strength, speed
                String playerName = current[0];
                String playerImage = current[1];
                String[] options = new String[4];
                for (int i = 0; i < 4; i++){
                    options[i] = current[i + 2];
                }

                int level = Integer.parseInt(current[6]);
                int exp = Integer.parseInt(current[7]);

                int vitality = Integer.parseInt(current[8]);
                int bigBrain = Integer.parseInt(current[9]);
                int sharpness = Integer.parseInt(current[10]);
                int hardness = Integer.parseInt(current[11]);
                int chunk = Integer.parseInt(current[12]);
                int strength = Integer.parseInt(current[13]);
                int speed = Integer.parseInt(current[14]);

                int baseJob = Integer.parseInt(current[15]);
                boolean canSwitch = Boolean.parseBoolean(current[16]);


                allPlayers[index] = new BattlePlayer(playerName, playerImage, options, level, exp, vitality, bigBrain,
                        sharpness, hardness, chunk, strength, speed, baseJob, canSwitch);
                allPlayers[index].setManagers(itemManager,spellManager);
                playerNames[index] = playerName;


                currentPlayerLine = buff.readLine();
            }


            buff.close();

        }
        catch (IOException e){
            System.out.println("io exception in player manager :(");
        }

    }


    public ArrayList<BattlePlayer> getParty(){
        ArrayList<BattlePlayer> partyAL = new ArrayList<BattlePlayer>();
        if(p1 != null){
            partyAL.add(p1);
        }
        if(p2 != null){
            partyAL.add(p2);
        }
        if(p3 != null){
            partyAL.add(p3);
        }
        if(p4 != null){
            partyAL.add(p4);
        }

        return partyAL;


    }


    public BattlePlayer searchByName(String name){

        if (p1 != null && p1.getName().equals(name)){
            return p1;
        }
        else if (p2 != null && p2.getName().equals(name)){
            return p2;
        }
        else if (p3 != null && p3.getName().equals(name)){
            return p3;
        }
        else if (p4 != null && p4.getName().equals(name)){
            return p4;
        }
        else{
            System.out.println("name not found in party!");
            return null;
        }
    }

    public String[] getPartyNames(){
        ArrayList<BattlePlayer> party = getParty();
        String[] names = new String[party.size()];
        for (int i = 0; i < party.size(); i++){
            names[i] = party.get(i).getName();
        }
        return names;
    }


    public void addMember(int dataMember){

        BattlePlayer member = allPlayers[dataMember];

        //check available slots
        if (p1 == null){
            setP1(member);

        }else if (p2 == null){
            setP2(member);

        }else if (p3 == null){
            setP3(member);

        }else if (p4 == null){
            setP4(member);

        }else{
            throw new RuntimeException("added player with no room");
        }


    }

    public void removeMember(int partyMember){
        switch (partyMember){
            case 1:
                p1 = null;
                break;
            case 2:
                p2 = null;
                break;
            case 3:
                p3 = null;
                break;
            case 4:
                p4 = null;
                break;
            default:
                break;

        }
    }

    public void setP1(BattlePlayer b){
        p1 = b;
    }
    public void setP2(BattlePlayer b){
        p2 = b;
    }
    public void setP3(BattlePlayer b){
        p3 = b;
    }
    public void setP4(BattlePlayer b){
        p4 = b;
    }

    public void dispose(){
        for (BattlePlayer p : allPlayers){
            if (p != null){ p.dispose();}
        }
    }

}
