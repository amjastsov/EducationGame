package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.dialogue.DialogueBox;
import com.mygdx.game.dialogue.DialogueLine;
import com.mygdx.game.npc.NPC;

public class Simple2DGame extends ApplicationAdapter {
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Texture characterSheet;
    private Texture bgTexture;
    private Texture groundTexture;

    private Animation<TextureRegion> walkLeftAnim;
    private Animation<TextureRegion> walkRightAnim;
    private TextureRegion idleLeft;
    private TextureRegion idleRight;

    private float stateTime;
    private BitmapFont font;
    private boolean facingLeft = true;
    private boolean moving = false;

    private OrthographicCamera camera;
    private Vector2 characterPosition;

    private Rectangle characterCollider;
    private Rectangle groundCollider;

    private NPC npc;
    private DialogueBox dialogueBox;
    private DialogueLine[] conversation;
    private int currentDialogueIndex = 0;
    private boolean inDialogue = false;

    private static final float SCREEN_WIDTH = 1280f;
    private static final float SCREEN_HEIGHT = 720f;
    private static final float FRAME_DURATION = 0.1f;
    private static final float SPEED = 150f;
    private static final float CHARACTER_WIDTH = 150f;
    private static final float CHARACTER_HEIGHT = 150f;
    private static final String PRESS_SPACE_TO_TALK = "Press SPACE to talk";

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        npc = new NPC(600, 30);
        dialogueBox = new DialogueBox();
        font = new BitmapFont();
        conversation = new DialogueLine[] {
            new DialogueLine("NPC", "Hello, do you know how to make dialogues with LIBGDX?"),
            new DialogueLine("Character", "Oh yeah, now I know all about that!")
        };

        characterSheet = new Texture("character_walk.png");
        bgTexture = new Texture("bg.png");
        groundTexture = new Texture("ground.png");

        characterSheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        bgTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        groundTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        TextureRegion[][] tmp = TextureRegion.split(characterSheet, 512, 512);
        walkLeftAnim = new Animation<>(FRAME_DURATION, tmp[0]);
        walkRightAnim = new Animation<>(FRAME_DURATION, tmp[1]);
        idleLeft = tmp[0][0];
        idleRight = tmp[1][0];

        stateTime = 0f;
        characterPosition = new Vector2(300, 64);

        camera = new OrthographicCamera(SCREEN_WIDTH, SCREEN_HEIGHT);
        camera.position.set(640, 360, 0);
        camera.update();

