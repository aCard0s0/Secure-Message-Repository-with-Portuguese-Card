package managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import models.User;

/**
 *
 * @author aCard0s0
 */
public class MessageManager {
    
    private final Gson gson;
    
    public MessageManager() {
        this.gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation()
                                    .create();
    }
    
    /**
     * @param user to create
     * @return the JSON message to send to server and create the user account
     */
    public String create(User user) {
       
        JsonObject msg = user.toJson().getAsJsonObject();
        msg.addProperty( "type", new String( "create" ) );
        return msg.toString();
    }
    
    /**
     * @param user
     * @return the JSON message to send to server to complete user creation account
     */
    public String secure(User user) {
        
        JsonObject msg = user.toJson().getAsJsonObject();
        msg.addProperty( "type", new String( "secure" ) );      // add type
        //msg.remove("name");
        //msg.remove("certList");
        return msg.toString();
    }
    
    /**
     *      just for test on login
     * @param user
     * @return 
     */
    public String fakeLogin(User user) {
        return "{\"type\":\"fakelogin\", "
                + "\"uuid\": \""+ user.getUuid()
                + "\"}";
    }
    
    /**
     * @param user
     * @return the JSON message for show intention of to do login in the server
     */
    public String wantLogin(User user){
        return "{\"type\":\"wantlog\", "
                + "\"uuid\": \""+ user.getUuid()
                + "\"}";
    }
    
    /**
     *  After show the intention to login with a message type: "wantlog", 
     * it was receive an challenge to sign.
     *  This message type now send all information needed to authenticate the user 
     * and do the login in the server.
     * 
     * @param user
     * @param challenge
     * @param signature
     * @param serialCert
     * @return 
     */
    public String login(User user, String challenge, String signature, String serialCert){
        
        return "{\"type\":\"sclogin\", "
                + "\"uuid\": \""+ user.getUuid() +"\", "
                + "\"challenge\": \""+ challenge +"\", "
                + "\"signature\": \""+ signature +"\", "
                + "\"serialCert\": \""+ serialCert
                + "\"}";
    }
    
    /**
     * @return the JSON message to send to server and return all users
     */
    public String getUsersList() {
        return "{\"type\":\"list\"}";
    }
    
    /**
     * @param id
     * @return the JSON message to send to server and return the user with id number
     */
    public String getUser(String id){
        return "{\"type\":\"list\", \"id\":\""+ id +"\"}";
    }
    
    /**
     * @param uuid
     * @return the JSON message to send to server and return the user with uuid number
     */
    public String getWhois(String uuid) {
        return "{\"type\":\"whois\", "
                + "\"uuid\":\""+ uuid +"\"}";
    }
    
    /**
     * @param user
     * @return the JSON message to send to server and return all new messages in a user's message box
     */
    public String getNewMessages(User user) {
        return "{\"type\":\"new\", \"id\":\""+ user.getId() +"\"}";
    }
    
    /**
     * @param user
     * @return the JSON message to send to server and return all messages in user's message box
     */
    public String getAllMessages(User user){
        return "{\"type\":\"all\", \"id\":\""+ user.getId() +"\"}";
    }
    
    /**
     * @param src source id
     * @param dst destination id
     * @param msg encrypted and signed message to be delivered to the target message box
     * @param copy replica of the message to be stored in the receipt box of the sender
     * @return the JSON message to send to server
     */
    public String sendMessage(User src, int dst, String msg, String copy){
        return "{\"type\":\"send\", \"src\":\""+ src.getId() +
                "\", \"dst\":\""+ dst +"\", \"msg\":\""+ msg +"\", \"copy\":\""+ copy +" \"}";
    }
    
    /**
     * @param src
     * @param msgId
     * @return the JSON message to send to server in order to receive a message from a user's message box
     */
    public String receiveMessage(User src, String msgId){ // TODO
        return "{\"type\":\"recv\", \"id\":\""+ src.getId() +"\", \"msg\":\""+ msgId +"\"}";
    }
    
    /**
     * @param src user id of the message box
     * @param msg message id
     * @param receipt signature over clear text message
     * @return the JSON message to send to server after receiving and validating a message from a message box
     */
    public String receiptMessage(User src, String msg, String receipt){ // TODO
        return "{\"type\":\"receipt\", \"id\":\""+ src.getId() 
                +"\", \"msg\":\""+ msg +"\", \"receipt\":\""+ receipt +"\"}";
    }
    
    /**
     * @param src user id of the receipt box
     * @param msg sent message id
     * @return the JSON message to send to server to check the reception status of a sent message
     */
    public String statusMessage(User src, String msg){ // TODO
        return "{\"type\":\"status\", \"id\":\""+ src.getId() +"\", \"msg\":\""+ msg+"\"}";
    }

    /**
     * @param id
     * @return the JSON message to send to server to receive public key from the id
     */
    public String getPublicKeyFrom(String id) {
        return "{\"type\":\"pbkey\", \"id\":\""+ id +"\"}";
    }
}
