//#if with_selection
//#if selected
//#outname "node_with_selection_selected.vert"
//#else
//#outname "node_with_selection_unselected.vert"
//#endif
//#endif
#version 330

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
uniform float globalTime;
uniform int selectionMode;
uniform float selectionTime;
varying vec4 fragColor;

void main() {	
    vec2 instancePosition = size * sizeMultiplier * vert + position;

    gl_Position = mvp * vec4(instancePosition, 0.0, 1.0);

    //bgra -> rgba because Java color is argb big-endian
    vec4 color = elementColor.bgra / 255.0;

    float animationTime = globalTime - selectionTime;

    //#if with_selection
    //#if selected
    color.rgb = color.rgb * colorMultiplier;
    //#else
    float colorLightenFactorEffective = colorLightenFactor;
    if(selectionMode==2) {
        colorLightenFactorEffective*= .5-exp(-5.*animationTime);
        color.rgb = mix(color.rgb, backgroundColor.rgb, colorLightenFactorEffective);
    }
    else if(selectionMode==1) {

        color.rgb = color.rgb*abs(sin(animationTime));
    }
    else if(selectionMode==0) {
        color.rgb = color.rgb;
    }


    //#endif
    //#else
    color.rgb = color.rgb * colorMultiplier;
    //#endif

    fragColor = color;
}
