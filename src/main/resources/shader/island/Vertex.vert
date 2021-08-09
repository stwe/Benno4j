#version 430

layout (location = 0) in vec2 aPosition;
layout (location = 1) in vec3 aColor;
layout (location = 2) in vec2 aUv;
layout (location = 3) in mat4 aModelMatrix;
layout (location = 7) in ivec4 aAnimInfo;
layout (location = 8) in int aTextureAtlasIndex;
layout (location = 9) in vec2 aOffset;
layout (location = 10) in float aHeight;

out vec2 vUv;
flat out int vTextureAtlasIndex;

uniform mat4 projection;
uniform mat4 view;
uniform float maxY;
uniform float nrOfRows;
uniform int updates;
uniform int delta;

void main()
{
    // (22, 206)

    // 1201
    //   6x
    //  758
    //  130 ms

    // updates alle 16,6 ms erhöht

    // zb 8 * 17 = total time von 136 ms
    int totalTime = updates * delta;

    // get current offset
    vec2 offset = aOffset;

    // get current atlas index
    vTextureAtlasIndex = aTextureAtlasIndex;

    // get anim info
    int currentGfx = aAnimInfo.x;
    int startGfx = aAnimInfo.y;
    int animCount = aAnimInfo.z;
    int frameTime = aAnimInfo.w;

    // cast nrOfRows
    int rows = int(nrOfRows);

    // work out new offset && new atlas index
    if (startGfx == 758) {
        int pic = (totalTime / frameTime) % animCount;
        offset.x = ((startGfx + pic) % rows) / nrOfRows;
        offset.y = ((startGfx + pic) / rows) / nrOfRows;
        vTextureAtlasIndex = ((startGfx + pic) / (rows * rows));
    }

    gl_Position = projection * view * aModelMatrix * vec4(aPosition, 0.0, 1.0);

    vUv.x = (aUv.x / nrOfRows) + offset.x;
    vUv.y = (aUv.y / nrOfRows) + offset.y;

    if (aUv.y == 1.0) {
        vUv.y = ((1.0 / nrOfRows) * aHeight / maxY) + offset.y;
    }
}
