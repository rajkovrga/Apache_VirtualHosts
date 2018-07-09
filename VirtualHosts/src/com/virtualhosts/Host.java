package com.virtualhosts;

import com.virtualhosts.apache.HostNotFoundException;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Class for manipulating System Hosts file
 * @author Dusan Malusev
 * @version 1.0
 */
public class Host {

    /**
     * Reference address
     */
    private InetAddress address;

    /**
     * Actual domain name
     */
    private String serverName;

    /**
     *  System Hosts file
     */
    private static final File file = new File(Config.HOSTS);

    /**
     * Content of the HOSTS file
     */
    private static List<String> content;

    /**
     * Primary constructor
     * @param address Reference address
     * @param serverName Domain name
     */
    public Host(InetAddress address, String serverName) {
        this.address = address;
        this.serverName = serverName;
        try {
            content = readFile();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Gets ip address by the bytes given
     * Constructor overload
     * @param address IP address in bytes
     * @param serverName Domain name
     * @throws UnknownHostException This Exception is thrown when the domain name is not found
     */
    public Host(byte[] address, String serverName) throws UnknownHostException {
        this(InetAddress.getByAddress(address), serverName);
    }

    /**
     *
     * @param hostname Searches the IP address by the given hostname
     * @param serverName Domain name
     * @throws UnknownHostException This Exception is thrown when the domain name is not found
     */
    public Host(String hostname, String serverName) throws UnknownHostException {
        this(InetAddress.getAllByName(hostname)[0], serverName);
    }

    /**
     * Overloaded constructor
     * Address = 127.0.0.1
     * @param serverName Domain name
     */
    public Host(String serverName) {
        this(InetAddress.getLoopbackAddress(), serverName);
    }

    /**
     * Getter for this.file
     * @return Hosts file
     */
    public static File getFile() {
        return Host.file;
    }

    /**
     * Checks if the host exits
     *
     * @return returns whether the host exits or not
     */
    public boolean hostExits(){
        return read().containsKey(this.serverName) && read().containsValue(this.address);
    }


    /**
     * Gets the list of hosts,
     * Abstraction for read method
     * @return List of hosts
     */
    public static List<Host> getAllHosts() {
        try {
            var read = Host.read();
            List<Host> list = new ArrayList<>(5);
            for(var key : read.keySet()) {
                list.add(new Host(read.get(key), key));
            }
            return list;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }



    /**
     * Writes to system hosts file
     * @throws Exception This exception is thrown when user doesn't have permission to write to file
     */
    public void write() throws Exception {
        try {

            //Checks if the hosts already exits
            if(hostExits()){
                System.out.println("Host already exists");
                return;
            }
            //Check if the file is writable
            if(!file.canWrite()) {
                throw new Exception("File is not writable, try running as Administrator/Root");
            }
            //Writes to hosts file
            FileWriter writer = new FileWriter(file);
            writer.append("\n")
                    .append(this.address.toString())
                    .append("\t")
                    .append(this.serverName);
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Gets single host from the Hosts file
     * @param hostName domain
     * @return new instance of Hosts class
     * @throws HostNotFoundException if the hosts isn't found
     *
     */
    public static Host get(String hostName) throws HostNotFoundException {
        try {
            var read = read();
            //Checks for the hostname
            if(read.containsKey(hostName)) {
                var address = read.get(hostName);
                return new Host(address, hostName);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        throw new HostNotFoundException();
    }

    /**
     * Reads all Lines of the HOSTS file in List
     * @return List of strings
     * @throws Exception This exception is thrown when file is not readable or user doesn't have permission to read it
     */
    private List<String> readFile() throws Exception {
        ArrayList<String> lines = new ArrayList<>(20);
        if(!file.canRead()) {
            throw new Exception("File is not readable, try running it as Administrator/Root");
        }
        String line;
        FileReader fileReader = new FileReader(file);
        BufferedReader reader = new BufferedReader(fileReader);
        while((line = reader.readLine())!= null) {
            lines.add(line);
        }

        //Cleaning up
        reader.close();
        fileReader.close();
        lines.trimToSize();
        return lines;
    }

    /**
     * Reading the hosts file into Map of String (Domain name) as key and InetAddress as Ip address
     * @return Map
     */
    public static Map<String,InetAddress> read(){
        var map = new HashMap<String, InetAddress>();
        String pattern =  "\\n?(\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\\.\\d{0,3})(\\t+|\\s*)(.*)\\n?";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher;
        byte[] bytes = new byte[4];
        for(var line : content) {
            matcher = regex.matcher(line);
            if(!matcher.matches()) continue;
            var ipStr = matcher.group(1);
            String[] ip = ipStr.split("\\.");
            for(int i = 0; i < ip.length; i++) {
                bytes[i] = Byte.parseByte(ip[i]);
            }
            try {
                map.put(matcher.group(3), InetAddress.getByAddress(bytes));
            } catch (UnknownHostException e) {
                System.out.println(e.getMessage());
            }
        }
        return map;
    }

    /**
     * Rewriting Hosts file after update or delete
     * @throws Exception This exception is thrown when user doesn't have access to read or write to the file
     */
    private void rewrite() throws Exception {
        StringBuilder builder = new StringBuilder();
        String address = this.address.toString().replace("/", "");
        Matcher matcher;
        Pattern regex = Pattern.compile("\\n?" + address + "[\\t|\\s]+" + this.serverName + "\\n?");
        for(var line : content) {
            matcher = regex.matcher(line);
            if(!matcher.matches())  {
                builder.append(line);
            }
        }
        //Checks if the file is writable
        if(!file.canWrite())  {
            throw new Exception("File is not writable");
        }
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(builder.toString());
            writer.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
    /**
     * Updates existing host
     * @param newHost new Host to override the existing one
     * @throws Exception This exception is thrown when user doesn't have access to read or write to the file
     */
    public void update(Host newHost) throws Exception {
        //Rewrites the whole HOSTS file
        rewrite();
        //Adds new host at the bottom
        newHost.write();
    }

    /**
     * Deletes one existing host
     * by the given parameters in the constructor or through the get method
     *
     * Public extension method to the private rewrite()
     * @throws Exception This exception is thrown when user doesn't have access to read or write to the file
     */
    public void delete() throws Exception {
        rewrite();
    }
    /**
     * Overriding to string method for better debugging
     * @return String
     */
    @Override
    public String toString() {
        return "DomainName: " + this.serverName + "\n" + "Address: " + this.address.toString();
    }
}
