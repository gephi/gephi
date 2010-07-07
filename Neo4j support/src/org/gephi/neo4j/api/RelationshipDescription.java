package org.gephi.neo4j.api;


import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.RelationshipType;


/**
 *
 * @author Martin Škurla
 */
public class RelationshipDescription {
    private final RelationshipType relationshipType;
    private final Direction direction;


    public RelationshipDescription(RelationshipType relationshipType, Direction direction) {
        this.relationshipType = relationshipType;
        this.direction = direction;
    }


    public Direction getDirection() {
        return direction;
    }

    public RelationshipType getRelationshipType() {
        return relationshipType;
    }
}
