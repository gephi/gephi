/*
Copyright 2008-2010 Gephi
Authors : Martin Škurla <bujacik@gmail.com>
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
package org.gephi.data.attributes.type;

import org.gephi.data.attributes.api.AttributeType;

/**
 * Complex type that define a list of String items. Can be created from a String
 * array, from a char array or from single string using either given or default separators.
 * <p>
 * String list is useful when, for a particular type, the number of string
 * that define an element is not known by advance.
 *
 * @author Martin Škurla
 * @author Mathieu Bastian
 * @see AttributeType
 */
public final class StringList extends AbstractList<String> {

    /**
     * Create a new string from a char array. One char per list cell.
     *
     * @param list      the list
     */
    public StringList(char[] list) {
        super(StringList.parse(list));
    }

    /**
     * Create a new string list with the given items.
     *
     * @param list      the list of string items
     */
    public StringList(String[] list) {
        super(list);
    }

    /**
     * Create a new string list with items found in the given value. Default
     * separators <code>,|;</code> are used to split the string in a list.
     *
     * @param input     a string with default separators
     */
    public StringList(String input) {
        this(input, AbstractList.DEFAULT_SEPARATOR);
    }

    /**
     * Create a new string list with items found using given separators.
     *
     * @param input     a string with separators defined in <code>separator</code>
     * @param separator the separators chars that are to be used to split
     *                  <code>value</code>
     */
    public StringList(String input, String separator) {
        super(input, separator, String.class);
    }

    private static String[] parse(char[] list) {
        String[] resultList = new String[list.length];

        for (int i = 0; i < list.length; i++) {
            resultList[i] = "" + list[i];
        }

        return resultList;
    }

    /**
     * Returns the item at the specified <code>index</code>. May return
     * <code>null</code> if <code>index</code> is out of range.
     *
     * @param index     the position in the string list
     * @return          the item at the specified position, or <code>null</code>
     */
    public String getString(int index) {
        return getItem(index);
    }
}
