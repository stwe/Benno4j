#version 330

out vec4 fragColor;

in vec2 vUv;

uniform sampler2D diffuseMap;

void main()
{
    fragColor = texture(diffuseMap, vUv);
}
