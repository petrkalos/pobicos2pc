/*
 * Author: Kwstas Lekkas , kwstasl@gmail.com
 */
package org.lekkas.PoDirectory;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NodeInfo {

    private char PoNodeAddr;
    private double Latitude;
    private double Longitude;
    private String server_ip;
    private int server_port;
    private int seed;
    private SocketChannel Sock;
    private long last_seen;
    private byte dev_class;
    private static short server_count;

    public NodeInfo(SocketChannel s) {
        Sock = s;
        PoNodeAddr = Reactor.registry.getAddr();
        last_seen = System.currentTimeMillis();
        Random r = new Random(System.currentTimeMillis());
        seed = r.nextInt();
        System.out.println("Created node with addr: " + (int) PoNodeAddr + " and seed: " + (int) seed);
    }

    public NodeInfo(SocketChannel s, double lat, double lon, byte dev_class) {
        Sock = s;
        last_seen = System.currentTimeMillis();
        PoNodeAddr = Reactor.registry.getAddr();
        Random r = new Random(System.currentTimeMillis());
        seed = r.nextInt();
        if (dev_class == Network_Msg.CLASS_MOBILE) {
            System.out.println("Created mobile node with addr: " + (int) PoNodeAddr
                    + ", seed: " + (int) seed + " lat: " + lat
                    + ", lon: " + lon);
        } else if (dev_class == Network_Msg.CLASS_SERVER) {
            System.out.println("Created server node with addr: " + (int) PoNodeAddr
                    + ", seed: " + (int) seed);
            server_count++;
        }
        this.dev_class = dev_class;
        Latitude = lat;
        Longitude = lon;
    }

    public NodeInfo(SocketChannel s, int server_port, byte dev_class) {
        this.Sock = s;
        this.last_seen = System.currentTimeMillis();
        PoNodeAddr = Reactor.registry.getAddr();
        Random r = new Random(System.currentTimeMillis());
        this.seed = r.nextInt();
        this.dev_class = dev_class;
        try {
            this.server_ip = s.getRemoteAddress().toString();
        } catch (IOException ex) {
            Logger.getLogger(NodeInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.server_port = server_port;

        System.out.println("Created server node with addr: " + (int) PoNodeAddr
                + ", seed: " + (int) seed);
        System.out.println("Port: " + server_port + ", IP: " + server_ip);
        server_count++;



        this.Latitude = -1;
        this.Longitude = -1;
    }

    public String getServerIP() {
        return this.server_ip;
    }

    public int getServerPort() {
        return this.server_port;
    }

    public static short getServerCounter() {
        return server_count;
    }

    public void setSock(SocketChannel s) {
        Sock = s;
    }

    public SocketChannel getSocketChannel() {
        return Sock;
    }

    public void setLatitude(double lat) {
        Latitude = lat;
    }

    public double getLatitude() {
        return Latitude;
    }

    public void setLongitude(double lon) {
        Longitude = lon;
    }

    public double getLongitude() {
        return Longitude;
    }

    public void setPoNodeAddr(char addr) {
        PoNodeAddr = addr;
    }

    public char getPoNodeAddr() {
        return PoNodeAddr;
    }

    public int getSeed() {
        return seed;
    }

    public long getLastSeen() {
        return last_seen;
    }

    public void setLastSeen(long d) {
        last_seen = d;
    }

    public boolean isServer() {
        return this.dev_class == Network_Msg.CLASS_SERVER;
    }
}
