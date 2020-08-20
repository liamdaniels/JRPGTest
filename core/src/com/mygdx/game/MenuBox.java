package com.mygdx.game;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;

/**
 * Menu box from much player can select options
 */
public class MenuBox {

    private static final int TILEDIM = 64;


    private final String MENUTOP = "menuboxtop.png";
    private final String MENUMID = "menuboxmid.png";
    private final String MENUBOT = "menuboxbot.png";
    private final String CURSOR = "cursor.png";
    //private final String FONTFILE = "Rubik-Regular.ttf";
    private final String FONTFILE = "Fontin-Regular.ttf";

    private final int MAXOPS = 30;

    //whether or not this should be displayed
    //Here for performance purposes. Don't wanna construct new MenuBox over and over in other classes
    private boolean active;

    //number of options that this menu has
    private int numOptions;

    //the options
    private String[] options;
    //options displayed as text
    private String displayFont;

    //the options that are to be displayed
    private Texture[] optionTex;
    private Texture cursorTex;

    private BitmapFont text;

    //name of this menu box. Informal (in code use only)
    private String name;

    private boolean scrolling;


    //x and y location of box (in tiles, not pixels)
    private int x;
    private int y;

    //option currently being selected
    private int currentOption;


    public MenuBox(int x, int y){
        this.active = false;
        this.x = x;
        this.y = y;
        currentOption = 0;
        this.options = new String[MAXOPS];
        numOptions = 0;
        optionTex = new Texture[MAXOPS];
        name= null;

        //Took this code from online, presumably makes a new BitmapFont with the desired font file
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(FONTFILE));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 50;
        text = generator.generateFont(parameter);
        generator.dispose();
        text.getData().setScale(0.7f, 1f);

        cursorTex = new Texture(CURSOR);

