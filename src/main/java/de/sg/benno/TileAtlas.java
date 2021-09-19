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

package de.sg.benno;

import de.sg.benno.file.ImageFile;
import de.sg.benno.ogl.resource.Texture;
import org.joml.Vector2f;

import java.io.IOException;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;
import static org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL45.glTextureStorage3D;
import static org.lwjgl.opengl.GL45.glTextureSubImage3D;

public class TileAtlas {

    //-------------------------------------------------
    // Constants GFX
    //-------------------------------------------------

    /**
     * Number of GFX atlas images.
     * 24 images a (16 rows * 16 rows)
     */
    public static final int NR_OF_GFX_ATLAS_IMAGES = 24;

    /**
     * Number of rows in GFX atlas image.
     */
    public static final int NR_OF_GFX_ROWS = 16;

    /**
     * Width of the largest GFX picture in the <i>stadtfld.bsh</i>.
     * Can also be read with the BennoFiles class.
     */
    public static final float MAX_GFX_WIDTH = 64.0f;

    /**
     * Height of the largest GFX picture in the <i>stadtfld.bsh</i>.
     * Can also be read with the BennoFiles class.
     */
    public static final float MAX_GFX_HEIGHT = 286.0f;

    //-------------------------------------------------
    // Constants MGFX
    //-------------------------------------------------

    /**
     * Number of MGFX atlas images.
     * 6 images a (32 rows * 32 rows)
     */
    public static final int NR_OF_MGFX_ATLAS_IMAGES = 6;

    /**
     * Number of rows in MGFX atlas image.
     */
    public static final int NR_OF_MGFX_ROWS = 32;

    /**
     * Width of the largest MGFX picture in the <i>stadtfld.bsh</i>.
     * Can also be read with the BennoFiles class.
     */
    public static final float MAX_MGFX_WIDTH = 32.0f;

    /**
     * Height of the largest MGFX picture in the <i>stadtfld.bsh</i>.
     * Can also be read with the BennoFiles class.
     */
    public static final float MAX_MGFX_HEIGHT = 143.0f;

    //-------------------------------------------------
    // Constants SGFX
    //-------------------------------------------------

    /**
     * Number of SGFX atlas images.
     * 2 images a (64 rows * 64 rows)
     */
    public static final int NR_OF_SGFX_ATLAS_IMAGES = 2;

    /**
     * Number of rows in SGFX atlas image.
     */
    public static final int NR_OF_SGFX_ROWS = 64;

    /**
     * Width of the largest SGFX picture in the <i>stadtfld.bsh</i>.
     * Can also be read with the BennoFiles class.
     */
    public static final float MAX_SGFX_WIDTH = 16.0f;

    /**
     * Height of the largest SGFX picture in the <i>stadtfld.bsh</i>.
     * Can also be read with the BennoFiles class.
     */
    public static final float MAX_SGFX_HEIGHT = 71.0f;

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The list of GFX tile atlas images.
     */
    private static final ArrayList<ImageFile> gfxAtlasImages = new ArrayList<>();

    /**
     * The list of MGFX tile atlas images.
     */
    private static final ArrayList<ImageFile> mgfxAtlasImages = new ArrayList<>();

    /**
     * The list of SGFX tile atlas images.
     */
    private static final ArrayList<ImageFile> sgfxAtlasImages = new ArrayList<>();

    /**
     * The GFX texture array.
     */
    private static Texture gfxTextureArray;

    /**
     * The MGFX texture array.
     */
    private static Texture mgfxTextureArray;

