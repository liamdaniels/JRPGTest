package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class BattlePlayer implements Battleable{

    private final int TILEDIM = 64;
    private final int MAXSPELLS = 50;

    private ItemManager itemManager;
    private SpellManager spellManager;

    private int[] spells;
    private int numSpells;

    private TextureRegion battleSprite;

    private Texture defaultTex;

    private String currentReaction;
    private int currentReactChange;
    private int currentReactEffect;

    private int battleDrawPos;


    private String name;

    private String[] options;

    private int level;
    private int exp;

    private int expToNextLevel;

    private int[] jobExps;


    //Just doing example stats for now
    private int mp;
    private int hp;

    private int maxHp;
    private int maxMp;

    //stats
    private int magicAttack;
    private int magicDefense;
    private int attack;
    private int defense;
    private int speed;

    private int baseSpeed;


    /** "propensity" stats
     * Basically like base stats in Pokemon
     * Determine how much character's stats increase at level up */
    private final int baseVitality; //for hp
    private final int baseBigbrain; //for mp
    private final int baseSharpness; //for magic attack
    private final int baseHardness; // for magic defense
    private final int baseChunk; //for defense
    private final int baseStrength; //for attack


    private int vitality; //for hp
    private int bigbrain; //for mp
    private int sharpness; //for magic attack
    private int hardness; // for magic defense
    private int chunk; //for defense
    private int strength; //for attack


    /** Player's job */
    private int currentJob;

    private boolean canSwitchJob;



    /**
     * The animations
     * 0: Idle
     * 1: Attack stab
     * 2: Item use
     * 3: Magic cast
     * 4: Oof
     * 5: PFP on menu
     */
    private int currentAnimation;
    /** Number of frames current animation is on, and the num frames it will be on when done */
    private int currentAniFrames;
    private int currentFrameGoal;

    /**
     * Effect info
     */
    private int effectFrameGoal;
    private int effectFramesThru;
    private int effectType;

    /**
     * Val 0 is weapon
     * Val 1 is armor
     * Val 2 is other
     */
    private int[] equippedItems;
    private int[] equipVals;


    //TODO: make "character files" or smth to store data eg base stats

    public BattlePlayer(String name, String sprite, String[] options,
                        int level, int exp,
                        int vitality, int bigbrain, int sharpness, int hardness, int chunk, int strength,
                        int speed,
                        int baseJob,
                        boolean canSwitch
                        ){

        this.name = name;
        defaultTex = new Texture(sprite + "/default.png");
        battleSprite = new TextureRegion(defaultTex);
        this.options = options;

        this.level = level;
        this.exp = exp;
        this.expToNextLevel = calcNextLevelExp();

        this.baseVitality = vitality;
        this.baseBigbrain = bigbrain;
        this.baseSharpness = sharpness;
        this.baseHardness = hardness;
        this.baseChunk = chunk;
        this.baseStrength = strength;

        this.vitality = vitality;
        this.bigbrain = bigbrain;
        this.sharpness = sharpness;
        this.hardness = hardness;
        this.chunk = chunk;
        this.strength = strength;

        this.baseSpeed = speed;
        this.speed = speed;

        equippedItems = new int[4];
        equipVals = new int[3];
        for(int i = 0;i < 3; i++){
            equippedItems[i] = -1;
            equipVals[i] = 0;
        }

        calcStats();

        this.hp = this.maxHp;
        this.mp = this.maxMp;

        currentReaction = "";
        currentReactChange = 0;

        currentReactEffect = -1;
        effectType = -1;

        spells = new int[MAXSPELLS];
        numSpells = 0;

        setJob(baseJob);
        canSwitchJob = canSwitch;

        jobExps = new int[JobManager.jobs.length];


    }


    public String[] getOps(){
        return options;
    }

    //graphics stuff


    public void draw(SpriteBatch batch, int x, int y){
        //little bit of updating here, oopsies
        //if animation is just starting, switch frame
        if (currentAniFrames == 0){
            setFrame(currentAnimation);
        }

        //finish if ani is done
        if (currentAniFrames == currentFrameGoal){
            //reset to normal
            setAnimation(0,-1);
        }else{
            currentAniFrames++;
        }


        batch.draw(battleSprite, x*TILEDIM, y*TILEDIM);
//
//        if (effectType != -1){
//            if (BattleAnimator.effects.getFrame() != effectType){
//                BattleAnimator.effects.setFrame(effectType);
//            }
//
//            BattleAnimator.effects.draw(batch, x*TILEDIM, y*TILEDIM);
//
//            if (effectFramesThru == effectFrameGoal){
//                effectType = -1;
//            }
//            effectFramesThru++;
//        }

    }

    public void setAnimation(int animation, int duration){
        this.currentAnimation = animation;
        this.currentAniFrames = 0;
        this.currentFrameGoal = duration;
    }

    public void setFrame(int frame){
        if (frame == 5){ //pfp on menu
            battleSprite.setRegion(6*TILEDIM,0,128,128);
        }else if (frame == 1){
            battleSprite.setRegion(frame*TILEDIM,0,96,128);
        }else if (frame == 2){
            battleSprite.setRegion((int)((frame+0.5)*TILEDIM),0,96,128);
        }
        else if (frame == 0){
            battleSprite.setRegion(frame*TILEDIM,0,64,128);
        }else{
            battleSprite.setRegion((frame+1)*TILEDIM,0,64,128);
        }


    }

    public void loadEffect(int eff, int duration){
        effectType = eff;
        effectFrameGoal = duration;
        effectFramesThru = 0;
    }




    //battle mechanic stats and methods
    //TODO: make more advanced


    //actions
    public void attack(Battleable target){
        if (!isDead()) {
            int dmg = DamageCalculator.calcAttackDamage(this,target);
            target.loadReaction("changeHP", (-1)*dmg, 0);
        }
    }








    //reactions
    public void changeHP(int change){

        hp += change;
        if (hp > maxHp){
            hp = maxHp;
        }
        if (hp < 0) {
            hp = 0;
        }

    }


    public void changeMP(int change){
        mp += change;
        if (mp > maxMp){
            mp = maxMp;
        }
        if (mp < 0) {
            mp = 0;
        }

    }











    public void setManagers(ItemManager i, SpellManager s){
        itemManager =i;
        spellManager = s;
    }




    //perform action and reaction


    public void performAction(String action, Battleable target, int index){
        int ATTACK_ANI = 2;
        if (!isDead()) {
            switch (action) {
                case "Attack":
                    attack(target);
                    setAnimation(1,60);
                    BattleAnimator.loadAnimation(ATTACK_ANI,target.getBattleDrawPos());
                    break;
                case "Do Nothing":
                    break;
                case "Item":
                    if (!isDead()) { itemManager.useBattleItem(index, target); }
                    setAnimation(2,60);
                    break;
                case "Magic":
                    if (!isDead()) { spellManager.castBattleSpell(spells[index],this,target); }
                    setAnimation(3,60);
                    break;
                default:
                    System.out.println("???? no action found");
            }
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
                    System.out.println("uh oh it's default. human react");
            }
        }


        currentReaction = "";
        currentReactChange = 0;
        currentReactEffect = -1;
    }


    //perform action and reaction: messages

    public void performMessage(String action, String used){
        switch(action){
            case "Attack":
                BattleManager.addToMessageQueue(name + " attacks!");
                break;
            case "Do Nothing":
                BattleManager.addToMessageQueue(name + " did nothing");
                break;
            case "Item":
                BattleManager.addToMessageQueue(name + " used " + used + "!");
                break;
            case "Magic":
                BattleManager.addToMessageQueue(used);
                break;
            default:
                System.out.println("???? no action found");
        }
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
                System.out.println("uh oh it's default. human react");
        }
    }








    //message methods (for advanced messages that are more than 1 line of code)

    public void hpChangeMessage(int change){
        if (change < 0) {
            BattleManager.addToMessageQueue(name + " takes " + (-1)*change + " damage");
        }else if(change > 0){
            BattleManager.addToMessageQueue(name + " gains " + change + " HP");
        }else if(change == 0){
            BattleManager.addToMessageQueue(name + " gains nada");
        }
        if (hp + change <= 0){
            BattleManager.addToMessageQueue(name + " died D:");
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
        return "Player";
    }


    public int getSpeed(){return speed;}

    public void setSpeed(int s){speed = s;}


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getHP(){
        return hp;
    }

    public int getMP(){
        return mp;
    }

    public String[] getOptions() {
        return options;
    }

    public int getLevel() {
        return level;
    }

    public int getExp() {
        return exp;
    }

    public int getMaxHP() {
        return maxHp;
    }

    public int getMaxMP() {
        return maxMp;
    }

    public int getMagicAttack() {
        return magicAttack;
    }

    public int getMagicDefense() {
        return magicDefense;
    }

    public int getAttack() {
        return attack;
    }

    public int getDefense() {
        return defense;
    }

    public int getVitality() {
        return vitality;
    }

    public int getBigbrain() {
        return bigbrain;
    }

    public int getSharpness() {
        return sharpness;
    }

    public int getHardness() {
        return hardness;
    }

    public int getChunk() {
        return chunk;
    }

    public int getStrength() {
        return strength;
    }

    public int[] getSpells() {
        return spells;
    }

    public int getNumSpells() {
        return numSpells;
    }

    //calculation stuff & levels



    public void calcStats(){
        this.maxHp = (int)(statCalcFunction(vitality)*2) + 10;
        this.maxMp = (int)(statCalcFunction(bigbrain)*1.5);
        this.magicAttack = statCalcFunction(sharpness);
        this.magicDefense = statCalcFunction(hardness);
        this.attack = statCalcFunction(strength) + equipVals[0];
        this.defense = statCalcFunction(chunk) + equipVals[1];
    }

    public int statCalcFunction(int baseStat){
        int returnStat;
        if (level <= 20){
            returnStat = (int)(Math.sqrt(level*100)*(baseStat/60.0));
        }
        else{
            returnStat = (int)((baseStat/60.0)*(level + 25));
        }


        if (returnStat < 0){//make sure no negative val
            returnStat = 0;
        }

        return returnStat;
    }

    public int getEquip(int index){
        return equippedItems[index];
    }

    public int equip(int item, int index){
        int oldEquip = equippedItems[index];
        equippedItems[index] = item;
        equipVals[index] = itemManager.getMagnitude(item);
        calcStats();
        return oldEquip;
    }



    //magic stuff

    public void addSpell(int spell){
        if(numSpells >= MAXSPELLS){ throw new RuntimeException("too many spells"); }
        spells[numSpells] = spell;
        numSpells++;
    }


    //job stuff

    public void setJob(int job){
        //TODO: this
        currentJob = job;

        vitality = baseVitality + JobManager.jobs[job].vital;
        bigbrain = baseBigbrain + JobManager.jobs[job].bigB;
        strength = baseStrength + JobManager.jobs[job].strength;
        chunk = baseChunk + JobManager.jobs[job].chunk;
        sharpness = baseSharpness + JobManager.jobs[job].sharp;
        hardness = baseHardness + JobManager.jobs[job].hard;
        speed = baseSpeed + JobManager.jobs[job].speed;

        calcStats();





    }

    public boolean canSwitchJob(){
        return canSwitchJob;
    }

    public int getJob(){
        return currentJob;
    }

    public boolean addJobExp(int add){
        if (currentJob == 0){ return false; }
        boolean gainedLevel = false;

        int currentJobExp = jobExps[currentJob];
        jobExps[currentJob] += add;

        //job exp threshholds
        int[] THRESH = {20, 40, 60, 80, 100};

        //if add pushes us over threshold, all spell
        if (jobExps[currentJob] >= THRESH[0] && currentJobExp < THRESH[0]){
            addSpell(JobManager.jobs[currentJob].abil1);
            gainedLevel = true;
        }
        if (jobExps[currentJob] >= THRESH[1] && currentJobExp < THRESH[1]){
            addSpell(JobManager.jobs[currentJob].abil2);
            gainedLevel = true;
        }
        if (jobExps[currentJob] >= THRESH[2] && currentJobExp < THRESH[2]){
            addSpell(JobManager.jobs[currentJob].abil3);
            gainedLevel = true;
        }
        if (jobExps[currentJob] >= THRESH[3] && currentJobExp < THRESH[3]){
            addSpell(JobManager.jobs[currentJob].abil4);
            gainedLevel = true;
        }
        if (jobExps[currentJob] >= THRESH[4] && currentJobExp < THRESH[4]){
            addSpell(JobManager.jobs[currentJob].abil5);
            gainedLevel = true;
        }

        return gainedLevel;
    }



    //exp stuff


    public boolean addExp(int add){
        this.exp += add;
        boolean leveledUp = false;
        while (exp > expToNextLevel){
            exp -= expToNextLevel;
            levelUp();
            leveledUp = true;
        }
        return leveledUp;
    }

    public void levelUp(){
        level ++    ;
        calcStats();
        expToNextLevel = calcNextLevelExp();
    }


    private int calcNextLevelExp(){
        return (int)Math.pow(level, 3);
    }

    public int getExpForNextLevel(){ return expToNextLevel - exp;}





    public void dispose(){
        battleSprite= null;
        defaultTex.dispose();
    }

    public boolean weakTo(int element){
        //for now, players have no weaknesses
        return false;
    }


    /**
     * Refunds MP. Used for multitarget spells
     * @param numTimes Number of times spell was cast extra
     * @param index    Player's index of spell
     */
    public void refundMP(int numTimes, int index){
        if (numTimes < 1){ return; }
        System.out.println("cost of " + spellManager.getName(spells[index]) +  ": " + spellManager.getCost(spells[index]));
        System.out.println("NUM TIMES:" + numTimes);
        int refund = numTimes * spellManager.getCost(spells[index]);
        changeMP(refund);
    }


    public int getBattleDrawPos(){
        return battleDrawPos;
    }

    public void setBattleDrawPos(int battleDrawPos) {
        this.battleDrawPos = battleDrawPos;
    }


}
