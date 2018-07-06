package com.virtualhosts.apache;

import com.virtualhosts.Config;
import com.virtualhosts.OsType;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.InetAddress;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VirtualHost {
    private String hostName;
    private String serverName;
    private InetAddress address;
    private String alias;
    private String publicFolder = "";
    private String documentRoot;
    private Boolean rewriteEngine = false;
    private OsType Os = Config.getOs();
    //Constructor
    public VirtualHost(
                        String hostname,
                       String serverName,
                       InetAddress address,
                       @Nullable String publicFolder,
                       @Nullable String alias,
                       @Nullable String documentRoot,
                       @Nullable Boolean rewriteEngine
    ) {
        this.hostName = hostname;
        this.alias = alias;
        this.serverName = serverName;
        this.address = address;
        if(publicFolder != null) {
            this.publicFolder = publicFolder;
        }
        if (documentRoot != null) {
            this.documentRoot = documentRoot;
        } else {
            this.documentRoot = "/var/www";
        }
        if(rewriteEngine != null) {
            this.rewriteEngine = rewriteEngine;
        }
        this.hostName = serverName.split("\\.")[0];
    }

    /**
     * Checks if the host exits
     * @param hostsFile Hosts file in /etc/
     * @return returns whether the host exits or not
     * @throws IOException if the file is not found
     */
   private boolean hostExits(File hostsFile) throws IOException {
        FileReader reader = new FileReader(hostsFile);
        //Regex for matching the line in the hosts file
        Pattern regex = Pattern.compile("\\n?(\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\\.\\d{0,3})(\\t | \\s+)(.*+)\\n?");
        Scanner scanner = new Scanner(reader);
        Matcher match;
        String line;
        while(scanner.hasNextLine()) {
            line = scanner.nextLine();
            match = regex.matcher(line);
            if(match.group(1).equals(this.address.toString()) && match.group(3).equals(this.serverName)) {
                reader.close();
                return true;
            }
        }
        reader.close();
        return false;
    }

    /**
     * Adds the configuration to apache for the given parameters
     * @throws FileAlreadyExistsException Throws an error if the config file exits
     * @throws IOException If it fails to create new config file, mainly because of root access
     */
    private void addConf() throws  FileAlreadyExistsException, IOException, NullPointerException {
        File newSite = null;
        FileWriter writer;
        if(this.Os == OsType.Linux) {
            newSite = new File(Config.SITESAVAILABLE.concat(this.hostName));
            if (newSite.isFile() || newSite.exists()) {
                throw new FileAlreadyExistsException("Config file exits");
            }
            if (newSite.createNewFile()) {
                System.out.println("File created");
            } else {
                System.out.println("Error has accured");
                return;
            }
        } else if(this.Os == OsType.Windows) {
            newSite = new File(Config.SITESAVAILABLE);
            if(!newSite.exists())
                throw new FileNotFoundException();
        }
        if(newSite == null) {
            throw new NullPointerException();
        }
        writer = new FileWriter(newSite);
        writer.write(this.toString());
        writer.close();
    }

    /**
     * Created the directory for the given virtual host
     * @throws FileAlreadyExistsException Throws an error if the folder exits
     */
    private void createDirectoryForVirtualHost() throws FileAlreadyExistsException {
        File site = new File(Config.SITES.concat(hostName));
        if(site.isDirectory()) {
            throw new FileAlreadyExistsException("Direcotory already exits");
        }
        if(site.mkdir()) {
            System.out.println("Directory created");
        } else {
            System.out.println("Error accured while creating directory");
        }
    }

    /**
     * Public method for uniting all other methods
     */
    public void createNewVirtualHost() {
        try {
            if(Config.getOs() == OsType.Linux)
                if(apacheExits()) {
                    throw new NotDirectoryException("Apache is not installed");
                }
            addConf();
            writeToHostsFile();
            createDirectoryForVirtualHost();
        } catch (FileAlreadyExistsException e) {
            e.getMessage();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes to system hosts file
     */
    private void writeToHostsFile() {
        File hosts = new File(Config.HOSTS);
        try {
            if(hostExits(hosts)){
                System.out.println("Host already exists");
                return;
            }
            FileWriter writer = new FileWriter(hosts);
            writer.append("\n")
                    .append(this.address.toString())
                    .append("\t")
                    .append(this.serverName);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks for the apache folder, to determent if the apache is installed or not
     * This method works only in LINUX !!!
     * @return boolean
     */
    private boolean apacheExits() {
        return !new File("/etc/apache2").isDirectory();
    }

    //Implement apache virtual host syntax
    @Override
    public String toString() {
        return "";
    }
}
