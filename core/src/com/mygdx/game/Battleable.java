package com.mygdx.game;

public interface Battleable {

    public void attack(Battleable target);

    public void changeHP(int change);

    public String getType();

    public int getSpeed();

    public boolean isDead();


    public void performAction(String action, Battleable target, int index);

    public void performMessage(String action, String used);

    public void loadReaction(String react, int change, int effect);

    public void performReaction();

    public void performReactionMessage();

    public String getName();

    public int[] getSpells();

    public int getAttack();

    public int getDefense();

    public int getMagicAttack();

    public int getMagicDefense();

    public boolean weakTo(int element);

    public void refundMP(int numTimes, int index);

    public int getBattleDrawPos();


}
