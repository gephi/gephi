/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.data.attributes;

import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.workspace.api.Workspace;
import org.gephi.workspace.spi.WorkspaceDuplicateProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = WorkspaceDuplicateProvider.class)
public class AttributeModelDuplicateProvider implements WorkspaceDuplicateProvider {

    public void duplicate(Workspace source, Workspace destination) {
        AttributeModel sourceModel = source.getLookup().lookup(AttributeModel.class);
        if (sourceModel != null) {
            AttributeModel destModel = destination.getLookup().lookup(AttributeModel.class);
            if (destModel == null) {
                //Create?
            }
            destModel.mergeModel(sourceModel);
        }
    }
}
