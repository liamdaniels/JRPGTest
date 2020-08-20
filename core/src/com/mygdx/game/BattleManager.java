package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


import java.util.ArrayDeque;
import java.util.ArrayList;


public class BattleManager {


    /** Helper BattleAction class just to simplify actions
     * Two parts: target (int) and action (string)
     */
    private class BattleAction{
        public String action;
        public int target;
        public int itemIndex;

        //TODO: USE!
        public int magicIndex;

        public BattleAction(){
            target = 0;
            action = "";
        }
    }



    private Texture backgroundPic;

    //no more than... 4?
    private int numPlayers;

    private int numEnemies;

    private BattlePlayer[] players;
    private Enemy[] enemies;

    private ItemManager itemManager;
    private SpellManager spellManager;

    private BattleAction[] playerActions;
    private BattleAction[] enemyActions;

    //for items, magic, anything with a lot of options
    private MenuBox miscScroll;


    private int battleSong;


    /**
     * -1 if multi targeting players
     * -2 if multi targeting enemies
     */
    private int multiTargeted;


    //used to determine who is currently acting on turn phase 0. Used in update battle
    private int actor;


    //used in update battle as well. To determine who is currently acting
    private Battleable turnActor;

    //used in update battle. Determine what the current act is
    private String currentAct;

    //used in update battle as well. To determine who is currently defending
    private Battleable reactor;

    //battle has 4 "turn phases" when a character attacks
    //0 is actor messages. 1 is actor acts. 2 is reactor message. 3 is reactor react
    private int turnPhase;



    private boolean battleWon;
    private int afterBattleFrames;



    /**
     * IDEA FOR MESSAGES
     * store each player's message box
     * have 1 message box for spells or smth, items or smth
     * 1 for selecting enemies
     */

    private MenuBox[] playerMenus;
    private MenuBox enemyBox;
    //TODO items and magic and stuff

    //current active menu box player is selecting
    private MenuBox currentMenuBox;

    private BabyTextBox messager;



    //display with each player's hp and mp stuff
    private BabyTextBox[] playerStatDisplays;


    //coords where players go
    private final int PLAYERSPOT_X = 4;
    private final int P1_SPOT_Y = 10;
    private final int PLAYER_Y_DIST = 3;

    private final int QUEUESIZE = 10;
    public static ArrayDeque<String> messageQueue;

    private final String WINMESSAGE = "you win :)";

    //all ppl in battle
    private ArrayList<Battleable> participants;

    private final int ENEMYSPOT_X = 11;
    private final int ENEMY_TOPSPOT_Y = 9;
    private final int ENEMY_Y_DIST = 4;
    //TODO: more spots

    private boolean battleActive;


    private int currentlySelecting;

    private int currentItemIndex;

    private String[] enemyNames;

    private ArrayList<BattlePlayer> livingPlayers;
    private ArrayList<Enemy> livingEnemies;


    private int totalBattleExp;
    private int totalMoney;



    public BattleManager(){
        backgroundPic = new Texture("battlepics/testbg.png");
        //numPlayers = 3;
        players = new BattlePlayer[4];
        playerActions = new BattleAction[4];




        enemyActions = new BattleAction[4];
        enemies = new Enemy[4];

        playerMenus = new MenuBox[4];


        currentMenuBox = null;

        messager = new BabyTextBox();

        messageQueue = new ArrayDeque<String>();

        participants = new ArrayList<Battleable>();

        String statDisplay = "";
        playerStatDisplays = new BabyTextBox[4];
        for (int i = 0; i < playerStatDisplays.length; i++){
            //make new stat display
            playerStatDisplays[i] = new BabyTextBox();
        }
        participants = new ArrayList<Battleable>();
        livingPlayers = new ArrayList<BattlePlayer>();
        livingEnemies = new ArrayList<Enemy>();
    }






