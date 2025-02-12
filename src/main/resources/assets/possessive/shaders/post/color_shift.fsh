#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

uniform vec3 RedMatrix;
uniform vec3 GreenMatrix;
uniform vec3 BlueMatrix;

uniform vec2 Threshold;

out vec4 fragColor;

void main() {
    vec4 InTexel = texture(InSampler, texCoord);

    // Color Matrix
    if ((InTexel.g > InTexel.r + Threshold.x) || (InTexel.g > InTexel.b + Threshold.y)) {
        float RedValue = dot(InTexel.rgb, RedMatrix);
        float GreenValue = dot(InTexel.rgb, GreenMatrix);
        float BlueValue = dot(InTexel.rgb, BlueMatrix);
        vec3 OutColor = vec3(RedValue, GreenValue, BlueValue);

        fragColor = vec4(OutColor, 1.0);
    } else {
        fragColor = vec4(InTexel.rgb, 1.0);
    }
}