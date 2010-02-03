/*
Copyright 2010 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke, Patrick J. McSweeney
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
package org.gephi.desktop.timeline;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.data.attributes.type.TimeInterval;
import org.gephi.graph.api.Node;
import org.gephi.graph.api.Edge;
import org.gephi.project.api.ProjectController;
import org.gephi.timeline.api.TimelineController;
import org.gephi.timeline.api.TimelineModel;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author jbilcke
 */
@ServiceProvider(service = TimelineController.class)
public class TimelineControllerImpl implements TimelineController {

    private TimelineModel model;
    public static final String COLUMN_KEY = "dynamicrange";
    public static final AttributeType COLUMN_TYPE = AttributeType.TIME_INTERVAL;
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public TimelineControllerImpl() {

        model = null;

        //Workspace events
        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
        pc.addWorkspaceListener(new WorkspaceListener() {

            public void initialize(Workspace workspace) {
            }

            public void select(Workspace workspace) {
                model = workspace.getLookup().lookup(TimelineModelImpl.class);
            }

            public void unselect(Workspace workspace) {
            }

            public void close(Workspace workspace) {
            }

            public void disable() {
                model = null;
            }
        });

        if (pc.getCurrentWorkspace() != null) {
            model = pc.getCurrentWorkspace().getLookup().lookup(TimelineModelImpl.class);
        }
    }

    public void pushSlice(Workspace workspace, String from, String to, Node node) {
        AttributeModel am = workspace.getLookup().lookup(AttributeModel.class);
        AttributeColumn col = null;
        if (am.getNodeTable().hasColumn(COLUMN_KEY)) {
            col = am.getNodeTable().getColumn(COLUMN_KEY);
        } else {
            col = am.getNodeTable().addColumn(COLUMN_KEY, COLUMN_TYPE);
        }

        DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        double f = 0.0, t = 0.0;
        try {
            f = (from==null || from.isEmpty())
                    ? Double.NEGATIVE_INFINITY
                    : ((Date) formatter.parse(from)).getTime();
            t = (to==null || to.isEmpty())
                    ? Double.POSITIVE_INFINITY
                    : ((Date) formatter.parse(to)).getTime();
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        node.getNodeData().getAttributes().setValue(col.getIndex(), new TimeInterval(f, t));
    }

    public void pushSlice(Workspace workspace, String from, String to, Edge edge) {
        AttributeModel am = workspace.getLookup().lookup(AttributeModel.class);

        AttributeColumn col = null;
        if (am.getEdgeTable().hasColumn(COLUMN_KEY)) {
            col = am.getEdgeTable().getColumn(COLUMN_KEY);
        } else {
            col = am.getEdgeTable().addColumn(COLUMN_KEY, COLUMN_TYPE);
        }
        DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        double f = 0.0, t = 0.0;
        try {
            f = (from==null || from.isEmpty())
                    ? Double.NEGATIVE_INFINITY
                    : ((Date) formatter.parse(from)).getTime();
            t = (to==null || to.isEmpty())
                    ? Double.POSITIVE_INFINITY
                    : ((Date) formatter.parse(to)).getTime();
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
        edge.getEdgeData().getAttributes().setValue(col.getIndex(), new TimeInterval(f, t));
    }

    public TimelineModel getModel(Workspace workspace) {
        return workspace.getLookup().lookup(TimelineModel.class);
    }

    public double getFrom(Workspace workspace) {
        TimelineModel tm = workspace.getLookup().lookup(TimelineModel.class);
        if (tm != null) {
            return tm.getFromValue();
        }
        return 0;
    }

    public double getTo(Workspace workspace) {
        TimelineModel tm = workspace.getLookup().lookup(TimelineModel.class);
        if (tm != null) {
            return tm.getToValue();
        }
        return 0;
    }

    public TimeInterval getTimeInterval(Workspace workspace) {
        TimelineModel tm = workspace.getLookup().lookup(TimelineModel.class);
        if (tm != null) {
            return tm.getTimeInterval();
        }
        return null;
    }

    public TimelineModel getModel() {
        return model;
    }

    public TimeInterval getTimeInterval() {
        if (model != null) {
            return model.getTimeInterval();
        }
        return null;
    }

    public double getFrom() {
        if (model != null) {
            return model.getFromValue();

        }
        return 0;
    }

    public double getTo() {
        if (model != null) {
            return model.getToValue();

        }
        return 0;
    }

    public void setMin(Workspace workspace, String min) {
        TimelineModel tm = workspace.getLookup().lookup(TimelineModel.class);
        if (tm != null) {
            DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
            double f = 0.0;
            try {
                f = ((Date) formatter.parse(min)).getTime();
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
            tm.setMinValue(f);
        }
    }

    public void setMax(Workspace workspace, String max) {
        TimelineModel tm = workspace.getLookup().lookup(TimelineModel.class);
        if (tm != null) {
            DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
            double f = 1.0;
            try {
                f = ((Date) formatter.parse(max)).getTime();
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
            tm.setMaxValue(f);
        }
    }

    public void setMin(Workspace workspace, double min) {
        TimelineModel tm = workspace.getLookup().lookup(TimelineModel.class);
        if (tm != null) {
            tm.setMinValue(min);
        }
    }

    public void setMax(Workspace workspace, double max) {
        TimelineModel tm = workspace.getLookup().lookup(TimelineModel.class);
        if (tm != null) {
            tm.setMaxValue(max);
        }
    }

    public void setMin(String min) {
        if (model != null) {
            DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
            double f = 0.0;
            try {
                f = ((Date) formatter.parse(min)).getTime();
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
            model.setMinValue(f);
        }
    }

    public void setMax(String max) {
        if (model != null) {
            DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
            double f = 0.0;
            try {
                f = ((Date) formatter.parse(max)).getTime();
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }
            model.setMaxValue(f);
        }
    }

    public void setMin(double min) {
        if (model != null) {
            model.setMinValue(min);
        }
    }

    public void setMax(double max) {
        if (model != null) {
            model.setMaxValue(max);
        }
    }
}
