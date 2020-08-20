package com.mygdx.game;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;


/**
 * NOTE TO USER!
 * The mapsfile cannot have any whitespace in it! This java version is 1.8 which does not have the strip method
 * apparently, so I am not using it.
 */


/**
 * Manages the game map! Uses 2D arrays of integers and "tile" objects
 */
public class MapManager {


    //TODO: CLARIFY PERMISSION GRID STUFF
    //TODO: IDEA: PERMISSIONS BELOW ZERO LOOK BLACK BUT ARE WARP AREAS


    //map file with exit info for maps
    private static final String EXITINFO = "maps/exitinfo.csv";
    private static final String SPRITEINFO = "maps/spriteinfo.csv";

    //height and width of how many tiles are shown on a screen at once
    private static final int TILESWIDTH = 17;
    private static final int TILESHEIGHT = 13;





    //number of pixels on height or width of tile (assuming tiles are gonna stay as squares)
    private static final int TILEDIM = 64;


    //The maps stored for the game. See Map class below
    private Map[] maps;

    //the current map the game is on in the map list (and int!)
    private Map currentMap;
    private int currentMapInt;

    //The outer map that the player is on, the loaded zone. Integers represent tiles
    private int[][] currentMapGrid;

    //The portion of the map seen by the player, represented with integers
    private int[][] screenMap;

    //The screen map represented as Tile objects that can be drawn
    private Tile[][] tileGrid;

    // Bottom right position of player's screen
    private int playerBRX;
    private int playerBRY;

    //Player's position in middle of screen
    private int playerX;
    private int playerY;

    //Exit information, stored with integers
    private int[][] exitInfo;

    //if player is moving, important for slow movement
    private boolean moving;


    //List of humans on the current map
    private Human[] currentSprites;

    //Humans to draw(used in draw method)
    private Human[][] humansToDraw;
    private int[] humansPerRow;


    private boolean midTransition;
    private int currentExitIndex;

    private ArrayDeque<Tile> foreground;

    private boolean playerInvis;

    /**
     * The Map class holds things that are in a map: their names, files, tilesets, height, and width
     */
    public class Map {

        //Name of map
        public String title;

        //Name of map file (for now: CSV file)
        public String mapFile;

        //Name of tileset file (in png form)
        public String tileSet;

        //Height and width of map, in tiles
        public int height;
        public int width;

        public int[] collisionNums;
        public int[] foregroundTiles;


        public int music;

        public int[][] possibleEncounters;





        /**
         * Constructs a Map.
         *
         * @param title Map title
         * @param mfile CSV file for map
         * @param tfile Image file for tileset
         * @param h     Height (in num tiles)
         * @param w     Width (in num tiles)
         * @param c     Collision nums (temporary)

         */
        public Map(String title, String mfile, String tfile, int h, int w, int[] c, int music,
                   int[][] possibleEncounters, int[] f) {
            this.title = title;
            this.mapFile = mfile;
            this.tileSet = tfile;
            this.height = h;
            this.width = w;
            this.collisionNums = c;
            this.music = music;
            this.possibleEncounters = possibleEncounters;
            this.foregroundTiles = f;
        }


        public void generateEncounter(){
            //if player is in car, no encounters!
            if (TestGame.eventFlags[100]){ return; }


            if (possibleEncounters == null || possibleEncounters.length == 0){ return; }

            //for now: universal encounter rate
            int ENCOUNTER_RATE = 30; //for 1/ENCOUNTER_RATE

            int randomNum = (int)(Math.random()*ENCOUNTER_RATE) + 1;

            if(randomNum == ENCOUNTER_RATE){//encounter time!
                TestGame.upcomingMode = "battle";
                TestGame.upcomingEnemies = possibleEncounters[(int)(Math.random()*possibleEncounters.length)];
            }
        }


    }

