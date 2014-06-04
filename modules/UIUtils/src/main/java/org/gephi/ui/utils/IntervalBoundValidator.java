/*
 Copyright 2008-2010 Gephi
 Authors : Eduardo Ramos <eduramiba@gmail.com>
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
package org.gephi.ui.utils;

import javax.swing.text.JTextComponent;
import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;

/**
 * Utils class to validate a string that contains a valid title for a column of
 * a
 * <code>AttributeTable</code>.
 *
 * @author Eduardo Ramos <eduramiba@gmail.com>
 */
public class IntervalBoundValidator implements Validator<String> {

    private JTextComponent intervalStartTextField = null;

    public IntervalBoundValidator() {
    }

    public IntervalBoundValidator(JTextComponent intervalStartTextField) {
        this.intervalStartTextField = intervalStartTextField;
    }

    @Override
    public boolean validate(Problems prblms, String componentName, String value) {
        return false;
//        try {
//            double time = DynamicParser.parseTime(value);
//            if (intervalStartTextField != null) {
//                //Also validate that this (end time) is greater or equal than start time.
//                try {
//                    double startTime = DynamicParser.parseTime(intervalStartTextField.getText());
//                    if (time < startTime) {
//                        prblms.add(NbBundle.getMessage(IntervalBoundValidator.class, "IntervalBoundValidator.invalid.interval.message"));
//                        return false;
//                    } else {
//                        return true;
//                    }
//                } catch (ParseException parseException) {
//                    return true;
//                }
//            } else {
//                return true;
//            }
//        } catch (ParseException ex) {
//            prblms.add(NbBundle.getMessage(IntervalBoundValidator.class, "IntervalBoundValidator.invalid.bound.message"));
//            return false;
//        }
    }
}
