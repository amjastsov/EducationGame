package com.mygdx.game.npc;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class NPC {
    private final Texture texture;
    private final Vector2 position;
    private final Rectangle collider;

    public NPC(float x, float y) {
        texture = new Texture("npc.png");
        position = new Vector2(x, y);
        collider = new Rectangle(x, y, 128, 128);
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, 150f, 150f);
    }

    public Rectangle getCollider() {
        return collider;
    }

    public void dispose() {
        texture.dispose();
    }
}
