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

package de.sg.benno.util;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.GL_MAX_TEXTURE_SIZE;
import static org.lwjgl.opengl.GL11.glGetIntegerv;

public class Util {

    //-------------------------------------------------
    // OpenGL
    //-------------------------------------------------

    /**
     * Gets the max width and height of a 1D or 2D texture that the GPU supports.
     *
     * @return The max width and height of a texture.
     */
    public static int getMaxTextureSize() {
        int[] maxSize = new int[1];
        glGetIntegerv(GL_MAX_TEXTURE_SIZE, maxSize);

        return maxSize[0];
    }

    //-------------------------------------------------
    // Load file
    //-------------------------------------------------

    /**
     * Get a file from the resources folder.
     * Works everywhere, IDEA, unit test and JAR file.
     *
     * @param filePath The file path.
     * @return InputStream
     * @throws FileNotFoundException If the file does not exist.
     */
    public static InputStream getFileFromResourceAsStream(String filePath) throws FileNotFoundException {
        var classLoader = Util.class.getClassLoader();
        var inputStream = classLoader.getResourceAsStream(Objects.requireNonNull(filePath, "filePath must not be null"));

        if (inputStream == null) {
            throw new FileNotFoundException("BinaryFile " + filePath + " not found.");
        } else {
            return inputStream;
        }
    }

    //-------------------------------------------------
    // Emulating unsigned arithmetic
    //-------------------------------------------------

    /*
    There are no primitive unsigned bytes in Java.
    The usual thing is to cast it to bigger type:
    */

    public static int byteToInt(byte value) {
        return (int) value & 0xFF;
    }

    public static int shortToInt(short value) {
        return (int) value & 0xFFFF;
    }

    //-------------------------------------------------
    // Bits
    //-------------------------------------------------

    /**
     * Extract k bits from position p.
     *
     * @param number The given value.
     * @param k Number of bits to extract.
     * @param p The position from which to extract.
     * @return The extracted value as integer.
     */
    public static int bitExtracted(int number, int k, int p) {
        return (((1 << k) - 1) & (number >> (p - 1)));
    }

    //-------------------------------------------------
    // Color
    //-------------------------------------------------

    public static int rgbToInt(int red, int green, int blue) {
        var alpha = 255;

        return (alpha << 24) |
                (red << 16) |
                (green << 8) |
                (blue);
    }
}
