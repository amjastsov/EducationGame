package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import com.mygdx.game.npc.NPC;
import com.mygdx.game.npc.Lightning;
import com.mygdx.game.cutscene.Cutscene;
import com.mygdx.game.gamestate.GameState;
import com.mygdx.game.dialogue.DialogueBox;
import com.mygdx.game.dialogue.DialogueLine;

public class Simple2DGame extends ApplicationAdapter {

    private Vector2 characterPosition;
    private Rectangle characterCollider;
    private Rectangle groundCollider;
    private BitmapFont font;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;
    private Music electricSound;
    private Texture characterSheet;
    private Texture bgTexture;
    private Texture groundTexture;
    private Texture npcFaceTexture;
    private Texture characterFaceTexture;
    private Texture lightningTexture;
    private TextureRegion idleLeft;
    private TextureRegion idleRight;
    private Animation<TextureRegion> walkLeftAnim;
    private Animation<TextureRegion> walkRightAnim;

    private NPC npc;
    private Cutscene cutscene;
    private MainMenu mainMenu;
    private GameState gameState = GameState.MENU;
    private Lightning lightning;
    private DialogueBox dialogueBox;
    private DialogueLine[] conversation;

    private boolean moving = false;
    private boolean facingLeft = true;
    private boolean inDialogue = false;
    private boolean cutsceneFinished = false;

    private float stateTime;
    private float lightningCooldown = 0f;
    private int currentDialogueIndex = 0;

    private static final float SPEED = 150f;
    private static final float SCREEN_WIDTH = 1280f;
    private static final float SCREEN_HEIGHT = 720f;
    private static final float FRAME_DURATION = 0.1f;
    private static final float CHARACTER_WIDTH = 150f;
    private static final float CHARACTER_HEIGHT = 150f;
    private static final float CUTSCENE_ZOOM_SPEED = 0.5f;
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
        npcFaceTexture = new Texture("npc_face.png");
        characterFaceTexture = new Texture("character_face.png");
        characterSheet = new Texture("character_walk.png");
        bgTexture = new Texture("bg.png");
        groundTexture = new Texture("ground.png");
        lightningTexture = new Texture("lightning.png");
        groundCollider = new Rectangle(0, 0, SCREEN_WIDTH, 25);
        mainMenu = new MainMenu();
        lightning = new Lightning(lightningTexture, 900, 70);
        characterPosition = new Vector2(300, 64);
        characterCollider = new Rectangle(characterPosition.x, characterPosition.y, 50, 130);
        TextureRegion[][] tmp = TextureRegion.split(characterSheet, 512, 512);
        walkLeftAnim = new Animation<>(FRAME_DURATION, tmp[0]);
        walkRightAnim = new Animation<>(FRAME_DURATION, tmp[1]);
        camera = new OrthographicCamera(SCREEN_WIDTH, SCREEN_HEIGHT);
        electricSound = Gdx.audio.newMusic(Gdx.files.internal("electric-sound.mp3"));

        characterSheet.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        bgTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        groundTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        camera.position.set(640, 360, 0);
        camera.setToOrtho(false, 1280, 720);
        camera.update();

        cutscene = new Cutscene(characterPosition, npc.getCollider(), camera);

        electricSound.setLooping(true);
        electricSound.setVolume(0.3f);

        idleLeft = tmp[0][0];
        idleRight = tmp[1][0];

