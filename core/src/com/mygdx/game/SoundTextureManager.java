package com.mygdx.game;

import com.badlogic.gdx.assets.AssetManager;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;


/**
 * Manager of Sounds and (some) Textures
 */
public class SoundTextureManager {

    private final static AssetManager assetManager = new AssetManager();

    public static Sound selectSound;

    public static Sound[] musics;
    public static int currentSongNum;

    private static final String SELECT_SOUND = "sounds/eh.mp3";

    private static final String[] MUSICS = {"sounds/calm.mp3",
            "sounds/kefka.mp3",
            "sounds/boss.mp3",
            "sounds/mog.mp3"};



    private final static String PLAYER_1_TEX = "sprites/jeff.png";
    private final static String PLAYER_2_TEX = "sprites/a.png";
    private final static String CARTEX = "sprites/car.png";




    public static Sound currentBgMusic;


    public static void loadAll(){
        // load all the sounds
        int NUM_MUSICS = 3;
        musics = new Sound[NUM_MUSICS];


        assetManager.load(SELECT_SOUND,Sound.class);
        for (String s: MUSICS){
            assetManager.load(s,Sound.class);
        }

        //load necessary textures

        assetManager.load(PLAYER_1_TEX, Texture.class);
        assetManager.load(PLAYER_2_TEX, Texture.class);
        assetManager.load(CARTEX, Texture.class);


        assetManager.finishLoading();


        selectSound = assetManager.get(SELECT_SOUND,Sound.class);

        for(int i = 0; i < musics.length; i++){
            musics[i] = assetManager.get(MUSICS[i], Sound.class);
        }
    }


    public static void dispose(){
        selectSound.dispose();
        for (Sound s: musics){
            s.dispose();
        }

        assetManager.unload(SELECT_SOUND);
        for (String s: MUSICS){
            assetManager.unload(s);
        }



        assetManager.unload(CARTEX);
        assetManager.unload(PLAYER_1_TEX);




        assetManager.dispose();

    }

    public static void playSelect(){
        selectSound.stop();
        selectSound.play();
    }


    public static void playNewBgSong(int songNum){
        if (currentSongNum != songNum || currentBgMusic == null) {
            if (currentBgMusic != null){ currentBgMusic.stop(); }
            currentBgMusic = musics[songNum];
            currentBgMusic.loop();
            currentSongNum = songNum;
        }
    }

    public static Texture getP1Tex(){
        return assetManager.get(PLAYER_1_TEX);
    }
    public static Texture getP2Tex(){
        return assetManager.get(PLAYER_2_TEX);
    }

    public static Texture getCarTex(){
            return assetManager.get(CARTEX);
    }




}
