package com.mygdx.game;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Collections;


public class ItemManager {



    private class Item{
        public String name;
        public int magnitude;
        public int function;
        public int effect;
        public int cost;
        public String description;

        public Item(String n, int m, int f, int e, int c, String d){
            name = n;
            magnitude = m;
            function = f;
            effect = e;
            cost = c;
            description = d;
        }

    }

    private final String INFO = "items/iteminfo.csv";

    private final int MAXITEMSIZE = 30;
    private final int GAMEITEMSTOTAL = 50;


    //item functions
    private int[] itemFuncs;
    //item "magnitudes"
    private int[] itemMags;
    //item names, of coures
    private String[] itemNames;
    //item costs
    private int[] itemCosts;
    //item effects
    private int[] itemEffects;

    //item data
    private Item[] itemData;


    //the items, by number
    private int[] items;

    //number of items
    private int numItems;

    private ArrayList<Integer> removalQueue;

    /**
     * Constructs an ItemManager
     *
     * The ItemManager class uses two arrays, one for item names and one
     *  of ints, which correspond to item functions, and another which
     *  corresponds to "how much," either damage or healing.
     *
     *
     */
    public ItemManager(){

        //make empty items list

        numItems = 0;
        itemData = new Item[GAMEITEMSTOTAL];

        items = new int[MAXITEMSIZE];

        removalQueue = new ArrayList<Integer>();


        //load item data
        try {
            BufferedReader buff = new BufferedReader(new FileReader(INFO));

            //first line is header
            buff.readLine();

            //now, load item data
            //start with next line
            String currentLine = buff.readLine();

            //index
            int i = 0;

            while(currentLine != null){
                String[] data = currentLine.split(",");

                String name = data[0];
                int mag = Integer.parseInt(data[1]);
                int func = Integer.parseInt(data[2]);
                int cost = Integer.parseInt(data[3]);
                int effct = Integer.parseInt(data[4]);
                String desc = data[5];

                itemData[i] = new Item(name,mag,func,effct,cost,desc);

                //go to next line
                currentLine = buff.readLine();
                i++;
            }


            buff.close();
        }
        catch(IOException e){
            System.out.println("io exception in item manager setup");
        }



    }

    /**
     * GENERAL KEY FOR ITEM FUNCTIONS:
     *
     * 0: healing/food (heals hp)
     * 1: damage to enemy
     * 2: equip weapon
     * 3: equip armor
     * 4: equip other (+def)?
     * 5: equip other (+speed)?
     * 6: equip other (no status or smth)?
     * //TODO: add more features
     *
     *
     *
     *
     *
     *
     *
     *
     *
     */


    /**
     * Adds an item.
     *
     * @param itemType  Type of item, from itemData
     * @return  true if item properly added, else false
     */
    public boolean addItem(int itemType){
        //if cannot add more items, return false
        if (numItems == MAXITEMSIZE){   return false; }

        items[numItems] = itemType;

        numItems++;

        return true;
    }

    /**
     * Use an item outside of battle.
     * @param index The item to use
     * @param player The player on whom item is used, if any
     */
    public void useItem(int index, BattlePlayer player){
        switch(itemData[items[index]].function){

            case 0: //heals hp, consumable
                player.changeHP(itemData[items[index]].magnitude);
                MenuMode.alertQueue.add(player.getName() +" used "+ itemData[items[index]].name);
                MenuMode.alertQueue.add("Healed " + itemData[items[index]].magnitude + " HP!");
                remove(index);
                break;
            case 1:
                MenuMode.alertQueue.add("You can't use that!");
                break;
            case 2:
                String equipName = itemData[items[index]].name;
                int prevEquip = player.equip(items[index],0);
                if (prevEquip < 0){//was nothing there. remove item
                    remove(index);
                }else{
                    items[index] = prevEquip;
                }
                MenuMode.alertQueue.add("Equipped " + equipName + "!");
                break;
            case 3:
                String equipArmorName = itemData[items[index]].name;
                int prevEquipp = player.equip(items[index],1);
                if (prevEquipp < 0){//was nothing there. remove item
                    remove(index);
                }else{
                    items[index] = prevEquipp;
                }
                MenuMode.alertQueue.add("Equipped " + equipArmorName + "!");
                break;
            default:
                //do nothing
                break;
        }

    }


    public void useBattleItem(int index,Battleable target){
        switch(itemData[items[index]].function){

            case 0: //heals hp, consumable
                target.loadReaction("changeHP",itemData[items[index]].magnitude, itemData[items[index]].effect);
                removalQueue.add(index);
                break;
            case 1: //deal dmg
                target.loadReaction("changeHP",(-1)*itemData[items[index]].magnitude,
                        itemData[items[index]].effect);
                removalQueue.add(index);
                break;
            case 2:
            case 3:
            case 4:
                BattleManager.messageQueue.add("Nothing happened.");
                break;
            default:
                //do nothing
                break;
        }
    }


    /**
     * Removes item from lists, then shifts lists back
     * @param index the number item to get rid of
     */
    public void remove(int index){
        //shift items back 1
        for (int i = index; i < numItems-1; i++){
            items[i] = items[i+1];
        }

        //decrease number of items
        numItems--;

    }

    public String[] getExistingItemNames(){
        String[] names = new String[numItems];
        for (int i = 0; i < numItems; i++){
            names[i] = itemData[items[i]].name;
        }
        return names;
    }


    public int getNumItems() {
        return numItems;
    }


    public boolean usedOnPlayer(int index){
        switch(itemData[items[index]].function){
            case 0: //food or healing hp
                return true;
            case 1: //battle damage item
                return false;
            case 2:
            case 3:
            case 4:
                return true;
            default:
                return false;

        }
    }

    public boolean usedOnEnemy(int index){
        switch(itemData[items[index]].function){
            case 0: //food or healing hp
                return false;
            case 1: //battle damage item
                return true;
            default:
                return false;

        }
    }


    public boolean isFull(){
        return numItems >= MAXITEMSIZE;
    }

    /**
     * Removes all items in the removalQueue.
     * Does so in reverse order so that shifting back
     *  doesn't affect things.
     */
    public void clearFromQueue(){
        Collections.sort(removalQueue);

        for(int i = removalQueue.size() - 1; i >= 0; i--){
            remove(removalQueue.get(i));
        }

        removalQueue.clear();
    }


    public String getItemName(int dataIndex){
        if(dataIndex < 0){ return "   ";}
        return itemData[dataIndex].name;
    }

    public String getItemNameWithCost(int dataIndex){
        return itemData[dataIndex].name + "........................." + itemData[dataIndex].cost;
    }

    /**
     * Gets name of item by index of user's inventory
     * @param inventoryIndex    Index of inventory
     * @return                  Name of that item
     */
    public String getIndexName(int inventoryIndex){
        return itemData[items[inventoryIndex]].name;
    }


    public int getCost(int dataIndex){
        return itemData[dataIndex].cost;
    }

    public int getIndexCost(int invIndex){
        return itemData[items[invIndex]].cost;
    }

    public int getMagnitude(int index){
        return itemData[index].magnitude;
    }

    public String getDescription(int index){
        return itemData[index].description;
    }

    public String getDescriptionByInventory(int inventoryIndex){
        return itemData[items[inventoryIndex]].description;
    }



}
