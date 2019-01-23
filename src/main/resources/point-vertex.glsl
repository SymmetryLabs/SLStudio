#version 330 core

uniform mat4 u_mvp;
uniform float u_pointSize;

in vec4 a_position;
in vec4 a_color;

out vec4 v_color;

void main()
{
        gl_Position =  u_mvp * a_position;
        gl_PointSize = u_pointSize;
        v_color = a_color / 255.0;
}
