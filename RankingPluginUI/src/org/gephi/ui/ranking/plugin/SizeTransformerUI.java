/*
Copyright 2008-2010 Gephi
Authors : Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.ui.ranking.plugin;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gephi.ranking.api.ObjectSizeTransformer;
import org.gephi.ranking.api.Ranking;
import org.gephi.ranking.api.RankingController;
import org.gephi.ranking.api.Transformer;
import org.gephi.ranking.api.TransformerUI;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = TransformerUI.class, position = 200)
public class SizeTransformerUI implements TransformerUI {

    public Icon getIcon() {
        return new ImageIcon(getClass().getResource("/org/gephi/ui/ranking/plugin/resources/size.png"));
    }

    public String getName() {
        return NbBundle.getMessage(SizeTransformerUI.class, "SizeTransformerUI.name");
    }

    public boolean isNodeTransformer() {
        return true;
    }

    public boolean isEdgeTransformer() {
        return true;
    }

    public JPanel getPanel(Transformer transformer, Ranking ranking) {
        return new SizeTransformerPanel(transformer, ranking);
    }

    public Class getTransformerClass() {
        return ObjectSizeTransformer.class;
    }

    public Transformer buildTransformer(Ranking ranking) {
        RankingController rc = Lookup.getDefault().lookup(RankingController.class);
        return rc.getObjectSizeTransformer(ranking);
    }
}
