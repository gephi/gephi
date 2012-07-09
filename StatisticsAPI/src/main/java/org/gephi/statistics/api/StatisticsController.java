/*
Copyright 2008-2010 Gephi
Authors : Patick J. McSweeney <pjmcswee@syr.edu>,
          Mathieu Bastian <mathieu.bastian@gephi.org>
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
package org.gephi.statistics.api;

import org.gephi.project.api.Workspace;
import org.gephi.statistics.spi.Statistics;
import org.gephi.statistics.spi.StatisticsBuilder;
import org.gephi.statistics.spi.StatisticsUI;
import org.gephi.utils.longtask.api.LongTaskListener;
import org.gephi.utils.longtask.spi.LongTask;

/**
 * Controller for executing Statistics/Metrics algorithms.
 * <p>
 * This controller is a service and can therefore be found in Lookup:
 * <pre>StatisticsController sc = Lookup.getDefault().lookup(StatisticsController.class);</pre>
 * 
 * @author Patrick J. McSweeney, Mathieu Bastian
 * @see StatisticsBuilder
 */
public interface StatisticsController {

    /**
     * Execute the statistics algorithm in a background thread and notify
     * <code>listener</code> when finished. The <code>statistics</code> should
     * implement {@link LongTask}.
     * @param statistics    the statistics algorithm instance
     * @param listener      a listener that is notified when execution finished
     * @throws IllegalArgumentException if <code>statistics</code> doesn't
     * implement {@link LongTask}
     */
    public void execute(Statistics statistics, LongTaskListener listener);

    /**
     * Executes <code>statistics</code> in the current thread.
     * @param statistics    the statistics to execute
     */
    public void execute(Statistics statistics);
    
    /**
     * Finds the builder from the statistics class.
     * @param statistics    the statistics class
     * @return              the builder, or <code>null</code> if not found
     */
    public StatisticsBuilder getBuilder(Class<? extends Statistics> statistics);

    /**
     * Returns the current <code>StatisticsModel</code>, from the current
     * workspace
     * @return              the current <code>StatisticsModel</code>
     */
    public StatisticsModel getModel();
    
    /**
     * Returns the <code>StatisticsModel</code> for <code>workspace</code>
     * @param               workspace the workspace to return the model for
     * @return              the <code>StatisticsModel</code> associated to
     *                      <code>workspace</code>
     */
    public StatisticsModel getModel(Workspace workspace);
}
