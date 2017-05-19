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
package scat.pojo;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author hantwister
 */
public class Host implements Serializable {

    public String host, method, os, login, password, sshPort, sshAuthMethod, sshKeyFile;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSshPort() {
        return sshPort;
    }

    public void setSshPort(String sshPort) {
        this.sshPort = sshPort;
    }

    public String getSshAuthMethod() {
        return sshAuthMethod;
    }

    public void setSshAuthMethod(String sshAuthMethod) {
        this.sshAuthMethod = sshAuthMethod;
    }

    public String getSshKeyFile() {
        return sshKeyFile;
    }

    public void setSshKeyFile(String sshKeyFile) {
        this.sshKeyFile = sshKeyFile;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.host);
        hash = 59 * hash + Objects.hashCode(this.method);
        hash = 59 * hash + Objects.hashCode(this.os);
        hash = 59 * hash + Objects.hashCode(this.login);
        hash = 59 * hash + Objects.hashCode(this.password);
        hash = 59 * hash + Objects.hashCode(this.sshPort);
        hash = 59 * hash + Objects.hashCode(this.sshAuthMethod);
        hash = 59 * hash + Objects.hashCode(this.sshKeyFile);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Host other = (Host) obj;
        if (!Objects.equals(this.host, other.host)) {
            return false;
        }
        if (!Objects.equals(this.method, other.method)) {
            return false;
        }
        if (!Objects.equals(this.os, other.os)) {
            return false;
        }
        if (!Objects.equals(this.login, other.login)) {
            return false;
        }
        if (!Objects.equals(this.password, other.password)) {
            return false;
        }
        if (!Objects.equals(this.sshPort, other.sshPort)) {
            return false;
        }
        if (!Objects.equals(this.sshAuthMethod, other.sshAuthMethod)) {
            return false;
        }
        if (!Objects.equals(this.sshKeyFile, other.sshKeyFile)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        String toReturn = method + "://" + login;
        if (password != null && password.trim().length() > 0) {
            toReturn += ":" + password;
        }
        toReturn += "@" + host;
        if ("SSH".equalsIgnoreCase(method) && sshPort != null && sshPort.trim().length() > 0) {
            toReturn += ":" + sshPort;
        }
        if (os != null && os.trim().length() > 0) {
            toReturn += "\nOS: " + os;
        }
        if ("SSH".equalsIgnoreCase(method) && sshAuthMethod != null && sshAuthMethod.trim().length() > 0) {
            toReturn += "\nSSH Auth Method: " + sshAuthMethod;
        }
        if ("SSH".equalsIgnoreCase(method) && "keyfile".equalsIgnoreCase(sshAuthMethod) && sshKeyFile != null && sshKeyFile.trim().length() > 0) {
            toReturn += "\nSSH Key File: " + sshKeyFile;
        }

        return toReturn;
    }
}