    public void loadNewBattle(PlayerManager playerManager, Enemy[] foes, ItemManager itemManager, SpellManager spellManager){
        this.itemManager = itemManager;
        this.spellManager = spellManager;

        participants.clear();
        livingEnemies.clear();
        livingPlayers.clear();

        ArrayList<BattlePlayer> party = playerManager.getParty();
        numPlayers = party.size();

        for (int i = 0; i < numPlayers; i++) {
            players[i] = party.get(i);
            players[i].setAnimation(0,-1);
            playerMenus[i] = new MenuBox(party.get(i).getOps(), PLAYERSPOT_X + 1, P1_SPOT_Y + 2 - (PLAYER_Y_DIST * i));
            playerMenus[i].setName("PLAYER");
            playerActions[i] = new BattleAction();
            players[i].setBattleDrawPos(i);
            livingPlayers.add(players[i]);
        }

        for (int i = 0; i < playerStatDisplays.length; i++){
            //make new stat display
            playerStatDisplays[i] = new BabyTextBox();
            if (i < numPlayers){
                String statDisplay = players[i].getName() + "\nHP: " + players[i].getHP() + "\nMP: " + players[i].getMP();
                playerStatDisplays[i].foreverDisplay(statDisplay, PLAYERSPOT_X-4, P1_SPOT_Y + 2 - (PLAYER_Y_DIST*i), 2, 0.8f,0.6f);
            }
        }


        numEnemies = foes.length;
        for (int i = 0; i < numEnemies; i++){
            enemies[i] = foes[i];
            enemies[i].setName(enemies[i].getName());
            enemyActions[i] = new BattleAction();
            enemies[i].setBattleDrawPos(i+4);
            livingEnemies.add(enemies[i]);
        }

        totalBattleExp = 0;
        totalMoney = 0;
        enemyNames = new String[numEnemies];
        for (int i = 0; i < numEnemies; i++){
            enemyNames[i] = enemies[i].getName();
            totalBattleExp += enemies[i].getExpYield();
            totalMoney += enemies[i].getMoneyYield();
        }
        enemyBox = new MenuBox(enemyNames,6,5);
        enemyBox.setName("ENEMYBOX");


        miscScroll = new MenuBox(6,5, 6);


        currentMenuBox = null;

        battleSong = enemies[0].getSong();

        initializeBattle();


    }


    public void update(int downMinusUp, boolean buttonPressed, boolean backButtonPressed){
        //only do anything if there is a battle going on, of course
        if (battleActive) {
            SoundTextureManager.playNewBgSong(battleSong);

            //if there are messages to display and one is not already being displayed, display them above all else
            if (!messageQueue.isEmpty() && !messager.isActive()) {
                String a = messageQueue.pollFirst();
                messager.displayNewMessage(a, 6, 10, 60);
            }else if (messageQueue.isEmpty()){
                //don't update battle until messages are done
                battleUpdate(downMinusUp,buttonPressed, backButtonPressed);
            }
            messager.update();
            for (int i = 0; i < numPlayers; i++){
                String statDisplay = players[i].getName() + "\nHP: " + players[i].getHP() + "\nMP: " + players[i].getMP();
                playerStatDisplays[i].changeMessage(statDisplay);
            }

        }
        else{
            if (currentMenuBox != null && currentMenuBox.isActive()){ currentMenuBox.setActive(false);  }

            //num frames to wait after messages are done
            int BATTLE_WAIT = 90;


            if (!messageQueue.isEmpty() && !messager.isActive()){
                String newMessage = messageQueue.pollFirst();
                if (newMessage.equals(WINMESSAGE)) {
                    messager.displayNewMessage(newMessage, 6, 9, 60);
                }else{
                    messager.displayNewMessage(newMessage, 6, 9, 30);
                }


                for(int i = 0; i < livingPlayers.size(); i++) {
                    livingPlayers.get(i).setAnimation(2, -1);
                }

            }
            else if (messageQueue.isEmpty() && afterBattleFrames < BATTLE_WAIT){
                afterBattleFrames++;

            }else if (messageQueue.isEmpty()){//time to end battle mode
                // we are done with battle mode
                if (battleWon) {
                    messager.setActive(false);
                    TestGame.upcomingMode = "world";
                } else {

                    //TODO: lost mode???

                }
            }
            messager.update();

        }

    }


