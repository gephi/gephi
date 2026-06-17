// Resolves the per-element index used to texelFetch the edge data texture.
// - Instanced rendering (u_vertsPerElement == 0): one instance per edge, use gl_InstanceID.
// - Array-draw rendering (u_vertsPerElement > 0): a non-instanced draw of N edges, each made of
//   u_vertsPerElement vertices, so the local edge index is gl_VertexID / u_vertsPerElement.
// u_elementOffset is the base index of the currently drawn range inside the element texture (which
// holds the category's edges as [unselected | selected]); it lets a single texture be drawn in
// ranges/batches without a per-subset texture.
uniform int u_vertsPerElement;
uniform int u_elementOffset;

int edgeElementIndex() {
    int local = (u_vertsPerElement > 0) ? (gl_VertexID / u_vertsPerElement) : gl_InstanceID;
    return u_elementOffset + local;
}
