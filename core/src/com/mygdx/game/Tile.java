
package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.*;

/**
 * Makes a tile! Kind of like FilmStrip class from ShipDemo
 */
public class Tile extends TextureRegion {
    /** The number of columns and rows in tileset */
    //FOR NOW: COLS is 16, ROWS is 2
    private final int TILESET_COLS = 16;
    private final int TILESET_ROWS = 10;

    /** The height and width of a single tile frame */
    private final int THEIGHT = 64;
    private final int TWIDTH = 64;

    /** The number of frames in this filmstrip */
    private final int SIZE = TILESET_COLS*TILESET_ROWS;




    /** Tile location */
    private int x;
    private int y;


    /** The active animation frame */
    private int tileNum;

    /**
     *
     *  Constructs a Tile
     *  Takes in tileSet, which it references to see which tile to be
     *
     * @param tileSet The tileset to refer to
     * @param x       Location of tile on screen (x)
     * @param y       Location of tile on screen (y)
     */
    public Tile(Texture tileSet, int x, int y) {
        super(tileSet);
        this.x = x;
        this.y = y;
        setFrame(0);
    }

    /**
     * Returns the current active frame.
     *
     * @return the current active frame.
     */
    public int getFrame() {
        return tileNum;
    }

    public int getY(){ return y;}

    public int getX(){ return x; }

    /**
     * Sets tile to one of the "tile frames" in the tileset
     *
     * @param frame The tile frame on the set to set this Tile to
     */
    public void setFrame(int frame) {
        if (frame < 0 || frame >= SIZE) {
            Gdx.app.error("Tile", "Invalid tile frame", new IllegalArgumentException());
            return;
        }
        this.tileNum = frame;
        int tileSetX = (frame % TILESET_COLS)*TWIDTH;
        int tileSetY = (frame / TILESET_COLS)*THEIGHT;
        setRegion(tileSetX,tileSetY,TWIDTH,THEIGHT);
    }

    public void dispose(){
        this.getTexture().dispose();
    }

}