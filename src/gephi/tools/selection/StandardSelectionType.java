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

package gephi.tools.selection;

import gephi.visualization.selection.SelectionArea;
import gephi.visualization.selection.SelectionType;
import javax.swing.ImageIcon;

/**
 *
 * @author Mathieu
 */
public class StandardSelectionType implements SelectionType
{
    private SelectionArea selectionArea;
	private String name;
	private ImageIcon icon;
	private boolean sliderActive=true;
	private int minTick=0;
	private int maxTick=100;
	private int currentSize=10;

	public StandardSelectionType(String name, SelectionArea area)
	{
		//UserPreferences prefs = Application.instance.getUserPreferences();

		//this.selectionArea = area;
		//this.name = prefs.getStringLocale(name);
		//this.icon = IconLoader.getInstance().getIconResourcePNG(name);
	}

    @Override
	public String getName() {
		return name;
	}

    @Override
	public void setName(String name) {
		this.name = name;
	}

	public ImageIcon getIcon() {
		return icon;
	}

	public boolean isSliderActive() {
		return sliderActive;
	}

	public void setSliderActive(boolean sliderActive) {
		this.sliderActive = sliderActive;
	}

	public int getMinTick() {
		return minTick;
	}

	public void setMinTick(int minTick) {
		this.minTick = minTick;
	}

	public int getMaxTick() {
		return maxTick;
	}

	public void setMaxTick(int maxTick) {
		this.maxTick = maxTick;
	}

    @Override
	public SelectionArea getSelectionArea() {
		return selectionArea;
	}

    @Override
	public void setSelectionArea(SelectionArea selectionArea) {
		this.selectionArea = selectionArea;
	}

	public int getCurrentSize() {
		return currentSize;
	}

	public void setCurrentSize(int currentSize) {
		this.currentSize = currentSize;
	}
}
