#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;
uniform float uMaterialMode;
uniform float uThroughWalls;
uniform float uTime;
uniform vec4 uTint;

in float vertexDistance;
in vec4 vertexColor;
in vec4 shadedColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
in vec3 viewNormal;
in vec3 viewPosition;

out vec4 fragColor;

float saturate(float value) {
    return clamp(value, 0.0, 1.0);
}

float fresnelTerm() {
    vec3 n = normalize(viewNormal);
    vec3 v = normalize(-viewPosition);
    return pow(1.0 - saturate(dot(n, v)), 1.65);
}

vec3 rainbow(float t) {
    return 0.55 + 0.45 * cos(6.2831853 * (vec3(0.0, 0.33, 0.67) + t));
}

void main() {
    vec4 mask = texture(Sampler0, texCoord0);
    if (mask.a < 0.05) {
        discard;
    }

    vec4 tint = vertexColor * ColorModulator * uTint;
    float alpha = tint.a * mask.a;
    vec3 base = tint.rgb;
    vec3 lit = shadedColor.rgb * lightMapColor.rgb * ColorModulator.rgb;
    float fresnel = fresnelTerm();
    float mode = floor(uMaterialMode + 0.5);

    vec3 rgb;
    if (mode < 0.5) {
        // White / shaded: Source2 primary-white style, colored by vertex tint with MC lighting.
        rgb = mix(base * 0.72, lit * 1.18, 0.72);
    } else if (mode < 1.5) {
        // Solid: flat unlit silhouette.
        rgb = base * 1.12;
    } else if (mode < 2.5) {
        // Illuminate: self-illum material, bright but still filled.
        float edge = smoothstep(0.10, 0.82, fresnel);
        rgb = base * 2.65 + base * edge * 3.25 + vec3(1.0) * edge * 0.35;
        alpha *= 0.96;
    } else if (mode < 3.5) {
        // Glow shell: exterior-only edge energy for model outer glow.
        float edge = smoothstep(0.02, 0.64, fresnel);
        float rim = smoothstep(0.38, 0.92, fresnel);
        rgb = base * (0.40 + edge * 1.35 + rim * 0.90);
        alpha *= pow(edge, 1.45) * 0.68 + rim * 0.26;
    } else if (mode < 4.5) {
        // Glass / iridescent: fresnel color shift with translucent body.
        vec3 oil = rainbow(texCoord0.x * 0.85 - texCoord0.y * 0.45 + fresnel * 0.35 + uTime * 0.08);
        rgb = mix(base * 0.32, oil * base * 2.2, 0.45 + fresnel * 0.45);
        alpha *= 0.50 + fresnel * 0.38;
    } else if (mode < 5.5) {
        // Flowing material: moving glossy streaks over tint.
        float flow = sin((texCoord0.y * 9.5 + texCoord0.x * 2.0 - uTime * 1.45) * 6.2831853);
        float streak = smoothstep(0.15, 0.95, 0.5 + 0.5 * flow);
        vec3 water = vec3(0.55, 0.82, 1.25) * (0.75 + streak * 0.55);
        rgb = mix(base * 0.55, base * water, 0.75) + base * fresnel * 0.65;
        alpha *= 0.72;
    } else {
        // Latex / glossy: lit surface with a broad specular band.
        float band = smoothstep(0.55, 0.98, sin((texCoord0.x * 1.2 - texCoord0.y * 1.6 + uTime * 0.05) * 6.2831853) * 0.5 + 0.5);
        rgb = lit * 0.82 + base * (0.38 + fresnel * 1.6) + vec3(1.0) * band * 0.35;
        alpha *= 0.86;
    }

    rgb = mix(overlayColor.rgb, rgb, overlayColor.a);
    if (uThroughWalls > 0.5) {
        rgb *= 0.86;
        alpha *= 0.58;
    }

    vec4 color = vec4(rgb, alpha);
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
