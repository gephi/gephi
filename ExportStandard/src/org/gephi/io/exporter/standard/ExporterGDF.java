/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.io.exporter.standard;

import java.io.BufferedWriter;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;
import org.gephi.io.exporter.GraphFileExporter;
import org.gephi.io.exporter.FileType;
import org.gephi.io.exporter.TextExporter;
import org.gephi.utils.longtask.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class ExporterGDF implements GraphFileExporter, TextExporter, LongTask {

    private boolean cancel = false;
    private ProgressTicket progressTicket;

    //Columns
    private NodeColumnsGDF[] defaultNodeColumnsGDFs;
    private EdgeColumnsGDF[] defaultEdgeColumnsGDFs;

    public boolean exportData(BufferedWriter writer) throws Exception {

        Progress.start(progressTicket);

        writer.write("test");

        Progress.finish(progressTicket);
        return !cancel;
    }

    private void defaultNodeColumns() {
        NodeColumnsGDF labelColumn = new NodeColumnsGDF("label") {

            @Override
            public boolean isEnable() {
                return true;
            }

            @Override
            public void writeData(StringBuilder builder, Node node) {
            }
        };

        NodeColumnsGDF visibleColumn = new NodeColumnsGDF("visible", DataTypeGDF.BOOLEAN) {

            @Override
            public boolean isEnable() {
                return true;
            }

            @Override
            public void writeData(StringBuilder builder, Node node) {
            }
        };

        NodeColumnsGDF labelVisibleColumn = new NodeColumnsGDF("labelvisible", DataTypeGDF.BOOLEAN) {

            @Override
            public boolean isEnable() {
                return true;
            }

            @Override
            public void writeData(StringBuilder builder, Node node) {
            }
        };

        NodeColumnsGDF widthColumn = new NodeColumnsGDF("width", DataTypeGDF.DOUBLE) {

            @Override
            public boolean isEnable() {
                return true;
            }

            @Override
            public void writeData(StringBuilder builder, Node node) {
            }
        };

        NodeColumnsGDF heightColumn = new NodeColumnsGDF("height", DataTypeGDF.DOUBLE) {

            @Override
            public boolean isEnable() {
                return true;
            }

            @Override
            public void writeData(StringBuilder builder, Node node) {
            }
        };

        NodeColumnsGDF xColumn = new NodeColumnsGDF("x", DataTypeGDF.DOUBLE) {

            @Override
            public boolean isEnable() {
                return true;
            }

            @Override
            public void writeData(StringBuilder builder, Node node) {
            }
        };

        NodeColumnsGDF yColumn = new NodeColumnsGDF("y", DataTypeGDF.DOUBLE) {

            @Override
            public boolean isEnable() {
                return true;
            }

            @Override
            public void writeData(StringBuilder builder, Node node) {
            }
        };

        NodeColumnsGDF colorColumn = new NodeColumnsGDF("color") {

            @Override
            public boolean isEnable() {
                return true;
            }

            @Override
            public void writeData(StringBuilder builder, Node node) {
            }
        };

        NodeColumnsGDF fixedColumn = new NodeColumnsGDF("fixed", DataTypeGDF.BOOLEAN) {

            @Override
            public boolean isEnable() {
                return true;
            }

            @Override
            public void writeData(StringBuilder builder, Node node) {
            }
        };

        NodeColumnsGDF styleColumn = new NodeColumnsGDF("style", DataTypeGDF.INT) {

            @Override
            public boolean isEnable() {
                return true;
            }

            @Override
            public void writeData(StringBuilder builder, Node node) {
            }
        };

        defaultNodeColumnsGDFs = new NodeColumnsGDF[10];
        defaultNodeColumnsGDFs[0] = labelColumn;
        defaultNodeColumnsGDFs[1] = visibleColumn;
        defaultNodeColumnsGDFs[2] = labelVisibleColumn;
        defaultNodeColumnsGDFs[3] = widthColumn;
        defaultNodeColumnsGDFs[4] = heightColumn;
        defaultNodeColumnsGDFs[5] = xColumn;
        defaultNodeColumnsGDFs[6] = yColumn;
        defaultNodeColumnsGDFs[7] = colorColumn;
        defaultNodeColumnsGDFs[8] = fixedColumn;
        defaultNodeColumnsGDFs[9] = styleColumn;
    }

    private void defaultEdgeColumns() {
        EdgeColumnsGDF labelColumn = new EdgeColumnsGDF("label") {

            @Override
            public boolean isEnable() {
                return true;
            }

            @Override
            public void writeData(StringBuilder builder, Edge node) {
            }
        };

        EdgeColumnsGDF weightColumn = new EdgeColumnsGDF("weight", DataTypeGDF.DOUBLE) {

            @Override
            public boolean isEnable() {
                return true;
            }

            @Override
            public void writeData(StringBuilder builder, Edge node) {
            }
        };

        EdgeColumnsGDF directedColumn = new EdgeColumnsGDF("directed", DataTypeGDF.BOOLEAN) {

            @Override
            public boolean isEnable() {
                return true;
            }

            @Override
            public void writeData(StringBuilder builder, Edge node) {
            }
        };

        EdgeColumnsGDF colorColumn = new EdgeColumnsGDF("color") {

            @Override
            public boolean isEnable() {
                return true;
            }

            @Override
            public void writeData(StringBuilder builder, Edge node) {
            }
        };

        EdgeColumnsGDF visibleColumn = new EdgeColumnsGDF("visible", DataTypeGDF.BOOLEAN) {

            @Override
            public boolean isEnable() {
                return true;
            }

            @Override
            public void writeData(StringBuilder builder, Edge node) {
            }
        };

        EdgeColumnsGDF labelVisibleColumn = new EdgeColumnsGDF("labelvisible", DataTypeGDF.BOOLEAN) {

            @Override
            public boolean isEnable() {
                return true;
            }

            @Override
            public void writeData(StringBuilder builder, Edge node) {
            }
        };

        defaultEdgeColumnsGDFs = new EdgeColumnsGDF[6];
        defaultEdgeColumnsGDFs[0] = labelColumn;
        defaultEdgeColumnsGDFs[1] = weightColumn;
        defaultEdgeColumnsGDFs[2] = directedColumn;
        defaultEdgeColumnsGDFs[3] = colorColumn;
        defaultEdgeColumnsGDFs[4] = visibleColumn;
        defaultEdgeColumnsGDFs[5] = labelVisibleColumn;
    }

    public boolean cancel() {
        cancel = true;
        return true;
    }

    public void setProgressTicket(ProgressTicket progressTicket) {
        this.progressTicket = progressTicket;
    }

    public String getName() {
        return NbBundle.getMessage(getClass(), "ExporterGDF_name");
    }

    public FileType[] getFileTypes() {
        FileType ft = new FileType(".gdf", NbBundle.getMessage(getClass(), "fileType_GDF_Name"));
        return new FileType[]{ft};
    }

    private enum DataTypeGDF {

        VARCHAR, BOOL, BOOLEAN, INTEGER, TINYINT, INT, DOUBLE, FLOAT
    };

    private abstract class NodeColumnsGDF {

        protected final String title;
        protected final DataTypeGDF type;
        protected final Object defaultValue;

        public NodeColumnsGDF(String title) {
            this(title, DataTypeGDF.VARCHAR);
        }

        public NodeColumnsGDF(String title, DataTypeGDF type) {
            this(title, type, null);
        }

        public NodeColumnsGDF(String title, DataTypeGDF type, Object defaultValue) {
            this.title = title;
            this.type = type;
            this.defaultValue = defaultValue;
        }

        public abstract boolean isEnable();

        public abstract void writeData(StringBuilder builder, Node node);

        public String getTitle() {
            return title;
        }

        public DataTypeGDF getType() {
            return type;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }
    }

    private abstract class EdgeColumnsGDF {

        protected final String title;
        protected final DataTypeGDF type;
        protected final Object defaultValue;

        public EdgeColumnsGDF(String title) {
            this(title, DataTypeGDF.VARCHAR);
        }

        public EdgeColumnsGDF(String title, DataTypeGDF type) {
            this(title, type, null);
        }

        public EdgeColumnsGDF(String title, DataTypeGDF type, Object defaultValue) {
            this.title = title;
            this.type = type;
            this.defaultValue = defaultValue;
        }

        public abstract boolean isEnable();

        public abstract void writeData(StringBuilder builder, Edge edge);

        public String getTitle() {
            return title;
        }

        public DataTypeGDF getType() {
            return type;
        }

        public Object getDefaultValue() {
            return defaultValue;
        }
    }
}
