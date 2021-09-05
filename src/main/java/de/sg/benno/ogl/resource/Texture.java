/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.ogl.resource;

import de.sg.benno.ogl.OglRuntimeException;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;

import static de.sg.benno.ogl.Log.LOGGER;
import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;
import static org.lwjgl.stb.STBImage.*;

/**
 * Represents an OpenGL texture resource.
 */
public class Texture implements Resource {

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The texture file path.
     */
    private String path;

    /**
     * OpenGL expects the 0.0 coordinate on the y-axis to be on the bottom side of the image,
     * but images usually have 0.0 at the top of the y-axis.
     * Setting this option flip the y-axis during image loading.
     */
    private boolean loadVerticalFlipped;

    /**
     * The texture id.
     */
    private int id;

    /**
     * The texture width.
     */
    private int width;

    /**
     * The texture height.
     */
    private int height;

    /**
     * The number of color channels.
     */
    private int nrChannels;

    /**
     * The OpenGL internal format.
     * Corresponds to the number of color components.
     */
    private int format;

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link Texture} object.
     *
     * @param path The texture file path.
     * @param loadVerticalFlipped This option flip the y-axis during image loading.
     */
    public Texture(String path, boolean loadVerticalFlipped) {
        LOGGER.debug("Creates Texture object.");

        this.path = path;
        this.loadVerticalFlipped = loadVerticalFlipped;
    }

    /**
     * Constructs a new {@link Texture} object.
     *
     * @param path The texture file path.
     */
    public Texture(String path) {
        this(path, false);
    }

    //-------------------------------------------------
    // Implement Resource
    //-------------------------------------------------

    @Override
    public void load() throws IOException {
        stbi_set_flip_vertically_on_load(loadVerticalFlipped);

        ByteBuffer buffer;
        var source = Texture.class.getResourceAsStream(path);
        if (source == null) {
            throw new OglRuntimeException("Texture file not found at " + path);
        }

        var rbc = Channels.newChannel(source);
        buffer = createByteBuffer(8 * 1024);

        while (true) {
            var bytes = rbc.read(buffer);

            if (bytes == -1) {
                break;
            }

            if (buffer.remaining() == 0) {
                buffer = resizeBuffer(buffer, buffer.capacity() * 3 / 2);
            }
        }

        buffer.flip();

        ByteBuffer imageBuffer;
        try (var stack = MemoryStack.stackPush()) {
            var x = stack.mallocInt(1);
            var y = stack.mallocInt(1);
            var channels = stack.mallocInt(1);

            imageBuffer = stbi_load_from_memory(buffer, x, y, channels, 0);
            if (imageBuffer == null) {
                throw new OglRuntimeException("Failed to load texture file " + path
                        + System.lineSeparator() + stbi_failure_reason());
            }

            width = x.get();
            height = y.get();
            nrChannels = channels.get();
        }

        if (nrChannels == STBI_grey)
            format = GL_RED;
        else if (nrChannels == STBI_rgb)
            format = GL_RGB;
        else if (nrChannels == STBI_rgb_alpha)
            format = GL_RGBA;

        createId();
        bind();

        glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, format, GL_UNSIGNED_BYTE, imageBuffer);
        glGenerateMipmap(GL_TEXTURE_2D);

        stbi_image_free(imageBuffer);

        LOGGER.debug("Texture file {} was successfully loaded. The Id is {}.", path, id);
    }

    /**
     * Increases the capacity of a {@link ByteBuffer}.
     *
     * @param buffer An existing {@link ByteBuffer}.
     * @param newCapacity The new capacity.
     *
     * @return {@link ByteBuffer}
     */
    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        var newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);

        return newBuffer;
    }

    @Override
    public void createId() {
        id = glGenTextures();
        if (id == 0) {
            throw new OglRuntimeException("Texture name creation has failed.");
        }
    }

    @Override
    public void bind() {
        glBindTexture(GL_TEXTURE_2D, id);
    }

    @Override
    public void cleanUp() {
        if (id > 0) {
            glDeleteTextures(id);
        }
    }

    //-------------------------------------------------
    // Bind / Unbind
    //-------------------------------------------------

    /**
     * Bind the texture to a texture unit (GL_TEXTURE0 to GL_TEXTURE15).
     *
     * @param textureUnit The OpenGL texture unit.
     */
    public void bindForReading(int textureUnit) {
        // make sure that the OpenGL constants are used here
        if (textureUnit < GL_TEXTURE0 || textureUnit > GL_TEXTURE15) {
            throw new OglRuntimeException("Invalid texture unit value.");
        }

        glActiveTexture(textureUnit);
        bind();
    }

    //-------------------------------------------------
    // Static
    //-------------------------------------------------

    public static void bind(int id, int target) {
        glBindTexture(target, id);
    }

    public static void bind(int id) {
        bind(id, GL_TEXTURE_2D);
    }

    public static void unbind(int target) {
        glBindTexture(target, 0);
    }

    public static void unbind() {
        unbind(GL_TEXTURE_2D);
    }

    public static void bindForReading(int id, int textureUnit, int target) {
        // make sure that the OpenGL constants are used here
        if (textureUnit < GL_TEXTURE0 || textureUnit > GL_TEXTURE15) {
            throw new OglRuntimeException("Invalid texture unit value.");
        }

        glActiveTexture(textureUnit);
        bind(id, target);
    }

    public static void bindForReading(int id, int textureUnit) {
        bindForReading(id, textureUnit, GL_TEXTURE_2D);
    }
}