        /**
     * Constructs a MapManager. This method is more useful for testing, as it
     * automatically starts the player in the middle.
     * @param mapsfile           The internal file for the manager containing info for all maps.
     * @param startingMapNum    The number map of maps on which to start.
     */
    public MapManager(String mapsfile, int startingMapNum){
        try {
            // open up mapsfile, get info, make maps
            BufferedReader buff = new BufferedReader(new FileReader(mapsfile));
            //first line is header
            buff.readLine();
            //second line contains info on number of maps in second slit
            int numMaps = Integer.parseInt(buff.readLine().split(",")[1]);
            Map[] maps = new Map[numMaps];
            //index used to add to map
            int mapIndex = 0;
            //this is the real first line
            String line = buff.readLine();
            while (line != null) {
                String[] mapStrings = line.split(",");
                //Thing zero is map title.
                String mapTitle = mapStrings[0];
                //Thing one is map file
                String mapFile = "maps/" + mapStrings[1];
                //Thing two is tileset file
                String tileFile = "maps/" + mapStrings[2];
                //Next is the height, then width
                int height = Integer.parseInt(mapStrings[3]);
                int width = Integer.parseInt(mapStrings[4]);
                //collisions are next, handling this is letter by letter
                //TODO: change to permissions file??


                int[] collList;
                //if file contains no coll info, make empty
                if (mapStrings.length > 5) {
                    String[] collStringList = mapStrings[5].split("/");
                    collList = new int[collStringList.length];
                    for (int i = 0; i < collList.length; i++) {

                        collList[i] = Integer.parseInt(collStringList[i]);
                    }
                }
                else{
                    collList = new int[0];
                }


                int music = Integer.parseInt(mapStrings[6]);

                int encounters = Integer.parseInt(mapStrings[7]);

                int[][] possEncounters;
                String ENCOUNTER_DATA = "maps/encounterdata.csv";
                if (encounters > 0){//has encounters
                    //go thru encounters file, get data
                    BufferedReader buff2 = new BufferedReader(new FileReader(ENCOUNTER_DATA));

                    for (int i = 0; i < encounters; i++) {
                        buff2.readLine(); //skip lines until at encounter number
                    }

                    String[] encounterStrings = buff2.readLine().split(",");
                    possEncounters = new int[encounterStrings.length][];

                    for(int i = 0; i < possEncounters.length; i++){
                        String[] encountersI = encounterStrings[i].split("#");
                        possEncounters[i] = new int[encountersI.length];
                        for (int j = 0; j < encountersI.length; j++){
                            possEncounters[i][j] = Integer.parseInt(encountersI[j]);
                        }
                    }

                }else{
                    possEncounters = null;
                }


                int[] foreList;
                //if file contains no coll info, make empty
                String[] foreStringList = mapStrings[8].split("/");
                foreList = new int[foreStringList.length];
                for (int i = 0; i < foreList.length; i++) {
                    foreList[i] = Integer.parseInt(foreStringList[i]);
                }




                //All the remaining strings are exit things, which I will just put into Map obj
                //using pi method to make things look nicer
                Map map = new Map(mapTitle, mapFile, tileFile, height, width, collList,music,possEncounters,foreList);

                maps[mapIndex] = map;
                mapIndex++;

                line = buff.readLine();
            }
            this.maps = maps;
            buff.close();
            //now open exit info file, store exit info
            buff = new BufferedReader(new FileReader(EXITINFO));
            //first line is header, skip
            buff.readLine();

            //now, num of lines should be equal to num of maps, else error will likely be thrown
            exitInfo = new int[maps.length][];
            for (int i = 0; i < maps.length; i++) {
                String b = buff.readLine();
                if (b == null){ continue; }
                System.out.println(b);
                String[] lineInfo = b.split(",");
                //num exits is at index 1
                exitInfo[i] = new int[lineInfo.length - 1];
                for (int j = 0; j < exitInfo[i].length; j++) {
                    exitInfo[i][j] = Integer.parseInt(lineInfo[j+1]);
                }

            }
            buff.close();
        }catch (IOException e){
            System.out.println("io exception in MM constuctor!oh no");
        }

        humansToDraw = new Human[TILESHEIGHT][TILESWIDTH];
        humansPerRow = new int[TILESHEIGHT];

        //This method basically constructs everything
        loadNewMap(startingMapNum,TILESWIDTH/2,TILESHEIGHT/2);


        moving = false;

        this.midTransition = false;

        foreground = new ArrayDeque<Tile>();

        playerInvis = false;
    }


