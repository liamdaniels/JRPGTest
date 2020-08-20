package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.Buffer;

public class ShopMode implements ModeController{

    private final String SHOPINFO = "items/shopinfo.csv";

    private final String TEXTBOXFILE = "textdisplaybg.png";

    private final String CURSOR = "cursor.png";
    private Texture cursorTex;

    private final int ALERTX = 2;
    private final int ALERTY = 10;

    private final int TILEDIM = 64;
    private final String BIG = "bigwindow.png";

    private final String[] WINDOW_OPTIONS = {"Buy","Sell","Exit"};
    private final String[] SHOPTIONS = {"Yes","No"};

    //display
    private Texture menuBg;
    private BitmapFont text;
    private final String FONTFILE = "Fontin-Regular.ttf";

    //managers for game
    private ItemManager itemManager;

    //displays price (for selling) and "do u wanna buy" as well as "no u can't buy lol"
    private BabyTextBox alerter;

    //used for item option selection and mode selection
    private MenuBox selector;

    //used for item descriptions
    private Texture descriptionBox;

    //name of shop currently on
    private String currentName;

    //for selecting which item to buy/sell
    private int shopSelect;

    /**
     * Modes:
     * 0 is nothing
     * 1 is buy
     * 2 is sell
     * 3 is exit
     */
    private int mode;

    /** items sold at shop */
    private int[] shopItems;
    /** Num of items being sold*/
    private int numItems;

    public ShopMode(ItemManager itemManager){

        this.itemManager = itemManager;

        mode = 0;

        menuBg = new Texture(BIG);

        //Took this code from online,  makes a new BitmapFont with the desired font file
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(FONTFILE));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 35;
        text = generator.generateFont(parameter);
        generator.dispose();

        selector = new MenuBox(WINDOW_OPTIONS,ALERTX,ALERTY - 2);
        selector.setActive(true);

        alerter = new BabyTextBox();

        int MAXSHOPITEMS = 10;
        shopItems = new int[MAXSHOPITEMS];

        cursorTex = new Texture(CURSOR);

