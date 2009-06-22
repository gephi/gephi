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
package org.gephi.validation;

import org.netbeans.validation.api.Problems;
import org.netbeans.validation.api.Validator;
import org.openide.util.NbBundle;

/**
 *
 * @author Mathieu Bastian
 */
public class BetweenZeroAndOneValidator implements Validator<String>
{

    @Override
    public boolean validate(Problems problems, String compName, String model) {
        boolean result = false;
        try {
            Double d = Double.parseDouble(model);
            result = d >= 0 && d <= 1;
        } catch (Exception e) {
        }
        if (!result) {
            String message = NbBundle.getMessage(PositiveNumberValidator.class,
                    "PositiveNumberValidator_NOT_POSITIVE", model);
            problems.add(message);
        }
        return result;
    }
}
