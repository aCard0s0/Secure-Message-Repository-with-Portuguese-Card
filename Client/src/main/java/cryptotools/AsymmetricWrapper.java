/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptotools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import managers.FileManager;

/**
 *  Code to save keys in files, and some code trying to decrypt with smartcard.
 * 
 * @author aCard0s0
 */
public class AsymmetricWrapper {
    
    public static final String CERT = "CITIZEN AUTHENTICATION CERTIFICATE";
    public static final String ASYMMETRIC_ALG = "RSA";
    public static final String PV_KEY = "key.pv";
    private KeyStore kstore = null;
    
    public AsymmetricWrapper() {
    }
    
    public AsymmetricWrapper(ProviderWrapper store) {
        kstore = store.getKeyStore();
    }
    
    public KeyPair generateKeyPair() {
        
        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance(ASYMMETRIC_ALG);
            kpg.initialize(1024);
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(AsymmetricWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return kpg.genKeyPair();
    }
    
    /**
     * @param data      data to encrypt
     * @param key       private or public
     * @return      encrypted data
     */
    public byte[] encryption(byte[] data, Key key) {
        
        byte[] result = null;
        Cipher cipher;
        
        try {
            cipher = Cipher.getInstance(ASYMMETRIC_ALG);
            cipher.init(Cipher.ENCRYPT_MODE, key); 
            result = cipher.doFinal(data);
            
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | 
                InvalidKeyException | IllegalBlockSizeException | 
                BadPaddingException ex) {
            Logger.getLogger(AsymmetricWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return result;
    }
    
    public byte[] decryption(byte[] data, PrivateKey key) {
        
        byte[] result = null;
        Cipher cipher;
        
        try {
            cipher = Cipher.getInstance(ASYMMETRIC_ALG);
            cipher.init(Cipher.DECRYPT_MODE, key);   
            result = cipher.doFinal(data);
            
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | 
                InvalidKeyException | IllegalBlockSizeException | 
                BadPaddingException ex) {
            Logger.getLogger(AsymmetricWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return result;
    }
    
    
    public byte[] decryption(byte[] data) {
        
        byte[] result = null;
        Certificate cert = null;
        //PrivateKey myPK = null;
        Cipher cipher = null;
        
        try {
            //cert = kstore.getCertificate("ECRaizEstado");
            Key myPK =  kstore.getKey(CERT, null);
            System.out.println("Private key: " + myPK);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec( myPK.getEncoded() );
            
            KeyFactory keyFactory = KeyFactory.getInstance(ASYMMETRIC_ALG);
            
            PrivateKey asd = null;
            try {
                asd = keyFactory.generatePrivate(keySpec);
                
            } catch (InvalidKeySpecException ex) {
                Logger.getLogger(AsymmetricWrapper.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            cipher = Cipher.getInstance(ASYMMETRIC_ALG);
            cipher.init(Cipher.DECRYPT_MODE, myPK );
            result = cipher.doFinal(data);
            
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | 
                InvalidKeyException | NoSuchPaddingException | IllegalBlockSizeException | 
                BadPaddingException ex) {
            Logger.getLogger(AsymmetricWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return result;
    }
    
    /*  Save private key in file named "reader.key"
    *   if path doesn't exist creates
    */
    public void savePrivateK(PrivateKey privateKey, String path) {
        
        File file = new File(path);
        if (!file.exists())     file.mkdirs();
        
        // private to encoded
        PKCS8EncodedKeySpec keySpec 
                = new PKCS8EncodedKeySpec( privateKey.getEncoded() );
        
        new FileManager().writeFile(keySpec.getEncoded(), path +"\\"+ PV_KEY);
        System.out.println("Private Key stored at "+ path);
    }
    
    /*  Load private key from file "key.pv"
    *   Path is validated befoure call this function
    *   @return private key stored on disk if exist, otherwise null
    */
    public PrivateKey loadPrivateKey(String path) {   
        
        PrivateKey pv = null;
        
        File file = new File(path);
        if (!file.exists())     return pv;
        
        // Read Private Key.
        byte[] encodedPvKey = new FileManager().readFile(path +"\\"+PV_KEY);
        
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            
            PKCS8EncodedKeySpec keySpec 
                        = new PKCS8EncodedKeySpec( encodedPvKey );
            
            pv = keyFactory.generatePrivate(keySpec);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(AsymmetricWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return pv;
    }
    
    /**
     * 
     */
    public PublicKey loadPublicKey(byte[] publicKey) {
        
        PublicKey pb = null;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            
            X509EncodedKeySpec keySpec
                    = new X509EncodedKeySpec( publicKey );
            
            keyFactory.generatePublic(keySpec);
            
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(AsymmetricWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return pb;
    }
    
    /* convert Public Key to enconded / byte[]
    */
    public byte[] publicKeyToencoded(PublicKey publicKey) {
        
        X509EncodedKeySpec keySpec = 
                new X509EncodedKeySpec( publicKey.getEncoded() );
        
        return keySpec.getEncoded();
    }
}