    public void endBattle(boolean playerWins){
        //TODO: this!
        battleActive = false;
        //just in case battle ends before clear
        itemManager.clearFromQueue();
        // clear old messages
        messageQueue.clear();

        if(playerWins){
            battleWon = true;

            messageQueue.add(WINMESSAGE);

            //players gain exp
            int expPerPlayer = (int)totalBattleExp/livingPlayers.size();
            messageQueue.add("Gained " + expPerPlayer + " exp each");
            for (int i = 0; i < livingPlayers.size(); i++){
                //have each living player gain exp
                // addExp returns true if player leveled up!
                if (livingPlayers.get(i).addExp(expPerPlayer)){
                    messageQueue.add(livingPlayers.get(i).getName() + " leveled up!");
                }
                if (livingPlayers.get(i).addJobExp(expPerPlayer)){
                    messageQueue.add(livingPlayers.get(i).getName() + " job leveled up!");
                }
            }
            messageQueue.add("Got " + totalMoney + " money");
            MenuMode.money += totalMoney;

        }else{
            battleWon = false;

            messageQueue.add(":(");
        }
    }

    public void battleUpdate(int downMinusUp, boolean buttonPressed, boolean backButtonPressed) {
        /**
         * IDEA:
         * have inner action class? something to store string and int target
         * have one for each participant (can just make two more arrays)
         * not in speed order since selection happens in player order
         *
         * then
         *
         * just go thru all participants in speed order (ie thru the array list)
         * then do perform action
         *
         *
         * IDEA PART 2:
         *
         * have modes of battle basically
         * so there's "selecting"
         * and then there's "turn going on"
         * ok wait actually I don't think we need bools for this
         * ok ok heres how we tell
         * so if currentMenu is null and messagequeue has things in it, then turn is going on
         * if CM is a window then we are selecting, allow for select
         *
         *
         * use currentlyselecting variable
         *
          */

        if (currentMenuBox == null && messageQueue.isEmpty() && actor >= participants.size()){ //last turn has just ended (or on turn 0). Begin select
            //first player menu is first not dead person
            int j;
            for(j = 0; players[j].isDead(); j++) { }
            currentMenuBox = playerMenus[j];
            currentlySelecting = j;
            currentMenuBox.setActive(true);

            for(int i=0; i < numPlayers; i++){
                playerActions[i].itemIndex = -1;
            }
            itemManager.clearFromQueue();

            //if any enemies/players died on last turn, reload options into the enemy box
            boolean anyDead = false;
            for (int i = 0; i < livingEnemies.size(); i++) {
                if (livingEnemies.get(i).isDead()) {
                    anyDead = true;
                    livingEnemies.remove(livingEnemies.get(i));
                    i--;
                }
            }
            if (anyDead){
                enemyNames = new String[livingEnemies.size()];
                for(int i = 0; i < livingEnemies.size(); i++){
                    enemyNames[i] = livingEnemies.get(i).getName();
                }
                enemyBox.loadNewOptions(enemyNames);
            }

            for (int i = 0; i < livingPlayers.size(); i++) {
                if (livingPlayers.get(i).isDead()) {
                    livingPlayers.remove(livingPlayers.get(i));
                    i--;
                }
            }
        }

        else if(messageQueue.isEmpty() && actor >= participants.size()) { //currently selecting stuff
            //alter select, of course
            currentMenuBox.alterSelect(downMinusUp);

            if (buttonPressed){ //add to action stuff
                //check if selecting attack or monster
                if(currentMenuBox.getName().equals("PLAYER")){
                    String act = currentMenuBox.selectOption();
                    playerActions[currentlySelecting].action = act;

                    if (act.equals("Attack")) {

                        //we just selected attack, so go to monster box
                        //deactivate previous
                        currentMenuBox.setActive(false);
                        currentMenuBox = enemyBox;
                        currentMenuBox.setActive(true);
                    }else if (act.equals("Item")){
                        //load item window, then set it to our next window
                        currentMenuBox.setActive(false);
                        miscScroll.loadNewOptions(itemManager.getExistingItemNames());
                        miscScroll.setName("ITEM");
                        currentMenuBox = miscScroll;
                        currentMenuBox.setActive(true);
                    }else if (act.equals("Magic")){
                        //load magic window, then set it to our next window
                        currentMenuBox.setActive(false);
                        miscScroll.loadNewOptions(
                                spellManager.getNamesAndCostsSmall(
                                        players[currentlySelecting].getSpells(),
                                        players[currentlySelecting].getNumSpells()
                                )
                        );
                        miscScroll.setName("MAGIC");
                        currentMenuBox = miscScroll;
                        currentMenuBox.setActive(true);
                    }
                }else if (currentMenuBox.getName() == "ENEMYBOX" || currentMenuBox.getName() == "TARGETS") {
                    int targ = currentMenuBox.selectOptionInt();
                    playerActions[currentlySelecting].target = targ;


                    //now, change currentlySelecting
                    currentlySelecting++;
                    //skip over dead people (or until done)
                    while (currentlySelecting < numPlayers && players[currentlySelecting].isDead()) {
                        currentlySelecting++;
                    }
                    //if done, disable window b/c we good. Then activate player actions
                    if (currentlySelecting >= numPlayers){
                        //deactivate previous
                        currentMenuBox.setActive(false);
                        currentMenuBox = null;


                        //set up actor!
                        actor = 0;
                    }
                    // if not done, go to next player's action box
                    else{
                        //deactivate previous
                        currentMenuBox.setActive(false);
                        currentMenuBox = playerMenus[currentlySelecting];
                        currentMenuBox.setActive(true);

                    }

                }else if (currentMenuBox.getName().equals("ITEM")){
                    int selected = currentMenuBox.selectOptionInt();
                    boolean sameItem = false;

                    //make sure not selecting same item as someone before
                    for (int h = currentlySelecting - 1; h >= 0; h--){
                        int prevIndex = playerActions[h].itemIndex;
                        if (prevIndex != -1){ //someone previous selected an item!
                            if (prevIndex == selected){
                                sameItem = true;
                                break;
                            }
                        }
                    }

                    if (!sameItem && itemManager.getNumItems() > 0) {
                        playerActions[currentlySelecting].itemIndex = selected;
                        String[] targets;

                        if (itemManager.usedOnPlayer(playerActions[currentlySelecting].itemIndex)) { // load in players
                            targets = new String[livingPlayers.size()];

                            for (int i = 0; i < livingPlayers.size(); i++) {
                                targets[i] = livingPlayers.get(i).getName();
                            }
                        } else if (itemManager.usedOnEnemy(playerActions[currentlySelecting].itemIndex)) { //load in enemies
                            targets = new String[livingEnemies.size()];

                            for (int i = 0; i < livingEnemies.size(); i++) {
                                targets[i] = livingEnemies.get(i).getName();
                            }
                        } else { //by default, load in all participants
                            //make targets box
                            targets = new String[participants.size()];

                            //for now, just add participants to this
                            for (int i = 0; i < participants.size(); i++) {
                                targets[i] = participants.get(i).getName();
                            }
                        }

                        currentMenuBox.setActive(false);
                        miscScroll.loadNewOptions(targets);
                        miscScroll.setName("TARGETS");
                        currentMenuBox = miscScroll;
                        currentMenuBox.setActive(true);
                    }else if (itemManager.getNumItems() <= 0){
                        //do nothing. display message
                        messager.displayNewMessage("There are no items!", 5,7, 60);

                    }
                    else{//same item
                        //do nada and display message
                        messager.displayNewMessage("Can't select same item", 5,7, 60);

                    }

                }else if (currentMenuBox.getName().equals("MAGIC")){
                    int selected = currentMenuBox.selectOptionInt();
                    //it's called item index, but using for magic, too
                    playerActions[currentlySelecting].magicIndex = selected;


                    if (players[currentlySelecting].getNumSpells() > 0
                            && spellManager.enoughMP(selected,players[currentlySelecting])) {

                        int currentSpellIndex =
                                players[currentlySelecting].getSpells()[playerActions[currentlySelecting].magicIndex];

                        if (!spellManager.multi(currentSpellIndex)) {
                            String[] targets;
                            if (spellManager.usedOnPlayer(currentSpellIndex)) {
                                // load in players
                                targets = new String[livingPlayers.size()];

                                for (int i = 0; i < livingPlayers.size(); i++) {
                                    targets[i] = livingPlayers.get(i).getName();
                                }
                            } else {
                                //load in enemies
                                targets = new String[livingEnemies.size()];

                                for (int i = 0; i < livingEnemies.size(); i++) {
                                    targets[i] = livingEnemies.get(i).getName();
                                }
                            }
                            currentMenuBox.setActive(false);
                            miscScroll.loadNewOptions(targets);
                            miscScroll.setName("TARGETS");
                            currentMenuBox = miscScroll;
                            currentMenuBox.setActive(true);
                        }
                        else {
                            //MULTI TARGET ATTACK

                            int targ;
                            if (spellManager.usedOnPlayer(currentSpellIndex)) {
                                targ = -1;
                            }else{
                                targ = -2;
                            }
                            playerActions[currentlySelecting].target = targ;


                            //now, change currentlySelecting
                            currentlySelecting++;
                            //skip over dead people (or until done)
                            while (currentlySelecting < numPlayers && players[currentlySelecting].isDead()) {
                                currentlySelecting++;
                            }
                            //if done, disable window b/c we good. Then activate player actions
                            if (currentlySelecting >= numPlayers) {
                                //deactivate previous
                                currentMenuBox.setActive(false);
                                currentMenuBox = null;


                                //set up actor!
                                actor = 0;
                            }
                            // if not done, go to next player's action box
                            else {
                                //deactivate previous
                                currentMenuBox.setActive(false);
                                currentMenuBox = playerMenus[currentlySelecting];
                                currentMenuBox.setActive(true);

                            }
                        }


                    }
                    else if (players[currentlySelecting].getNumSpells() <= 0 ){
                        messager.displayNewMessage("You have no magic!", 5,7, 60);
                    }else{
                        messager.displayNewMessage("Not enough MP!", 5,7, 60);
                    }


                }
            }else if (backButtonPressed){
                if (currentMenuBox.getName().equals("PLAYER")){
                    //go to previous player, if exists
                    int prevSelector = currentlySelecting - 1;
                    while (prevSelector >= 0 && players[prevSelector].isDead()){
                        prevSelector--;
                    }
                    if (prevSelector >= 0){ // if there exists a living player before this one
                        currentlySelecting = prevSelector;
                        currentMenuBox = playerMenus[currentlySelecting];
                    }

                }else {
                    //go to this player's first menu
                    currentMenuBox = playerMenus[currentlySelecting];
                }
                currentMenuBox.setActive(true);
            }





        }
        else if (actor < participants.size()){ //it's time to perform an action/reaction or message



            if (turnPhase == 0) { //set up the turn, queue message for display

                int i = actor;


                //here: determine actor, reactor, and action
                //in player's action, we will determine reaction

                //either player or enemy, so get type for determining action
                if (participants.get(i).getType() == "Player") {

                    //get the player who this participant is
                    //pIndex is player index
                    int pIndex = 0;
                    for (int j = 0; j < players.length; j++) {
                        if (participants.get(i) == players[j]) {
                            pIndex = j;
                        }
                    }

                    turnActor = players[pIndex];
                    currentAct = playerActions[pIndex].action;
                    if (currentAct.equals("Item")) {
                        currentItemIndex = playerActions[pIndex].itemIndex;
                    }else{//MAGIC
                        currentItemIndex = playerActions[pIndex].magicIndex;
                    }


                    // check if player should target other player or enemy or both (which list being used)
                    if (currentAct.equals("Item")){
                        int itemDex = playerActions[pIndex].itemIndex;
                        if (itemManager.usedOnPlayer(itemDex)) {
                            reactor = livingPlayers.get(playerActions[pIndex].target);
                        }else if (itemManager.usedOnEnemy(itemDex)){
                            reactor = livingEnemies.get(playerActions[pIndex].target);
                        }else{
                            reactor = participants.get(playerActions[pIndex].target);
                        }
                    }
                    else if (currentAct.equals("Magic")){
                        //similar to above
                        int magicDex = players[pIndex].getSpells()[playerActions[pIndex].magicIndex];
                        if (spellManager.usedOnPlayer(magicDex) && playerActions[pIndex].target >= 0) {
                            reactor = livingPlayers.get(playerActions[pIndex].target);
                        }else if (playerActions[pIndex].target >= 0) {
                            reactor = livingEnemies.get(playerActions[pIndex].target);
                        }else { //multitarget!!!
                            reactor = null;
                            multiTargeted = playerActions[pIndex].target;
                        }
                    }
                    else  { //assume attacking enemy
                        reactor = livingEnemies.get(playerActions[pIndex].target);
                        if (reactor.isDead()) { //get first not dead enemy
                            int targ = 0;
                            for (targ = 0; targ < livingEnemies.size() && livingEnemies.get(targ).isDead(); targ++) {
                            }
                            if (targ >= livingEnemies.size()){ //all enemies are dead, one dead guy is left
                                reactor = livingEnemies.get(0);
                            }else {
                                reactor = livingEnemies.get(targ);
                            }
                        }
                    }


                } else {
                    //for now, just have them attack random player
                    //TODO: change to more than this lol
                    int target = (int) (Math.random() * (numPlayers));
                    if (target >= numPlayers) {
                        throw new RuntimeException("bro u implemented random wrong");
                    }
                    while (players[target].isDead()) {
                        //re roll until not dead
                        target = (int) (Math.random() * (numPlayers));
                    }

                    turnActor = participants.get(i);
                    currentAct = "";
                    reactor = players[target];

                }

                //since we are in phase 0, simply display actor's message
                if (currentItemIndex < 0){ currentItemIndex = 0;}
                if (currentAct.equals("Item")) {
                    turnActor.performMessage(currentAct, itemManager.getIndexName(currentItemIndex));
                }else if (currentAct.equals("Magic")){
                    turnActor.performMessage(currentAct,
                            spellManager.getName(turnActor.getSpells()[currentItemIndex]));
                }else{
                    turnActor.performMessage(currentAct, "");
                }

                turnPhase++;

            }
            else if (turnPhase == 1){
                //phase 1: actor acts
                if (reactor != null) {
                    turnActor.performAction(currentAct, reactor, currentItemIndex);
                }else{
                    //multi target
                    if (multiTargeted == -1){//players
                        for (int i = 0; i < livingPlayers.size(); i++){
                            turnActor.performAction(currentAct, livingPlayers.get(i), currentItemIndex);
                            if (i < livingPlayers.size()-1) {
                                turnActor.refundMP(1, currentItemIndex);
                            }
                        }
                    }else{//enemies
                        for (int i = 0; i < livingEnemies.size(); i++){
                            turnActor.performAction(currentAct, livingEnemies.get(i), currentItemIndex);
                            if (i < livingEnemies.size()-1) {
                                turnActor.refundMP(1, currentItemIndex);
                            }
                        }
                    }
                }
                turnPhase++;


            }
            else if (turnPhase == 2){
                //phase 2: reactor react message
                //reactor's reaction is already loaded after player's action

                if (reactor != null) {
                    reactor.performReactionMessage();
                }else{
                    //multi target
                    if (multiTargeted == -1){//players
                        for (BattlePlayer b : livingPlayers){
                            b.performReactionMessage();
                        }
                    }else{//enemies
                        for (Enemy e : livingEnemies){
                            e.performReactionMessage();
                        }
                    }
                }

                turnPhase++;



            }

            else { //turnPhase == 3, hopefully
                assert(turnPhase == 3);


                //phase 3: reactor reacts

                if (reactor != null) {
                    reactor.performReaction();
                }else{
                    //multi target
                    if (multiTargeted == -1){//players
                        for (BattlePlayer b : livingPlayers){
                            b.performReaction();
                        }
                    }else{//enemies
                        for (Enemy e : livingEnemies){
                            e.performReaction();
                        }
                    }
                }

                turnPhase++;




                //next actor, and reset turn phase
                actor++;
                turnPhase = 0;
            }




        }

        //at end, clean out dead ppl
        for (int i = 0; i < participants.size(); i++){
            if (participants.get(i).isDead()){

                //subtract 1 from actor if the dead person has already acted that turn
                if (actor > i){
                    actor--;
                }

                participants.remove(participants.get(i));
                i--;
            }
        }



        //check if it's over
        String dead = calcDead();
        if (dead == "Humans") {
            endBattle(false);
        } else if (dead == "Enemies") {
            endBattle(true);
        }

    }

