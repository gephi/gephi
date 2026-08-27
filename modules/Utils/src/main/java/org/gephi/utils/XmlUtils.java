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

package org.gephi.utils;

/**
 * Utility methods for producing well-formed XML.
 */
public class XmlUtils {

    private XmlUtils() {
    }

    /**
     * Removes the null character (U+0000) from the given string.
     * <p>
     * XML forbids the null character even as a numeric character reference, in both XML 1.0 and XML 1.1,
     * so it is the only character that cannot be preserved when writing XML text or attribute values and
     * must be dropped rather than escaped. Every other control character can be written as-is by an XML
     * 1.1 writer, which escapes it to a character reference automatically instead of rejecting it.
     *
     * @param str string to sanitize, can be null
     * @return the sanitized string, or the original string if it did not contain a null character, or null
     * if the input was null
     */
    public static String stripInvalidXmlChars(String str) {
        if (str == null || str.indexOf(0) == -1) {
            return str;
        }
        return str.replace("\0", "");
    }
}
