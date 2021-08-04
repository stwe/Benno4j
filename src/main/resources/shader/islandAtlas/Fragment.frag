#version 430

out vec4 fragColor;

in vec2 vUv;

uniform sampler2DArray sampler;

void main()
{
    // index = 0
    vec3 uv = vec3(vUv, 0);
    fragColor = texture(sampler, uv);
}
