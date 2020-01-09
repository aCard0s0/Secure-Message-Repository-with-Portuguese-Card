package server;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import description.UserDescription;
import java.security.cert.Certificate;
import java.util.Base64;
import java.util.UUID;
import static main.Main.LOGGER;

/**
 *      @author aCard0s0
 *      @author NelsonCosta
 */
public class ServerCommands {

    ServerControl registry;
    UserDescription me;

    public ServerCommands(ServerControl registry) {
        
        this.registry = registry;
    }
    
    public String create( JsonObject data ) {
        
        LOGGER.info( "Message Type Received: CREATE" );
        System.out.println(data);
        
        JsonElement uuid = data.get( "uuid" );

        if (uuid == null) {
            LOGGER.error( "No \"uuid\" field in \"create\" request: " + data );
            return error( "Wrong request format" );
        }

        if (registry.userExists( uuid.getAsString() )) {
            LOGGER.error( "User already exists: " + data );
            return error( "uuid already exists" );
        }

        data.remove ( "type" );
        me = registry.addUser( data );

        return result( "\"result\":\"" + me.id + "\"" );
    }

    public String whois( JsonObject data ) {
        
        LOGGER.info( "Message Type: WHOIS" );
        
        String uuid = data.get( "uuid" ).getAsString();
        
        if (uuid == null) {
            LOGGER.error( "No \"uuid\" field in \"whois\" request: " + data );
            return error( "Wrong request format" );
        }
        
        if (!registry.userExists( uuid )) {
            LOGGER.error( "User doesn't exists: " + data );
            return error( "uuid doesn't exists" );
        }
        
        UserDescription user = registry.getUserByUuid( uuid );
        
        return result( "\"result\":" + user.id  );
    }
    
    public String list( JsonObject data ) {
        
        LOGGER.info( "Message Type: LIST" );
        
        String list;
        int user = 0; // 0 means all users
        JsonElement id = data.get( "id" );

        if (id != null) {
            user = id.getAsInt();
        }

        //LOGGER.debug( "List " + (user == 0 ? "all users" : "user ") + user );

        list = registry.listUsers( user );
        
        return result( "\"data\":" + (list == null ? "[]" : list) );
    }

    public String newMessage( JsonObject data ) {
        LOGGER.info( "Message Type: NEW" );
        
        JsonElement id = data.get( "id" );
            int user = id == null ? -1 : id.getAsInt();

            if (id == null || user <= 0) {
                LOGGER.error( "No valid \"id\" field in \"new\" request: " + data );
                return error( "\"Wrong request format\"" );
            }

            return result( "\"result\":" + registry.userNewMessages( user ) );
    }

    public String all( JsonObject data ) {
        
        LOGGER.info( "Message Type: ALL" );

        JsonElement id = data.get( "id" );
        int user = id == null ? -1 : id.getAsInt();

        if (id == null || user <= 0) {
            LOGGER.error( "No valid \"id\" field in \"new\" request: " + data );
            return error( "\"Wrong request format\"" );
        }

        return result( "\"result\":[" + registry.userAllMessages( user ) + "," +
                    registry.userSentMessages( user ) + "]" );
    }

    public String send( JsonObject data ) {
        
        LOGGER.info( "Message Type: SEND" );

        JsonElement src = data.get( "src" );
        JsonElement dst = data.get( "dst" );
        JsonElement msg = data.get( "msg" );
        JsonElement copy = data.get( "copy" );

        if (src == null || dst == null || msg == null || copy == null) {
            LOGGER.error( "Badly formated \"send\" request: " + data );
            return error( "\"Wrong request format\"" );
        }

        int srcId = src.getAsInt();
        int dstId = dst.getAsInt();

        if (registry.userExists( srcId ) == false) {
            LOGGER.error( "Unknown source id for \"send\" request: " + data );
            return error( "\"Wrong parameters\"" );
        }

        if (registry.userExists( dstId ) == false) {
            LOGGER.error( "Unknown destination id for \"send\" request: " + data );
            return error( "\"Wrong parameters\"" );
        }

        // Save message and copy

        String response = registry.sendMessage( srcId, dstId,
                                                msg.getAsString(),
                                                copy.getAsString() );

        return result( "\"result\":" + response );
    }

