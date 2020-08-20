package com.mygdx.game;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.Buffer;

/**
 * Manages enemies
 * stores 4 slots where enemies go and fills them in when needed
 */
public class EnemyManager {

    private final int MAX_FOES = 4;
    private Enemy[] enemies;


    private final String INFOFILE = "battle/monsterinfo.csv";
    private final String ATTACKFILE = "battle/enemyattackinfo.csv";

    public EnemyManager(){
        enemies = new Enemy[MAX_FOES];

        //Make new enemies in each slot.
        //This is so we don't have to instantiate a new enemy
        // when we enter a new battle. We simply modify the
        // old ones.
        for(int i = 0; i < MAX_FOES; i++){
            enemies[i] = new Enemy();
        }

    }


    //TODO: store enemy data in files, of course
    public Enemy[] calcEnemies(int[] specs){
        int numEnemies = specs.length;
        Enemy[] returnMe = new Enemy[numEnemies];
        for (int i = 0; i < numEnemies; i++){
            loadEnemy(i, specs[i]);
            returnMe[i] = enemies[i];
        }
        return returnMe;

    }


    public void loadEnemy(int num, int enemyDataNum){

        // TODO: Make it so it searches thru a file and then get the data from that file then it loads it into
        //  enemies[num].

        try{
            BufferedReader buff = new BufferedReader(new FileReader(INFOFILE));

            //first line is header
            buff.readLine();

            //skip lines until reaching enemy data
            for(int i = 0; i < enemyDataNum; i++){
                buff.readLine();
            }

            String[] data = buff.readLine().split(",");

            //ORDER: Name, sprite, hp, mp, atk, def, spatk, spdef, speed

            enemies[num].setName(data[0]);
            enemies[num].setSprite(data[1]);
            enemies[num].setHp(Integer.parseInt(data[2]));
            enemies[num].setMp(Integer.parseInt(data[3]));
            enemies[num].setAttack(Integer.parseInt(data[4]));
            enemies[num].setDefense(Integer.parseInt(data[5]));
            enemies[num].setMagicAttack(Integer.parseInt(data[6]));
            enemies[num].setMagicDefense(Integer.parseInt(data[7]));
            enemies[num].setSpeed(Integer.parseInt(data[8]));
            enemies[num].setExpYield(Integer.parseInt(data[9]));

            //now, load attacks
            int attack1 = Integer.parseInt(data[10]);
            int attack2 = Integer.parseInt(data[11]);
            int attack3 = Integer.parseInt(data[12]);
            int attack4 = Integer.parseInt(data[13]);

            int[] attacks = {attack1, attack2, attack3, attack4};

            BufferedReader buff2 = new BufferedReader(new FileReader(ATTACKFILE));
            //first line is header
            buff2.readLine();

            //read next line
            String attackInfo = buff2.readLine();

            //fo thru attacks, get data for each
            //precondition: enemy attacks must be in order
            for (int i = 0; i < attacks.length; i++){
                int linesToSkip;
                if ( i == 0){ linesToSkip = attacks[i]; }
                else{ linesToSkip = attacks[i] - attacks[i-1]; }

                //skip lines until on correct info
                for (int j = 0; j < linesToSkip; j++){
                    attackInfo = buff2.readLine();
                }

                //get data for this move
                String[] moveData = attackInfo.split(",");

                String atkName = moveData[0];
                String atkMsg = moveData[1];
                int pss = Integer.parseInt(moveData[2]);
                int dmg = Integer.parseInt(moveData[3]);
                int eff = Integer.parseInt(moveData[4]);
                int anim = Integer.parseInt(moveData[5]);

                enemies[num].setAttack(i, atkName, atkMsg, pss, dmg, eff, anim);

            }
            buff2.close();

            //get weakness data

            //clear old
            enemies[num].clearWeaknesses();

            String vulnInfo = data[14];
            if (!vulnInfo.equals("none")){
                //weaknesses are split by the hashtag
                String[] weaknesses = vulnInfo.split("#");

                for(String w : weaknesses){
                    enemies[num].addWeakness(Integer.parseInt(w));
                }
            }

            int money = Integer.parseInt(data[15]);
            enemies[num].setMoneyYield(money);

            int song = Integer.parseInt(data[16]);
            enemies[num].setSong(song);

            buff.close();
        }
        catch (IOException e){
            System.out.println("ioexception during load enemy");
        }


    }


    public void dispose(){
        for (Enemy p : enemies){
            if (p != null){ p.dispose();}
        }
    }




}
