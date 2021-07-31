#version 430

layout (location = 0) in vec2 aPosition;
layout (location = 1) in vec3 aColor;
layout (location = 2) in vec2 aUv;
layout (location = 3) in mat4 aModelMatrix;
layout (location = 7) in int aTextureIndex;
layout (location = 8) in float aTextureHeight;

out vec2 vUv;
flat out int vTextureIndex;

uniform mat4 projection;
uniform mat4 view;
uniform float maxY;

void main()
{
    gl_Position = projection * view * aModelMatrix * vec4(aPosition, 0.0, 1.0);

    vUv = aUv;
    if (vUv.y == 1.0) {
        vUv.y = aTextureHeight / maxY;
    }

    vTextureIndex = aTextureIndex;
}
