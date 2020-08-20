package com.mygdx.game;


import com.badlogic.gdx.assets.AssetManager;

/**
 * This does very arbitrary things for certain event flags!
 * So far: only needed in world mode
 */
public class EventFlagManager {

    /** Must set to true when any event flag is altered, which is currently
     *   only possible by text. */
    public static boolean eventFlagChanged = false;

    public static AssetManager textureManager = new AssetManager();

    /** Access to all the world map objects */
    FilmStrip player;
    MapManager map;



    public EventFlagManager(FilmStrip player, MapManager map){
        this.map = map;
        this.player = player;
    }




    public void update(){
        //only do calculations if an event flag changed this frame
        if(eventFlagChanged){
            //reset
            eventFlagChanged = false;

            //check very specific things!!

            //flag 100: human is car!
            if (TestGame.eventFlags[100] && !player.isBigHitbox()){//not already car
                //switch to car
                player.setNewTex(SoundTextureManager.getCarTex(),2,2,128,128,true);

            }else if (!TestGame.eventFlags[100] && player.isBigHitbox()){
                player.setNewTex(SoundTextureManager.getP1Tex(),8,2,128, 64,false);
            }



        }
    }






}
