#version 330

in vec4 v_color;

layout (location = 0) out vec4 o_color;
layout (location = 1) out vec4 o_bloom;

void main()
{
    o_color = v_color;
		float brightness = dot(v_color.rgb, vec3(0.2126, 0.7152, 0.0722));
		o_bloom = brightness * v_color;
}
