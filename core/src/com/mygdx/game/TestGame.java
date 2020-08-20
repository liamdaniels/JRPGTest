package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;


public class TestGame extends ApplicationAdapter {


	/**
	 * Static things for switching between modes.
	 * Subject to change for sure
	 */
	public static String upcomingMode = "";
	//for loading battles. Should be changed when it's time for battle mode
	public static int[] upcomingEnemies = new int[0];

	//for loading shops. Should be changed when time for shop mode
	public static int shopType;



	/** Event flags
	 *
	 * event map (subject to change):
	 * 0-25 character dissappearing
	 * 25-75 character text change
	 * 75-100 big game events
	 *
	 * TODO: expand on this
	 *
	 *
	 * */
	public static boolean[] eventFlags = new boolean[200];


	SpriteBatch batch;

	/** Player mode for exploring and walking */
	WorldMode world;
	/** Battle mode for.. battle */
	BattleMode battle;
	/** mode for menu */
	MenuMode menu;
	/** mode for shoppin*/
	ShopMode shop;


	/** managers of players, enemies, items, equips, etc*/
	PlayerManager playerManager;
	EnemyManager enemyManager;
	ItemManager itemManager;
	SpellManager spellManager;


	/** Polymorphic reference to the active player mode */
	ModeController controller;

	InputController input;




	private static final int TILESWIDTH = 17;
	private static final int TILESHEIGHT = 13;

	private static final int TILEDIM = 64;

	private static final int WIDTH = TILEDIM*TILESWIDTH;
	private static final int HEIGHT = TILEDIM*TILESHEIGHT;



	@Override
	public void create () {
		// must make spritebatch
		batch = new SpriteBatch();


		// this changes window size
		Gdx.graphics.setWindowedMode(WIDTH, HEIGHT);
		// this line literally changes the spritebatch size
		batch.getProjectionMatrix().setToOrtho2D(0, 0, WIDTH, HEIGHT);

		SoundTextureManager.loadAll();

		input = new InputController();

		itemManager = new ItemManager();

		itemManager.addItem(0);
		itemManager.addItem(0);
		itemManager.addItem(0);
		for(int i = 0; i < 2; i++){
			itemManager.addItem(1);
		}
		itemManager.addItem(2);
		itemManager.addItem(3);
		itemManager.addItem(4);
		itemManager.addItem(5);


		spellManager = new SpellManager();
		enemyManager = new EnemyManager();

		playerManager = new PlayerManager(itemManager,spellManager);

		//for now
		playerManager.addMember(0);
		playerManager.addMember(2);
		playerManager.addMember(1);

		playerManager.getParty().get(0).addSpell(0);
		playerManager.getParty().get(0).addSpell(1);
		playerManager.getParty().get(0).addSpell(2);
		playerManager.getParty().get(1).addSpell(0);
		playerManager.getParty().get(1).addSpell(3);
		playerManager.getParty().get(2).addSpell(1);




		world = new WorldMode(itemManager);
		battle = new BattleMode();
		menu = new MenuMode(playerManager, itemManager, spellManager);
		shop = new ShopMode(itemManager);


		controller = world;




	}


	@Override
	public void render () {


		//------update-------
		if (!upcomingMode.equals("") && BattleAnimator.halfwayDone()){
			//this means it's time to change modes!

			switch(upcomingMode){
				case "battle":
					controller = battle;
					battle.load(playerManager,enemyManager.calcEnemies(upcomingEnemies), itemManager, spellManager);
					break;
				case "world":
					controller = world;
					SoundTextureManager.playNewBgSong(world.getCurrentMapSong());
					break;
				case "menu":
					for (int i = 0; i < playerManager.getParty().size(); i++ ){
						playerManager.getParty().get(i).setAnimation(5,-1);
					}
					controller = menu;
					break;
				case "shop":
					shop.loadShop(shopType);
					controller = shop;
					break;
				default:
					//uh oh this better not happen. Just do nothing
					System.out.println("Tried to change modes to " + upcomingMode + "?");
					break;
			}

			//reset back to empty string
			upcomingMode = "";
		}else if (!upcomingMode.equals("") && !BattleAnimator.animating()){
			BattleAnimator.loadAnimation(0); //default transition animation
		}else if(upcomingMode.equals("")) {

			controller.update(input);

		}
		BattleAnimator.updateAnimation();


		//------draw-------


		batch.begin();
		controller.draw(batch);
		BattleAnimator.drawAnimation(batch);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
		battle.dispose();
		world.dispose();
		menu.dispose();
		shop.dispose();
		playerManager.dispose();
		enemyManager.dispose();
		SoundTextureManager.dispose();
		batch = null;
		input = null;


	}


	/*
		turns number into pixel for grid
	 */
	private int decW(double num){
		return (int)(WIDTH*num);
	}

	/*
		turns number into pixel for grid
	 */
	private int decH(double num){
		return (int)(HEIGHT*num);
	}




}
