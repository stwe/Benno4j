package de.sg.benno.file;

import de.sg.benno.BennoRuntimeException;
import de.sg.benno.Util;
import de.sg.benno.chunk.Chunk;
import de.sg.ogl.resource.Texture;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

import static de.sg.ogl.Log.LOGGER;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;

public class BshFile extends BinaryFile {

    private static class TextureHeader {
        final BufferedImage image;
        final int type;
        final int offset;

        TextureHeader(BufferedImage image, int type, int offset) {
            this.image = image;
            this.type = type;
            this.offset = offset;
        }
    }

    private static final int NUMBER_OF_CHUNKS = 1;
    private static final String CHUNK_ID = "BSH";

    private static final int END_MARKER = 255;
    private static final int END_OF_ROW = 254;

    private static final String OUTPUT_DIR = "out";

    private final int[] palette;

    private final boolean saveAsPng;

    private final ArrayList<Integer> offsets = new ArrayList<>();
    private final ArrayList<BshTexture> bshTextures = new ArrayList<>();
    private final HashSet<Integer> possibleInvalidOffsets = new HashSet<>();

    private int maxX = -999;
    private int maxY = -999;

    private final Chunk chunk0;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    public BshFile(Path path, int[] palette, boolean saveAsPng) throws IOException {
        super(Objects.requireNonNull(path, "path must not be null"));

        LOGGER.debug("Creates BshFile object from file {}.", path);

        this.palette = Objects.requireNonNull(palette, "palette must not be null");
        this.saveAsPng = saveAsPng;

        if (getNumberOfChunks() != NUMBER_OF_CHUNKS) {
            throw new BennoRuntimeException("Invalid number of Chunks.");
        }

        if (!chunkIndexHasId(0, CHUNK_ID)) {
            throw new BennoRuntimeException("Invalid Chunk Id.");
        }

        chunk0 = getChunk(0);

        readDataFromChunks();
    }

    public BshFile(Path path, int[] palette) throws IOException {
        this(path, palette, false);
    }

    //-------------------------------------------------
    // Getter
    //-------------------------------------------------

    public ArrayList<BshTexture> getBshTextures() {
        return bshTextures;
    }

    public int getMaxX() {
        return maxX;
    }

    public int getMaxY() {
        return maxY;
    }

    //-------------------------------------------------
    // BinaryFileInterface
    //-------------------------------------------------

    @Override
    public void readDataFromChunks() throws IOException {
        LOGGER.debug("Start reading BSH data from Chunks...");

        readOffsets();
        validateOffsets();
        decodeTextures();
        createGlTextures();
        setMaxValues();

        LOGGER.debug("BSH data read successfully.");
    }

    //-------------------------------------------------
    // Clean up
    //-------------------------------------------------

    public void cleanUp() {
        LOGGER.debug("Clean up OpenGL textures.");

        var i = 0;
        for (var texture : bshTextures) {
            if (texture.getTextureId() > 0) {
                glDeleteTextures(texture.getTextureId());
                i++;
            }
        }

        LOGGER.debug("{} OpenGL textures was deleted.", i);
    }

    //-------------------------------------------------
    // Offsets
    //-------------------------------------------------

    private void readOffsets() {
        // get offset of the first texture
        var texturesStartOffset = chunk0.getData().getInt();
        offsets.add(texturesStartOffset);

        // add remaining offsets
        for (var offset = chunk0.getData().getInt(); chunk0.getData().position() <= texturesStartOffset; offset = chunk0.getData().getInt()) {
            offsets.add(offset);
        }

        LOGGER.debug("Detected {} texture offsets.", offsets.size());
    }

    private void validateOffsets() {
        for (int i = 0; i < offsets.size(); i++) {
            if (i + 1 < offsets.size()) {
                var offset = offsets.get(i);
                var nextOffset = offsets.get(i + 1);
                if (nextOffset - offset == 20) {
                    possibleInvalidOffsets.add(offset);
                }
            }
        }

        if (!possibleInvalidOffsets.isEmpty()) {
            LOGGER.warn("Detected {} possible invalid texture offsets.", possibleInvalidOffsets.size());
        }
    }

    //-------------------------------------------------
    // Texture
    //-------------------------------------------------

    private void decodeTextures() throws IOException {
        for (var offset : offsets) {
            if (!possibleInvalidOffsets.contains(offset)) {
                chunk0.getData().position(offset);

                var textureHeader = readTextureHeader(offset);

                if (textureHeader.type == 13) {
                    decodeTexture13(textureHeader);
                } else {
                    decodeTexture(textureHeader);
                }
            }
        }

        LOGGER.debug("A total of {} bsh textures were created.", bshTextures.size());
    }