        descriptionBox = new Texture(TEXTBOXFILE);
    }




    public void update(InputController inputs){
        //input read
        inputs.readInput();

        int inputLetterB = inputs.getB();
        int inputLetterD = inputs.getD();
        int inputLetterS = inputs.getS();
        int inputU = inputs.getUp();
        int inputD = inputs.getDown();
        int inputL = inputs.getLeft();
        int inputR = inputs.getRight();


        inputLetterB = inputs.applyDelay(inputLetterB,6, 20);
        inputLetterD = inputs.applyDelay(inputLetterD, 4, 20);
        inputLetterS = inputs.applyDelay(inputLetterS, 5, 20);
        inputU = inputs.applyDelay(inputU, 0, 20);
        inputD = inputs.applyDelay(inputD, 1, 20);
        inputL = inputs.applyDelay(inputL, 2, 20);
        inputR = inputs.applyDelay(inputR, 3, 20);



        if(mode == 0){ //selecting buy, sell, or exit

            selector.alterSelect(inputD - inputU);

            if (inputLetterD == 1){
                //switch into different mode!
                int nextMode = selector.selectOptionInt() + 1;
                switchMode(nextMode);
            }
        }
        else if(mode == 1){ //buying
            int mvmt = inputD - inputU;

            //if not saying yes/no to buying
            if (!selector.isActive()) {

                //apply up and down input
                if (shopSelect + mvmt > -1 && shopSelect + mvmt < numItems) {
                    shopSelect += mvmt;
                }

                if (inputLetterD == 1){
                    //say "wanna buy yes no"
                    alerter.foreverDisplay("Buy?",ALERTX, ALERTY,2,1,1);
                    selector.setActive(true);
                }else if (inputLetterS == 1){
                    //go back to mode 0
                    switchMode(0);
                }

            }else{
                //saying yes/no to item
                selector.alterSelect(mvmt);

                if (inputLetterD == 1){
                    //said either yes or no
                    if (selector.selectOptionInt() == 0){//yes
                        buy(shopSelect);
                    }else{//no, presumably
                        alerter.setActive(false);
                        selector.setActive(false);
                    }
                }
            }



        }

        else{ //selling

            //if not saying yes/no to buying
            if (!selector.isActive()) {
                int mvmt = inputR - inputL + (3 * inputD) - (3 * inputU);

                //apply UDLR to shopSelect
                if (shopSelect + mvmt > -1 && shopSelect + mvmt < itemManager.getNumItems()) {
                    shopSelect += mvmt;
                }

                if (inputLetterD == 1 && itemManager.getNumItems() > 0){
                    //say "wanna buy yes no"
                    alerter.foreverDisplay( "$"+ itemManager.getIndexCost(shopSelect)/2 + ". Sell?",
                            ALERTX, ALERTY,2,1,1);
                    selector.setActive(true);
                }else if (inputLetterS == 1){
                    //go back to mode 0
                    switchMode(0);
                }

            }else{
                int mvmt = inputD - inputU;

                //saying yes/no to item
                selector.alterSelect(mvmt);

                if (inputLetterD == 1){
                    //said either yes or no
                    if (selector.selectOptionInt() == 0){//yes
                        sell(shopSelect);
                    }else{//no, presumably
                        alerter.setActive(false);
                        selector.setActive(false);
                    }
                }
            }

        }
        alerter.update();
    }


    public void draw(SpriteBatch batch){

        batch.draw(menuBg, 0, 0);

        if (mode == 1){//buyin

            for (int i = 0; i < numItems; i++){
                String item = itemManager.getItemNameWithCost(shopItems[i]);
                text.draw(batch, item, (5)*TILEDIM, (12-i)*TILEDIM);
            }

            //draw cursor
            batch.draw(cursorTex, (4)*TILEDIM, (11.25f-shopSelect)*TILEDIM);


            batch.draw(descriptionBox,(0.5f)*TILEDIM, TILEDIM*(-2));
            text.draw(batch, itemManager.getDescription(shopItems[shopSelect]), TILEDIM, TILEDIM);

        }else if (mode == 2){//selling

            for (int i = 0; i < itemManager.getNumItems(); i++){
                String item = itemManager.getIndexName(i);
                int xPlacement = (i % 3)*4 + 1;
                int yPlacement = 12 - (i / 3);
                text.draw(batch, item, (xPlacement)*TILEDIM, (yPlacement-0.5f)*TILEDIM);
            }

            int xCPlacement = (shopSelect % 3)*4;
            int yCPlacement = 12 - (shopSelect / 3);
            //draw cursor
            batch.draw(cursorTex, (xCPlacement)*TILEDIM, (yCPlacement-1)*TILEDIM);


        }


        selector.draw(batch);
        alerter.draw(batch);

        //draw shop name
        text.draw(batch,currentName,2*(TILEDIM),12.5f*(TILEDIM));

        //tell user how much money they have
        text.draw(batch,"Money: \n$"+MenuMode.money,14*(TILEDIM),12*(TILEDIM));

    }


    public void dispose(){
        text.dispose();
        selector.dispose();
        alerter.dispose();
        menuBg.dispose();
    }





    public void switchMode(int m){
        mode = m;
        shopSelect = 0;
        switch(m){
            case 0://not buy or sell
                selector.loadNewOptions(WINDOW_OPTIONS);
                selector.setActive(true);
                break;
            case 1://buy
            case 2://sell
                selector.loadNewOptions(SHOPTIONS);
                selector.setActive(false);
                break;
            case 3://exit
                selector.loadNewOptions(WINDOW_OPTIONS);
                mode = 0;
                selector.setActive(true);
                TestGame.upcomingMode = "world";
                break;
        }
    }



    public void loadShop(int shopNum){

        try{
            BufferedReader buff = new BufferedReader(new FileReader(SHOPINFO));

            //first line is header
            buff.readLine();

            //load shop. Skip lines until at shop
            for (int i = 0; i < shopNum; i++){
                buff.readLine();
            }

            //following line is this shop
            String[] thisShop = buff.readLine().split(",");

            currentName = thisShop[0];

            //num items in this shop is the len of above array (minus one b/c shop name)
            numItems = thisShop.length - 1;
            for(int i = 0;i < numItems; i++){
                shopItems[i] = Integer.parseInt(thisShop[i+1]);
            }


             buff.close();
        }
        catch(IOException e){
            System.out.println("IO EXCEPTION shop mode");
        }
        shopSelect = 0;
    }


    public void buy(int index){
        int itemToBuy = shopItems[index];

        //first, check if buying is possible
        if (itemManager.isFull()){
            alerter.displayNewMessage("Your items are full!", ALERTX, ALERTY, 60);
        }else if(itemManager.getCost(index) > MenuMode.money){
            alerter.displayNewMessage("Not enough money!", ALERTX, ALERTY, 60);
        }else{//ok, can buy
            alerter.displayNewMessage("Ka-ching!", ALERTX, ALERTY, 60);
            itemManager.addItem(index);
            MenuMode.money -= itemManager.getCost(index);
        }
        selector.setActive(false);
    }

    public void sell(int index){
        alerter.displayNewMessage("Sold!", ALERTX, ALERTY, 60);
        MenuMode.money += itemManager.getIndexCost(index)/2;
        itemManager.remove(index);

        selector.setActive(false);

        //decrement shop select, but don't let below 0
        shopSelect -= 1;
        if (shopSelect < 0){
            shopSelect = 0;
        }
    }


}
