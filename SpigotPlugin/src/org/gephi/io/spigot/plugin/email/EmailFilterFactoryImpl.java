/*
Copyright 2008-2010 Gephi
Authors : Yi Du <duyi001@gmail.com>
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

package org.gephi.io.spigot.plugin.email;

import org.gephi.io.spigot.plugin.email.spi.EmailFilter;
import org.gephi.io.spigot.plugin.email.spi.EmailFilterFactory;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;


/**
 *
 * @author Yi Du
 */
@ServiceProvider(service = EmailFilterFactory.class)
public class EmailFilterFactoryImpl implements EmailFilterFactory{

    @Override
    public EmailFilter createEmailFilter(String filterType) {
        EmailFilter[] filters = Lookup.getDefault().lookupAll(EmailFilter.class).toArray(new EmailFilter[0]);
        for(EmailFilter filter : filters){
            if(filter.getFilterType().equals(filterType))
                return filter;
        }
        return null;
    }

}
