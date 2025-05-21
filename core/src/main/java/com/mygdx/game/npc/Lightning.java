package com.mygdx.game.npc;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Lightning {
    private Sprite lightningSprite;
    private float timer;
    private boolean visible = false;

    public Lightning(Texture texture, float x, float y) {
        lightningSprite = new Sprite(texture);
        lightningSprite.setPosition(x, y);
        lightningSprite.setAlpha(0f);
    }

    public void trigger() {
        timer = 0.2f;
        visible = true;
        lightningSprite.setAlpha(1f);
    }

    public void update(float delta) {
        if (visible) {
            timer -= delta;
            if (timer <= 0) {
                lightningSprite.setAlpha(0f);
                visible = false;
            }
        }
    }

    public void draw(SpriteBatch batch) {
        if (visible) lightningSprite.draw(batch);
    }

    public Vector2 getPosition() {
        return new Vector2(lightningSprite.getX(), lightningSprite.getY()); // or however you store its position
    }
}
