package com.firemarshalls.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.AtlasRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.codeandweb.physicseditor.PhysicsShapeCache;

import java.util.*;

import com.badlogic.gdx.Input.Keys;
import com.firemarshalls.entities.FireTruck;
import com.firemarshalls.entities.Goose;
import com.firemarshalls.entities.Player;


public class BaseBattle implements Screen {
	//variables that will be given to the screen
	public Game orgGame;
	public MapScreen returnScreen;
	public Integer[] locationOfGeese;
	private FireTruck truckObj;
	public FireTruck rewardTruck;
	public FireStation sendingStation;
	public float x;
	public float y;
	public boolean defeated;
	public Sprite sprite;
	private Player player;
	public int id;

	//variables for sprite management/viewports
	private SpriteBatch batch;
	private TextureAtlas textureAtlas;
	private OrthographicCamera camera;
	private FitViewport viewport;
	private World world;
	private Box2DDebugRenderer debugRenderer;
	private Body ground;
	private Body wallLeft;
	private Body wallRight;

	//managing variables for projectiles
	private Body[] bodiesList = new Body[15];
	private String[] names = new String[15];

	//ui variables
	private Sprite[] lives;
	private Sprite[] water;

	//variable that ensures only 1 hit can happen per projectile
	private boolean gotHit;

	public static PhysicsShapeCache physicsBodies;
	public static HashMap<String, Sprite> sprites;

	public static final float SCALE = 0.05f;
	public static float SHOT_SCALE = SCALE*12;

	public BaseBattle(int id, Game org, MapScreen screen, Player player, Integer[] loc, FireTruck rew, FireStation station, Float x, Float y) {
		this.id=id;
		this.locationOfGeese=loc;
		this.returnScreen = screen;
		this.sendingStation=station;
		this.rewardTruck=rew;
		this.player = player;
		this.truckObj = player.activeTruck;
		this.orgGame = org;
		this.x=x;
		this.y=y;
		this.defeated=false;
		this.sprite = MapScreen.mapSprites.get("alienBase");
	}
	private Goose[] gooseObjList;
	private Integer gooseNum;
	private Integer dyingGoose;
	public Integer deadGoose;

	// Everything that belongs to the LibGDX library must be defined within this function
	@Override
	public void show () {
		//physics stuff
		Box2D.init();
		debugRenderer = new Box2DDebugRenderer();
		debugRenderer.setDrawBodies(false);
		world = new World(new Vector2(0,-150),true); //note that y=-150 means gravity pulls down with a force of 150
		physicsBodies = new PhysicsShapeCache("physics.xml");

		//disposable objects
		batch = new SpriteBatch();
		textureAtlas = new TextureAtlas("sprites.txt");
        sprites = addSprites();

		//non-disposable objects
		camera = new OrthographicCamera();
		viewport = new FitViewport(120, 60, camera);

		// Draw the first truck body
		truckObj.drawBody(world, physicsBodies);

		// Draw the geese bodies
		gooseNum = locationOfGeese.length/2;
		gooseObjList = new Goose[gooseNum];
		for(int i = 0;i<gooseObjList.length;i++) {
			gooseObjList[i] = new Goose(locationOfGeese[i*2],locationOfGeese[(i*2)+1],i+4);
			if (gooseObjList[i].x < truckObj.x) {
				gooseObjList[i].right = false;
			}
			gooseObjList[i].drawBody(world, physicsBodies);
		}

		//life/water UI
		int hp = truckObj.maxHealth/10;
		lives = new Sprite[hp];
		for(int i = 0; i<hp;i++) {
			lives[i] = sprites.get("healthFull");
		}
		water = new Sprite[truckObj.maxWater/3];
		for(int i = 0; i<water.length;i++) {
			water[i] = sprites.get("tankFull");
		}
	}

	//Physics stuff for the world
	static final float STEP_TIME = 1f / 60f;
	static final int VELOCITY_ITERATIONS = 6;
	static final int POSITION_ITERATIONS = 2;
	float accumulator = 0;
	private void stepWorld() {
		float delta = Gdx.graphics.getDeltaTime();
		accumulator += Math.min(delta, 0.25f);
		if (accumulator >= STEP_TIME) {
			accumulator -= STEP_TIME;
			world.step(STEP_TIME, VELOCITY_ITERATIONS,POSITION_ITERATIONS);
		}
	}

