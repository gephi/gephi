/*
Copyright 2008 WebAtlas
Authors : Mathieu Bastian, Mathieu Jacomy, Julian Bilcke
Website : http://www.gephi.org

This file is part of Gephi.

Gephi is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Gephi is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Gephi.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.gephi.ui.ranking.plugin;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gephi.ranking.api.LabelColorTransformer;
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
@ServiceProvider(service = TransformerUI.class)
public class LabelColorTransformerUI implements TransformerUI {

    public Icon getIcon() {
        return new ImageIcon(getClass().getResource("/org/gephi/ui/ranking/plugin/resources/labelcolor.png"));
    }

    public String getName() {
        return NbBundle.getMessage(LabelColorTransformerUI.class, "LabelColorTransformerUI.name");
    }

    public boolean isNodeTransformer() {
        return true;
    }

    public boolean isEdgeTransformer() {
        return true;
    }

    public Class getTransformerClass() {
        return LabelColorTransformer.class;
    }

    public Transformer buildTransformer(Ranking ranking) {
        RankingController rc = Lookup.getDefault().lookup(RankingController.class);
        return rc.getLabelColorTransformer(ranking);
    }

    public JPanel getPanel(Transformer transformer, Ranking ranking) {
        return new ColorTransformerPanel(transformer, ranking);
    }
}
