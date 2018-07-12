package com.virtualhosts;


import com.virtualhosts.apache.HostNotFoundException;
import com.virtualhosts.apache.VirtualHost;

import java.io.File;
import java.net.InetAddress;
import java.util.regex.Pattern;


/**
 * Main class - here all magic happens
 * @author Dusan Malusev
 * @version 1.0
 */
public class Main {

    /**
     * Current operating system
     */
    private static OsType type = Config.getOs();

     /**
     * Represents Apache virtual host
     */
    private static VirtualHost virtualHost;

    /**
     * Commands passed to the application
     * [0] - virtual-host (manipulating with apache virtual hosts) | host (modifies system hosts file)
     * [1] - create | update | delete
     *
     */
    private static String[] commands = new String[2];

    /**
     * Domain name
     */
    private static String serverName = null;

    /**
     * Ip address for virtual-host or host
     */
    private static InetAddress address = null;

    /**
     * Folder from where apache will serve website
     */
    private static String publicFolder = null;

    /**
     * Path to the website
     */
    private static String documentRoot = null;

    /**
     * Alias for website
     */
    private static String alias = null;

    /**
     * Determent's if the RewriteEngine will be activated or not
     */
    private static Boolean rewriteEngine = false;

    /**
     * Gets the host by the server name
     */
    private static String get;

    /**
     * Entry point
     * @param args Argument passed through the console
     */
    public static void main(String[] args) {
        try {
            setDefaults();
        } catch (Exception e) {
            System.out.println();
        }
        try {
            parseArgs(args);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return;
        }
        if (!Config.isAbsolutePath()) {
            System.out.println("--sites-available and --sites-dest parameters must be absolute path to xampp or other apache installation");
            return;
        }

        switch (commands[0]) {
            case "virtual-host":
                virtualHost = new VirtualHost(serverName, address, documentRoot, publicFolder, alias, rewriteEngine);
                break;
        }

        Host host;
        switch (commands[1]) {
            case "create":
                if (virtualHost != null) {
                    virtualHost.createNewVirtualHost();
                } else {
                    host = new Host(address, serverName);
                    try {
                        host.write();
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                        return;
                    }
                }
                break;
            case "update":
                if (virtualHost != null) {
                    System.out.println("This method is not implemented yet");
                    return;
                } else {
                    try {
                        if(get == null) {
                            System.out.println("you must provide us with --get parameter");
                            return;
                        }
                        host = Host.get(get);
                        host.update(new Host(address, serverName));
                    } catch (HostNotFoundException | Exception e) {
                        System.out.println(e.getMessage());
                        return;
                    }
                }
                break;
            case "delete":
                if (virtualHost != null) {
                    System.out.println("This method is not implemented yet");
                    return;
                } else {
                    try {
                        if(get == null) {
                            System.out.println("you must provide us with --get parameter");
                            return;
                        }
                        host = Host.get(get);
                        host.delete();
                    } catch (HostNotFoundException | Exception e) {
                        System.out.println(e.getMessage());
                        return;
                    }
                }
                break;
            default:
                System.out.println("This command doesn't exist");
                break;
        }
    }

    /**
     * Parsing the command given from the console
     *
     * @param args Arguments from the console
     * @throws Exception Throws Exception if some of the arguments are not correct
     */
    private static void parseArgs(String[] args) throws Exception {
        for (var i = 0; i < args.length; i++)
            switch (args[i].toLowerCase()) {
                case "--command":
                    commands[0] = args[i + 1];
                    commands[1] = args[i + 2];
                    if (!validateCommand()) {
                        throw new Exception("Command are not valid");
                    }
                    break;
                case "--server-name":
                    serverName = args[i + 1];
                    break;
                case "--ip-address":
                    String[] strBytes;
                    if (Pattern.matches(
                            "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}",
                            args[i + 1]
                    )) {
                        strBytes = args[i + 1].split("\\.");
                    } else
                        throw new Exception("Ip address is not in correct format");
                    byte[] bytes = new byte[4];
                    for (int j = 0; j < strBytes.length; j++) {
                        bytes[j] = Byte.parseByte(strBytes[j]);
                    }
                    address = InetAddress.getByAddress(bytes);
                    break;
                case "--public-folder":
                    publicFolder = args[i + 1];
                    break;
                case "--document-root":
                    documentRoot = args[i + 1];
                    break;
                case "--alias":
                    alias = args[i + 1];
                    break;
                case "--rewrite-engine":
                    rewriteEngine = Boolean.getBoolean(args[i + 1]);
                    break;
                case "--sites-available":
                    Config.SITESAVAILABLE = args[i + 1];
                    break;
                case "--sites-dest":
                    Config.SITES = args[i + 1];
                    break;
                case "--get":
                    get = args[i + 1];
                default:
                    throw new Exception("This parameter doesn't exist");

            }
        if ((Config.SITESAVAILABLE.equals("/etc/apache2/sites-available/") || Config.SITES.equals("/var/www")) && type == OsType.Windows) {
            throw new Exception("--sites-available and --sites-dest parameters must be provided, because xampp is not installed in default location");
        }
    }

    /**
     * Validates the commands given from the console
     *
     * @return If the command is valid
     */
    private static boolean validateCommand() {
        return (commands[0].equals("virtual-host") || commands[0].equals("host")) &&
                (commands[1].equals("create") || commands[1].equals("update") || commands[1].equals("delete"));
    }


    /**
     * Sets the default parameters for the different OSes
     *
     * @throws Exception Throws exception if xampp is not installed on windows in default location
     */
    private static void setDefaults() throws Exception {
        switch (type) {
            case Windows:
                File file = new File("C:\\xampp\\htdocs");
                File sites = new File("C:\\xampp\\apache\\conf\\extra\\httpd-vhosts.conf");
                Config.HOSTS = "C:\\Windows\\System32\\drivers\\etc\\hosts";
                if (!file.exists()) {
                    System.out.println(
                            "Provide us with path to htdocs folder with --sites-dest flag or install XAMPP in default location"
                    );
                }
                if (!sites.exists()) {
                    System.out.println("Provide us with path to apache\\config\\extra folder with --sites-available flag or install XAMPP in default location");
                }
                Config.SITES = "C:\\xampp\\htdocs\\";
                Config.SITESAVAILABLE = "C:\\xampp\\apache\\conf\\extra\\httpd-vhosts.conf";
                break;
            case Linux:
                break;
            case Mac:
                throw new Exception("Macs are not supported");
            default:
                throw new Exception("This type of operating system is not supported");
        }
    }
}