	static int MOUSE_TIMER = 0;
	static List<Integer> scheduledToRemove = new ArrayList<>();
	@Override
	public void render (float speed) {
		//setup/background
		Gdx.gl.glClearColor(0.57f, 0.77f, 0.85f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stepWorld();

		batch.begin();

		for (int i = 0; i < bodiesList.length; i++) {
			if(bodiesList[i]!=null) {
				Body body = bodiesList[i];
				String name = names[i];

				if (name!=null) {
					Vector2 position = body.getPosition();
					float degrees = (float) Math.toDegrees(body.getAngle());
					drawSprite(name, position.x, position.y, degrees, SHOT_SCALE);
				}
			}
		}

		// draw the truck sprite on the body
		truckObj.body.setTransform(truckObj.x,0,0);
		Vector2 position = truckObj.body.getPosition();
		truckObj.sprite = drawSprite(truckObj.spriteName, position.x, position.y,0,1);

		//update the truck based on button inputs
		truckObj.locationUpdate(world, physicsBodies);

		// draw the goose sprite on the body
		for(int i = 0;i<gooseObjList.length;i++) {
			if (gooseObjList[i].dying && gooseObjList[i].alive) {
				if (gooseObjList[i].x > 119 || gooseObjList[i].y > 59) {
					deadGoose=i;
				}
				gooseObjList[i].x+=0.3;
				gooseObjList[i].y+=0.3;
			}
			if (gooseObjList[i].alive) {
				gooseObjList[i].body.setTransform(gooseObjList[i].x, gooseObjList[i].y, 0);
				Vector2 goosePosition = gooseObjList[i].body.getPosition();
				gooseObjList[i].sprite = drawSprite(gooseObjList[i].spriteName, goosePosition.x, goosePosition.y, 0, 1f);
			}
		}

		updateHealth(lives);
		updateWater(water);

		batch.end();

		//goose firing!
		for(int i = 0;i<gooseObjList.length;i++) {
			if (gooseObjList[i].shotTimer == 0 && gooseObjList[i].alive && !gooseObjList[i].dying) {
				Float[] calcs = gooseObjList[i].shotCalcs(truckObj);
				gooseShot(calcs, gooseObjList[i].x, gooseObjList[i].y);
				Random r = new Random();
				gooseObjList[i].shotTimer = r.nextInt(60) + 60;
			}
			else {
				gooseObjList[i].shotTimer -= 1;
			}
		}

		//countdown until the mouse can be clicked again
		if (MOUSE_TIMER!=0) MOUSE_TIMER-=1;

		// check left mouse button click
		if(Gdx.input.isTouched() && MOUSE_TIMER == 0 && truckObj.water>0){
			MOUSE_TIMER = truckObj.timer;
			Vector3 mousePos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(mousePos);
			ShapeRenderer sr = new ShapeRenderer();
			sr.setProjectionMatrix(camera.combined);

			float[] forces = truckObj.shotCalcs(mousePos);

			if (forces[4]==1) {
				addShot(forces);
				truckObj.water--;
			} else {
				Random r = new Random();
				for(int i = 0; i<forces[4]*2;i++) {
					forces[0] += - 10 + r.nextInt(20);
					forces[1] += - 10 + r.nextInt(20);
					addShot(forces);
					if(i%2==0) {truckObj.water--;};
				}
			}
		}

		if(Gdx.input.isKeyPressed(Input.Keys.X)) {
            this.orgGame.setScreen(returnScreen);
        }

		gotHit = false;

		//collision checks
		world.setContactListener(new ContactListener() {
			//mass ID is how we identify collision
			//floor/walls: Weight 0
			//water: Weight 1
			//truck: Weight 2
			//goo: Weight 3
			//goose: Weight 4+, each weight identifies a different goose
			@Override
			public void beginContact(Contact contact) {
				//water collides with goose
				if((contact.getFixtureB().getBody().getMass()==1 && contact.getFixtureA().getBody().getMass()>=4)){
					scheduleRemoveGoose((int)contact.getFixtureA().getBody().getMass());
					scheduleRemoveObject(contact.getFixtureB().getBody());
				}
				//goo collides with the environment
				if((contact.getFixtureB().getBody().getMass()==3 && contact.getFixtureA().getBody().getMass()==0)){
					scheduleRemoveObject(contact.getFixtureB().getBody());
				}
				if((contact.getFixtureB().getBody().getMass()==0 && contact.getFixtureA().getBody().getMass()==3)){
					scheduleRemoveObject(contact.getFixtureA().getBody());
				}
				//goo collides with the truck
				if((contact.getFixtureB().getBody().getMass()==3 && contact.getFixtureA().getBody().getMass()==2)){
					scheduleRemoveObject(contact.getFixtureB().getBody());
					if(!gotHit) truckObj.getHit();
					gotHit = true;
				}
			}
			@Override
			public void endContact(Contact contact) { }
			@Override
			public void preSolve(Contact contact, Manifold oldManifold) { }
			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) { }
		});

		debugRenderer.render(world, camera.combined);

		if (dyingGoose != null) {
			gooseObjList[dyingGoose-4].onCollision();
			dyingGoose = null;
		}

		if (deadGoose != null) {
			gooseObjList[deadGoose].alive = false;
			world.destroyBody(gooseObjList[deadGoose].body);
			deadGoose=null;
			gooseNum--;
			if (gooseNum==0){
				if(rewardTruck!=null) sendingStation.updateTrucks(rewardTruck);
				this.defeated=true;
				returnScreen.numOfBases--;
				this.orgGame.setScreen(returnScreen);
			}
		}

		for (int i : scheduledToRemove){
			if(bodiesList[i]!=null) world.destroyBody(bodiesList[i]);
			bodiesList[i] = null;
		}
		scheduledToRemove.clear();

		if (truckObj.health==0) {
			if (sendingStation.hasParked) {
				returnScreen.resetBase(id);
			}
			else {
				orgGame.setScreen(new MainMenu(orgGame));
			}
		}
	}

