/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
        mruFileList = new ArrayList<String>(maxSize);
        listenerList = new EventListenerList();
        retrieve();
    }

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

    /** {@inheritDoc} */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        listenerList.add(PropertyChangeListener.class, listener);
    }

    /** {@inheritDoc} */
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

    /** Return the backing store Preferences
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
