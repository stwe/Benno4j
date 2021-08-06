package de.sg.benno;

import de.sg.benno.file.ImageFile;
import de.sg.ogl.resource.Texture;
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

public final class TileAtlas {

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

    /**
     * The path to GFX atlas images.
     */
    public static final String ATLAS_GFX_PATH = "atlas/GFX/";

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

    /**
     * The path to MGFX atlas images.
     */
    public static final String ATLAS_MGFX_PATH = "atlas/MGFX/";

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

    /**
     * The path to SGFX atlas images.
     */
    public static final String ATLAS_SGFX_PATH = "atlas/SGFX/";

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
     * The GFX texture array Id.
     */
    private static int gfxTextureArrayId;

    /**
     * The MGFX texture array Id.
     */
    private static int mgfxTextureArrayId;

    /**
     * The SGFX texture array Id.
     */
    private static int sgfxTextureArrayId;

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
     * Get {@link #gfxTextureArrayId}.
     *
     * @return {@link #gfxTextureArrayId}
     */
    public static int getGfxTextureArrayId() {
        return gfxTextureArrayId;
    }

    /**
     * Get {@link #mgfxTextureArrayId}.
     *
     * @return {@link #mgfxTextureArrayId}
     */
    public static int getMgfxTextureArrayId() {
        return mgfxTextureArrayId;
    }

    /**
     * Get {@link #sgfxTextureArrayId}.
     *
     * @return {@link #sgfxTextureArrayId}
     */
    public static int getSgfxTextureArrayId() {
        return sgfxTextureArrayId;
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
            var path = ATLAS_GFX_PATH + i + ".png";

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

    }

    /**
     * Loads SGFX atlas images.
     */
    private static void loadSGfxAtlasImages() {

    }

    /**
     * Create GFX texture array.
     */
    private static void createGfxTextureArray() {
        gfxTextureArrayId = Texture.generateNewTextureId();
        Texture.bind(gfxTextureArrayId, GL_TEXTURE_2D_ARRAY);

        glTextureStorage3D(
                gfxTextureArrayId,
                1,
                GL_RGBA8,
                (int)MAX_GFX_WIDTH * NR_OF_GFX_ROWS,
                (int)MAX_GFX_HEIGHT * NR_OF_GFX_ROWS,
                NR_OF_GFX_ATLAS_IMAGES
        );

        var zOffset = 0;
        for (var atlas : gfxAtlasImages) {
            glTextureSubImage3D(
                    gfxTextureArrayId,
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

    }

    /**
     * Create SGFX texture array.
     */
    private static void createSGfxTextureArray() {

    }
}
