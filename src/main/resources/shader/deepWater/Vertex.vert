#version 430

layout (location = 0) in vec2 aPosition;
layout (location = 1) in vec3 aColor;
layout (location = 2) in vec2 aUv;
layout (location = 3) in mat4 aModelMatrix;
layout (location = 7) in int aTextureIndex;
layout (location = 8) in int aSelected;

out vec3 vColor;
out vec2 vUv;
flat out int vTextureIndex;
flat out int vSelected;

uniform mat4 projection;
uniform mat4 view;

void main()
{
    gl_Position = projection * view * aModelMatrix * vec4(aPosition, 0.0, 1.0);
    vColor = aColor;
    vUv = aUv;
    vTextureIndex = aTextureIndex;
    vSelected = aSelected;
}
