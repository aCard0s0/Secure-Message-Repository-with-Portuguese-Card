/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package models;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  REF: https://stackoverflow.com/questions/134492/how-to-serialize-an-object-into-a-string
 * 
 * @author acard0s0
 */
public class User {
    
    private final Gson gson;
    @Expose private int id;                             // server internal id
    @Expose private int uuid;                           // CC number
    @Expose private String name;                        // user name
    @Expose private Map<BigInteger, String> certList;   // certificates used by the user on CC
    private SecData privData;                           // RSA privKey, Diffie-Hellman, ...
    @Expose private String encPrivData;
    @Expose private SecSignature signPrivData;          // Signature related info for SecData
    @Expose private PbKeyInfo pbInfo;                   // Info to reveive messages
    @Expose private HashKeyInfo hashInfo;                // To encrypt SecData and login
    
    public User() {
        this.gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    }
    
    public User(int uuid, String name, Map<BigInteger, String> map){
        this.gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        this.name = name;
        this.uuid = uuid;
        this.certList = map;
        this.privData = null;
        this.encPrivData = null;
        this.signPrivData = null;
        this.pbInfo = null;
        this.hashInfo = null;
    }
    
    // GSON Operations
    
    /**
     *      Convert this instance in Base64 Strings to send by JSON format
     * (Attention to the SecData process)
     * @return 
     */
    public JsonElement toJson() {
        
        Type type = new TypeToken<User>() {}.getType();
        
        if(privData != null)
            privData.getPvKeyInfo().encode();                   // encode RSA private key
        
        encPrivData = Base64.getEncoder().encodeToString(   // serialize private Data
                toByteArray(privData)
        );
        
        if(pbInfo != null)
            pbInfo.encodePbKey();                               // encode RSA public key
        
        return gson.toJsonTree(this, type);
    }
    
    /**
     *      Creates a User from the information provide by the server.
     *   (Attention to the SecData process)
     * @param src
     * @param objClass
     * @return User
     */
    public User fromJson(String src, Type objClass) {
        
        User u = gson.fromJson(src, objClass);
        // ask for pin to access privdata
        SecData tmp = (SecData) User.fromString(u.getEncPrivData());  // deserialize private data 
        u.setPrivData( tmp );
        u.getPrivData().getPvKeyInfo().decode();        // decode private key
        u.getPbInfo().decodePbkey();                    // decode publickey
        return u;
    }
    
    /** Read the object from Base64 string. */
    public static Object fromString( String s ) {
        
        byte [] data = Base64.getDecoder().decode( s );
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
    
    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setConfirPrivData(SecSignature signPrivData) {
        this.signPrivData = signPrivData;
    }

    public void setPbInfo(PbKeyInfo pbInfo) {
        this.pbInfo = pbInfo;
    }

    public void setHashKeyInfo(HashKeyInfo info) {
        this.hashInfo = info;
    }
    
    public void setPrivData(SecData secData) {
        this.privData = secData;
    }
    
    public void setEncPrivData(byte[] encPrivData) {
        this.encPrivData = Base64.getEncoder().encodeToString(encPrivData);
    }

    public void setSignPrivData(SecSignature signPrivData) {
        this.signPrivData = signPrivData;
    }
    
    
    // Getters

    public int getId() {
        return id;
    }

    public int getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }
    
    public Map<BigInteger, String> getCertList() {
        return certList;
    }

    public String getEncPrivData() {
        return encPrivData;
    }
    
    public PbKeyInfo getPbInfo() {
        return pbInfo;
    }
    
    public SecData getPrivData() {
        return privData;
    }
    /*
    /**
     * @return the encoded string in base64 of the Signature Certification
     *
    public String getSignCert() {
        try {
            return Base64.getEncoder().encodeToString(signCert.getEncoded());
        } catch (CertificateEncodingException ex) {
            System.err.println("Erro: encoder cert");
        }
        return "";
    }

    /**
     * @return the encoded string in base64 of the Authentication Certification
     *
    public String getAuthCert() {
        try {
            return Base64.getEncoder().encodeToString(authCert.getEncoded());
        } catch (CertificateEncodingException ex) {
            System.err.println("Erro: encoder cert");
        }
        return "";
    }*/
    
}