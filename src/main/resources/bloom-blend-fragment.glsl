#version 330 core

out vec4 o_fragColor;

in vec2 v_texCoords;

uniform sampler2D scene;
uniform sampler2D bloomBlur;

void main()
{
        vec3 hdrColor = texture(scene, v_texCoords).rgb;
        vec3 bloomColor = texture(bloomBlur, v_texCoords).rgb;
        hdrColor += bloomColor; // additive blending

        // tone mapping
        vec3 result = vec3(1.0) - exp(-hdrColor);
        result = hdrColor;

        // also gamma correct while we're at it
        //result = pow(result, vec3(1.0 / 2.2));
        o_fragColor = vec4(result, 1.0);
        /*
          o_fragColor = result.x == -1000 ? vec4(result, 1.0) :
          result.x < 0 ? vec4(1, 0, 0, 1) :
          result.x > 1 ? vec4(0, 0, 1, 1) :
          vec4(result, 1);
        */
}
