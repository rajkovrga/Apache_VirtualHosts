package com.virtualhosts;


import com.virtualhosts.apache.VirtualHost;

import java.io.File;
import java.net.InetAddress;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) throws Exception {
        OsType type = Config.getOs();
        String hostname = null;
        String serverName = null;
        InetAddress address = null;
        String publicFolder = null;
        String documentRoot = null;
        String allias = null;
        Boolean rewriteEngine = false;
        for (var i = 0; i < args.length; i++)
            switch (args[i].toLowerCase())
            {
                case "--hostname":
                    hostname = args[i + 1];
                    break;
                case "--server-name":
                    serverName = args[i + 1];
                    break;
                case "--ip-address":
                    String[] strBytes;
                    if(Pattern.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}", args[i+1])) {
                        strBytes = args[i + 1].split("\\.");
                    }
                    else
                        throw new Exception("Ip address is not in corret format");
                    byte[] bytes = new byte[4];
                    for ( int j = 0; j < strBytes.length; j++) {
                        bytes[j] = Byte.parseByte(strBytes[j]);
                    }
                    address = InetAddress.getByAddress(bytes);
                    break;
                case "--public-folder":
                    publicFolder = args[i + 1];
                    break;
                case "--document-root":
                    documentRoot = args[ i + 1];
                    break;
                case "--alias":
                    allias = args[i+1];
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
            }
        assert type != null;
        switch (type) {
            case Windows:
                File file = new File("C:\\xampp\\htdocs");
                File sites = new File("C:\\xampp\\apache\\conf\\extra\\httpd-vhosts.conf");
                if (!file.exists()) {
                    System.out.println("Provide us with path to htdocs folder with --sites-dest flag or install XAMPP in default location");
                    return;
                }
                if (!sites.exists()) {
                    System.out.println("Provide us with path to apache\\config\\extra folder with --sites-available flag or install XAMPP in default location");
                    return;
                }
                Config.SITES = "C:\\xampp\\htdocs\\";
                Config.SITESAVAILABLE = "C:\\xampp\\apache\\conf\\extra\\httpd-vhosts.conf";
                Config.HOSTS = "C:\\Windows\\System32\\drivers\\etc\\hosts";
                break;
            case Linux:
                break;
            case Mac:
                System.out.println("Macs are not supported");
                return;
            default:
                System.out.println("This type of operating system is not supported");
                return;
        }
        if (hostname == null || serverName == null || address == null){
            System.out.println("Hostname or ServerName or IpAddress is not supplied");
            return;
        }
        VirtualHost vh = new VirtualHost(hostname, serverName, address, publicFolder, allias, documentRoot, rewriteEngine);
        vh.createNewVirtualHost();
    }
}
