/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s): Soot Phengsy
 */

package org.netbeans.swing.dirchooser;

import java.util.logging.Level;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.*;
import javax.swing.plaf.basic.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.security.AccessControlException;
import java.text.MessageFormat;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.border.EmptyBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.UIResource;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.swing.dirchooser.spi.CustomDirectoryProvider;
import org.openide.awt.HtmlRenderer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;


/**
 * An implementation of a customized filechooser.
 *
 * @author Soot Phengsy, inspired by Jeff Dinkins' Swing version
 */
public class DirectoryChooserUI extends BasicFileChooserUI {

    private static final Dimension horizontalStrut1 = new Dimension(25, 1);
    private static final Dimension verticalStrut1  = new Dimension(1, 4);
    private static final Dimension verticalStrut2  = new Dimension(1, 6);
    private static final Dimension verticalStrut3  = new Dimension(1, 8);
    private static Dimension PREF_SIZE = new Dimension(425, 245);
    private static Dimension MIN_SIZE = new Dimension(425, 245);
    private static Dimension TREE_PREF_SIZE = new Dimension(380, 230);
    private static final int ACCESSORY_WIDTH = 250;
    
    private static final Logger LOG = Logger.getLogger(DirectoryChooserUI.class.getName());

    private static final String TIMEOUT_KEY="nb.fileChooser.timeout"; // NOI18N

    private JPanel centerPanel;
    
    private JLabel lookInComboBoxLabel;
    
    private JComboBox directoryComboBox;
    
    private DirectoryComboBoxModel directoryComboBoxModel;
    
    private ActionListener directoryComboBoxAction = new DirectoryComboBoxAction();
    
    private FilterTypeComboBoxModel filterTypeComboBoxModel;
    
    private JTextField filenameTextField;
    
    private JComponent placesBar;
    private boolean placesBarFailed = false;
    
    private JButton approveButton;
    private JButton cancelButton;
    
    private JPanel buttonPanel;
    private JPanel bottomPanel;
    
    private JComboBox filterTypeComboBox;
    
    private int lookInLabelMnemonic = 0;
    private String lookInLabelText = null;
    private String saveInLabelText = null;
    
    private int fileNameLabelMnemonic = 0;
    private String fileNameLabelText = null;
    
    private int filesOfTypeLabelMnemonic = 0;
    private String filesOfTypeLabelText = null;
    
    private String upFolderToolTipText = null;
    private String upFolderAccessibleName = null;
    
    private String newFolderToolTipText = null;
    
    private String homeFolderTooltipText = null;
    
    private Action newFolderAction = new NewDirectoryAction();
    
    private BasicFileView fileView = new DirectoryChooserFileView();
    
    private static JTree tree;
    
    private DirectoryTreeModel model;
    
    private DirectoryNode newFolderNode;
    
    private JComponent treeViewPanel;
    
    private InputBlocker blocker;
    
    private static JFileChooser fileChooser;
    
    private boolean changeDirectory = true;
    
    private boolean showPopupCompletion = false;
    
    private boolean addNewDirectory = false;
    
    private JPopupMenu popupMenu;
    
    private FileCompletionPopup completionPopup;
    
    private RequestProcessor.Task updateWorker;
    
    private boolean useShellFolder = false;
    
    private JButton upFolderButton;
    private JButton newFolderButton;
    
    private JComponent topCombo, topComboWrapper, topToolbar;
    private JPanel slownessPanel;

    private CustomDirectoryProvider customDirectoryProvider;

    public static ComponentUI createUI(JComponent c) {
        return new DirectoryChooserUI((JFileChooser) c);
    }

    public DirectoryChooserUI(JFileChooser filechooser) {
        super(filechooser);

        Collection<? extends CustomDirectoryProvider> directoryProviders =
                Lookup.getDefault().lookupAll(CustomDirectoryProvider.class);

        customDirectoryProvider = null;
        for (CustomDirectoryProvider directoryProvider : directoryProviders) {
            if (directoryProvider.isEnabled()) {
                this.customDirectoryProvider = directoryProvider;
                break;
            }
        }
    }
    
    public void installUI(JComponent c) {
        super.installUI(c);
    }
    
    public void uninstallComponents(JFileChooser fc) {
        fc.removeAll();
        super.uninstallComponents(fc);
    }
    
    public void installComponents(JFileChooser fc) {
        fileChooser = fc;
        
        fc.setFocusCycleRoot(true);
        fc.setBorder(new EmptyBorder(4, 10, 10, 10));
        fc.setLayout(new BorderLayout(8, 8));
        
        updateUseShellFolder();
        createCenterPanel(fc);
        fc.add(centerPanel, BorderLayout.CENTER);
        
        if (fc.isMultiSelectionEnabled()) {
            setFileName(getStringOfFileNames(fc.getSelectedFiles()));
        } else {
            setFileName(getStringOfFileName(fc.getSelectedFile()));
        }
        
        if(fc.getControlButtonsAreShown()) {
            addControlButtons();
        }
        
        createPopup();
    }

    @Override
    public String getDialogTitle(JFileChooser fc) {
        String title = super.getDialogTitle(fc);
        fc.getAccessibleContext().setAccessibleDescription(title);
        return title;
    }
    
    private void updateUseShellFolder() {
        // Decide whether to use the ShellFolder class to populate shortcut
        // panel and combobox.
        
        Boolean prop =
                (Boolean)fileChooser.getClientProperty(DelegatingChooserUI.USE_SHELL_FOLDER);
        if (prop != null) {
            useShellFolder = prop.booleanValue();
        } else {
            // See if FileSystemView.getRoots() returns the desktop folder,
            // i.e. the normal Windows hierarchy.
            useShellFolder = false;
            File[] roots = fileChooser.getFileSystemView().getRoots();
            if (roots != null && roots.length == 1) {
                File[] cbFolders = getShellFolderRoots();
                if (cbFolders != null && cbFolders.length > 0 && roots[0] == cbFolders[0]) {
                    useShellFolder = true;
                }
            }
        }
        
        if (Utilities.isWindows()) {
            if (useShellFolder) {
                if (placesBar == null) {
                    placesBar = getPlacesBar();
                }
                if (placesBar != null) {
                    fileChooser.add(placesBar, BorderLayout.BEFORE_LINE_BEGINS);
                    if (placesBar instanceof PropertyChangeListener) {
                        fileChooser.addPropertyChangeListener((PropertyChangeListener)placesBar);
                    }
                }
            } else {
                if (placesBar != null) {
                    fileChooser.remove(placesBar);
                    if (placesBar instanceof PropertyChangeListener) {
                        fileChooser.removePropertyChangeListener((PropertyChangeListener)placesBar);
                    }
                    placesBar = null;
                }
            }
        }
    }

    /** Returns instance of WindowsPlacesBar class or null in case of failure
     */
    private JComponent getPlacesBar () {
        if (placesBarFailed) {
            return null;
        }
        try {
            Class<?> clazz = Class.forName("sun.swing.WindowsPlacesBar");
            Class[] params = new Class[] { JFileChooser.class, Boolean.TYPE };
            Constructor<?> constr = clazz.getConstructor(params);
            return (JComponent)constr.newInstance(fileChooser, isXPStyle().booleanValue());
        } catch (Exception exc) {
            // reflection not succesfull, just log the exception and return null
            Logger.getLogger(DirectoryChooserUI.class.getName()).log(
                    Level.FINE, "WindowsPlacesBar class can't be instantiated.", exc);
            placesBarFailed = true;
            return null;
        }
    }
    
    /** Reflection alternative of
     * sun.awt.shell.ShellFolder.getShellFolder(file)
     */
    private File getShellFolderForFile (File file) {
        try {
            Class<?> clazz = Class.forName("sun.awt.shell.ShellFolder");
            return (File) clazz.getMethod("getShellFolder", File.class).invoke(null, file);
        } catch (Exception exc) {
            // reflection not succesfull, just log the exception and return null
            Logger.getLogger(DirectoryChooserUI.class.getName()).log(
                    Level.FINE, "ShellFolder can't be used.", exc);
            return null;
        }
    }

    /** Reflection alternative of
     * sun.awt.shell.ShellFolder.getShellFolder(dir).getLinkLocation()
     */
    private File getShellFolderForFileLinkLoc (File file) {
        try {
            Class<?> clazz = Class.forName("sun.awt.shell.ShellFolder");
            Object sf = clazz.getMethod("getShellFolder", File.class).invoke(null, file);
            return (File) clazz.getMethod("getLinkLocation").invoke(sf);
        } catch (Exception exc) {
            // reflection not succesfull, just log the exception and return null
            Logger.getLogger(DirectoryChooserUI.class.getName()).log(
                    Level.FINE, "ShellFolder can't be used.", exc);
            return null;
        }
        
    }
    
    /** Reflection alternative of
     * sun.awt.shell.ShellFolder.get("fileChooserComboBoxFolders")
     */ 
    private File[] getShellFolderRoots () {
        try {
            Class<?> clazz = Class.forName("sun.awt.shell.ShellFolder");
            return (File[]) clazz.getMethod("get", String.class).invoke(null, "fileChooserComboBoxFolders");
        } catch (Exception exc) {
            // reflection not succesfull, just log the exception and return null
            Logger.getLogger(DirectoryChooserUI.class.getName()).log(
                    Level.FINE, "ShellFolder can't be used.", exc);
            return null;
        }
    }
    
