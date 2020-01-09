package server;

import java.io.File;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.gson.*;
import description.UserDescription;
import java.io.ByteArrayInputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import static utilities.Configurations.MBOXES_PATH;
import static utilities.Configurations.RECEIPTS_PATH;
import static utilities.Configurations.DESC_FILE_NAME;
import static main.Main.LOGGER;
import sun.security.x509.X509CertImpl;

/**
 *  Class provider by André Zúquete and João Barraca, professors of UA in the class.
 * 
 * Modified by:
 *      @author aCard0s0
 *      @author nC0sta
 * 
 *  Note: Esta class contem um file manager/mensages, e um User manager...
 */
public class ServerControl {
    
    ConcurrentSkipListSet<UserDescription> users = null;
    FileManager fmang;
    
    public ServerControl() {
        
        fmang = new FileManager();
        users = fmang.getUsersOnServer();
    }

    boolean messageWasRed ( int id, String message ) {
        if (message.charAt( 0 ) == '_') {
            return (new File( userMessageBox( id ) + "/" + message )).exists();
        } else {
            return (new File( userMessageBox( id ) + "/_" + message )).exists();
        }
    }

    boolean messageExists ( int id, String message ) {
        return (new File( userMessageBox( id ) + "/" + message )).exists();
    }

    boolean copyExists ( int id, String message ) {
        return (new File( userReceiptBox( id ) + "/" + message )).exists();
    }

    synchronized boolean userExists ( int id ) {
        return users.contains( new UserDescription( id ) );
    }

    synchronized boolean userExists ( String uuid ) {

        for (UserDescription u: users) {
            if (u.uuid.equals( uuid )) {
                return true;
            }
        }

        return false;
    }

    synchronized JsonElement getUserById ( int id ) {

        for (UserDescription u: users) {
            if (u.id == id ) {
                return u.description;
            }
        }

        return null;
    }

    /**
     *  Search in the internal list of user from the one with the given uuid.
     * @param uuid
     * @return 
     */
    synchronized UserDescription getUserByUuid ( String uuid ) {

        for (UserDescription u: users) {
            if (u.description.getAsJsonObject().get("uuid").getAsString().equals( uuid )) {
                return u;
            }
        }

        return null;
    }
    
    synchronized UserDescription addUser ( JsonElement description ) {

        int id;
        String path = null;

        // Find a free user id

        for (id = 1; userExists( id ); id++) {}

        //LOGGER.info( "Add user \"" + id + "\": " + description );

        // Add it to the users' internal list

        UserDescription user = new UserDescription( id, description );
        users.add( user );

        // Create message box, recepit box and save description

        try {
            (new File( userMessageBox( id ) )).mkdir();
            (new File( userReceiptBox( id ) )).mkdir();
        } catch (Exception e) {
            LOGGER.error( "Cannot create directory " + path + ": " + e );
            System.exit ( 1 );
        }

        try {
            path = MBOXES_PATH + "/" + Integer.toString( id ) + "/" + DESC_FILE_NAME;
            fmang.saveOnFile( path, description.toString() );
        } catch (Exception e ) {
            LOGGER.error( "Cannot create description file " + path + ": " + e );
            System.exit ( 1 );
        }

        return user;
    }
    
    synchronized UserDescription updateUser ( JsonObject description ) {
        
        int id = description.get( "id" ).getAsInt();      // TODO: fix that
        String path = null;
        
        LOGGER.info( "Update user \"" + id + "\": " + description );
        
        for(UserDescription u : users) {
            if(u.id == id){
                users.remove(u);
                break;
            }
        }
        
        UserDescription user = new UserDescription( id, description );
        users.add(user);
        
        try {
            path = MBOXES_PATH + "/" + Integer.toString( id ) + "/" + DESC_FILE_NAME;
            fmang.saveOnFile( path, description.toString() );
        } catch (Exception e ) {
            LOGGER.error( "Cannot create description file " + path + ": " + e );
            System.exit ( 1 );
        }
        return user;
    }

