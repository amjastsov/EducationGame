package com.mygdx.game.cutscene;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Cutscene {
    private enum State {
        ZOOMING_IN,
        WAIT_BEFORE_MOVE,
        MOVING_TO_NPC,
        WAIT_AFTER_MOVE,
        ZOOMING_OUT,
        FINISHED
    }

    private State state = State.ZOOMING_IN;

    private final Vector2 characterPosition;
    private final Rectangle npcCollider;
    private final OrthographicCamera camera;

    private final Vector2 originalCameraPosition;
    private final float originalZoom;

    private static final float ZOOM_SPEED = 0.5f;
    private static final float MOVE_SPEED = 100f;
    private static final float TARGET_ZOOM = 0.5f;
    private static final float WORLD_WIDTH = 1280;
    private static final float WORLD_HEIGHT = 720;

    private boolean facingLeft = false;
    private boolean moving = false;
    private boolean isFinished = false;
    private float waitTimer = 0f;

    public Cutscene(Vector2 characterPosition, Rectangle npcCollider, OrthographicCamera camera) {
        this.characterPosition = characterPosition;
        this.npcCollider = npcCollider;
        this.camera = camera;

        this.originalCameraPosition = new Vector2(camera.position.x, camera.position.y);
        this.originalZoom = camera.zoom;
    }

    public void update(float delta) {
        switch (state) {
            case ZOOMING_IN:
                zoomIn(delta);
                break;

            case WAIT_BEFORE_MOVE:
                waitTimer -= delta;
                if (waitTimer <= 0) {
                    state = State.MOVING_TO_NPC;
                }
                break;

            case MOVING_TO_NPC:
                moveCharacter(delta);
                break;

            case WAIT_AFTER_MOVE:
                waitTimer -= delta;
                if (waitTimer <= 0) {
                    state = State.ZOOMING_OUT;
                }
                break;

            case ZOOMING_OUT:
                returnCameraToOriginal(delta);
                break;

            case FINISHED:
                isFinished = true;
                break;
        }

        clampCameraToBounds();
        camera.update();
    }

    private void zoomIn(float delta) {
        if (camera.zoom > TARGET_ZOOM) {
            camera.zoom -= ZOOM_SPEED * delta;
            camera.zoom = MathUtils.clamp(camera.zoom, TARGET_ZOOM, originalZoom);
            followCharacter(delta);
        } else {
            waitTimer = 1f;
            state = State.WAIT_BEFORE_MOVE;
        }
    }

    private void moveCharacter(float delta) {
        if (characterPosition.x < npcCollider.x - 60f) {
            characterPosition.x += MOVE_SPEED * delta;
            facingLeft = false;
            moving = true;
            followCharacter(delta);
        } else {
            moving = false;
            waitTimer = 1f;
            state = State.WAIT_AFTER_MOVE;
        }
    }

    private void returnCameraToOriginal(float delta) {
        camera.position.x += (originalCameraPosition.x - camera.position.x) * 2f * delta;
        camera.position.y += (originalCameraPosition.y - camera.position.y) * 2f * delta;
        camera.zoom += (originalZoom - camera.zoom) * 2f * delta;

        if (Math.abs(camera.position.x - originalCameraPosition.x) < 0.5f &&
            Math.abs(camera.position.y - originalCameraPosition.y) < 0.5f &&
            Math.abs(camera.zoom - originalZoom) < 0.01f) {
            camera.position.set(originalCameraPosition.x, originalCameraPosition.y, 0);
            camera.zoom = originalZoom;
            state = State.FINISHED;
        }
    }

    private void followCharacter(float delta) {
        float targetX = characterPosition.x + 32;
        float targetY = characterPosition.y + 64;
        camera.position.x += (targetX - camera.position.x) * 2f * delta;
        camera.position.y += (targetY - camera.position.y) * 2f * delta;
    }

    private void clampCameraToBounds() {
        float viewportWidth = camera.viewportWidth * camera.zoom;
        float viewportHeight = camera.viewportHeight * camera.zoom;
        float halfWidth = viewportWidth / 2f;
        float halfHeight = viewportHeight / 2f;

        camera.position.x = MathUtils.clamp(camera.position.x, halfWidth, WORLD_WIDTH - halfWidth);
        camera.position.y = MathUtils.clamp(camera.position.y, halfHeight, WORLD_HEIGHT - halfHeight);
    }

    public boolean isFinished() {
        return isFinished;
    }

    public boolean isMoving() {
        return moving;
    }

    public boolean isFacingLeft() {
        return facingLeft;
    }
}
