varying vec4 v_color;

void main()
{
        float brightness = dot(v_color.rgb, vec3(0.2126, 0.7152, 0.0722));
        gl_FragColor = v_color;
        // If bloom output is needed, encode it in alpha or another channel, or use multiple passes (not supported in GLSL 120)
        // gl_FragData[1] is not available in GLSL 120
}
