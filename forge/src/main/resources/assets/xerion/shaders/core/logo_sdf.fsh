#version 150

uniform sampler2D uTexture;
uniform float uPxRange;
uniform float uAlpha;

in vec2 vUV;
out vec4 fragColor;

float screenPxRange() {
    vec2 texSize = vec2(textureSize(uTexture, 0));
    vec2 unitRange = vec2(uPxRange) / texSize;
    vec2 screenTexSize = vec2(1.0) / max(fwidth(vUV), vec2(0.000001));
    return max(0.5 * dot(unitRange, screenTexSize), 1.0);
}

void main() {
    vec4 sampleColor = texture(uTexture, vUV);
    float screenDistance = (sampleColor.a - 0.5) * screenPxRange();
    float coverage = clamp(screenDistance * 1.4 + 0.5, 0.0, 1.0);
    fragColor = vec4(sampleColor.rgb, coverage * clamp(uAlpha, 0.0, 1.0));
}
