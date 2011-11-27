package org.lekkas.poclient.AppLoader;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import org.lekkas.poclient.PoAPI.PoAPI;

import java.util.ArrayList;
import java.util.Scanner;

public class PoAppPillLoader implements PacketListener, PoAppPillLoaderI {

    /*
     * Reminder: bytes are stored Little-Endian
     */
    static int[] androidAppBundle;
    static PoUART con;
    private static final String TAG = "PoAppPillLoader";
    private static int offset = 0;
    private static final PoAppPillLoader INSTANCE = new PoAppPillLoader();
    private static byte[] rootAgentID;
    private static STATE APP_STATE;
    int bundleSize;
    int dataSent;
    int completeMarksPrinted;

    public static PoAppPillLoader getInstance() {
        return INSTANCE;
    }

    private PoAppPillLoader() {
        APP_STATE = STATE.IDLE;
        rootAgentID = new byte[4];
    }

    public void loadApp(String path) {
        ArrayList<String> app = new ArrayList();
        String str;
        int i = 0;

        try {
            FileInputStream fis = new FileInputStream(path);
            Scanner sc = new Scanner(fis);
            sc.useDelimiter(",");

            while (sc.hasNext()) {
                str = sc.next();
                str = str.replace("  ", "");
                str = str.replace(" ", "");
                str = str.replace("\n", "");
                str = str.replace("0x", "");
                if (!str.isEmpty()) {
                    app.add(str);
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        androidAppBundle = new int[app.size() - 1];
        for (i = 0; i < app.size() - 1; i++) {
            androidAppBundle[i] = Integer.parseInt(app.get(i), 16);
            //System.out.println(i+"--"+androidAppBundle[i]);
        }
        
        this.loadAndStartApp();
        
    }

    public boolean isAppRunning() {
        return (APP_STATE == STATE.RUNNING);
    }

    public void loadAndStartApp() {
        if (APP_STATE == STATE.RUNNING) {
            return;
        }

        con = new PoUART(this);
        retrieveBundleSize();

        sendFirstPacket();
    }

    public void startApp() {
        //con = new PoUART(this);
        sendAppStartPacket();
        APP_STATE = STATE.RUNNING;
    }

    public void killRunningApp() {
        if (APP_STATE == STATE.IDLE) {
            return;
        }
        con = new PoUART(this);
        byte[] buf = new byte[5];
        buf[0] = (byte) 0x09;

        for (int i = 1; i <= 4; i++) {
            buf[i] = rootAgentID[i - 1];
        }
        sendKillPacket(buf);
    }

    /**
     * Function retrieving application bundle size from file and
     * converting it to the int value.
     *     
     */
    private void retrieveBundleSize() {
        byte bundleSizeArray[] = new byte[4];
        bundleSizeArray[0] = (byte) (androidAppBundle[0] & 0xFF);
        bundleSizeArray[1] = (byte) (androidAppBundle[1] & 0xFF);
        bundleSizeArray[2] = (byte) (androidAppBundle[2] & 0xFF);
        bundleSizeArray[3] = (byte) (androidAppBundle[3] & 0xFF);

        bundleSize = byteArrayToInt(bundleSizeArray, 0);
        System.out.println(TAG + "Application bundle size: " + bundleSize);
    }

    /**
     * Function converting array of bytes into integer value.
     *
     * NOTE: Little endian assumed here.
     *
     * @param b array of bytes to convert
     * @param offset where to start converting
     *
     * @return int value
     */
    private int byteArrayToInt(byte[] b, int offset) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (i) * 8;
            value += (b[i + offset] & 0x000000FF) << shift;
        }
        return value;
    }

    private void sendAppStartPacket() {
        byte payload[] = new byte[1];
        payload[0] = (byte) 0x06; // APP_PILL_CONTROL_START	

        con.sendPacket((byte) 0x01, payload);
    }

    /**
     * Function sending first packet to the node so 
     * it can initialize application bundle upload.
     *     
     */
    private void sendFirstPacket() {
        byte payload[] = {(byte) 0x01}; // APP_PILL_CONTROL_START_UPLOAD
        con.sendPacket((byte) 0x01, payload);
    }

    /**
     * Function sending data packet with the specified byte
     * payload. .
     *   
     * @param payload array of bytes to send to the node
     */
    private void sendDataPacket(byte[] payload) {
        con.sendPacket((byte) 0x02, payload);
    }

    /**
     * Function called when new UART packet is received from the node.
     *   
     * If node is ready for new data packet, send it. 
     * If node notifies upload error, show message and exit the application.
     * If node notifies upload finish, show message and exit the application.
     *
     * @param channel byte representing channel
     * @param payload payload of the received message
     */
    public void packetReceived(byte channel, byte[] payload) {
        //System.out.println("Packet received on channel " + channel + "\n" + new String(payload));

        if (channel == 0x01 && payload.length == 1) // control packet
        {
            switch (payload[0]) {
                case 0x02:  // node finished uploading
                    System.out.println(TAG + "\nDone!");
                    offset = 0;
                    this.startApp();
                    //con.cancel();
                    break;
                case 0x03:  // node ready for data
                    int maxlen = con.getMaxPayloadLen();
                    byte data[] = new byte[maxlen];
                    int i;
                    for (i = 0; ((i < maxlen) && ((i + offset) < bundleSize)); i++) {
                        data[i] = (byte) (androidAppBundle[i + offset] & 0xFF);
                    }
                    offset += i;
                    System.out.println(TAG + "Sending: " + byteArrayToAscii(data));
                    this.sendDataPacket(data);
                    break;
                case 0x04: // upload error
                    System.out.println(TAG + "\nUpload error notified by PoAppPillM!");
                    break;
                case 0x07: // application started
                    System.out.println(TAG + "\nApplication root agent started!");
                    con.cancel();
                    break;
                case 0x08: // application starting error
                    System.err.println(TAG + "\nError when trying to start application root agent!");
                    System.gc();
                    System.exit(0);
                    break;
                case 0x0B:
                    System.err.println(TAG + "\nError when trying to KILL application root agent!");
                    break;
            }
        } else if (channel == 0x01 && payload.length == 5) {
            if (payload[0] == 0x07) {
                PoAPI.setHostsRootAgent(true);
                System.out.println(TAG + "\nApplication root agent started!(L==5)");
                for (int i = 0; i < 4; i++) {
                    rootAgentID[i] = payload[1 + i];
                }
                con.cancel();
            }
        }

        if (payload[0] == 0x0A) {
            PoAPI.setHostsRootAgent(false);
            System.out.println(TAG + "\nApplication root agent KILLED SUCCESSFULLY!!");
            con.cancel();
        }
    }

    /*
     * sendKillPacket()
     * kills current application.
     */
    private void sendKillPacket(byte[] payload) {
        if (APP_STATE == STATE.RUNNING) {
            PoAPI.setHostsRootAgent(false);
            con.sendPacket((byte) 0x01, payload);
            APP_STATE = STATE.IDLE;
            System.out.println("Application Killed!");

        }
    }

    private String byteArrayToAscii(byte[] b) {
        String result = "";
        if (b == null) {
            return result;
        }

        for (int i = 0; i < b.length; i++) {
            result +=
                    Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    public static String getConfigSetting(String setting) {
        byte[] settingBytes = setting.getBytes();
        boolean found = false;
        int i;
        StringBuilder s = new StringBuilder();

        for (i = 0; i < androidAppBundle.length - setting.length() - 1; i++) {
            found = true;
            for (int j = 0; j < setting.length(); j++) {
                if (!(androidAppBundle[i + j] == settingBytes[j])) {
                    found = false;
                    break;
                }
            }
            if (found) {
                break;
            }
        }
        if (found) { // i now ponts to the first ascii character of our setting string.
            i += (setting.length() + 1);	// i now points to the start of the setting value

            char ch = (char) androidAppBundle[i];

            while (ch != 0x00) {
                s.append(ch);
                ch = (char) androidAppBundle[++i];
            }
        }
        System.out.println(TAG + "Extracted value: " + setting + " = " + s.toString());
        return s.toString();
    }

    enum STATE {

        RUNNING,
        IDLE
    };
}
