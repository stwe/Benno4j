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
layout (location = 12) in int aSelected;

//-------------------------------------------------
// Out
//-------------------------------------------------

out vec2 vUv;
flat out int vTextureAtlasIndex;
out float skipTile;
flat out int vSelected;

//-------------------------------------------------
// Globals
//-------------------------------------------------

vec2 uvOffset;
int rows;

int currentGfx;
int startGfx;
int animCount;
int frameTime;

int animAdd;
int rotation;
int orientation;

int totalTime;
int frame;

//-------------------------------------------------
// Uniforms
//-------------------------------------------------

uniform mat4 projection;
uniform mat4 view;
uniform float maxY;
uniform float nrOfRows;
uniform int updates;
uniform int delta;
uniform float showGrid;

//-------------------------------------------------
// Helper
//-------------------------------------------------

vec2 calcUvOffset(int gfx) {
    return vec2(
        (gfx % rows) / nrOfRows,
        (gfx / rows) / nrOfRows
    );
}

int calcTextureAtlasIndex(int gfx) {
    return (gfx / (rows * rows));
}

//-------------------------------------------------
// Beach
//-------------------------------------------------

// todo
void animateBeach() {
    if (startGfx >= 680 && startGfx <= 811) {
        int gfxOffset = (totalTime / frameTime) % animCount;
        int gfx = startGfx + gfxOffset;

        uvOffset = calcUvOffset(gfx);
        vTextureAtlasIndex = calcTextureAtlasIndex(gfx);
    }
}

//-------------------------------------------------
// River
//-------------------------------------------------

// todo
void animateRiver() {
    if (animCount > 0 && animAdd == 4 && startGfx != 758) {
        int gfxOffset = (totalTime / frameTime) % animCount;
        gfxOffset *= animAdd;
        gfxOffset += orientation;

        int gfx = startGfx + gfxOffset;

        uvOffset = calcUvOffset(gfx);
        vTextureAtlasIndex = calcTextureAtlasIndex(gfx);
    }
}

//-------------------------------------------------
// River Corner
//-------------------------------------------------

void animateRiverCorner() {
    /*
    ausgehend vom Start, bei welchem Animationsschritt sind wir gerade?
    Überlauf verhindern; steht auch irgendwo in den Tiles, kann aber
    berechnet werden

    Test:

    also wenn Start Id = 1648 und current frame 1691

    gegeben: 16 x 6 Animationen

    start bei 1648 + 16 = 1664

    1691 - 1664 = 27
    27 % 16 = 11
    --> 1648 + 11 = 1659

    1691 - 1659 = 16x
    32 = 16x
    --> x = 2

    Ergebnisse: Start bei 1659 --> 1691 ist der 2te Frame
    */

    if (startGfx == 1648 || startGfx == 1744) {
        int startOffset = startGfx + animAdd;
        int diff = currentGfx - startOffset;
        int rest = diff % animAdd;
        int start = startGfx + rest;

        frame *= animAdd;
        int gfx = start + frame;

        uvOffset = calcUvOffset(gfx);
        vTextureAtlasIndex = calcTextureAtlasIndex(gfx);
    }
}

//-------------------------------------------------
// Main
//-------------------------------------------------

void main()
{
    // render all tiles
    skipTile = 0.0;

    // cast
    rows = int(nrOfRows);

    // get anim info
    currentGfx = aAnimInfo.x;
    startGfx = aAnimInfo.y;
    animCount = aAnimInfo.z;
    frameTime = aAnimInfo.w;

    // get rotation info
    animAdd = aAnimAdd.x;
    rotation = aAnimAdd.y;
    orientation = aAnimAdd.z;

    // zb 8 * 17 = total time of 136 ms
    totalTime = updates * delta;
    frame = (totalTime / frameTime) % animCount;

    // get current offset
    uvOffset = aOffset;

    // get current atlas index
    vTextureAtlasIndex = aTextureAtlasIndex;

    // animate beach area
    animateBeach();

    // animate river
    animateRiver();

    // animate river corner
    animateRiverCorner();

    // render rest
    gl_Position = projection * view * aModelMatrix * vec4(aPosition, 0.0, 1.0);

    vUv.x = (aUv.x / nrOfRows) + uvOffset.x;
    vUv.y = (aUv.y / nrOfRows) + uvOffset.y;

    // "cheat" uv for showing a simple grid
    if (showGrid > 0.5) {
        vUv.y += 0.0001;
    }

    if (aUv.y == 1.0) {
        vUv.y = ((1.0 / nrOfRows) * aHeight / maxY) + uvOffset.y;
    }

    vSelected = aSelected;
}
