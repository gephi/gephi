package org.gephi.preview.controller;

import org.gephi.preview.GraphCustomizerImpl;
import org.gephi.preview.api.GraphCustomizer;
import org.gephi.preview.api.PreviewController;

/**
 *
 * @author jeremy
 */
public class PreviewControllerImpl implements PreviewController {

    private final GraphCustomizerImpl customizer = new GraphCustomizerImpl();
    //private GraphImpl graph;

    public GraphCustomizer getCustomizer() {
        return customizer;
    }
}
