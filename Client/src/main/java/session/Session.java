package session;

import managers.MessageManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cryptotools.AsymmetricWrapper;
import cryptotools.PBKDF2;
import cryptotools.SignWrapper;
import java.util.Base64;
import java.util.Scanner;
import models.User;
import managers.CardManager;
import cryptotools.ProviderWrapper;
import cryptotools.SymmetricWrapper;
import java.security.PublicKey;
import models.SecData;

/**
 *      Ideia, separar em communOperations e authOperations
 * 
 * @author aCard0s0
 */
public class Session {
    
    private final Scanner sca;
    private final CardManager cardManager;
    private final ClientConnection conn;
    private final MessageManager msgs;
    private User me;                            // the user logged
    private boolean login;                      // indicate if user successful login
    
    private final ProviderWrapper provStore;
    private final AsymmetricWrapper asym;
    private final SymmetricWrapper sym;
    private final PBKDF2 hashOps;
    private final SignWrapper signOps;          // to do the signature operations
    private final CryptoWorkFlow tools;
    
    public Session(Scanner sca, CardManager cardManager, ClientConnection conn, MessageManager msgs) {
        
        this.sca = sca;
        this.cardManager = cardManager;             
        this.conn = conn;
        this.msgs = msgs;
        provStore = new ProviderWrapper();                  // only one instanciation
        asym = new AsymmetricWrapper();
        sym = new SymmetricWrapper();
        hashOps = new PBKDF2();
        signOps = new SignWrapper(provStore);
        tools = new CryptoWorkFlow(asym, sym, hashOps, signOps);
    }
   
    /**
     *  Create account and login with SmartCard. Assumes that SmartCard is insert.
     */
    public void scCreateAcc() {
        
        JsonObject reply;                       // reply message
        JsonElement elem;                       // elem inside reply message
        
        cardManager.waitForCard();
        if (!cardManager.isCardPresent())       // already print err message
            return ;
        
        // get User info from smartcard
        User newUser = provStore.getUser();
        
        // send message and get reply
        reply = conn.sendAndReply( msgs.create(newUser) );
        
        if( reply.has("error") ){          // error message instead.
            elem = reply.get("error");
            System.err.println("Error Creating Pre Account: "+ elem.getAsString());
            
        } else if( reply.has("result") ){
            
            elem = reply.get("result");
            newUser.setId( elem.getAsInt() );
            System.out.println(
                    "Create Pre Account Successful.\n"
                    + "Internal Server ID: "+ newUser.getId() +"\n"
                    + "Universal ID: "+ newUser.getUuid());
            sca.nextLine();
            System.out.print("Password: ");
            String pass = sca.nextLine();
            
            System.out.println("Generating secure data...");
            tools.generateSignKeyPair();
            newUser.setPbInfo(tools.getUserPB());                   // set Public key Info
            tools.generateAndSaveHash(String.valueOf(newUser.getId()), pass);
            newUser.setHashKeyInfo(tools.getUserHash());
            // create object with all private information,
            // atm, we only have RSA private key.
            SecData sec = new SecData(tools.getUserPV());
            newUser.setPrivData( sec );
            tools.encryptSecData( sec );
            newUser.setEncPrivData( tools.getEncPrivData() );
            tools.signSecData();
            newUser.setSignPrivData( tools.getSignPrivData() );
            
            // send message and get reply
            reply = conn.sendAndReply( msgs.secure(newUser) );
            
            if( reply.has("error") ){
                elem = reply.get("error");
                System.err.println("Creating Account Error: "+ elem.getAsString());
            
            } else if( reply.has("result") ){
                elem = reply.get("result");
                System.out.println("User Secure Data updated. Successful Saved");
                me = newUser;
                login = true;
            }
        }
    }
    
    /**
     *      DEMO
     */
    public void fakeLogin() {
        
        JsonObject reply;                      // reply message
        
        cardManager.waitForCard();
        if (!cardManager.isCardPresent())        // already print err message
            return;
        
        // need uuid for login
        User user = provStore.getUser();
        
        // send message and get reply
        reply = conn.sendAndReply( msgs.fakeLogin(user) );
        
        if( reply.has("error") ){
            System.err.println("Login Error: "+ reply.get("error").getAsString());
        
        } else {
            me = tools.decodeUser(reply);
            login = true;
            System.out.println("Login Successful.");
        }
    }
    
    /**             TODO
     *  Login with SmartCard.
     * 
     *  This operation will send a message of type: "login" and the uuid from the
     * smartcard (cc number) then on server side will get the corresponded user information
     * and 
     * 
     * @return True if login was successful, False otherwise
     */
    public void scLogin() {
        
        JsonObject reply;                      // reply message
        
        cardManager.waitForCard();
        if (!cardManager.isCardPresent())        // already print err message
            return;
        
        // need uuid for login
        User user = provStore.getUser();
        
        // send message and get reply
        reply = conn.sendAndReply( msgs.wantLogin(user) );    
        
        if( reply.has("error") ){
            System.err.println("Login Error: "+ reply.get("error").getAsString());
            
        } else if( reply.has("result") ){
            System.out.println("Challenger send to confirm identity.");
            
            byte[] challenge = reply.get("result").getAsString().getBytes();
            byte[] signature = signOps.signWithAuthCert(challenge);
            
            String msg = msgs.login( 
                    user, 
                    Base64.getEncoder().encodeToString( challenge ),
                    Base64.getEncoder().encodeToString( signature ),
                    provStore.getAuthCert().getSerialNumber().toString()
            );
            reply = conn.sendAndReply( msg );
            
            if( reply.has("error") ){
                System.err.println("Error: "+ reply.get("error").getAsString());
                
            } else {
                me = tools.decodeUser(reply);
                login = true;
                System.out.println("Login Successful.");
            }
        }
    }
    
