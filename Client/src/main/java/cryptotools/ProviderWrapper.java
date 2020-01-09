/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cryptotools;

import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.User;
import sun.security.pkcs11.SunPKCS11;
import sun.security.x509.X509CertImpl;

/**
 *
 * @author aCard0s0
 */
public class ProviderWrapper {
    
    private static final String winFile = "src/main/resources/card/WinCitizenCard.cfg";
    private static final String unixFile = "src/main/resources/card/UnixCitizenCard.cfg";
    private static final String KStype = "PKCS11";
    private static final String ProviderCC = "SunPKCS11-PTeID";
    public static final String AUTHENTICATION_CERT = "CITIZEN AUTHENTICATION CERTIFICATE";
    public static final String SIGNATURE_CERT = "CITIZEN SIGNATURE CERTIFICATE";
    
    private Provider prov;
    private KeyStore kstore;
    
    public ProviderWrapper() {
        
        String os = System.getProperty("os.name");
        
        if (os.toLowerCase().contains("windows"))
            prov = new SunPKCS11(winFile);
        else
            prov = new SunPKCS11(unixFile);
        
        Security.addProvider(prov);
        
        try {
            kstore = KeyStore.getInstance(KStype, prov);
            kstore.load(null, null);
            
        } catch (IOException | NoSuchAlgorithmException | 
                CertificateException | KeyStoreException ex) {
            Logger.getLogger(ProviderWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * @return The KeyStore instance, initiate with type: "PKCS11" and provider: "SunPKCS11-PTeID"
     */
    public KeyStore getKeyStore() {
        return kstore;
    }
    
    /**
     * @return the provider instance, initiate with file WinCitizenCard.cfg or
     * UnixCitizenCard.cfg, depends of OS system
     */
    public Provider getProvider() {
        return prov;
    }
    
    /**
     * @return all cert in the smartcard, the Signature and Authentication certificate.
     */
    public Map<BigInteger, String> getCertList() {
        
        Map<BigInteger, String> lcerts = new HashMap<>(); 
        X509Certificate cert;
        
        try {
            cert = getAuthCert();
            String encoded = Base64.getEncoder().encodeToString(cert.getEncoded());
            lcerts.put(cert.getSerialNumber(), encoded);
            
            cert = getSignCert();
            String encoded2 = Base64.getEncoder().encodeToString(cert.getEncoded());
            lcerts.put(cert.getSerialNumber(), encoded2);
            
        } catch (CertificateEncodingException ex) {
            Logger.getLogger(ProviderWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return lcerts;
    }
    
    /**
     * @return the Signature certificate from smartcard if read successful,
     * or return empty x509 certificate otherwise.
     */
    public X509Certificate getSignCert() {
        
        try {
            return new X509CertImpl(
                    getCertificate(SIGNATURE_CERT).getEncoded()
            );
        } catch (CertificateException ex) {
            Logger.getLogger(ProviderWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new X509CertImpl();
    }
    
    public byte[] getEncAuthCert() {
        
        byte[] result = null;
        
        try {
            result = getCertificate(AUTHENTICATION_CERT).getEncoded();
        } catch (CertificateEncodingException ex) {
            Logger.getLogger(ProviderWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    /**
     * @return the Authentication certificate from smartcard if read successful,
     * or return empty x509 certificate otherwise.
     */
    public X509Certificate getAuthCert() {
        
        try {
            return new X509CertImpl(
                    getCertificate(AUTHENTICATION_CERT).getEncoded()
            );
        } catch (CertificateException ex) {
            Logger.getLogger(ProviderWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new X509CertImpl();
    }
    
    /**
     * @param alias
     * @return the certificate with a the alias.
     */
    public Certificate getCertificate(String alias){
        
        Certificate cert = null;
        try {
            cert = kstore.getCertificate(alias);
            
        } catch (KeyStoreException ex) {
            Logger.getLogger(ProviderWrapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return cert;
    }
    
    /**
     *  Get the info from user smart card, Authentication Cert.
     * 
     * @return 
     */
    public User getUser() {
        
        X509Certificate authCert = getAuthCert();
        String[] tmpArr = authCert.getSubjectDN().getName().split(",");
        
        String name = tmpArr[0].split("=")[1];
        String number = tmpArr[1].split("=")[1].replaceAll("[^0-9]+", "");
        String card = tmpArr[6].split("=")[1];
        String country = tmpArr[7].split("=")[1];
        
        String menu = "\nWelcome,"
                    + "\n  "+ name
                    + "\n  nº "+ number
                    + "\n  "+ card + " ("+ country +")\n";
        System.out.println(menu);
        
        return new User(Integer.parseInt(number), name, getCertList() );
    }
}
