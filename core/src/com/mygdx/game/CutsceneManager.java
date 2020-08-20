package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;

/**
 * Manages cutscenes on the overworld. While active, controls player's movement and sprite movement, as
 *  well as the text that displays.
 */
public class CutsceneManager {

    private ArrayDeque<String> instructions;

    private MapManager mapManager;
    private TextEventHandler messageHandler;
    private FilmStrip player;

    private Human[] partyHumans;

    private boolean active;

    /** Set to current instruction.
     *  Set to null when instruction ends and we get to next one.
     */
    private String[] currentInstruction;

    //countdown for "wait" instruction
    private int waitingFrames;

    //for moving player
    private int movingstage;
    private int stepsLeft;
    private int xmove;
    private int ymove;



    public CutsceneManager(MapManager m, TextEventHandler t, FilmStrip player){
        messageHandler = t;
        mapManager       = m;
        active = false;
        currentInstruction = null;
        instructions = new ArrayDeque<String>();
        this.player = player;

        //TODO: not always 2 most likely. add if needed
        partyHumans = new Human[2];
        partyHumans[0] = new Human(SoundTextureManager.getP1Tex(), false);
        partyHumans[1] = new Human(SoundTextureManager.getP2Tex(), false);
    }

    public void beginCutscene(String cutsceneFile){
        active = true;
        currentInstruction = null;
        instructions.clear();
        loadCutscene(cutsceneFile);
    }

    private void loadCutscene(String cutsceneFile){
        try{
            BufferedReader buff = new BufferedReader(new FileReader(cutsceneFile));

            //each line of file is an instruction
            //load all lines of fileinto cutsceneActions
            String currentLine = buff.readLine();

            while (currentLine != null){
                instructions.addLast(currentLine);
                currentLine = buff.readLine();

            }

            buff.close();
        }
        catch(IOException e){
            System.out.println("IO EXCEPTION cutscene manager");
        }
    }


    public void update(){
        if (!isActive()){//do nothing, no cutscene
            return;
        }

        if (currentInstruction == null){
            executeFirst();
        }else{
            instructionMid();
        }
    }



    /**
     * Executes the first instruction in the instruction queue
     */
    private void executeFirst(){
        if (instructions.isEmpty()){ //done w/cutscene
            endCutscene();
            return;
        }
        currentInstruction = instructions.pollFirst().split(" ");
        instructionBegin();
    }


