package com.virtualhosts.apache;

import com.virtualhosts.Config;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Inet4Address;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VirtualHost {
    private String hostName;
    private String serverName;
    private Inet4Address address;
    private String alias;
    private String publicFolder = "";
    private String documentRoot;
    private Boolean rewriteEngine = false;

    //Constructor
    public VirtualHost(
                        String hostname,
                       String serverName,
                       Inet4Address address,
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
    private void addConf() throws  FileAlreadyExistsException, IOException {
        File newSite = new File(Config.SITESAVAILABLE.concat(this.hostName));
        if(newSite.isFile() || newSite.exists()) {
            throw new FileAlreadyExistsException("Config file exits");
        }
        if(newSite.createNewFile()) {
            System.out.println("File created");
        } else {
            System.out.println("Error has accured");
        }
        FileWriter writer = new FileWriter(newSite);
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
     * @throws NotDirectoryException If the apache is not installed throws "Apache is not installed"
     */
    public void createNewVirtualHost() throws NotDirectoryException {
        try {
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
        if(apacheExits()) {
            System.out.println("Apache2 is not installed");
            return;
        }
        String hostsFileLocation = "/etc/hosts";
        File hosts = new File(hostsFileLocation);
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
//                    .append('\n');
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks for the apache folder, to determent if the apache is installed or not
     * @return boolean
     */
    private boolean apacheExits() {
        return !new File("/etc/apache2").isDirectory();
    }

    //Implement apache virtual host sytax
    @Override
    public String toString() {
        return "";
    }
}
