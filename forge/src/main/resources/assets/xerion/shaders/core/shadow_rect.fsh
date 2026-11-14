#version 150

uniform vec2 uRectSize;
uniform vec2 uQuadSize;
uniform vec4 uColor;
uniform float uRadius;
uniform float uSpread;

in vec2 vUV;
out vec4 fragColor;

float roundedBoxSDF(vec2 p, vec2 halfSize, float radius) {
    vec2 q = abs(p) - halfSize + vec2(radius);
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
}

void main() {
    vec2 p = vUV * uQuadSize - uQuadSize * 0.5;
    vec2 halfSize = uRectSize * 0.5;
    float distanceToEdge = roundedBoxSDF(p, halfSize, uRadius);
    float spread = max(uSpread, 0.001);
    float aa = max(fwidth(distanceToEdge), 0.5);
    float outsideMask = smoothstep(-aa, aa, distanceToEdge);
    float shadow = outsideMask * (1.0 - smoothstep(0.0, spread, distanceToEdge));
    fragColor = vec4(uColor.rgb, uColor.a * shadow);
}
