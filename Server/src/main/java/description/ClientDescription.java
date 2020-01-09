package description;

import java.io.OutputStream;
import com.google.gson.*;

public class ClientDescription implements Comparable {

    private String id;		 // id extracted from the JASON description
    private JsonElement description; // JSON description of the client, including id
    private OutputStream out;	 // Stream to send messages to the client

    public ClientDescription ( String id, JsonElement description, OutputStream out )
    {
        this.id = id;
        this.description = description;
        this.out = out;
    }

    public int compareTo ( Object x )
    {
        return ((ClientDescription) x).id.compareTo( id );
    }

}