    public String recv( JsonObject data ) {
        
        LOGGER.info( "Message Type: RECV" );

        JsonElement id = data.get( "id" );
        JsonElement msg = data.get( "msg" );

        if (id == null || msg == null) {
            LOGGER.error( "Badly formated \"recv\" request: " + data );
            return error( "Wrong request format" );
        }

        int fromId = id.getAsInt();

        if (registry.userExists( fromId ) == false) {
            LOGGER.error( "Unknown source id for \"recv\" request: " + data );
            return error( "Wrong parameters" );
        }

        if (registry.messageExists( fromId, msg.getAsString() ) == false) {
            LOGGER.error( "Unknown message for \"recv\" request: " + data );
            return error( "Wrong parameters" );
        }

        // Read message

        String response = registry.recvMessage( fromId, msg.getAsString() );

        return result( "\"result\":" + response );
    }

    public String receipt( JsonObject data ) {
        
        LOGGER.info( "Message Type: RECEIPT" );

        JsonElement id = data.get( "id" );
        JsonElement msg = data.get( "msg" );
        JsonElement receipt = data.get( "receipt" );

        if (id == null || msg == null || receipt == null) {
            LOGGER.error( "Badly formated \"receipt\" request: " + data );
            return error( "Wrong request format" );
        }

        int fromId = id.getAsInt();

        if (registry.messageWasRed( fromId, msg.getAsString() ) == false) {
            LOGGER.error( "Unknown, or not yet red, message for \"receipt\" request: " + data );
            return error( "Wrong parameters" );
        }

        // Store receipt
        registry.storeReceipt( fromId, msg.getAsString(), receipt.getAsString() );
        return result("\"result\":\"Succeful Sended\""); // dont have a response
    }

    public String status( JsonObject data ) {
        
        LOGGER.info( "Message Type: STATUS" );

        JsonElement id = data.get( "id" );
        JsonElement msg = data.get( "msg" );

        if (id == null || msg == null) {
            LOGGER.error( "Badly formated \"status\" request: " + data );
            return error( "Wrong request format" );
        }

        int fromId = id.getAsInt();

        if (registry.copyExists( fromId, msg.getAsString() ) == false) {
            LOGGER.error( "Unknown message for \"status\" request: " + data );
            return error( "Wrong parameters" );
        }

        // Get receipts

        String response = registry.getReceipts( fromId, msg.getAsString() );

        return result("\"result\":" + response );
    }
    
    public String unknown() {
        
        LOGGER.error( "Unknown request" );
        return error( "Unknown request" );
    }
    
    public String result( String result ) {
        return "{"+ result +"}\n";
    }
    
    public String error( String error ) {
        return "{\"error\":\""+ error +"\"}\n";
    }

    /** MY FUNCTIONS. */
    
    /**
     *  Message to complete the registration.
     * 
     * @param data
     * @return 
     */
    public String secureData(JsonObject data) {
        
        LOGGER.info( "Message Type: SECURE DATA" );
        
        JsonElement uuid = data.get( "uuid" );
        JsonElement pvData = data.get( "encPrivData" );
        JsonElement signData = data.get( "signPrivData" );
        JsonElement pbk = data.get( "pbInfo" );
        JsonElement hashInfo = data.get( "hashInfo" );
        
        if(uuid == null || pvData == null || signData == null || pbk == null || hashInfo == null) {
            LOGGER.error( "Badly formated \"recv\" request: " + data );
            return error( "Wrong request format" );
        }
        
        if (!registry.userExists( uuid.getAsString() )) {
            LOGGER.error( "User doesn't exists: " + data );
            return error( "uuid doesn't exists" );
        }
        
        data.remove ( "type" );
        me = registry.updateUser( data );
        
        return result( "\"result\":\"ID: "+ me.id+", Secure Data Updated \"" );
    }
    
