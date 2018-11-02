#version 420

uniform mat4 u_mvp;

layout(location = 0) in vec4 a_position;
layout(location = 1) in vec4 a_color;

out vec4 v_color;

void main()
{
    gl_Position =  u_mvp * a_position;
		v_color = a_color;
}
