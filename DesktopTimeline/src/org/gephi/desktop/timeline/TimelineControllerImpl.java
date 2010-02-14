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
import org.joda.time.DateTime;
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
                if (model == null) {
                    model = new TimelineModelImpl();
                    workspace.add(model);
                }
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
            if (model == null) {
                model = new TimelineModelImpl();
                pc.getCurrentWorkspace().add(model);
            }
        }
    }

    public void pushSlice(Workspace workspace, String from, String to, Object obj) {
        AttributeModel am = workspace.getLookup().lookup(AttributeModel.class);
        AttributeColumn col = null;
        if (am.getNodeTable().hasColumn(COLUMN_KEY)) {
            col = am.getNodeTable().getColumn(COLUMN_KEY);
        } else {
            col = am.getNodeTable().addColumn(COLUMN_KEY, COLUMN_TYPE);
        }

        DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
        double f = Double.NEGATIVE_INFINITY, t = Double.POSITIVE_INFINITY;
        try {
            if (from != null && !from.isEmpty()) {
                f =
                        ((Date) formatter.parse(from)).getTime();
            }
            if (to != null && !to.isEmpty()) {
                t =
                        ((Date) formatter.parse(to)).getTime();
            }
            if (model.getUnit() == null) {
                model.setUnit(DateTime.class);
            }

        } catch (ParseException ex) {
            try {
                if (from != null && !from.isEmpty()) {
                    f = Double.parseDouble(from);
                }
                if (to != null && !to.isEmpty()) {
                    t = Double.parseDouble(to);
                }
                if (model.getUnit() == null) {
                    model.setUnit(Double.class);
                }
            } catch (NumberFormatException ex2) {
                Exceptions.printStackTrace(ex);
            }
        }
        TimeInterval ti = new TimeInterval(f, t);

        if (f < model.getMinValue()) {
            model.setMinValue(f);
            //System.out.println("fixing min to " + f);
        }
        if (t > model.getMaxValue() && t != Double.POSITIVE_INFINITY) {
            model.setMaxValue(t);
            //System.out.println("fixing max to " + new DateTime(new Date((long)t)));
        } else if (f+1>model.getMaxValue()) {
            //System.out.println("fixing max to " + new DateTime(new Date((long)f+1)));
            model.setMaxValue(f + 1);
        }

        if (obj instanceof Node) {
            ((Node) obj).getNodeData().getAttributes().setValue(col.getIndex(), ti);
        } else {
            ((Edge) obj).getEdgeData().getAttributes().setValue(col.getIndex(), ti);
        }
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
            double f = Double.POSITIVE_INFINITY;
            try {
                if (min != null) {
                    f = ((Date) formatter.parse(min)).getTime();
                }
                if (tm.getUnit() == null) {
                    model.setUnit(DateTime.class);
                }
            } catch (ParseException ex) {
                try {
                    f = Double.parseDouble(min);
                    if (tm.getUnit() == null) {
                        model.setUnit(Double.class);
                    }
                } catch (NumberFormatException ex2) {
                    Exceptions.printStackTrace(ex);
                }
            }
            setMin(workspace, f);
        }
    }

    public void setMax(Workspace workspace, String max) {
        TimelineModel tm = workspace.getLookup().lookup(TimelineModel.class);
        if (tm != null) {
            DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
            double f = Double.NEGATIVE_INFINITY;
            try {
                if (max != null) {
                    f = ((Date) formatter.parse(max)).getTime();
                }
                if (tm.getUnit() == null) {
                    tm.setUnit(DateTime.class);
                }
            } catch (ParseException ex) {
                try {
                    f = Double.parseDouble(max);
                    if (model.getUnit() == null) {
                        model.setUnit(Double.class);
                    }
                } catch (NumberFormatException ex2) {
                    Exceptions.printStackTrace(ex);
                }
            }

            setMax(workspace, f);
        }
    }

    public void setMin(Workspace workspace, double min) {
        TimelineModel tm = workspace.getLookup().lookup(TimelineModel.class);
        if (tm != null && min < tm.getMinValue()) {
            tm.setMinValue(min);
        }
    }

    public void setMax(Workspace workspace, double max) {
        TimelineModel tm = workspace.getLookup().lookup(TimelineModel.class);
        if (tm != null && max > tm.getMaxValue()) {
            tm.setMaxValue(max);
        }
    }

    public void setMin(String min) {
        if (model != null) {
            DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
            double f = Double.POSITIVE_INFINITY;
            try {
                if (min != null) {
                    f = ((Date) formatter.parse(min)).getTime();
                }
                if (model.getUnit() == null) {
                    model.setUnit(DateTime.class);
                }
            } catch (ParseException ex) {
                try {
                    f = Double.parseDouble(min);
                    if (model.getUnit() == null) {
                        model.setUnit(Double.class);
                    }
                } catch (NumberFormatException ex2) {
                    Exceptions.printStackTrace(ex);
                }
            }
            setMin(f);
        }
    }

    public void setMax(String max) {
        if (model != null) {
            DateFormat formatter = new SimpleDateFormat(DATE_FORMAT);
            double f = Double.NEGATIVE_INFINITY;
            try {
                if (max != null) {
                    f = ((Date) formatter.parse(max)).getTime();
                }
                if (model.getUnit() == null) {
                    model.setUnit(DateTime.class);
                }
            } catch (ParseException ex) {
                try {
                    f = Double.parseDouble(max);
                    if (model.getUnit() == null) {
                        model.setUnit(Double.class);
                    }
                } catch (NumberFormatException ex2) {
                    Exceptions.printStackTrace(ex);
                }
            }
            setMax(f);
        }
    }

    public void setMin(double min) {
        if (model != null && min < model.getMinValue()) {
            model.setMinValue(min);
        }
    }

    public void setMax(double max) {
        if (model != null && max > model.getMaxValue()) {
            model.setMaxValue(max);
        }
    }

    public void pushSlice(Workspace workspace, String from, String to, Node node) {
        pushSlice(workspace, from, to, (Object) node);
    }

    public void pushSlice(Workspace workspace, String from, String to, Edge edge) {
        pushSlice(workspace, from, to, (Object) edge);
    }
}
