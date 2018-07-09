package com.virtualhosts.apache;

import com.virtualhosts.Config;
import com.virtualhosts.Host;
import com.virtualhosts.OsType;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;

public class VirtualHost {
    private String hostName;
    private String serverName;
    private InetAddress address;
    private Host hosts;
    private String alias;
    private String publicFolder = "";
    private String documentRoot;
    private Boolean rewriteEngine = false;
    private OsType Os = Config.getOs();
    //Constructor
    public VirtualHost(
                       String serverName,
                       @Nullable InetAddress address,
                       @Nullable String publicFolder,
                       @Nullable String alias,
                       @Nullable String documentRoot,
                       @Nullable Boolean rewriteEngine
    ) {
        this.hostName = serverName.split("\\.")[0];
        this.alias = alias;
        if(address == null) {
            this.address = InetAddress.getLoopbackAddress();
        } else {
            this.address = address;
        }
        this.serverName = serverName;
        this.hosts = new Host(address, serverName);
        if(publicFolder != null) {
            this.publicFolder = publicFolder;
        }
        if (documentRoot != null) {
            this.documentRoot = documentRoot;
        } else {
            this.documentRoot = Config.SITES + this.hostName;
        }
        if(rewriteEngine != null) {
            this.rewriteEngine = rewriteEngine;
        }
    }
    public VirtualHost(String serverName) {
        this(serverName, null, null, null, null, false);
    }
    public VirtualHost(String serverName, byte[] address) throws UnknownHostException {
        this(serverName, InetAddress.getByAddress(address), null, null, null,false);
    }
    public VirtualHost(String serverName, byte[] address, String publicFolder) throws UnknownHostException {
        this(serverName, InetAddress.getByAddress(address), publicFolder, null, null,false);
    }
    public VirtualHost(String serverName, byte[] address, String publicFolder, String documentRoot) throws UnknownHostException {
        this(serverName, InetAddress.getByAddress(address), publicFolder, null, documentRoot,false);
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
            throw new FileAlreadyExistsException("Directory already exits");
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
            try {
                this.hosts.write();
            } catch (Exception e) {
                e.printStackTrace();
                e.getMessage();
            }
            createDirectoryForVirtualHost();
        } catch (FileAlreadyExistsException e) {
            e.getMessage();
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