    private void printList(int[] ls){
        String k="[";
        for (int i=0; i < ls.length; i++){
            k+= ls[i] + ", ";
        }
        System.out.println(k + "]");
    }

//
//    /**
//     * Same as above, with new parameters.
//     * @param mapsfile           The internal map file for the manager.
//     * @param startingMapNum    The number map of maps on which to start.
//     * @param playerStartX      The starting X location of player in the starting map
//     * @param playerStartY      The starting Y location of player in the starting map
//     */
//    public MapManager(String mapsfile, int startingMapNum, int playerStartX, int playerStartY){
//
//        try {
//            // open up mapsfile, get info, make maps
//            BufferedReader buff = new BufferedReader(new FileReader(mapsfile));
//            //first line is header
//            buff.readLine();
//            //second line contains info on number of maps in second slit
//            int numMaps = Integer.parseInt(buff.readLine().split(",")[1]);
//            Map[] maps = new Map[numMaps];
//            //index used to add to map
//            int mapIndex = 0;
//            //this is the real first line
//            String line = buff.readLine();
//            while (line != null) {
//                String[] mapStrings = line.split(",");
//                //Thing zero is map title.
//                String mapTitle = mapStrings[0];
//                //Thing one is map file
//                String mapFile = "maps/" + mapStrings[1];
//                //Thing two is tileset file
//                String tileFile = "maps/" + mapStrings[2];
//                //Next is the height, then width
//                int height = Integer.parseInt(mapStrings[3]);
//                int width = Integer.parseInt(mapStrings[4]);
//                //collisions are next, handling this is letter by letter
//                //TODO: change to permissions file??
//                String collString  = mapStrings[5];
//                int[] collList = new int[collString.length()];
//                for(int i = 0; i < collList.length; i++){
//
//                    collList[i] = Integer.parseInt(collString.substring(i,i+1));
//                }
//                int music = Integer.parseInt(mapStrings[6]);
//                //All the remaining strings are exit things, which I will just put into Map obj
//                //using pi method to make things look nicer
//                Map map = new Map(mapTitle, mapFile, tileFile, height, width, collList, music);
//
//                maps[mapIndex] = map;
//                mapIndex++;
//
//                line = buff.readLine();
//            }
//            this.maps = maps;
//            buff.close();
//            //now open exit info file, store exit info
//            buff = new BufferedReader(new FileReader(EXITINFO));
//            //first line is header, skip
//            buff.readLine();
//            //now, num of lines should be equal to num of maps, else error will likely be thrown
//            exitInfo = new int[maps.length][];
//            System.out.println("hello");
//            for (int i = 0; i < maps.length; i++){
//                String[] lineInfo = buff.readLine().split(",");
//                //num exits is at index 1
//                exitInfo[i] = new int[Integer.parseInt(lineInfo[1])];
//                for (int j = 0; j < exitInfo[i].length; j++){
//                    exitInfo[i][j] = Integer.parseInt(lineInfo[j+2]);
//                    System.out.println(exitInfo[i][j]);
//                }
//            }
//            buff.close();
//
//
//
//        }
//        catch (IOException e){
//            System.out.println("io exception in MM constuctor!oh no");
//        }
//
//
//
//
//
//        //This method basically constructs everything
//        loadNewMap(startingMapNum,playerStartX,playerStartY);
//
//        moving = false;
//
//    }


    public boolean isMoving(){return moving;}

