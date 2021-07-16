#version 430

out vec4 fragColor;

in vec3 vColor;
in vec2 vUv;
flat in int vTextureIndex;

//niform sampler2DArray sampler;

void main()
{
    vec3 uv = vec3(vUv, vTextureIndex);
    //fragColor = texture(sampler, uv);
    fragColor = vec4(1.0, 0.1, 0.1, 1.0);
}
