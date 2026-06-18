# VizEngine Large-Graph Optimization

This document summarizes the changes made to the `VisualizationEngine` so it can handle large
graphs (millions of nodes and edges) with efficient GPU usage and minimal per-frame overhead.

The goal was twofold:

- **Static / interactive views** (pan, zoom, select on a still graph) should cost **near zero** —
  no CPU rebuilds, no GPU re-uploads.
- **Live layout** (node positions changing every frame) should stay **smooth**, streaming only what
  actually changed.

These optimizations build on top of the texture-backed rendering pipeline (node/edge data stored in
`RGBA32F`/`RG32F` data textures and read in the shaders via `texelFetch`).

---

## The problem (before)

Every frame, while the view was marked as "updating", the engine did a full O(N + E) rebuild and a
full GPU re-upload **unconditionally**, even when nothing had changed:

| # | Symptom | Where |
|---|---------|-------|
| R1 | The whole node data texture was re-uploaded every frame (~16 MB at 1M nodes) | `NodeDataTextureStore` always marked itself dirty |
| R2 | All node texels were rewritten on the CPU every frame | `NodeDataTextureStore.fillFromGraph()` |
| R3 | Visible-element collection scanned sparse arrays sized to `maxStoreId + 1` | `NodesCallback` / `EdgesCallback` |
| R4 | Edge data was scanned 3–5 times per update (self-loop + directed + undirected, doubled under selection) | `AbstractEdgeData.updateData()` |
| R5 | No idle/dirty gating: pan/zoom/select (pure shader uniforms) still triggered the full rebuild + upload | `VizEngine` |
| R6 | Node attribute and edge element buffers were re-uploaded every update even when the visible set was unchanged | `InstancedNodeData` / `IndirectNodeData` |