    /**
     * Loads a new map for the Map Manager. Changes tile values, tileset, map file
     * @param mapNum   The number map to switch to (will be addressed in some internal file)
     * @param startX The x-coord player starts at when entering map
     * @param startY The y-coord player starts at when entering map
     */
    public void loadNewMap(int mapNum, int startX, int startY){

        System.out.println("loading new map");

        playerX = startX;
        playerY = startY;
        playerBRX = playerX - TILESWIDTH/2;
        playerBRY = playerY - TILESHEIGHT/2;

        currentMap = maps[mapNum];
        currentMapInt = mapNum;

        //Construct current grid using current map dimensions
        currentMapGrid = new int[currentMap.width][currentMap.height];

        screenMap = new int[TILESWIDTH][TILESHEIGHT];
        tileGrid = new Tile[TILESWIDTH][TILESHEIGHT];
        for (int i = 0; i < tileGrid.length; i++){
            for (int j = 0; j < tileGrid[i].length; j++){
                if (tileGrid[i][j] != null){ tileGrid[i][j].dispose(); }
                tileGrid[i][j] = new Tile(new Texture(currentMap.tileSet),TILEDIM*i,TILEDIM*j);
            }
        }

        //load in sprites for this map
        loadSprites();



        //next, load the map file
        loadMapFile(currentMap.mapFile, currentMapGrid);

        SoundTextureManager.playNewBgSong(currentMap.music);
    }

    /**
     * Loads the sprite data for the current map into the game
     */
    private void loadSprites(){

        try{

            BufferedReader buff = new BufferedReader(new FileReader(SPRITEINFO));

            //first line is header
            buff.readLine();

            //skip lines until next line is one with data we want
            for (int i = 0; i < currentMapInt; i++){
                buff.readLine();
            }


            //now this is the line we want
            String b = buff.readLine();
            if (b == null){
                System.out.println("sprites is null");
                currentSprites = new Human[0];
                return;
            }
            String[] info = b.split(",");

            //index 0 of info is useless(current map num) so can skip, start at 1
            //each file has 5 vals per sprite

            int INFO_PER_SPRITE = 8;

            int numSprites = (info.length-1) / INFO_PER_SPRITE;
            currentSprites = new Human[numSprites];

            for(int i = 0; i < currentSprites.length; i++){
                //since 7 infos per sprite, multiply by 7. Add 1 b/c val 0 is useless
                int spriteIndex = (i*INFO_PER_SPRITE) + 1;

                //info one gets from file
                String spriteFile = info[spriteIndex];
                int spriteX = Integer.parseInt(info[spriteIndex+1]);
                int spriteY = Integer.parseInt(info[spriteIndex+2]);
                int scriptNum = Integer.parseInt(info[spriteIndex+3]);
                int eventF = Integer.parseInt(info[spriteIndex+4]);
                boolean bigHitbox = Boolean.parseBoolean(info[spriteIndex+5]);
                int movementpattern = Integer.parseInt(info[spriteIndex+6]);
                int startingDirec = Integer.parseInt(info[spriteIndex+7]);


                currentSprites[i] = new Human(spriteFile,spriteX,spriteY,
                        scriptNum, eventF, movementpattern, bigHitbox, startingDirec);
            }



            buff.close();





        }
        catch (IOException e){
            System.out.println("io exception during load sprites :(");
        }


    }


    /**
     * Does the MapManger's share of the update that occurs on each frame.
     */
    public void update(boolean inCutscene){

        //don't continue updating if loading next map
        if (midTransition){
            if (BattleAnimator.halfwayDone()){
                //transition animation is half done so we good
                loadNewMap(exitInfo[currentMapInt][currentExitIndex],
                        exitInfo[currentMapInt][currentExitIndex+1],
                        exitInfo[currentMapInt][currentExitIndex+2]);
                midTransition = false;
            }
            return;
        }


        //update map
        calculateMap();
        updateTiles();


        //Movement is calculated before update is called. Can't tell whether to do this before or after
        //I've tried both, and they yield similar results.
        if (currentMapGrid[playerX][playerY] < 0){
            //Tile below zero means it is a warp!
            //Different negative numbers indicate different warps, so find which one it is
            //-1 means exit 1, -2 means exit 2, etc.
            int exitNum = Math.abs(currentMapGrid[playerX][playerY]);

            //index of exit will be four times the exit num, minus one
            // this is bc there are 4 values per exit (next map, x start, y start, and the tile type which exit is)
            int exitIndex = (exitNum-1)*4;

            if (exitIndex > exitInfo[currentMapInt].length){
                throw new RuntimeException("not enough room for current map stuff");
            }

            //load the new map

            //first, display animation
            BattleAnimator.loadAnimation(3);

            midTransition = true;
            currentExitIndex = exitIndex;


        }

        if (!inCutscene) {
            //update sprites
            for (Human h : currentSprites) {
                //move human dependent on their movement pattern
                h.naturalUpdate(this);

            }
        }


    }


