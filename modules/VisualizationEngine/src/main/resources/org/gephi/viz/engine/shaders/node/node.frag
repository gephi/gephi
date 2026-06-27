//#include "../common.frag.glsl"

//#include "common.node.frag.uniform.glsl"

//#include "common.node.struct.glsl"

//#include "common.node.frag.glsl"

//#include "../common.animation.glsl"
in vec2 vLocal;

flat in VertexData vertexData;
out vec4 fragColor;
float box(vec3 p, vec3 b){ p=abs(p)-b;return length(max(vec3(0), p))+min(0., max(p.x, max(p.y, p.z))); }
vec3 erot(vec3 p, vec3 ax, float t){ return mix(dot(ax, p)*ax, p, cos(t))+cross(ax, p)*sin(t); }
void main(void) {
    vec4 color = vertexData.color;
    borderColor(color, vLocal);

    vec3 ro =vec3(0, 0., -5.), rt=vec3(0);
    vec3 z = normalize(rt-ro), x=vec3(z.z, 0, -z.x);
    vec3 rd =mat3(x, cross(z, x), z)*normalize(vec3(vLocal, 1.0));
    vec3 col = vec3(0.);
    for (float i=0., e=0., g=0.;i++<99.;){
        vec3 p= ro+rd*g;
        p= erot(p, normalize(color.rgb-.5), .785+globalTime*(1+(color.r)));
        float h= box(p, vec3(1.5));
        g+=e=max(.001, h/1.5);
        col += vec3(1.)*.05/exp(i*i*e);
    }

    color.rgb *= sqrt(col);
    fragColor = color;
}
