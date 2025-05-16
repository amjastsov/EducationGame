package com.mygdx.game.dialogue;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;

public class DialogueBox {
    private final BitmapFont font;
    private final ShapeRenderer shapeRenderer;
    private static final float X = 0;
    private static final float Y = 350;
    private static final float WIDTH = 1080;
    private final float height;
    private boolean visible = false;
    private DialogueLine currentLine;

    public DialogueBox() {
        font = new BitmapFont();
        shapeRenderer = new ShapeRenderer();
        height = 65;
    }

    public void show(DialogueLine line) {
        this.currentLine = line;
        this.visible = true;
    }

    public void hide() {
        this.visible = false;
    }

    public boolean isVisible() {
        return visible;
    }

    public void render(SpriteBatch batch) {
        if (!visible || currentLine == null) return;
        // End batch before using ShapeRenderer
        batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.8f);
        shapeRenderer.rect(X, Y, WIDTH, height);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        // Resume batch to draw text
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, currentLine.speaker() + ": " + currentLine.text(), X + 20, Y + 230);
    }

    public void dispose() {
        font.dispose();
        shapeRenderer.dispose();
    }
}