	private void updateHealth(Sprite[] lives) {
		int life = truckObj.health/10;
		int maxLife = lives.length;
		for(int i = 0; i<maxLife;i++) {
			lives[i].setX((i*5));
			lives[i].setY(53);
			if(i>=life){
				lives[i] = sprites.get("healthEmpty");
			}
			lives[i].draw(batch);
		}
	}

	private void updateWater(Sprite[] water) {
		int w = truckObj.water;
		int maxw = water.length;
		for(int i = 0; i<maxw;i++) {
			water[i].setX((i*5));
			water[i].setY(46);
			if(((i+1)*3)-1==w) {
				water[i] = sprites.get("tankEmpty1");
			} else if (((i+1)*3)-2==w) {
				water[i] = sprites.get("tankEmpty2");
			} else if (((i+1)*3)-3>=w) {
				water[i] = sprites.get("tankEmpty3");
			}
			water[i].draw(batch);
		}
        if(w==0) {
            Sprite runAway = sprites.get("runAway");
            runAway.setX(0);
            runAway.setY(46);
            runAway.draw(batch);
        }
	}

	public void scheduleRemoveObject(Body body) {
		int num = BODYDICT.get(body);
		if (bodiesList[num]!=null) {
			scheduledToRemove.add(num);
		}
	}

	public void scheduleRemoveGoose(int id) {
		dyingGoose = id;
	}

	//
	// These are the shot addition functions.
	//

	public static int CURRENT_ITR = 0;
	static HashMap<Body, Integer> BODYDICT = new HashMap<Body,Integer>();
	private void addShot(float[] forces) {
		float forceX = forces[0];
		float forceY = forces[1];
		float truckShootX = forces[2];
		float truckShootY = forces[3];

		//if the list of bodies is too long, remove the oldest one
		if(bodiesList[bodiesList.length-1]!=null){
			if (bodiesList[CURRENT_ITR]!=null) {
				bodiesList[CURRENT_ITR].setActive(false);
				world.destroyBody(bodiesList[CURRENT_ITR]);
			}

			bodiesList[CURRENT_ITR]= createBody("waterBall", truckShootX, truckShootY, 0, forceX,forceY,SHOT_SCALE, 1);
			names[CURRENT_ITR] = "waterBall";
			BODYDICT.put(bodiesList[CURRENT_ITR], CURRENT_ITR);

			CURRENT_ITR += 1;
			if(CURRENT_ITR >= bodiesList.length) {
				CURRENT_ITR = 0;
			}
		}

		for (int i = 0; i < bodiesList.length; i++) {
			if (bodiesList[i]==null) {
				bodiesList[i] = createBody("waterBall", truckShootX, truckShootY, 0,forceX,forceY, SHOT_SCALE, 1);
				names[i] = "waterBall";
				BODYDICT.put(bodiesList[i], i);
				break;
			}
		}
	}

	private void gooseShot(Float[] forces, Float gooseX, Float gooseY) {
		float forceX = forces[0];
		float forceY = forces[1];
		System.out.println(forceY);

		//if the list of bodies is too long, remove the oldest one
		if(bodiesList[bodiesList.length-1]!=null){
			if (bodiesList[CURRENT_ITR]!=null) {
				bodiesList[CURRENT_ITR].setActive(false);
				world.destroyBody(bodiesList[CURRENT_ITR]);
			}

			bodiesList[CURRENT_ITR]= createBody("gooBall", gooseX, gooseY+(40*SCALE), 0, forceX,forceY,SHOT_SCALE, 3);
			names[CURRENT_ITR] = "gooBall";
			BODYDICT.put(bodiesList[CURRENT_ITR], CURRENT_ITR);

			CURRENT_ITR += 1;
			if(CURRENT_ITR >= bodiesList.length) {
				CURRENT_ITR = 0;
			}
		}

		for (int i = 0; i < bodiesList.length; i++) {
			if (bodiesList[i]==null) {
				bodiesList[i] = createBody("gooBall", gooseX, gooseY+(40*SCALE), 0,forceX,forceY, SHOT_SCALE, 3);
				names[i] = "gooBall";
				BODYDICT.put(bodiesList[i], i);
				break;
			}
		}
	}

