#version 330
layout (location = 0) in vec3 a_pos;
layout (location = 1) in vec2 a_texCoords;

out vec2 v_texCoords;

void main()
{
    v_texCoords = a_texCoords;
    gl_Position = vec4(a_pos, 1.0);
}
