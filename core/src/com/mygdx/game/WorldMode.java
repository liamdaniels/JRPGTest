package com.mygdx.game;


import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import jdk.jfr.Event;


/**
 * Like the GameMode from the ShipDemo.
 * Basically like the walking around and stuff. Not battle or menu
 */
public class WorldMode implements ModeController {

    //important files

    private static final String MAPSFILE = "maps/mapinfo.csv";


    private FilmStrip player;


    private TextBox box;


    private MapManager mapManager;

    private TextEventHandler messageHandler;

    private CutsceneManager cutsceneManager;

    private EventFlagManager eventFlagManager;

    //how much player is currently displaced
    private int xmove;
    private int ymove;

    //stage of player's movement and displacement
    private int movingstage;


    /**
     * To switch to cutscene mode (cutscene mode is "in" worldmode, basically)
     * will be empty string when no cutscene is present
     */
    public static String upcomingCutscene = "";


    public WorldMode(ItemManager itemManager){

        // inputs

        player = new FilmStrip(SoundTextureManager.getP1Tex(),8,2,128, 64);

        box = new TextBox();
        box.passItemManager(itemManager);

        movingstage = 0;
        xmove = 0;
        ymove = 0;

        mapManager = new MapManager(MAPSFILE,0);
        messageHandler = new TextEventHandler();

        cutsceneManager = new CutsceneManager(mapManager, messageHandler, player);
        eventFlagManager = new EventFlagManager(player,mapManager);


    }



    public void update(InputController inputs){


        //check if it's cutscene time
        if (!upcomingCutscene.equals("")){
            cutsceneManager.beginCutscene(upcomingCutscene);
            upcomingCutscene = "";
        }



        //-----update------
        inputs.readInput();

        // read UDLR inputs
        int inputL = inputs.getLeft();
        int inputR = inputs.getRight();
        int inputU = inputs.getUp();
        int inputD = inputs.getDown();
        int inputLetterD = inputs.getD();
        int inputLetterS = inputs.getS();
        int inputLetterB = inputs.getB();



        inputLetterB = inputs.applyDelay(inputLetterB,6, 20);
        inputLetterD = inputs.applyDelay(inputLetterD,4, 20);

        if (inputLetterS == 1){//TODO: REMOVE. this is for a test
            mapManager.reloadMapAtLocation();
        }


        //IMPORTANT!!
        // Do not remove this if statement from this spot. It must come before box.update
        //     or else bad things will happen.

        if (inputLetterD == 1 && !box.isDisplaying()){
            mapManager.attemptTalk(player.getDirection(),messageHandler);
        }
        box.update(inputLetterD == 1, inputD - inputU,messageHandler);



        //none of this should happen if text or cutscene are happening
        if (!box.isDisplaying() && !cutsceneManager.isActive() && !mapManager.isMidTransition()) {

            //if B pressed, menu
            if (inputLetterB == 1){
                //switch to menu mode
                TestGame.upcomingMode = "menu";
            }

            //just to save a bit of calculation
            if (movingstage == 0 && (inputD - inputU != 0 || inputL - inputR != 0)) { // if player moves at all

                movingstage = 1;
                xmove = inputR - inputL;
                ymove = inputU - inputD;
            } else if (movingstage == 0) {
                //do nada
            } else if (movingstage >= 7) {
                movingstage = 7; //in case it's too big
                mapManager.move(xmove, ymove);
                movingstage = 0;
            } else {
                movingstage++;

                //if in car, moving stage zooms up
                if (TestGame.eventFlags[100]){ movingstage += 3; }

            }

            player.update(xmove, ymove, (movingstage / 4));



        }else{
            // for the menu box!
            inputD = inputs.applyDelay(inputD,1, 10);
            inputU = inputs.applyDelay(inputU,0, 10);

            //cutscene update
            cutsceneManager.update();

        }

        mapManager.update(cutsceneManager.isActive());

        eventFlagManager.update();


    }

    public void draw(SpriteBatch batch){

        mapManager.draw(batch);
        mapManager.drawSprites(batch, player, xmove, ymove);
        cutsceneManager.draw(batch);

        //TODO: improve foreground
        //mapManager.draw(batch, player, xmove, ymove);

        box.draw(batch);



    }

    public void dispose(){
        player.getTexture().dispose();
        player = null;
        mapManager.dispose();
        mapManager = null;
        box.dispose();
        box = null;

    }


    public int getCurrentMapSong(){
        return mapManager.currentMapSong();
    }




}
