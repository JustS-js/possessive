#version 150

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

uniform sampler2D depthtex0;

in vec2 texCoord;
in vec2 oneTexel;

vec3 FogColor = vec3(0.1, 0.1, 0.1);

out vec4 fragColor;

float greeneness(vec3 color) {
    float MaxValue = max(color.r, max(color.g, color.b));
    if (MaxValue == 0) return 0;
    return color.g / MaxValue;
}

float foggyness(vec2 coord) {

    float fog = 1;
    /*fog = pow(fog / 1.001, 30.0) + 0.1; //make it sharp
    fog = clamp(fog, 0.0, 1.0); // no more than 1 and 0*/
    return fog;
}

float depth(vec2 coord) {
    float near = 5;
    float far = 20;
    return 2.0 * near * far / (far + near - (2.0 * texture(InDepthSampler, coord).r - 1.0) * (far - near)) / far;
}

float linearizeDepth(vec2 coord) {
    float near = 0.05;
    float far = 256;
    float depth = texture(InDepthSampler, coord).r * 2 - 1;
    return (near * far) / (depth * (near - far) + far);
}

void main() {
    vec4 InTexel = texture(InSampler, texCoord);

    vec3 Monochrome = vec3(0.2126,0.7512,0.0722);
    float GrayScaledColor = dot(InTexel.rgb, Monochrome);
    vec3 OutColor = vec3(GrayScaledColor, GrayScaledColor, GrayScaledColor);//vec3(RedValue, GreenValue, BlueValue);

    float Intencity = pow(greeneness(InTexel.rgb), 2);
    vec3 MixedColor = mix(InTexel.rgb, OutColor.rgb, Intencity);
    vec3 FoggyColor = mix(MixedColor, FogColor, depth(texCoord));

    float m = linearizeDepth(texCoord) / 256;
    fragColor = vec4(MixedColor.rgb, 1.0);
}