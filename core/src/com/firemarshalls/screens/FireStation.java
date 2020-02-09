package com.firemarshalls.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.firemarshalls.entities.FireTruck;

import java.util.HashMap;

public class FireStation implements Screen {
    private Game orgGame;
    private MapScreen mapScreen;
    private SpriteBatch batch;
    private Stage stage;
    private float height;
    private float width;
    public boolean underAttack = false;
    public FireTruck[] trucks;
    public HashMap<String,Sprite> mapSprites;
    public Sprite redSprite;
    public Sprite blueSprite;
    public Sprite greenSprite;
    public boolean hasParked;
    public float x;
    public float y;

    public FireStation(Game g, MapScreen s, HashMap<String,Sprite> mapSprites) {
        orgGame=g;
        mapScreen=s;
        this.mapSprites=mapSprites;

        stage = new Stage();
        Gdx.input.setInputProcessor(stage);
        height = stage.getHeight();
        width = stage.getWidth();
        trucks = new FireTruck[3];
        batch = new SpriteBatch();

        x = 940;
        y = 1250;
    }


    @Override
    public void show() {
        Skin skin = new Skin(Gdx.files.internal("default/skin/uiskin.json"));

        TextButton mainButton = new TextButton("Welcome to the fire station, where you can pick a truck!", skin);
        mainButton.setPosition(width*3/10,height*8/10);
        mainButton.setSize(width*4/10,height*1/10);
        redSprite = mapSprites.get("redR");
        blueSprite = mapSprites.get("blueR");
        greenSprite = mapSprites.get("greenR");

        stage.addActor(mainButton);
        stage.addActor(truckButton1(skin));
        stage.addActor(truckButton2(skin));
        stage.addActor(truckButton3(skin));
    }

    private TextButton truckButton1(Skin skin) {
        TextButton button = new TextButton("Not unlocked yet...", skin);
        button.setPosition(width*3/10,height*5.5f/10);
        button.setSize(width*4/10,height*1/10);

        if(trucks[0]!=null) {
            if(trucks[0].name=="red") {
                button.setText("The classic red vehicle. Everyone loves it.\nAlways gets the job done.");
                button.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent e, float x, float y) {
                        mapScreen.setActiveTruck(trucks[0]);
                        trucks[0] = new FireTruck("p", 10, 3, 1); //placeholder truck
                        if((trucks[1]==null||trucks[1].name=="p")&&(trucks[2]==null||trucks[2].name=="p")) hasParked = false;
                        orgGame.setScreen(mapScreen);
                    }
                });
            }
            if(trucks[0].name=="p") {
                button.setText("This is where the red car used to be parked.\nDon't you miss it?");
            }
        }
        return button;
    }

    private TextButton truckButton2(Skin skin) {
        TextButton button = new TextButton("Not unlocked yet...", skin);
        button.setPosition(width*3/10,height*4/10);
        button.setSize(width*4/10,height*1/10);

        if(trucks[1]!=null) {
            if(trucks[1].name=="blue") {
                button.setText("The blue truck is fast, but weaker to alien goo.\nStay on your toes to dodge enemy attacks!");
                button.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent e, float x, float y) {
                        mapScreen.setActiveTruck(trucks[1]);
                        trucks[1] = new FireTruck("p", 10, 3, 1); //placeholder truck
                        if((trucks[0]==null||trucks[0].name=="p")&&(trucks[2]==null||trucks[2].name=="p")) hasParked = false;
                        orgGame.setScreen(mapScreen);
                    }
                });
            }
            if(trucks[1].name=="p") {
                button.setText("This is where the blue truck used to be parked.\nDon't you miss it?");
            }
        }
        return button;
    }

    private TextButton truckButton3(Skin skin) {
        TextButton button = new TextButton("Not unlocked yet...", skin);
        button.setPosition(width*3/10,height*2.5f/10);
        button.setSize(width*4/10,height*1/10);
        System.out.println(trucks[0]);

        if(trucks[2]!=null) {
            if(trucks[2].name=="green") {
                button.setText("The green truck has lots of firepower, and wipes away corrupted geese.\nBe careful not to run out of water, though!");
                button.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent e, float x, float y) {
                        mapScreen.setActiveTruck(trucks[2]);
                        trucks[2] = new FireTruck("p", 10, 3, 1); //placeholder truck
                        if((trucks[0]==null||trucks[0].name=="p")&&(trucks[1]==null||trucks[1].name=="p")) hasParked = false;
                        orgGame.setScreen(mapScreen);
                    }
                });
            }
            if(trucks[2].name=="p") {
                button.setText("This is where the green car used to be parked.\nDon't you miss it?");
            }
        }
        return button;
    }

    public void updateTrucks(FireTruck newTruck) {
        if (newTruck.name=="red") {
            this.trucks[0] = newTruck;
        }
        if (newTruck.name=="blue") {
            this.trucks[1] = newTruck;
        }
        if (newTruck.name=="green") {
            this.trucks[2] = newTruck;
        }
        if (!underAttack) {
            newTruck.refresh();
        }
        hasParked=true;
        System.out.println(trucks[0]);
    }


    @Override
    public void render(float delta) {
        //Gdx.gl.glClearColor(0, 0, 0,0);
        //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();

        batch.begin();
        Sprite spr;
        for(int i = 0; i<3; i++) {
            if(trucks[i]!=null) {
                if (trucks[i].name!="p") {
                    spr = mapSprites.get(trucks[i].name + "R");
                    spr.setPosition(width*2/10,height*2.5f/10 + ((2-i)*height*1.5f/10));
                    spr.setOrigin(0f,0f);
                    spr.draw(batch);
                }
            }
        }
        batch.end();
    }

    @Override
    public void resize(int i, int i1) {

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

    @Override
    public void dispose() {
        batch.dispose();
    }

    public void draw(Batch batch) {
        Sprite spr = MapScreen.mapSprites.get("fireStat");
        spr.setX(this.x);
        spr.setY(this.y);
        spr.draw(batch);
    }
}
