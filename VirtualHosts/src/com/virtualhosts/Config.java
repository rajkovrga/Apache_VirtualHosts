package com.virtualhosts;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * Class for managing configuration of the program
 * Manages: all paths, OS Version
 * @author Dusan Malusev
 * @version 1.0
 *
 */
public class Config implements Serializable {
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
    static String HOSTS = "/etc/hosts";


    //Setting constructor to private, so that nobody could create new object
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

    /**
     * Creating config.xml file for storing Configuration
     *
     * @throws IOException Throws an error if file couldn't be created
     */
    private static void createConfigFile() throws IOException {
        File config = new File("config.xml");
        if(!config.exists()) {
            boolean created = config.createNewFile();
            if(created) {
                System.out.println("File created successfully");
            } else {
                System.out.println("An error has accures while creating file");
            }
        }
    }

    public static boolean isAbsolutePath() {
        return new File(SITES).isAbsolute() && new File(SITESAVAILABLE).isAbsolute();
    }


    // TODO - implement writeConfig
    /**
     * Writes the initial configuration
     * @throws IOException Throws an error if file couldn't be created
     */
    public static void writeConfig() throws IOException {
        //Creating xml configuration
        createConfigFile();
        //Write XML
    }

    // TODO - implement modifyConfig
    /**
     * Modifies the existing configuration
     *
     * @throws Exception Throws exception if configuration file doesn't exit
     */
    public static void modifyConfig() throws Exception {
        //Check if config file exits
        if(!new File("config.xml").exists()) {
            throw new Exception("Config file doesn't exits, use write method to create new");
        }

    }

}
