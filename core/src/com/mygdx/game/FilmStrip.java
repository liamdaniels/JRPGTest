
package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.*;


/**
 * Highkey stole most of this from Cornell.
 * I changed some of the code tho
 */
public class FilmStrip extends TextureRegion {
    /** The number of columns in this filmstrip */
    private int cols;

    /** The width of a single frame; computed from column count */
    private int rwidth;

    /** The height of a single frame; computed from row count */
    private int rheight;

    /** The number of frames in this filmstrip */
    private int size;

    /** The active animation frame */
    private int frame;

    /** direction player is facing */
    private int direction;

    /** If hitbox is 2 long or not */
    private boolean bigHitbox;



    /**
     * Creates a new filmstrip from the given texture.
     *
     * The parameter size is to indicate that there are unused frames in
     * the filmstrip.  The value size must be less than or equal to
     * rows*cols, or this constructor will raise an error.
     *
     * @param texture The texture image to use
     * @param rows The number of rows in the filmstrip
     * @param cols The number of columns in the filmstrip
     * @param rh
     * @param rw
     */
    public FilmStrip(Texture texture, int rows, int cols, int rh, int rw) {
        super(texture);
        if (size > rows*cols) {
            Gdx.app.error("FilmStrip", "Invalid strip size", new IllegalArgumentException());
            return;
        }


        this.cols = cols;
        this.size = cols*rows;
        rwidth  = rw;
        rheight = rh;
        direction = 0;
        setFrame(0);

        bigHitbox = false;
    }

    /**
     * Returns the number of frames in this filmstrip.
     *
     * @return the number of frames in this filmstrip.
     */
    public int getSize() {
        return size;
    }

    /**
     * Returns the current active frame.
     *
     * @return the current active frame.
     */
    public int getFrame() {
        return frame;
    }

    /**
     * Sets the active frame as the given index.
     *
     * If the frame index is invalid, an error is raised.
     *
     * @param frame the index to make the active frame
     */
    public void setFrame(int frame) {

        this.frame = frame;
        int x = (frame % cols)*rwidth;
        int y = (frame / cols)*rheight;
        setRegion(x,y,rwidth,rheight);
    }

    public void setDirectionalFrame(int miniFrame){
        //if hitbox is BIG, then setFrame differently
        if (bigHitbox){
            setFrame(direction);
            return;
        }

        setFrame((direction * cols) + miniFrame);
    }


    public void update(int xWalk, int yWalk, int aniFrame){
        //determine direction using xWalk and yWalk
        //down is 0, up is 1, left is 2, right is 3

        if (xWalk > 0){
            direction = 3;
        }else if (xWalk < 0){
            direction = 2;
        }else if (yWalk > 0){
            direction = 1;
        }else if (yWalk < 0){
            direction = 0;
        }

        setDirectionalFrame(aniFrame);

    }


    public void update(int direction, int aniFrame){
        this.direction = direction;
        setDirectionalFrame(aniFrame);
    }


    public void draw(SpriteBatch batch,int x, int y,int xWalk, int yWalk, int maxFrames){
        int xMvmt = (64/maxFrames) * xWalk;
        int yMvmt = (64/maxFrames) * yWalk;

        batch.draw(this,x + xMvmt,y + yMvmt);

    }

    public void draw(SpriteBatch batch,int x, int y){

        batch.draw(this,x,y);



    }

    public int getDirection(){return direction;}

    public void setNewTex(Texture texture, int rows, int cols, int rh, int rw, boolean hitbox){
        this.setTexture(texture);


        this.cols = cols;
        this.size = cols*rows;
        rwidth  = rw;
        rheight = rh;
        direction = 0;
        bigHitbox = hitbox;

        setFrame(0);

    }

    public void setBigHitbox(boolean h){
        bigHitbox = h;
    }

    public boolean isBigHitbox(){
        return bigHitbox;
    }


}