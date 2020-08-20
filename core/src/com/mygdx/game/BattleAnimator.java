package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Contains data for all the animations, what files to use
 */
public class BattleAnimator {
    private static final int TILEDIM = 64;


    public static int animationType;
    public static int animationDuration;
    public static int framesThru;

    //static display of effects!

   // public static FilmStrip effects = new FilmStrip(new Texture("battlepics/anims.png"), 1, 4, 128, 64);

//    public static void drawEffect(SpriteBatch batch, int x, int y){
//        batch.draw(effects,x,y);
//    }
//
//    public static void loadEffect(int type, int duration){
//        animationDuration = duration;
//        animationType = type;
//        framesThru = 0;
//    }






    //for sudden on-screen animations

    public static FilmStrip currentAnimation;
    public static int currentFrameGoal;
    public static int currentNumFilmFrames;
    public static int currentFramesThru;
    private static int currentX;
    private static int currentY;


    //for animation on screen

    public static void loadAnimation(int animationNum){
        String FILE_FOR_ANIMATIONS = "animations/animationinfo.csv";
        try{
            BufferedReader  buff = new BufferedReader(new FileReader(FILE_FOR_ANIMATIONS));
            buff.readLine(); // first line is header

            for (int i = 0; i < animationNum; i++){
                //skip lines until at animation
                buff.readLine();
            }

            //now at animation we want
            String[] info = buff.readLine().split(",");

            //header in order: num, file, duration, numFrames, rows, cols, rowheight, rowwidth, x, y
            String fileName = "animations/" + info[1];
            currentFrameGoal = Integer.parseInt(info[2]);
            currentFramesThru = 0;
            currentNumFilmFrames = Integer.parseInt(info[3]);
            int rows = Integer.parseInt(info[4]);
            int cols = Integer.parseInt(info[5]);
            int tileheight = Integer.parseInt(info[6]);
            int tilewidth = Integer.parseInt(info[7]);
            currentX = Integer.parseInt(info[8]);
            currentY = Integer.parseInt(info[9]);

            buff.close();

            currentAnimation = new FilmStrip(new Texture(fileName), rows, cols, tileheight, tilewidth);
        }
        catch(IOException e){
            System.out.println("ioexception during animation file");
        }
    }

    public static void loadAnimation(int animationNum, int pos){
        String FILE_FOR_ANIMATIONS = "animations/animationinfo.csv";
        try{
            BufferedReader  buff = new BufferedReader(new FileReader(FILE_FOR_ANIMATIONS));
            buff.readLine(); // first line is header

            for (int i = 0; i < animationNum; i++){
                //skip lines until at animation
                buff.readLine();
            }

            //now at animation we want
            String[] info = buff.readLine().split(",");

            //header in order: num, file, duration, numFrames, rows, cols, rowheight, rowwidth, x, y
            String fileName = "animations/" + info[1];
            currentFrameGoal = Integer.parseInt(info[2]);
            currentFramesThru = 0;
            currentNumFilmFrames = Integer.parseInt(info[3]);
            int rows = Integer.parseInt(info[4]);
            int cols = Integer.parseInt(info[5]);
            int tileheight = Integer.parseInt(info[6]);
            int tilewidth = Integer.parseInt(info[7]);
            currentX = getBattlePositionX(pos);
            currentY = getBattlePositionY(pos);
            buff.close();

            currentAnimation = new FilmStrip(new Texture(fileName), rows, cols, tileheight, tilewidth);
        }
        catch(IOException e){
            System.out.println("ioexception during animation file");
        }
    }

    public static void loadAnimation(int animationNum, int x, int y){
        String FILE_FOR_ANIMATIONS = "animations/animationinfo.csv";
        try{
            BufferedReader  buff = new BufferedReader(new FileReader(FILE_FOR_ANIMATIONS));
            buff.readLine(); // first line is header

            for (int i = 0; i < animationNum; i++){
                //skip lines until at animation
                buff.readLine();
            }

            //now at animation we want
            String[] info = buff.readLine().split(",");

            //header in order: num, file, duration, numFrames, rows, cols, rowheight, rowwidth, x, y
            String fileName = "animations/" + info[1];
            currentFrameGoal = Integer.parseInt(info[2]);
            currentFramesThru = 0;
            currentNumFilmFrames = Integer.parseInt(info[3]);
            int rows = Integer.parseInt(info[4]);
            int cols = Integer.parseInt(info[5]);
            int tileheight = Integer.parseInt(info[6]);
            int tilewidth = Integer.parseInt(info[7]);
            currentX = x;
            currentY = y;
            buff.close();

            currentAnimation = new FilmStrip(new Texture(fileName), rows, cols, tileheight, tilewidth);
        }
        catch(IOException e){
            System.out.println("ioexception during animation file");
        }
    }

    public static void drawAnimation(SpriteBatch batch){
        if (animating()) {
            batch.draw(currentAnimation, currentX * TILEDIM, currentY * TILEDIM);
        }

    }

    public static void updateAnimation(){
        if (animating()) {

            currentFramesThru++;
            int singleFrameDuration = currentFrameGoal / currentNumFilmFrames;
            int currentFrame = currentFramesThru / singleFrameDuration;
            if (currentAnimation.getFrame() != currentFrame) {
                 currentAnimation.setFrame(currentFrame);
            }
            if (currentFramesThru > currentFrameGoal) {
                //dispose and make null
                currentAnimation.getTexture().dispose();
                currentAnimation = null;
            }
        }
    }

    public static boolean animating(){
        return currentAnimation != null;
    }


    public static boolean halfwayDone(){
        return animating() && currentFramesThru >= (currentFrameGoal/2);
    }






    /**
     * Battle position system:
     * 0, 1, 2, 3 = players 1234
     * 4,5,6,7 = enemies 1234
     *
     * @param pos The position
     * @return X for literal position on screen
     */
    private static int getBattlePositionX(int pos){
        if (pos > 3){ //is enemy
            return 11;
        }else{ //is player
            return 4;
        }
    }
    private static int getBattlePositionY(int pos){
        if (pos > 3){ //is enemy
            pos = pos - 4;
            return (9 - (4*pos));
        }else{ //is player
            return (10 - (3*pos));
        }
    }


}
