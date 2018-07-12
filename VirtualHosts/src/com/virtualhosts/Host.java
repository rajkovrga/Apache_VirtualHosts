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
     * Cleans the ip address
     * eg. localhost/127.0.0.1 will be just 127.0.0.1
     * @param ipAddress IpAddress to be cleaned
     * @return Cleaned ip address
     */
    public String cleanIpAddress(String ipAddress) {
        Pattern replace = Pattern.compile("((\\w+)?/)(.*)");
        Matcher matcher = replace.matcher(ipAddress);

        if(matcher.matches()) {
            ipAddress = ipAddress.replaceFirst("((\\w+)?/)", "");
        }
        return ipAddress;
    }

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
     * Return ip address of the host
     * @return Ip address
     */
    public InetAddress getAddress() {
        return this.address;
    }

    /**
     * Gets the domain name of the host
     * @return Domain name
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Returns the list of all lines in the system hosts file
     * @return List of lines
     */
    public static List<String> getContent() {
        if(content == null) {
            try {
                content = readFile();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return content;
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
    private boolean hostExits(){
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
     * Writes to hosts file
     * @throws Exception Throws exception if host already exits or file is not writable
     */
    public void write() throws Exception {
        if(hostExits()){
            throw new Exception("Host already exists");
        }
        write(this.toString(), true);
        System.out.println("You host added successfully");
    }

    /**
     * Private method that writes to system hosts file
     * @param output Actual string that will be dumped to the system Hosts file
     * @param append Deferments if the first parameter will be appended to the file or file will be completely rewritten
     * @throws Exception This exception is thrown when user doesn't have permission to write to file
     */
    private void write(String output, boolean append) throws Exception {
        try {
            //Check if the file is writable
            if(!file.canWrite()) {
                throw new Exception("File is not writable, try running as Administrator/Root");
            }
            String address = cleanIpAddress(this.address.toString());
            //Writes to hosts file
            FileWriter writer = new FileWriter(file, append);
            writer.append(output);
            writer.close();
//            System.out.println("You host added successfully");
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
    private static List<String> readFile() throws Exception {
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
    public static Map<String,InetAddress> read() {
        var map = new HashMap<String, InetAddress>();
        String pattern =  "\\n?(\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\\.\\d{0,3})(\\t+|\\s*)(.*)\\n?";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher;
        byte[] bytes = new byte[4];
        for(var line : getContent()) {
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
        if(!hostExits()) {
            throw new Exception("Host doesn't exist");
        }
        StringBuilder builder = new StringBuilder();
        Matcher matcher;
        Pattern regex = Pattern.compile("\\n?" + cleanIpAddress(this.address.toString()) + "[\\t|\\s]+" + this.serverName + "\\n?");
        for(var line : getContent()) {
            matcher = regex.matcher(line);
            if(!matcher.matches())  {
                builder.append(line);
            }
        }
        try {
           write(builder.toString(), false);
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
        this.address = newHost.address;
        this.serverName = newHost.serverName;
        System.out.println("Host updated successfully");
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
        System.out.println("Host deleted successfully");
    }

    /**
     * Overriding to string method for better debugging
     * @return String
     */
    @Override
    public String toString() {
        return "\r\n" + this.cleanIpAddress(this.address.toString()) + "\t" + this.serverName;
    }
}