	//
	// From now on, these are not commonly referenced or utilized, they're backend sprite/body creation functions
	//

	private Body createBody(String name, float x, float y, float rotation, float forceX, float forceY, float scale, float mass) {
		Body body = physicsBodies.createBody(name, world, SCALE*scale, SCALE*scale);
		body.setTransform(x,y,rotation);

		MassData md = new MassData();
		md.mass = mass;
		body.setMassData(md);

		body.setLinearVelocity(forceX,forceY);

		return body;
	}

	private void createBox() {
		if (ground != null) {
			world.destroyBody(ground);
		}
		if (wallLeft != null) {
			world.destroyBody(wallLeft);
		}
		if (wallRight != null) {
			world.destroyBody(wallRight);
		}

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.StaticBody;

		//ground
		FixtureDef groundFixtureDef = new FixtureDef();
		groundFixtureDef.friction = 0.1f;
		PolygonShape groundShape = new PolygonShape();
		groundShape.setAsBox(camera.viewportWidth,1);
		groundFixtureDef.shape = groundShape;
		ground = world.createBody(bodyDef);
		ground.createFixture(groundFixtureDef);
		ground.setTransform(0,0,0);

		//wallleft
		FixtureDef wallLeftFixtureDef = new FixtureDef();
		wallLeftFixtureDef.friction = 1;
		PolygonShape wallLeftShape = new PolygonShape();
		wallLeftShape.setAsBox(0,camera.viewportHeight);
		wallLeftFixtureDef.shape = wallLeftShape;
		wallLeft = world.createBody(bodyDef);
		wallLeft.createFixture(wallLeftFixtureDef);
		wallLeft.setTransform(0,0,0);

		//wallright
		FixtureDef wallRightFixtureDef = new FixtureDef();
		wallRightFixtureDef.friction = 1;
		PolygonShape wallRightShape = new PolygonShape();
		wallRightShape.setAsBox(0,camera.viewportHeight);
		wallRightFixtureDef.shape = wallRightShape;
		wallRight = world.createBody(bodyDef);
		wallRight.createFixture(wallRightFixtureDef);
		wallRight.setTransform(camera.viewportWidth,0,0);

		groundShape.dispose();
		wallLeftShape.dispose();
		wallRightShape.dispose();
	}

	private HashMap<String, Sprite> addSprites() {
		Array<AtlasRegion> regions = textureAtlas.getRegions();
		HashMap<String,Sprite> tempSprites = new HashMap<String,Sprite>();

		for (AtlasRegion region : regions) {
			Sprite sprite = textureAtlas.createSprite(region.name);

			float width = sprite.getWidth() * SCALE;
			float height = sprite.getHeight() * SCALE;

			sprite.setSize(width, height);
			sprite.setOrigin(0,0);

			tempSprites.put(region.name, sprite);
		}
		return tempSprites;
	}

	public Sprite drawSprite(String name, float x, float y, float rotation, float scale) {
		Sprite sprite = sprites.get(name);

		sprite.setPosition(x, y);
		sprite.setRotation(rotation);
		sprite.setOrigin(0f,0f);
		sprite.setScale(scale);

		sprite.draw(batch);
		return sprite;
	}

	public void updateParameters(Game org, MapScreen screen, Player player) {
		this.orgGame = org;
		this.returnScreen=screen;
		this.truckObj=player.activeTruck;
	}

	public void resize (int width, int height) {
		viewport.update(width, height, true);

		batch.setProjectionMatrix(camera.combined);
		truckObj.boxSize = 60*(SCALE/0.1f);

		createBox();
	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void hide() {

	}

	// This is important to reduce lag, clears out unused variables
	@Override
	public void dispose () {
		batch.dispose();
		textureAtlas.dispose();
		world.dispose();
		debugRenderer.dispose();
	}

	public void draw(Batch batch) {
		if (!defeated) {
			this.sprite.setX(this.x);
			this.sprite.setY(this.y);
			this.sprite.draw(batch);
		}
	}
}