    /**
     * * Send message to server to send a message to a user's message box
     */
    public void sendMessage() {
        
        JsonObject reply;
        JsonElement elem;                       // elem inside reply message
        int uuidDest;
        String msgToSend;
        
        System.out.println("UUID destination: ");
        uuidDest = sca.nextInt();
        sca.nextLine();
        System.out.println("Mensagem: ");
        msgToSend = sca.nextLine();
        
        reply = conn.sendAndReply( msgs.getWhois( String.valueOf(uuidDest) ));
        
        if( reply.has("error") ){          // error message instead.
            System.err.println("Error Sending Message: "+ reply.get("error").getAsString());
            
        } else if( reply.has("result") ) {
            
            String id = reply.get("result").getAsString();
            reply = conn.sendAndReply( msgs.getPublicKeyFrom( id ) );
            
            if( reply.has("error") ){          // error message instead.
                System.err.println("Error Sending Message: "+ reply.get("error").getAsString());
            
            } else {
                
                // verify public key
                byte[] encPk = Base64.getDecoder().decode( reply.get("encPbk").getAsString() );
                /*byte[] signature = Base64.getDecoder().decode( reply.get("signature").getAsString() );
                byte[] cert = Base64.getDecoder().decode( reply.get("cert").getAsString() );
                if (!signOps.verifySign(encPk, signature, cert)){
                    System.err.println("Error verifying public key signature, from "+ uuidDest);
                }*/
                
                PublicKey pbKey = tools.decodePbKey( encPk );
                String data = tools.encryptMessage(msgToSend, pbKey);
                String dataCopy = tools.encryptMessageCopy(msgToSend, me.getPrivData().getPvKeyInfo());
                
                String msg = msgs.sendMessage(me, Integer.parseInt(id), data, dataCopy);
                reply = conn.sendAndReply(msg);
                if(reply.has("error")) {
                    elem = reply.get("error");
                    System.err.println("Error Sending Message: "+ elem.getAsString());
                
                } else if (reply.has("result")) {
                    System.out.println("Message Successful Sended to "+ uuidDest);
                }   
            }
        }
    }
    
    /**
     * * Send message to server to receive a message from a user's message box
     */
    public void receiveMessage() {
        
        JsonObject reply;
        JsonElement elem;                       // elem inside reply message
        
        System.out.print("Message ID: ");
        String msdId = sca.next();
        
        String msg = msgs.receiveMessage( me , msdId);
        reply = conn.sendAndReply(msg);
        if (reply.has("error")) {
            System.err.println("Error Receiving Message: "+ reply.get("error").getAsString());
        
        } else if (reply.has("result")) {
            
            System.out.println();
            JsonArray rsp = reply.get("result").getAsJsonArray();
            System.out.println("Message sended from User ID: "+ rsp.get(0) +"\n");
            
            String message = tools.decrypMessage(rsp.get(1), me.getPrivData().getPvKeyInfo());
            System.out.println(message);
            System.out.println("\n-- Press enter to continue --");
            sca.nextLine();
            
            System.out.println("Generating receipt.");
            byte[] clearSign = signOps.signWithAuthCert(message.getBytes());
            String sign = Base64.getEncoder().encodeToString(clearSign);
            conn.sendAndReply( msgs.receiptMessage(me, msdId, sign) );
            if (reply.has("error")) {
                elem = reply.get("error");
                System.err.println("Error Receiving Message: "+ elem.getAsString());
            } else {
                System.out.println("Receipt sended.");
            }
        }
    }
    
    /**
     * @return True if log, False otherwise
     */
    public boolean isLogin() {
        return this.login;
    }
    
    /**
     *  Set all information's user to null.
     */
    public void logout() {
        login = false;
        me = null;
        System.out.println("Session terminated.");
    }
    
    public void login() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public User getUser() {
        return me;
    }

    JsonObject status() {
        
        JsonObject reply, result = null;
        
        System.out.print("Message ID: ");
        String msdId = sca.next();
        
        String msgType = msgs.statusMessage(me, msdId);
        reply = conn.sendAndReply(msgType);
        
        if (reply.has("error")) {
            System.err.println("Error Receiving Message: "+ reply.get("error").getAsString());
        
        } else if (reply.has("result")) {
        
            JsonObject info = reply.get("result").getAsJsonObject();
            String msg = info.get("msg").getAsString();
            JsonArray receipts = info.get("receipts").getAsJsonArray();
            
            // decript data and add it.
            result = new JsonObject();
            result.addProperty("msg", msg);
        }
        
        return reply; // result
    }
}
