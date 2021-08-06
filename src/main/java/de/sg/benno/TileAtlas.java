package de.sg.benno;

import de.sg.benno.file.ImageFile;
import de.sg.ogl.resource.Texture;

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
    // Constants
    //-------------------------------------------------

    /**
     * Number of GFX atlas images.
     * 24 images a (16 rows * 16 rows) pics = 6144
     */
    public static final int NR_OF_GFX_ATLAS_IMAGES = 24;

    /**
     * Number of rows in GFX atlas image.
     */
    public static final int NR_OF_GFX_ROWS = 16;

    /**
     * Width of the largest picture in the <i>stadtfld.bsh</i>.
     * Can also be read with the BennoFiles class.
     */
    public static final float MAX_GFX_WIDTH = 64.0f;

    /**
     * Height of the largest picture in the <i>stadtfld.bsh</i>.
     * Can also be read with the BennoFiles class.
     */
    public static final float MAX_GFX_HEIGHT = 286.0f;

    /**
     * The path to GFX atlas images.
     */
    public static final String ATLAS_GFX_PATH = "atlas/GFX/";

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The list of GFX tile atlas images.
     */
    private static final ArrayList<ImageFile> gfxAtlasImages = new ArrayList<>();

    /**
     * The texture array Id.
     */
    private static int gfxTextureArrayId;

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

    //-------------------------------------------------
    // Textures into Gpu
    //-------------------------------------------------

    static {
        loadGfxAtlasImages();
        createTextureArray();
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
     * Create GFX texture array.
     */
    private static void createTextureArray() {
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
}
