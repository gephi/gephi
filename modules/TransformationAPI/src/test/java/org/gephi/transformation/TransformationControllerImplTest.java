package org.gephi.transformation;

import static org.junit.Assert.assertThrows;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import junit.framework.TestCase;
import org.gephi.graph.api.Graph;

import org.gephi.transformation.api.TransformationController;

import org.gephi.transformation.spi.TransformationOperation;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TransformationControllerImplTest extends TestCase {

    @Test
    public void nominalTest(){
        Graph graph = Mockito.mock(Graph.class);
        when(graph.getNodeCount()).thenReturn(1);

        TransformationOperation operation = Mockito.mock(TransformationOperation.class);

        TransformationController transformationController = new TransformationControllerImpl();
        transformationController.apply(operation, graph);
        verify(graph,times(1)).readLock();
        verify(graph,times(1)).readUnlock();
        verify(operation,times(1)).transformation(graph);


    }
    @Test
    public void onOperationExceptionTest(){

        Graph graph = Mockito.mock(Graph.class);
        when(graph.getNodeCount()).thenReturn(1);

        TransformationOperation operation = Mockito.mock(TransformationOperation.class);
        doThrow(new NullPointerException("")).when(operation).transformation(graph);

        TransformationController transformationController = new TransformationControllerImpl();
        assertThrows(RuntimeException.class, () -> {
            transformationController.apply(operation, graph);
        });
        verify(graph,times(1)).readLock();
        verify(graph,times(1)).readUnlock();
        verify(operation,times(1)).transformation(graph);


    }
    @Test
    public void emptyGraphDoesntRaiseExceptionTest(){
        Graph graph = Mockito.mock(Graph.class);
        when(graph.getNodeCount()).thenReturn(0);

        TransformationOperation operation = Mockito.mock(TransformationOperation.class);
        TransformationController transformationController = new TransformationControllerImpl();
        transformationController.apply(operation, graph);

        verify(graph,times(1)).readLock();
        verify(graph,times(1)).readUnlock();
        verify(operation,times(0)).transformation(graph);


    }

}
