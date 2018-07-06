package com.virtualhosts;


public class Config {
    public static String SITESAVAILABLE = "/etc/apache2/sites-available/";
    public static String SITES = "/var/www";
    public static String HOSTS = "/etc/hosts";

    public static OsType getOs() {
        String os = System.getProperty("os.name");
        if (os.toLowerCase().contains("windows")) return OsType.Windows;
        else if (os.toLowerCase().contains("linux")) return OsType.Linux;
        else if (os.toLowerCase().contains("mac")) return OsType.Mac;
        else return null;
    }
}
