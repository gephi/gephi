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
package org.gephi.ui.utils;

import java.awt.Color;

/**
 *
 * @author Mathieu Bastian
 */
public class ColorUtils {

    public static String encode(Color color) {
        char[] buf = new char[8];
        String s = Integer.toHexString(color.getRed());
        if (s.length() == 1) {
            buf[0] = '0';
            buf[1] = s.charAt(0);
        } else {
            buf[0] = s.charAt(0);
            buf[1] = s.charAt(1);
        }
        s = Integer.toHexString(color.getGreen());
        if (s.length() == 1) {
            buf[2] = '0';
            buf[3] = s.charAt(0);
        } else {
            buf[2] = s.charAt(0);
            buf[3] = s.charAt(1);
        }
        s = Integer.toHexString(color.getBlue());
        if (s.length() == 1) {
            buf[4] = '0';
            buf[5] = s.charAt(0);
        } else {
            buf[4] = s.charAt(0);
            buf[5] = s.charAt(1);
        }
        s = Integer.toHexString(color.getAlpha());
        if (s.length() == 1) {
            buf[6] = '0';
            buf[7] = s.charAt(0);
        } else {
            buf[6] = s.charAt(0);
            buf[7] = s.charAt(1);
        }
        return String.valueOf(buf);
    }

    public static Color decode(String nm) throws NumberFormatException {
        int i = (int) Long.parseLong(nm, 16); //Bug 4215269
        return new Color((i >> 24) & 0xFF, (i >> 16) & 0xFF, (i >> 8) & 0xFF, i & 0xFF);
    }

    public static Color decode(float[] array) {
        if (array.length == 3) {
            return new Color(array[0], array[1], array[2]);
        } else if (array.length == 4) {
            return new Color(array[0], array[1], array[2], array[3]);
        } else {
            throw new IllegalArgumentException("Must be a 3 or 4 length array");
        }
    }
}
