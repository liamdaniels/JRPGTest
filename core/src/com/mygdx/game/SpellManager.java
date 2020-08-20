package com.mygdx.game;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Ideas behind SpellManager:
 *
 * Spells are loaded in with a file on startup, with each spell corresponding to an int
 * Each BattlePlayer has a list of ints which are their spells
 *
 *
 */
public class SpellManager {

    /** num spells in the game */
    private final int MAXSPELLS = 50;

    private final String SPELLDATA = "battle/magicdata.csv";

    //TODO: this. lol
    /**
     * Spell elements. Here they are
     * 0: Healing spell. Heals ppl
     * 1: Fire
     * 2: Electric
     * 3: Ice
     * 4: Poison
     * 5: Grass
     * 6: Lawful
     * 7: Holy
     * idk lol, we'll see for rest
     * also maybe add status effect ones
     */
    private int[] elements;

    private int[] magnitudes;

    private int[] costs;

    private String[] names;

    private int[] effects;

    private boolean[] multiTarget;

    private int[] animations;

    private int numSpells;

    public SpellManager(){
        elements = new int[MAXSPELLS];
        magnitudes = new int[MAXSPELLS];
        names = new String[MAXSPELLS];
        costs = new int[MAXSPELLS];
        effects = new int[MAXSPELLS];
        multiTarget = new boolean[MAXSPELLS];
        animations = new int[MAXSPELLS];
        numSpells=0;

        //load magic info into buffers
        try {
            BufferedReader buff = new BufferedReader(new FileReader(SPELLDATA));

            //first line is header
            buff.readLine();

            //start with next line
            String currentLine = buff.readLine();

            while(currentLine != null){
                String[] data = currentLine.split(",");

                String name = data[0];
                int mag = Integer.parseInt(data[1]);
                int elemt = Integer.parseInt(data[2]);
                int cost = Integer.parseInt(data[3]);
                int effct = Integer.parseInt(data[4]);
                boolean multi = Boolean.parseBoolean(data[5]);
                int anim = Integer.parseInt(data[6]);

                addSpell(name,mag,elemt,cost,effct,multi,anim);

                //go to next line
                currentLine = buff.readLine();
            }

            buff.close();
        }catch (IOException e){
            System.out.println("ioexception in spell manager");
        }



    }

    public void addSpell(String name, int mag, int element, int cost, int effect, boolean multi,int anim){
        elements[numSpells] =element;
        magnitudes[numSpells] =mag;
        names[numSpells] =name;
        costs[numSpells] = cost;
        effects[numSpells] = effect;
        multiTarget[numSpells] = multi;
        animations[numSpells] = anim;
        numSpells++;
    }


    public void castSpellOutside(int index, BattlePlayer caster, BattlePlayer target){
        if (usedOnPlayer(index)) { //for now, healing!
            int cost = costs[index];
            if (cost <= caster.getMP()) {
                caster.changeMP((-1)*cost);

                int mgAtk = caster.getMagicAttack();
                int mgDef = caster.getMagicDefense();
                int healing = DamageCalculator.calcMagicHeal(mgAtk,mgDef,magnitudes[index]);
                target.changeHP(healing);

                MenuMode.alertQueue.add(caster.getName() + " used " + names[index]);
                MenuMode.alertQueue.add(target.getName() + " healed " + healing + " HP");
            }else{
                MenuMode.alertQueue.add("Not enough MP!");
            }
        }
        else{
            MenuMode.alertQueue.add("Can't use this");
        }

    }

    /**
     * Casts battle spell
     * @param index  Spell index (not player's spells index)
     * @param target Thing being cast on
     */
    public void castBattleSpell(int index, BattlePlayer caster, Battleable target){
        if (elements[index] == 0){ //healing spell!
            int mgAtk = caster.getMagicAttack();
            int mgDef = caster.getMagicDefense();
            int healing = DamageCalculator.calcMagicHeal(mgAtk,mgDef,magnitudes[index]);
            target.loadReaction("changeHP",healing, effects[index]);

        }else{ //for now
            //damage spell
            int damage = DamageCalculator.calcMagicDamage(caster,target, magnitudes[index], elements[index]);
            target.loadReaction("changeHP",damage*(-1), effects[index]);

        }
        caster.changeMP((-1)*costs[index]);
        if (animations[index] > -1){ //there is an animation to display
            BattleAnimator.loadAnimation(animations[index]);
        }
    }

    public boolean enoughMP(int index, BattlePlayer b){
        return b.getMP() >= costs[b.getSpells()[index]];
    }

    public boolean usedOnPlayer(int index){
        // TODO: status, too
        return elements[index] == 0;
    }

    public String getName(int index){
        return names[index];
    }

    public String[] getNames(int[] spellIndices, int numSpells){
        String[] spellNames = new String[numSpells];
        for(int i = 0; i <numSpells; i++){
            spellNames[i] = names[spellIndices[i]];
        }
        return spellNames;
    }

    public String[] getNamesAndCosts(int[] spellIndices, int numSpells){
        String[] spellNames = new String[numSpells];
        for(int i = 0; i <numSpells; i++){
            spellNames[i] = names[spellIndices[i]] + "  (" + costs[spellIndices[i]] +" MP)";
        }
        return spellNames;
    }

    //same method as above, but without "MP" after, so it can fit in a tiny window
    public String[] getNamesAndCostsSmall(int[] spellIndices, int numSpells){
        String[] spellNames = new String[numSpells];
        for(int i = 0; i <numSpells; i++){
            spellNames[i] = names[spellIndices[i]] + " (" + costs[spellIndices[i]] +")";
        }
        return spellNames;
    }

    public boolean multi(int index){
        return multiTarget[index];
    }

    public int getCost(int index){
        return costs[index];
    }

}