    /**
     * Does first action in current instruction
     * e.g. For "MOVE", doles out move statement to humans,
     *  and then for instructionMid, goes to next instruction
     *  if movement is done
     */
    private void instructionBegin(){
        switch(currentInstruction[0]){
            case "INIT": //initiate cutscene for sprites
                for (int i = 1; i < currentInstruction.length; i++){
                    //set human to move
                    int index = Integer.parseInt(currentInstruction[i]);
                    mapManager.getSprite(index).initCutscene();
                }
                //done, no need to have mid update
                currentInstruction = null;
                break;
            case "HMOVE"://move (human)
                int numSteps = Integer.parseInt(currentInstruction[1]);
                int zoomFactor = Integer.parseInt(currentInstruction[2]);
                for (int i = 3; i < currentInstruction.length; i++){
                    //set human to move
                    int index = Integer.parseInt(currentInstruction[i]);

                    if (index >= 0) {
                        mapManager.getSprite(index).move(numSteps, zoomFactor);
                    }else{// it is one of the humans in cutscenemanager!
                        // -1 is first in list (index 0), -2 is second (index 1), etc.
                        index = (-1*index) - 1;
                        partyHumans[index].move(numSteps, zoomFactor);
                    }
                }
                break;
            case "CD"://change direction (human)
                int direction = Integer.parseInt(currentInstruction[1]);
                for (int i = 2; i < currentInstruction.length; i++){
                    int index = Integer.parseInt(currentInstruction[i]);

                    if (index >= 0) {
                        mapManager.getSprite(index).changeDirection(direction);
                    }else{// it is one of the humans in cutscenemanager!
                        // -1 is first in list (index 0), -2 is second (index 1), etc.
                        index = (-1*index) - 1;
                       partyHumans[index].changeDirection(direction);
                    }
                }
                //done, no need to have mid update
                currentInstruction = null;
                break;
            case "HPORT"://teleport (human)
                int x = Integer.parseInt(currentInstruction[1]);
                int y = Integer.parseInt(currentInstruction[2]);
                int index = Integer.parseInt(currentInstruction[3]);

                if (index >= 0) {
                    mapManager.getSprite(index).teleport(x,y);
                }else{// it is one of the humans in cutscenemanager!
                    // -1 is first in list (index 0), -2 is second (index 1), etc.
                    index = (-1*index) - 1;
                    partyHumans[index].teleport(x,y);
                }


                //done, no need to have mid update
                currentInstruction = null;
                break;
            case "RELEPORT"://teleport human relative to player (hence "releport" heehee)
                int rpX = mapManager.getPlayerX() + Integer.parseInt(currentInstruction[1]);
                int rpY = mapManager.getPlayerY() + Integer.parseInt(currentInstruction[2]);
                int indexx = Integer.parseInt(currentInstruction[3]);

                if (indexx >= 0) {
                    mapManager.getSprite(indexx).teleport(rpX,rpY);
                }else{// it is one of the humans in cutscenemanager!
                    // -1 is first in list (index 0), -2 is second (index 1), etc.
                    indexx = (-1*indexx) - 1;
                    partyHumans[indexx].teleport(rpX,rpY);
                }

                //done, no need to have mid update
                currentInstruction = null;
                break;
            case "WAIT"://do nothing
                waitingFrames =  Integer.parseInt(currentInstruction[1]);
                break;
            case "TEX"://load text
                int textNum =  Integer.parseInt(currentInstruction[1]);
                messageHandler.loadTextNum(textNum);
                break;
            case "ANIM"://load animation
                int aniNum =  Integer.parseInt(currentInstruction[1]);
                //load for x and y if that's an option
                if (currentInstruction.length > 2){
                    int aniX =  Integer.parseInt(currentInstruction[2]);
                    int aniY =  Integer.parseInt(currentInstruction[3]);
                    BattleAnimator.loadAnimation(aniNum, aniX, aniY);
                }
                else {
                    BattleAnimator.loadAnimation(aniNum);
                }
                //done, no need to have mid update
                currentInstruction = null;
                break;
            case "PMOVE"://move player
                xmove = Integer.parseInt(currentInstruction[1]);
                ymove = Integer.parseInt(currentInstruction[2]);
                stepsLeft = Integer.parseInt(currentInstruction[3]);
                movingstage = 0;

                break;
            case "PFRAME": //set player frame
                int frameNum = Integer.parseInt(currentInstruction[1]);

                player.setFrame(frameNum);

                //done, no need to have mid update
                currentInstruction = null;
                break;
            case "HFRAME": //set sprite frame
                int hIndex = Integer.parseInt(currentInstruction[1]);
                int hFrameNum = Integer.parseInt(currentInstruction[2]);

                if (hIndex >= 0) {
                    mapManager.getSprite(hIndex).setFrame(hFrameNum);
                }else{// it is one of the humans in cutscenemanager!
                    // -1 is first in list (index 0), -2 is second (index 1), etc.
                    hIndex = (-1*hIndex) - 1;
                    partyHumans[hIndex].setFrame(hFrameNum);
                }

                //done, no need to have mid update
                currentInstruction = null;
                break;
            case "PTEX": //set player texture
                int texIndex = Integer.parseInt(currentInstruction[1]);

                setPlayerTex(texIndex);

                //done, no need to have mid update
                currentInstruction = null;
                break;
            case "INVIS": //make player invisible, or visible if they were invisible

                mapManager.togglePlayerInvis();

                //done, no need to have mid update
                currentInstruction = null;
                break;
            case "LOADMAP": //load a new map

                int newMap = Integer.parseInt(currentInstruction[1]);
                int newX = Integer.parseInt(currentInstruction[2]);
                int newY = Integer.parseInt(currentInstruction[3]);

                mapManager.loadNewMap(newMap, newX, newY);

                //done, no need to have mid update
                currentInstruction = null;
                break;
        }
    }

    private void instructionMid(){
        switch(currentInstruction[0]) {
            case "WAIT":
                waitingFrames--;
                if (waitingFrames <= 0){
                    currentInstruction = null;
                }
                break;
            case "HMOVE":
                //update all them
                int index = 0;
                for (int i = 3; i < currentInstruction.length; i++){
                    //set human to move
                    index = Integer.parseInt(currentInstruction[i]);
                    mapManager.getSprite(index).cutsceneUpdate();

                }
                //since all are moving same num steps, only need to check one
                if (!mapManager.getSprite(index).isMoving()){
                    //mot moving, so they are done, so go to next step
                    currentInstruction= null;
                }
                break;
            case "TEX":
                if(!messageHandler.isActive()){
                    //done displaying text!
                    currentInstruction= null;
                }
                break;
            case "PMOVE":
                if (movingstage >= 7) {//done with step
                    movingstage = 7; //in case it's too big
                    mapManager.move(xmove, ymove);
                    movingstage = 0;
                    //finished a step
                    stepsLeft--;
                } else {
                    movingstage++;
                }

                player.update(xmove, ymove, (movingstage / 4));

                if(stepsLeft <= 0){
                    //done moving
                    currentInstruction= null;
                }
                break;
        }
    }

    public void endCutscene(){
        active = false;

        //reset guys from cutscene
        for (int i = 0; i < mapManager.getNumSprites(); i++){
            mapManager.getSprite(i).initNaturalScene();
        }

        //reset party humans
        for (Human h : partyHumans){
            //put where can't be seen
            h.teleport(-40,-40);
        }
    }

    public boolean isActive(){
        return active;
    }


    public void draw(SpriteBatch batch){
        //simply draw the party humans
        int TILEDIM = 64;

        for(Human h: partyHumans){
            if (h.getX() >= 0){
                h.draw(batch, TILEDIM*h.getX(), TILEDIM*h.getY());
            }
        }
    }

    private void setPlayerTex(int index){
        switch(index){
            case 1:
                player.setNewTex(SoundTextureManager.getP1Tex(),8,2,128, 64,false);
                break;
            case 2:
                player.setNewTex(SoundTextureManager.getP2Tex(),8,2,128, 64,false);
                break;
            default:
                throw new RuntimeException("What the heck set player tex not 1 or 2");
        }
    }

    


}
