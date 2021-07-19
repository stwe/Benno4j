#version 430

out vec4 fragColor;

in vec3 vColor;
in vec2 vUv;

uniform sampler2DArray sampler;
uniform int textureIndex;

void main()
{
    vec3 uv = vec3(vUv, textureIndex);
    fragColor = texture(sampler, uv);
    //fragColor = vec4(vColor, 1.0);
}
