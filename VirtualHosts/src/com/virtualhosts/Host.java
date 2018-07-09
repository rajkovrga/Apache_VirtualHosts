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
    private static String[] content;



    //Constructors
    public Host(InetAddress address, String serverName) {
        this.address = address;
        this.serverName = serverName;
        try {
            content = readFile();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Host(byte[] address, String serverName) throws UnknownHostException {
        this(InetAddress.getByAddress(address), serverName);
    }

    public Host(String hostname, String serverName) throws UnknownHostException {
        this(InetAddress.getAllByName(hostname)[0], serverName);
    }

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
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        return null;
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
     *
     * TODO - Check this method
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
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
        throw new HostNotFoundException();
    }

    private String[] readFile() throws Exception {
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
        return (String[])lines.toArray();
    }

    /**
     * Reading the hosts file into Map
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
                e.printStackTrace();
            }
        }
        return map;
    }

    /**
     * Rewriting Hosts file after update or delete
     *
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
        if(!file.canWrite())  {
            throw new Exception("File is not writable");
        }
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(builder.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Updates existing host
     *
     */
    public void update(Host newHost) throws Exception {
        rewrite();
        newHost.write();
    }

    /**
     * Deletes existing host
     *
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
