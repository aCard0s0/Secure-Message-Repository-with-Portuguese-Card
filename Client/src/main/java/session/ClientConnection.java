package session;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import utilities.Configurations;

/**
 *
 * @author acard0s0
 */
public class ClientConnection {
    
    private Socket server;
    private OutputStream out;
    private JsonReader in;
        
    public ClientConnection() {
        server = null;
        out = null;
        in = null;
    }
    
    /**
     *  Connect to the address and port pre-define on Configuration file.
     * 
     * @return True if client successful connect to server, False otherwise.
     */
    public boolean startConnection() {
       
        boolean result = false;
        
        try {
            server = new Socket( InetAddress.getByName(Configurations.Address), Configurations.Port);
            System.out.print( "\nClient connected on "+ Configurations.Address +":"+ Configurations.Port + "\n" );
            
            out = server.getOutputStream();
            result = true;
            
        } catch (IOException ex) {
            System.err.println("Cannot connect to server");
            result = false;
        
        } finally {
            return result;
        }
        
    }
    
    public JsonObject sendAndReply( String data ) {
        
        JsonElement reply;
        
        try {
            out.write( data.getBytes( StandardCharsets.UTF_8 ) );
            
            in = new JsonReader( new InputStreamReader ( server.getInputStream(), "UTF-8") );
            reply = new JsonParser().parse( in );
            
            if (reply.isJsonObject()) {
                return reply.getAsJsonObject();
            }
        } catch (IOException ex) {
        }
        return null;
    }
    
    /**
     *  Close the connection to the server.
     */
    public void closeConnection() {
        
        try {
            if(server != null) {
                server.close();
            }
        } catch (IOException ex) {
        }

    }
}
