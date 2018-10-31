attribute vec4 a_position;
attribute vec4 a_color;

varying vec4 v_color;

uniform mat4 u_mvp;

void main()
{
    gl_Position =  u_mvp * a_position;
		v_color = a_color;
}
