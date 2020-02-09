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
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.Array;
import com.firemarshalls.entities.FireTruck;
import com.firemarshalls.entities.Player;

import java.util.HashMap;

public class MapScreen implements Screen {
    private boolean first = true;
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;

    private SpriteBatch spriteBatch;
    private TextureAtlas textureAtlas;
    public static HashMap<String, Sprite> mapSprites;
    private FireStation fireStation;

    private Player player;
    private Game orgGame;
    public int numOfBases;

    //ui variables
    private Sprite[] lives;
    private Sprite[] water;
    private Sprite stationBubble;
    private Sprite alienBubble;

    public BaseBattle railStationBattle;
    public BaseBattle ouseBridgeBattle;
    public BaseBattle yorkMinsterBattle;

    public float SCALE = 0.5f;

    public MapScreen(Game game) {
        orgGame = game;
        numOfBases=3;
    }

    @Override
    public void show() {
        // create a loader for the map and load the correct map file
        TmxMapLoader loader = new TmxMapLoader();
        map = loader.load("map V2.tmx");

        // renders the map, the number represents the scaling of the map
        renderer = new OrthogonalTiledMapRenderer(map, 3);
        camera = new OrthographicCamera();
        textureAtlas = new TextureAtlas("mapsprites.txt");
        spriteBatch = new SpriteBatch();
        mapSprites = addSprites();
        if (first == true) {
            fireStation = new FireStation(orgGame, this, mapSprites);
            this.player = new Player(new FireTruck("red", 50, 21, 50), (TiledMapTileLayer) map.getLayers().get(0));
            player.setPosition(9 * player.getCollisionLayer().getTileWidth() * 3, (player.getCollisionLayer().getHeight() - 38) * player.getCollisionLayer().getTileHeight() * 3);
            railStationBattle = new BaseBattle(1, orgGame, this, player, generateRailStationGeese(), new FireTruck("blue", 30, 21, 80), fireStation, 1330f, 2670f);
            yorkMinsterBattle = new BaseBattle(3, orgGame, this, player, generateYorkMinsterGeese(), null, fireStation, 3240f, 2440f);
            ouseBridgeBattle = new BaseBattle(2, orgGame, this, player, generateOuseBridgeGeese(), new FireTruck("green", 50, 30, 30), fireStation, 1650f, 3400f);
        }
        first = false;

        //life/water UI
        int hp = player.activeTruck.maxHealth / 10;
        lives = new Sprite[hp];
        for (int i = 0; i < hp; i++) {
            lives[i] = mapSprites.get("healthFull");
        }
        water = new Sprite[player.activeTruck.maxWater / 3];
        for (int i = 0; i < water.length; i++) {
            water[i] = mapSprites.get("tankFull");
        }
        stationBubble = mapSprites.get("station");
        alienBubble = mapSprites.get("aliens");
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.position.set(player.getX() + player.sprite.getWidth() / 2, player.getY() + player.sprite.getHeight() / 2, 0);
        camera.update();

        renderer.setView(camera);
        renderer.render();

        renderer.getBatch().begin();

        // the alien bases
        if (!railStationBattle.defeated) railStationBattle.draw(renderer.getBatch());
        if (!ouseBridgeBattle.defeated) ouseBridgeBattle.draw(renderer.getBatch());
        if (!yorkMinsterBattle.defeated) yorkMinsterBattle.draw(renderer.getBatch());

        fireStation.draw(renderer.getBatch());

        player.update(delta);
        player.draw(renderer.getBatch());

        renderer.getBatch().end();

        spriteBatch.begin();
        updateHealth(lives, spriteBatch);
        updateWater(water, spriteBatch);
        if (player.doesCollideWith(fireStation.x, fireStation.y)) {
            stationBubble.setX(camera.viewportWidth / 2);
            stationBubble.setY(camera.viewportHeight / 10);
            stationBubble.draw(spriteBatch);
        }
        if (    (player.doesCollideWith(railStationBattle.x, railStationBattle.y) && !railStationBattle.defeated) ||
                (player.doesCollideWith(ouseBridgeBattle.x, ouseBridgeBattle.y) && !ouseBridgeBattle.defeated) ||
                (player.doesCollideWith(yorkMinsterBattle.x, yorkMinsterBattle.y)) && !yorkMinsterBattle.defeated) {
            alienBubble.setX(camera.viewportWidth / 2);
            alienBubble.setY(camera.viewportHeight / 10);
            alienBubble.draw(spriteBatch);
        }
        spriteBatch.end();

        if (Gdx.input.isKeyPressed(Input.Keys.B) && player.doesCollideWith(railStationBattle.x, railStationBattle.y) && !railStationBattle.defeated) {
            railStationBattle.updateParameters(orgGame, this, player);
            orgGame.setScreen(railStationBattle);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.B) && player.doesCollideWith(ouseBridgeBattle.x, ouseBridgeBattle.y)&& !ouseBridgeBattle.defeated) {
            ouseBridgeBattle.updateParameters(orgGame,this,player);
            orgGame.setScreen(ouseBridgeBattle);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.B) && player.doesCollideWith(yorkMinsterBattle.x, yorkMinsterBattle.y) && !yorkMinsterBattle.defeated) {
            yorkMinsterBattle.updateParameters(orgGame, this,player);
            orgGame.setScreen(yorkMinsterBattle);
        }

        if (Gdx.input.isKeyPressed(Input.Keys.F) && player.doesCollideWith(fireStation.x, fireStation.y)) {
            fireStation.updateTrucks(player.activeTruck);
            player.activeTruck = null;
            orgGame.setScreen(fireStation);
        }

        if (numOfBases==0) {
            orgGame.setScreen(new MainMenu(orgGame));
        }
    }

    public void resetBase(int id) {
        player.setPosition(9 * player.getCollisionLayer().getTileWidth() * 3, (player.getCollisionLayer().getHeight() - 38) * player.getCollisionLayer().getTileHeight() * 3);
        orgGame.setScreen(this);
        if(id == 1) {
            railStationBattle.dispose();
            railStationBattle = new BaseBattle(1, orgGame, this, player, generateRailStationGeese(), new FireTruck("blue", 30, 21, 80), fireStation, 1250f, 2670f);
        }
        if(id == 3) {
            yorkMinsterBattle.dispose();
            yorkMinsterBattle = new BaseBattle(3, orgGame, this, player, generateYorkMinsterGeese(), null, fireStation, 3240f, 2440f);
        }
        if(id == 2) {
            ouseBridgeBattle.dispose();
            ouseBridgeBattle = new BaseBattle(2, orgGame, this, player, generateOuseBridgeGeese(), new FireTruck("green", 50, 30, 30), fireStation, 1650f, 3400f);
        }
        orgGame.setScreen(fireStation);
    }

    public void setActiveTruck(FireTruck truck) {
        player.activeTruck = truck;
    }

    private void updateHealth(Sprite[] lives, Batch cbatch) {
        int life = player.activeTruck.health / 10;
        int maxLife = lives.length;
        for (int i = 0; i < maxLife; i++) {
            lives[i].setX(i * (camera.viewportWidth / 50));
            lives[i].setY(camera.viewportHeight * 9 / 10);
            if (i >= life) {
                lives[i] = mapSprites.get("healthEmpty");
            }
            lives[i].draw(cbatch);
        }
    }

    private void updateWater(Sprite[] water, Batch cbatch) {
        int w = player.activeTruck.water;
        int maxw = water.length;
        for (int i = 0; i < maxw; i++) {
            water[i].setX(i * (camera.viewportWidth / 35));
            water[i].setY(camera.viewportHeight * 85 / 100);
            if (((i + 1) * 3) - 1 == w) {
                water[i] = mapSprites.get("tankEmpty1");
            } else if (((i + 1) * 3) - 2 == w) {
                water[i] = mapSprites.get("tankEmpty2");
            } else if (((i + 1) * 3) - 3 >= w) {
                water[i] = mapSprites.get("tankEmpty3");
            }
            water[i].draw(cbatch);
        }
        if (w == 0) {
            Sprite runAway = mapSprites.get("refuel");
            runAway.setX(0);
            runAway.setY(camera.viewportHeight * 85 / 100);
            runAway.draw(cbatch);
        }
    }

    @Override
    // resize the game window to fit the size of the users window
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        camera.update();
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    private HashMap<String, Sprite> addSprites() {
        Array<TextureAtlas.AtlasRegion> regions = textureAtlas.getRegions();
        HashMap<String, Sprite> tempSprites = new HashMap<String, Sprite>();

        for (TextureAtlas.AtlasRegion region : regions) {
            Sprite sprite = textureAtlas.createSprite(region.name);

            float width = sprite.getWidth() * SCALE;
            float height = sprite.getHeight() * SCALE;

            sprite.setSize(width, height);
            sprite.setOrigin(0, 0);

            tempSprites.put(region.name, sprite);
        }
        return tempSprites;
    }

    private Integer[] generateRailStationGeese() {
        Integer[] locationOfGeese = new Integer[6];
        locationOfGeese[0] = 70;
        locationOfGeese[1] = 0;
        locationOfGeese[2] = 100;
        locationOfGeese[3] = 20;
        locationOfGeese[4] = 100;
        locationOfGeese[5] = 0;
        return locationOfGeese;
    }

    private Integer[] generateOuseBridgeGeese() {
        Integer[] locationOfGeese = new Integer[8];
        locationOfGeese[0]=50;
        locationOfGeese[1]=0;
        locationOfGeese[2]=65;
        locationOfGeese[3]=0;
        locationOfGeese[4]=75;
        locationOfGeese[5]=0;
        locationOfGeese[6]=90;
        locationOfGeese[7]=0;
        return locationOfGeese;
    }

    private Integer[] generateYorkMinsterGeese() {
        Integer[] locationOfGeese = new Integer[10];
        locationOfGeese[0] = 70;
        locationOfGeese[1] = 0;
        locationOfGeese[2] = 100;
        locationOfGeese[3] = 20;
        locationOfGeese[4] = 100;
        locationOfGeese[5] = 0;
        locationOfGeese[6] = 80;
        locationOfGeese[7] = 30;
        locationOfGeese[8] = 90;
        locationOfGeese[9] = 30;
        return locationOfGeese;
    }

    //this section clears the assets on each frame so they can be reloaded in the right place
    @Override
    public void dispose() {
        map.dispose();
        renderer.dispose();
        player.sprite.getTexture().dispose();
    }
}
