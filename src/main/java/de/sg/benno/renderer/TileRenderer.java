package de.sg.benno.renderer;

import de.sg.benno.file.BshTexture;
import de.sg.ogl.Color;
import de.sg.ogl.Log;
import de.sg.ogl.OpenGL;
import de.sg.ogl.SgOglEngine;
import de.sg.ogl.buffer.Vao;
import de.sg.ogl.resource.Geometry;
import de.sg.ogl.resource.Shader;
import de.sg.ogl.resource.Texture;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;

public class TileRenderer {

    private final SgOglEngine engine;

    private final Geometry quadGeometry;
    private final Shader shader;
    private final Vao vao;

    public TileRenderer(SgOglEngine engine) throws Exception {
        this.engine = engine;

        this.quadGeometry = engine.getResourceManager().loadGeometry(Geometry.GeometryId.QUAD_2D);
        this.shader = engine.getResourceManager().loadResource(Shader.class, "sprite");
        this.vao = new Vao();

        this.initVao();
    }

    public void render(BshTexture texture, Vector2f position) {
        OpenGL.enableAlphaBlending();
        shader.bind();
        Texture.bindForReading(texture.getTextureId(), GL_TEXTURE0);

        var size = new Vector2f(texture.getBufferedImage().getWidth(), texture.getBufferedImage().getHeight());

        Matrix4f modelMatrix = new Matrix4f();
        modelMatrix
                .identity()
                .translate(new Vector3f(position, 0.0f))
                .scale(new Vector3f(size, 1.0f));

        shader.setUniform("model", modelMatrix);
        shader.setUniform("projection", new Matrix4f(engine.getWindow().getOrthographicProjectionMatrix()));
        shader.setUniform("diffuseMap", 0);

        vao.bind();
        vao.drawPrimitives(GL_TRIANGLES);
        vao.unbind();

        OpenGL.disableBlending();
        Shader.unbind();
        Texture.unbind();
    }

    private void initVao() {
        vao.addVbo(quadGeometry.vertices, quadGeometry.drawCount, quadGeometry.defaultBufferLayout);
    }

    public void cleanUp() {
        Log.LOGGER.debug("Clean up TileRenderer.");
        this.vao.cleanUp();
    }
}
