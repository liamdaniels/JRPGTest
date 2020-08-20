package com.mygdx.game;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Sprite that you can talk to
 */
public class Human {

    //TODO: Eventually make into int[]?
    private int dialogueNum;

    private final int TILEDIM = 64;

    private int x;

    private int eventFlag;

    private int y;

    private int naturalX;
    private int naturalY;

    private int xWalk;
    private int yWalk;

    private FilmStrip tex;

    private boolean bigHitbox;




    /**
     * Movement patterns:
     * 0: don't move, but turns when talked to
     * 1: move around randomly
     * 2: literally never moves under any circumstance
     *
     *
     *
     */
    private int movementPattern;

    private int directionFacing;

    private int moveCycle;
    private int cycleDuration;


    /** talking to player*/
    private boolean talking;


    /** Moving (in cutscene) */
    private boolean cutsceneMoving;
    private int numStepsLeft;


    public Human(String nameFile, int x, int y, int dia, int evflag, int move, boolean bic, int direc){

        int TEXCOLS = 2;
        int FRAMEHEIGHT = 128;

        int frameWidth = 64;
        int texRows = 8;


        if (bic){
            frameWidth *= 2;
            texRows /= 2;
        }

        tex = new FilmStrip(new Texture(nameFile), texRows, TEXCOLS, FRAMEHEIGHT, frameWidth);
        tex.setBigHitbox(bic);
        this.naturalX = x;
        this.naturalY = y;
        this.x = x;
        this.y = y;
        this.dialogueNum =  dia;
        this.eventFlag = evflag;


        this.movementPattern = move;

        bigHitbox = bic;

        //down is 0, up is 1, left is 2, right is 3
        directionFacing = direc;

        initNaturalScene();
    }

    /**
     * Event flag for a party cutscene human!
     * @param tex The human's texture
     * @param bic Is big or not
     */
    public Human(Texture texture, boolean bic){
        int TEXCOLS = 2;
        int FRAMEHEIGHT = 128;

        int frameWidth = 64;
        int texRows = 8;


        if (bic){
            frameWidth *= 2;
            texRows /= 2;
        }

        tex = new FilmStrip(texture, texRows, TEXCOLS, FRAMEHEIGHT, frameWidth);
        tex.setBigHitbox(bic);


        bigHitbox = bic;

        directionFacing = 0;

        //set to wayy offscreen
        x = -40;
        y = -40;

        //idk who cares
        eventFlag = 69;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX(){return x;}

    public int getY(){return y;}

    public int getDialogueNum(){ return dialogueNum;}

    public void draw(SpriteBatch batch, int drawX, int drawY){
        if (!TestGame.eventFlags[eventFlag]) {
            if ((cutsceneMoving || movementPattern == 1) && moveCycle <= cycleDuration/4){
                int extraMv = (moveCycle/10) + 1;
                tex.draw(batch, drawX, drawY, xWalk*extraMv, yWalk*extraMv, 3);
            }else{
                tex.draw(batch, drawX, drawY);
            }

        }
    }

    public int getEventFlag(){
        return eventFlag;
    }

    public boolean isActive(){
        return !TestGame.eventFlags[eventFlag];
    }


    public int getMovement(){
        return movementPattern;
    }

    public void naturalUpdate(MapManager mapMan){
        if (talking){


            //check if player has moved away yet. If they have, done talking
            if (Math.abs(mapMan.getPlayerX() - x) > 1 || Math.abs(mapMan.getPlayerY() - y) > 1){
                talking = false;
            }
        }

        if (movementPattern == 0 || talking){
            return;
        } //nothing to do here

        switch(movementPattern) {

            case 1:
                moveCycle++;
                if (moveCycle >= cycleDuration) {

                    moveCycle = 0;
                    //randomize next direction
                    directionFacing = (int) (Math.random() * 3);

                    if (directionFacing > 1) {
                        xWalk = ((directionFacing - 2) * 2 - 1);
                        yWalk = 0;
                    } else {
                        yWalk = (2 * directionFacing - 1);
                        xWalk = 0;
                    }

                    if (!mapMan.canMoveHuman(x + xWalk, y + yWalk)){
                        xWalk = 0;
                        yWalk = 0;
                    }
                }

                //only "move" on first 30 frames
                int ani = 0;
                if (moveCycle < 30){
                    ani = 1;
                }

                if (moveCycle == 30){
                    int futureX = x;
                    int futureY = y;

                    //down is 0, up is 1, left is 2, right is 3
                    if (directionFacing > 1) {
                        futureX = x + ((directionFacing - 2) * 2 - 1);
                    } else {
                        futureY = y + (2 * directionFacing - 1);
                    }

                    //literally move the character, is possible
                    if (mapMan.canMoveHuman(futureX, futureY)) {
                        this.x = futureX;
                        this.y = futureY;
                    }

                    xWalk = 0;
                    yWalk = 0;
                }


                //TODO: change frame
                tex.update(directionFacing, ani);
                break;
            case 2:
                //don't move
                break;

        }



    }




    public void setTalking(boolean b){
        talking = b;

    }


    public void teleport(int x, int y){
        this.x = x;
        this.y = y;
    }

    public void initNaturalScene(){
        moveCycle = 0;
        this.x = naturalX;
        this.y = naturalY;
        switch(movementPattern){
            case 1:
                cycleDuration = 120;
                break;
            default:
                cycleDuration = 0;
        }


        talking = false;
    }

    //cutscene methods

    public void initCutscene(){
        moveCycle = 0;
        cycleDuration = 120;
        directionFacing = 0;

    }

    public void changeDirection(int d){
        //movement pattern 2 means NEVER CHANGE DIRECTION!
        if (movementPattern == 2){ return; }
        if (x == 12) {
            System.out.println("changing direction to " + d);
            System.out.println("my mvmt is " + movementPattern);
        }

        directionFacing = d;
        tex.update(directionFacing, 0);
    }

    public void move(int numSteps, int walkLength){
        cutsceneMoving = true;
        cycleDuration = walkLength;
        numStepsLeft = numSteps;
        if (directionFacing > 1) {
            xWalk = ((directionFacing - 2) * 2 - 1);
            yWalk = 0;
        } else {
            yWalk = (2 * directionFacing - 1);
            xWalk = 0;
        }
    }

    public void cutsceneUpdate(){
        if (cutsceneMoving){
            int ani = 0;
            if (moveCycle <= cycleDuration/4){
                ani = 1;
            }

            //literally move the character
            if (moveCycle == cycleDuration/4) {
                x = x + xWalk;
                y = y + yWalk;
            }

            tex.update(directionFacing, ani);
            moveCycle++;

            if(moveCycle >= cycleDuration){
                moveCycle = 0;
                if (numStepsLeft <= 1) {
                    numStepsLeft--;
                    cutsceneMoving = false;
                    xWalk = 0;
                    yWalk = 0;
                }else{
                    numStepsLeft--;
                }
            }
        }



    }

    public boolean isMoving(){
        return cutsceneMoving;
    }

    public void setFrame(int frame){
        tex.setFrame(frame);
    }



    public boolean isBigHitbox(){
        return bigHitbox;
    }


}
