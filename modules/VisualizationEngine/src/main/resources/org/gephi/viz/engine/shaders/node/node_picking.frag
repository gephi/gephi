#version 330

#ifdef GL_ES
precision highp float;
precision highp int;
#endif

flat in vec4 vPickColor;
in vec2 vLocal;
out vec4 fragColor;

void main(void) {
    // Only the inscribed disk of the quad is pickable, matching the visible node shape. A hard edge
    // (no antialiasing / alpha-to-coverage) is used so every covered pixel carries an exact id.
    if (dot(vLocal, vLocal) > 1.0) {
        discard;
    }
    fragColor = vPickColor;
}
