/*
 * This file is part of the Benno4j project.
 *
 * Copyright (c) 2021. stwe <https://github.com/stwe/Benno4j>
 *
 * License: GPLv2
 */

package de.sg.benno.ogl.resource;

import de.sg.benno.ogl.Config;
import de.sg.benno.ogl.OglRuntimeException;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Scanner;

import static de.sg.benno.ogl.Log.LOGGER;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL31.glGetUniformBlockIndex;
import static org.lwjgl.opengl.GL31.glUniformBlockBinding;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;
import static org.lwjgl.opengl.GL40.GL_TESS_CONTROL_SHADER;
import static org.lwjgl.opengl.GL40.GL_TESS_EVALUATION_SHADER;

/**
 * Represents a shader program.
 */
public class ShaderProgram implements Resource {

    //-------------------------------------------------
    // Constants
    //-------------------------------------------------

    private static final String VERTEX_SHADER_FILE_NAME = Config.VERTEX_SHADER_FILE_NAME;
    private static final String TESSELLATION_CONTROL_SHADER_FILE_NAME = Config.TESSELLATION_CONTROL_SHADER_FILE_NAME;
    private static final String TESSELLATION_EVALUATION_SHADER_FILE_NAME = Config.TESSELLATION_EVALUATION_SHADER_FILE_NAME;
    private static final String GEOMETRY_SHADER_FILE_NAME = Config.GEOMETRY_SHADER_FILE_NAME;
    private static final String FRAGMENT_SHADER_FILE_NAME = Config.FRAGMENT_SHADER_FILE_NAME;

    //-------------------------------------------------
    // Types
    //-------------------------------------------------

    /**
     * The different shader types.
     */
    public enum Options
    {
        VERTEX_SHADER,
        TESSELLATION_CONTROL_SHADER,
        TESSELLATION_EVALUATION_SHADER,
        GEOMETRY_SHADER,
        FRAGMENT_SHADER
    }

    //-------------------------------------------------
    // Member
    //-------------------------------------------------

    /**
     * The shader program file path.
     */
    private final String path;

    /**
     * The shader {@link Options} to load.
     */
    private final EnumSet<Options> options;

    /**
     * The handle of the program.
     */
    private int id;

    /**
     * The handle of the vertex shader.
     */
    private int vertexShaderId;

    /**
     * The handle of the tessellation control shader.
     */
    private int tessellationControlShaderId;

    /**
     * The handle of the tessellation evaluation shader.
     */
    private int tessellationEvaluationShaderId;

    /**
     * The handle of the geometry shader.
     */
    private int geometryShaderId;

    /**
     * The handle of the fragment shader.
     */
    private int fragmentShaderId;

    /**
     * A list of the {@link Uniform} objects found.
     */
    private final ArrayList<Uniform> foundUniforms = new ArrayList<>();

    /**
     * Stores Uniform locations.
     */
    private final HashMap<String, Integer> uniforms = new HashMap<>();

    /**
     * Stores Uniform-block locations.
     */
    private final HashMap<String, Integer> uniformBlocks = new HashMap<>();

    //-------------------------------------------------
    // Ctors.
    //-------------------------------------------------

    /**
     * Constructs a new {@link ShaderProgram} object.
     *
     * @param path The shader program file path.
     * @param options The shader {@link Options}.
     */
    ShaderProgram(String path, EnumSet<Options> options) {
        LOGGER.debug("Creates ShaderProgram object.");

        this.path = path;
        this.options = options;
    }

    /**
     * Constructs a new {@link ShaderProgram} object.
     *
     * @param path The shader program file path.
     */
    ShaderProgram(String path) {
        this(path, EnumSet.of(Options.VERTEX_SHADER, Options.FRAGMENT_SHADER));
    }

    //-------------------------------------------------
    // Implement Resource
    //-------------------------------------------------