    public boolean isMidTransition(){
        return midTransition;
    }

    /**
     * Loads the map CSV file into the map grid
     * @param mapFile   The CSV file from which to load the map
     * @param grid      The map grid to load into
     */
    private void loadMapFile(String mapFile,int[][] grid)  {
        try {
            BufferedReader buff = new BufferedReader(new FileReader(mapFile));
            // For now, we know all vals will be split with commas since I'm making the files
            // ALSO, we know CSVs will be right size (else error above) so we good, don't have to worry about readline being null!
            for (int i = grid[0].length - 1; i >= 0; i--) {
                String line = buff.readLine();
                //Make sure grid and mapFile correlate
                if (line == null){
                    throw new RuntimeException("Map file does not match grid!");
                }
                String[] nums = line.split(",");
                //Checking same thing as above
                if (nums.length != grid.length){
                    throw new RuntimeException("Map file does not match grid!");
                }
                for (int j = 0; j < grid.length; j++) {
                    grid[j][i] = Integer.parseInt(nums[j]);

                }

            }
            buff.close();
        }
        catch (IOException e) {
            System.out.println("there was an IO exception!");
            e.printStackTrace();
        }

    }

    /**
     * Calculates the player's integer tile map using their position and the current big map.
     */
    private void calculateMap(){
        for (int i = 0; i < screenMap.length; i++){
            for (int j = 0; j < screenMap[i].length; j++){
                //check if tile is OOB
                if (i + playerBRX > -1
                        && i + playerBRX < currentMap.width
                        && j + playerBRY > -1
                        && j + playerBRY < currentMap.height) {
                    //If it's not, we are good, continue as normal.
                    if (currentMapGrid[i + playerBRX][j + playerBRY] >= 0) {
                        screenMap[i][j] = currentMapGrid[i + playerBRX][j + playerBRY];
                    }else{
                        //Below 0, so do exit tile
                        // Get tile from exit data

                        int exitIndex = (((-1)*currentMapGrid[i + playerBRX][j + playerBRY]) -1)*4;
                        screenMap[i][j] = exitInfo[currentMapInt][exitIndex+3];
                    }

                }
                //if it is, make area zero (default bg tile, likely black)
                else{
                    screenMap[i][j] = 0;
                }

            }
        }
    }

    /**
     * Moves player about the big grid
     * @param incX	The amount player should move by in horizontal direction
     * @param incY The amount player should move by in vertical direction
     */
    public void move(int incX, int incY){
        //check if it's out of bounds in y
        if (playerY + incY >= 0 && playerY + incY < (currentMap.height)){

            playerBRY += incY;
            playerY += incY;
        }
        //check OOB for x, too

        if (playerX + incX >= 0 && playerX + incX < (currentMap.width)){

            playerBRX += incX;
            playerX += incX;
        }
        //if player's new location is OOB or running into sprite, reset back
        if (outOfBounds(currentMapGrid[playerX][playerY]) || touchingSprite(playerX,playerY)){
            playerBRY -= incY;
            playerY -= incY;
            playerBRX -= incX;
            playerX -= incX;
        }

        if (incX != 0 || incY != 0){
            currentMap.generateEncounter();
        }

    }

    /**
     * Basically just searching with the OOB list.
     * I am not doing binary search for now as there are not enough things for it to matter.
     * This method's existence is likely temporary, like collisionNums.
     * @param num Num of block to search for
     * @return	True if num block if OOB (in list), false otherwise
     */
    private boolean outOfBounds(int num){
        for (int j : currentMap.collisionNums){
            if (j == num){ return true;}
        }
        return false;
    }

