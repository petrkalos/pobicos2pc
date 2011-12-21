/*
 * Author: Kwstas Lekkas , kwstasl@gmail.com
 */
package org.kalos.clientmgr;

public class Network_Msg {

    public static final byte POBICOS_MSG = 0x01;
    public static final byte REGISTRY_REQ = 0x02;
    public static final byte REG_REPLY_FAIL = 0x03;
    public static final byte REG_REPLY_WELCOME = 0x04;
    public static final byte REG_REPLY_PONG = 0x05;
    public static final byte SERVER_REGISTRY_REQ = 0x06;
    public static final byte SERVER_SEARCH_REQ = 0x07;
    public static final byte SERVER_SEARCH_RES = 0x08;
    public static final byte SERVER_SEARCH_FAIL = 0x09;
    public byte msg_type;
    public byte payload_len;	// length of 256 is enough for current pobicos implementation
    // as MAX_MSG_DATA < 200
    public static final int HEADER_LEN = 2;		// {MSG_TYPE, PAYLOAD_LEN}
    public static final int LAT_OFFSET = 1;
    public static final int LON_OFFSET = 1 + 8;
    public static final int ADDR_OFFSET = 17;	// 1 + 8 + 8 bytes to addr
    public static final int CLASS_OFFSET = 0;
    public static final int PORT_OFFSET = 23;
    public static final byte CLASS_MOBILE = 0x01;
    public static final byte CLASS_SERVER = 0x02;
    public byte[] payload;

    public Network_Msg(byte msg_type, byte len, byte[] p) {
        this.msg_type = msg_type;
        payload_len = len;
        payload = p.clone();
    }
}
