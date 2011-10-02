/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
Website : http://www.gephi.org

This file is part of Gephi.

DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

Copyright 2011 Gephi Consortium. All rights reserved.

The contents of this file are subject to the terms of either the GNU
General Public License Version 3 only ("GPL") or the Common
Development and Distribution License("CDDL") (collectively, the
"License"). You may not use this file except in compliance with the
License. You can obtain a copy of the License at
http://gephi.org/about/legal/license-notice/
or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
specific language governing permissions and limitations under the
License.  When distributing the software, include this License Header
Notice in each file and include the License files at
/cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
License Header, with the fields enclosed by brackets [] replaced by
your own identifying information:
"Portions Copyrighted [year] [name of copyright owner]"

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 3, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 3] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 3 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 3 code and therefore, elected the GPL
Version 3 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

Contributor(s):

Portions Copyrighted 2011 Gephi Consortium.
*/


package org.gephi.graph.api;

/**
 * Get and set any object to this attributes class. See <b>AttributesAPI</b>
 * module to look at columns definition. This interface is extended by
 * <code>AttributeRow</code> that one can find in the AttributesAPI. Cast this
 * class to <code>AttributeRow</code> to profit from all features.
 * <p>
 * In few words, attribute values can be get with the column index or id.
 * 
 * @author Mathieu Bastian
 */
public interface Attributes {

    /**
     * Returns the number of values this row has.
     * @return          the number of values
     */
    public int countValues();

    /**
     * Returns the value located at the specified column.
     * @param column    the column <b>id</b> or <b>title</b>
     * @return          the value for the specified column position, or
     *                  <code>null</code> if not found
     */
    public Object getValue(String column);

    /**
     * Returns the value located at the specified column position.
     * @param index     the column index
     * @return          the value for the specified column position, or
     *                  <code>null</code> if index out of range
     */
    public Object getValue(int index);

    /**
     * Sets the value for a specified column. Accepts <code>null</code> values.
     * @param column    the column <b>id</b> or <b>title</b>
     * @param value     the value that is to be set at the specified column position
     */
    public void setValue(String column, Object value);

    /**
     * Sets the value for a specified column position. Accepts <code>null</code> values.
     * @param index     the column index
     * @param value     the value that is to be set at the specified column position
     */
    public void setValue(int index, Object value);

    /**
     * Resets the content of the row.
     */
    public void reset();
}