    public void initializeBattle(){

        //add everything to participants, in order of speed
        //this is some bad time complexity, but so few players/enemies it doesn't matter

        //first, add all players
        for(int i = 0; i < numPlayers; i++){
            if(participants.isEmpty()){
                participants.add(players[i]);
            }else{
                //keep scrolling until speed is more
                int posToAdd = 0;
                while (posToAdd < participants.size()
                        && players[i].getSpeed() < participants.get(posToAdd).getSpeed()){
                    posToAdd++;
                }
                participants.add(posToAdd,players[i]);
            }
        }



        //now for enemies
        for(int i = 0; i < numEnemies; i++){
            //keep scrolling until speed is more
            int posToAdd = 0;
            while (posToAdd < participants.size()
                    && enemies[i].getSpeed() < participants.get(posToAdd).getSpeed()){
                posToAdd++;
            }
            participants.add(posToAdd,enemies[i]);
        }


        actor = participants.size();
        currentlySelecting = 0;


        battleWon = false;
        battleActive = true;
        turnPhase = 0;

        afterBattleFrames = 0;



    }

    /**
     * Scroll thru humans and enemies, see if all of one group is dead
     * Important for determining if battle is over or not
     * @return "Humans", "Enemies" or "None" if nobody's dead
     */
    public String calcDead(){
        int numHumans = 0;
        int numEnemies = 0;
        for (Battleable b: participants){
            if(b.getType().equals("Player")){
                numHumans++;
            }else{
                numEnemies++;
            }
        }
        //If humans and enemies are dead, humans should lose
        if (numHumans == 0){
            return "Humans";
        }else if (numEnemies == 0){
            return "Enemies";
        }else{
            return "None";
        }
    }

    //dude this is soooo sus but I do not see a better option for messages... yet
    public static void addToMessageQueue(String message){
        messageQueue.addLast(message);
    }



    public void draw(SpriteBatch batch){
        batch.draw(backgroundPic,0,0);
        //for now: don't draw dead players
        //obviously don't draw dead enemies
        for (int i = 0; i < numPlayers; i++) {
            if(!players[i].isDead()) {
                players[i].draw(batch, PLAYERSPOT_X, P1_SPOT_Y - (i * PLAYER_Y_DIST));
            }
        }
        for (int i = 0; i < numEnemies; i++){
            if(!enemies[i].isDead()) {
                enemies[i].draw(batch, ENEMYSPOT_X, ENEMY_TOPSPOT_Y - (i * ENEMY_Y_DIST));
            }
        }
        if (currentMenuBox != null){

            currentMenuBox.draw(batch);
        }
        for (int i = 0; i < numPlayers; i++){
            if(!players[i].isDead()) {
                playerStatDisplays[i].draw(batch);
            }
        }

        messager.draw(batch);

    }

    public void dispose(){
        backgroundPic.dispose();
    }


}
