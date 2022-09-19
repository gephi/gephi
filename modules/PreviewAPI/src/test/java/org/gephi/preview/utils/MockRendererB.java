package org.gephi.preview.utils;

import org.gephi.preview.api.CanvasSize;
import org.gephi.preview.api.Item;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.preview.api.RenderTarget;
import org.gephi.preview.spi.ItemBuilder;
import org.gephi.preview.spi.Renderer;

public class MockRendererB implements Renderer {
    @Override
    public String getDisplayName() {
        return "B";
    }

    @Override
    public void preProcess(PreviewModel previewModel) {

    }

    @Override
    public void render(Item item, RenderTarget target, PreviewProperties properties) {

    }

    @Override
    public void postProcess(PreviewModel previewModel, RenderTarget target, PreviewProperties properties) {

    }

    @Override
    public PreviewProperty[] getProperties() {
        return new PreviewProperty[0];
    }

    @Override
    public boolean isRendererForitem(Item item, PreviewProperties properties) {
        return false;
    }

    @Override
    public boolean needsItemBuilder(ItemBuilder itemBuilder, PreviewProperties properties) {
        return false;
    }

    @Override
    public CanvasSize getCanvasSize(Item item, PreviewProperties properties) {
        return null;
    }
}
