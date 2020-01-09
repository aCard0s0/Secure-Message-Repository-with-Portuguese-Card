/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aCard0s0
 */
public class MessageData implements Serializable {
    
    private final long serialVersionUID = 1;
    
    private byte[] encriptData;
    private byte[] encriptKey;
    private byte[] signature;
    private byte[] cert;
    
    public MessageData() {
    }

    public MessageData(byte[] encriptData, byte[] encriptKey, byte[] signature, byte[] cert) {
        this.encriptData = encriptData;
        this.encriptKey = encriptKey;
        this.signature = signature;
        this.cert = cert;
    }

    /** Read the object from Base64 string. */
    public static Object fromString( byte[] data ) {
        
        
        ObjectInputStream ois;
        Object o = null;
        
        try {
            ois = new ObjectInputStream( new ByteArrayInputStream(  data ) );
            o  = ois.readObject();
            ois.close();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return o;
   }

    /** Write the object to a Base64 string. */
    public static byte[] toByteArray( Serializable o ) {
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream( baos );
            oos.writeObject( o );
            oos.close();
        } catch (IOException ex) {
            Logger.getLogger(User.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return baos.toByteArray(); 
    }

    public byte[] getEncriptData() {
        return encriptData;
    }

    public byte[] getEncriptKey() {
        return encriptKey;
    }

    public byte[] getSignature() {
        return signature;
    }

    public byte[] getCert() {
        return cert;
    }
    
}
