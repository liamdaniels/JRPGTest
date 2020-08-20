package com.mygdx.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

/**
 * basically menu box without the cursor
 */
public class BabyTextBox {
    private final int MAXLINES = 12;
    private final int TILEDIM = 64;

    private final String MENUTOP = "menuboxtop.png";
    private final String MENUMID = "menuboxmid.png";
    private final String MENUBOT = "menuboxbot.png";
    private final String FONTFILE = "Fontin-Regular.ttf";

    private boolean active;

    private int numLines;

    private String message;

    private Texture[] linesTex;

    private BitmapFont text;


    //num frames that it has been active, and num frames that box must display for
    private int framesActive;
    private int currentFramesGoal;

    private int x;
    private int y;


    public BabyTextBox(){
        //Took this code from online, presumably makes a new BitmapFont with the desired font file
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(FONTFILE));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 45;
        text = generator.generateFont(parameter);
        generator.dispose();
        text.getData().setScale(0.7f, 1f);

        this.x = 0;
        this.y = 0;


        this.active = false;
        this.framesActive = 0;

        message = null;
        numLines = 0;
        linesTex = new Texture[MAXLINES];



    }

    public void update(){
        if (active){
            this.framesActive++;
        }
        //If box have reached the number of frames we need to display for,
        // then it is done being active.
        if (framesActive == currentFramesGoal){
            active = false;
        }


    }

    /**
     * Puts new message up for display.
     * Precondition: message cannot have a word longer than 10 letters
     * @param message   Message to display on the box
     * @param x         The x coord (in tiles) where to display
     * @param y         The y coord (in tiles) where to display
     * @param numFrames The number of frames to display message for
     */
    public void displayNewMessage(String message, int x, int y, int numFrames){

        this.x = x;
        this.y = y;

        //by default, at least 2 lines (top and bottom)
        numLines = 2;

        //I have decided that every 10 characters necesitates a new line
        int MAXLINE = 10;
        if (message.length() > MAXLINE) { //more than one word. I am lazy, splitting up text box by word
            String[] messageSplit = message.split(" ");
            message = messageSplit[0];
            for (int i = 1; i < messageSplit.length; i ++){
                message += "\n" + messageSplit[i];
                numLines++;
            }

            this.message = message;
        }
        else {
            this.message = message;
        }


        for (int i = 0; i < numLines; i++){
            String menuType;
            //make it diff it it's bottom, top or middle
            if (i == 0) {
                menuType = MENUTOP;
            }else if (i == numLines-1){
                menuType = MENUBOT;
            }else{
                menuType = MENUMID;
            }
            if (linesTex[i] != null){ linesTex[i].dispose(); }
            linesTex[i] = new Texture(menuType);
        }


        this.active = true;
        this.framesActive = 0;

        currentFramesGoal = numFrames;


    }



    /**
     * Puts new message up for display. Goes on as long as window is active
     *  OR until displaying different message
     * @param message   Message to display on the box
     * @param x         The x coord (in tiles) where to display
     * @param y         The y coord (in tiles) where to display
     * @param lines     Num lines to display
     */
    public void foreverDisplay(String message, int x, int y, int lines, float xScale, float yScale){

        this.x = x;
        this.y = y;

        //by default, at least 2 lines (top and bottom)
        numLines = lines;

        this.message = message;

        for (int i = 0; i < numLines; i++){
            String menuType;
            //make it diff it it's bottom, top or middle
            if (i == 0) {
                menuType = MENUTOP;
            }else if (i == numLines-1){
                menuType = MENUBOT;
            }else{
                menuType = MENUMID;
            }
            linesTex[i] = new Texture(menuType);
        }

        text.getData().setScale(xScale, yScale);


        this.active = true;
        this.framesActive = 0;

        currentFramesGoal = -1;

    }

    public void changeMessage(String m){
        message = m;
    }


    public boolean isActive(){ return active;}

    public void setActive(boolean a){
        active = a;
    }

    public void draw(SpriteBatch batch){
        if (active){

            for(int i = 0; i < numLines; i++){
                batch.draw(linesTex[i], TILEDIM*x, TILEDIM*(y) - (i*TILEDIM));
            }

            text.draw(batch, message, (float)((x*TILEDIM) + (TILEDIM*0.5)), (float)((y*TILEDIM) + (TILEDIM*0.675)));


        }
    }




    public void dispose(){
        for (int i = 0; i < linesTex.length; i++){
            if (linesTex[i] != null) { linesTex[i].dispose(); }
        }
    }

}
