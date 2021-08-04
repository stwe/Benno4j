#version 430

layout (location = 0) in vec2 aPosition;
layout (location = 1) in vec3 aColor;
layout (location = 2) in vec2 aUv;

out vec2 vUv;

uniform mat4 model;
uniform mat4 view;
uniform mat4 projection;

uniform float nrOfRows;
uniform vec2 offset;

/*
(0, 1) --------- (1, 1)
  |                |
  |                |
  |                |
  |                |
(0, 0) --------- (1, 0)
*/

/*

0)
1)  x = 0.065, y = 0.0
2)  x = 0.125, y = 0.0

16) x =  0.0, y = 0.0625

*/


void main()
{
    gl_Position = projection * view * model * vec4(aPosition, 0.0, 1.0);

    vUv.x = (aUv.x / nrOfRows) + offset.x;
    vUv.y = (aUv.y / nrOfRows) + offset.y; // 286 statt 32

    /*
    tx = 0 -> 0.065 Abstand entspricht 32 Pixel
    tx = 1 -> 0.125

    ty = 0 -> 0
    ty = 1 -> 0,065 entsprechen 286 pixel
    */

    if (vUv.y > 0.05) {
        vUv.y = (1.0 / nrOfRows) * 32 / 286;
    }
}
