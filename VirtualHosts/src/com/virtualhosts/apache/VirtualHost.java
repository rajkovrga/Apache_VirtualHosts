package com.virtualhosts.apache;

import com.virtualhosts.Config;
import com.virtualhosts.Host;
import com.virtualhosts.OsType;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.NotDirectoryException;
import java.util.regex.Pattern;

/**
 * Class for manipulating Apache VirtualHosts
 *
 * @author Dusan Malusev
 * @version 1.0
 */
public class VirtualHost {

    /**
     * Folder name
     */
    private String hostName;

    /**
     * Domain
     * <p>
     * eg. example.com
     */
    private String serverName;

    /**
     * Ip Address of the website
     * <p>
     * eg. 127.0.0.1
     */
    private InetAddress address;

    /**
     * Reference to the hosts file for further manipulation
     */
    private Host hosts;

    /**
     * Alias of the website
     * <p>
     * eg. www.example.com
     */
    private String alias;

    /**
     * Folder from where site is served
     * <p>
     * eg. /path/to/the/folder/public
     */
    private String publicFolder = "";

    /**
     * Folder where website will be stored
     * eg. /var/www/example/
     */
    private String documentRoot;

    /**
     * Apache Rewrite Engine
     * <p>
     * default - false
     */
    private Boolean rewriteEngine = false;

    /**
     * Current operating system
     */
    private OsType Os = Config.getOs();

    /**
     * Default constructor
     *
     * @param serverName    Domain name
     * @param address       Ip Address
     * @param publicFolder  Public folder from where the content will be served
     * @param alias         Alias for the web-site - usually www.example.com
     * @param documentRoot  Document root
     * @param rewriteEngine Initially set RewriteEngine off -Default false
     */
    public VirtualHost(String serverName, @Nullable InetAddress address, @Nullable String publicFolder, @Nullable String documentRoot, @Nullable String alias, @Nullable Boolean rewriteEngine) {
        this.hostName = serverName.split("\\.")[0];
        this.alias = alias;

        if (address == null) {
            this.address = InetAddress.getLoopbackAddress();
        } else {
            this.address = address;
        }
        this.serverName = serverName;

        if (publicFolder != null) {
            this.publicFolder = publicFolder;
        }
        if (documentRoot != null) {
            this.documentRoot = documentRoot;
        } else {
            this.documentRoot = Config.SITES + this.hostName;
        }
        if (rewriteEngine != null) {
            this.rewriteEngine = rewriteEngine;
        }
        this.hosts = new Host(this.address, this.serverName);
    }

    public VirtualHost(String serverName) {
        this(serverName, null, null, null, null, false);
    }

    public VirtualHost(String serverName, byte[] address) throws UnknownHostException {
        this(serverName, InetAddress.getByAddress(address), null, null, null, false);
    }

    public VirtualHost(String serverName, byte[] address, String publicFolder) throws UnknownHostException {
        this(serverName, InetAddress.getByAddress(address), publicFolder, null, null, false);
    }

    public VirtualHost(String serverName, byte[] address, String publicFolder, String documentRoot) throws UnknownHostException {
        this(serverName, InetAddress.getByAddress(address), publicFolder, null, documentRoot, false);
    }

    /**
     * Public method for uniting all other methods
     */
    public void createNewVirtualHost() {
        try {
            if (Config.getOs() == OsType.Linux)
                if (apacheExits()) {
                    throw new NotDirectoryException("Apache is not installed");
                }
            write();
            if(! (new File(this.documentRoot).isDirectory())) {
                createDirectoryForVirtualHost();
            }
            try {
                this.hosts.write();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    //TODO - Create update and delete methods

    /**
     *  Gets the existing virtual host
     * @return an Existing virtual host
     */
    public static VirtualHost get(String serverName) {
        return null;
    }

    /**
     * Deletes an existing virtual host
     */
    public void deleteVirtualHost() { }

    /**
     * Updates an existing virtual host
     * @param newHost New host that will replace the old one
     */
    public void updateVirtualHost(VirtualHost newHost) { }

    /**
     * Checks for the apache folder, to determent if the apache is installed or not
     * This method works only in LINUX !!!
     *
     * @return boolean
     */
    private boolean apacheExits() {
        return !new File("/etc/apache2").isDirectory();
    }

    /**
     * Created the directory for the given virtual host
     *
     * @throws FileAlreadyExistsException Throws an error if the folder exits
     */
    private void createDirectoryForVirtualHost() throws FileAlreadyExistsException {
        File site = new File(Config.SITES.concat(hostName));
        if (site.isDirectory()) {
            throw new FileAlreadyExistsException("Directory already exits");
        }
        if (site.mkdir()) {
            System.out.println("Directory created");
        } else {
            System.out.println("Error accured while creating directory");
        }
    }


    /**
     * Reads the file until it hits the EOF
     * @param file File to be read
     * @return Content of the file
     * @throws IOException This exception is thrown if the file is not found or its not readable
     */
    private String readToEnd(File file) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String line;
        while((line = bufferedReader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    /**
     * Check if the host exits
     * @param host Virtual Host file
     * @return If the hosts file exits
     * @throws IOException This exception is thrown if the file is not found or its not readable
     */
    private boolean hostExits(File host) throws IOException {
        String match = this.toString();
        Pattern p = Pattern.compile(match);
        String content = readToEnd(host);
        if(p.matcher(content).matches()) {
            return true;
        }
        return false;
    }

    /**
     * Adds the configuration to apache for the given parameters
     *
     * @throws FileAlreadyExistsException Throws an error if the config file exit
     * @throws IOException                If it fails to create new config file, mainly because of root access
     * @throws NullPointerException       This exception is thrown when file (on windows default apache config file | on linux if new apache host couldn't be created
     */
    private void write() throws FileAlreadyExistsException, IOException, NullPointerException {
        File newSite = null;
        FileWriter writer;
        if (this.Os == OsType.Linux) {
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
        } else if (this.Os == OsType.Windows) {
            newSite = new File(Config.SITESAVAILABLE);
            if (!newSite.exists())
                throw new FileNotFoundException();
        }
        if (newSite == null) {
            throw new NullPointerException();
        }
        if(!hostExits(newSite)) {
            writer = new FileWriter(newSite, true);
            writer.write(this.toString());
            writer.close();
        } else {
            System.out.println("Virtual Hosts already exits");
        }
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("\r\n<VirtualHost ").append(this.hosts.cleanIpAddress(this.address.toString())).append(":80>");
        builder.append("\r\n\tServerName ").append(serverName);
        File f = new File(documentRoot);
        if (!f.isAbsolute()) {
            documentRoot = Config.SITES + documentRoot;
        }
        if (publicFolder.equals("")) {
            if (Config.getOs() == OsType.Windows)
                builder.append("\r\n\tDocumentRoot \"").append(documentRoot).append("\"");
            else
                builder.append("\r\n\tDocumentRoot ").append(documentRoot);
        } else {
            builder.append("\r\n\tDocumentRoot ").append(documentRoot).append(Config.getOs() == OsType.Linux ? "/" : "\\").append(publicFolder);
        }
        if (rewriteEngine) {
            builder.append("\r\n\tRewriteEngine on");
        }
        if (alias != null) {
            builder.append("\r\n\tServerAlias ").append(alias);
        }
        builder.append("\r\n</VirtualHost>");
        return builder.toString();
    }
}
