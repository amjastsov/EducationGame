package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Simple2DGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Texture npcSheet;
    private Texture bgTexture;
    private Texture groundTexture;

    private Animation<TextureRegion> walkLeftAnim;
    private Animation<TextureRegion> walkRightAnim;
    private TextureRegion idleLeft;
    private TextureRegion idleRight;

    private float stateTime;
    private boolean facingLeft = true;
    private boolean moving = false;

    private OrthographicCamera camera;
    private Vector2 npcPosition;

    private Rectangle npcCollider;
    private Rectangle groundCollider;

    private static final float FRAME_DURATION = 0.1f;
    private static final float SPEED = 150f;
    private static final float CHARACTER_WIDTH = 150f;
    private static final float CHARACTER_HEIGHT = 150f;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        npcSheet = new Texture("character_walk.png");
        bgTexture = new Texture("bg.png");
        groundTexture = new Texture("ground.png");

        npcSheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        bgTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        groundTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        TextureRegion[][] tmp = TextureRegion.split(npcSheet, 512, 512);
        walkLeftAnim = new Animation<>(FRAME_DURATION, tmp[0]);
        walkRightAnim = new Animation<>(FRAME_DURATION, tmp[1]);
        idleLeft = tmp[0][0];
        idleRight = tmp[1][0];

        stateTime = 0f;
        npcPosition = new Vector2(300, 64);

        camera = new OrthographicCamera(1280, 720);
        camera.position.set(640, 360, 0);
        camera.update();

        npcCollider = new Rectangle(npcPosition.x, npcPosition.y, 50, 130);
        groundCollider = new Rectangle(0, 0, 1280, 25);
    }

    @Override
    public void render() {
        handleInput();
        stateTime += Gdx.graphics.getDeltaTime();

        npcCollider.setPosition(npcPosition.x + 50, npcPosition.y - 40);

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(bgTexture, 0, 0, 1280, 720);
        batch.draw(groundTexture, 0, 0, 1280, 720);

        TextureRegion currentFrame;
        if (moving) {
            currentFrame = (facingLeft ? walkLeftAnim.getKeyFrame(stateTime, true) : walkRightAnim.getKeyFrame(stateTime, true));
        } else {
            currentFrame = (facingLeft ? idleLeft : idleRight);
        }

        batch.draw(currentFrame, npcPosition.x, npcPosition.y - 45, CHARACTER_WIDTH, CHARACTER_HEIGHT);
        batch.end();

        // colliders for test
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 0, 0, 1);
        shapeRenderer.rect(npcCollider.x, npcCollider.y, npcCollider.width, npcCollider.height);
        shapeRenderer.setColor(0, 1, 0, 1);
        shapeRenderer.rect(groundCollider.x, groundCollider.y, groundCollider.width, groundCollider.height);
        shapeRenderer.end();
    }

    private void handleInput() {
        moving = false;
        float delta = Gdx.graphics.getDeltaTime();

        if ((Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) && npcPosition.x > 0) {
            npcPosition.x -= SPEED * delta;
            facingLeft = true;
            moving = true;
        } else if ((Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) && npcPosition.x < 1280 - CHARACTER_WIDTH) {
            npcPosition.x += SPEED * delta;
            facingLeft = false;
            moving = true;
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        npcSheet.dispose();
        bgTexture.dispose();
        groundTexture.dispose();
        shapeRenderer.dispose();
    }
}
