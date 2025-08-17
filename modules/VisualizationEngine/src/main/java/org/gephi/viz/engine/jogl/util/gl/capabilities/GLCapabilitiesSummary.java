package org.gephi.viz.engine.jogl.util.gl.capabilities;

import com.jogamp.opengl.GL;
import static com.jogamp.opengl.GL.GL_EXTENSIONS;
import static com.jogamp.opengl.GL.GL_RENDERER;
import static com.jogamp.opengl.GL.GL_VENDOR;
import static com.jogamp.opengl.GL.GL_VERSION;
import static com.jogamp.opengl.GL2ES2.GL_SHADING_LANGUAGE_VERSION;
import static com.jogamp.opengl.GL2ES3.GL_CONTEXT_FLAGS;
import static com.jogamp.opengl.GL2ES3.GL_MAJOR_VERSION;
import static com.jogamp.opengl.GL2ES3.GL_MINOR_VERSION;
import static com.jogamp.opengl.GL2ES3.GL_NUM_EXTENSIONS;
import com.jogamp.opengl.util.GLBuffers;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.gephi.viz.engine.jogl.util.gl.GLFunctions;
import org.gephi.viz.engine.util.gl.OpenGLOptions;

/**
 *
 * @author gbarbieri
 */
public final class GLCapabilitiesSummary {

    public GLCapabilitiesSummary(GL gl, Profile profile) {
        initVersion(gl, profile);
        initExtensions(gl);
        if (check(4, 3) || extensions.KHR_debug) {
            gl.glGetIntegerv(GL_CONTEXT_FLAGS, data);
            version.CONTEXT_FLAGS = data.get(0);
        }
    }

    private GLVersionData version;
    private GLExtensionData extensions;

    private final IntBuffer data = GLBuffers.newDirectIntBuffer(1);

    private boolean check(int majorVersionRequire, int minorVersionRequire) {
        return (version.MAJOR_VERSION * 100 + version.MINOR_VERSION * 10)
                >= (majorVersionRequire * 100 + minorVersionRequire * 10);
    }

    public GLVersionData getVersion() {
        return version;
    }

    public GLExtensionData getExtensions() {
        return extensions;
    }

    private void initVersion(GL gl, Profile profile) {
        version = new GLVersionData(profile);

        gl.glGetIntegerv(GL_MINOR_VERSION, data);
        version.MINOR_VERSION = data.get(0);
        gl.glGetIntegerv(GL_MAJOR_VERSION, data);
        version.MAJOR_VERSION = data.get(0);

        version.RENDERER = gl.glGetString(GL_RENDERER);
        version.VENDOR = gl.glGetString(GL_VENDOR);
        version.VERSION = gl.glGetString(GL_VERSION);
        version.SHADING_LANGUAGE_VERSION = gl.glGetString(GL_SHADING_LANGUAGE_VERSION);
    }

