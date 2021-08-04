#version 430

out vec4 fragColor;

in vec2 vUv;

uniform sampler2DArray sampler;

/*
(0, 1) --------- (1, 1)
  |                |
  |                |
  |                |
  |                |
(0, 0) --------- (1, 0)
*/

void main()
{
    // index = 0
    vec3 uv = vec3(vUv, 0);
    fragColor = texture(sampler, uv);
}
