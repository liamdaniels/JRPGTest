package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;

public class Enemy implements Battleable{


    /**
     * Helper class for each enemy attack
     */
    private class EnemyAttack{
        public String name;
        public String displayMsg;

        /** 0 for physical, 1 for special(magic), 2 for status*/
        public int pss;

        public int baseDmg;
        public int effect;

        public int animation;

        public EnemyAttack(){
        }
    }

    private final int NUMATKS = 4;
    private final int TILEDIM = 64;

    private int battleDrawPos;
    private int song;

    private int hp;
    private int mp;
    private int magicAttack;
    private int magicDefense;
    private int attack;
    private int defense;
    private int speed;

    private int moneyYield;

    private int expYield;

    private String name;

    private Texture sprite;

    private EnemyAttack[] attacks;
    private int currentAttack;


    private String currentReaction;
    private int currentReactChange;
    private int currentReactEffect;

    private int frameGoal;
    private int framesThru;
    private int effectType;

    //elemental weaknesses, repped by ints
    private ArrayList<Integer> weaknesses;

    public Enemy(){

        sprite = new Texture("battlepics/monstertest.png");

        currentReaction = "";
        currentReactChange = 0;
        currentReactEffect = -1;
        effectType = -1;

        attacks = new EnemyAttack[NUMATKS];
        for (int i = 0; i < NUMATKS; i++){
            attacks[i] = new EnemyAttack();
        }

        weaknesses = new ArrayList<Integer>();
    }



    public void draw(SpriteBatch batch, int x, int y){

        batch.draw(sprite, x*TILEDIM, y*TILEDIM);

//        if (effectType != -1){
//            if (BattleAnimator.effects.getFrame() != effectType){
//                BattleAnimator.effects.setFrame(effectType);
//            }
//
//            BattleAnimator.effects.draw(batch, x*TILEDIM, y*TILEDIM);
//
//            if (framesThru == frameGoal){
//                effectType = -1;
//            }
//            framesThru++;
//        }
    }

    public void loadEffect(int eff, int duration){
        effectType = eff;
        frameGoal = duration;
        framesThru = 0;
    }








    // battle mechanic methods


    //actions
    public void attack(Battleable target){
        if (!isDead()) {
            int dmg = DamageCalculator.calcAttackDamage(this,target);
            target.loadReaction("changeHP", (-1)*dmg, 0);
        }
    }

    public void generateAttack(){
        currentAttack = (int)(Math.random()*NUMATKS);
    }








    //reactions
    public void changeHP(int change){

        hp += change;

        if (hp < 0) {
            hp = 0;
        }

    }




    //perform action and reaction


    public void performAction(String action, Battleable target, int index){
        //here action does not matter. generate action using attacks list
        if(attacks[currentAttack].name.equals("Attack")) { //default attack, use defaults
            int ATTACK_ANI = 2;
            int dmg = DamageCalculator.calcAttackDamage(this,target);
            int ef = 0;
            target.loadReaction("changeHP", (-1)*dmg, ef);
            BattleAnimator.loadAnimation(ATTACK_ANI,target.getBattleDrawPos());
        }
        else if(attacks[currentAttack].pss == 0){//physical attack
            //TODO: calculation
            int dmg = (-1)*attacks[currentAttack].baseDmg;
            int ef= attacks[currentAttack].effect;
            target.loadReaction("changeHP", dmg, ef);
        }else if(attacks[currentAttack].pss == 1){//special
            //TODO
        }else{ //status move
            //TODO
        }

        if (attacks[currentAttack].animation > -1){//there is animation
            BattleAnimator.loadAnimation(attacks[currentAttack].animation);
        }

    }

    public void performReaction(){
        String react = currentReaction;
        int change = currentReactChange;
        if (!isDead()) {
            switch (react) {
                case "changeHP":
                    changeHP(change);


                    break;
                case "":
                    break;
                default:
                    System.out.println("uh oh it's default. enemy react");
            }
        }


        currentReaction = "";
        currentReactChange = 0;
        currentReactEffect = -1;
    }


    //perform action and reaction: messages

    public void performMessage(String action, String used){
        //roll! do not do this when actually attacking, tho
        generateAttack();

        //here action does not matter. generate action using attacks list
        String msg = attacks[currentAttack].displayMsg;
        BattleManager.addToMessageQueue(name + " " + msg);
    }

    public void performReactionMessage(){
        String react = currentReaction;
        int change = currentReactChange;

        if (isDead()){
            BattleManager.addToMessageQueue(name + " was already gone");
            return;
        }

        switch(react){
            case "changeHP":
                hpChangeMessage(change);
                loadEffect(currentReactEffect, 60);
                break;
            case "":
                break;
            default:
                System.out.println("uh oh it's default. in enemy react");
        }
    }





    //message methods (for advanced messages that are more than 1 line of code)

    public void hpChangeMessage(int change){
        if (change < 0) {
            BattleManager.addToMessageQueue(name + " takes " + (-1)*change + " damage");
        }else if(change > 0){
            BattleManager.addToMessageQueue(name + " gains " + change + " HP");
        }
        if (hp+change <= 0){
            BattleManager.addToMessageQueue(name + " died");
        }
    }






    //load reaction

    public void loadReaction(String react, int change, int effect){
        this.currentReaction = react;
        this.currentReactChange = change;
        this.currentReactEffect = effect;
    }






    //getters, setters

    public boolean isDead(){
        return hp == 0;
    }

    public String getType(){
        return "Enemy";
    }

    public int getSpeed(){return speed;}

    public void setSpeed(int s){speed = s;}


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMp() {
        return mp;
    }

    public void setMp(int mp) {
        this.mp = mp;
    }


    public int getMagicAttack() {
        return magicAttack;
    }

    public void setMagicAttack(int magicAttack) {
        this.magicAttack = magicAttack;
    }

    public int getMagicDefense() {
        return magicDefense;
    }

    public void setMagicDefense(int magicDefense) {
        this.magicDefense = magicDefense;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public void setSprite(String sprite) {
        //TODO: is this bad? oh no
        if(this.sprite != null) {
            this.sprite.dispose();
        }
        this.sprite = new Texture(sprite);
    }

    public void setExpYield(int e){
        expYield = e;
    }

    public int getExpYield(){
        return expYield;
    }

    public void setAttack(int index, String name, String message, int pss, int dmg, int effect, int anim){
        attacks[index].name = name;
        attacks[index].displayMsg = message;
        attacks[index].pss = pss;
        attacks[index].baseDmg = dmg;
        attacks[index].effect = effect;
        attacks[index].animation = anim;
    }

    public void clearWeaknesses(){
        weaknesses.clear();
    }

    public void addWeakness(int w){
        weaknesses.add(w);
    }

    /**
     * Bad polymorphism
     * @return nothing, never use this method
     */
    public int[] getSpells(){
        return new int[1];
    }

    /**
     * Bad polymorphism returns
     */
    public void refundMP(int a, int b){}


    public void setMoneyYield(int m){moneyYield = m;}

    public int getMoneyYield() {
        return moneyYield;
    }

    public boolean weakTo(int element){
        return weaknesses.contains(element);
    }

    public void dispose(){
        sprite.dispose();
    }

    public int getBattleDrawPos() {
        return battleDrawPos;
    }

    public void setBattleDrawPos(int battleDrawPos) {
        this.battleDrawPos = battleDrawPos;
    }

    public int getSong() {
        return song;
    }

    public void setSong(int song) {
        this.song = song;
    }
}
