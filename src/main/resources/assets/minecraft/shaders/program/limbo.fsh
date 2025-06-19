#version 150

uniform sampler2D MainSampler;
uniform sampler2D MainDepthSampler;

uniform float Time;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

float noise(vec2 coord) {
    return fract( sin(coord.x*111.+ coord.y*532.) * 2556. * cos(Time / 50));
}
vec3 smoothNoise(vec2 coord) {
    vec2 local_coord = fract(coord);
    // smooth
    local_coord = local_coord*local_coord*(3.-2.*local_coord);

    vec2 id = floor(coord);

    float bottom_left = noise(id);
    float bottom_right = noise(id+vec2(1,0));
    float bottom = mix(bottom_left, bottom_right, local_coord.x);

    float top_left = noise(id+vec2(0,1));
    float top_right = noise(id+vec2(1,1));
    float top = mix(top_left, top_right, local_coord.x);

    float interpolate = mix(bottom, top, local_coord.y);
    return vec3(interpolate);
}

vec3 perlin(vec2 coord) {
    vec3 color = smoothNoise(coord * 4.);
    color += smoothNoise(coord * 8.) * .5;
    color += smoothNoise(coord * 16.) * .25;
    color += smoothNoise(coord * 32.) * .125;
    color += smoothNoise(coord * 64.) * .0625;

    color /= 2;
    return clamp(color + clamp(cos(Time / 100 * 3.14 * 2), -0.3, 0.3), 0, 1);
}

float greeneness(vec3 color) {
    float MaxValue = max(color.r, max(color.g, color.b));
    if (MaxValue == 0) return 0;
    return color.g / MaxValue;
}

float linearizeDepth(vec2 coord, float near, float far) {
    float depth = texture(MainDepthSampler, coord).r;
    return (near * far) / (depth * (near - far) + far) / far;
}

void main() {

    vec4 InTexel = texture(MainSampler, texCoord);

    vec3 Monochrome = vec3(0.2126,0.7512,0.0722);
    float GrayScaledColorValue = dot(InTexel.rgb, Monochrome);
    vec3 GrayScaledColor = vec3(GrayScaledColorValue, GrayScaledColorValue, GrayScaledColorValue);//vec3(RedValue, GreenValue, BlueValue);

    float Intencity = pow(greeneness(InTexel.rgb), 3);
    vec3 ImportantColor = mix(InTexel.rgb, GrayScaledColor.rgb, Intencity);

    vec3 PerlinColor = perlin(texCoord);

    vec3 DarkenDepthMap = vec3(pow(vec3(1. - linearizeDepth(texCoord, 0.05, 32)).r, 2));
    vec3 DarkenColor = mix(vec3(0), ImportantColor, DarkenDepthMap.r);

    vec3 FoggyColor = PerlinColor;
    float treshold = 0.0005;
    float z = DarkenDepthMap.r;
    if (DarkenDepthMap.r >= treshold) {
        FoggyColor = DarkenColor;
    }

    fragColor = vec4(FoggyColor, 1.0);
}