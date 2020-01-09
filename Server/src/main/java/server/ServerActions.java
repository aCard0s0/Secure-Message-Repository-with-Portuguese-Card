package server;

import java.net.Socket;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import com.google.gson.*;
import com.google.gson.stream.*;
import java.io.IOException;
import message.MessageTypes;
import static main.Main.LOGGER;

/**
 *  Class provider by André Zúquete and João Barraca, professors of UA in the class.
 *  
 * Modified by:
 *      @author aCard0s0
 *      @author nC0sta
 */
public class ServerActions implements Runnable {
    
    boolean registered = false;

    private Socket client;                  // socket conection
    private JsonReader in;
    private OutputStream out;
    private ServerCommands behaviour;       // tells what to do

    public ServerActions(Socket c, ServerControl registry) {
        
        client = c;
        behaviour = new ServerCommands( registry );

        // set connections
        try {
            in = new JsonReader( new InputStreamReader ( c.getInputStream(), "UTF-8") );
            LOGGER.info( "Server message received" );
            out = c.getOutputStream();
            LOGGER.info( "Server message to send" );
            
        } catch (IOException e) {
            LOGGER.error( "Cannot use client socket: " + e );
            Thread.currentThread().interrupt();
        }
    }
    
    public void run() {
        
        JsonObject data;    // input information
        String reply;       // response message
        
        // Lifecycle
        while (true) {
            
            // read request
            data = readCommand();
            
            if (data == null) {
                try {
                    client.close();
                } catch (IOException e) {}
                return;
            }
            
            // information processing
            reply = processAndReply( data );            
            
            // send response
            try {
                LOGGER.info( "Send result: " + reply );
                out.write ( reply.getBytes( StandardCharsets.UTF_8 ) ); // write result on client socket
            } catch (IOException e ) {}
        }
    }

    private JsonObject readCommand () {
        
        try {
            JsonElement data = new JsonParser().parse( in );
            if (data.isJsonObject()) {
                //LOGGER.debug( "Data: " + data );
                return data.getAsJsonObject();
            }
            LOGGER.error( "Error while reading command from socket (not a JSON object), connection will be shutdown\n" );
            
            return null;
            
        } catch (JsonIOException e) {
            LOGGER.error( "Error while reading JSON command from socket, connection will be shutdown\n" );
            return null;
        } catch (JsonSyntaxException e) {
            LOGGER.error( "Error while reading JSON command from socket, connection will be shutdown\n" );
            return null;
        }
    }
    
    private String processAndReply( JsonObject data ) {
        
        String reply;
        JsonElement cmd = data.get( "type" );

        if (cmd == null) {
            LOGGER.info( "Invalid command in request: " + data );
            return behaviour.error( "Invalid command in request: " + data );
        }

        MessageTypes enumType = MessageTypes.valueOf(cmd.getAsString().toUpperCase());
        
        switch( enumType ) {
            case CREATE:    reply = behaviour.create( data );
                break;
            
            case SECURE:    reply = behaviour.secureData( data );
                break;
            
            case FAKELOGIN: reply = behaviour.fakeLogin( data );
                break;
            
                // ==============================================
            case WANTLOG:   reply = behaviour.wantToLogin( data );
                break;
                
            case SCLOGIN:   reply = behaviour.scLogin( data );
                break;
                
            case PASSLOGIN: reply = behaviour.passLogin( data );
                break;
                // ==============================================
                
            case LIST:      reply = behaviour.list( data );
                break;
                
            case NEW:       reply = behaviour.newMessage( data );
                break;
                
            case ALL:       reply = behaviour.all( data );
                break;
             
            case WHOIS:     reply = behaviour.whois( data );
                break;
                
            case PBKEY:     reply = behaviour.publickey( data );
                break;    
            
            case SEND:      reply = behaviour.send( data );
                break;
                
            case RECV:      reply = behaviour.recv( data );
                break;
                
            case RECEIPT:   reply = behaviour.receipt( data );
                break;
                
            case STATUS:    reply = behaviour.status( data );
                break;
                
            default:        reply = behaviour.unknown();
        }
        return reply;
    }
 
}