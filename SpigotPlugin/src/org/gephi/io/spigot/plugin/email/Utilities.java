/*
Copyright 2008-2010 Gephi
Authors : Yi Du <duyi001@gmail.com>
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
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
