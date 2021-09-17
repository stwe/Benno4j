/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021, stwe <https://github.com/stwe/Benno4j>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.sg.benno.ogl.input;

import de.sg.benno.ogl.Window;
import org.joml.Vector2d;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Encapsulates mouse access.
 */
public class MouseInput {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The previous position.
     */
    private final Vector2d previousPos;

    /**
     * The current position.
     */
    private final Vector2d currentPos;

    /**
     * The mouse displacement from the {@link #previousPos}.
     */
    private final Vector2f displVec;

    /**
     * Info whether the mouse is in the window.
     */
    private boolean inWindow = false;

    /**
     * Flag whether the left mouse button was pressed.
     */
    private boolean leftButtonPressed = false;

    /**
     * Flag whether the right mouse button was pressed.
     */
    private boolean rightButtonPressed = false;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link MouseInput} object.
     */
    public MouseInput() {
        this.previousPos = new Vector2d(-1.0, -1.0);
        this.currentPos = new Vector2d(0.0, 0.0);
        this.displVec = new Vector2f();
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #currentPos}.
     *
     * @return {@link #currentPos}
     */
    public Vector2d getCurrentPos() {
        return currentPos;
    }

    /**
     * Get current x position.
     *
     * @return float
     */
    public float getX() {
        return (float)currentPos.x;
    }

    /**
     * Get current y position.
     *
     * @return float
     */
    public float getY() {
        return (float)currentPos.y;
    }

    /**
     * Get {@link #displVec}.
     *
     * @return {@link #displVec}
     */
    public Vector2f getDisplVec() {
        return displVec;
    }

    /**
     * Get {@link #inWindow}.
     *
     * @return {@link #inWindow}
     */
    public boolean isInWindow() {
        return inWindow;
    }

    /**
     * Get {@link #leftButtonPressed}.
     *
     * @return {@link #leftButtonPressed}
     */
    public boolean isLeftButtonPressed() {
        return leftButtonPressed;
    }

    /**
     * Get {@link #rightButtonPressed}.
     *
     * @return {@link #rightButtonPressed}
     */
    public boolean isRightButtonPressed() {
        return rightButtonPressed;
    }

    //-------------------------------------------------
    // Init
    //-------------------------------------------------

    /**
     * Registers a set of callbacks to process mouse events.
     *
     * @param window The {@link Window} object to get the window handle.
     */
    public void init(Window window) {
        // Registers a callback that will be invoked when the mouse is moved.
        glfwSetCursorPosCallback(window.getWindowHandle(), (windowHandle, xpos, ypos) -> {
            currentPos.x = xpos;
            currentPos.y = ypos;
        });

        // Registers a callback that will be invoked when the mouse enters our window.
        glfwSetCursorEnterCallback(window.getWindowHandle(), (windowHandle, entered) -> {
            inWindow = entered;
        });

        // Registers a callback that will be invoked when a mouse button is pressed.
        glfwSetMouseButtonCallback(window.getWindowHandle(), (windowHandle, button, action, mode) -> {
            leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS;
            rightButtonPressed = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS;
        });
    }

    //-------------------------------------------------
    // Logic
    //-------------------------------------------------

    /**
     * Calculates the mouse displacement from the previous position and stores it into the {@link #displVec} variable.
     */
    public void input() {
        displVec.x = 0.0f;
        displVec.y = 0.0f;

        if (previousPos.x > 0 && previousPos.y > 0 && inWindow) {
            double deltax = currentPos.x - previousPos.x;
            double deltay = currentPos.y - previousPos.y;
            boolean rotateX = deltax != 0;
            boolean rotateY = deltay != 0;

            if (rotateX) {
                displVec.y = (float) deltax;
            }

            if (rotateY) {
                displVec.x = (float) deltay;
            }
        }

        previousPos.x = currentPos.x;
        previousPos.y = currentPos.y;
    }
}
