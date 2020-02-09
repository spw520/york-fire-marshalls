package com.firemarshalls.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.World;
import com.codeandweb.physicseditor.PhysicsShapeCache;
import com.firemarshalls.screens.BaseBattle;

import java.util.Random;

public class FireTruck {
    public String name;
    private String spriteR;
    private String spriteL;
    public String spriteName;
    public String bodyR;
    public String bodyL;
    public Body body;
    public float boxSize;
    public String fireMode;
    public int maxHealth;
    public int maxWater;
    public float speed;
    public Sprite sprite;
    public int timer;

    public int water;
    public int health;
    public float x;                 //the current location of the truck in the minigame=
    public boolean right;           //true means the truck faces right, false means it faces left

    public FireTruck(String name, int maxHealth, int maxWater, int speed) {
        this.name = name;
        this.x = 0;

        //this will all be replaced by looking up in dictionary instead of this static behaviour
        this.spriteR = this.name + "R";
        this.spriteL = this.name + "L";
        this.spriteName = this.spriteR;
        this.bodyR = "truckR";
        this.bodyL = "truckL";
        this.speed = speed * (BaseBattle.SCALE/0.1f);
        this.right = true;
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.maxWater = maxWater;
        this.water = maxWater;
        this.timer = 60;
    }

    public void getHit() {
        this.health -= 10;
        System.out.println("Oh no you took damage, you are now at " + this.health + " life.");
        if (this.health == 0) {
            System.out.println("Big Death Effect");
        }
    }

    public void moveBody(World world, PhysicsShapeCache physicsBodies, boolean right) {
        boolean prevRight = this.right;
        this.right = right;

        //move the truck relative to its position
        if (right) {
            this.x += Gdx.graphics.getDeltaTime() * speed;
            if (this.x + this.sprite.getWidth() > this.boxSize){
                this.x = this.boxSize-this.sprite.getWidth();
            }
            this.spriteName = this.spriteR;
        }
        else {
            this.x -= Gdx.graphics.getDeltaTime() * speed;
            if (this.x < 0){
                this.x = 0;
            }
            this.spriteName = this.spriteL;
        }

        //redraw body if the truck flipped
        if (prevRight!=this.right) {
            world.destroyBody(this.body);
            this.drawBody(world, physicsBodies);
        }
    }

    public Body drawBody(World world, PhysicsShapeCache physicsBodies) {
        String b;
        if (right)  { b = this.bodyR;}
        else { b = this.bodyL;}

        this.body = physicsBodies.createBody(b, world, BaseBattle.SCALE, BaseBattle.SCALE);

        MassData md = new MassData();
        md.mass=2;
        body.setMassData(md);

        return this.body;
    }

    public float[] shotCalcs(Vector3 mousePos){
        float truckShootX = this.sprite.getX() + this.sprite.getWidth()/2;
        float truckShootY = this.sprite.getY() + this.sprite.getHeight();
        Vector2 mouseVec = new Vector2(mousePos.x,mousePos.y);

        float[] stats = new float[5];

        int numOfShots=1;
        if (name == "green") {
            Random r = new Random();
            numOfShots = r.nextInt(2)+2;
        }

        stats[0] = (mouseVec.x-truckShootX);
        stats[1] = ((mouseVec.y-truckShootY-3)/40)*120;
        stats[2] = truckShootX;
        stats[3] = truckShootY;
        stats[4] = numOfShots;
        return(stats);
    }


    public void locationUpdate(World world, PhysicsShapeCache physicsBodies) {
        //move the x value of the truck based on button inputs
        if(Gdx.input.isKeyPressed(Input.Keys.DPAD_LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            this.moveBody(world, physicsBodies,false); }
        if(Gdx.input.isKeyPressed(Input.Keys.DPAD_RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            this.moveBody(world, physicsBodies,true); }
    }

    public void refresh() {
        this.health=this.maxHealth;
        this.water=this.maxWater;
    }
}
