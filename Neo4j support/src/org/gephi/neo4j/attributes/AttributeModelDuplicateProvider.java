/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.neo4j.attributes;

import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspaceDuplicateProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = WorkspaceDuplicateProvider.class, position = 100)
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
