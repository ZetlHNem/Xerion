#version 150

uniform sampler2D uTexture;
uniform vec2 uTexelSize;
uniform float uOffset;
uniform float uOpacity;
uniform float uFlipY;
uniform vec4 uTint;
uniform vec4 uUvRect;
uniform vec2 uQuadSize;
uniform float uMaskRadius;

in vec2 vUV;
in vec2 vLocal;
out vec4 fragColor;

vec4 kawase(vec2 uv, float offset) {
    uv = clamp(uv, vec2(0.0), vec2(1.0));
    vec2 sampleOffset = uTexelSize * offset;
    vec4 color = texture(uTexture, uv) * 0.28;
    color += texture(uTexture, clamp(uv + vec2( sampleOffset.x,  sampleOffset.y), vec2(0.0), vec2(1.0))) * 0.12;
    color += texture(uTexture, clamp(uv + vec2(-sampleOffset.x,  sampleOffset.y), vec2(0.0), vec2(1.0))) * 0.12;
    color += texture(uTexture, clamp(uv + vec2( sampleOffset.x, -sampleOffset.y), vec2(0.0), vec2(1.0))) * 0.12;
    color += texture(uTexture, clamp(uv + vec2(-sampleOffset.x, -sampleOffset.y), vec2(0.0), vec2(1.0))) * 0.12;
    color += texture(uTexture, clamp(uv + vec2( sampleOffset.x, 0.0), vec2(0.0), vec2(1.0))) * 0.06;
    color += texture(uTexture, clamp(uv + vec2(-sampleOffset.x, 0.0), vec2(0.0), vec2(1.0))) * 0.06;
    color += texture(uTexture, clamp(uv + vec2(0.0,  sampleOffset.y), vec2(0.0), vec2(1.0))) * 0.06;
    color += texture(uTexture, clamp(uv + vec2(0.0, -sampleOffset.y), vec2(0.0), vec2(1.0))) * 0.06;
    return color;
}

void main() {
    vec2 local = clamp(vLocal, vec2(0.0), vec2(1.0));
    vec2 uv = mix(uUvRect.xy, uUvRect.zw, local);
    uv = mix(uv, vec2(uv.x, 1.0 - uv.y), clamp(uFlipY, 0.0, 1.0));
    vec4 blurred = kawase(uv, max(uOffset, 0.0));
    vec3 color = mix(blurred.rgb, uTint.rgb, clamp(uTint.a, 0.0, 1.0));
    float mask = 1.0;
    if (uMaskRadius > 0.0) {
        vec2 p = local * uQuadSize;
        vec2 halfSize = uQuadSize * 0.5;
        vec2 d = abs(p - halfSize) - (halfSize - vec2(uMaskRadius));
        float dist = length(max(d, vec2(0.0))) + min(max(d.x, d.y), 0.0) - uMaskRadius;
        mask = 1.0 - smoothstep(-0.75, 0.75, dist);
    }
    fragColor = vec4(color, clamp(uOpacity, 0.0, 1.0) * mask);
}
