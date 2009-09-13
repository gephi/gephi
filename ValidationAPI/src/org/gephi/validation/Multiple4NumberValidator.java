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
public final class Multiple4NumberValidator implements Validator<String> {

    @Override
    public boolean validate(Problems problems, String compName, String model) {
        boolean result = false;
        try {
            Integer i = Integer.parseInt(model);
            result = i > 0 && i%4 ==0;
        } catch (Exception e) {
        }
        if (!result) {
            String message = NbBundle.getMessage(Multiple4NumberValidator.class,
                    "Multiple4NumberValidator_NOT_MULTIPLE", model);
            problems.add(message);
        }
        return result;
    }
}