    synchronized String listUsers ( int id ) {

        if (id == 0) {
            LOGGER.info( "Looking for all connected users" );
        } else {
            LOGGER.info( "Looking for \"" + id + "\"" );
        }

        if (id != 0) {
            JsonElement user = getUserById( id );
            if (user != null) {
                return "[" + user + "]";
            }
            return null;
        } else {
            String list = null;
            for (UserDescription u: users) {
                JsonObject user = u.description.getAsJsonObject();
                String info = "{ \"id\":\""+ user.get("id")     +"\", "
                            + "\"uuid\":"+ user.get("uuid")   +", "
                            + "\"name\":"+ user.get("name")     +"}";
                if (list == null) {
                    
                    list = "[" + info;
                } else {
                    list += "," + info;
                }
            }

            if (list == null) {
                list = "[]";
            } else {
                list += "]";
            }
            
            return list;
        }
    }

    String userAllMessages ( int id ) {
        return "[" + userMessages( userMessageBox( id ), "_?+[0-9]+_[0-9]+" ) + "]";
    }

    String userNewMessages ( int id ) {
        return "[" + userMessages( userMessageBox( id ), "[0-9]+_[0-9]+" ) + "]";
    }

    String userSentMessages ( int id ) {
        return "[" + userMessages( userReceiptBox( id ), "[0-9]+_[0-9]+" ) + "]";
    }

    private String userMessages ( String path, String pattern ) {

        File mbox = new File( path );
        Pattern msgPattern = Pattern.compile( pattern );
        String result = "";

        LOGGER.debug( "Look for files at " + path + " with pattern " + pattern );

        try {
            for (File file: mbox.listFiles()) {
                //LOGGER.debug( "\tFound file " + file.getName() );
                Matcher m = msgPattern.matcher( file.getName() );
                if (m.matches()) {
                    if (result.length() > 0) {
                        result += ',';
                    }
                    result += "\"" + file.getName() + "\"";
                }
            }
        } catch (Exception e) {
            LOGGER.error( "Error while listing messages in directory " + mbox.getName() + ": " + e );
        }

        return result;
    }

    String sendMessage ( int src, int dst, String msg, String receipt ) {

        int nr = 0;
        String result;
        String path = null;

        try {
            path = userMessageBox( dst ) + "/";
            nr = fmang.newFile( path, src + "_" );
            fmang.saveOnFile ( path + src + "_" + nr, msg );

            result = "[\"" + src + "_" + nr + "\"";

            path = userReceiptBox( src ) + "/" + dst + "_";
            fmang.saveOnFile ( path + nr, receipt );
        } catch (Exception e) {
            LOGGER.error( "Cannot create message or copy file " + path + nr + ": " + e );
            return "[\"\",\"\"]";
        }

        return result + ",\"" + dst + "_" + nr + "\"]";
    }

    String readMsgFile ( int id, String msg ) throws Exception {

        String path = userMessageBox( id ) + "/";

        if (msg.charAt( 0 ) == '_') { // Already red
            path += msg;
        } else {
            File f = new File( path + "_" + msg );
            if (f.exists()) {         // Already red  
                path += "_" + msg;
            }
            else { // Rename before reading
                try {
                    f = new File( path + msg );
                    path += "_" + msg;
                    f.renameTo ( new File ( path ) );
                } catch (Exception e) {
                    LOGGER.error( "Cannot rename message file to " + path + ": " + e );
                    path += msg; // Fall back to the non-renamed file
                }
            }
        }

        return fmang.readFromFile ( path );
    }

    String recvMessage ( int id, String msg ) {

        String result = "[";

        // Extract message sender id

        Pattern p = Pattern.compile( "_?+([0-9]+)_[0-9]+" );
        Matcher m = p.matcher( msg );

        if (m.matches() == false) {
            LOGGER.error( "Internal error, wrong message file name (" + msg + ") format!" );
            System.exit ( 2 );
        }

        result += m.group( 1 ) + ",";

        // Read message

        try {
            result += "\"" + readMsgFile( id, msg ) + "\"";
        } catch(Exception e) {
            LOGGER.error( "Cannot read message " + msg + " from user " + id + ": " + e );
            result += "\"\"";
        }

        return result + "]";
    }

    String userMessageBox ( int id ) {
        return MBOXES_PATH + "/" + Integer.toString( id );
    }

    String userReceiptBox ( int id ) {
        return RECEIPTS_PATH + "/" + Integer.toString( id );
    }

