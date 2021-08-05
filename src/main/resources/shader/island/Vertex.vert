#version 430

layout (location = 0) in vec2 aPosition;
layout (location = 1) in vec3 aColor;
layout (location = 2) in vec2 aUv;
layout (location = 3) in mat4 aModelMatrix;
layout (location = 7) in int aTextureIndex;
layout (location = 8) in int aTextureAtlasIndex;
layout (location = 9) in vec2 aOffset;
layout (location = 10) in float aHeight;

out vec2 vUv;
flat out int vTextureAtlasIndex;

uniform mat4 projection;
uniform mat4 view;
uniform float maxY;
uniform float nrOfRows;

void main()
{
    gl_Position = projection * view * aModelMatrix * vec4(aPosition, 0.0, 1.0);

    vUv.x = (aUv.x / nrOfRows) + aOffset.x;
    vUv.y = (aUv.y / nrOfRows) + aOffset.y;

    if (aUv.y == 1.0) {
        vUv.y = ((1.0 / nrOfRows) * aHeight / maxY) + aOffset.y;
    }

    vTextureAtlasIndex = aTextureAtlasIndex;
}
