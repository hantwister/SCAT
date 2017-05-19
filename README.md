# SCAT
#### HP SiteScope Credential Acquisition Tool


### What is this?
A tool that abuses a remote code execution vulnerability in default HP SiteScope (v11.32 et al) installations to steal credentials stored in SiteScope.

### What remote code execution vulnerability?
HP SiteScope by default enables Java Management Extensions with authentication disabled on port 28006.

### Is this vulnerability known?
Yes and no. SiteScope's help pages discuss enabling authentication for JMX as an optional step during setup, with a vague warning that administrators may want to prevent unauthorized entry, but the consequences may not be immediately clear.

### How do I use this tool?
`java -jar SCAT.jar ip.or.fqdn.here`

### What if I just wanted to run Mimikatz?
While you might be missing out on credentials SiteScope has for remote hosts, you can run Metasploit's `exploit/multi/misc/java_jmx_server` module to obtain a shell, which will be running as `SYSTEM` in default Windows installations.

### What should SiteScope users do?
Read the help pages, and make certain you've followed instructions for enabling authentication for JMX.

### What can HP do to fix this?
A couple of things could help. They could disable the `MLet` MBean, or severely restrict what MBeans the `MLet` MBean is allowed to instantiate. They could disable remote JMX functionality by default, or enable authentication by default with a password randomly generated upon installation.
