#version 430

layout (location = 0) in vec2 aPosition;
layout (location = 1) in vec3 aColor;
layout (location = 2) in vec2 aUv;
layout (location = 3) in mat4 aModelMatrix;
layout (location = 7) in ivec4 aAnimInfo;
layout (location = 8) in int aTextureAtlasIndex;
layout (location = 9) in vec2 aOffset;
layout (location = 10) in float aHeight;
layout (location = 11) in ivec3 aAnimAdd;

out vec2 vUv;
flat out int vTextureAtlasIndex;

vec2 offset;

uniform mat4 projection;
uniform mat4 view;
uniform float maxY;
uniform float nrOfRows;
uniform int updates;
uniform int delta;

// rendert eine Animation, wenn die Frames einfach nacheinander abgelegt sind
// z.B. Wasserkachel mit GFX 758 + sechs weitere Bilder direkt danach
void renderAnimatedGfx(int gfx) {
    // zb 8 * 17 = total time of 136 ms
    int totalTime = updates * delta;

    // get anim info
    int currentGfx = aAnimInfo.x;
    int startGfx = aAnimInfo.y;
    int animCount = aAnimInfo.z;
    int frameTime = aAnimInfo.w;

    // cast nrOfRows
    int rows = int(nrOfRows);

    // work out new offset && new atlas index
    if (startGfx == gfx) {
        int pic = (totalTime / frameTime) % animCount;

        offset.x = ((startGfx + pic) % rows) / nrOfRows;
        offset.y = ((startGfx + pic) / rows) / nrOfRows;

        vTextureAtlasIndex = ((startGfx + pic) / (rows * rows));
    }
}

// Felder (eigentlich keine animation)
void renderAnimatedTiles1() {
    // zb 8 * 17 = total time of 136 ms
    int totalTime = updates * delta;

    // get anim info
    int currentGfx = aAnimInfo.x;
    int startGfx = aAnimInfo.y;
    int animCount = aAnimInfo.z;
    //int frameTime = aAnimInfo.w;
    int frameTime = 900;

    // get rotation info
    int animAdd = aAnimAdd.x;
    int rot = aAnimAdd.y;
    int orient = aAnimAdd.z;

    // cast nrOfRows
    int rows = int(nrOfRows);

    // work out new offset && new atlas index
    if (animCount > 0 && animAdd == 1 && startGfx != 758) {
        int pic = (totalTime / frameTime) % animCount;

        startGfx += (animAdd * pic) + orient;
        offset.x = ((startGfx) % rows) / nrOfRows;
        offset.y = ((startGfx) / rows) / nrOfRows;

        vTextureAtlasIndex = ((startGfx) / (rows * rows));
    }
}

// Flüsse - Prototyp
void renderAnimatedTiles4() {
    // zb 8 * 17 = total time of 136 ms
    int totalTime = updates * delta;

    // get anim info
    int currentGfx = aAnimInfo.x;
    int startGfx = aAnimInfo.y;
    int animCount = aAnimInfo.z;
    int frameTime = aAnimInfo.w;

    // get rotation info
    int animAdd = aAnimAdd.x;
    int rot = aAnimAdd.y;
    int orient = aAnimAdd.z;

    // cast nrOfRows
    int rows = int(nrOfRows);

    // work out new offset && new atlas index
    if (animCount > 0 && animAdd == 4 && startGfx != 758) {
        int pic = (totalTime / frameTime) % animCount;

        startGfx += (animAdd * pic) + orient;
        offset.x = ((startGfx) % rows) / nrOfRows;
        offset.y = ((startGfx) / rows) / nrOfRows;

        vTextureAtlasIndex = ((startGfx) / (rows * rows));
    }
}

void main()
{
    // get current offset
    offset = aOffset;

    // get current atlas index
    vTextureAtlasIndex = aTextureAtlasIndex;

    renderAnimatedGfx(758);
    //renderAnimatedTiles1();
    renderAnimatedTiles4();

    gl_Position = projection * view * aModelMatrix * vec4(aPosition, 0.0, 1.0);

    vUv.x = (aUv.x / nrOfRows) + offset.x;
    vUv.y = (aUv.y / nrOfRows) + offset.y;

    if (aUv.y == 1.0) {
        vUv.y = ((1.0 / nrOfRows) * aHeight / maxY) + offset.y;
    }
}