    /**
     * Sees if tile is of the "in foreground" type
     * I am not doing binary search for now as there are not enough things for it to matter.
     * @param num Num of block to search for
     * @return	True if num block is a foreground tile (in list), else false
     */
    private boolean inForeground(int num){
        for (int j : currentMap.foregroundTiles){
            if (j == num){return true;}
        }
        return false;
    }


    /**
     * Checks whether a player moving to this x and y would cause them to run into a sprite.
     * @param x The player's potential grid x
     * @param y The player's potential grid y
     * @return whether this x and y would run into a sprite
     */
    private boolean touchingSprite(int x,int y){
        for (Human h: currentSprites){
            //big hitbox means human takes up 2 tiles
            // So, if big hitbox, check adjacent tile too
            if (h.isBigHitbox() && h.getX() + 1 == x && h.getY() == y){
                return h.isActive();
            }

            if(h.getX() == x && h.getY() == y){
                return h.isActive();
            }
        }
        return false;
    }

    /**
     * Finds the sprite that the player could potentially touch, should they move to
     *  this square. Returns null if no sprite there
     * @param x The player's potential grid x
     * @param y The player's potential grid y
     * @return The human this would maybe touch
     */
    private Human spriteTouched(int x,int y){
        for (Human h: currentSprites){
            //big hitbox means human takes up 2 tiles
            // So, if big hitbox, check adjacent tile too
            if (h.isBigHitbox() && h.getX() + 1 == x && h.getY() == y && h.isActive()){
                return h;
            }

            if(h.getX() == x && h.getY() == y && h.isActive()){
                return h;
            }
        }
        return null;
    }

    /**
     * Updates the grid tiles to match the integer tiles
     */
    private void updateTiles() {
        for (int i = 0; i < tileGrid.length; i++){
            for (int j = 0; j < tileGrid[i].length; j++){
                tileGrid[i][j].setFrame(screenMap[i][j]);
            }
        }
    }

    /**
     * Master draw for Map Manager
     * @param batch
     * @param player
     * @param xmove
     * @param ymove
     */
    public void draw(SpriteBatch batch, FilmStrip player, int xmove, int ymove){
        for (int i = 0; i < tileGrid.length; i++){
            for (int j = 0; j < tileGrid[i].length; j++){
                if (inForeground(tileGrid[i][j].getFrame())) {
                    foreground.add(tileGrid[i][j]);
                }else{
                    batch.draw(tileGrid[i][j], tileGrid[i][j].getX(), tileGrid[i][j].getY());
                }
            }
        }
        drawSprites(batch,player,xmove,ymove);
        while(!foreground.isEmpty()){
            Tile t = foreground.pollFirst();
            batch.draw(t, t.getX(), t.getY());
        }

    }


    /**
     * Draws the tiles on the screen!
     * @param batch The SpriteBatch, likely from main where MapManger is used.
     */
    public void draw(SpriteBatch batch){
        for (int i = 0; i < tileGrid.length; i++){
            for (int j = 0; j < tileGrid[i].length; j++){
                batch.draw(tileGrid[i][j], tileGrid[i][j].getX(), tileGrid[i][j].getY());
            }
        }

    }

