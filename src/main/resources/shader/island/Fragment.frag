#version 430

//-------------------------------------------------
// Out
//-------------------------------------------------

out vec4 fragColor;

//-------------------------------------------------
// In
//-------------------------------------------------

in vec2 vUv;
flat in int vTextureAtlasIndex;
in float skipTile;
flat in int vSelected;

//-------------------------------------------------
// Uniforms
//-------------------------------------------------

uniform sampler2DArray sampler;

//-------------------------------------------------
// Main
//-------------------------------------------------

void main()
{
    if (skipTile == 1.0) {
        discard;
    }

    vec3 uv = vec3(vUv, vTextureAtlasIndex);
    fragColor = texture(sampler, uv);

    fragColor.r /= vSelected;
    fragColor.g /= vSelected;
    fragColor.b /= vSelected;
}
