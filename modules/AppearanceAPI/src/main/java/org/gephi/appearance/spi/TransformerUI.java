/*
 Copyright 2008-2013 Gephi
 Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2013 Gephi Consortium. All rights reserved.

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

 Portions Copyrighted 2013 Gephi Consortium.
 */
package org.gephi.appearance.spi;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.JPanel;
import org.gephi.appearance.api.Function;

/**
 * Defines the user interface associated with a transformer.
 * <p>
 * It is a one-to-one relationship as only a single transformer UI can be
 * associated with a transformer.
 * <p>
 * Implementations of this class should be singleton services by adding the
 * <code>@ServiceProvider</code> annotation:
 * <pre>@ServiceProvider(service = TransformerUI.class, position = 2000)</pre>
 * The position parameter is optional but can be used to control the order in
 * which the transformers appear in the user interface. The higher the last.
 *
 * @param <T> transformer class
 */
public interface TransformerUI<T extends Transformer> {

    /**
     * Returns the transformer category.
     *
     * @return transformer category
     */
    public TransformerCategory getCategory();

    /**
     * Returns the transformer panel for the given function.
     *
     * @param function function
     * @return transformer panel
     */
    public JPanel getPanel(Function function);

    /**
     * Returns the transformer's display name.
     *
     * @return display name
     */
    public String getDisplayName();

    /**
     * Returns the transformer's description.
     *
     * @return description or null if missing
     */
    public String getDescription();

    /**
     * Returns the transformer's icon.
     *
     * @return icon or null if missing
     */
    public Icon getIcon();

    /**
     * Returns the control buttons associated with this transformer.
     *
     * @return control buttons or null if missing
     */
    public AbstractButton[] getControlButton();

    /**
     * Returns the transformer class this transformer UI is associated with.
     *
     * @return transformer class
     */
    public Class<? extends T> getTransformerClass();
}