        stateTime = 0f;
    }

    @Override
    public void render() {
        clearScreen();

        if (handleMenuState()) return;

        float delta = Gdx.graphics.getDeltaTime();
        stateTime += delta;

        updateCutsceneOrInput(delta);
        updateCameraZoomIfNeeded(delta);
        updateCharacterCollider();
        renderGame();
        updateLightningLogic(delta);
        // show colliders for debugging
//        showColliders();
    }

    /**
     * Clears the screen by resetting the current framebuffer to a blank state.
     */
    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    /**
     * Handles the game state when the game is in the MENU state. Updates
     * and renders the main menu and transitions to the GAME state if
     * the play button is pressed.
     *
     * @return true if the game state is MENU and the menu is handled; false otherwise.
     */
    private boolean handleMenuState() {
        if (gameState == GameState.MENU) {
            mainMenu.update(camera);
            mainMenu.render(batch, shapeRenderer, camera);
            if (mainMenu.isPlayPressed()) {
                gameState = GameState.GAME;
            }
            return true;
        }
        return false;
    }

    /**
     * Updates the game logic by either progressing through a cutscene or handling user input
     * depending on the current state of the cutscene.
     *
     * @param delta the time elapsed since the last frame, used for updating the cutscene
     *              or handling time-sensitive input.
     */
    private void updateCutsceneOrInput(float delta) {
        if (!cutsceneFinished) {
            cutscene.update(delta);
            moving = cutscene.isMoving();
            facingLeft = cutscene.isFacingLeft();
            if (cutscene.isFinished()) {
                cutsceneFinished = true;
            }
        } else {
            handleInput();
        }
    }

    /**
     * Updates the camera's zoom level if necessary, particularly after the cutscene has
     * finished playing. This method gradually adjusts the zoom level back to a default
     * value of 1.0 for normal gameplay.
     *
     * @param delta the time elapsed since the last frame, used to calculate the incremental zoom adjustment
     */
    private void updateCameraZoomIfNeeded(float delta) {
        if (cutsceneFinished && camera.zoom != 1f) {
            camera.zoom += CUTSCENE_ZOOM_SPEED * delta;
            if (camera.zoom > 1f) {
                camera.zoom = 1f;
            }
        }
    }

    /**
     * Updates the position of the character's collider based on the current position of the character.
     */
    private void updateCharacterCollider() {
        characterCollider.setPosition(characterPosition.x + 50, characterPosition.y - 40);
    }

    /**
     * Renders the main gameplay screen, drawing the background, NPC, player character,
     * dialogue box, and other visual elements. Also handles certain dynamic elements
     * like animations and conditional prompts.
     */
    private void renderGame() {
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(bgTexture, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
        batch.draw(groundTexture, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);

        npc.render(batch);
        lightning.draw(batch);

        TextureRegion currentFrame = getAnimationFrame();
        batch.draw(currentFrame, characterPosition.x, characterPosition.y - 45, CHARACTER_WIDTH, CHARACTER_HEIGHT);

        dialogueBox.render(batch);

        if (canStartDialogue()) {
            font.draw(batch, PRESS_SPACE_TO_TALK, npc.getCollider().x, npc.getCollider().y + 140);
        }

        batch.end();

        handleDialogue();

        if (canPlaySound()) {
            if (!electricSound.isPlaying()) {
                electricSound.play();
            }
            updateElectricSoundVolume();
        } else {
            if (electricSound.isPlaying()) {
                electricSound.pause();
            }
        }
    }

    /**
     * Updates the logic for the lightning effect, including its visibility, cooldown, and triggering
     * logic based on the player's proximity.
     *
     * @param delta the time elapsed since the last frame, used to update animations and cooldown timers
     */
    private void updateLightningLogic(float delta) {
        lightning.update(delta);

        float distanceToLightning = characterPosition.dst(900, 70);
        if (distanceToLightning < 100 && lightningCooldown <= 0f) {
            lightning.trigger();
            lightningCooldown = 0.5f;
        }

        if (lightningCooldown > 0f) {
            lightningCooldown -= delta;
        }
    }

    /**
     * Updates the electric sound volume based on the character's distance from the lightning.
     * The closer the character is, the louder the sound (up to maxVolume).
     */
    private void updateElectricSoundVolume() {
        float maxDistance = 300f;  // distance at which sound is barely audible
        float minDistance = 50f;   // distance at which sound is at maxVolume
        float maxVolume = 0.7f;

        float distance = characterPosition.dst(lightning.getPosition()); // Assuming lightning has a getPosition()

        if (distance < minDistance) {
            electricSound.setVolume(maxVolume);
        } else if (distance > maxDistance) {
            electricSound.setVolume(0f);
        } else {
            // Linear interpolation between min and max distance
            float volume = maxVolume * (1 - (distance - minDistance) / (maxDistance - minDistance));
            electricSound.setVolume(volume);
        }
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
        return npc.getCollider().overlaps(characterCollider) && !inDialogue && !dialogueBox.isVisible() && cutsceneFinished;
    }

    /**
     * Determines if a sound can be played based on the current game state and conditions.
     * This method checks the following conditions:
     * 1. The player is not in a dialogue.
     * 2. The cutscene has finished.
     * 3. The game is in the "GAME" state.
     *
     * @return true if all the above conditions are met, indicating that a sound can be played; false otherwise.
     */
    private boolean canPlaySound() {
        return !inDialogue && cutsceneFinished && gameState == GameState.GAME;
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
        shapeRenderer.setColor(0, 0, 1, 1); // Blue - NPC
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
                Texture texture = conversation[currentDialogueIndex].speaker().equals("NPC") ? npcFaceTexture : characterFaceTexture;
                dialogueBox.startTyping(conversation[currentDialogueIndex], texture);
                electricSound.stop();
            }
        } else if (inDialogue && Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (dialogueBox.isTyping()) {
                dialogueBox.skipTyping();
            } else {
                currentDialogueIndex++;
                if (currentDialogueIndex >= conversation.length) {
                    dialogueBox.hide();
                    inDialogue = false;
                } else {
                    Texture texture = conversation[currentDialogueIndex].speaker().equals("NPC") ? npcFaceTexture : characterFaceTexture;
                    dialogueBox.startTyping(conversation[currentDialogueIndex], texture);
                    electricSound.stop();
                }
            }
        }
    }

    @Override
    public void dispose() {
        npc.dispose();
        batch.dispose();
        bgTexture.dispose();
        dialogueBox.dispose();
        electricSound.dispose();
        groundTexture.dispose();
        shapeRenderer.dispose();
        characterSheet.dispose();
        lightningTexture.dispose();
    }
}