    private void initExtensions(GL gl) {
        extensions = new GLExtensionData();

        final List<String> extensionsList = new ArrayList<>();

        if (gl.isGL2ES3()) {
            gl.glGetIntegerv(GL_NUM_EXTENSIONS, data);
            version.NUM_EXTENSIONS = data.get(0);

            for (int i = 0; i < version.NUM_EXTENSIONS; i++) {
                String extension = GLFunctions.glGetStringi(gl.getGL2ES3(), GL_EXTENSIONS, i);
                extensionsList.add(extension.trim());
            }
        } else {
            String[] parts = gl.glGetString(GL_EXTENSIONS).split(Pattern.quote(","));
            for (String extension : parts) {
                extensionsList.add(extension.trim());
            }
        }

        for (String extension : extensionsList) {
            switch (extension) {
                case "GL_ARB_multitexture":
                    extensions.ARB_multitexture = true;
                    break;
                case "GL_ARB_transpose_matrix":
                    extensions.ARB_transpose_matrix = true;
                    break;
                case "GL_ARB_multisample":
                    extensions.ARB_multisample = true;
                    break;
                case "GL_ARB_texture_env_add":
                    extensions.ARB_texture_env_add = true;
                    break;
                case "GL_ARB_texture_cube_map":
                    extensions.ARB_texture_cube_map = true;
                    break;
                case "GL_ARB_texture_compression":
                    extensions.ARB_texture_compression = true;
                    break;
                case "GL_ARB_texture_border_clamp":
                    extensions.ARB_texture_border_clamp = true;
                    break;
                case "GL_ARB_point_parameters":
                    extensions.ARB_point_parameters = true;
                    break;
                case "GL_ARB_vertex_blend":
                    extensions.ARB_vertex_blend = true;
                    break;
                case "GL_ARB_matrix_palette":
                    extensions.ARB_matrix_palette = true;
                    break;
                case "GL_ARB_texture_env_combine":
                    extensions.ARB_texture_env_combine = true;
                    break;
                case "GL_ARB_texture_env_crossbar":
                    extensions.ARB_texture_env_crossbar = true;
                    break;
                case "GL_ARB_texture_env_dot3":
                    extensions.ARB_texture_env_dot3 = true;
                    break;
                case "GL_ARB_texture_mirrored_repeat":
                    extensions.ARB_texture_mirrored_repeat = true;
                    break;
                case "GL_ARB_depth_texture":
                    extensions.ARB_depth_texture = true;
                    break;
                case "GL_ARB_shadow":
                    extensions.ARB_shadow = true;
                    break;
                case "GL_ARB_shadow_ambient":
                    extensions.ARB_shadow_ambient = true;
                    break;
                case "GL_ARB_window_pos":
                    extensions.ARB_window_pos = true;
                    break;
                case "GL_ARB_vertex_program":
                    extensions.ARB_vertex_program = true;
                    break;
                case "GL_ARB_fragment_program":
                    extensions.ARB_fragment_program = true;
                    break;
                case "GL_ARB_vertex_buffer_object":
                    extensions.ARB_vertex_buffer_object = true;
                    break;
                case "GL_ARB_occlusion_query":
                    extensions.ARB_occlusion_query = true;
                    break;
                case "GL_ARB_shader_objects":
                    extensions.ARB_shader_objects = true;
                    break;
                case "GL_ARB_vertex_shader":
                    extensions.ARB_vertex_shader = true;
                    break;
                case "GL_ARB_fragment_shader":
                    extensions.ARB_fragment_shader = true;
                    break;
                case "GL_ARB_shading_language_100":
                    extensions.ARB_shading_language_100 = true;
                    break;
                case "GL_ARB_texture_non_power_of_two":
                    extensions.ARB_texture_non_power_of_two = true;
                    break;
                case "GL_ARB_point_sprite":
                    extensions.ARB_point_sprite = true;
                    break;
                case "GL_ARB_fragment_program_shadow":
                    extensions.ARB_fragment_program_shadow = true;
                    break;
                case "GL_ARB_draw_buffers":
                    extensions.ARB_draw_buffers = true;
                    break;
                case "GL_ARB_texture_rectangle":
                    extensions.ARB_texture_rectangle = true;
                    break;
                case "GL_ARB_color_buffer_float":
                    extensions.ARB_color_buffer_float = true;
                    break;
                case "GL_ARB_half_float_pixel":
                    extensions.ARB_half_float_pixel = true;
                    break;
                case "GL_ARB_texture_float":
                    extensions.ARB_texture_float = true;
                    break;
                case "GL_ARB_pixel_buffer_object":
                    extensions.ARB_pixel_buffer_object = true;
                    break;
                case "GL_ARB_depth_buffer_float":
                    extensions.ARB_depth_buffer_float = true;
                    break;
                case "GL_ARB_draw_instanced":
                    extensions.ARB_draw_instanced = true;
                    break;
                case "GL_ARB_framebuffer_object":
                    extensions.ARB_framebuffer_object = true;
                    break;
                case "GL_ARB_framebuffer_sRGB":
                    extensions.ARB_framebuffer_sRGB = true;
                    break;
                case "GL_ARB_geometry_shader4":
                    extensions.ARB_geometry_shader4 = true;
                    break;
                case "GL_ARB_half_float_vertex":
                    extensions.ARB_half_float_vertex = true;
                    break;
                case "GL_ARB_instanced_arrays":
                    extensions.ARB_instanced_arrays = true;
                    break;
                case "GL_ARB_map_buffer_range":
                    extensions.ARB_map_buffer_range = true;
                    break;
                case "GL_ARB_texture_buffer_object":
                    extensions.ARB_texture_buffer_object = true;
                    break;
                case "GL_ARB_texture_compression_rgtc":
                    extensions.ARB_texture_compression_rgtc = true;
                    break;
                case "GL_ARB_texture_rg":
                    extensions.ARB_texture_rg = true;
                    break;
                case "GL_ARB_vertex_array_object":
                    extensions.ARB_vertex_array_object = true;
                    break;
                case "GL_ARB_uniform_buffer_object":
                    extensions.ARB_uniform_buffer_object = true;
                    break;
                case "GL_ARB_compatibility":
                    extensions.ARB_compatibility = true;
                    break;
                case "GL_ARB_copy_buffer":
                    extensions.ARB_copy_buffer = true;
                    break;
                case "GL_ARB_shader_texture_lod":
                    extensions.ARB_shader_texture_lod = true;
                    break;
                case "GL_ARB_depth_clamp":
                    extensions.ARB_depth_clamp = true;
                    break;
                case "GL_ARB_draw_elements_base_vertex":
                    extensions.ARB_draw_elements_base_vertex = true;
                    break;
                case "GL_ARB_fragment_coord_conventions":
                    extensions.ARB_fragment_coord_conventions = true;
                    break;
                case "GL_ARB_provoking_vertex":
                    extensions.ARB_provoking_vertex = true;
                    break;
                case "GL_ARB_seamless_cube_map":
                    extensions.ARB_seamless_cube_map = true;
                    break;
                case "GL_ARB_sync":
                    extensions.ARB_sync = true;
                    break;
                case "GL_ARB_texture_multisample":
                    extensions.ARB_texture_multisample = true;
                    break;
                case "GL_ARB_vertex_array_bgra":
                    extensions.ARB_vertex_array_bgra = true;
                    break;
                case "GL_ARB_draw_buffers_blend":
                    extensions.ARB_draw_buffers_blend = true;
                    break;
                case "GL_ARB_sample_shading":
                    extensions.ARB_sample_shading = true;
                    break;
                case "GL_ARB_texture_cube_map_array":
                    extensions.ARB_texture_cube_map_array = true;
                    break;
                case "GL_ARB_texture_gather":
                    extensions.ARB_texture_gather = true;
                    break;
                case "GL_ARB_texture_query_lod":
                    extensions.ARB_texture_query_lod = true;
                    break;
                case "GL_ARB_shading_language_include":
                    extensions.ARB_shading_language_include = true;
                    break;
                case "GL_ARB_texture_compression_bptc":
                    extensions.ARB_texture_compression_bptc = true;
                    break;
                case "GL_ARB_blend_func_extended":
                    extensions.ARB_blend_func_extended = true;
                    break;
                case "GL_ARB_explicit_attrib_location":
                    extensions.ARB_explicit_attrib_location = true;
                    break;
                case "GL_ARB_occlusion_query2":
                    extensions.ARB_occlusion_query2 = true;
                    break;
                case "GL_ARB_sampler_objects":
                    extensions.ARB_sampler_objects = true;
                    break;
                case "GL_ARB_shader_bit_encoding":
                    extensions.ARB_shader_bit_encoding = true;
                    break;
                case "GL_ARB_texture_rgb10_a2ui":
                    extensions.ARB_texture_rgb10_a2ui = true;
                    break;
                case "GL_ARB_texture_swizzle":
                    extensions.ARB_texture_swizzle = true;
                    break;
                case "GL_ARB_timer_query":
                    extensions.ARB_timer_query = true;
                    break;
                case "GL_ARB_vertex_type_2_10_10_10_rev":
                    extensions.ARB_vertex_type_2_10_10_10_rev = true;
                    break;
                case "GL_ARB_draw_indirect":
                    extensions.ARB_draw_indirect = true;
                    break;
                case "GL_ARB_gpu_shader5":
                    extensions.ARB_gpu_shader5 = true;
                    break;
                case "GL_ARB_gpu_shader_fp64":
                    extensions.ARB_gpu_shader_fp64 = true;
                    break;
                case "GL_ARB_shader_subroutine":
                    extensions.ARB_shader_subroutine = true;
                    break;
                case "GL_ARB_tessellation_shader":
                    extensions.ARB_tessellation_shader = true;
                    break;
                case "GL_ARB_texture_buffer_object_rgb32":
                    extensions.ARB_texture_buffer_object_rgb32 = true;
                    break;
                case "GL_ARB_transform_feedback2":
                    extensions.ARB_transform_feedback2 = true;
                    break;
                case "GL_ARB_transform_feedback3":
                    extensions.ARB_transform_feedback3 = true;
                    break;
                case "GL_ARB_ES2_compatibility":
                    extensions.ARB_ES2_compatibility = true;
                    break;
                case "GL_ARB_get_program_binary":
                    extensions.ARB_get_program_binary = true;
                    break;
                case "GL_ARB_separate_shader_objects":
                    extensions.ARB_separate_shader_objects = true;
                    break;
                case "GL_ARB_shader_precision":
                    extensions.ARB_shader_precision = true;
                    break;
                case "GL_ARB_vertex_attrib_64bit":
                    extensions.ARB_vertex_attrib_64bit = true;
                    break;
                case "GL_ARB_viewport_array":
                    extensions.ARB_viewport_array = true;
                    break;
                case "GL_ARB_cl_event":
                    extensions.ARB_cl_event = true;
                    break;
                case "GL_ARB_debug_output":
                    extensions.ARB_debug_output = true;
                    break;
                case "GL_ARB_robustness":
                    extensions.ARB_robustness = true;
                    break;
                case "GL_ARB_shader_stencil_export":
                    extensions.ARB_shader_stencil_export = true;
                    break;
                case "GL_ARB_base_instance":
                    extensions.ARB_base_instance = true;
                    break;
                case "GL_ARB_shading_language_420pack":
                    extensions.ARB_shading_language_420pack = true;
                    break;
                case "GL_ARB_transform_feedback_instanced":
                    extensions.ARB_transform_feedback_instanced = true;
                    break;
                case "GL_ARB_compressed_texture_pixel_storage":
                    extensions.ARB_compressed_texture_pixel_storage = true;
                    break;
                case "GL_ARB_conservative_depth":
                    extensions.ARB_conservative_depth = true;
                    break;
                case "GL_ARB_internalformat_query":
                    extensions.ARB_internalformat_query = true;
                    break;
                case "GL_ARB_map_buffer_alignment":
                    extensions.ARB_map_buffer_alignment = true;
                    break;
                case "GL_ARB_shader_atomic_counters":
                    extensions.ARB_shader_atomic_counters = true;
                    break;
                case "GL_ARB_shader_image_load_store":
                    extensions.ARB_shader_image_load_store = true;
                    break;
                case "GL_ARB_shading_language_packing":
                    extensions.ARB_shading_language_packing = true;
                    break;
                case "GL_ARB_texture_storage":
                    extensions.ARB_texture_storage = true;
                    break;
                case "GL_KHR_texture_compression_astc_hdr":
                    extensions.KHR_texture_compression_astc_hdr = true;
                    break;
                case "GL_KHR_texture_compression_astc_ldr":
                    extensions.KHR_texture_compression_astc_ldr = true;
                    break;
                case "GL_KHR_debug":
                    extensions.KHR_debug = true;
                    break;
                case "GL_ARB_arrays_of_arrays":
                    extensions.ARB_arrays_of_arrays = true;
                    break;
                case "GL_ARB_clear_buffer_object":
                    extensions.ARB_clear_buffer_object = true;
                    break;
                case "GL_ARB_compute_shader":
                    extensions.ARB_compute_shader = true;
                    break;
                case "GL_ARB_copy_image":
                    extensions.ARB_copy_image = true;
                    break;
                case "GL_ARB_texture_view":
                    extensions.ARB_texture_view = true;
                    break;
                case "GL_ARB_vertex_attrib_binding":
                    extensions.ARB_vertex_attrib_binding = true;
                    break;
                case "GL_ARB_robustness_isolation":
                    extensions.ARB_robustness_isolation = true;
                    break;
                case "GL_ARB_ES3_compatibility":
                    extensions.ARB_ES3_compatibility = true;
                    break;
                case "GL_ARB_explicit_uniform_location":
                    extensions.ARB_explicit_uniform_location = true;
                    break;
                case "GL_ARB_fragment_layer_viewport":
                    extensions.ARB_fragment_layer_viewport = true;
                    break;
                case "GL_ARB_framebuffer_no_attachments":
                    extensions.ARB_framebuffer_no_attachments = true;
                    break;
                case "GL_ARB_internalformat_query2":
                    extensions.ARB_internalformat_query2 = true;
                    break;
                case "GL_ARB_invalidate_subdata":
                    extensions.ARB_invalidate_subdata = true;
                    break;
                case "GL_ARB_multi_draw_indirect":
                    extensions.ARB_multi_draw_indirect = true;
                    break;
                case "GL_ARB_program_interface_query":
                    extensions.ARB_program_interface_query = true;
                    break;
                case "GL_ARB_robust_buffer_access_behavior":
                    extensions.ARB_robust_buffer_access_behavior = true;
                    break;
                case "GL_ARB_shader_image_size":
                    extensions.ARB_shader_image_size = true;
                    break;
                case "GL_ARB_shader_storage_buffer_object":
                    extensions.ARB_shader_storage_buffer_object = true;
                    break;
                case "GL_ARB_stencil_texturing":
                    extensions.ARB_stencil_texturing = true;
                    break;
                case "GL_ARB_texture_buffer_range":
                    extensions.ARB_texture_buffer_range = true;
                    break;
                case "GL_ARB_texture_query_levels":
                    extensions.ARB_texture_query_levels = true;
                    break;
                case "GL_ARB_texture_storage_multisample":
                    extensions.ARB_texture_storage_multisample = true;
                    break;
                case "GL_ARB_buffer_storage":
                    extensions.ARB_buffer_storage = true;
                    break;
                case "GL_ARB_clear_texture":
                    extensions.ARB_clear_texture = true;
                    break;
                case "GL_ARB_enhanced_layouts":
                    extensions.ARB_enhanced_layouts = true;
                    break;
                case "GL_ARB_multi_bind":
                    extensions.ARB_multi_bind = true;
                    break;
                case "GL_ARB_query_buffer_object":
                    extensions.ARB_query_buffer_object = true;
                    break;
                case "GL_ARB_texture_mirror_clamp_to_edge":
                    extensions.ARB_texture_mirror_clamp_to_edge = true;
                    break;
                case "GL_ARB_texture_stencil8":
                    extensions.ARB_texture_stencil8 = true;
                    break;
                case "GL_ARB_vertex_type_10f_11f_11f_rev":
                    extensions.ARB_vertex_type_10f_11f_11f_rev = true;
                    break;
                case "GL_ARB_bindless_texture":
                    extensions.ARB_bindless_texture = true;
                    break;
                case "GL_ARB_compute_variable_group_size":
                    extensions.ARB_compute_variable_group_size = true;
                    break;
                case "GL_ARB_indirect_parameters":
                    extensions.ARB_indirect_parameters = true;
                    break;
                case "GL_ARB_seamless_cubemap_per_texture":
                    extensions.ARB_seamless_cubemap_per_texture = true;
                    break;
                case "GL_ARB_shader_draw_parameters":
                    extensions.ARB_shader_draw_parameters = true;
                    break;
                case "GL_ARB_shader_group_vote":
                    extensions.ARB_shader_group_vote = true;
                    break;
                case "GL_ARB_sparse_texture":
                    extensions.ARB_sparse_texture = true;
                    break;
                case "GL_ARB_ES3_1_compatibility":
                    extensions.ARB_ES3_1_compatibility = true;
                    break;
                case "GL_ARB_clip_control":
                    extensions.ARB_clip_control = true;
                    break;
                case "GL_ARB_conditional_render_inverted":
                    extensions.ARB_conditional_render_inverted = true;
                    break;
                case "GL_ARB_derivative_control":
                    extensions.ARB_derivative_control = true;
                    break;
                case "GL_ARB_direct_state_access":
                    extensions.ARB_direct_state_access = true;
                    break;
                case "GL_ARB_get_texture_sub_image":
                    extensions.ARB_get_texture_sub_image = true;
                    break;
                case "GL_ARB_shader_texture_image_samples":
                    extensions.ARB_shader_texture_image_samples = true;
                    break;
                case "GL_ARB_texture_barrier":
                    extensions.ARB_texture_barrier = true;
                    break;
                case "GL_KHR_context_flush_control":
                    extensions.KHR_context_flush_control = true;
                    break;
                case "GL_KHR_robust_buffer_access_behavior":
                    extensions.KHR_robust_buffer_access_behavior = true;
                    break;
                case "GL_KHR_robustness":
                    extensions.KHR_robustness = true;
                    break;
                case "GL_ARB_pipeline_statistics_query":
                    extensions.ARB_pipeline_statistics_query = true;
                    break;
                case "GL_ARB_sparse_buffer":
                    extensions.ARB_sparse_buffer = true;
                    break;
                case "GL_ARB_transform_feedback_overflow_query":
                    extensions.ARB_transform_feedback_overflow_query = true;
                    break;
                // EXT
                case "GL_EXT_gpu_shader4":
                    extensions.EXT_gpu_shader4 = true;
                    break;
                case "GL_EXT_texture_compression_s3tc":
                    extensions.EXT_texture_compression_s3tc = true;
                    break;
                case "GL_EXT_texture_compression_latc":
                    extensions.EXT_texture_compression_latc = true;
                    break;
                case "GL_EXT_transform_feedback":
                    extensions.EXT_transform_feedback = true;
                    break;
                case "GL_EXT_direct_state_access":
                    extensions.EXT_direct_state_access = true;
                    break;
                case "GL_EXT_texture_filter_anisotropic":
                    extensions.EXT_texture_filter_anisotropic = true;
                    break;
                case "GL_EXT_texture_array":
                    extensions.EXT_texture_array = true;
                    break;
                case "GL_EXT_texture_snorm":
                    extensions.EXT_texture_snorm = true;
                    break;
                case "GL_EXT_texture_sRGB_decode":
                    extensions.EXT_texture_sRGB_decode = true;
                    break;
                case "GL_EXT_framebuffer_multisample_blit_scaled":
                    extensions.EXT_framebuffer_multisample_blit_scaled = true;
                    break;
                case "GL_EXT_shader_integer_mix":
                    extensions.EXT_shader_integer_mix = true;
                    break;
                case "GL_EXT_polygon_offset_clamp":
                    extensions.EXT_polygon_offset_clamp = true;
                    break;
                // NV
                case "GL_NV_explicit_multisample":
                    extensions.NV_explicit_multisample = true;
                    break;
                case "GL_NV_shader_buffer_load":
                    extensions.NV_shader_buffer_load = true;
                    break;
                case "GL_NV_vertex_buffer_unified_memory":
                    extensions.NV_vertex_buffer_unified_memory = true;
                    break;
                case "GL_NV_shader_buffer_store":
                    extensions.NV_shader_buffer_store = true;
                    break;
                case "GL_NV_bindless_multi_draw_indirect":
                    extensions.NV_bindless_multi_draw_indirect = true;
                    break;
                case "GL_NV_blend_equation_advanced":
                    extensions.NV_blend_equation_advanced = true;
                    break;
                case "GL_NV_deep_texture3D":
                    extensions.NV_deep_texture3D = true;
                    break;
                case "GL_NV_shader_thread_group":
                    extensions.NV_shader_thread_group = true;
                    break;
                case "GL_NV_shader_thread_shuffle":
                    extensions.NV_shader_thread_shuffle = true;
                    break;
                case "GL_NV_shader_atomic_int64":
                    extensions.NV_shader_atomic_int64 = true;
                    break;
                case "GL_NV_bindless_multi_draw_indirect_count":
                    extensions.NV_bindless_multi_draw_indirect_count = true;
                    break;
                case "GL_NV_uniform_buffer_unified_memory":
                    extensions.NV_uniform_buffer_unified_memory = true;
                    break;
                // AMD
                case "GL_ATI_texture_compression_3dc":
                    extensions.ATI_texture_compression_3dc = true;
                    break;
                case "GL_AMD_depth_clamp_separate":
                    extensions.AMD_depth_clamp_separate = true;
                    break;
                case "GL_AMD_stencil_operation_extended":
                    extensions.AMD_stencil_operation_extended = true;
                    break;
                case "GL_AMD_vertex_shader_viewport_index":
                    extensions.AMD_vertex_shader_viewport_index = true;
                    break;
                case "GL_AMD_vertex_shader_layer":
                    extensions.AMD_vertex_shader_layer = true;
                    break;
                case "GL_AMD_shader_trinary_minmax":
                    extensions.AMD_shader_trinary_minmax = true;
                    break;
                case "GL_AMD_interleaved_elements":
                    extensions.AMD_interleaved_elements = true;
                    break;
                case "GL_AMD_shader_atomic_counter_ops":
                    extensions.AMD_shader_atomic_counter_ops = true;
                    break;
                case "GL_AMD_shader_stencil_value_export":
                    extensions.AMD_shader_stencil_value_export = true;
                    break;
                case "GL_AMD_transform_feedback4":
                    extensions.AMD_transform_feedback4 = true;
                    break;
                case "GL_AMD_gpu_shader_int64":
                    extensions.AMD_gpu_shader_int64 = true;
                    break;
                case "GL_AMD_gcn_shader":
                    extensions.AMD_gcn_shader = true;
                    break;
                // Intel
                case "GL_INTEL_map_texture":
                    extensions.INTEL_map_texture = true;
                    break;
                case "GL_INTEL_fragment_shader_ordering":
                    extensions.INTEL_fragment_shader_ordering = true;
                    break;
                case "GL_INTEL_performance_query":
                    extensions.INTEL_performance_query = true;
                    break;
            }
        }
    }

    public boolean isVAOSupported(OpenGLOptions openGLOptions) {
        return (version.MAJOR_VERSION >= 3 || extensions.ARB_vertex_array_object) && !openGLOptions.isDisableVAOS();
    }

    public boolean isInstancingSupported() {
        return (version.MAJOR_VERSION >= 3 || extensions.ARB_draw_instanced) && extensions.ARB_instanced_arrays;
    }

    public boolean isIndirectDrawSupported() {
        return
            extensions.ARB_draw_indirect &&
            extensions.ARB_multi_draw_indirect &&
            extensions.ARB_buffer_storage;
    }
}
