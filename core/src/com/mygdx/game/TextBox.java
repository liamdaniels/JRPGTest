package com.mygdx.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;

public class TextBox {

    //x and y at which background text box spawns
    private final int BOX_X = 32;
    private final int BOX_Y = 10;

    // max number of letters that fit in a line nicely. Figured out with science
    private final int MAXLINELEN = 45;

    private final String TEXTBOXFILE = "textdisplaybg.png";
    private final String FONTFILE = "Fontin-Regular.ttf";


    //for control codes
    private boolean tpCodeOn;
    private boolean menuCodeOn;
    private int nextTp1;
    private int nextTp2;

    //one menubox used for potential option selection
    private MenuBox menuBox;

    private Texture backgroundBox;

    private BitmapFont textFont;

    //Text to display.
    private String[] textToDisplay;

    //Current talker (if applicable)
    private String currentTalker;

    //line of text (in textToDisplay) currently being displayed at top
    private int textLine;

    private int menuOpSelected;

    private boolean battleTriggered;
    private boolean shopTriggered;

    private String nextCutscene;

    // True as long as text is being displayed.
    // Becomes true when new text must be displayed.
    // Becomes false when textToDisplay[textLine] is the empty string.
    private boolean displayingText;

    private ItemManager itemManager;

    private boolean itemTrigger;
    private String currentItem;


    private ArrayDeque<Integer> flagQueue;

    /**
     * Constructs a textbox
     * Makes a font and makes open displayable text array. Done when game is loaded and then never again.
     */
    public TextBox(){
        //Took this code from online, presumably makes a new BitmapFont with the desired font file
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(FONTFILE));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 35;
        textFont = generator.generateFont(parameter);
        generator.dispose();

        backgroundBox = new Texture(TEXTBOXFILE);
        //textFont.getData().setScale(1f, 1f);

        displayingText = false;

        //Most text in cutscenes shouldn't be longer than 12 lines I guess? Subject to change
        textToDisplay = new String[12];
        //make everything empty string
        for (int i = 0; i < textToDisplay.length; i++){
            textToDisplay[i] = "";
        }
        tpCodeOn = false;
        menuCodeOn = false;

        menuBox = new MenuBox(4,8);

        textLine = 0;

        //important to set this to -1 for menu window later!
        menuOpSelected = -1;

        battleTriggered = false;

        currentTalker = "";

