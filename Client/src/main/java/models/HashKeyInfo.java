/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import com.google.gson.annotations.Expose;
import java.io.Serializable;
import java.util.Base64;

/**
 *
 * @author aCard0s0
 */
public class HashKeyInfo implements Serializable {
    
    private final long serialVersionUID = 1;
    
    @Expose private String salt;
    @Expose private int iterations;
    @Expose private byte[] toLog;

    public HashKeyInfo(String salt, int iterations) {
        this.salt = salt;
        this.iterations = iterations;
        this.toLog = null;
    }
    
    public byte[] getSalt() {
        return Base64.getDecoder().decode(salt);
    }
    
    /**
     *  This data is to login without smartcard.
     * 
     * @param data 
     */
    public void setToLog(byte[] data) {
        this.toLog = data;
    }
}
