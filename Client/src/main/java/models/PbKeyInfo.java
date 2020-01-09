package models;

import com.google.gson.annotations.Expose;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aCard0s0
 */
public class PbKeyInfo implements Serializable {
    
    private final long serialVersionUID = 1;
    
    private PublicKey pbk;
    @Expose private String encPbk;
    @Expose private byte[] signature;
    @Expose private BigInteger serialCert;

    public PbKeyInfo() {
    }

    public PbKeyInfo(PublicKey pb, byte[] signature, BigInteger serialCert) {
        this.pbk = pb;
        this.serialCert = serialCert;
        this.signature = signature;
    }

    public void encodePbKey() {
        
        X509EncodedKeySpec keySpec =  new X509EncodedKeySpec( pbk.getEncoded() );
        encPbk = Base64.getEncoder().encodeToString(keySpec.getEncoded());
    }
    
    public void decodePbkey() {
        
        KeyFactory keyFactory;
        byte[] tmpKey;
        
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            tmpKey = Base64.getDecoder().decode(encPbk);
            X509EncodedKeySpec keySpec  = new X509EncodedKeySpec( tmpKey );
            pbk = keyFactory.generatePublic(keySpec);
        
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(PbKeyInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // Getters
    public PublicKey getPbK() {
        return pbk;
    }
    
    public byte[] getSignature() {
        return signature;
    }
    
    public BigInteger getSerialCert() {
        return serialCert;
    }
}
