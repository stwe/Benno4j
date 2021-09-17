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

package de.sg.benno.file;

import de.sg.benno.ogl.resource.Texture;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Represents a BshTexture.
 * Contains a {@link BufferedImage} and an OpenGl {@link Texture} for each Bsh image.
 */
public class BshTexture {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * A {@link BufferedImage} object.
     */
    private final BufferedImage bufferedImage;

    /**
     * A {@link Texture} object.
     */
    private Texture texture;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link BshTexture} object.
     *
     * @param bufferedImage {@link BufferedImage}
     */
    BshTexture(BufferedImage bufferedImage) {
        this.bufferedImage = Objects.requireNonNull(bufferedImage, "bufferedImage must not be null");
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #bufferedImage}.
     *
     * @return {@link #bufferedImage}
     */
    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    /**
     * Get the width of the {@link #bufferedImage}.
     *
     * @return int
     */
    public int getWidth() {
        return bufferedImage.getWidth();
    }

    /**
     * Get the height of the {@link #bufferedImage}.
     *
     * @return int
     */
    public int getHeight() {
        return bufferedImage.getHeight();
    }

    /**
     * Get {@link #texture}.
     *
     * @return {@link #texture}
     */
    public Texture getTexture() {
        return texture;
    }

    //-------------------------------------------------
    // Setter
    //-------------------------------------------------

    /**
     * Set a {@link Texture}.
     *
     * @param texture {@link Texture}
     */
    void setTexture(Texture texture) {
        this.texture = texture;
    }
}