    @Override
    public void load() throws FileNotFoundException {
        // creates an empty program
        createId();

        var shaderPath = Config.SHADER_PROGRAMS_PATH + path;

        // add VERTEX_SHADER
        if (options.contains(Options.VERTEX_SHADER)) {
            addVertexShader(readFileIntoString(shaderPath + VERTEX_SHADER_FILE_NAME));
        }

        // add TESSELLATION_CONTROL_SHADER
        if (options.contains(Options.TESSELLATION_CONTROL_SHADER)) {
            addTessellationControlShader(readFileIntoString(shaderPath + TESSELLATION_CONTROL_SHADER_FILE_NAME));
        }

        // add TESSELLATION_EVALUATION_SHADER
        if (options.contains(Options.TESSELLATION_EVALUATION_SHADER)) {
            addTessellationEvaluationShader(readFileIntoString(shaderPath + TESSELLATION_EVALUATION_SHADER_FILE_NAME));
        }

        // add GEOMETRY_SHADER
        if (options.contains(Options.GEOMETRY_SHADER)) {
            addGeometryShader(readFileIntoString(shaderPath + GEOMETRY_SHADER_FILE_NAME));
        }

        // add FRAGMENT_SHADER
        if (options.contains(Options.FRAGMENT_SHADER)) {
            addFragmentShader(readFileIntoString(shaderPath + FRAGMENT_SHADER_FILE_NAME));
        }

        linkAndValidateProgram();
        addFoundUniforms();
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void createId() {
        id = glCreateProgram();
        if (id == 0) {
            throw new OglRuntimeException("Shader program creation has failed.");
        }

        LOGGER.debug("A new Shader program was created. The Id is {}.", id);
    }

    @Override
    public void bind() {
        glUseProgram(id);
    }

    @Override
    public void cleanUp() {
        LOGGER.debug("Start clean up for ShaderProgram program {}.", id);

        unbind();

        if (vertexShaderId > 0) {
            glDeleteShader(vertexShaderId);
            LOGGER.debug("Vertex shader {} was deleted.", vertexShaderId);
        }

        if (tessellationControlShaderId > 0) {
            glDeleteShader(tessellationControlShaderId);
            LOGGER.debug("Tessellation control shader {} was deleted.", tessellationControlShaderId);
        }

        if (tessellationEvaluationShaderId > 0) {
            glDeleteShader(tessellationEvaluationShaderId);
            LOGGER.debug("Tessellation evaluation shader {} was deleted.", tessellationEvaluationShaderId);
        }

        if (geometryShaderId > 0) {
            glDeleteShader(geometryShaderId);
            LOGGER.debug("Geometry shader {} was deleted.", geometryShaderId);
        }

        if (fragmentShaderId > 0) {
            glDeleteShader(fragmentShaderId);
            LOGGER.debug("Fragment shader {} was deleted.", fragmentShaderId);
        }

        if (id > 0) {
            glDeleteProgram(id);
            LOGGER.debug("Shader program {} was deleted.", id);
        }
    }

    //-------------------------------------------------
    // Unbind
    //-------------------------------------------------

    /**
     * Unbind shader program.
     */
    public static void unbind() {
        glUseProgram(0);
    }

    //-------------------------------------------------
    // Set uniforms
    //-------------------------------------------------

    /**
     * Sets the uniform variable for specified location.
     *
     * @param uniformName The specified location.
     * @param value Value to set.
     */
    public void setUniform(String uniformName, int value) {
        glUniform1i(uniforms.get(uniformName), value);
    }

    /**
     * Sets the uniform variable for specified location.
     *
     * @param uniformName The specified location.
     * @param value Value to set.
     */
    public void setUniform(String uniformName, float value) {
        glUniform1f(uniforms.get(uniformName), value);
    }

    /**
     * Sets the uniform variable for specified location.
     *
     * @param uniformName The specified location.
     * @param value Value to set.
     */
    public void setUniform(String uniformName, boolean value) {
        // if value == true load 1 else 0 as float
        glUniform1f(uniforms.get(uniformName), value ? 1.0f : 0.0f);
    }

    /**
     * Sets the uniform variable for specified location.
     *
     * @param uniformName The specified location.
     * @param value Value to set.
     */
    public void setUniform(String uniformName, Vector2f value) {
        glUniform2f(uniforms.get(uniformName), value.x, value.y);
    }

    /**
     * Sets the uniform variable for specified location.
     *
     * @param uniformName The specified location.
     * @param value Value to set.
     */
    public void setUniform(String uniformName, Vector3f value) {
        glUniform3f(uniforms.get(uniformName), value.x, value.y, value.z);
    }

    /**
     * Sets the uniform variable for specified location.
     *
     * @param uniformName The specified location.
     * @param value Value to set.
     */
    public void setUniform(String uniformName, Vector4f value) {
        glUniform4f(uniforms.get(uniformName), value.x, value.y, value.z, value.w);
    }

    /**
     * Sets the uniform variable for specified location.
     *
     * @param uniformName The specified location.
     * @param value Value to set.
     */
    public void setUniform(String uniformName, Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(
                    uniforms.get(uniformName),
                    false,
                    value.get(stack.mallocFloat(16))
            );
        }
    }

    //-------------------------------------------------
    // Set uniform block
    //-------------------------------------------------

    public void setUniformBlockBindingPoint(String uniformBlockName, int bindingPoint) {
        glUniformBlockBinding(id, uniformBlocks.get(uniformBlockName), bindingPoint);
    }

    //-------------------------------------------------
    // Create Shader
    //-------------------------------------------------