    /**
     * The SGFX texture array.
     */
    private static Texture sgfxTextureArray;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * All methods are static.
     */
    private TileAtlas() {
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    /**
     * Get {@link #gfxTextureArray}.
     *
     * @return {@link #gfxTextureArray}
     */
    public static Texture getGfxTextureArray() {
        return gfxTextureArray;
    }

    /**
     * Get {@link #mgfxTextureArray}.
     *
     * @return {@link #mgfxTextureArray}
     */
    public static Texture getMgfxTextureArray() {
        return mgfxTextureArray;
    }

    /**
     * Get {@link #sgfxTextureArray}.
     *
     * @return {@link #sgfxTextureArray}
     */
    public static Texture getSgfxTextureArray() {
        return sgfxTextureArray;
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * Get the texture offset.
     *
     * @param textureIndex The texture index.
     * @param nrOfRows The number of rows.
     *
     * @return {@link Vector2f}
     */
    public static Vector2f getTextureOffset(int textureIndex, int nrOfRows) {
        return new Vector2f(
                getTextureXOffset(textureIndex, nrOfRows),
                getTextureYOffset(textureIndex, nrOfRows)
        );
    }

    /**
     * Get the x texture offset.
     *
     * @param textureIndex The texture index.
     * @param nrOfRows The number of rows.
     *
     * @return float
     */
    private static float getTextureXOffset(int textureIndex, int nrOfRows) {
        int column = textureIndex % nrOfRows;
        return (float)column / (float)nrOfRows;
    }

    /**
     * Get the y texture offset.
     *
     * @param textureIndex The texture index.
     * @param nrOfRows The number of rows.
     *
     * @return float
     */
    private static float getTextureYOffset(int textureIndex, int nrOfRows) {
        int row = textureIndex / nrOfRows;
        return (float)row / (float)nrOfRows;
    }

    //-------------------------------------------------
    // Textures into Gpu
    //-------------------------------------------------

    static {
        loadGfxAtlasImages();
        loadMGfxAtlasImages();
        loadSGfxAtlasImages();

        createGfxTextureArray();
        createMGfxTextureArray();
        createSGfxTextureArray();
    }

    /**
     * Loads GFX atlas images.
     */
    private static void loadGfxAtlasImages() {
        for (var i = 0; i < NR_OF_GFX_ATLAS_IMAGES; i++) {
            var path = BennoConfig.ATLAS_GFX_PATH + i + ".png";

            ImageFile image = null;
            try {
                image = new ImageFile(path);
            } catch (IOException e) {
                e.printStackTrace();
            }

            gfxAtlasImages.add(image);
        }
    }

    /**
     * Loads MGFX atlas images.
     */
    private static void loadMGfxAtlasImages() {
        for (var i = 0; i < NR_OF_MGFX_ATLAS_IMAGES; i++) {
            var path = BennoConfig.ATLAS_MGFX_PATH + i + ".png";

            ImageFile image = null;
            try {
                image = new ImageFile(path);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mgfxAtlasImages.add(image);
        }
    }

    /**
     * Loads SGFX atlas images.
     */
    private static void loadSGfxAtlasImages() {
        for (var i = 0; i < NR_OF_SGFX_ATLAS_IMAGES; i++) {
            var path = BennoConfig.ATLAS_SGFX_PATH + i + ".png";

            ImageFile image = null;
            try {
                image = new ImageFile(path);
            } catch (IOException e) {
                e.printStackTrace();
            }

            sgfxAtlasImages.add(image);
        }
    }

    /**
     * Create GFX texture array.
     */
    private static void createGfxTextureArray() {
        gfxTextureArray = new Texture();
        Texture.bind(gfxTextureArray.getId(), GL_TEXTURE_2D_ARRAY);

        glTextureStorage3D(
                gfxTextureArray.getId(),
                1,
                GL_RGBA8,
                (int)MAX_GFX_WIDTH * NR_OF_GFX_ROWS,
                (int)MAX_GFX_HEIGHT * NR_OF_GFX_ROWS,
                NR_OF_GFX_ATLAS_IMAGES
        );

        var zOffset = 0;
        for (var atlas : gfxAtlasImages) {
            glTextureSubImage3D(
                    gfxTextureArray.getId(),
                    0,
                    0, 0,
                    zOffset,
                    (int)MAX_GFX_WIDTH * NR_OF_GFX_ROWS,
                    (int)MAX_GFX_HEIGHT * NR_OF_GFX_ROWS,
                    1,
                    GL_BGRA,
                    GL_UNSIGNED_INT_8_8_8_8_REV,
                    atlas.getArgb()
            );

            zOffset++;
        }

        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
    }

    /**
     * Create MGFX texture array.
     */
    private static void createMGfxTextureArray() {
        mgfxTextureArray = new Texture();
        Texture.bind(mgfxTextureArray.getId(), GL_TEXTURE_2D_ARRAY);

        glTextureStorage3D(
                mgfxTextureArray.getId(),
                1,
                GL_RGBA8,
                (int)MAX_MGFX_WIDTH * NR_OF_MGFX_ROWS,
                (int)MAX_MGFX_HEIGHT * NR_OF_MGFX_ROWS,
                NR_OF_MGFX_ATLAS_IMAGES
        );

        var zOffset = 0;
        for (var atlas : mgfxAtlasImages) {
            glTextureSubImage3D(
                    mgfxTextureArray.getId(),
                    0,
                    0, 0,
                    zOffset,
                    (int)MAX_MGFX_WIDTH * NR_OF_MGFX_ROWS,
                    (int)MAX_MGFX_HEIGHT * NR_OF_MGFX_ROWS,
                    1,
                    GL_BGRA,
                    GL_UNSIGNED_INT_8_8_8_8_REV,
                    atlas.getArgb()
            );

            zOffset++;
        }

        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
    }

    /**
     * Create SGFX texture array.
     */
    private static void createSGfxTextureArray() {
        sgfxTextureArray = new Texture();
        Texture.bind(sgfxTextureArray.getId(), GL_TEXTURE_2D_ARRAY);

        glTextureStorage3D(
                sgfxTextureArray.getId(),
                1,
                GL_RGBA8,
                (int)MAX_SGFX_WIDTH * NR_OF_SGFX_ROWS,
                (int)MAX_SGFX_HEIGHT * NR_OF_SGFX_ROWS,
                NR_OF_SGFX_ATLAS_IMAGES
        );

        var zOffset = 0;
        for (var atlas : sgfxAtlasImages) {
            glTextureSubImage3D(
                    sgfxTextureArray.getId(),
                    0,
                    0, 0,
                    zOffset,
                    (int)MAX_SGFX_WIDTH * NR_OF_SGFX_ROWS,
                    (int)MAX_SGFX_HEIGHT * NR_OF_SGFX_ROWS,
                    1,
                    GL_BGRA,
                    GL_UNSIGNED_INT_8_8_8_8_REV,
                    atlas.getArgb()
            );

            zOffset++;
        }

        glBindTexture(GL_TEXTURE_2D_ARRAY, 0);
    }
}
