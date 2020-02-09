package com.firemarshalls.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.firemarshalls.screens.MapScreen;

public class Player {

    //Vector2 holds an x and a y value, used for sprite coordinates
    //the movement velocity of the sprite
    private Vector2 velocity = new Vector2();


    private String blockedKey = "blocked";
    public FireTruck activeTruck;
    private float speed = 300f, increment;
    public Sprite sprite;
    public String spriteL;
    public String spriteR;
    public String spriteC;
    public Float x;
    public Float y;

    private TiledMapTileLayer collisionLayer;

    //constructor for the player
    public Player(FireTruck truck, TiledMapTileLayer collisionLayer) {
        this.activeTruck = truck;
        this.collisionLayer = collisionLayer;
        this.spriteL = activeTruck.name + "L";
        this.spriteR = activeTruck.name + "R";
        this.spriteC = this.spriteR;
        this.sprite = MapScreen.mapSprites.get(this.spriteC);
    }

    //calculate the next position of the sprite based on velocity and current pos
    public void update(float delta) {
        this.spriteL = activeTruck.name + "L";
        this.spriteR = activeTruck.name + "R";
        //clamp velocity to a max value
        if (velocity.y > speed)
            velocity.y = speed;
        else if (velocity.y < -speed)
            velocity.y = -speed;

        if(Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.DPAD_UP)) {
            velocity.y = speed;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.DPAD_LEFT)) {
            this.spriteC = this.spriteL;
            velocity.x = -speed;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DPAD_DOWN)) {
            velocity.y = -speed;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.DPAD_RIGHT)){
            this.spriteC = this.spriteR;
            velocity.x = speed;
        }

        //save old position
        float oldX = x, oldY = y;
        boolean collisionX = false, collisionY = false;

        // move on x
        x += velocity.x * delta;

        // calculate the increment for step in #collidesLeft() and #collidesRight()
        increment = collisionLayer.getTileWidth();
        increment = sprite.getWidth() < increment ? sprite.getWidth() / 2 : increment / 2;

        if(velocity.x < 0) // going left
            collisionX = collidesLeft();
        else if(velocity.x > 0) // going right
            collisionX = collidesRight();

        // react to x collision
        if(collisionX) {
            x=oldX;
        }

        // move on y
        y += velocity.y * delta;

        // calculate the increment for step in #collidesBottom() and #collidesTop()
        increment = collisionLayer.getTileHeight();
        increment = sprite.getHeight() < increment ? sprite.getHeight() / 2 : increment / 2;

        if(velocity.y < 0) // going down
            collisionY = collidesBottom();
        else if(velocity.y > 0) // going up
            collisionY = collidesTop();

        // react to y collision
        if(collisionY) {
            y=oldY;
        }
        velocity.x = 0;
        velocity.y = 0;
    }


    private boolean isCellBlocked(float x, float y) {
        TiledMapTileLayer.Cell cell = collisionLayer.getCell((int) (x / collisionLayer.getTileWidth()) / 3, (int) (y / collisionLayer.getTileHeight() / 3));
        return cell != null && cell.getTile() != null && cell.getTile().getProperties().containsKey(blockedKey);
    }

    public boolean collidesRight() {
        for(float step = 0; step <= sprite.getHeight(); step += increment)
            if(isCellBlocked(x + sprite.getWidth(), y + step))
                return true;
        return false;
    }

    public boolean collidesLeft() {
        for(float step = 0; step <= sprite.getHeight(); step += increment)
            if(isCellBlocked(x, y + step))
                return true;
        return false;
    }

    public boolean collidesTop() {
        for(float step = 0; step <= sprite.getWidth(); step += increment)
            if(isCellBlocked(x + step, y + sprite.getHeight()))
                return true;
        return false;

    }

    public boolean collidesBottom() {
        for(float step = 0; step <= sprite.getWidth(); step += increment)
            if(isCellBlocked(x + step, y))
                return true;
        return false;
    }

    public void setPosition(Float x, Float y) {
        this.x = x;
        this.y = y;
    }

    public Float getX(){
        return this.x;
    }

    public Float getY(){
        return this.y;
    }

    public boolean doesCollideWith(float x, float y) {
        if (this.x<x && this.x+this.sprite.getWidth()>x&&this.y<y&&this.y+this.sprite.getHeight()>y) {
            return true;
        }
        return false;
    }

    public void draw(Batch batch) {
        this.sprite = MapScreen.mapSprites.get(this.spriteC);
        this.sprite.setPosition(x,y);
        this.sprite.setSize(collisionLayer.getWidth() * 2, collisionLayer.getHeight() * 3);
        this.sprite.setOrigin(0f,0f);
        this.sprite.draw(batch);
    }

    public TiledMapTileLayer getCollisionLayer(){
        return collisionLayer;
    }
}
