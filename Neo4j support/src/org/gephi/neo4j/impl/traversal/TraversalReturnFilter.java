package org.gephi.neo4j.impl.traversal;

import java.util.Collection;
import org.gephi.neo4j.api.FilterDescription;
import org.gephi.neo4j.api.FilterOperator;
import org.neo4j.commons.Predicate;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.traversal.Position;

/**
 *
 * @author Martin Škurla
 */
public class TraversalReturnFilter implements Predicate<Position> {
    private Collection<FilterDescription> filterDescriptions;
    private boolean restrictMode;
    private boolean matchCase;

    public TraversalReturnFilter(Collection<FilterDescription> filterDescriptions, boolean restrictMode, boolean matchCase) {
        this.filterDescriptions = filterDescriptions;
        this.restrictMode = restrictMode;
        this.matchCase = matchCase;
    }

    @Override
    public boolean accept(Position position) {
        Node node = position.node();

        for (FilterDescription filterDescription : filterDescriptions) {
            if (node.hasProperty(filterDescription.getPropertyKey())) {
                Object nodePropertyValue = node.getProperty(filterDescription.getPropertyKey());

                boolean isValid =
                    doValidation(nodePropertyValue, filterDescription.getOperator(), filterDescription.getPropertyValue());

                if (isValid == false)
                    return false;
            }
            else
                return !restrictMode;
        }
        return true;
    }

    private boolean doValidation(Object nodePropertyValue, FilterOperator operator, String expectedValue) {
        try {
            if (TypeHelper.isWholeNumber(nodePropertyValue))
                return operator.executeOnWholeNumbers((Number) nodePropertyValue,
                                                       TypeHelper.parseWholeNumber(expectedValue));

            else if (TypeHelper.isRealNumber(nodePropertyValue))
                return operator.executeOnRealNumbers((Number) nodePropertyValue,
                                                      TypeHelper.parseRealNumber(expectedValue));

            else if (TypeHelper.isBoolean(nodePropertyValue))
                return operator.executeOnBooleans((Boolean) nodePropertyValue,
                                                   TypeHelper.parseBoolean(expectedValue));

            else if (TypeHelper.isCharacter(nodePropertyValue))
                return operator.executeOnCharacters((Character) nodePropertyValue,
                                                     TypeHelper.parseCharacter(expectedValue),
                                                     matchCase);

            else if (TypeHelper.isArray(nodePropertyValue)) {
                return false;//TODO finish
            }

            else
                return operator.executeOnStrings((String) nodePropertyValue, expectedValue, matchCase);
        }
        catch (NotParsableException npe) {
            return false;
        }
    }
}