    void storeReceipt ( int id, String msg, String receipt ) {

        Pattern p = Pattern.compile( "_?+([0-9]+)_([0-9])" );
        Matcher m = p.matcher( msg );

        if (m.matches() == false) {
            LOGGER.error( "Internal error, wrong message file name (" + msg + ") format!" );
            System.exit ( 2 );
        }

        String path = userReceiptBox( Integer.parseInt( m.group( 1 ) ) ) + "/_" + id + "_" + m.group( 2 ) + "_" + System.currentTimeMillis();

        try {
            fmang.saveOnFile ( path, receipt );
        } catch (Exception e) {
            LOGGER.error( "Cannot create receipt file " + path + ": " + e );
        }

    }

    String getReceipts ( int id, String msg ) {

        Pattern p = Pattern.compile( "_(([0-9])+_[0-9])_([0-9]+)" );
        File dir = new File( userReceiptBox( id ) );
        String result;
        String receipt;
        String copy;
        int receipts = 0;

        try {
            copy = fmang.readFromFile( userReceiptBox( id ) + "/" + msg );
        } catch(Exception e) {
            LOGGER.error( "Cannot read a copy file: " + e );
            copy = "";
        }

        result = "{\"msg\":\"" + copy + "\",\"receipts\":[";

        for (File f: dir.listFiles()) {
            Matcher m = p.matcher ( f.getName() );
            if (m.matches() && m.group( 1 ).equals( msg )) {
                if (receipts != 0) {
                    result += ",";
                }

                try {
                    receipt = fmang.readFromFile( userReceiptBox( id ) + "/" + f.getName() );
                } catch(Exception e) {
                    LOGGER.error( "Cannot read a receipt file: " + e );
                    receipt = "";
                }

                result += "{\"date\":" + m.group( 3 ) + ",\"id\":" + m.group( 2 ) + ",";
                result += "\"receipt\":\"" + receipt  + "\"}";
                receipts++;
            }
        }

        return result + "]}";
    }

    /**
     *  This method will verify if the user with uuid has a certificate with given serial 
     * @param asString
     * @param serial
     * @return 
     */
    public boolean certBelongsUuid(String uuid, String serial) {
        
        JsonObject user = getUserByUuid(uuid).getDescription();
        JsonObject certPEM = user.get("certList").getAsJsonObject();    // auth used for login
        byte[] certDec = Base64.getDecoder().decode( certPEM.get(serial).getAsString() );
        
        CertificateFactory cf = null;
        Certificate cert = null;
        X509Certificate x509cert = null;
        
        try {
            cf = CertificateFactory.getInstance("X.509");
            cert = cf.generateCertificate(new ByteArrayInputStream(certDec));
            x509cert = new X509CertImpl(cert.getEncoded());

        } catch (CertificateException ex) {
            System.err.println("Erro converting certificate");
        }
        return x509cert.getSerialNumber().toString().equals(serial);
    }
    
    /**
     *      TODO: Map<serial, certificate> implementation.
     *      
     *  This method will give the certificate with serial given of user uuid.
     *      
     * @param asString
     * @param serial
     * @return certificate if exists, null otherwise
     */
    public X509Certificate getCertificateBySerial(String uuid, String serial) {
        
        JsonObject user = getUserByUuid(uuid).getDescription();
        JsonObject certPEM = user.get("certList").getAsJsonObject();    // auth used for login
        byte[] certDec = Base64.getDecoder().decode( certPEM.get(serial).getAsString() );
        
        CertificateFactory cf = null;
        Certificate cert = null;
        X509Certificate x509cert = null;
        
        try {
            cf = CertificateFactory.getInstance("X.509");
            cert = cf.generateCertificate(new ByteArrayInputStream(certDec));
            x509cert = new X509CertImpl(cert.getEncoded());

        } catch (CertificateException ex) {
            System.err.println("Erro converting certificate");
        }
        return x509cert;
    }
    
    /**
     *      This metod verify the signature from challenge send to login.
     * 
     * @param challenge
     * @param signature
     * @param cert
     * @return 
     */
    boolean verifySignature(byte[] challenge, byte[] signature, Certificate cert) {
        
        Signature verSig = null;
        
        try {
            verSig = Signature.getInstance("SHA1withRSA");
            PublicKey certPublicKey = cert.getPublicKey();
            verSig.initVerify(certPublicKey);
            verSig.update(challenge);
            if (verSig.verify(signature)) {
                return true;
            }
            
        } catch (NoSuchAlgorithmException ex) {
        } catch (InvalidKeyException ex) {
            Logger.getLogger(ServerControl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SignatureException ex) {
            Logger.getLogger(ServerControl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return false;
    }

}