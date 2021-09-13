/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.ogl.renderer;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class RenderUtil {

    /**
     * Creates a model matrix for rendering.
     *
     * @return {@link Matrix4f}
     */
    public static Matrix4f createModelMatrix(Vector2f position, Vector2f size) {
        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix
                .identity()
                .translate(new Vector3f(position, 0.0f))
                .scale(new Vector3f(size, 1.0f));

        return modelMatrix;
    }
}
