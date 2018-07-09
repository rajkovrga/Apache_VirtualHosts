package com.virtualhosts;

/**
 * Class for managing configuration of the program
 * Manages: all paths, OS Version
 * @author Dusan Malusev
 * @version 1.0
 *
 */
public class Config {
    /**
     * On Linux systems apache virtual hosts go into separate .conf files into sites-available in apache2 folder
     * Specific to Linux OS
     * /etc/apache2/sites-available/
    */
    public static String SITESAVAILABLE = "/etc/apache2/sites-available/";

    /**
     * Path to the folder where all websites will be stored
     * Default path is set to the Linux OS path
     * /var/www/
     */
    public static String SITES = "/var/www";
    /**
     * Path to System hosts file
     * Default to Linux OS
     * /etc/hosts
     */
    public static String HOSTS = "/etc/hosts";

    private Config() {}
    /**
     * Gets the OS version
     * @return OsType Enum
     */
    public static OsType getOs() {
        String os = System.getProperty("os.name");
        if (os.toLowerCase().contains("windows")) return OsType.Windows;
        else if (os.toLowerCase().contains("linux")) return OsType.Linux;
        else if (os.toLowerCase().contains("mac")) return OsType.Mac;
        else return null;
    }
}