        flagQueue = new ArrayDeque<Integer>();

    }

    public void passItemManager(ItemManager i){
        itemManager = i;
    }


    /**
     *
     * @param text
     *
     * Precondition: 'text' does not have more than can fit in array textToDisplay, and no word is more than 33 chars
     */
    public void loadText(String text){
        //TODO: add more control codes! :D


//          CONTROL CODES
//          All start with a [ bracket
//
//          1. [tp xxx : where xxx is text pointer to go to
//          2. [mn StringForOption1 StringForOption2 xxx yyy : where xxx and yyy are text pointers for option 1 and 2 resp
//          3. [ba xxx yyy zzz www : where numbers are enemies to battle
//          4. [it String1 xxx yyy fulldirect zzz : where String1, xxx (magnitude), yyy (type) are item info, fulldirect is
//              for if items are full. zzz is event flag to toggle, if needed
//          5. [evset xxx : toggle event flag xxx
//          6. [evtp xxx yyy : if xxx is true, go to yyy
//          7. [shop xxx : opens shop of type xxx
//          8. [cuts string1 : opens up cutscene (string1 is cutscene file)
//          9. [px : display name of player x

        //by default, speaker is none
        currentTalker = "";

        //split text into words for ease
        String[] words = text.split(" ");

        //iterate thru words, add to nearest available line
        int displayLine = 0;
        int charsLeft = MAXLINELEN;

        //empty out text lines to prevent previous text overflow
        for (int i = 0; i < textToDisplay.length; i++){
            textToDisplay[i] = "";
        }



        for (int i = 0; i < words.length; i++){

            //add talker, if applicable
            //talker syntax should be like this
            // <Putin> DAA you want to mess with meee
            if (words[i].charAt(0) == '<'){
                currentTalker = words[i].substring(1, words[i].length() - 1);
                continue;
            }


            //check for control codes
            if (words[i].equals("[tp")){ //"go to xxx directory" code
                // this means next word is going to be the next directory
                nextTp1 = Integer.parseInt(words[i+1]);
                tpCodeOn = true;
                // we are done. Break. yes I know this looks stupid but it's like a perfect time for break
                break;
            }else if (words[i].equals("[mn")){ //"create menu box, then redirect based on response" code
                // next word will be first option, then after that second option
                String[] options = {words[i+1], words[i+2]};
                // next 2 words after that are directories for options 1 and 2, respectively
                nextTp1 = Integer.parseInt(words[i+3]);
                nextTp2 = Integer.parseInt(words[i+4]);
                menuCodeOn = true;
                //add things to menu box. also make it active
                menuBox.loadNewOptions(options);

                // we are done. Break. yes I know this looks stupid but it's like a perfect time for break
                break;
            }else if (words[i].equals("[ba")) { //"initiate battle" code
                //we know (by precondition) that these are the last words
                //so, by seeing how many words are left, we can see how many enemies are meant to be in fight
                int numEnemies = words.length - (i+1);
                int[] enemies = new int[numEnemies];
                for(int j = 0; j < numEnemies; j++){
                    enemies[j] = Integer.parseInt(words[i+1+j]);
                }
                TestGame.upcomingEnemies = enemies;

                battleTriggered = true;
                break;

            }else if (words[i].equals("[it")) { //"give item" code
                if (itemManager.isFull()){
                    //do not add item, just direct to item full code
                    nextTp1 = Integer.parseInt(words[i+2]);
                    tpCodeOn = true;

                    break;
                }else{

                    int addingItem = Integer.parseInt(words[i+1]);
                    currentItem = itemManager.getItemName(addingItem);

                    if (words.length > i+3){ //words has event flag arg to toggle
                        int flag = Integer.parseInt(words[i+3]);
                        TestGame.eventFlags[flag] = !TestGame.eventFlags[flag];
                        EventFlagManager.eventFlagChanged = true;
                    }

                    itemManager.addItem(addingItem);

                    itemTrigger = true;

                    break;

                }
            } else if (words[i].equals("[evset")){ //toggle event flag
                //sets up flags to be toggled, but doesn't actually toggle until text is done
                int flag = Integer.parseInt(words[i+1]);
                flagQueue.add(flag);
                i = i + 1;
                continue;
            } else if (words[i].equals("[evtp")){//tp if event flag true, else continue
                int flag = Integer.parseInt(words[i+1]);
                if (TestGame.eventFlags[flag]){ //tp
                    nextTp1 = Integer.parseInt(words[i+2]);
                    tpCodeOn = true;
                    break;
                }else{ //dont
                    i = i+2;//i+2 because continue takes us to change before next iteration
                    continue;
                }

            }else if (words[i].equals("[shop")){//shopping time!
                //next num is shop number
                TestGame.shopType = Integer.parseInt(words[i+1]);

                shopTriggered = true;

                break;


            }else if (words[i].equals("[cuts")) { //cutscene!
                //next num is cutscene num
                nextCutscene = words[i+1];

                break;
            }else if (words[i].length() > 2 && words[i].substring(0,2).equals("[p")){
                int playerNum = Integer.parseInt(words[i].substring(2));
                words[i] = PlayerManager.playerNames[playerNum];
            }





            //check if there is space to add space and word
            if (charsLeft - (1 + words[i].length()) < 0){
                //go to next line, reset chars left per line
                displayLine ++;
                charsLeft = MAXLINELEN;

            }
            //if it not is the first letter in the line, add a space
            if (charsLeft < MAXLINELEN){

                textToDisplay[displayLine] += " ";
                charsLeft -= 1;
            }else{
                //set this line to an empty string so it can be added to
                textToDisplay[displayLine] = "";
            }
            textToDisplay[displayLine] += words[i];
            charsLeft -= words[i].length();

        }



        textLine = 0;
    }



    public void update(boolean buttonPressed, int downMinusUp, TextEventHandler tex){
        //note: don't load text multiple times
        if(menuBox.isActive()){ //if this is true, then no scrolling. only select
            menuBox.alterSelect(downMinusUp);
            if (buttonPressed){
                menuOpSelected = menuBox.selectOptionInt();
                menuBox.setActive(false);
            }
        }
        else if (tex.isActive() && !displayingText){
            //set up text display
            loadText(tex.getMessage());
            textLine = 0;
            displayingText = true;
        }
        else if (!buttonPressed || !tex.isActive()){
            //do nothing
            //other conditions only happen if both the above conditions are true
        }
        else{
            //this condition: in midst of displaying text, button pressed

            // scroll to next
            textLine ++;

            //if we are done, then stop displaying OR control code
            if (textLine + 2 >= textToDisplay.length || textToDisplay[textLine + 2].equals("")){
                //toggle event flags, if applicable
                while (!flagQueue.isEmpty()){
                    int flag = flagQueue.pollFirst();
                    TestGame.eventFlags[flag] = !TestGame.eventFlags[flag];
                    EventFlagManager.eventFlagChanged = true;
                }


                if (tpCodeOn){
                    tex.loadTextNum(nextTp1);
                    textLine = 0;
                    tpCodeOn = false;
                    loadText(tex.getMessage());

                }
                else if (itemTrigger){

                    textLine = 0;
                    currentTalker = "";
                    loadText("(Obtained " + currentItem + ").");
                    itemTrigger = false;
                }
                //two situations for menu code on
                //1. menu box had not been loaded yet
                //2. menu box was just done and selected
                //I use the menuOpSelected variable to help with this. It is -1 if window is unloaded
                else if (menuCodeOn && menuOpSelected == -1){
                    menuBox.setActive(true);
                }
                else if (menuCodeOn){
                    if (menuOpSelected == 0){
                        tex.loadTextNum(nextTp1);
                    }else{
                        tex.loadTextNum(nextTp2);
                    }
                    menuOpSelected = -1;
                    textLine = 0;
                    loadText(tex.getMessage());
                    menuCodeOn = false;
                }
                else{
                    displayingText = false;
                    tex.deactivate();
                    if (battleTriggered){
                        battleTriggered = false;
                        TestGame.upcomingMode = "battle";
                    }else if (shopTriggered){
                        shopTriggered = false;
                        TestGame.upcomingMode = "shop";
                    }else if (nextCutscene != null){
                        WorldMode.upcomingCutscene = "text/cutscenes/" + nextCutscene;
                        nextCutscene = null;
                    }
                }

            }



        }

    }



    public void draw(SpriteBatch batch){


        // if supposed to display, display. Else don't
        if (displayingText) {

            batch.draw(backgroundBox, BOX_X, BOX_Y);

            textFont.draw(batch, currentTalker, BOX_X + 64, BOX_Y + 256 - 32);

            textFont.draw(batch, textToDisplay[textLine], BOX_X + 64, BOX_Y + 256 - 64 - 16 - 16);
            textFont.draw(batch, textToDisplay[textLine+1], BOX_X + 64, BOX_Y + 256 - 64 - 16 - 48);

            //in case we are out of bounds
            if (textLine + 2 < textToDisplay.length) {
                textFont.draw(batch, textToDisplay[textLine + 2], BOX_X + 64, BOX_Y + 256 - 64 - 16 - 64 - 16);
            }

            menuBox.draw(batch);
        }

    }



    public void dispose(){
        backgroundBox.dispose();
        textFont.dispose();
    }


    public boolean isDisplaying(){ return displayingText; }

    public void loadTexNum(int num){

    }




}
