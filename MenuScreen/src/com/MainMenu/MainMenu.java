package com.MainMenu;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

public class MainMenu extends ApplicationAdapter {


        SpriteBatch batch;

        @Override
        public void create() {
            banana = textureAtlas.createSprite("banana");

            batch = new SpriteBatch();
        }

        @Override
        public void dispose() {
            textureAtlas.dispose();
        }

        @Override
        public void render() {
            Gdx.gl.glClearColor(1, 0, 0, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            batch.begin();
            banana.draw(batch);
            batch.end();
        }
    }
}