    private void decodeTexture13(TextureHeader textureHeader) throws IOException {
        int x = 0;
        int y = 0;

        while (true) {
            int numAlpha = chunk0.getData().getInt();

            if (numAlpha == END_MARKER) {
                break;
            }

            if (numAlpha == END_OF_ROW) {
                x = 0;
                y++;
                continue;
            }

            for (int i = 0; i < numAlpha; i++) {
                textureHeader.image.setRGB(x, y, 0);
                x++;
            }

            int numPixels = chunk0.getData().getInt();
            for (int i = 0; i < numPixels; i++) {
                var b = chunk0.getData().get();
                var g = chunk0.getData().get();
                var r = chunk0.getData().get();
                var a = chunk0.getData().get();

                var color = Util.rgbToInt(Util.byteToInt(r), Util.byteToInt(g), Util.byteToInt(b));

                textureHeader.image.setRGB(x, y, color);
                x++;
            }
        }

        if (saveAsPng) {
            saveAsPng(textureHeader.image, textureHeader.offset);
        }

        var bshTexture = new BshTexture(textureHeader.image);
        bshTextures.add(bshTexture);
    }

    private void decodeTexture(TextureHeader textureHeader) throws IOException {
        int x = 0;
        int y = 0;

        while (true) {
            int numAlpha = Util.byteToInt(chunk0.getData().get());

            // if the next byte is 0xFF the image has reached it’s end
            if (numAlpha == END_MARKER) {
                break;
            }

            // if the next byte is 0xFE the current pixel line has reached it’s end
            if (numAlpha == END_OF_ROW) {
                x = 0;
                y++;
                continue;
            }

            // ... else it’s an Texture Chunk
            // number of pixels to skip / number of transparent pixels
            for (int i = 0; i < numAlpha; i++) {
                textureHeader.image.setRGB(x, y, 0);
                x++;
            }

            // number of pixels that follow
            int numPixels = Util.byteToInt(chunk0.getData().get());
            for (int i = 0; i < numPixels; i++) {
                // pixels to insert at current position
                var colorIndex = Util.byteToInt(chunk0.getData().get());
                var color = palette[colorIndex];
                textureHeader.image.setRGB(x, y, color);
                x++;
            }
        }

        if (saveAsPng) {
            saveAsPng(textureHeader.image, textureHeader.offset);
        }

        var bshTexture = new BshTexture(textureHeader.image);
        bshTextures.add(bshTexture);
    }

    private void createGlTextures() {
        for (var bshTexture : bshTextures) {
            var dbb = (DataBufferInt) bshTexture.getBufferedImage().getRaster().getDataBuffer();

            var id = Texture.generateNewTextureId();
            Texture.bind(id);
            Texture.useNoFilter();

            bshTexture.setTextureId(id);

            glTexImage2D(
                    GL_TEXTURE_2D,
                    0,
                    GL_RGBA8,
                    bshTexture.getBufferedImage().getWidth(),
                    bshTexture.getBufferedImage().getHeight(),
                    0,
                    GL_BGRA,
                    GL_UNSIGNED_INT_8_8_8_8_REV,
                    dbb.getData()
            );

            Texture.unbind();
        }
    }

    private TextureHeader readTextureHeader(int offset) {
        // with && height
        var width = chunk0.getData().getInt();
        var height = chunk0.getData().getInt();

        if (width <= 0 || height <= 0) {
            throw new BennoRuntimeException("Invalid width or height.");
        }

        // type
        var type = chunk0.getData().getInt();

        // length
        var length = chunk0.getData().getInt();

        return new TextureHeader(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB), type, offset);
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    private void saveAsPng(BufferedImage image, int offset) throws IOException {
        Files.createDirectories(Paths.get(OUTPUT_DIR));

        var bshFilename = getPath().getFileName().toString().toLowerCase();

        String filename = OUTPUT_DIR + "/" +
                bshFilename.substring(0, bshFilename.lastIndexOf(".")) + "_" +
                offset + ".png";

        File file = new File(filename);
        if (!file.exists()) {
            var result = file.createNewFile();
            if (!result) {
                throw new BennoRuntimeException("Unexpected error.");
            }
        }

        ImageIO.write(image, "PNG", file);
    }

    private void setMaxValues() {
        for (var bshTexture : bshTextures) {
            if (bshTexture.getBufferedImage().getWidth() > maxX) {
                maxX = bshTexture.getBufferedImage().getWidth();
            }

            if (bshTexture.getBufferedImage().getHeight() > maxY) {
                maxY = bshTexture.getBufferedImage().getHeight();
            }
        }
    }
}
