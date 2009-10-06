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
package org.gephi.ui.ranking.transformer;

import org.gephi.ui.ranking.TransformerUI;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import org.gephi.ranking.ColorTransformer;
import org.gephi.ranking.Ranking;
import org.gephi.ranking.RankingController;
import org.gephi.ranking.Transformer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class ColorTransformerUI implements TransformerUI {

    public Icon getIcon() {
        return new ImageIcon(getClass().getResource("/org/gephi/ui/ranking/transformer/color.png"));
    }

    public String getName() {
        return NbBundle.getMessage(ColorTransformerUI.class, "ColorTransformerUI.name");
    }

    public boolean isNodeTransformer() {
        return true;
    }

    public boolean isEdgeTransformer() {
        return true;
    }

    public Class getTransformerClass() {
        return ColorTransformer.class;
    }

    public Transformer buildTransformer(Ranking ranking) {
        RankingController rc = Lookup.getDefault().lookup(RankingController.class);
        return rc.getColorTransformer(ranking);
    }

    public JPanel getPanel(Transformer transformer) {
        return new ColorTransformerPanel(transformer);
    }
}
