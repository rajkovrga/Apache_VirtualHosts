package com.virtualhosts;

enum OsType {
    Windows, Linux, Mac
}


public class Config {
    public static String SITESAVAILABLE = "/etc/apache2/sites-available/";
    public static String SITES = "/var/www";


    public  static OsType getOs() {
        String os = System.getProperty("os.name");
        switch (os) {
            case "Linux":
                return OsType.Linux;
            case "Widows":
                return OsType.Windows;
            case "Mac":
                return OsType.Mac;
                default:
                    return null;
        }
    }
}
