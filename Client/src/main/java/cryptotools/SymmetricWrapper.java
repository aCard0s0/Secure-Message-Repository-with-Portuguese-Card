/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptotools;

import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author aCard0s0
 */
public class SymmetricWrapper {

    public static final String SYMMETRIC_ALG = "AES";
    public static final int    KEY_LENGTH = 128;
    
    private KeyStore kstore = null;
    private KeyGenerator generator;
    private SecretKey key;
    
    public SymmetricWrapper() {
    }
    
    /**
     *  Generate new key from pre-define algorithm, AES 128 bits
     */
    public SecretKey generateNewKey() {
        try {
            generator = KeyGenerator.getInstance( SYMMETRIC_ALG );
            generator.init( KEY_LENGTH );
            key = generator.generateKey();
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(SymmetricWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return key;
    }
    
    /**
     * @return the last key generated NOT the last one used,
     *  null if object was instantiate with provider. 
     * (private key does not leave SmartCard).
     */
    public SecretKey getKey() {
        return key;
    }
    
    /**
     * @param data to encrypt
     * @param key, if null it will be use the last one generated
     * @return cryptogram if success.
     */
    public byte[] encryption(byte[] data, byte[] key) {
        
        byte[] result = null;
        Cipher cipher;
        
        try {
            cipher = Cipher.getInstance(SYMMETRIC_ALG);
            cipher.init( 
                    Cipher.ENCRYPT_MODE, 
                    new SecretKeySpec(key, SYMMETRIC_ALG) 
            );
            result = cipher.doFinal(data);

        } catch (InvalidKeyException | IllegalBlockSizeException | 
                BadPaddingException | NoSuchAlgorithmException | 
                NoSuchPaddingException ex) {
            Logger.getLogger(SymmetricWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    /**
     * @param data to decrypt
     * @param key 
     * @return cryptogram if success.
     */
    public byte[] decryption(byte[] data, byte[] key) {
        
        byte[] result = null;
        Cipher cipher;
        
        try {
            cipher = Cipher.getInstance(SYMMETRIC_ALG);
            cipher.init(
                    Cipher.DECRYPT_MODE, 
                    new SecretKeySpec(key, SYMMETRIC_ALG)
            );
            result = cipher.doFinal(data);
        
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | 
                InvalidKeyException | IllegalBlockSizeException |
                BadPaddingException ex) {
            Logger.getLogger(SymmetricWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return result;
    }
}
