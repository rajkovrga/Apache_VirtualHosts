package com.virtualhosts;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Hosts {
    private InetAddress address;
    private String serverName;
    private File file;
    public Hosts(InetAddress address, String serverName) {
        this.address = address;
        this.serverName = serverName;
        this.file = new File(Config.HOSTS);
    }

    /**
     * Checks if the host exits
     * @return returns whether the host exits or not
     * @throws IOException if the file is not found
     */
    public boolean hostExits() throws IOException {
        return read().containsKey(this.serverName) && read().containsValue(this.address);
    }


    /**
     * Reading the hosts file into Map
     * @return Map
     * @throws FileNotFoundException throws this if the hosts file doesn't exit
     */
    public Map<String,InetAddress> read() throws FileNotFoundException {
        var map = new HashMap<String, InetAddress>();
        Scanner scanner = new Scanner(this.file);
        Pattern regex = Pattern.compile(
                "\\n?(\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\\.\\d{0,3})(\\t | \\s+)(.*+)\\n?"
        );
        Matcher matcher;
        byte[] bytes = new byte[4];
        while(scanner.hasNextLine()) {
            matcher = regex.matcher(scanner.nextLine());
            String[] ip = matcher.group(1).split("\\.");
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
     */
    public void write() {
        try {
            if(hostExits()){
                System.out.println("Host already exists");
                return;
            }
            FileWriter writer = new FileWriter(this.file);
            writer.append("\n")
                    .append(this.address.toString())
                    .append("\t")
                    .append(this.serverName);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
