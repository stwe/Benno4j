#version 330

out vec4 fragColor;

in vec2 vUv;

uniform sampler2D bottomLayer;
uniform sampler2D shipsLayer;
uniform sampler2D cameraLayer;

void main()
{
    vec4 bottomLayerColor = texture(bottomLayer, vUv);
    vec4 shipsLayerColor = texture(shipsLayer, vUv);
    vec4 cameraLayerColor = texture(cameraLayer, vUv);

    fragColor = bottomLayerColor + shipsLayerColor + (cameraLayerColor * 2.0);
}