A key constraint: **graphstore exposes no cheap "did a position/size/color change" version**
(`setX` / `setColor` don't bump `graphVersion`). So geometry changes are detected with a content
hash (robust, O(N) scan) and/or an explicit host dirty-hook. `GraphObserver.hasGraphChanged()` is
still used for structural / attribute-column changes (add/remove, weight column).

---

## What changed (after)

The work was done in four phases. Each phase was compiled and checkstyle-validated.

### Phase 0 — Measurement

A lightweight, process-wide stats utility was added so every optimization can be measured.

- **`DataUploadStats`** (new): atomic counters for texture uploads / skips / bytes, buffer uploads /
  skips / bytes, and world-updates run vs. skipped. `snapshot()` returns an immutable record.
- **`VizEngineDemo`** can now generate a synthetic graph and report stats:
  - Press **`G`** to build a random graph. Size is configurable via
    `-Dviz.demo.nodes=N` and `-Dviz.demo.edges=M` (defaults: 200k nodes / 400k edges).
  - Per-second deltas of the upload counters are printed to stdout.
  - The Force Atlas 2 demo layout calls `engine.markDataDirty()` each iteration (see Phase 1).

### Phase 1 — Idle / dirty gating (kills R1, R5)

The engine now skips work when nothing relevant changed.

- **Node geometry is content-hashed during the fill** (`NodeDataTextureStore`). The texture is only
  re-uploaded when the hash differs from what is already on the GPU, so idle / pan / zoom / select
  frames skip the multi-MB transfer entirely.
- **Coarse per-frame change signature** in `VizEngine` (`shouldRunWorldUpdate()` /
  `computeWorldSignature()`): combines `GraphObserver.hasGraphChanged()`, the selection state, the
  rendering options, and the view boundaries. When the signature is unchanged, the whole world
  update is skipped (recorded as a skipped world-update in `DataUploadStats`).
- **`VizEngine.markDataDirty()`** (new, opt-in host hook): hosts that mutate geometry (layout, node
  move, appearance) can call this to flag a change. Once a host uses it, truly-idle frames become
  O(1) (they skip even the O(N) hash scan). The content hash remains the safe default for hosts that
  don't call it, so behavior stays correct without cooperation.

### Phase 2 — Cost proportional to what's visible (kills R3, R4)

- **`NodesCallback` / `EdgesCallback`** now expose a **compact, dense array** of the visible
  elements. The thread-safe parallel collection still writes into the sparse array, then `end()`
  compacts once into a dense list.
- **`AbstractNodeData.update()`** and **`AbstractEdgeData.updateData()`** iterate the dense
  `O(visible)` arrays instead of scanning `0..maxStoreId`. Selection is still keyed by `storeId`.
  This is the big win when zoomed in on a small region of a huge graph.

### Phase 3 — Aggressive GPU upload path (kills residual R1 during layout, R6)

**1. Split node texture (streaming positions vs. rarely-changing style)**

The single `RGBA32F` node texel `(x, y, size, colorBits)` is now two `RG32F` textures:

- **position** texture `(x, y)` — bound to texture unit 1; streams during layout.
- **style** texture `(size, colorBits)` — bound to texture unit 2; re-uploaded only when its content
  hash changes (size/color edits, add/remove).

`GLDataTexture` chooses its GL format from the texel component count
(1 → `R32F`, 2 → `RG32F`, 3 → `RGB32F`, 4 → `RGBA32F`), so the position stream is half the bandwidth
of the old `RGBA32F` texel and the style upload is skipped entirely during a layout.

In the shaders, a helper `vec4 fetchNodeData(int storeId)` (in `common.datatexture.glsl`)
reassembles the original `vec4(x, y, size, colorBits)` from the two textures. Every node/edge vertex
shader that read the old `u_nodeTexture` now calls `fetchNodeData(...)` instead — node, picking, the
six edge-line variants, and the three self-loop variants. The shaders are JCP-preprocessed at build,
so the helper is inlined into each compiled shader.

**2. Dirty-row sub-range uploads**

`NodeDataTextureStore` keeps a **per-row XOR hash of node positions** and derives the contiguous
`[firstRow, firstRow + rowCount)` span that changed since the last upload. `GLDataTexture` gained a
sub-range upload (`uploadAlways(gl, texelCount, firstRow, rowCount)`), so only the touched rows
transfer. A global layout still touches every row (full upload), but a localized edit (dragging a
node or a contiguous cluster) streams just a few rows. A texel-count change or (re)allocation forces
a full upload.

**3. PBO double-buffering (on by default)**

Texture data is uploaded through an orphaned + mapped **ping-pong Pixel Buffer Object** so the
CPU→GPU copy overlaps with the GPU consuming the previous frame, avoiding sync stalls while a layout
streams positions.

- Enabled by default. Disable with `-Dviz.engine.texturePbo=false` to force the direct
  `glTexSubImage2D` path.
- If a driver refuses the buffer mapping, the code falls back to the direct path automatically.

**4. Buffer-upload gating (kills R6)**

- `AbstractNodeData` tracks a content hash of the visible-node attribute buffers
  (`attributesUploadNeeded()`); `InstancedNodeData` and `IndirectNodeData` skip the attribute
  (and indirect command) buffer re-upload when the visible set is unchanged.
- Edge element textures use the content-hash-gated `GLDataTexture.upload(...)`, so they are not
  re-uploaded during a layout (edge texels are position-independent — they store store-ids + weight
  + color).
- `GLBufferMutable` now records buffer uploads in `DataUploadStats`.

---

## New tunables

| Tunable | Default | Effect |
|---------|---------|--------|
| `VizEngine.markDataDirty()` | — | Host hook; call when geometry changes so idle frames become O(1). Optional; the content hash is the safe fallback. |
| `-Dviz.engine.texturePbo=false` | PBO **on** | Forces the direct `glTexSubImage2D` upload path instead of PBO streaming. |
| `-Dviz.demo.nodes=N` | 200000 | Synthetic graph node count in `VizEngineDemo` (press `G`). |
| `-Dviz.demo.edges=M` | 400000 | Synthetic graph edge count in `VizEngineDemo` (press `G`). |

---

## Files touched

**New**

- `util/gl/DataUploadStats.java` — upload/skip counters.

**Core engine / upload path**

- `VizEngine.java` — change signature, world-update gating, `markDataDirty()`.
- `jogl/pipeline/common/NodeDataTextureStore.java` — split pos/style textures, per-row position
  hashes, dirty-row span, style hash gating.
- `jogl/util/gl/GLDataTexture.java` — format-by-component-count, sub-range uploads, PBO ping-pong.
- `jogl/util/gl/GLBufferMutable.java` — upload stats.

**Per-update cost / gating**

- `util/structure/NodesCallback.java`, `util/structure/EdgesCallback.java` — compact visible arrays.
- `jogl/pipeline/common/AbstractNodeData.java`, `jogl/pipeline/common/AbstractEdgeData.java` —
  iterate compact arrays; attribute-buffer hash gating.
- `jogl/pipeline/instanced/InstancedNodeData.java`, `jogl/pipeline/indirect/IndirectNodeData.java` —
  skip unchanged buffer uploads.

**Texture-split wiring**

- `util/gl/Constants.java` — two node texture units + sampler names.
- `jogl/models/DataTextureModelSupport.java`, `jogl/models/nodedisk/AbstractNodeDiskModel.java` —
  register/set both node samplers.

**Shaders** (`src/main/resources/.../shaders/`)

- `common.datatexture.glsl` — two node samplers + `fetchNodeData()` helper.
- `node/common.node.vert.uniform.glsl`, `edge/common.edge.vert.uniform.glsl` — dropped the old
  `u_nodeTexture` sampler.
- `node/node.vert`, `node/node_picking.vert`, `node/node_with_selection_{selected,unselected}.vert`
- `edge/edge-line-{directed,undirected}*.vert` (6 files), `edge/selfloop{,_selected,_unselected}.vert`

**Demo**

- `VizEngineDemo.java` — synthetic graph generator, `markDataDirty()` during layout, stats reporting.

---

## Validation

- `mvn -o -pl modules/VisualizationEngine compile` and the `enableCheckStyle` profile pass after
  every phase. The JCP shader preprocessing was verified in `target/generated-resources` (the
  `fetchNodeData` helper inlines correctly and no `u_nodeTexture` reference remains).

**Runtime checks to perform in `VizEngineDemo`** (GLSL only compiles at runtime, so these require a
GPU and are not covered by the build):

1. Press `G` for a large synthetic graph; confirm nodes, edges, directed arrows and self-loops
   render correctly.
2. Pan / zoom / select on a still graph → the per-second upload counters should show ~0 texture
   uploads (only skips).
3. Run a layout → only position uploads occur (style uploads stay at 0) and motion stays smooth.
4. Confirm node **picking** still selects the correct node.
5. Optionally re-run with `-Dviz.engine.texturePbo=false` to compare the PBO and direct paths.

---

## Out of scope (possible follow-ups)

- Draw-side LOD / culling for extreme zoom-out (sub-pixel nodes/edges) — changes visuals, so it
  belongs behind an option.
- GL4-only persistent-mapped buffers (`ARB_buffer_storage`) for the indirect pipeline — a more
  aggressive streaming path that was intentionally deferred.
