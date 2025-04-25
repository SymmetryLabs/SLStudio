uniform mat4 u_mvp;
uniform float u_pointSize;

attribute vec4 a_position;
attribute vec4 a_color;

varying vec4 v_color;

void main()
{
        gl_Position =  u_mvp * a_position;
        /* poor-man's bloom filter: scale each point by its luminance. */
        float lum = dot(vec4(0.2126, 0.7152, 0.0722, 0), a_color) / sqrt(3.0);
        gl_PointSize = u_pointSize * (1.0 + 3.5 * lum);
        v_color = a_color;
}
