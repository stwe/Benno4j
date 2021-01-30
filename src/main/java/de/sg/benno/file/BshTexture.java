package de.sg.benno.file;

import java.awt.image.BufferedImage;
import java.util.Objects;

public class BshTexture {

    private final BufferedImage bufferedImage;

    /**
     * A texture name/id using glGenTextures.
     */
    private int textureId;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public BshTexture(BufferedImage bufferedImage) {
        this.bufferedImage = Objects.requireNonNull(bufferedImage, "bufferedImage must not be null");
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    public int getTextureId() {
        return textureId;
    }

    //-------------------------------------------------
    // Setter
    //-------------------------------------------------

    public void setTextureId(int textureId) {
        this.textureId = textureId;
    }
}