        characterCollider = new Rectangle(characterPosition.x, characterPosition.y, 50, 130);
        groundCollider = new Rectangle(0, 0, SCREEN_WIDTH, 25);
    }

    @Override
    public void render() {
        handleInput();

        stateTime += Gdx.graphics.getDeltaTime();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        characterCollider.setPosition(characterPosition.x + 50, characterPosition.y - 40);

        batch.begin();

        batch.draw(bgTexture, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        batch.draw(groundTexture, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        npc.render(batch);

        TextureRegion currentFrame = getAnimationFrame();
        batch.draw(currentFrame, characterPosition.x, characterPosition.y - 45, CHARACTER_WIDTH, CHARACTER_HEIGHT);

        dialogueBox.render(batch);

        if (canStartDialogue()) {
            font.draw(batch, PRESS_SPACE_TO_TALK, npc.getCollider().x, npc.getCollider().y + 140);
        }

        batch.end();

        // Handle dialogue logic outside the batch
        handleDialogue();
        // show colliders for debugging
        showColliders();
    }

    /**
     * Retrieves the current frame of animation based on the character's movement and direction state.
     * If the character is moving, it plays the relevant walking animation based on the direction
     * the character is facing (left or right). If the character is stationary, it returns the
     * appropriate idle texture for the direction the character is facing.
     *
     * @return the {@code TextureRegion} representing the current animation frame. If moving, it is
     *         a frame from the walking animation; if idle, it is a static frame.
     */
    private TextureRegion getAnimationFrame() {
        TextureRegion currentFrame;
        if (moving) {
            currentFrame = (facingLeft ? walkLeftAnim.getKeyFrame(stateTime, true)
                : walkRightAnim.getKeyFrame(stateTime, true));
        } else {
            currentFrame = (facingLeft ? idleLeft : idleRight);
        }
        return currentFrame;
    }

    /**
     * Determines if a dialogue can be initiated between the player character and the NPC.
     * <p>
     * This method checks three conditions to decide if dialogue can start:
     * 1. The character's collider overlaps with the NPC's collider.
     * 2. The player is not yet in a dialogue.
     * 3. The dialogue box is currently visible.
     *
     * @return true if the above conditions are met, indicating that dialogue can start; false otherwise.
     */
    private boolean canStartDialogue() {
        return npc.getCollider().overlaps(characterCollider) && !inDialogue && !dialogueBox.isVisible();
    }

    /**
     * Handles user input for character movement in the game.
     * <p>
     * This method processes keyboard input for controlling the movement of the player character
     * and updates the character's position accordingly. The movement logic is only executed
     * if the character is not currently in dialogue mode.
     * <p>
     * Movement details:
     * - If the "A" or LEFT key is pressed and the character's position is greater than 0, the
     *   character moves to the left. The `facingLeft` flag is set to true, and the `moving` flag
     *   is set to indicate motion.
     * - If the "D" or RIGHT key is pressed and the character's position is within the game screen's
     *   right boundary (calculated as 1280 minus the character's width), the character moves to the
     *   right. The `facingLeft` flag is set to false, and the `moving` flag is set to indicate motion.
     * <p>
     * The method also uses the delta time provided by LibGDX to ensure smooth and frame-rate-independent
     * movement of the character.
     */
    private void handleInput() {
        moving = false;
        float delta = Gdx.graphics.getDeltaTime();
        if (!inDialogue) {
            if ((Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) && characterPosition.x > 0) {
                characterPosition.x -= SPEED * delta;
                facingLeft = true;
                moving = true;
            } else if ((Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT))
                && characterPosition.x < SCREEN_WIDTH - CHARACTER_WIDTH) {
                characterPosition.x += SPEED * delta;
                facingLeft = false;
                moving = true;
            }
        }
    }

    /**
     * Renders debugging visuals for colliders in the game.
     * <p>
     * This method uses a ShapeRenderer to draw rectangular outlines
     * representing the colliders of game objects, such as the player character,
     * the ground, and the NPC. Each collider is drawn in a specific color
     * for visual distinction:
     * - Red: Player's collider.
     * - Green: Ground's collider.
     * - Blue: NPC's collider.
     * <p>
     * The method sets the ShapeRenderer's projection matrix to match the
     * camera’s combined matrix to ensure proper alignment with the game's
     * coordinate system.
     * <p>
     * This method is typically used for debugging purposes to visualize
     * interactions and ensure the colliders are correctly positioned.
     */
    private void showColliders() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1, 0, 0, 1); // Red - player
        shapeRenderer.rect(characterCollider.x, characterCollider.y, characterCollider.width, characterCollider.height);
        shapeRenderer.setColor(0, 1, 0, 1); // Green - ground
        shapeRenderer.rect(groundCollider.x, groundCollider.y, groundCollider.width, groundCollider.height);
        shapeRenderer.setColor(0, 0, 1, 1);
        shapeRenderer.rect(npc.getCollider().x, npc.getCollider().y, npc.getCollider().width, npc.getCollider().height);
        shapeRenderer.end();
    }

    /**
     * Handles the dialogue interaction between the player and the NPC, determining when
     * dialogues should start, progress, or end based on player input and interaction conditions.
     * <p>
     * The method checks if the player's collider overlaps with the NPC's collider and if the
     * dialogue box is visible to determine if a dialogue can be initiated.
     * <p>
     * - If the SPACE key is pressed while conditions are met, the dialogue starts from the first
     *   line of the conversation.
     * - If already in a dialogue and the SPACE key is pressed, the conversation progresses to
     *   the next dialogue line.
     * - If the conversation reaches the end (no more lines in the dialogue), the dialogue box
     *   is hidden and dialogue mode ends.
     * <p>
     * This method must be invoked periodically, such as within the render cycle, to ensure that
     * it can detect user input and update the dialogue states appropriately.
     */
    private void handleDialogue() {
        if (npc.getCollider().overlaps(characterCollider) && !inDialogue && !dialogueBox.isVisible()) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                inDialogue = true;
                currentDialogueIndex = 0;
                dialogueBox.show(conversation[currentDialogueIndex]);
            }
        } else if (inDialogue && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            currentDialogueIndex++;
            if (currentDialogueIndex >= conversation.length) {
                dialogueBox.hide();
                inDialogue = false;
            } else {
                dialogueBox.show(conversation[currentDialogueIndex]);
            }
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        bgTexture.dispose();
        groundTexture.dispose();
        shapeRenderer.dispose();
        npc.dispose();
        characterSheet.dispose();
        dialogueBox.dispose();
    }
}
