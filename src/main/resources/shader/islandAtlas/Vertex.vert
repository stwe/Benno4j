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

void main()
{
    gl_Position = projection * view * model * vec4(aPosition, 0.0, 1.0);
    vUv = (aUv / nrOfRows) + offset;
}
