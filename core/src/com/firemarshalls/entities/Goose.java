package com.firemarshalls.entities;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.World;
import com.codeandweb.physicseditor.PhysicsShapeCache;
import com.firemarshalls.entities.FireTruck;
import com.firemarshalls.screens.BaseBattle;

import java.util.Random;

public class Goose {
    public String name;
    private String spriteR;
    private String spriteL;
    private String spriteDead;
    public String spriteName;
    public String bodyR;
    public String bodyL;
    public String bodyDead;
    public Body body;
    public Sprite sprite;
    public Boolean alive;
    public Boolean dying;
    public Integer shotTimer;

    public float x;                 //the current location of the goose in the minigame
    public float y;                 //the current height of the goose in the minigame
    public boolean right;           //true means the goose faces right, false means it faces left
    public int id;                  //the primary way of identifying the goose's body, used in collision
    private BaseBattle bb;

    public Goose(int x, int y, int id) {
        this.x = x;
        this.y = y;
        this.id = id;

        //this will all be replaced by looking up in dictionary instead of this static behaviour
        this.spriteR = "gooseIdleR";
        this.spriteL = "gooseIdleL";
        this.spriteDead = "gooseFlying";
        this.spriteName = this.spriteL;
        this.bodyR = "gooseIdleR";
        this.bodyL = "gooseIdleL";
        this.bodyDead = "gooseFlying";
        this.alive = true;
        this.dying = false;
        this.shotTimer = 0;
    }

    public Float[] shotCalcs(FireTruck truck) {
        //this function detects the two edges of the player's movement field
        //then it takes the x and y velocities it needs to hit those places from its own location
        //then it takes the two x's and y's and randomly picks a value between them

        //start by just having it shoot at the truck ever time
        Float xDist1 = (truck.x-5) - this.x;
        Float xDist2 = (truck.x+5) - this.x;
        Float yDist = this.y;

        Random r = new Random();

        Float yForce = (float) r.nextInt(60)+160;
        Float xForce1 = xDist1*1.1f + (yForce+yDist-60)/2;
        Float xForce2 = xDist2*1.1f + (yForce+yDist-60)/2;

        Float[] calc = new Float[2];
        calc[0] = (r.nextFloat()*(xForce1-xForce2))+xForce2;
        calc[1] = yForce;

        return(calc);
    }

    public boolean onCollision(){
        if(dying == true || alive == false) { return (false); }
        // replace sprite
        this.spriteName = this.spriteDead;
        this.dying = true;

        return(true);
    }

    public Body drawBody(World world, PhysicsShapeCache physicsBodies) {
        String b;
        if (right)  { b = this.bodyR;}
        else { b = this.bodyL;}

        this.body = physicsBodies.createBody(b,world, BaseBattle.SCALE, BaseBattle.SCALE);

        MassData md = new MassData();
        md.mass=this.id;
        body.setMassData(md);

        return this.body;
    }
}
