package cryptotools;

import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.Base64;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *      Wrapper for Signature Operation adapter for Client Side
 * @author aCard0s0
 */
public class SignWrapper {
    
    final static Scanner sca = new Scanner(System.in);
    public static final String SING_ALGT = "SHA1withRSA";
    public static final String AUTHENTICATION_CERT = "CITIZEN AUTHENTICATION CERTIFICATE";
    public static final String SIGNATURE_CERT = "CITIZEN SIGNATURE CERTIFICATE";
    
    private final ProviderWrapper kstore;
    private Signature sign;
    
    public SignWrapper( ProviderWrapper store ) {
        kstore = store;
    }

    /**
     * @return the providerWrapper reference
     */
    public ProviderWrapper getKstore() {
        return kstore;
    }
    
    /**
     *  Sign the information passed by parameter data.
     *  This function use the Authentication Certificate
     * 
     * @param data to sign 
     * @return the signature
     */
    public byte[] signWithAuthCert(byte[] data) {
        return sign(data, AUTHENTICATION_CERT);
    }
    
    /**
     *  Sign the information passed by parameter data.
     *  This function use the Signature Certificate
     * 
     * @param data to sign
     * @return the signature
     */
    public byte[] signWithSignCert(byte[] data) {
        return sign(data, SIGNATURE_CERT);
    }
    
    /**
     *  Sign the information passed by parameter data.
     * 
     * @param data to sign
     * @return the signature
     */
    private byte[] sign(byte[] data, String alias) {
        
        byte[] signature = null;
        Key privateKeyCC;
        
        try {
            sign = Signature.getInstance( SING_ALGT );
            privateKeyCC = kstore.getKeyStore().getKey(alias, null);
            sign.initSign((PrivateKey) privateKeyCC);
            sign.update( data );
            signature = sign.sign();
            
        } catch (NoSuchAlgorithmException | KeyStoreException | 
                UnrecoverableKeyException | InvalidKeyException | SignatureException ex) {
            Logger.getLogger(SignWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return signature;
    }
    
    /**
     *  Verify signature with Signature Certificate from CC.
     * 
     * @param data
     * @param signature
     * @return true if the data is original, false if modified
     */
    public boolean verifySignBySignCC(byte[] data, byte[] signature) {
        return verifySign(data, signature, kstore.getSignCert());
    }
    
    /**
     *  Verify signature with Authentication Certificate from CC.
     * @param data
     * @param signature
     * @return true if the data is original, false if modified
     */
    public boolean verifySignByAuthCC(byte[] data, byte[] signature) {
        return verifySign(data, signature, kstore.getAuthCert());
    }
    
    /**
     *  Verify signature by serial certificate number. Search for the serial in
     * Map structure in ProviderWrapper
     * 
     * @param data
     * @param signature
     * @param serial
     * @return true if the data is original, false if modified
     */
    public boolean verifySignBySerial(byte[] data, byte[] signature, BigInteger serial) {
        
        String certBase64 = kstore.getCertList().get(serial);
        byte[] certDecode = Base64.getDecoder().decode(certBase64);
        
        CertificateFactory cf;
        Certificate cert = null;
        try {
            cf = CertificateFactory.getInstance("X.509");
            cert = cf.generateCertificate(new ByteArrayInputStream(certDecode));

        } catch (CertificateException ex) {
            Logger.getLogger(SignWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return verifySign(data, signature, cert); 
    }
    
    /**
     * @param data after the cipher
     * @param signature result from the cipher
     * @param cert
     * @return true if the data is original, false if modified
     */
    public boolean verifySign(byte[] data, byte[] signature, Certificate cert) {
        
        boolean success = true;
        Signature verSig = null;
        
        try {
            PublicKey certPublicKey = cert.getPublicKey();
            verSig.initVerify(certPublicKey);
            verSig.update(data);
            
            if (verSig.verify(signature)) {
                System.out.println("Signature verified successfully");
                success = true;
                
            } else {
                System.err.println("Signature failed, information may have changed.");
                String rsp;
                do {
                    System.out.println("Do you wish to continue? (y/n)");
                    rsp = sca.nextLine();
                    if (rsp.equals("n") || rsp.equals("no") )
                        success = false;
                }while(!"y".equals(rsp) && !"yes".equals(rsp)
                        && !"n".equals(rsp) && !"no".equals(rsp));
            }
        } catch ( InvalidKeyException | SignatureException ex) {
            Logger.getLogger(SignWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return success;
    }

    public boolean verifySign(byte[] data, byte[] signature, byte[] encCert){
        
        CertificateFactory cf;
        Certificate cert = null;
        try {
            cf = CertificateFactory.getInstance("X.509");
            cert = cf.generateCertificate(new ByteArrayInputStream(encCert));

        } catch (CertificateException ex) {
            Logger.getLogger(SignWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return verifySign(data, signature, cert);
    }
}