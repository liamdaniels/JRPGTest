package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class MenuMode implements ModeController{

    public static int money = 0;

    private final String MENUTOP = "bigmenutop.png";
    private final String MENUMID = "bigmenumid.png";
    private final String MENUBOT = "bigmenubot.png";
    private final String CURSOR = "cursor.png";
    private final String TEXTBOXFILE = "textdisplaybg.png";


    private Texture cursorTex;

    private final String FONTFILE = "AbrilFatface-Regular.otf";
    private final int TILEDIM = 64;
    private final String BIG = "bigwindow.png";
    private final String[] OPTIONS = {"Gang", "Job", "Magic", "Things"};

    //display
    private Texture menuBg;
    private Texture itemDescripBg;
    private BitmapFont text;

    //managers for game
    private PlayerManager playerManager;
    private ItemManager itemManager;
    private SpellManager spellManager;

    //thing that selects which menu thing to be on (players, items, etc)
    private int currentOption;
    private MenuBox windowSelect;

    //displays alerts such as "john used item" or "can't do xyz"
    private BabyTextBox alerter;

    public static ArrayDeque<String> alertQueue = new ArrayDeque<String>();

    //Whether player is "in window" or not. Like whether they are selecting
    // which window option or if they are selecting within the window
    private boolean inWindow;

    //cursor ints
    private int playerCursor;
    private int itemCursor;
    private int magicCursor;
    private int jobCursor;

    private BattlePlayer selectedMagicPlayer;

    private BattlePlayer selectedJobPlayer;

    //specific for player status mode
    private int playerStatusMode;

    //used for item selection, equip selection, all the needed selections
    private MenuBox selector;



    //TODO: all the other modes, switching between modes, etc.

    public MenuMode(PlayerManager playerManager, ItemManager itemManager, SpellManager spellManager){
        currentOption = 0;

        this.playerManager = playerManager;
        this.itemManager = itemManager;
        this.spellManager = spellManager;

        menuBg = new Texture(BIG);

        //Took this code from online, presumably makes a new BitmapFont with the desired font file
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(FONTFILE));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 35;
        text = generator.generateFont(parameter);
        generator.dispose();


        windowSelect = new MenuBox(OPTIONS, 14, 6);
        windowSelect.setActive(true);

        inWindow = false;

        playerCursor = 0;
        itemCursor = 0;

        alerter = new BabyTextBox();

        selector = new MenuBox(6, 9);

        cursorTex = new Texture(CURSOR);

        playerStatusMode = -1;

        itemDescripBg = new Texture(TEXTBOXFILE);
    }



    @Override
    public void update(InputController inputs) {



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

        if (!alertQueue.isEmpty() && !alerter.isActive()){ //if alerts must de displayed

            alerter.displayNewMessage(alertQueue.pollFirst(), 6, 9, 60);

        }
        else if (alerter.isActive()){
            alerter.update();
        }
        else {
            //switch back to world if letter B clicked
            if (inputLetterB == 1) {
                inWindow = false;
                //switch back to world mode
                TestGame.upcomingMode = "world";
            }
            if (!inWindow) {
                //change option based on input
                int downMinusUp = inputD - inputU;
                if (currentOption + downMinusUp >= 0 && currentOption + downMinusUp < OPTIONS.length){
                    currentOption += downMinusUp;
                    windowSelect.alterSelect(downMinusUp);
                }

                if (inputLetterD == 1){ //if "confirm" button clicked on window
                    inWindow = true;
                }

            }else { //player is "in window"!
                //case by case depending on which option we are on
                //"gang" option leads to nada for now
                if (OPTIONS[currentOption].equals("Gang")){
                    //can get out with S button
                    gangUpdate(inputU, inputD, inputLetterD, inputLetterS);
                }else if (OPTIONS[currentOption].equals("Job")){
                    jobUpdate(inputU, inputD, inputR, inputL, inputLetterD, inputLetterS);
                }
                else if (OPTIONS[currentOption].equals("Magic")) {
                    magicUpdate(inputU, inputD, inputR, inputL, inputLetterD, inputLetterS);
                }
                else if (OPTIONS[currentOption].equals("Things")) {
                   itemUpdate(inputU, inputD, inputR, inputL, inputLetterD, inputLetterS);
                }
            }
        }

    }



    @Override
    public void draw(SpriteBatch batch) {

        batch.draw(menuBg, 0, 0);

        if (currentOption == 0){ //looking at player
            ArrayList<BattlePlayer> party = playerManager.getParty();

            if (playerStatusMode == -1) {

                for (int i = 0; i < party.size(); i++) {

                    //draw name, picture
                    text.draw(batch, party.get(i).getName(), (1) * TILEDIM, (12 - (i * 3) + 0.5f) * TILEDIM);
                    party.get(i).draw(batch, 1, (12 - (i * 3) - 2));

                    //draw lvl, HP, MP
                    text.draw(batch, "Level   " + party.get(i).getLevel(), (5) * TILEDIM,
                            (12 - (i * 3) + 0.5f) * TILEDIM);
                    text.draw(batch, JobManager.jobs[party.get(i).getJob()].name, (5) * TILEDIM,
                            (12 - (i * 3)) * TILEDIM);
                    text.draw(batch, "HP: " + party.get(i).getHP() + "/" + party.get(i).getMaxHP(), (5) * TILEDIM,
                            (12 - (i * 3) - 0.5f) * TILEDIM);
                    text.draw(batch, "MP: " + party.get(i).getMP() + "/" + party.get(i).getMaxMP(),
                            (5) * TILEDIM, (12 - (i * 3) - 1f) * TILEDIM);


                    //draw equips


                    text.draw(batch, itemManager.getItemName(party.get(i).getEquip(0)), (10) * TILEDIM,
                            (12 - (i * 3)) * TILEDIM);
                    text.draw(batch, itemManager.getItemName(party.get(i).getEquip(1)), (10) * TILEDIM,
                            (12 - (i * 3) - 0.5f) * TILEDIM);
                    text.draw(batch, itemManager.getItemName(party.get(i).getEquip(2)),
                            (10) * TILEDIM, (12 - (i * 3) - 1f) * TILEDIM);



                }
                //draw money
                text.draw(batch, "Money:\n$" + money, 13.5f*TILEDIM, 12*TILEDIM);

                //draw cursor
                int cursorX = 0;
                int cursorY = 12 - (playerCursor * 3);
                batch.draw(cursorTex, (cursorX)*TILEDIM,(cursorY-0.75f)*TILEDIM);
            }else{
                BattlePlayer currentP = playerManager.getParty().get(playerStatusMode);

                text.draw(batch, currentP.getName(), (1) * TILEDIM, (12.5f) * TILEDIM);
                currentP.draw(batch, 1, (10));

                //draw lvl, HP, MP
                text.draw(batch, "Level   " + currentP.getLevel(), (5) * TILEDIM, (12.5f) * TILEDIM);
                text.draw(batch, "HP: " + currentP.getHP() + "/" + currentP.getMaxHP(), (5) * TILEDIM, (11.5f) * TILEDIM);
                text.draw(batch, "MP: " + currentP.getMP() + "/" + currentP.getMaxMP(), (5) * TILEDIM, (11) * TILEDIM);

                //draw stats

                String row1a = "Attack: " + currentP.getAttack();
                String row1b = "Defense: " + currentP.getDefense();
                String row2a = "Magic Attack: " + currentP.getMagicAttack();
                String row2b = "Magic Defense: " + currentP.getMagicDefense();
                String row3a = "Speed: " + currentP.getSpeed();
                String row3b = " ";//empty row

                String row4a = "Strength: " + currentP.getStrength();
                String row4b = "Chunk: " + currentP.getChunk();
                String row5a = "Sharpness: " + currentP.getSharpness();
                String row5b = "Hardness: " + currentP.getHardness();
                String row6a = "Vitality: " + currentP.getVitality();
                String row6b = "Big Brain: " + currentP.getBigbrain();

                String row7 = "Exp needed for next level: " + currentP.getExpForNextLevel();

                //first row
                text.draw(batch, row1a, (1) * TILEDIM, (9) * TILEDIM);
                text.draw(batch, row1b, (9) * TILEDIM, (9) * TILEDIM);

                //second row
                text.draw(batch, row2a, (1) * TILEDIM, (8) * TILEDIM);
                text.draw(batch, row2b, (9) * TILEDIM, (8) * TILEDIM);

                //third row
                text.draw(batch, row3a, (1) * TILEDIM, (7) * TILEDIM);
                text.draw(batch, row3b, (9) * TILEDIM, (7) * TILEDIM);

                //fourth row
                text.draw(batch, row4a, (1) * TILEDIM, (5) * TILEDIM);
                text.draw(batch, row4b, (9) * TILEDIM, (5) * TILEDIM);

                //fifth row
                text.draw(batch, row5a, (1) * TILEDIM, (4) * TILEDIM);
                text.draw(batch, row5b, (9) * TILEDIM, (4) * TILEDIM);

                //sixth row
                text.draw(batch, row6a, (1) * TILEDIM, (3) * TILEDIM);
                text.draw(batch, row6b, (9) * TILEDIM, (3) * TILEDIM);

                //seventh row: display exp stuff
                text.draw(batch, row7, (1) * TILEDIM, (1) * TILEDIM);

            }
        }else if (currentOption == 1){//job
            ArrayList<BattlePlayer> party = playerManager.getParty();

            if (playerStatusMode == -1) {

                for (int i = 0; i < party.size(); i++) {

                    //draw name, picture
                    text.draw(batch, party.get(i).getName(), (1) * TILEDIM, (12 - (i * 3) + 0.5f) * TILEDIM);
                    party.get(i).draw(batch, 1, (12 - (i * 3) - 2));

                    //draw lvl, HP, MP
                    text.draw(batch, "Level   " + party.get(i).getLevel(), (5) * TILEDIM,
                            (12 - (i * 3) + 0.5f) * TILEDIM);
                    text.draw(batch, JobManager.jobs[party.get(i).getJob()].name, (5) * TILEDIM,
                            (12 - (i * 3)) * TILEDIM);
                    text.draw(batch, "HP: " + party.get(i).getHP() + "/" + party.get(i).getMaxHP(), (5) * TILEDIM,
                            (12 - (i * 3) - 0.5f) * TILEDIM);
                    text.draw(batch, "MP: " + party.get(i).getMP() + "/" + party.get(i).getMaxMP(),
                            (5) * TILEDIM, (12 - (i * 3) - 1f) * TILEDIM);


                    //draw equips


                    text.draw(batch, itemManager.getItemName(party.get(i).getEquip(0)), (10) * TILEDIM,
                            (12 - (i * 3)) * TILEDIM);
                    text.draw(batch, itemManager.getItemName(party.get(i).getEquip(1)), (10) * TILEDIM,
                            (12 - (i * 3) - 0.5f) * TILEDIM);
                    text.draw(batch, itemManager.getItemName(party.get(i).getEquip(2)),
                            (10) * TILEDIM, (12 - (i * 3) - 1f) * TILEDIM);



                }
                //draw money
                text.draw(batch, "Money:\n$" + money, 13.5f*TILEDIM, 12*TILEDIM);

                //draw cursor
                int cursorX = 0;
                int cursorY = 12 - (playerCursor * 3);
                batch.draw(cursorTex, (cursorX)*TILEDIM,(cursorY-0.75f)*TILEDIM);
            }else{
                //draw player name
                text.draw(batch, selectedJobPlayer.getName(), (4) * TILEDIM, (12.75f) * TILEDIM);


                for(int i = 0; i < JobManager.jobs.length; i++){
                    //3 spells per row, 10 rows max
                    int xPlacement = (i % 3)*5;
                    int yPlacement = 12 - (i / 3);

                    text.draw(batch, JobManager.jobs[i].name, (xPlacement+0.5f)*TILEDIM,yPlacement*TILEDIM);

                }

                //draw cursor
                int cursorX = (jobCursor % 3)*5;
                int cursorY = 12 - (jobCursor / 3);
                batch.draw(cursorTex, (cursorX-0.5f)*TILEDIM,(cursorY-0.75f)*TILEDIM);






            }
        }
        else if (currentOption == 2){ //looking at magic
            if (playerStatusMode == -1) { //draw player status windows, selecting one
                ArrayList<BattlePlayer> party = playerManager.getParty();


                for (int i = 0; i < party.size(); i++) {

                    //draw name, picture
                    text.draw(batch, party.get(i).getName(), (1) * TILEDIM, (12 - (i * 3) + 0.5f) * TILEDIM);
                    party.get(i).draw(batch, 1, (12 - (i * 3) - 2));


                    //draw lvl, HP, MP
                    text.draw(batch, "Level   " + party.get(i).getLevel(), (5) * TILEDIM, (12 - (i * 3) + 0.5f) * TILEDIM);
                    text.draw(batch, JobManager.jobs[party.get(i).getJob()].name, (5) * TILEDIM,
                            (12 - (i * 3)) * TILEDIM);
                    text.draw(batch, "HP: " + party.get(i).getHP() + "/" + party.get(i).getMaxHP(), (5) * TILEDIM, (12 - (i * 3) - 0.5f) * TILEDIM);
                    text.draw(batch, "MP: " + party.get(i).getMP() + "/" + party.get(i).getMaxMP(),
                            (5) * TILEDIM, (12 - (i * 3) - 1f) * TILEDIM);

                }
                //draw money
                text.draw(batch, "Money:\n$" + money, 13.5f*TILEDIM, 12*TILEDIM);

                //draw cursor
                int cursorX = 0;
                int cursorY = 12 - (playerCursor * 3);
                batch.draw(cursorTex, (cursorX)*TILEDIM,(cursorY-0.75f)*TILEDIM);
            }
            else {
                //draw player name, MP
                text.draw(batch, selectedMagicPlayer.getName(), (4) * TILEDIM, (12.75f) * TILEDIM);
                text.draw(batch, "MP: " + selectedMagicPlayer.getMP() + "/" + selectedMagicPlayer.getMaxMP(),
                        (10) * TILEDIM, (12.75f) * TILEDIM);


                //draw player's spells
                String[] spells = spellManager.getNamesAndCosts(selectedMagicPlayer.getSpells(),
                        selectedMagicPlayer.getNumSpells());
                for(int i = 0; i < spells.length; i++){
                    //3 spells per row, 10 rows max
                    int xPlacement = (i % 3)*5;
                    int yPlacement = 12 - (i / 3);

                    text.draw(batch, spells[i], (xPlacement+0.5f)*TILEDIM,yPlacement*TILEDIM);

                }

                //draw cursor
                int cursorX = (magicCursor % 3)*5;
                int cursorY = 12 - (magicCursor / 3);
                batch.draw(cursorTex, (cursorX-0.5f)*TILEDIM,(cursorY-0.75f)*TILEDIM);










            }


        }
        else if (currentOption == 3){ //looking at items

            String[] names = itemManager.getExistingItemNames();
            int numItems = itemManager.getNumItems();
            for(int i = 0; i < numItems; i++){
                //3 items per row, 10 rows ?

                int xPlacement = (i % 3)*5;
                int yPlacement = 12 - (i / 3);

                text.draw(batch, names[i], (xPlacement+0.5f)*TILEDIM,yPlacement*TILEDIM);

            }
            //draw cursor
            int cursorX = (itemCursor % 3)*5;
            int cursorY = 12 - (itemCursor / 3);
            batch.draw(cursorTex, (cursorX-0.5f)*TILEDIM,(cursorY-0.75f)*TILEDIM);

            batch.draw(itemDescripBg, (0.5f)*TILEDIM, TILEDIM*(-2));
            text.draw(batch, itemManager.getDescriptionByInventory(itemCursor), TILEDIM,TILEDIM);
        }

        if (!inWindow) {
            windowSelect.draw(batch);
        }


        selector.draw(batch);
        alerter.draw(batch);



    }

    @Override
    public void dispose() {

        menuBg.dispose();
        text.dispose();
        windowSelect.dispose();
        alerter.dispose();
        selector.dispose();

    }

    private void gangUpdate(int inputU, int inputD, int inputLetterD, int inputLetterS){
        if (playerStatusMode == -1){ //currently looking at players
            int mvmt = inputD - inputU;
            if (playerCursor + mvmt > -1 && playerCursor + mvmt < playerManager.getParty().size()){
                if (playerCursor + mvmt != playerCursor){
                    SoundTextureManager.playSelect();
                }
                playerCursor += mvmt;
            }

            if(inputLetterD == 1){
                playerStatusMode = playerCursor;
                SoundTextureManager.playSelect();
            }else if (inputLetterS == 1){
                inWindow = false;
            }

        }else{
            if (inputLetterS == 1){
                playerStatusMode = -1;
            }
        }
    }

    private void itemUpdate(int inputU, int inputD, int inputR, int inputL, int inputLetterD, int inputLetterS){
        if (selector.isActive()){ //in a selector menu box
            //could either be in use/throw window or in player select window
            //either way, selector must
            selector.alterSelect(inputD - inputU);
            if (inputLetterD == 1) { //confirm  button clicked!
                //another branch. depends on if player selecting window or item selecting window
                if (selector.getName().equals("usewindow")){
                    switch(selector.selectOption()){
                        case "Use": //either must select player or not!
                            if (itemManager.usedOnPlayer(itemCursor)){
                                //go to player window next
                                setPlayerSelector();
                                break;
                            }else{
                                itemManager.useItem(itemCursor, null);
                                if (itemCursor >= itemManager.getNumItems() && itemCursor > 0){
                                    itemCursor--;
                                }
                                selector.setActive(false);
                            }

                            break;
                        case "Toss":
                            itemManager.remove(itemCursor);
                            alertQueue.add("Tossed.");
                            if (itemCursor >= itemManager.getNumItems() && itemCursor > 0){
                                itemCursor--;
                            }
                            selector.setActive(false);
                            break;
                        default:
                            break;
                    }
                }else { //selecting player!
                    //since button pressed, get player
                    BattlePlayer target = playerManager.searchByName(selector.selectOption());
                    itemManager.useItem(itemCursor, target);
                    if (itemCursor >= itemManager.getNumItems() && itemCursor > 0){
                        itemCursor--;
                    }
                    selector.setActive(false);
                }
            }
            else if (inputLetterS == 1){
                //go back to item menu
                selector.setActive(false);
            }
        }
        else{
            //alter item cursor depending on input
            //don't let it go past 30 or below 0
            //note: ^^^ should change if max item size changes from 30 to other!
            //Pressing right increases item cursor by 1, left minus 1,
            // and then up and down move by 3

            int movement = inputR - inputL + (3 * inputD) - (3 * inputU);
            if (itemCursor + movement > -1 && itemCursor + movement < itemManager.getNumItems()) {
                if (itemCursor + movement != itemCursor){ SoundTextureManager.playSelect(); }
                itemCursor += movement;
            }
            if (inputLetterD == 1 && itemManager.getNumItems() > 0){ //confirm  button
                //load item options into selector, set selector active
                String[] itemOps = {"Use", "Toss"};
                selector.loadNewOptions(itemOps);
                selector.setActive(true);
                selector.setName("usewindow");
            }
            //can get out of window with S button
            if (inputLetterS == 1){
                inWindow = false;
            }
        }



    }

    public void magicUpdate(int inputU, int inputD, int inputR, int inputL, int inputLetterD, int inputLetterS){
        //allows user to select player, then look at their magic
        if (playerStatusMode == -1){ //currently looking at players
            int mvmt = inputD - inputU;
            if (playerCursor + mvmt > -1 && playerCursor + mvmt < playerManager.getParty().size()){
                if (playerCursor + mvmt != playerCursor){ SoundTextureManager.playSelect(); }
                playerCursor += mvmt;
            }

            if(inputLetterD == 1){
                playerStatusMode = playerCursor;
                selectedMagicPlayer = playerManager.getParty().get(playerCursor);
            }else if (inputLetterS == 1){
                inWindow = false;
            }
        }else{

            if (selector.isActive()) {
                //in player select window
                selector.alterSelect(inputD - inputU);

                if (inputLetterD == 1) { //confirm pressed
                    //since button pressed, get player
                    BattlePlayer target = playerManager.searchByName(selector.selectOption());
                    spellManager.castSpellOutside(magicCursor, selectedMagicPlayer, target);
                    selector.setActive(false);
                }
                else if (inputLetterS == 1){//go back
                    selector.setActive(false);
                }
            }
            else{
                //alter magic cursor depending on input
                //don't let it go past max or below 0
                //Pressing right increases cursor by 1, left minus 1,
                // and then up and down move by 3
                int movement = inputR - inputL + (3 * inputD) - (3 * inputU);
                if (magicCursor + movement > -1 && magicCursor + movement < selectedMagicPlayer.getNumSpells()) {
                    if (magicCursor + movement != magicCursor){ SoundTextureManager.playSelect(); }
                    magicCursor += movement;
                }

                if (inputLetterD == 1 && selectedMagicPlayer.getNumSpells() > 0){ //confirm  button
                    //load options into selector, set selector active
                    // BUT only if spell can be used outside of battle
                    int spellIndex = selectedMagicPlayer.getSpells()[magicCursor];
                    if (!spellManager.usedOnPlayer(spellIndex)){
                        //cannot use. tell user
                        alertQueue.add("Not usable outside battle!");
                    }else{ //we good. Go to player select
                        setPlayerSelector();
                    }
                }

                //can get out of window with S button
                if (inputLetterS == 1) {
                    playerStatusMode = -1;
                }
            }
        }

    }

    public void jobUpdate(int inputU, int inputD, int inputR, int inputL, int inputLetterD, int inputLetterS){
        if (playerStatusMode == -1){ //currently looking at players
            int mvmt = inputD - inputU;
            if (playerCursor + mvmt > -1 && playerCursor + mvmt < playerManager.getParty().size()){
                if (playerCursor + mvmt != playerCursor){ SoundTextureManager.playSelect(); }
                playerCursor += mvmt;
            }

            if(inputLetterD == 1) {
                if (playerManager.getParty().get(playerCursor).canSwitchJob()) {
                    playerStatusMode = playerCursor;
                    selectedJobPlayer = playerManager.getParty().get(playerCursor);
                    jobCursor = 0;
                }else{
                    alertQueue.add("Can't change job!");
                }
            }else if (inputLetterS == 1){
                inWindow = false;
            }
        }else{

            int movement = inputR - inputL + (3 * inputD) - (3 * inputU);
            if (jobCursor + movement > -1 && jobCursor + movement < JobManager.jobs.length) {
                if (jobCursor + movement != jobCursor){ SoundTextureManager.playSelect(); }
                jobCursor += movement;
            }

            if (inputLetterD == 1){ //confirm  button
                selectedJobPlayer.setJob(jobCursor);
                alertQueue.add("Set job to " + JobManager.jobs[jobCursor].name);
            }

            //can get out of window with S button
            if (inputLetterS == 1) { playerStatusMode = -1; }

        }
    }


    private void setPlayerSelector(){
        String[] names = playerManager.getPartyNames();
        selector.loadNewOptions(names);
        selector.setActive(true);
        selector.setName("playerselect");
    }

}
