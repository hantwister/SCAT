/*
 * Copyright (C) 2017 hantwister
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package scat.mbean;

import java.util.Collection;
import java.util.List;
import scat.pojo.Credential;
import scat.pojo.Host;
import scat.pojo.SiteScopeUser;

/**
 *
 * @author hantwister
 */
public interface ExtractorMBean {
    public Collection<SiteScopeUser> getSiteScopeUsers() throws Throwable;
    public Collection<Credential> getCredentials() throws Throwable;
    public Collection<Host> getHosts() throws Throwable;
    public List<String> getFile(String filename) throws Throwable;
}
