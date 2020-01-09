package main;

import session.OptionsController;

/**
 *      Security Project - Client Side
 * 
 *  Author @aCard0s0, André Cardoso, Mec: 65069, email: marquescardoso@ua.pt
 */
public class Main 
{
    public static void main( String[] args )
    {
        OptionsController app = new OptionsController();
        app.start();
    }
}

/*PBKDF2 asd = new PBKDF2();
        String createHash = asd.createHash("asd");
        
        SymmetricWrapper sym = new SymmetricWrapper();
        
        sym.generateNewKey();
        SecretKey key = sym.getKey();
        System.out.println(key.getEncoded().length);
        
        byte[] encryption = sym.encryption("test".getBytes(), Base64.getDecoder().decode(createHash));
        
        byte[] decryption = sym.decryption(encryption, Base64.getDecoder().decode(createHash));
        
        String qwe = new String(decryption);
        System.out.println(qwe);*/