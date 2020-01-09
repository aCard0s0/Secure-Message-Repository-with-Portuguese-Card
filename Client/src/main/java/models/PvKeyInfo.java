package models;

import com.google.gson.annotations.Expose;
import cryptotools.SignWrapper;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aCard0s0
 */
public class PvKeyInfo implements Serializable {
    
    private final long serialVersionUID = 1;
    
    private PrivateKey pv;
    @Expose private String encPvK;
    @Expose private byte[] signature;
    @Expose private BigInteger serial;
    
    public PvKeyInfo() {
    }

    public PvKeyInfo(PrivateKey pb, byte[] signature, BigInteger serialCert) {
        this.pv = pb;
        this.signature = signature;
        this.serial = serialCert;
    }
    
    public void encode() {
        
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec( pv.getEncoded() );
        encPvK = Base64.getEncoder().encodeToString(keySpec.getEncoded());
    }
    
    public void decode() {
        
        byte[] tmp;
        KeyFactory keyFactory;
        
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            tmp = Base64.getDecoder().decode(encPvK);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec( tmp );
            pv = keyFactory.generatePrivate(keySpec);
            
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(PvKeyInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // Getters
    public PrivateKey getPv() {
        return pv;
    }
    public byte[] getSignature() {
        return signature;
    }
    public BigInteger getSerial() {
        return serial;
    }
}