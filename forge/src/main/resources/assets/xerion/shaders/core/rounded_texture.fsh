#version 150

uniform sampler2D uTexture;
uniform vec2 uSize;
uniform vec4 uUvRect;
uniform float uRadius;
uniform float uFeather;
uniform float uAlpha;

in vec2 vLocal;
out vec4 fragColor;

float roundedBoxSDF(vec2 p, vec2 halfSize, float radius) {
    vec2 q = abs(p) - halfSize + vec2(radius);
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
}

void main() {
    vec2 local = clamp(vLocal, vec2(0.0), vec2(1.0));
    vec2 uv = mix(uUvRect.xy, uUvRect.zw, local);
    vec4 tex = texture(uTexture, uv);

    vec2 halfSize = uSize * 0.5;
    vec2 p = local * uSize - halfSize;
    float distanceToEdge = roundedBoxSDF(p, halfSize, max(uRadius, 0.0));
    float feather = max(uFeather, fwidth(distanceToEdge) * 0.75);
    float mask = smoothstep(feather, -feather, distanceToEdge);

    fragColor = vec4(tex.rgb, tex.a * mask * clamp(uAlpha, 0.0, 1.0));
}
