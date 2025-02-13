// inspired by Lymbo from DorNell8
#version 150

#define BLUR_ENABLED//is blur enabled?
#define BLUR_SIZE 0.0065//Blur value [0.0095 0.0065 0.001]
#define NOISE_ENABLED//is noise enabled?. Make picture more darker
#define NOISE_COL_SIZE 2.0//Noise Color Size [0.0 1.0 2.0 3.0 4.0]
#define DARK 3.0//Make Picture more darker [1.0 3.0 5.0 7.0]
#define FOG_ENABLED//is fog enabled?
#define FOG_DISTANCE 34.0//Fog distance size [34.0 30.0 24.0]
#define FOG_COLOR 0.15//Fog color [0.10 0.15 0.20]

varying vec2 texcoord;
varying vec4 color;
varying vec2 coord0;

uniform sampler2D colortex0;
uniform float frameTimeCounter;

uniform float near;
uniform float far;
uniform float viewWidth;
uniform float viewHeight;
uniform float centerDepthSmooth;

uniform sampler2D depthtex1;
uniform sampler2D depthtex0;

const float density = 0.005;

float nrand(vec2 n)
{
    return fract(sin(dot(n.xy, vec2(12.9898, 78.233)))* 43758.5453);
}

float n3rand(vec2 n)
{
    float t = fract(frameTimeCounter);
    float nrnd0 = nrand( n + 0.07*t );
    float nrnd1 = nrand( n + 0.11*t );
    float nrnd2 = nrand( n + 0.13*t );
    return (nrnd0+nrnd1+nrnd2) / 3.0;
}

float normpdf(in float x, in float sigma)
{
    return 0.39894*exp(-0.5*x*x/(sigma*sigma))/sigma;
}

vec4 blur() {

    const int mSize = 25;
    const int kSize = (mSize-1)/2;
    float kernel[mSize];
    vec3 final_colour = vec3(0.0);

    float sigma = 7.0;
    float Z = 0.0;
    for (int j = 0; j <= kSize; ++j)
    {
        kernel[kSize+j] = kernel[kSize-j] = normpdf(float(j), sigma);
    }

    for (int j = 0; j < mSize; ++j)
    {
        Z += kernel[j];
    }

    for (int i=-kSize; i <= kSize; ++i)
    {
        for (int j=-kSize; j <= kSize; ++j)
        {
            #ifdef BLUR_SIZE
            final_colour += kernel[kSize+j] * kernel[kSize+i] * texture(colortex0, (texcoord + vec2(float(i),float(j)) * BLUR_SIZE)).rgb;
            #endif

        }
    }
    #ifdef BLUR_ENABLED
    return vec4(final_colour/(Z*Z), 1.0);
    #else
    return vec4(0.0);
    #endif
}

void main() {

    vec3 color = texture(colortex0, texcoord).rgb;
    vec3 finalColor = vec3(dot(color, vec3(0.33333333)));

    if (NOISE_ENABLED) {
        finalColor = vec3(pow(finalColor.x, NOISE_COL_SIZE)) * vec3(n3rand(texcoord + vec2(float(frameTimeCounter) / 12000.0)) + 0.3);
    }

    vec3 bloomColor = blur().rgb;
    bloomColor = vec3(pow(bloomColor.r, DARK));
    bloomColor = vec3(dot(bloomColor, vec3(0.333)));

    float fog = texture2D(depthtex0, texcoord).r; //fog yeah

    if (!FOG_ENABLED) {
        fog = 0.0;
    } else {
        fog = pow(fog / 1.001, FOG_DISTANCE) + 0.1; //make it sharp
        fog = clamp(fog, 0.0, 1.0); // no more than 1 and 0
    }

    gl_FragColor = vec4(mix(bloomColor + finalColor, vec3(FOG_COLOR), fog), 1.0);

}