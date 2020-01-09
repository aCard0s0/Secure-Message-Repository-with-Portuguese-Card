package session;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import cryptotools.AsymmetricWrapper;
import cryptotools.PBKDF2;
import cryptotools.SignWrapper;
import cryptotools.SymmetricWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import models.HashKeyInfo;
import models.MessageData;
import models.PbKeyInfo;
import models.PvKeyInfo;
import models.SecData;
import models.SecSignature;
import models.User;

/**
 *
 * @author aCard0s0
 */
public class CryptoWorkFlow {

    private AsymmetricWrapper asym;
    private SymmetricWrapper sym;
    private PBKDF2 hashOps;
    private SignWrapper signOps;
    
    private PvKeyInfo userPV;
    private PbKeyInfo userPB;
    private HashKeyInfo userHash;
    private byte[] sessionKey;
    private byte[] encPrivData;
    private SecSignature signPrivData;
    
    public CryptoWorkFlow(AsymmetricWrapper asym, SymmetricWrapper sym, PBKDF2 hash, SignWrapper signOps) {
        this.asym = asym;
        this.sym = sym;
        this.hashOps = hash;
        this.signOps = signOps;
        
        userPV = null;
        userPB = null;
        userHash = null;
        sessionKey = null;
        encPrivData = null;
        signPrivData = null;
    }
    
    /**
     *  Create and Sign a RSA Key Pair
     */
    public void generateSignKeyPair() {
        
        KeyPair keyPair = asym.generateKeyPair();
        byte[] signaturePv = signOps.signWithAuthCert(keyPair.getPrivate().getEncoded());
        byte[] signaturePb = signOps.signWithAuthCert(keyPair.getPublic().getEncoded());
        BigInteger serial = signOps.getKstore().getAuthCert().getSerialNumber();
        
        userPV = new PvKeyInfo(keyPair.getPrivate(), signaturePv, serial);
        userPB = new PbKeyInfo(keyPair.getPublic(), signaturePb, serial);
    }
    
    /**
     * @param id        will be encrypt with hash generated to do login without Smartcard
     * @param pass      password needed to create hash
     */
    public void generateAndSaveHash(String id, String pass) {
        
        HashKeyInfo hashInfo = null;
        sessionKey = Base64.getDecoder().decode(hashOps.createHash(pass));
        
        byte[] encryption = sym.encryption(     // if id == enc(id) : login
                id.getBytes(), 
                sessionKey
        );
        userHash = hashOps.getHashInfo();
        userHash.setToLog(encryption);
    }
    
    /**
     *  Serialize and encrypt the secure data.
     * 
     * @param secData 
     */
    public void encryptSecData(SecData secData) {
        
        // encrypt SecData       
        encPrivData = sym.encryption(    
                User.toByteArray(secData), sessionKey
        );
    }

    public void signSecData() {
        
        byte[] signature = signOps.signWithAuthCert(encPrivData);
        BigInteger serial = signOps.getKstore().getAuthCert().getSerialNumber();
        signPrivData = new SecSignature(signature, serial);
    }
    
    public User decodeUser(JsonObject userEncData) {
        
        Type type = new TypeToken<User>() {}.getType();
        return new User().fromJson(userEncData.toString(), type);
    }
    
    // Getters
    public PbKeyInfo getUserPB() {
        return userPB;
    }

    public PvKeyInfo getUserPV() {
        return userPV;
    }

    public HashKeyInfo getUserHash() {
        return userHash;
    }

    public byte[] getEncPrivData() {
        return encPrivData;
    }

    public SecSignature getSignPrivData() {
        return signPrivData;
    }

    public PublicKey decodePbKey(byte[] encPk) {
        
        KeyFactory keyFactory;
        PublicKey pbk = null;
        
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec keySpec  = new X509EncodedKeySpec( encPk );
            pbk = keyFactory.generatePublic(keySpec);
        
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(PbKeyInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        return pbk;
    }

    /**
     * 
     * @param msgToSend
     * @param pbKey
     * @return MessageData object serialize and base64 encoded.
     */
    public String encryptMessage(String msgToSend, PublicKey pbKey) {
        return encrytFlow(msgToSend.getBytes(), pbKey);
    }

    public String encryptMessageCopy(String msgToSend, PvKeyInfo pvKeyInfo) {
        return encrytFlow(msgToSend.getBytes(), pvKeyInfo.getPv());
    }
    
    private String encrytFlow(byte[] msgToSend, Key key){
        
        SecretKey aesKey = sym.generateNewKey();
        byte[] encrypData = sym.encryption(msgToSend, aesKey.getEncoded());
        byte[] encriptKey = asym.encryption(aesKey.getEncoded(), key);
        byte[] signature = signOps.signWithAuthCert(encriptKey);
        byte[] cert = signOps.getKstore().getEncAuthCert();

        //System.out.println("Data: "+ Arrays.toString(encrypData) );
        //System.out.println("Key: "+ Arrays.toString(encriptKey) );
        
        MessageData data = new MessageData(encrypData, encriptKey, signature, cert);
        return Base64.getEncoder().encodeToString(MessageData.toByteArray(data));
    }

    public String decrypMessage(JsonElement serializeData, PvKeyInfo pvKeyInfo) {
        
        String obj = serializeData.getAsString();
        byte [] encData = Base64.getDecoder().decode( obj );
        MessageData data = (MessageData) MessageData.fromString(encData);
        
        //System.out.println("Data: "+ Arrays.toString(data.getEncriptData()) );
        //System.out.println("Key: "+ Arrays.toString(data.getEncriptKey()) );
        //signOps.verifySign(data.getEncriptKey(), data.getSignature(), data.getCert());
        byte[] decryptKey = asym.decryption(data.getEncriptKey(), pvKeyInfo.getPv());
        byte[] decryption = sym.decryption(data.getEncriptData(), decryptKey);
        return new String( decryption );
    }
    
}
