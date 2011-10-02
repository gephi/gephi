/*
Copyright 2008-2010 Gephi
Authors : Yi Du <duyi001@gmail.com>
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
package org.gephi.io.spigot.plugin.email;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Map;
import org.openide.util.Exceptions;

/**
 *
 * @author Yi Du
 */
public class Utilities {

    /**
     *
     * @param codecType the current codecType of message
     * @param content content to parse
     * @return string after codec
     */
    public static String codecTranslate(String codecType, String content) {
        if(codecType == null || content == null)
            return content;

        if (codecType.equalsIgnoreCase("UTF-8"))
            return content;
        else if (codecType.equalsIgnoreCase("gb2312")) {
            String s = null;
            try {
                s = new String(content.getBytes("gb2312"), "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                Exceptions.printStackTrace(ex);
                return content;
            }
            return s;
        } else if (codecType.equalsIgnoreCase("gbk")) {
            String s = null;
            try {
                s = new String(content.getBytes("gbk"), "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                Exceptions.printStackTrace(ex);
                return content;
            }
            return s;
        }else if (codecType.equalsIgnoreCase("ISO-8859-1")) {
            String s = null;
            try {
                s = new String(content.getBytes("ISO-8859-1"), "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                Exceptions.printStackTrace(ex);
                return content;
            }
            return s;
        }else if (codecType.equalsIgnoreCase("us-ascii")) {
            String s = null;
            try {
                s = new String(content.getBytes("us-ascii"), "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                Exceptions.printStackTrace(ex);
                return content;
            }
            return s;
        }
        else{// the upper sereval codecs appear frequently, so doing this way to increase speed
            Map<String, Charset> map = Charset.availableCharsets();
            for(String o :map.keySet()){
                if(o.equalsIgnoreCase(codecType)){
                    String s = null;
                    try {
                        s = new String(content.getBytes(o), "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        Exceptions.printStackTrace(ex);
                    return s;
                    }
                }
            }
            //System.out.println("unsupported codec type");
            return content;
        }
    }
}
