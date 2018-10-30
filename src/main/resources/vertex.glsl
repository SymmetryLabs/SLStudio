attribute vec4 a_position;

uniform mat4 u_mvp;

void main()
{
    gl_Position =  u_mvp * a_position;
}
