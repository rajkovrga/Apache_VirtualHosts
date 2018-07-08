package com.virtualhosts;

import com.virtualhosts.apache.HostNotFoundException;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Host {
    private InetAddress address;
    private String serverName;
    private static File file = new File(Config.HOSTS);
    public Host(InetAddress address, String serverName) {
        this.address = address;
        this.serverName = serverName;
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
     * @return returns whether the host exits or not
     * @throws IOException if the file is not found
     */
    public boolean hostExits() throws Exception {
        return read().containsKey(this.serverName) && read().containsValue(this.address);
    }

    /**
     * Gets the list of hosts,
     * Abstraction for read method
     * @return List of hosts
     */
    public static List<Host> getAllHosts() {
        try {
            var read = read();
            List<Host> list = new ArrayList<>(5);
            for(var key : read.keySet()) {
                list.add(new Host(read.get(key), key));
            }
            return list;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
    }

    /**
     * Reading the hosts file into Map
     * @return Map
     * @throws FileNotFoundException throws this if the hosts file doesn't exit
     */
    public static Map<String,InetAddress> read() throws Exception {
        //Check if the file is readable
        if(!file.canRead()) {
            throw new Exception("File is not readable, try running it as Administrator/Root");
        }
        var map = new HashMap<String, InetAddress>();

        Scanner scanner = new Scanner(Host.getFile());
        String pattern =  "\\n?(\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\\.\\d{0,3})(\\t+|\\s*)(.*)\\n?";
        Pattern regex = Pattern.compile(pattern);
        Matcher matcher;
        byte[] bytes = new byte[4];
        while(scanner.hasNextLine()) {
            matcher = regex.matcher(scanner.nextLine());
            //Checks if the line matches,
            // if it doesn't continues to the next line
            if(!matcher.matches()) continue;

            //Ip Address group
            var ipStr = matcher.group(1);

            //Splits the line to String[]
            String[] ip = ipStr.split("\\.");
            for(int i = 0; i < ip.length; i++) {
                bytes[i] = Byte.parseByte(ip[i]);
            }
            try {
                map.put(matcher.group(3), InetAddress.getByAddress(bytes));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    /**
     * Writes to system hosts file
     * TODO - Check this method
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
            e.printStackTrace();
        }
    }

    /**
     * Gets single host from the Hosts file
     * @param hostName domain
     * @return new instance of Hosts class
     * @throws HostNotFoundException if the hosts isn't found
     */
    public static Host get(String hostName) throws HostNotFoundException {
        try {
            var read = read();
            //Checks for the hostname
            if(read.containsKey(hostName)) {
                var address = read.get(hostName);
                return new Host(address, hostName);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        throw new HostNotFoundException();
    }

    //TODO - Find some good ideas to solve problems for rewriting updating and deleting
    //TODO - Add params to rewrite
    //TODO - More checking
    /**
     * Rewriting Hosts file after update or delete
     *
     * TODO - Need implementing
     */
    private void rewrite() {

    }
    /**
     * Updates existing host
     *
     * TODO - Needs implementing
     */
    public void update() {

    }

    /**
     * Deletes existing host
     *
     *  TODO - Needs implementing
     */
    public void delete() {

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
