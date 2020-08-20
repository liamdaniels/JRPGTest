package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;



/**
 * controls inputs
 */
public class InputController {


    /** ints for directional movement. idk lol just for test */
    private int up;
    private int down;
    private int left;
    private int right;
    private int d;
    private int s;
    private int b;

    //input times for input delay
    //FOR NOW: map
    //0 = up, 1 = down, 2 = left, 3 = right
    //4 = d,
    private int[] times;

    public int getUp() {
        return up;
    }

    public int getDown() {
        return down;
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public int getD() { return d; }

    public int getS() { return s;}

    public int getB() { return b;}

    public InputController(){
        up = 0;
        down = 0;
        left = 0;
        right = 0;
        d = 0;
        s = 0;


        times = new int[7];
        for (int i = 0; i < times.length; i ++){
            times[i] = 0;
        }
    }

    public void readInput(){
        int keyup    = Input.Keys.UP;
        int keydown  = Input.Keys.DOWN;
        int keyleft  = Input.Keys.LEFT;
        int keyright = Input.Keys.RIGHT;
        int keyD = Input.Keys.D;
        int keyS = Input.Keys.S;
        int keyB = Input.Keys.B;

        if (Gdx.input.isKeyPressed(keyup)) {
            up = 1;
        }
        else{
            up = 0;
        }

        if (Gdx.input.isKeyPressed(keydown)) {
            down = 1;
        }
        else{
            down = 0;
        }


        if (Gdx.input.isKeyPressed(keyleft)) {
            left = 1;
        }
        else{
            left = 0;
        }


        if (Gdx.input.isKeyPressed(keyright)) {
            right = 1;
        }
        else{
            right = 0;
        }

        if (Gdx.input.isKeyPressed(keyD)) {
            d = 1;
        }
        else{
            d = 0;
        }

        if (Gdx.input.isKeyPressed(keyS)) {
            s = 1;
        }
        else{
            s = 0;
        }

        if (Gdx.input.isKeyPressed(keyB)) {
            b = 1;
        }
        else{
            b = 0;
        }

    }

    /**
     * FOR NOW: map
     *     0 = up, 1 = down, 2 = left, 3 = right
     *     4 = d
     * @param input     Input value (0 or 1)
     * @param key       Which key it is
     * @param numFrames How many frames to delay for
     * @return The proper int, given delay
     */
    public int applyDelay(int input, int key, int numFrames){

        if (times[key] >= numFrames){
            times[key] = 0;
            return 0;
        }else if (times[key] > 0){
            times[key]++;
            return 0;
        }
        else if(input == 1 && times[key] == 0){

            times[key] = 1;
            return 1;
        }
        return input;
    }



}
