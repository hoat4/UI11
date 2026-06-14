#version 100

precision highp float;

attribute vec2 a_pos;
attribute vec4 a_color;

uniform mat4 u_transform;

varying vec4 v_color;

void main() {
    gl_Position = u_transform * vec4(a_pos, 0, 1);
    v_color = a_color;
}