    /**
     * Draws all the sprites on the screen,
     * @param batch  The SpriteBatch, likely from main where MapManger is used.
     * @param player The player of the game. Must make sure they appear behind other sprites
     * @param xmove X offset for player from movement
     * @param ymove Y offset for player from movement
     */
    public void drawSprites(SpriteBatch batch, FilmStrip player, int xmove, int ymove){
        //scrolling thru all sprites twice so that player gets drawn after high ppl, below low ppl

        //first, set all humans per row to 0
        for (int i = 0; i < humansPerRow.length; i++){
            humansPerRow[i] = 0;
        }

        //do a weird variant of bucket sort on currentSprites to get highest Y first
        for (int i = 0; i < currentSprites.length; i++) {
            Human h = currentSprites[i];
            int xDraw = h.getX() - playerBRX;
            int yDraw = h.getY() - playerBRY;

            //check if xDraw, yDraw in bounds
            if (xDraw > -1 && yDraw > -1 && xDraw < TILESWIDTH && yDraw < TILESHEIGHT) {
                humansToDraw[yDraw][humansPerRow[yDraw]] = h;
                humansPerRow[yDraw]++;
            }
        }



        //this loop only draws high up bois
        for (int i = humansToDraw.length-1; i > (TILESHEIGHT/2); i--){
            for (int j = 0; j < humansPerRow[i]; j++){
                Human h =humansToDraw[i][j];
                int humanXDraw = h.getX() - playerBRX;
                int humanYDraw = h.getY() - playerBRY;
                h.draw(batch, humanXDraw*TILEDIM, humanYDraw*TILEDIM);
            }
        }

        if (!playerInvis) {
            player.draw(batch, TILEDIM * (TILESWIDTH / 2), TILEDIM * (TILESHEIGHT / 2), xmove, ymove, 7);
        }

        //this loop only draws low down and level bois
        for (int i = (TILESHEIGHT/2); i > -1; i--){
            for (int j = 0; j < humansPerRow[i]; j++){
                Human h =humansToDraw[i][j];
                int humanXDraw = h.getX() - playerBRX;
                int humanYDraw = h.getY() - playerBRY;
                h.draw(batch, humanXDraw*TILEDIM, humanYDraw*TILEDIM);
            }
        }

    }


    public void attemptTalk(int direction, TextEventHandler tex){
        //down is 0, up is 1, left is 2, right is 3
        //get targeted x and y using direction. It's one forward from player's spot


        //just making them zero so that they are initialized always
        int targetX = 0;
        int targetY = 0; //lol "target" this aint machine learning

        //the direction talking target should face, assuming they exist
        //down is 0, up is 1, left is 2, right is 3
        int targetDirect;

        switch (direction){
            case 0: //down
                targetX = playerX;
                targetY = playerY - 1;
                targetDirect = 1; //face up
                break;
            case 1: //UP
                targetX = playerX;
                targetY = playerY + 1;
                targetDirect = 0; //face down
                break;
            case 2: //left
                targetX = playerX - 1;
                targetY = playerY;
                targetDirect = 3; //face right
                break;
            case 3: //right
                targetX = playerX + 1;
                targetY = playerY;
                targetDirect = 2; //face left
                break;
            default:
                targetDirect = 69420;
                System.out.println("uh oh that is no direction!");
        }

        //now, go find potential human
        Human talker = spriteTouched(targetX,targetY);

        //only talk if someone is in that spot, of course
        if (talker != null){

            tex.loadTextNum(talker.getDialogueNum());
            talker.setTalking(true);
            talker.changeDirection(targetDirect);
        }

    }


    public void dispose(){
        for (Tile[] t : tileGrid){
            for (Tile j: t){
                j.dispose();
            }
        }
    }


    public int currentMapSong(){
        return currentMap.music;
    }

    /**
     * Tells if a Human object can move to a specific tile. Checks collisions with walls,
     *  the player, and other Human objects.
     * @param futureX   The x of the tile to which the Human would like to move
     * @param futureY   The y of the tile to which the Human would like to move
     * @return          True if the tile (futureX, futureY) is free, else false
     */
    public boolean canMoveHuman(int futureX, int futureY){
        return !(outOfBounds(currentMapGrid[futureX][futureY]) || touchingSprite(futureX,futureY)
                || (futureX == playerX && futureY == playerY));
    }


    public int getPlayerX() {
        return playerX;
    }

    public int getPlayerY() {
        return playerY;
    }


    public Human getSprite(int index){
        return currentSprites[index];
    }

    public int getNumSprites(){
        return currentSprites.length;
    }

    /**
     * FOR DEBUGGING PURPOSES:
     * reloads map at location, self-explanatory
     */
    public void reloadMapAtLocation(){
        loadNewMap(currentMapInt,playerX,playerY);
    }

    public void togglePlayerInvis(){
        playerInvis = !playerInvis;
    }


}


