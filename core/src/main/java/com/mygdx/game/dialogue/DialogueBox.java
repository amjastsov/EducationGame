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

    private boolean typing = false;
    private StringBuilder displayedText = new StringBuilder();
    private float typeTimer = 0;
    private int charIndex = 0;
    private static final float CHAR_INTERVAL = 0.03f; // seconds per character

    public DialogueBox() {
        font = new BitmapFont();
        shapeRenderer = new ShapeRenderer();
        height = 65;
    }

    public void startTyping(DialogueLine line) {
        this.currentLine = line;
        this.visible = true;
        this.typing = true;
        this.displayedText = new StringBuilder();
        this.typeTimer = 0;
        this.charIndex = 0;
    }

    public void skipTyping() {
        if (currentLine != null) {
            displayedText.append(currentLine.text());
            typing = false;
        }
    }

    public boolean isTyping() {
        return typing;
    }

    public void hide() {
        this.visible = false;
        this.typing = false;
        this.displayedText = new StringBuilder();
        this.charIndex = 0;
    }

    public boolean isVisible() {
        return visible;
    }

    public void render(SpriteBatch batch) {
        if (!visible || currentLine == null) return;

        float delta = Gdx.graphics.getDeltaTime();
        if (typing) {
            typeTimer += delta;
            while (typeTimer >= CHAR_INTERVAL && charIndex < currentLine.text().length()) {
                displayedText.append(currentLine.text().charAt(charIndex));
                charIndex++;
                typeTimer -= CHAR_INTERVAL;
            }
            if (charIndex >= currentLine.text().length()) {
                typing = false;
            }
        }

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
        font.draw(batch, currentLine.speaker() + ": " + displayedText.toString(), X + 20, Y + 230);
    }

    public void dispose() {
        font.dispose();
        shapeRenderer.dispose();
    }
}
