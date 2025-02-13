#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec3 RedMatrix;
uniform vec3 GreenMatrix;
uniform vec3 BlueMatrix;

out vec4 fragColor;

float greeneness(vec3 color) {
    float MaxValue = max(color.r, max(color.g, color.b));
    if (MaxValue == 0) return 0;
    return color.g / MaxValue;
}

void main() {
    vec4 InTexel = texture(InSampler, texCoord);

    float RedValue = dot(InTexel.rgb, RedMatrix);
    float GreenValue = dot(InTexel.rgb, GreenMatrix);
    float BlueValue = dot(InTexel.rgb, BlueMatrix);
    vec3 OutColor = vec3(RedValue, GreenValue, BlueValue);

    float Intencity = pow(greeneness(InTexel.rgb), 2);
    vec3 MixedColor = mix(InTexel.rgb, OutColor.rgb, Intencity);
    fragColor = vec4(MixedColor.rgb, 1.0);
}