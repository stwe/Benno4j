#version 430

out vec4 fragColor;

in vec2 vUv;
flat in int vTextureAtlasIndex;

uniform sampler2DArray sampler;

void main()
{
    vec3 uv = vec3(vUv, vTextureAtlasIndex);
    fragColor = texture(sampler, uv);
}
