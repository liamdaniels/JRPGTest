package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class BattleMode implements ModeController{


    private static final int TILESWIDTH = 17;
    private static final int TILESHEIGHT = 13;

    private static final int TILEDIM = 64;


    private BattleManager battleManager;


    public BattleMode(){
        battleManager = new BattleManager();
    }

     public void update(InputController inputs){

        //read inputs, apply appropriate delay
         inputs.readInput();

         int inputU = inputs.getUp();
         int inputD = inputs.getDown();
         int inputLetterD = inputs.getD();
         int inputLetterS = inputs.getS();


         inputU = inputs.applyDelay(inputU,0, 10);
         inputD = inputs.applyDelay(inputD,1, 10);
         inputLetterD = inputs.applyDelay(inputLetterD, 4, 15);
         inputLetterS = inputs.applyDelay(inputLetterS, 5, 15);

         battleManager.update(inputD - inputU, inputLetterD == 1, inputLetterS == 1);







     }

     public void draw(SpriteBatch batch){

        battleManager.draw(batch);



     }

     public void dispose(){
        battleManager.dispose();

     }

     public void load(PlayerManager playerManager, Enemy[] foes, ItemManager itemManager, SpellManager spellManager){
        battleManager.loadNewBattle(playerManager, foes, itemManager, spellManager);
     }







}
