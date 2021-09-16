#version 330

layout (location = 0) in vec2 aPosition;
layout (location = 1) in vec2 aUv;

out vec2 vUv;

uniform mat4 model;

void main()
{
    gl_Position = model * vec4(aPosition.x, 1.0 - aPosition.y, 0.0, 1.0);
    vUv = aUv;
}
