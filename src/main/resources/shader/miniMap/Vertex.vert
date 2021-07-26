#version 330

layout (location = 0) in vec2 aPosition;
layout (location = 1) in vec3 aColor;
layout (location = 2) in vec2 aUv;
layout (location = 3) in mat4 aModelMatrix;
layout (location = 7) in vec3 aTileColor;

out vec3 vColor;

void main()
{
    gl_Position = aModelMatrix * vec4(aPosition, 0.0, 1.0);
    vColor = aTileColor;
}
