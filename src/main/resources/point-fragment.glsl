varying vec4 v_color;

void main()
{
    gl_FragColor = v_color;
    // If bloom output is needed, encode it in alpha or another channel, 
    // use multiple passes (not supported in GLSL 120)
    // gl_FragData[1] is not available in GLSL 120
}
