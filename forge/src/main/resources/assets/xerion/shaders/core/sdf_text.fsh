#version 150

uniform sampler2D uTexture;
uniform vec4 uColor;
uniform float uWeight;
uniform float uPxRange;
uniform float uGamma;
uniform float uSharpness;
uniform float uShadowEnabled;
uniform vec2 uShadowOffset;
uniform vec4 uShadowColor;

in vec2 vUV;
out vec4 fragColor;

float screenPxRange() {
    vec2 texSize = vec2(textureSize(uTexture, 0));
    vec2 unitRange = vec2(uPxRange) / texSize;
    vec2 screenTexSize = vec2(1.0) / fwidth(vUV);
    return max(0.5 * dot(unitRange, screenTexSize), 1.0);
}

float msdfAlpha(vec2 uv) {
    float sd = 1.0 - texture(uTexture, uv).r + uWeight;
    float w = max(fwidth(sd), 0.5 / screenPxRange()) * uSharpness;
    float alpha = smoothstep(0.5 - w, 0.5 + w, sd);
    return pow(alpha, uGamma);
}

void main() {
    float alpha = msdfAlpha(vUV);
    float textAlpha = uColor.a * alpha;

    if (uShadowEnabled > 0.5) {
        vec2 shadowUv = vUV - uShadowOffset * fwidth(vUV);
        float shadowAlpha = msdfAlpha(shadowUv);
        float shadowOutAlpha = uShadowColor.a * shadowAlpha * (1.0 - textAlpha);
        float outAlpha = textAlpha + shadowOutAlpha;
        vec3 outRgb = vec3(0.0);
        if (outAlpha > 0.0001) {
            outRgb = (uColor.rgb * textAlpha + uShadowColor.rgb * shadowOutAlpha) / outAlpha;
        }
        fragColor = vec4(outRgb, outAlpha);
    } else {
        fragColor = vec4(uColor.rgb, textAlpha);
    }
}