    /**
     * Creates a shader with specified type.
     *
     * @param shaderType Type of the shader.
     *
     * @return The handle of the shader.
     */
    private static int createShaderObject(int shaderType) {
        var shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new OglRuntimeException("Shader object creation has failed. The type is " + shaderType + ".");
        }

        return shaderId;
    }

    //-------------------------------------------------
    // Helper
    //-------------------------------------------------

    /**
     * Read the shader code into a string.
     *
     * @param path The path to the shader file.
     * @return The shader code as string.
     * @throws FileNotFoundException If the file does not exist.
     */
    private static String readFileIntoString(String path) throws FileNotFoundException {
        String result;

        var in = ShaderProgram.class.getResourceAsStream(path);
        if (in == null) {
            throw new FileNotFoundException("Resource " + path + " not found.");
        }

        try (Scanner scanner = new Scanner(in, java.nio.charset.StandardCharsets.UTF_8.name())) {
            result = scanner.useDelimiter("\\A").next();
        }

        return result;
    }

    /**
     * Add a Vertex Shader.
     *
     * @param shaderCode The shader code.
     */
    private void addVertexShader(String shaderCode) {
        LOGGER.debug("A Vertex Shader is added to program {}.", id);
        vertexShaderId = addShader(shaderCode, GL_VERTEX_SHADER);
        LOGGER.debug("A new Vertex Shader was added to program {}. The Id is {}.", id, vertexShaderId);
    }

    /**
     * Add a Tessellation Control Shader.
     *
     * @param shaderCode The shader code.
     */
    private void addTessellationControlShader(String shaderCode) {
        LOGGER.debug("A Tessellation Control Shader is added to program {}.", id);
        tessellationControlShaderId = addShader(shaderCode, GL_TESS_CONTROL_SHADER);
        LOGGER.debug("A new Tessellation Control Shader was added to program {}. The Id is {}.", id, tessellationControlShaderId);
    }

    /**
     * Add a Tessellation Evaluation Shader.
     *
     * @param shaderCode The shader code.
     */
    private void addTessellationEvaluationShader(String shaderCode) {
        LOGGER.debug("A Tessellation Evaluation Shader is added to program {}.", id);
        tessellationEvaluationShaderId = addShader(shaderCode, GL_TESS_EVALUATION_SHADER);
        LOGGER.debug("A new Tessellation Evaluation Shader was added to program {}. The Id is {}.", id, tessellationEvaluationShaderId);
    }

    /**
     * Add a Geometry Shader.
     *
     * @param shaderCode The shader code.
     */
    private void addGeometryShader(String shaderCode) {
        LOGGER.debug("A Geometry Shader is added to program {}.", id);
        geometryShaderId = addShader(shaderCode, GL_GEOMETRY_SHADER);
        LOGGER.debug("A new Geometry Shader was added to program {}. The Id is {}.", id, geometryShaderId);
    }

    /**
     * Add a Fragment Shader.
     *
     * @param shaderCode The shader code.
     */
    private void addFragmentShader(String shaderCode) {
        LOGGER.debug("A Fragment Shader is added to program {}.", id);
        fragmentShaderId = addShader(shaderCode, GL_FRAGMENT_SHADER);
        LOGGER.debug("A new Fragment Shader was added to program {}. The Id is {}.", id, fragmentShaderId);
    }

    /**
     * Attaches a shader object to this program.
     *
     * @param shaderCode The shader code.
     * @param shaderType The shader type.
     *
     * @return The handle of the shader.
     */
    private int addShader(String shaderCode, int shaderType) {
        var shaderId = createShaderObject(shaderType);

        compileShader(shaderId, shaderCode);
        checkCompileStatus(shaderId);
        glAttachShader(id, shaderId);

        findStructs(shaderCode);
        findUniforms(shaderCode);

        return shaderId;
    }

    /**
     * Compiles a shader object.
     *
     * @param shaderId The handle of the shader.
     * @param shaderCode The shader code.
     */
    private static void compileShader(int shaderId, String shaderCode) {
        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);
    }

    /**
     * Checks compile status.
     *
     * @param shaderId The handle of the shader.
     */
    private static void checkCompileStatus(int shaderId) {
        var status = glGetShaderi(shaderId, GL_COMPILE_STATUS);
        if (status == GL_FALSE) {
            throw new OglRuntimeException("Error while compiling Shader code. Log: " + glGetShaderInfoLog(shaderId, 1024));
        }
    }

    /**
     * Links the shader program.
     */
    private void linkAndValidateProgram() {
        // link
        glLinkProgram(id);
        var status = glGetProgrami(id, GL_LINK_STATUS);
        if (status == GL_FALSE) {
            throw new OglRuntimeException("Error while linking Shader program. Log: " + glGetProgramInfoLog(id, 1024));
        }

        // always detach shaders after a successful link
        if (vertexShaderId != 0)
        {
            glDetachShader(id, vertexShaderId);
        }

        if (tessellationControlShaderId != 0)
        {
            glDetachShader(id, tessellationControlShaderId);
        }

        if (tessellationEvaluationShaderId != 0)
        {
            glDetachShader(id, tessellationEvaluationShaderId);
        }

        if (geometryShaderId != 0)
        {
            glDetachShader(id, geometryShaderId);
        }

        if (fragmentShaderId != 0)
        {
            glDetachShader(id, fragmentShaderId);
        }

        // validate
        glValidateProgram(id);
        status = glGetProgrami(id, GL_VALIDATE_STATUS);
        if (status == GL_FALSE ) {
            LOGGER.warn("Warning validating Shader code. Log: " + glGetProgramInfoLog(id, 1024));
        }
    }

    //-------------------------------------------------
    // Uniforms
    //-------------------------------------------------

    private void findStructs(String shaderCode) {
        // todo
    }

    /**
     * Searches for all uniforms in the shader code.
     *
     * @param shaderCode The shader code.
     */
    private void findUniforms(String shaderCode) {
        final String uniformKeyword = "uniform";

        var positions = findAllOccurances(shaderCode, uniformKeyword);

        var uniformPositions = new ArrayList<Integer>();
        var uniformBlockPositions = new ArrayList<Integer>();

        if (!positions.isEmpty()) {
            for (var position : positions) {
                var blockStart = shaderCode.indexOf("{", position);
                var commandEnd = shaderCode.indexOf(";", position);

                if (blockStart < commandEnd) {
                    uniformBlockPositions.add(position);
                } else {
                    uniformPositions.add(position);
                }
            }
        }

        // uniforms
        if (!uniformPositions.isEmpty()) {
            LOGGER.debug("{} uniforms were found in the Shader.", uniformPositions.size());

            var i = 1;
            for (var position : uniformPositions) {
                var commandEnd = shaderCode.indexOf(";", position);

                var typeStartPosition = position + uniformKeyword.length() + 1;
                var typeEndPosition = shaderCode.indexOf(" ", typeStartPosition);

                var uniform = new Uniform();
                uniform.isUniformBlock = false;
                uniform.name = shaderCode.substring(typeEndPosition + 1, commandEnd);
                uniform.type = shaderCode.substring(typeStartPosition, typeEndPosition);
                foundUniforms.add(uniform);

                LOGGER.debug("Uniform #{} with type: {} and name: {}", i, uniform.type, uniform.name);
                i++;
            }
        }

        // uniform blocks
        if (!uniformBlockPositions.isEmpty()) {
            LOGGER.debug("{} uniform blocks were found in the Shader.", uniformBlockPositions.size());

            var i = 1;
            for (var position : uniformBlockPositions) {
                var blockStart = shaderCode.indexOf("{", position);
                var nameStartPosition = position + uniformKeyword.length() + 1;

                var name = shaderCode.substring(nameStartPosition, blockStart);

                var uniform = new Uniform();
                uniform.isUniformBlock = true;
                uniform.name = name.trim().replace(System.lineSeparator(), "");
                foundUniforms.add(uniform);

                LOGGER.debug("Uniform block #{} with name: {}", i, uniform.name);
                i++;
            }
        }
    }

    /**
     * Find all occurrences/positions of a sub string in a given string.
     *
     * @param text The given string.
     * @param str The substring to search for.
     *
     * @return All positions.
     */
    private ArrayList<Integer> findAllOccurances(String text, String str) {
        int index = 0;

        var positions = new ArrayList<Integer>();

        while (true) {
            index = text.indexOf(str, index);
            if (index != -1) {
                positions.add(index);
                index += str.length();
            } else {
                break;
            }
        }

        return positions;
    }

    /**
     * Create and store Uniform locations.
     */
    private void addFoundUniforms() {
        for (var uniform : foundUniforms) {
            if (uniform.isUniformBlock) {
                var uniformBlockId = glGetUniformBlockIndex(id, uniform.name);
                if (uniformBlockId < 0) {
                    throw new OglRuntimeException("Invalid uniform block name: " + uniform.name + ".");
                }

                uniformBlocks.put(uniform.name, uniformBlockId);
            } else {
                var uniformId = glGetUniformLocation(id, uniform.name);
                if (uniformId < 0) {
                    throw new OglRuntimeException("Invalid uniform name: " + uniform.name + ".");
                }

                uniforms.put(uniform.name, uniformId);
            }
        }
    }
}
