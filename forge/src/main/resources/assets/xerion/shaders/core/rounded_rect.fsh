#version 150

uniform vec2 uSize;
uniform vec4 uColor;
uniform float uRadius;
uniform float uFeather;
uniform float uStrokeWidth;

in vec2 vUV;
out vec4 fragColor;

float roundedBoxSDF(vec2 p, vec2 halfSize, float radius) {
    vec2 q = abs(p) - halfSize + vec2(radius);
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
}

void main() {
    vec2 halfSize = uSize * 0.5;
    vec2 p = vUV * uSize - halfSize;
    float distanceToEdge = roundedBoxSDF(p, halfSize, uRadius);
    float feather = max(uFeather, fwidth(distanceToEdge) * 0.75);
    float fillAlpha = smoothstep(feather, -feather, distanceToEdge);

    vec2 innerHalfSize = max(halfSize - vec2(uStrokeWidth), vec2(0.0));
    float innerRadius = max(uRadius - uStrokeWidth, 0.0);
    float innerDistance = roundedBoxSDF(p, innerHalfSize, innerRadius);
    float innerAlpha = smoothstep(feather, -feather, innerDistance);
    float strokeAlpha = clamp(fillAlpha - innerAlpha, 0.0, 1.0);
    float alpha = uStrokeWidth > 0.0 ? strokeAlpha : fillAlpha;

    fragColor = vec4(uColor.rgb, uColor.a * alpha);
}
