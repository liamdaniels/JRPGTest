package com.mygdx.game;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class TextEventHandler {

    private String currentMessage;

    private final String TEXTS = "text/bigtext1.txt";

    private boolean eventOn;



    public TextEventHandler(){
        eventOn = false;
        currentMessage = null;
    }

    //TODO: do something other than string, like an NPC or something?
    public void activate(String s){

        eventOn = true;
        currentMessage = s;
    }

    public void deactivate(){
        eventOn = false;
        currentMessage = null;
    }

    public String getMessage(){
        return currentMessage;
    }

    public boolean isActive(){
        return eventOn;
    }

    /**
     * Loads text using a number and the big text file
     * @param num the number for the text in the big file
     */
    public void loadTextNum(int num){
        try{
            //open file of big texts. Getting the text is O(n) I think which is probably ok
            BufferedReader buff = new BufferedReader(new FileReader(TEXTS));

            //file is literally in order, line by line.
            for (int i = 0; i < num; i++){
                buff.readLine();
            }
            activate(buff.readLine());
            buff.close();
        }
        catch(IOException e){
            System.out.println("io exception in load text num");
        }
    }



}
