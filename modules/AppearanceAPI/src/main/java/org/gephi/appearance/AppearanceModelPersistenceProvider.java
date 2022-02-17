package org.gephi.appearance;

import java.awt.Color;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.XMLEvent;
import org.gephi.appearance.api.Interpolator;
import org.gephi.graph.api.AttributeUtils;
import org.gephi.graph.api.Column;
import org.gephi.project.api.Workspace;
import org.gephi.project.spi.WorkspacePersistenceProvider;
import org.gephi.project.spi.WorkspaceXMLPersistenceProvider;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = WorkspacePersistenceProvider.class)
public class AppearanceModelPersistenceProvider implements WorkspaceXMLPersistenceProvider {


    @Override
    public void writeXML(XMLStreamWriter writer, Workspace workspace) {
        AppearanceModelImpl model = workspace.getLookup().lookup(AppearanceModelImpl.class);
        if (model != null) {
            try {
                writeXML(writer, model);
            } catch (XMLStreamException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public void readXML(XMLStreamReader reader, Workspace workspace) {
        AppearanceModelImpl model = workspace.getLookup().lookup(AppearanceModelImpl.class);
        if (model == null) {
            model = new AppearanceModelImpl(workspace);
            workspace.add(model);
        }
        try {
            readXML(reader, model);
        } catch (XMLStreamException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public String getIdentifier() {
        return "appearancemodel";
    }

    protected void writeXML(XMLStreamWriter writer, AppearanceModelImpl model)
        throws XMLStreamException {
        writer.writeStartElement("localscale");
        writer.writeAttribute("ranking", String.valueOf(model.isRankingLocalScale()));
        writer.writeAttribute("partition", String.valueOf(model.isPartitionLocalScale()));
        writer.writeEndElement();

        //Rankings
        writeRankings(writer, model.getNodeRankings(), "node");
        writeRankings(writer, model.getEdgeRankings(), "edge");

        //Partitions
        writePartitions(writer, model.getNodePartitions(), "node");
        writePartitions(writer, model.getEdgePartitions(), "edge");
    }

    public void readXML(XMLStreamReader reader, AppearanceModelImpl model) throws XMLStreamException {
        boolean end = false;
        while (reader.hasNext() && !end) {
            Integer eventType = reader.next();
            if (eventType.equals(XMLEvent.START_ELEMENT)) {
                String name = reader.getLocalName();
                if ("localscale".equalsIgnoreCase(name)) {
                    String partition = reader.getAttributeValue(null, "partition");
                    String ranking = reader.getAttributeValue(null, "ranking");
                    model.setPartitionLocalScale(Boolean.parseBoolean(partition));
                    model.setRankingLocalScale(Boolean.parseBoolean(ranking));
                } else if ("rankings".equalsIgnoreCase(name)) {
                    String elementClass = reader.getAttributeValue(null, "for");
                    readRankings(reader,
                        elementClass.equals("node") ? model.getNodeRankings() : model.getEdgeRankings());
                } else if ("partitions".equalsIgnoreCase(name)) {
                    String elementClass = reader.getAttributeValue(null, "for");
                    readPartitions(reader,
                        elementClass.equals("node") ? model.getNodePartitions() : model.getEdgePartitions());
                }
            } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                if (getIdentifier().equalsIgnoreCase(reader.getLocalName())) {
                    end = true;
                }
            }
        }
    }

    protected void writeRankings(XMLStreamWriter writer, RankingImpl[] rankings, String elementClass)
        throws XMLStreamException {
        writer.writeStartElement("rankings");
        writer.writeAttribute("for", elementClass);
        for (RankingImpl ranking : rankings) {
            if (ranking.getInterpolator() != RankingImpl.DEFAULT_INTERPOLATOR) {
                writer.writeStartElement("ranking");
                writer.writeAttribute("class", ranking.getClass().getSimpleName());
                if (ranking instanceof AttributeRankingImpl) {
                    Column col = ranking.getColumn();
                    writer.writeAttribute("column", col != null ? col.getId() : "");
                }
                writeInterpolator(writer, ranking.getInterpolator());
                writer.writeEndElement();
            }
        }
        writer.writeEndElement();
    }

    protected void readRankings(XMLStreamReader reader, RankingImpl[] rankings) throws XMLStreamException {
        Map<String, RankingImpl> graphRankings =
            Arrays.stream(rankings).filter(r -> !(r instanceof AttributeRankingImpl)).collect(
                Collectors.toMap(r -> r.getClass().getSimpleName(), r -> r));
        Map<String, RankingImpl> attributeRankings =
            Arrays.stream(rankings).filter(r -> r instanceof AttributeRankingImpl).filter(r -> r.getColumn() != null)
                .collect(
                    Collectors.toMap(r -> r.getColumn().getId(), r -> r));

        RankingImpl ranking = null;
        boolean end = false;
        while (reader.hasNext() && !end) {
            Integer eventType = reader.next();
            if (eventType.equals(XMLEvent.START_ELEMENT)) {
                String name = reader.getLocalName();
                if ("ranking".equalsIgnoreCase(name)) {
                    String rankingClass = reader.getAttributeValue(null, "class");
                    String rankingColumn = reader.getAttributeValue(null, "column");
                    if (rankingColumn != null) {
                        ranking = attributeRankings.get(rankingColumn);
                    } else {
                        ranking = graphRankings.get(rankingClass);
                    }
                } else if ("interpolator".equalsIgnoreCase(name) && ranking != null) {
                    readInterpolator(reader, ranking);
                }
            } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                ranking = null;
                if ("rankings".equalsIgnoreCase(reader.getLocalName())) {
                    end = true;
                }
            }
        }
    }

    protected void writeInterpolator(XMLStreamWriter writer, Interpolator interpolator)
        throws XMLStreamException {
        String type = null;
        if (interpolator == Interpolator.LOG2) {
            type = "log2";
        } else if (interpolator == Interpolator.LINEAR) {
            type = "linear";
        } else if (interpolator instanceof Interpolator.BezierInterpolator) {
            type = "bezier";
        }
        if (type != null) {
            writer.writeStartElement("interpolator");
            writer.writeAttribute("type", type);
            if (type.equals("bezier")) {
                Interpolator.BezierInterpolator bezierInterpolator =
                    (Interpolator.BezierInterpolator) interpolator;
                writer.writeAttribute("x1", String.valueOf(bezierInterpolator.getControl1().getX()));
                writer.writeAttribute("y1", String.valueOf(bezierInterpolator.getControl1().getY()));
                writer.writeAttribute("x2", String.valueOf(bezierInterpolator.getControl2().getX()));
                writer.writeAttribute("y2", String.valueOf(bezierInterpolator.getControl2().getY()));
            }
            writer.writeEndElement();
        }
    }

    protected void readInterpolator(XMLStreamReader reader, RankingImpl ranking) {
        String type = reader.getAttributeValue(null, "type");
        Interpolator interpolator = null;
        switch (type) {
            case "log2":
                interpolator = Interpolator.LOG2;
                break;
            case "linear":
                interpolator = Interpolator.LINEAR;
                break;
            case "bezier":
                float x1 = Float.parseFloat(reader.getAttributeValue(null, "x1"));
                float y1 = Float.parseFloat(reader.getAttributeValue(null, "y1"));
                float x2 = Float.parseFloat(reader.getAttributeValue(null, "x2"));
                float y2 = Float.parseFloat(reader.getAttributeValue(null, "y2"));
                interpolator = new Interpolator.BezierInterpolator(x1, y1, x2, y2);
                break;
        }
        if (interpolator != null) {
            ranking.setInterpolator(interpolator);
        }
    }

    protected void writePartitions(XMLStreamWriter writer, PartitionImpl[] partitions, String elementClass)
        throws XMLStreamException {
        writer.writeStartElement("partitions");
        writer.writeAttribute("for", elementClass);
        for (PartitionImpl partition : partitions) {
            if (!partition.colorMap.isEmpty()) {
                writer.writeStartElement("partition");
                writer.writeAttribute("class", partition.getClass().getSimpleName());
                if (partition instanceof AttributePartitionImpl) {
                    Column col = partition.getColumn();
                    writer.writeAttribute("column", col != null ? col.getId() : "");
                }
                for (Map.Entry<Object, Color> entry : partition.colorMap.entrySet()) {
                    String key = AttributeUtils.print(entry.getKey());
                    int rgba = (entry.getValue().getAlpha() << 24) | entry.getValue().getRGB();
                    writer.writeStartElement("color");
                    writer.writeAttribute("for", key);
                    writer.writeAttribute("rgba", String.valueOf(rgba));
                    writer.writeEndElement();
                }
                writer.writeEndElement();
            }
        }
        writer.writeEndElement();
    }

    protected void readPartitions(XMLStreamReader reader, PartitionImpl[] partitions) throws XMLStreamException {
        Map<String, PartitionImpl> graphPartitions =
            Arrays.stream(partitions).filter(r -> !(r instanceof AttributePartitionImpl)).collect(
                Collectors.toMap(r -> r.getClass().getSimpleName(), r -> r));
        Map<String, PartitionImpl> attributePartitions =
            Arrays.stream(partitions).filter(r -> r instanceof AttributePartitionImpl)
                .filter(r -> r.getColumn() != null)
                .collect(
                    Collectors.toMap(r -> r.getColumn().getId(), r -> r));

        PartitionImpl partition = null;
        boolean end = false;
        while (reader.hasNext() && !end) {
            Integer eventType = reader.next();
            if (eventType.equals(XMLEvent.START_ELEMENT)) {
                String name = reader.getLocalName();
                if ("partition".equalsIgnoreCase(name)) {
                    String partitionClass = reader.getAttributeValue(null, "class");
                    String partitionColumn = reader.getAttributeValue(null, "column");
                    if (partitionColumn != null) {
                        partition = attributePartitions.get(partitionColumn);
                    } else {
                        partition = graphPartitions.get(partitionClass);
                    }
                } else if ("color".equalsIgnoreCase(name) && partition != null) {
                    Color color = new Color(Integer.parseInt(reader.getAttributeValue(null, "rgba")), true);
                    String keyStr = reader.getAttributeValue(null, "for");
                    Object key = AttributeUtils.parse(keyStr, partition.getValueType());
                    partition.colorMap.put(key, color);
                }
            } else if (eventType.equals(XMLStreamReader.END_ELEMENT)) {
                if ("partitions".equalsIgnoreCase(reader.getLocalName())) {
                    end = true;
                } else if ("partition".equalsIgnoreCase(reader.getLocalName())) {
                    partition = null;
                }
            }
        }
    }
}
