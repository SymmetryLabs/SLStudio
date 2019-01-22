#version 330

out vec4 o_fragColor;

in vec2 v_texCoords;

uniform sampler2D scene;
uniform sampler2D bloomBlur;

void main()
{
    const float gamma = 2.2;
    vec3 hdrColor = texture(scene, v_texCoords).rgb;
    vec3 bloomColor = texture(bloomBlur, v_texCoords).rgb;
		hdrColor += bloomColor; // additive blending

		// tone mapping
    vec3 result = vec3(1.0) - exp(-hdrColor);

    // also gamma correct while we're at it
    result = pow(result, vec3(1.0 / gamma));
    o_fragColor = vec4(result, 1.0);
}