    private void createBottomPanel(JFileChooser fc) {
        bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.LINE_AXIS));
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.PAGE_AXIS));
        labelPanel.add(Box.createRigidArea(verticalStrut1));
        
        JLabel fnl = new JLabel(fileNameLabelText);
        fnl.setDisplayedMnemonic(fileNameLabelMnemonic);
        fnl.setAlignmentY(0);
        labelPanel.add(fnl);
        
        labelPanel.add(Box.createRigidArea(new Dimension(1,12)));
        
        JLabel ftl = new JLabel(filesOfTypeLabelText);
        ftl.setDisplayedMnemonic(filesOfTypeLabelMnemonic);
        labelPanel.add(ftl);
        
        bottomPanel.add(labelPanel);
        bottomPanel.add(Box.createRigidArea(new Dimension(15, 0)));
        
        JPanel fileAndFilterPanel = new JPanel();
        fileAndFilterPanel.add(Box.createRigidArea(verticalStrut3));
        fileAndFilterPanel.setLayout(new BoxLayout(fileAndFilterPanel, BoxLayout.Y_AXIS));
        
        filenameTextField = new JTextField(24) {
            public Dimension getMaximumSize() {
                return new Dimension(Short.MAX_VALUE, super.getPreferredSize().height);
            }
        };
        
        filenameTextField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateCompletions();
            }
            public void removeUpdate(DocumentEvent e) {
                updateCompletions();
            }
            public void changedUpdate(DocumentEvent e) {}
        });
        
        filenameTextField.addKeyListener(new TextFieldKeyListener());
        
        fnl.setLabelFor(filenameTextField);
        filenameTextField.addFocusListener(
                new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                if (!getFileChooser().isMultiSelectionEnabled()) {
                    tree.clearSelection();
                }
            }
        });
        
        // disable TAB focus transfer, we need it for completion
        Set<AWTKeyStroke> tKeys = filenameTextField.getFocusTraversalKeys(java.awt.KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        Set<AWTKeyStroke> newTKeys = new HashSet<AWTKeyStroke>(tKeys);
        newTKeys.remove(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, 0));
        // #107305: enable at least Ctrl+TAB if we have TAB for completion
        newTKeys.add(AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_TAB, KeyEvent.CTRL_DOWN_MASK));
        filenameTextField.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, newTKeys);
        
        fileAndFilterPanel.add(filenameTextField);
        fileAndFilterPanel.add(Box.createRigidArea(verticalStrut3));
        
        filterTypeComboBoxModel = createFilterComboBoxModel();
        fc.addPropertyChangeListener(filterTypeComboBoxModel);
        filterTypeComboBox = new JComboBox(filterTypeComboBoxModel);
        ftl.setLabelFor(filterTypeComboBox);
        filterTypeComboBox.setRenderer(createFilterComboBoxRenderer());
        fileAndFilterPanel.add(filterTypeComboBox);
        
        bottomPanel.add(fileAndFilterPanel);
        bottomPanel.add(Box.createRigidArea(horizontalStrut1));
        createButtonsPanel(fc);
    }
    
    private void createButtonsPanel(JFileChooser fc) {
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        
        approveButton = new JButton(getApproveButtonText(fc)) {
            public Dimension getMaximumSize() {
                return approveButton.getPreferredSize().width > cancelButton.getPreferredSize().width ?
                    approveButton.getPreferredSize() : cancelButton.getPreferredSize();
            }
        };
        // #107791: No mnemonics desirable on Mac 
        if (!Utilities.isMac()) {
            approveButton.setMnemonic(getApproveButtonMnemonic(fc));
        }
        approveButton.addActionListener(getApproveSelectionAction());
        approveButton.setToolTipText(getApproveButtonToolTipText(fc));
        buttonPanel.add(Box.createRigidArea(verticalStrut1));
        buttonPanel.add(approveButton);
        buttonPanel.add(Box.createRigidArea(verticalStrut2));
        
        cancelButton = new JButton(cancelButtonText) {
            public Dimension getMaximumSize() {
                return approveButton.getPreferredSize().width > cancelButton.getPreferredSize().width ?
                    approveButton.getPreferredSize() : cancelButton.getPreferredSize();
            }
        };
        // #107791: No mnemonics desirable on Mac 
        if (!Utilities.isMac()) {
            cancelButton.setMnemonic(cancelButtonMnemonic);
        }
        cancelButton.setToolTipText(cancelButtonToolTipText);
        cancelButton.addActionListener(getCancelSelectionAction());
        buttonPanel.add(cancelButton);

        //TODO initial approve button disabled code started
        if (customDirectoryProvider != null)
            approveButton.setEnabled(false);
        //TODO initial approve button disabled code ended
    }
    
    private void createCenterPanel(final JFileChooser fc) {
        centerPanel = new JPanel(new BorderLayout());
        treeViewPanel = createTree();
        treeViewPanel.setPreferredSize(TREE_PREF_SIZE);
        JPanel treePanel = new JPanel();
        treePanel.setLayout(new BorderLayout());
        JComponent accessory = fc.getAccessory();
        topToolbar = createTopToolbar();
        topCombo = createTopCombo(fc);
        topComboWrapper = new JPanel(new BorderLayout());
        topComboWrapper.add(topCombo, BorderLayout.CENTER);
        if (accessory == null) {
            topComboWrapper.add(topToolbar, BorderLayout.EAST);
        }
        treePanel.add(topComboWrapper, BorderLayout.NORTH);
        treePanel.add(treeViewPanel, BorderLayout.CENTER);
        centerPanel.add(treePanel, BorderLayout.CENTER);
        // control width of accessory panel, don't allow to jump (change width)
        JPanel wrapAccessory = new JPanel() {
            private Dimension prefSize = new Dimension(ACCESSORY_WIDTH, 0);
            private Dimension minSize = new Dimension(ACCESSORY_WIDTH, 0);
            
            public Dimension getMinimumSize () {
                if (fc.getAccessory() != null) {
                    minSize.height = getAccessoryPanel().getMinimumSize().height;
                    return minSize;
                }
                return super.getMinimumSize();
            }
            public Dimension getPreferredSize () {
                if (fc.getAccessory() != null) {
                    Dimension origPref = getAccessoryPanel().getPreferredSize();
                    LOG.fine("AccessoryWrapper.getPreferredSize: orig pref size: " + origPref);
                    
                    prefSize.height = origPref.height;
                    
                    prefSize.width = Math.max(prefSize.width, origPref.width);
                    int centerW = centerPanel.getWidth();
                    if (centerW != 0 && prefSize.width > centerW / 2) {
                        prefSize.width = centerW / 2;
                    }
                    LOG.fine("AccessoryWrapper.getPreferredSize: resulting pref size: " + prefSize);
                    
                    return prefSize;
                }
                return super.getPreferredSize();
            }
        };
        wrapAccessory.setLayout(new BorderLayout());
        JPanel accessoryPanel = getAccessoryPanel();
        if (accessory != null) {
            accessoryPanel.add(topToolbar, BorderLayout.NORTH);
            accessoryPanel.add(accessory, BorderLayout.CENTER);
        }
        wrapAccessory.add(accessoryPanel, BorderLayout.CENTER);
        centerPanel.add(wrapAccessory, BorderLayout.EAST);
        createBottomPanel(fc);
        centerPanel.add(bottomPanel, BorderLayout.SOUTH);
    }
    
    
    
    private JComponent createTopCombo(JFileChooser fc) {
        JPanel panel = new JPanel();
        if (fc.getAccessory() != null) {
            panel.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
        } else {
            panel.setBorder(BorderFactory.createEmptyBorder(6, 0, 10, 0));
        }
        panel.setLayout(new BorderLayout());
        
        Box labelBox = Box.createHorizontalBox();
        
        lookInComboBoxLabel = new JLabel(lookInLabelText);
        lookInComboBoxLabel.setDisplayedMnemonic(lookInLabelMnemonic);
        lookInComboBoxLabel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        lookInComboBoxLabel.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        
        labelBox.add(lookInComboBoxLabel);
        labelBox.add(Box.createRigidArea(new Dimension(9,0)));
        
        // fixed #97525, made the height of the
        // combo box bigger.
        directoryComboBox = new JComboBox() {
            public Dimension getMinimumSize() {
                Dimension d = super.getMinimumSize();
                d.width = 60;
                return d;
            }
            
            public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                // Must be small enough to not affect total width and height.
                d.height = 24;
                d.width = 150;
                return d;
            }
        };
        directoryComboBox.putClientProperty( "JComboBox.lightweightKeyboardNavigation", "Lightweight" );
        lookInComboBoxLabel.setLabelFor(directoryComboBox);
        directoryComboBoxModel = createDirectoryComboBoxModel(fc);
        directoryComboBox.setModel(directoryComboBoxModel);
        directoryComboBox.addActionListener(directoryComboBoxAction);
        directoryComboBox.setRenderer(createDirectoryComboBoxRenderer(fc));
        directoryComboBox.setAlignmentX(JComponent.LEFT_ALIGNMENT);
        directoryComboBox.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        directoryComboBox.setMaximumRowCount(8);
        
        panel.add(labelBox, BorderLayout.WEST);
        panel.add(directoryComboBox, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JComponent createTopToolbar() {
        JToolBar topPanel = new JToolBar();
        topPanel.setFloatable(false);
        
        if (Utilities.isWindows()) {
            topPanel.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
        }
        
        upFolderButton = new JButton(getChangeToParentDirectoryAction());
        upFolderButton.setText(null);
        // fixed bug #97049
        final boolean isMac = Utilities.isMac();
        Icon upFolderIcon = null;
        if (!isMac) {
            upFolderIcon = UIManager.getIcon("FileChooser.upFolderIcon");
        }
        // on Mac all icons from UIManager are the same, some default, so load our own.
        // it's also fallback if icon from UIManager not found, may happen
        if (isMac || upFolderIcon == null) {
            upFolderIcon = ImageUtilities.image2Icon(ImageUtilities.loadImage("/org/netbeans/swing/dirchooser/resources/upFolderIcon.gif", false));
        }
        upFolderButton.setIcon(upFolderIcon);
        upFolderButton.setToolTipText(upFolderToolTipText);
        upFolderButton.getAccessibleContext().setAccessibleName(upFolderAccessibleName);
        upFolderButton.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
        upFolderButton.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        
        if(useShellFolder) {
            upFolderButton.setFocusPainted(false);
        }
        
        topPanel.add(upFolderButton);
        topPanel.add(Box.createRigidArea(new Dimension(2, 0)));
        
        // no home on Win platform
        if (!Utilities.isWindows()) {
            JButton homeButton = new JButton(getGoHomeAction());
            Icon homeIcon = null;
            if (!isMac) {
                homeIcon = UIManager.getIcon("FileChooser.homeFolderIcon");
            }
            if (isMac || homeIcon == null) {
                homeIcon = ImageUtilities.image2Icon(ImageUtilities.loadImage("/org/netbeans/swing/dirchooser/resources/homeIcon.gif", false));
            }
            homeButton.setIcon(homeIcon);
            homeButton.setText(null);
            
            String tooltip = homeButton.getToolTipText();
            if (tooltip == null) {
                tooltip = homeFolderTooltipText;
                if (tooltip == null) {
                    tooltip = NbBundle.getMessage(DirectoryChooserUI.class,
                            "TLTP_HomeFolder");
                }
                homeButton.setToolTipText( tooltip );
            }

            topPanel.add(homeButton);
        }
        
        newFolderButton = new JButton(newFolderAction);
        newFolderButton.setText(null);
        // fixed bug #97049
        Icon newFolderIcon = null;
        if (!isMac) {
            newFolderIcon = UIManager.getIcon("FileChooser.newFolderIcon");
        }
        // on Mac all icons from UIManager are the same, some default, so load our own.
        // it's also fallback if icon from UIManager not found, may happen
        if (isMac || newFolderIcon == null) {
            newFolderIcon = ImageUtilities.image2Icon(ImageUtilities.loadImage("/org/netbeans/swing/dirchooser/resources/newFolderIcon.gif", false));
        }
        newFolderButton.setIcon(newFolderIcon);
        newFolderButton.setToolTipText(newFolderToolTipText);
        newFolderButton.setAlignmentX(JComponent.RIGHT_ALIGNMENT);
        newFolderButton.setAlignmentY(JComponent.CENTER_ALIGNMENT);
        
        if(useShellFolder) {
            newFolderButton.setFocusPainted(false);
        }
        
        topPanel.add(newFolderButton);
        topPanel.add(Box.createRigidArea(new Dimension(2, 0)));
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(4, 9, 8, 0));
        panel.add(topPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JComponent createTree() {
        final DirectoryHandler dirHandler = createDirectoryHandler(fileChooser);
        // #106011: don't show "colors, food, sports" sample model after init :-)
        tree = new JTree(new Object[0]) {

            @Override
            protected void processMouseEvent(MouseEvent e) {
                dirHandler.preprocessMouseEvent(e);
                super.processMouseEvent(e);
            }

            // For speed (#127170):
            @Override
            public boolean isLargeModel() {
                return true;
            }

            // To work with different font sizes (#106223); see: http://www.javalobby.org/java/forums/t19562.html
            private boolean firstPaint = true;
            @Override
            public void setFont(Font f) {
                firstPaint = true;
                super.setFont(f);
            }
            @Override
            public void paint(Graphics g) {
                if (firstPaint) {
                    g.setFont(getFont());
                    setRowHeight(Math.max(/* icon height plus insets? */17, g.getFontMetrics().getHeight()));
                    firstPaint = false;
                    // Setting the fixed height will generate another paint request, no need to complete this one
                    return;
                }
                super.paint(g);
            }

        };
        // #105642: start with right content in tree 
        File curDir = fileChooser.getCurrentDirectory();
        if (curDir == null) {
            curDir = fileChooser.getFileSystemView().getRoots()[0];
        }
        updateTree(curDir);
        
        tree.setFocusable(true);
        tree.setOpaque(true);
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setToggleClickCount(0);
        tree.addTreeExpansionListener(new TreeExpansionHandler());
        TreeKeyHandler keyHandler = new TreeKeyHandler();
        tree.addKeyListener(keyHandler);
        tree.addFocusListener(keyHandler);
        tree.addMouseListener(dirHandler);
        tree.addFocusListener(dirHandler);
        tree.addTreeSelectionListener(dirHandler);
        
        if(fileChooser.isMultiSelectionEnabled()) {
            tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        } else {
            tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        }
        
        TreeCellEditor tce = new DirectoryCellEditor(tree, fileChooser, new JTextField());
        tree.setCellEditor(tce);
        tce.addCellEditorListener(dirHandler);
        tree.setCellRenderer(new DirectoryTreeRenderer());
        JScrollPane scrollBar = new JScrollPane(tree);
        scrollBar.setViewportView(tree);
        tree.setInvokesStopCellEditing(true);
        
        return scrollBar;
    }

    /** 
     * Handles keyboard quick search in tree and delete action.
     */
    class TreeKeyHandler extends KeyAdapter implements FocusListener {
        
        StringBuffer searchBuf = new StringBuffer();
        
        java.util.List<TreePath> paths;
                
        public void keyPressed(KeyEvent evt) {
            if(evt.getKeyCode() == KeyEvent.VK_DELETE) {
                deleteAction();
            }
            
            // F2 as rename shortcut
            if (evt.getKeyCode() == KeyEvent.VK_F2) {
                DirectoryNode node = (DirectoryNode)tree.getLastSelectedPathComponent();
                if (node != null) {
                    applyEdit(node);
                }
            }
            
            if (isCharForSearch(evt)) {
                evt.consume();
            } else {
                resetBuffer();
            }

            // #105527: keyboard invocation of tree's popup menu 
            if ((evt.getKeyCode() == KeyEvent.VK_F10) && evt.isShiftDown() && !popupMenu.isShowing()) {
                JTree tree = (JTree) evt.getSource();
                int selRow = tree.getLeadSelectionRow();
                if (selRow >= 0) {
                    Rectangle bounds = tree.getRowBounds(selRow);
                    popupMenu.show(tree, bounds.x + bounds.width / 2, bounds.y + bounds.height * 3 / 5);
                    evt.consume();
                }
            }
        }
        
        @Override
        public void keyTyped(KeyEvent evt) {
            char keyChar = evt.getKeyChar();
            if (isCharForSearch(evt)) {
                if (paths == null) {
                    paths = getVisiblePaths();
                }
                searchBuf.append(keyChar);
                String searchedText = searchBuf.toString().toLowerCase();
                String curFileName = null;
                for (TreePath path : paths) {
                    curFileName = fileChooser.getName(((DirectoryNode) path.getLastPathComponent()).getFile());
                    if (curFileName != null && curFileName.toLowerCase().startsWith(searchedText)) {
                        tree.makeVisible(path);
                        tree.scrollPathToVisible(path);
                        tree.setSelectionPath(path);
                        break;
                    }
                }
            } else {
                resetBuffer();
            }
        }
        
        public void focusGained(FocusEvent e) {
            resetBuffer();
        }

        public void focusLost(FocusEvent e) {
            resetBuffer();
        }
        
        private boolean isCharForSearch (KeyEvent evt) {
            char ch = evt.getKeyChar();
            // refuse backspace key
            if ((int)ch == 8) {
                return false;
            }
            // #110975: refuse modifiers
            if (evt.getModifiers() != 0) {
                return false;
            }
            return (Character.isJavaIdentifierPart(ch) && !Character.isIdentifierIgnorable(ch)) 
                    || Character.isSpaceChar(ch);
        }
        
        private void resetBuffer () {
            searchBuf.delete(0, searchBuf.length());
            paths = null;
        }
        
    }
    
    private java.util.List<TreePath> getVisiblePaths () {
        int rowCount = tree.getRowCount();
        DirectoryNode node = null;
        java.util.List<TreePath> result = new ArrayList<TreePath>(rowCount);
        for (int i = 0; i < rowCount; i++) {
            result.add(tree.getPathForRow(i));
        }
        return result;
    }
    
    private void createPopup() {
        popupMenu = new JPopupMenu();
        JMenuItem item1 = new JMenuItem(getBundle().getString("LBL_NewFolder"));
        item1.addActionListener(newFolderAction);
        
        JMenuItem item2 = new JMenuItem(getBundle().getString("LBL_Rename"));
        item2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                DirectoryNode node = (DirectoryNode)tree.getLastSelectedPathComponent();
                applyEdit(node);
            }
        });
        
        JMenuItem item3 = new JMenuItem(getBundle().getString("LBL_Delete"));
        item3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                deleteAction();
            }
        });
        
        popupMenu.add(item1);
        popupMenu.add(item2);
        popupMenu.add(item3);
            }

    // remove multiple directories
    private void deleteAction() {
        // fixed #97079 to be able to delete one or more folders
        final TreePath[] nodePath = tree.getSelectionPaths();
        
        if(nodePath == null) {
            return;
        }
        String message = "";
        
        if(nodePath.length == 1) {
            
            File file = ((DirectoryNode)nodePath[0].getLastPathComponent()).getFile();
            // Don't do anything if it's a special file
            if(!canWrite(file)) {
                return;
            }
            message = MessageFormat.format(getBundle().getString("MSG_Delete"), file.getName());
        } else {
            message = MessageFormat.format(getBundle().getString("MSG_Delete_Multiple"), nodePath.length);
        }
        
        int answer = JOptionPane.showConfirmDialog(fileChooser, message , getBundle().getString("MSG_Confirm"), JOptionPane.YES_NO_OPTION);
        
        if (answer == JOptionPane.YES_OPTION) {
            
            RequestProcessor.getDefault().post(new Runnable() {
                DirectoryNode node;
                ArrayList<File> list = new ArrayList<File>();
                int cannotDelete;
                ArrayList<DirectoryNode> nodes2Remove = new ArrayList<DirectoryNode>(nodePath.length);

                public void run() {
                    if (!EventQueue.isDispatchThread()) {
                        // first pass, out of EQ thread, deletes files
                        setCursor(fileChooser, Cursor.WAIT_CURSOR);
                        cannotDelete = 0;
                        for(int i = 0; i < nodePath.length; i++) {
                            DirectoryNode nodeToDelete = (DirectoryNode)nodePath[i].getLastPathComponent();
                            try {
                                FileObject fo = FileUtil.toFileObject(nodeToDelete.getFile());
                                fo.delete();
                                nodes2Remove.add(nodeToDelete);
                            } catch (IOException ignore) {
                                cannotDelete++;

                                if(canWrite(nodeToDelete.getFile())) {
                                    list.add(nodeToDelete.getFile());
                                }
                            }
                        }
                        // send to second pass
                        EventQueue.invokeLater(this);
                    } else {
                        // second pass, in EQ thread
                        for (DirectoryNode curNode : nodes2Remove) {
                            model.removeNodeFromParent(curNode);
                        }

                        setCursor(fileChooser, Cursor.DEFAULT_CURSOR);
                        if(cannotDelete > 0) {
                            String message = "";
                            
                            if(cannotDelete == 1) {
                                message = cannotDelete + " " + getBundle().getString("MSG_Sing_Delete");
                            } else {
                                message = cannotDelete + " " + getBundle().getString("MSG_Plur_Delete");
                            }
                            
                            setSelected((File[])list.toArray(new File[list.size()]));
                            
                            JOptionPane.showConfirmDialog(fileChooser, message , getBundle().getString("MSG_Confirm"), JOptionPane.OK_OPTION);
                        } else {
                            setSelected(new File[] {null});
                            setFileName("");
                        }
                    }
                }
            });
        }
    }
    
    private void updateCompletions() {
        String name = normalizeFile(getFileName());
        int slash = name.lastIndexOf(File.separatorChar);
        if (slash != -1) {
            String prefix = name.substring(0, slash + 1);
            File d = new File(prefix);
            if (d.isDirectory()) {
                File[] children = d.listFiles();
                if(children != null) {
                    Vector list = buildList(name, children);
                    
                    if(completionPopup == null) {
                        completionPopup = new FileCompletionPopup(fileChooser, filenameTextField, list);
                    } else if (completionPopup.isShowing() || 
                            (showPopupCompletion && fileChooser.isShowing())) {
                        completionPopup.setDataList(list);
                    }
                    
                    if(showPopupCompletion && fileChooser.isShowing() && !completionPopup.isShowing()) {
                        java.awt.Point los = filenameTextField.getLocation();
                        int popX = los.x;
                        int popY = los.y + filenameTextField.getHeight() - 6;
                        completionPopup.showPopup(filenameTextField, popX, popY);
                    }
                }
            }
        }
    }
    
    public Vector<File> buildList(String text, File[] children) {
        Vector<File> files = new Vector<File>(children.length);
        
        for(int i = children.length - 1; i >= 0; i--) {
            File completion = children[i];
            
            if(fileChooser.accept(completion)) {
                String path = completion.getAbsolutePath();
                
                if (path.regionMatches(true, 0, text, 0, text.length())) {
                    
                    if(fileChooser.getFileSelectionMode() == JFileChooser.DIRECTORIES_ONLY) {
                        if(completion.isDirectory()) {
                            files.add(completion);
                        }
                    } else if(fileChooser.getFileSelectionMode() == JFileChooser.FILES_ONLY) {
                        if(completion.isFile()) {
                            files.add(completion);
                        }
                    } else if(fileChooser.getFileSelectionMode() == JFileChooser.FILES_AND_DIRECTORIES) {
                        files.add(completion);
                    }
                }
            }
        }
        
        Collections.sort(files, DirectoryNode.FILE_NAME_COMPARATOR);
        return files;
    }
    
    
    private static String normalizeFile(String text) {
        // See #21690 for background.
        // XXX what are legal chars for var names? bash manual says only:
        // "The braces are required when PARAMETER [...] is followed by a
        // character that is not to be interpreted as part of its name."
        Pattern p = Pattern.compile("(^|[^\\\\])\\$([a-zA-Z_0-9.]+)");
        Matcher m;
        while ((m = p.matcher(text)).find()) {
            // Have an env var to subst...
            // XXX handle ${PATH} too? or don't bother
            String var = System.getenv(m.group(2));
            if (var == null) {
                // Try Java system props too, and fall back to "".
                var = System.getProperty(m.group(2), "");
            }
            // XXX full readline compat would mean vars were also completed with TAB...
            text = text.substring(0, m.end(1)) + var + text.substring(m.end(2));
        }
        if (text.equals("~")) {
            return System.getProperty("user.home");
        } else if (text.startsWith("~" + File.separatorChar)) {
            return System.getProperty("user.home") + text.substring(1);
        } else {
            int i = text.lastIndexOf("//");
            if (i != -1) {
                // Treat /home/me//usr/local as /usr/local
                // (so that you can use "//" to start a new path, without selecting & deleting)
                return text.substring(i + 1);
            }
            i = text.lastIndexOf(File.separatorChar + "~" + File.separatorChar);
            if (i != -1) {
                // Treat /usr/local/~/stuff as /home/me/stuff
                return System.getProperty("user.home") + text.substring(i + 2);
            }
            return text;
        }
    }
    
    private static ResourceBundle getBundle() {
        return NbBundle.getBundle(DirectoryChooserUI.class);
    }
    
    private void updateTree(final File file) {
        // fixed bug #97522
        if(updateWorker != null) {
            // try to cancel previous update if possible
            if (!updateWorker.isFinished()) {
                updateWorker.cancel();
            }
            // #105642 - wait for previous update, to keep time order
            updateWorker.waitFinished();
        }
        
        updateWorker = RequestProcessor.getDefault().post(new Runnable() {
            DirectoryNode node;
            long startTime;
            public void run() {
                if (!EventQueue.isDispatchThread()) {
                    // first pass, out of EQ thread
                    markStartTime();
                    setCursor(fileChooser, Cursor.WAIT_CURSOR);
                    node = new DirectoryNode(file);
                    node.loadChildren(fileChooser, true);
                    // send to second pass
                    EventQueue.invokeLater(this);
                } else {
                    // second pass, in EQ thread
                    model = new DirectoryTreeModel(node);
                    tree.setModel(model);
                    tree.repaint();
                    setCursor(fileChooser, Cursor.DEFAULT_CURSOR);
                    checkUpdate();
                }
            }

        });
        
    }

    private void markStartTime () {
        if (fileChooser.getClientProperty(DelegatingChooserUI.START_TIME) == null) {
            fileChooser.putClientProperty(DelegatingChooserUI.START_TIME,
                    Long.valueOf(System.currentTimeMillis()));
        }
    }

    private void checkUpdate() {
        if (Utilities.isWindows() && useShellFolder) {
            Long startTime = (Long) fileChooser.getClientProperty(DelegatingChooserUI.START_TIME);
            if (startTime == null) {
                return;
            }
            // clean for future marking
            fileChooser.putClientProperty(DelegatingChooserUI.START_TIME, null);

            long elapsed = System.currentTimeMillis() - startTime.longValue();
            long timeOut = NbPreferences.forModule(DirectoryChooserUI.class).
                    getLong(TIMEOUT_KEY, 10000);
            if (timeOut > 0 && elapsed > timeOut && slownessPanel == null) {
                JLabel slownessNote = new JLabel(
                        NbBundle.getMessage(DirectoryChooserUI.class, "MSG_SlownessNote"));
                slownessNote.setForeground(Color.RED);
                slownessPanel = new JPanel();
                JButton notShow = new JButton(
                        NbBundle.getMessage(DirectoryChooserUI.class, "BTN_NotShow"));
                notShow.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        NbPreferences.forModule(DirectoryChooserUI.class).putLong(TIMEOUT_KEY, 0);
                        centerPanel.remove(slownessPanel);
                        centerPanel.revalidate();
                    }
                });
                JPanel notShowP = new JPanel();
                notShowP.add(notShow);
                slownessPanel.setLayout(new BorderLayout());
                slownessPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
                slownessPanel.add(BorderLayout.CENTER, slownessNote);
                slownessPanel.add(BorderLayout.SOUTH, notShowP);
                centerPanel.add(BorderLayout.NORTH, slownessPanel);
                centerPanel.revalidate();
            }
        }
    }

    private Boolean isXPStyle() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Boolean themeActive = (Boolean)toolkit.getDesktopProperty("win.xpstyle.themeActive");
        if(themeActive == null)
            themeActive = Boolean.FALSE;
        if (themeActive.booleanValue() && System.getProperty("swing.noxp") == null) {
            themeActive = Boolean.TRUE;
        }
        return themeActive;
    }
    
    protected void installStrings(JFileChooser fc) {
        super.installStrings(fc);
        
        Locale l = fc.getLocale();
        
        lookInLabelMnemonic = UIManager.getInt("FileChooser.lookInLabelMnemonic");
        lookInLabelText = UIManager.getString("FileChooser.lookInLabelText",l);
        saveInLabelText = UIManager.getString("FileChooser.saveInLabelText",l);
        
        fileNameLabelMnemonic = UIManager.getInt("FileChooser.fileNameLabelMnemonic");
        fileNameLabelText = UIManager.getString("FileChooser.fileNameLabelText",l);
        
        filesOfTypeLabelMnemonic = UIManager.getInt("FileChooser.filesOfTypeLabelMnemonic");
        filesOfTypeLabelText = UIManager.getString("FileChooser.filesOfTypeLabelText",l);
        
        upFolderToolTipText =  UIManager.getString("FileChooser.upFolderToolTipText",l);
        upFolderAccessibleName = UIManager.getString("FileChooser.upFolderAccessibleName",l);
        
        newFolderToolTipText = UIManager.getString("FileChooser.newFolderToolTipText",l);
        homeFolderTooltipText = UIManager.getString("FileChooser.homeFolderToolTipText",l);
        
    }
    
    protected void installListeners(JFileChooser fc) {
        super.installListeners(fc);
        ActionMap actionMap = getActionMap();
        SwingUtilities.replaceUIActionMap(fc, actionMap);
    }
    
    protected ActionMap getActionMap() {
        return createActionMap();
    }
    
    protected ActionMap createActionMap() {
        AbstractAction escAction = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                getFileChooser().cancelSelection();
            }
            public boolean isEnabled(){
                return getFileChooser().isEnabled();
            }
        };
        ActionMap map = new ActionMapUIResource();
        map.put("approveSelection", getApproveSelectionAction());
        map.put("cancelSelection", escAction);
        map.put("Go Up", getChangeToParentDirectoryAction());
        return map;
    }
    
    public Action getNewFolderAction() {
        return newFolderAction;
    }
    
    public void uninstallUI(JComponent c) {
        c.removePropertyChangeListener(filterTypeComboBoxModel);
        cancelButton.removeActionListener(getCancelSelectionAction());
        approveButton.removeActionListener(getApproveSelectionAction());
        filenameTextField.removeActionListener(getApproveSelectionAction());
        super.uninstallUI(c);
    }
    
    /**
     * Returns the preferred size of the specified
     * <code>JFileChooser</code>.
     * The preferred size is at least as large,
     * in both height and width,
     * as the preferred size recommended
     * by the file chooser's layout manager.
     *
     * @param c  a <code>JFileChooser</code>
     * @return   a <code>Dimension</code> specifying the preferred
     *           width and height of the file chooser
     */
    public Dimension getPreferredSize(JComponent c) {
        int prefWidth = PREF_SIZE.width;
        Dimension d = c.getLayout().preferredLayoutSize(c);
        if (d != null) {
            return new Dimension(d.width < prefWidth ? prefWidth : d.width,
                    d.height < PREF_SIZE.height ? PREF_SIZE.height : d.height);
        } else {
            return new Dimension(prefWidth, PREF_SIZE.height);
        }
    }
    
    /**
     * Returns the minimum size of the <code>JFileChooser</code>.
     *
     * @param c  a <code>JFileChooser</code>
     * @return   a <code>Dimension</code> specifying the minimum
     *           width and height of the file chooser
     */
    public Dimension getMinimumSize(JComponent c) {
        return MIN_SIZE;
    }
    
    /**
     * Returns the maximum size of the <code>JFileChooser</code>.
     *
     * @param c  a <code>JFileChooser</code>
     * @return   a <code>Dimension</code> specifying the maximum
     *           width and height of the file chooser
     */
    public Dimension getMaximumSize(JComponent c) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }
    
    private String getStringOfFileName(File file) {
        if (file == null) {
            return null;
        } else {
            JFileChooser fc = getFileChooser();
            if (fc.isDirectorySelectionEnabled() && !fc.isFileSelectionEnabled()) {
                if(fc.getFileSystemView().isDrive(file)) {
                    return file.getPath();
                } else {
                    return file.getPath();
                }
            } else {
                return file.getName();
            }
        }
    }
    
    private String getStringOfFileNames(File[] files) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; files != null && i < files.length; i++) {
            if (i > 0) {
                buf.append(" ");
            }
            if (files.length > 1) {
                buf.append("\"");
            }
            buf.append(getStringOfFileName(files[i]));
            if (files.length > 1) {
                buf.append("\"");
            }
        }
        return buf.toString();
    }
    
    /* The following methods are used by the PropertyChange Listener */
    
    private void fireSelectedFileChanged(PropertyChangeEvent e) {
        File f = (File) e.getNewValue();
        JFileChooser fc = getFileChooser();
        if (f != null
                && ((fc.isFileSelectionEnabled() && !f.isDirectory())
                || (f.isDirectory() && fc.isDirectorySelectionEnabled()))) {
            
            setFileName(getStringOfFileName(f));
        }

        //TODO button visibility code started
        if (customDirectoryProvider != null)
            approveButton.setEnabled(customDirectoryProvider.isValidCustomDirectory(f));
        //TODO button visibility code ended
    }
    
    private void fireSelectedFilesChanged(PropertyChangeEvent e) {
        File[] files = (File[]) e.getNewValue();
        JFileChooser fc = getFileChooser();
        if (files != null
                && files.length > 0
                && (files.length > 1 || fc.isDirectorySelectionEnabled() || !files[0].isDirectory())) {
            setFileName(getStringOfFileNames(files));
        }
    }
    
    private void fireDirectoryChanged(PropertyChangeEvent e) {
        JFileChooser fc = getFileChooser();
        FileSystemView fsv = fc.getFileSystemView();
        showPopupCompletion = false;
        setFileName("");
        clearIconCache();
        File currentDirectory = fc.getCurrentDirectory();
        if(currentDirectory != null) {
            directoryComboBoxModel.addItem(currentDirectory);
            newFolderAction.setEnabled(currentDirectory.canWrite());
            getChangeToParentDirectoryAction().setEnabled(!fsv.isRoot(currentDirectory));
            updateTree(currentDirectory);
            if (fc.isDirectorySelectionEnabled() && !fc.isFileSelectionEnabled()) {
                if (fsv.isFileSystem(currentDirectory)) {
                    setFileName(getStringOfFileName(currentDirectory));
                } else {
                    setFileName(null);
                }
            }
        }
    }
    
    private void fireFilterChanged(PropertyChangeEvent e) {
        clearIconCache();
    }
    
    private void fireFileSelectionModeChanged(PropertyChangeEvent e) {
        clearIconCache();
        JFileChooser fc = getFileChooser();
        
        File currentDirectory = fc.getCurrentDirectory();
        if (currentDirectory != null
                && fc.isDirectorySelectionEnabled()
                && !fc.isFileSelectionEnabled()
                && fc.getFileSystemView().isFileSystem(currentDirectory)) {
            
            setFileName(currentDirectory.getPath());
        } else {
            setFileName(null);
        }
    }
    
    private void fireMultiSelectionChanged(PropertyChangeEvent e) {
        if (getFileChooser().isMultiSelectionEnabled()) {
            tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        } else {
            tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            getFileChooser().setSelectedFiles(null);
        }
    }
    
    private void fireAccessoryChanged(PropertyChangeEvent e) {
        if(getAccessoryPanel() != null) {
            JComponent oldAcc = (JComponent) e.getOldValue();
            JComponent newAcc = (JComponent) e.getNewValue();
            JComponent accessoryPanel = getAccessoryPanel();
            if(oldAcc != null) {
                accessoryPanel.remove(oldAcc);
            }
            if (oldAcc != null && newAcc == null) {
                accessoryPanel.remove(topToolbar);
                topComboWrapper.add(topToolbar, BorderLayout.EAST);
                topCombo.setBorder(BorderFactory.createEmptyBorder(6, 0, 10, 0));
                topCombo.revalidate();
            }
            
            if(newAcc != null) {
                getAccessoryPanel().add(newAcc, BorderLayout.CENTER);
            }
            
            if (oldAcc == null && newAcc != null) {
                topComboWrapper.remove(topToolbar);
                topCombo.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
                accessoryPanel.add(topToolbar, BorderLayout.NORTH);
            }
            
        }
    }
    
    private void fireApproveButtonTextChanged(PropertyChangeEvent e) {
        JFileChooser chooser = getFileChooser();
        approveButton.setText(getApproveButtonText(chooser));
        approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
        // #107791: No mnemonics desirable on Mac 
        if (!Utilities.isMac()) {
            approveButton.setMnemonic(getApproveButtonMnemonic(chooser));
        }
    }
    
    private void fireDialogTypeChanged(PropertyChangeEvent e) {
        JFileChooser chooser = getFileChooser();
        approveButton.setText(getApproveButtonText(chooser));
        approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
        // #107791: No mnemonics desirable on Mac 
        if (!Utilities.isMac()) {
            approveButton.setMnemonic(getApproveButtonMnemonic(chooser));
        }
        if (chooser.getDialogType() == JFileChooser.SAVE_DIALOG) {
            lookInComboBoxLabel.setText(saveInLabelText);
        } else {
            lookInComboBoxLabel.setText(lookInLabelText);
        }
    }
    
    private void fireApproveButtonMnemonicChanged(PropertyChangeEvent e) {
        // #107791: No mnemonics desirable on Mac 
        if (!Utilities.isMac()) {
            approveButton.setMnemonic(getApproveButtonMnemonic(getFileChooser()));
        }
    }
    
    private void fireControlButtonsChanged(PropertyChangeEvent e) {
        if(getFileChooser().getControlButtonsAreShown()) {
            addControlButtons();
        } else {
            removeControlButtons();
        }
    }
    
    /*
     * Listen for filechooser property changes, such as
     * the selected file changing, or the type of the dialog changing.
     */
    public PropertyChangeListener createPropertyChangeListener(JFileChooser fc) {
        return new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                String s = e.getPropertyName();
                if(s.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
                    fireSelectedFileChanged(e);
                } else if (s.equals(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY)) {
                    fireSelectedFilesChanged(e);
                } else if(s.equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY) && changeDirectory) {
                    fireDirectoryChanged(e);
                } else if(s.equals(JFileChooser.FILE_FILTER_CHANGED_PROPERTY)) {
                    fireFilterChanged(e);
                } else if(s.equals(JFileChooser.FILE_SELECTION_MODE_CHANGED_PROPERTY)) {
                    fireFileSelectionModeChanged(e);
                } else if(s.equals(JFileChooser.MULTI_SELECTION_ENABLED_CHANGED_PROPERTY)) {
                    fireMultiSelectionChanged(e);
                } else if(s.equals(JFileChooser.ACCESSORY_CHANGED_PROPERTY)) {
                    fireAccessoryChanged(e);
                } else if (s.equals(JFileChooser.APPROVE_BUTTON_TEXT_CHANGED_PROPERTY) ||
                        s.equals(JFileChooser.APPROVE_BUTTON_TOOL_TIP_TEXT_CHANGED_PROPERTY)) {
                    fireApproveButtonTextChanged(e);
                } else if(s.equals(JFileChooser.DIALOG_TYPE_CHANGED_PROPERTY)) {
                    fireDialogTypeChanged(e);
                } else if(s.equals(JFileChooser.APPROVE_BUTTON_MNEMONIC_CHANGED_PROPERTY)) {
                    fireApproveButtonMnemonicChanged(e);
                } else if(s.equals(JFileChooser.CONTROL_BUTTONS_ARE_SHOWN_CHANGED_PROPERTY)) {
                    fireControlButtonsChanged(e);
                } else if (s.equals(DelegatingChooserUI.USE_SHELL_FOLDER)) {
                    updateUseShellFolder();
                    fireDirectoryChanged(e);
                } else if (s.equals("componentOrientation")) {
                    ComponentOrientation o = (ComponentOrientation)e.getNewValue();
                    JFileChooser cc = (JFileChooser)e.getSource();
                    if (o != (ComponentOrientation)e.getOldValue()) {
                        cc.applyComponentOrientation(o);
                    }
                } else if (s.equals("ancestor")) {
                    if (e.getOldValue() == null && e.getNewValue() != null) {
                        filenameTextField.selectAll();
                        filenameTextField.requestFocus();
                    }
                }
            }
        };
    }
    
    protected void removeControlButtons() {
        bottomPanel.remove(buttonPanel);
    }
    
    protected void addControlButtons() {
        bottomPanel.add(buttonPanel);
    }
    
    public String getFileName() {
        if(filenameTextField != null) {
            return filenameTextField.getText();
        } else {
            return null;
        }
    }
    
    public void setFileName(String filename) {
        if(filenameTextField != null) {
            filenameTextField.setText(filename);
        }
    }
    
    private DirectoryComboBoxRenderer createDirectoryComboBoxRenderer(JFileChooser fc) {
        return new DirectoryComboBoxRenderer();
    }
    
    protected DirectoryComboBoxModel createDirectoryComboBoxModel(JFileChooser fc) {
        return new DirectoryComboBoxModel();
    }
    
    protected FilterComboBoxRenderer createFilterComboBoxRenderer() {
        return new FilterComboBoxRenderer();
    }
    
    protected FilterTypeComboBoxModel createFilterComboBoxModel() {
        return new FilterTypeComboBoxModel();
    }
    
    protected JButton getApproveButton(JFileChooser fc) {
        return approveButton;
    }
    
    public FileView getFileView(JFileChooser fc) {
        
        // fix bug #96957, should use DirectoryChooserFileView
        // only on windows
        if (Utilities.isWindows()) {
            if (useShellFolder) {
                fileView = new DirectoryChooserFileView();
            }
        } else {
            fileView = (BasicFileView) super.getFileView(fileChooser);
        }
        return fileView;
    }
    
    private void setSelected(File[] files) {
        changeDirectory = false;
        fileChooser.setSelectedFiles(files);
        changeDirectory = true;
    }
    
    private DirectoryHandler createDirectoryHandler(JFileChooser chooser) {
        return new DirectoryHandler(chooser);
    }
    
    private void addNewDirectory(final TreePath path) {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        DirectoryNode selectedNode = (DirectoryNode)path.getLastPathComponent();
                        
                        if(selectedNode == null || !canWrite(selectedNode.getFile())) {
                            return;
                        }
                        
                        try {
                            newFolderNode = new DirectoryNode(fileChooser.getFileSystemView().createNewFolder(selectedNode.getFile()));
                            model.insertNodeInto(newFolderNode, selectedNode, selectedNode.getChildCount());
                            applyEdit(newFolderNode);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }
        });
    }
    
    private void applyEdit(DirectoryNode node) {
        TreeNode[] nodes = model.getPathToRoot(node);
        TreePath editingPath = new TreePath(nodes);
        tree.setEditable(true);
        tree.makeVisible(editingPath);
        tree.scrollPathToVisible(editingPath);
        tree.setSelectionPath(editingPath);
        tree.startEditingAtPath(editingPath);
        
        JTextField editField = DirectoryCellEditor.getTextField();
        editField.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        editField.setRequestFocusEnabled(true);
        editField.requestFocus();
        editField.setSelectionStart(0);
        editField.setSelectionEnd(editField.getText().length());
    }
    
    private static boolean canWrite(File f) {
        boolean writeable = false;
        if (f != null) {
            try {
                writeable = f.canWrite();
            } catch (AccessControlException ex) {
                writeable = false;
            }
        }
        return writeable;
    }
    
    private void expandNode(final JFileChooser fileChooser, final TreePath path) {
        
        RequestProcessor.getDefault().post(new Runnable() {
            DirectoryNode node;
            
            public void run() {
                if (!EventQueue.isDispatchThread()) {
                    // first pass, out of EQ thread, loads data
                    markStartTime();
                    setCursor(fileChooser, Cursor.WAIT_CURSOR);
                    node = (DirectoryNode) path.getLastPathComponent();
                    node.loadChildren(fileChooser, true);
                    // send to second pass
                    EventQueue.invokeLater(this);
                } else {
                    // second pass, in EQ thread
                    ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(node);
                  /*
                   * This happens when the add new directory action is called
                   * and the node is not loaded.  Furthermore, it ensures that
                   * adding a new directory execute after the UI has finished
                   * displaying the children of the expanded node.
                   */
                    if(addNewDirectory) {
                        addNewDirectory(path);
                        addNewDirectory = false;
                    }
                    setCursor(fileChooser, Cursor.DEFAULT_CURSOR);
                    checkUpdate();
                }
            }
        });
    }
    
    private void setCursor(JComponent comp, int type) {
        Window window = SwingUtilities.getWindowAncestor(comp);
        if (window != null) {
            Cursor cursor = Cursor.getPredefinedCursor(type);
            window.setCursor(cursor);
            window.setFocusable(true);
        }
        
        JRootPane pane = fileChooser.getRootPane();
        if( null == blocker )
            blocker = new InputBlocker();
        
        if(type == Cursor.WAIT_CURSOR) {
            blocker.block(pane);
        } else if (type == Cursor.DEFAULT_CURSOR){
            blocker.unBlock(pane);
        }
    }
    
    /*************** HELPER CLASSES ***************/
    
    private class IconIndenter implements Icon {
        final static int space = 10;
        Icon icon = null;
        int depth = 0;
        
        public void paintIcon(Component c, Graphics g, int x, int y) {
            if (icon == null) {
                return;
            }
            if (c.getComponentOrientation().isLeftToRight()) {
                icon.paintIcon(c, g, x+depth*space, y);
            } else {
                icon.paintIcon(c, g, x, y);
            }
        }
        
        public int getIconWidth() {
            return icon != null ? icon.getIconWidth() + depth*space : null;
        }
        
        public int getIconHeight() {
            return icon != null ? icon.getIconHeight() : null;
        }
        
    }
    
    private class DirectoryComboBoxRenderer  extends JLabel implements ListCellRenderer, UIResource {
        IconIndenter indenter = new IconIndenter();
        
        public DirectoryComboBoxRenderer() {
            setOpaque(true);
        }
        
        public Component getListCellRendererComponent(JList list, Object value,
                int index, boolean isSelected,
                boolean cellHasFocus) {
            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            if (value == null) {
                setText("");
                return this;
            }
            File directory = (File)value;
            setText(getFileChooser().getName(directory));
            Icon icon = getFileChooser().getIcon(directory);
            indenter.icon = icon;
            indenter.depth = directoryComboBoxModel.getDepth(index);
            setIcon(indenter);
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }
        
        // #89393: GTK needs name to render cell renderer "natively"
        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
    } // end of DirectoryComboBoxRenderer
    
    /**
     * Data model for a type-face selection combo-box.
     */
    private class DirectoryComboBoxModel extends AbstractListModel implements ComboBoxModel {
        Vector<File> directories = new Vector<File>();
        int[] depths = null;
        File selectedDirectory = null;
        JFileChooser chooser = getFileChooser();
        FileSystemView fsv = chooser.getFileSystemView();
        
        public DirectoryComboBoxModel() {
            // Add the current directory to the model, and make it the
            // selectedDirectory
            File dir = getFileChooser().getCurrentDirectory();
            if(dir != null) {
                addItem(dir);
            }
        }
        
        /**
         * Adds the directory to the model and sets it to be selected,
         * additionally clears out the previous selected directory and
         * the paths leading up to it, if any.
         */
        private void addItem(File directory) {
            
            if(directory == null) {
                return;
            }
            
            directories.clear();
            
            if(useShellFolder) {
                directories.addAll(Arrays.asList(getShellFolderRoots()));
            } else {
                directories.addAll(Arrays.asList(fileChooser.getFileSystemView().getRoots()));
            }
            
            
            // Get the canonical (full) path. This has the side
            // benefit of removing extraneous chars from the path,
            // for example /foo/bar/ becomes /foo/bar
            File canonical = null;
            try {
                canonical = directory.getCanonicalFile();
            } catch (IOException e) {
                // Maybe drive is not ready. Can't abort here.
                canonical = directory;
            }
            
            // create File instances of each directory leading up to the top
            File sf = getShellFolderForFile(canonical);
            File f = sf;
            Vector<File> path = new Vector<File>(10);

            
            /*
             * Fix for IZ#122534 :
             * NullPointerException at 
             * org.netbeans.swing.dirchooser.DirectoryChooserUI$DirectoryComboBoxModel.addItem
             * 
             */
            while( f!= null) {
                path.addElement(f);
                f = f.getParentFile();
            }

            int pathCount = path.size();
            // Insert chain at appropriate place in vector
            for (int i = 0; i < pathCount; i++) {
                f = path.get(i);
                if (directories.contains(f)) {
                    int topIndex = directories.indexOf(f);
                    for (int j = i-1; j >= 0; j--) {
                        directories.insertElementAt(path.get(j), topIndex+i-j);
                    }
                    break;
                }
            }
            calculateDepths();
            setSelectedItem(sf);
        }
        
        private void calculateDepths() {
            depths = new int[directories.size()];
            for (int i = 0; i < depths.length; i++) {
                File dir = directories.get(i);
                File parent = dir.getParentFile();
                depths[i] = 0;
                if (parent != null) {
                    for (int j = i-1; j >= 0; j--) {
                        if (parent.equals(directories.get(j))) {
                            depths[i] = depths[j] + 1;
                            break;
                        }
                    }
                }
            }
        }
        
        public int getDepth(int i) {
            return (depths != null && i >= 0 && i < depths.length) ? depths[i] : 0;
        }
        
        public void setSelectedItem(Object selectedDirectory) {
            this.selectedDirectory = (File)selectedDirectory;
            fireContentsChanged(this, -1, -1);
        }
        
        public Object getSelectedItem() {
            return selectedDirectory;
        }
        
        public int getSize() {
            return directories.size();
        }
        
        public Object getElementAt(int index) {
            return directories.elementAt(index);
        }
    }
    
    /**
     * Render different type sizes and styles.
     */
    private class FilterComboBoxRenderer extends JLabel implements ListCellRenderer, UIResource {
        
        public FilterComboBoxRenderer() {
            setOpaque(true);
        }
        
        public Component getListCellRendererComponent(JList list,
                Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            
            // #89393: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N
            
            if (value != null && value instanceof FileFilter) {
                setText(((FileFilter)value).getDescription());
            }
            
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }
        
        // #89393: GTK needs name to render cell renderer "natively"
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
        
    } // end of FilterComboBoxRenderer
    
    /**
     * Data model for a type-face selection combo-box.
     */
    protected class FilterTypeComboBoxModel extends AbstractListModel implements ComboBoxModel, PropertyChangeListener {
        protected FileFilter[] filters;
        protected FilterTypeComboBoxModel() {
            super();
            filters = getFileChooser().getChoosableFileFilters();
        }
        
        public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if(prop == JFileChooser.CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY) {
                filters = (FileFilter[]) e.getNewValue();
                fireContentsChanged(this, -1, -1);
            } else if (prop == JFileChooser.FILE_FILTER_CHANGED_PROPERTY) {
                fireContentsChanged(this, -1, -1);
            }
        }
        
        public void setSelectedItem(Object filter) {
            if(filter != null) {
                getFileChooser().setFileFilter((FileFilter) filter);
                setFileName(null);
                fireContentsChanged(this, -1, -1);
            }
        }
        
        public Object getSelectedItem() {
            // Ensure that the current filter is in the list.
            // NOTE: we shouldnt' have to do this, since JFileChooser adds
            // the filter to the choosable filters list when the filter
            // is set. Lets be paranoid just in case someone overrides
            // setFileFilter in JFileChooser.
            FileFilter currentFilter = getFileChooser().getFileFilter();
            boolean found = false;
            if(currentFilter != null) {
                for(int i=0; i < filters.length; i++) {
                    if(filters[i] == currentFilter) {
                        found = true;
                    }
                }
                if(found == false) {
                    getFileChooser().addChoosableFileFilter(currentFilter);
                }
            }
            return getFileChooser().getFileFilter();
        }
        
        public int getSize() {
            if(filters != null) {
                return filters.length;
            } else {
                return 0;
            }
        }
        
        public Object getElementAt(int index) {
            if(index > getSize() - 1) {
                // This shouldn't happen. Try to recover gracefully.
                return getFileChooser().getFileFilter();
            }
            if(filters != null) {
                return filters[index];
            } else {
                return null;
            }
        }
    }
    
    /**
     * Gets calls when the ComboBox has changed the selected item.
     */
    private class DirectoryComboBoxAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            File f = (File)directoryComboBox.getSelectedItem();
            getFileChooser().setCurrentDirectory(f);
        }
    }
    
    private class DirectoryChooserFileView extends BasicFileView {
        
        public Icon getIcon(File f) {
            Icon icon = getCachedIcon(f);
            if (icon != null) {
                return icon;
            }
            
            if (f != null) {
                try {
                    icon = fileChooser.getFileSystemView().getSystemIcon(f);
                } catch (NullPointerException exc) {
                    // workaround for JDK bug 6357445, in IZ: 145832, please remove when fixed
                    LOG.log(Level.FINE, "JDK bug 6357445 encountered, NPE caught", exc); // NOI18N
                }
            }
            
            if (icon == null) {
                icon = super.getIcon(f);
            }
            
            cacheIcon(f, icon);
            return icon;
        }
    }
    
    private class TextFieldKeyListener extends KeyAdapter {
        public void keyPressed(KeyEvent evt) {
            showPopupCompletion = true;
            int keyCode = evt.getKeyCode();
            // #105801: completionPopup might not be ready when updateCompletions not called (empty text field)
            if (completionPopup != null && !completionPopup.isVisible()) {
                if (keyCode == KeyEvent.VK_ENTER) {
                    File file = new File(filenameTextField.getText());
                    if(file.exists() && file.isDirectory()) {
                        setSelected(new File[] {file});
                        fileChooser.approveSelection();
                    }
                }
                
                if ((keyCode == KeyEvent.VK_TAB || keyCode == KeyEvent.VK_DOWN) ||
                    (keyCode == KeyEvent.VK_RIGHT && 
                    (filenameTextField.getCaretPosition() >= (filenameTextField.getDocument().getLength() - 1)))) {
                    updateCompletions();
                }
                
            }
            
            if(filenameTextField.isFocusOwner() && 
                    (completionPopup == null || !completionPopup.isVisible()) &&
                    keyCode == KeyEvent.VK_ESCAPE) {
                fileChooser.cancelSelection();
            }
        }
    }
    
    private class DirectoryHandler extends MouseAdapter 
            implements TreeSelectionListener, CellEditorListener, ActionListener,
                        FocusListener, Runnable {
        private JFileChooser fileChooser;
        /** current selection holder */
        private WeakReference<TreePath> curSelPath;
        /** timer for slow click to rename feature */ 
        private Timer renameTimer;
        /** path to rename for slow click to rename feature */
        private TreePath pathToRename;
        
        public DirectoryHandler(JFileChooser fileChooser) {
            this.fileChooser = fileChooser;
        }
        
        /************ imple of TreeSelectionListener *******/
        
        public void valueChanged(TreeSelectionEvent e) {
            showPopupCompletion = false;
            FileSystemView fsv = fileChooser.getFileSystemView();
            JTree tree = (JTree) e.getSource();
            TreePath path = tree.getSelectionPath();
            TreePath curSel = e.getNewLeadSelectionPath();
            curSelPath = (curSel != null) ? new WeakReference<TreePath>(curSel) : null;
            
            if(path != null) {
                
                DirectoryNode node = (DirectoryNode)path.getLastPathComponent();
                File file = node.getFile();
                
                if(file != null) {
                    setSelected(getSelectedNodes(tree.getSelectionPaths()));
                    newFolderAction.setEnabled(canWrite(file) && file.isDirectory());
                    
                    if(file.isDirectory()) {
                        setDirectorySelected(true);
                    }
                }
            }
        }
        
        private File[] getSelectedNodes(TreePath[] paths) {
            Vector<File> files = new Vector<File>();
            for(int i = 0; i < paths.length; i++) {
                File file = ((DirectoryNode)paths[i].getLastPathComponent()).getFile();
                if(file.isDirectory()
                        && fileChooser.isTraversable(file)
                        && !fileChooser.getFileSystemView().isFileSystem(file)) {
                    continue;
                }
                files.add(file);
            }
            return files.toArray(new File[files.size()]);
        }
        
        /********* impl of MouseListener ***********/
        
        public void mouseClicked(MouseEvent e) {
            final JTree tree = (JTree) e.getSource();
            Point p = e.getPoint();
            final int x = e.getX();
            final int y = e.getY();
            int row = tree.getRowForLocation(x, y);
            TreePath path = tree.getPathForRow(row);
            
            if (path != null) {
                
                DirectoryNode node = (DirectoryNode) path.getLastPathComponent();
                newFolderAction.setEnabled(canWrite(node.getFile()));
    
                if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 2)) {
                    cancelRename();
                    if(node.isNetBeansProject()) {
                        fileChooser.approveSelection();
                    } else if (node.getFile().isFile() && !node.getFile().getPath().endsWith(".lnk")){
                        fileChooser.approveSelection();
                    } else {
                        changeTreeDirectory(node.getFile());
                    }
                    
                }
                
                // handles click to rename feature
                if (SwingUtilities.isLeftMouseButton(e) && (e.getClickCount() == 1)) {
                    if (pathToRename != null) {
                        if (renameTimer != null) {
                            renameTimer.stop();
                        }
                        // start slow click rename timer
                        renameTimer = new Timer(800, this);
                        renameTimer.setRepeats(false);
                        renameTimer.start();
                    }
                }
                
                ((DirectoryTreeModel) tree.getModel()).nodeChanged(node);
                if (row == 0) {
                    tree.revalidate();
                    tree.repaint();
                }
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            handlePopupMenu(e);
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            handlePopupMenu(e);
        }
        
        private void handlePopupMenu (MouseEvent e) {
            if (!e.isPopupTrigger()) {
                return;
            }
            final JTree tree = (JTree) e.getSource();
            Point p = e.getPoint();
            int x = e.getX();
            int y = e.getY();
            int row = tree.getRowForLocation(x, y);
            TreePath path = tree.getPathForRow(row);
            
            if (path != null) {
                DirectoryNode node = (DirectoryNode) path.getLastPathComponent();
                ((DirectoryTreeModel) tree.getModel()).nodeChanged(node);
                if(!fileChooser.getFileSystemView().isFileSystem(node.getFile())) {
                    return;
                }

                tree.setSelectionPath(path);
                popupMenu.show(tree, x, y);
            }            
        }
        
        private void changeTreeDirectory(File dir) {
            if (File.separatorChar == '\\' && dir.getPath().endsWith(".lnk")) {
                File linkLocation = getShellFolderForFileLinkLoc(dir);
                if (linkLocation != null && fileChooser.isTraversable(linkLocation)) {
                    dir = linkLocation;
                } else {
                    return;
                }
            }
            fileChooser.setCurrentDirectory(dir);
        }
        
        /********** implementation of CellEditorListener ****************/

        /** Refresh filename text field after rename */
        public void editingStopped(ChangeEvent e) {
            DirectoryNode node = (DirectoryNode) tree.getLastSelectedPathComponent();
            if (node != null) {
                setFileName(getStringOfFileName(node.getFile()));
            }
        }

        public void editingCanceled(ChangeEvent e) {
            // no operation
        }

        /********** ActionListener impl, slow-double-click rename ******/ 
        
        public void actionPerformed(ActionEvent e) {
            if (tree.isFocusOwner() && isSelectionKept(pathToRename)) {
                DirectoryNode node = (DirectoryNode)tree.getLastSelectedPathComponent();
                if (node != null) {
                    applyEdit(node);
                }
            }
            // clear
            cancelRename();
        }
        
        void preprocessMouseEvent (MouseEvent e) {
            if ((e.getID() != MouseEvent.MOUSE_PRESSED) || (e.getButton() != MouseEvent.BUTTON1)) {
                return;
            }
            TreePath clickedPath = tree.getPathForLocation(e.getX(), e.getY());
            if (clickedPath != null && isSelectionKept(clickedPath)) {
                pathToRename = clickedPath;
            }
        }
        
        private boolean isSelectionKept (TreePath selPath) {
            if (curSelPath != null) {
                TreePath oldSel = curSelPath.get();
                if (oldSel != null && oldSel.equals(selPath)) {
                    return true;
                }
            }
            return false;
        }
        
        private void cancelRename () {
            if (renameTimer != null) {
                renameTimer.stop();
                renameTimer = null;
            }
            pathToRename = null;
        }
        
        /******** implementation of focus listener, for slow click rename cancelling ******/ 

        public void focusGained(FocusEvent e) {
            // don't allow to invoke click to rename immediatelly after focus gain
            // what may happen is that tree gains focus by mouse
            // click on selected item - on some platforms selected item
            // is not visible without focus and click to rename will
            // be unwanted and surprising for users
            
            // see run method
            SwingUtilities.invokeLater(this);
        }

        public void run() {
            cancelRename();
        }

        public void focusLost(FocusEvent e) {
            cancelRename();
        }

    }
    
    private class TreeExpansionHandler implements  TreeExpansionListener  {
        public void treeExpanded(TreeExpansionEvent evt) {
            TreePath path = evt.getPath();
            DirectoryNode node = (DirectoryNode) path
                    .getLastPathComponent();
            if(!node.isLoaded()) {
                expandNode(fileChooser, path);
            } else {
                // fixed #96954, to be able to add a new directory
                // when the node has been already loaded
                if(addNewDirectory) {
                    addNewDirectory(path);
                    addNewDirectory = false;
                }
                // Fix for IZ#123815 : Cannot refresh the tree content
                refreshNode( path , node );
            }
        }
        public void treeCollapsed(TreeExpansionEvent event) {
        }
        
    }

    // Fix for IZ#123815 : Cannot refresh the tree content
    private void refreshNode( final TreePath path, final DirectoryNode node ){
        final File folder = node.getFile();

        // Additional fixes for IZ#116859 [60cat] Node update bug in the "open project" panel while deleting directories
        if ( !folder.exists() ){
            TreePath parentPath = path.getParentPath();
            boolean refreshTree = false;

            if(tree.isExpanded(path)) {
                tree.collapsePath(path);
                refreshTree = true;
            }
            model.removeNodeFromParent( node );
            if ( refreshTree ){
                tree.expandPath( parentPath );
            }
            return;
        }

        RequestProcessor.getDefault().post(new Runnable() {
            private Set<String> realDirs;
            public void run() {
                if (!EventQueue.isDispatchThread()) {
                    // first phase
                    realDirs = new HashSet<String>();
                    File[] files = folder.listFiles();
                    files = files == null ? new File[0] : files;
                    for (File file : files) {
                        if ( !file.isDirectory() ){
                            continue;
                        }
                        String name = file.getName();
                        realDirs.add( name );
                    }
                    SwingUtilities.invokeLater(this);
                } else {
                    // second phase, in EQ thread, invoked from first phase
                    int count = node.getChildCount();
                    Map<String,DirectoryNode> currentFiles =
                        new HashMap<String,DirectoryNode>( );
                    for( int i=0; i< count ; i++ ){
                        TreeNode child = node.getChildAt(i);
                        if ( child instanceof DirectoryNode ){
                            File file = ((DirectoryNode)child).getFile();
                            currentFiles.put( file.getName() , (DirectoryNode)child);
                        }
                    }

                    Set<String> realCloned = new HashSet<String>( realDirs );
                    if ( realCloned.removeAll( currentFiles.keySet()) ){
                        // Handle added folders
                        for ( String name : realCloned ){
                            DirectoryNode added = new DirectoryNode( new File( folder, name ) );
                            model.insertNodeInto( added, node, node.getChildCount());
                        }
                    }
                    Set<String> currentNames = new HashSet<String>( currentFiles.keySet());
                    if ( currentNames.removeAll( realDirs )){
                        // Handle deleted folders
                        for ( String name : currentNames ){
                            DirectoryNode removed = currentFiles.get( name );
                            model.removeNodeFromParent( removed );
                        }
                    }
                }
            }
        });
    }

    
    private class NewDirectoryAction extends AbstractAction {
        public void actionPerformed(ActionEvent e) {
            final TreePath path = tree.getSelectionPath();
            
            if(path == null) {
                // if no nodes are selected, get the root node
                // fixed #96954, to be able to add a new directory
                // in the current directory shown in the tree
                addNewDirectory(new TreePath(model.getPathToRoot((DirectoryNode)tree.getModel().getRoot())));
            }
            
            if(path != null) {
                if(tree.isExpanded(path)) {
                    addNewDirectory(path);
                } else {
                    addNewDirectory = true;
                    tree.expandPath(path);
                }
            }
        }
    }
    
    private class DirectoryTreeRenderer implements TreeCellRenderer {
        HtmlRenderer.Renderer renderer = HtmlRenderer.createRenderer();

        public Component getTreeCellRendererComponent(
                JTree tree,
                Object value,
                boolean isSelected,
                boolean expanded,
                boolean leaf,
                int row,
                boolean hasFocus) {
            
            Component stringDisplayer = renderer.getTreeCellRendererComponent(tree,
                    value,
                    isSelected,
                    expanded,
                    leaf,
                    row,
                    hasFocus);
            
            if(value instanceof DirectoryNode) {
                tree.setShowsRootHandles(true);
                DirectoryNode node = (DirectoryNode)value;
                ((JLabel)stringDisplayer).setIcon(getNodeIcon(node));
                ((JLabel)stringDisplayer).setText(getNodeText(node.getFile()));
            }
                Font f = stringDisplayer.getFont();
                stringDisplayer.setPreferredSize(new Dimension(stringDisplayer.getPreferredSize().width, 30));

            // allow some space around icon of items
            ((JComponent)stringDisplayer).setBorder(BorderFactory.createEmptyBorder(1, 0, 1, 0));
            stringDisplayer.setSize(stringDisplayer.getPreferredSize());
            
            return stringDisplayer;
        }
        
        private Icon getNodeIcon(DirectoryNode node) {
            File file = node.getFile();
            if(file.exists()) {
                //TODO icon changer code started
                if (customDirectoryProvider != null && customDirectoryProvider.isValidCustomDirectory(file)) {
                    return customDirectoryProvider.getCustomDirectoryIcon();
                }
                //TODO icon changer code ended

                return fileChooser.getIcon(file);
            } else {
                return null;
            }
        }
        
        private String getNodeText(File file) {
            if(file.exists()) {
                return "<html>" + fileChooser.getName(file) + "</html>";
            } else {
                return "<html></html>";
            }
        }
    }
    
    private class DirectoryTreeModel extends DefaultTreeModel {
        
        public DirectoryTreeModel(TreeNode root) {
            super(root);
        }
        
        public void valueForPathChanged(TreePath path, Object newValue) {
            boolean refreshTree = false;
            DirectoryNode node = (DirectoryNode)path.getLastPathComponent();
            File f = node.getFile();
            File newFile = new File(f.getParentFile(), (String)newValue);
            
            if(f.renameTo(newFile)) {
                // fix bug #97521, #96960
                if(tree.isExpanded(path)) {
                    tree.collapsePath(path);
                    refreshTree = true;
                }
                
                node.setFile(newFile);
                node.removeAllChildren();
                
                ((DefaultTreeModel) tree.getModel()).nodeStructureChanged(node);
                if(refreshTree) {
                    tree.expandPath(path);
                }
            }
        }
    }
}
