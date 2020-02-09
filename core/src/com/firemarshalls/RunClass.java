package com.firemarshalls;

import com.badlogic.gdx.Game;
import com.firemarshalls.screens.MainMenu;

public class RunClass extends Game {
    //definitions
    int num;
    boolean paused;
    private Game game;

    @Override
    public void create () {
        this.game = this;
        num = 10;
        paused = false;
    }

    @Override
    public void render () {
        if(!paused){
            paused=true;
            setScreen(new MainMenu(game));
        }
        if(paused){
            super.render();
        }
    }

    public void pause() {
        super.pause();
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
