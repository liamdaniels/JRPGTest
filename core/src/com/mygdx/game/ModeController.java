package com.mygdx.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public interface ModeController {

    //TODO: more docs

    public void update(InputController inp);

    public void draw(SpriteBatch batch);

    public void dispose();

}