    /**
     *      Generate and send a challenge to be sign by the client.
     * @param data
     * @return the challenge to be sign by the client.
     */
    public String wantToLogin(JsonObject data) {
        
        LOGGER.info( "Message Type: WANTLOG" );
        
        JsonElement uuid = data.get( "uuid" );
        
        if (uuid == null) {
            LOGGER.error( "No \"uuid\" field in \"wantlog\" request: " + data );
            return error( "Wrong request format" );
        }

        if (!registry.userExists( uuid.getAsString() )) {
            LOGGER.error( "User does not exists: " + data );
            return error( "uuid does not exists" );
        }
        
        // generate challenge to sign
        UUID challenge = UUID.randomUUID();
        
        return result("\"result\":" + challenge.toString() );
    }

    /**
     *      JUST FOR TESTING
     * @param data
     * @return 
     */
    public String fakeLogin(JsonObject data) {
        
        LOGGER.info( "Message Type: FAKELOGIN" );
        
        JsonElement uuid = data.get( "uuid" );
        
        if (uuid == null) {
            LOGGER.error( "No \"uuid\" field in \"fakelogin\" request: " + data );
            return error( "Wrong request format" );
        }
        
        me = registry.getUserByUuid(uuid.getAsString());
        
        return me.description.toString();
    }
    
    /**
     *  Message params are verify.
     *  Certificate is verify if belongs to user, uuid.
     *  Signature of the challenge is verify.
     * 
     * @param data
     * @return login message if successful, error otherwise
     */
    public String scLogin(JsonObject data) {
        
        LOGGER.info( "Message Type: SCLOGIN" );
        
        JsonElement uuid = data.get( "uuid" );
        JsonElement challengeJE = data.get( "challenge" );
        JsonElement signatureJE = data.get( "signature" );
        JsonElement serialCertJE = data.get( "serialCert" );
        
        if (uuid == null || challengeJE == null || signatureJE == null || serialCertJE == null) {
            LOGGER.error( "Badly formated \"login\" request: " + data );
            return error( "Wrong request format" );
        }
         
        if (!registry.userExists( uuid.getAsString() )) {
            LOGGER.error( "Unknown source uuid for \"login\" request: " + data );
            return error( "uuid does not exists" );
        }
        
        String serial = serialCertJE.getAsString();
        
        if (!registry.certBelongsUuid(uuid.getAsString(), serial)){
            LOGGER.error( "Serial Certification: "+ serialCertJE +" doesn't belongs to uuid: "+ uuid.getAsString() );
            return error( "Serial Certification doesn't belongs to this uuid");
        }
        
        byte[] challenge = Base64.getDecoder().decode(challengeJE.getAsString());
        byte[] signature = Base64.getDecoder().decode(signatureJE.getAsString());
        Certificate cert = registry.getCertificateBySerial(uuid.getAsString(), serial);
        
        if (!registry.verifySignature(challenge, signature, cert)) {
            return error( "Signature virification fail" );
        }
        
        me = registry.getUserByUuid( uuid.getAsString() );
        
        return me.description.toString();
    }
    
    /**
     *      TODO
     * @param data
     * @return 
     */
    public String passLogin(JsonObject data) {
        
        LOGGER.info( "Message Type: PASSLOGIN" );
        
        JsonElement uuid = data.get( "uuid" );
        
        if (uuid == null ) {
            LOGGER.error( "Badly formated \"login\" request: " + data );
            return error( "Wrong request format" );
        }
        
        
        
        return "";
    }
    
    /**
     * @param data
     * @return public key from authentication certificate
     */
    String publickey(JsonObject data) { 
        
        LOGGER.info( "Message Type: PBKEY" );
        
        JsonElement id = data.get( "id" );
        
        if (id == null) {
            LOGGER.error( "Badly formated \"pbkey\" request: " + data );
            return error( "Wrong request format" );
        }
        
        JsonObject userDesc = registry.getUserById( id.getAsInt() ).getAsJsonObject();
        JsonObject pbInfo = userDesc.get("pbInfo").getAsJsonObject();
        
        //String serialCert = pbInfo.get("serialCert").getAsString();
        //String encCert = userDesc.get("certList").getAsJsonObject().get(serialCert).getAsString();
        
        //pbInfo.remove("serialCert");
        //pbInfo.addProperty("cert", encCert);
        
        //LOGGER.info( "PbKey from: "+ id +" access by: ..." );
        
        return pbInfo.toString();
    }

}