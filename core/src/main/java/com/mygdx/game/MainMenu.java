package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.graphics.OrthographicCamera;

public class MainMenu {

    private Rectangle playButtonBounds;
    private BitmapFont font;
    private boolean playPressed;

    public MainMenu() {
        font = new BitmapFont();
        font.getData().setScale(2f);
        playPressed = false;
        // We'll initialize the button bounds later based on the camera
        playButtonBounds = new Rectangle(0, 0, 200, 50);
    }

    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer, OrthographicCamera camera) {
        camera.update();

        float centerX = camera.viewportWidth / 2f;
        float centerY = camera.viewportHeight / 2f;

        // Center the button
        playButtonBounds.setPosition(centerX - playButtonBounds.width / 2f, centerY - playButtonBounds.height / 2f);

        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);

        batch.begin();
        font.draw(batch, "My Simple Game", centerX - 100, centerY + 80);
        font.draw(batch, "[ PLAY ]", playButtonBounds.x + 50, playButtonBounds.y + 35);
        batch.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 1, 1, 1);
        shapeRenderer.rect(playButtonBounds.x, playButtonBounds.y, playButtonBounds.width, playButtonBounds.height);
        shapeRenderer.end();
    }

    public void update(OrthographicCamera camera) {
        if (Gdx.input.justTouched()) {
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos); // Convert from screen to world coordinates

            if (playButtonBounds.contains(touchPos.x, touchPos.y)) {
                playPressed = true;
            }
        }
    }

    public boolean isPlayPressed() {
        return playPressed;
    }
}
