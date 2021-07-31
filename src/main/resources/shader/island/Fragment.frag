#version 430

out vec4 fragColor;

in vec2 vUv;
flat in int vTextureIndex;

uniform sampler2DArray sampler;

void main()
{
    vec3 uv = vec3(vUv, vTextureIndex);
    fragColor = texture(sampler, uv);
}