        scrolling = false;

    }

    public MenuBox(String[] options, int x, int y){
        //using numoptions for everything so I can have large texture lists yee
        this.options = new String[MAXOPS];
        this.numOptions = options.length;
        for (int i = 0; i < numOptions; i++){
            this.options[i] = options[i];
        }
        this.active = false;
        this.x = x;
        this.y = y;
        currentOption = 0;

        name= null;

        //make display string
        //subject to change I think
        //idea for now: 1 tex per option
        displayFont = "";
        for (int i = 0; i < numOptions; i++){
            displayFont += options[i] + "\n";
        }


        //Took this code from online, presumably makes a new BitmapFont with the desired font file
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(FONTFILE));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 50;
        text = generator.generateFont(parameter);
        generator.dispose();
        text.getData().setScale(0.7f, 1f);

        cursorTex = new Texture(CURSOR);


        //create necessary textures
        //idea for now: 1 tex per option
        optionTex = new Texture[MAXOPS];
        for (int i = 0; i < numOptions; i++){
            String menuType;
            //make it diff it it's bottom, top or middle
            if (i == 0) {
                menuType = MENUTOP;
            }else if (i == numOptions-1){
                menuType = MENUBOT;
            }else{
                menuType = MENUMID;
            }
            optionTex[i] = new Texture(menuType);
        }

        scrolling = false;

    }


    /**
     * Make scrolling text box! So can have like 30 items, for example
     * @param options
     * @param x
     * @param y
     * @param height
     */
    public MenuBox(String[] options, int x, int y, int height){
        this.scrolling = true;

        this.options = new String[MAXOPS];
        this.numOptions = options.length;
        for (int i = 0; i < numOptions; i++){
            this.options[i] = options[i];
        }

        this.active = false;
        this.x = x;
        this.y = y;
        currentOption = 0;

        name= null;

        //Took this code from online, presumably makes a new BitmapFont with the desired font file
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(FONTFILE));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 50;
        text = generator.generateFont(parameter);
        generator.dispose();
        text.getData().setScale(0.7f, 1f);

        cursorTex = new Texture(CURSOR);

        optionTex = new Texture[height];
        for (int i = 0; i < height; i++){
            String menuType;
            //make it diff it it's bottom, top or middle
            if (i == 0) {
                menuType = MENUTOP;
            }else if (i == height-1){
                menuType = MENUBOT;
            }else{
                menuType = MENUMID;
            }
            optionTex[i] = new Texture(menuType);
        }

    }


    public MenuBox(int x, int y, int height){
        this.scrolling = true;

        this.options = new String[MAXOPS];
        this.numOptions = 0;

        this.active = false;
        this.x = x;
        this.y = y;
        currentOption = 0;

        name= null;

        //Took this code from online, presumably makes a new BitmapFont with the desired font file
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(FONTFILE));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 50;
        text = generator.generateFont(parameter);
        generator.dispose();
        text.getData().setScale(0.7f, 1f);

        cursorTex = new Texture(CURSOR);

        optionTex = new Texture[height];
        for (int i = 0; i < height; i++){
            String menuType;
            //make it diff it it's bottom, top or middle
            if (i == 0) {
                menuType = MENUTOP;
            }else if (i == height-1){
                menuType = MENUBOT;
            }else{
                menuType = MENUMID;
            }
            optionTex[i] = new Texture(menuType);
        }

    }



    public void loadNewOptions(String[] options){

        this.numOptions = options.length;
        for (int i = 0; i < numOptions; i++){
            this.options[i] = options[i];
        }

        if (!scrolling) {
            for (int i = 0; i < numOptions; i++) {
                String menuType;
                //make it diff it it's bottom, top or middle
                if (i == 0) {
                    menuType = MENUTOP;
                } else if (i == numOptions - 1) {
                    menuType = MENUBOT;
                } else {
                    menuType = MENUMID;
                }
                if (optionTex[i] != null) {
                    optionTex[i].dispose();
                }
                optionTex[i] = new Texture(menuType);
            }
        }

        currentOption = 0;


    }

    public void setName(String n){
        name = n;
    }
    public String getName(){ return name; }


    public void setActive(boolean a){
        active = a;
    }


    public boolean isActive(){ return active;}


    public String selectOption(){
        return options[currentOption];
    }

    public int selectOptionInt(){ return currentOption; }

    public void alterSelect(int alt){
        //if option would be out of range, don't alter


        if (currentOption + alt >= 0 && currentOption + alt < numOptions
                && currentOption != currentOption + alt) {
            currentOption += alt;
            SoundTextureManager.selectSound.stop();
            SoundTextureManager.selectSound.play();
        }

    }





    public void draw(SpriteBatch batch){
        if (active) {

            if (!scrolling) {
                for (int i = 0; i < numOptions; i++) {
                    batch.draw(optionTex[i], TILEDIM * x, TILEDIM * (y) - (i * TILEDIM));
                }

                for (int i = 0; i < numOptions; i++) {
                    text.draw(batch, options[i], (float) (TILEDIM * (x + 0.25)), (float) (TILEDIM * (y + 0.75) - ((i) * TILEDIM)));
                }

                //draw cursor
                batch.draw(cursorTex, (float) (TILEDIM * (x) - (TILEDIM * 0.75)), (TILEDIM * y) - (currentOption * TILEDIM));
            }
            else{
                for (int i = 0; i < optionTex.length; i++) {
                    batch.draw(optionTex[i], TILEDIM * x, TILEDIM * (y) - (i * TILEDIM));
                }

                for (int i = 0; i < optionTex.length; i++) {
                    if (i + currentOption < numOptions) {
                        String optionToDraw = options[i + currentOption];
                        text.draw(batch, optionToDraw, (float) (TILEDIM * (x + 0.25)), (float) (TILEDIM * (y + 0.75) - ((i) * TILEDIM)));
                    }
                }

                //draw cursor at top cuz im lazy (for now)
                batch.draw(cursorTex, (float) (TILEDIM * (x) - (TILEDIM * 0.75)), (TILEDIM * y));

            }
        }
    }


    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void setX(int y) {
        this.x = y;
    }
    public int getX() {
        return x;
    }


    public void dispose(){
        for (int i = 0; i < optionTex.length; i++){
            if (optionTex[i] != null){ optionTex[i].dispose(); }
        }
        cursorTex.dispose();
    }









}
