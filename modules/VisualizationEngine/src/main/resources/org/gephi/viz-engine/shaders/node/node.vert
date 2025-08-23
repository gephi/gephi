//#if with_selection
//#if selected
//#outname "node_with_selection_selected.vert"
//#else
//#outname "node_with_selection_unselected.vert"
//#endif
//#endif
#version 100

uniform mat4 mvp;
uniform float sizeMultiplier;
uniform float colorMultiplier;
//#if with_selection
//#if !selected
uniform vec4 backgroundColor;
uniform float colorLightenFactor;
//#endif
//#endif

attribute vec2 vert;
attribute vec2 position;
attribute vec4 elementColor;
attribute float size;

varying vec4 fragColor;

void main() {	
    vec2 instancePosition = size * sizeMultiplier * vert + position;
    gl_Position = mvp * vec4(instancePosition, 0.0, 1.0);

    //bgra -> rgba because Java color is argb big-endian
    vec4 color = elementColor.bgra / 255.0;

    //#if with_selection
    //#if selected
    color.rgb = color.rgb * colorMultiplier;
    //#else
    color.rgb = color.rgb * colorMultiplier;
    color.rgb = mix(color.rgb, backgroundColor.rgb, colorLightenFactor);
    //#endif
    //#else
    color.rgb = color.rgb * colorMultiplier;
    //#endif

    fragColor = color;
}
