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
package org.gephi.desktop.mrufiles.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.event.EventListenerList;
import org.gephi.desktop.mrufiles.api.MostRecentFiles;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = MostRecentFiles.class)
public class MostRecentFilesImpl implements MostRecentFiles {
    //http://blogs.kiyut.com/tonny/2007/08/09/netbeans-platform-and-most-recently-used-file-mru/

    protected static String DEFAULT_NODE_NAME = "prefs";
    protected String nodeName = null;
    private EventListenerList listenerList;
    public static final String MRU_FILE_LIST_PROPERTY = "MRUFileList";
    private List<String> mruFileList;
    private int maxSize;

    public MostRecentFilesImpl() {
        nodeName = "mrufiles";
        maxSize = 9; // default is 9
        mruFileList = new ArrayList<>(maxSize);
        listenerList = new EventListenerList();
        retrieve();
    }

    @Override
    public List<String> getMRUFileList() {
        return mruFileList;
    }

    public void setMRUFileList(List<String> list) {
        this.mruFileList.clear();
        for (int i = 0; i < list.size(); i++) {
            this.mruFileList.add(list.get(i));
            if (i >= maxSize) {
                break;
            }
        }
        firePropertyChange(MRU_FILE_LIST_PROPERTY, null, mruFileList);
        store();
    }

    @Override
    public void addFile(String absolutePath) {
        // remove the old
        mruFileList.remove(absolutePath);

        // add to the top
        mruFileList.add(0, absolutePath);
        while (mruFileList.size() > maxSize) {
            mruFileList.remove(mruFileList.size() - 1);
        }
        firePropertyChange(MRU_FILE_LIST_PROPERTY, null, mruFileList);
        store();
    }

    protected void store() {
        Preferences prefs = getPreferences();

        // clear the backing store
        try {
            prefs.clear();
        } catch (BackingStoreException ex) {
        }

        for (int i = 0; i < mruFileList.size(); i++) {
            String str = mruFileList.get(i);
            prefs.put(MRU_FILE_LIST_PROPERTY + i, str);
        }
    }

    protected void retrieve() {
        mruFileList.clear();
        Preferences prefs = getPreferences();

        for (int i = 0; i < maxSize; i++) {
            String str = prefs.get(MRU_FILE_LIST_PROPERTY + i, null);
            if (str != null) {
                mruFileList.add(str);
            } else {
                break;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listenerList.add(PropertyChangeListener.class, listener);
    }

    /**
     * {@inheritDoc}
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        listenerList.remove(PropertyChangeListener.class, listener);
    }

    protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        PropertyChangeEvent event = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == PropertyChangeListener.class) {
                ((PropertyChangeListener) listeners[i + 1]).propertyChange(event);
            }
        }
    }

    /**
     * Return the backing store Preferences
     *
     * @return Preferences
     */
    protected final Preferences getPreferences() {
        String name = DEFAULT_NODE_NAME;
        if (nodeName != null) {
            name = nodeName;
        }

        Preferences prefs = NbPreferences.forModule(this.getClass()).node("options").node(name);

        return prefs;
    }
}
