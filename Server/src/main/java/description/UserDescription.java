package description;

import com.google.gson.*;

public class UserDescription implements Comparable {

    public int id;                      // id extracted from the CREATE command
    public JsonElement description;     // JSON user's description
    public String uuid;                 // User unique identifier (across sessions)

    public UserDescription ( int id, JsonElement description ) {
        
        this.id = id;
        this.description = description;
        uuid = description.getAsJsonObject().get( "uuid" ).getAsString();
        description.getAsJsonObject().addProperty( "id", new Integer( id ) );
    }

    public UserDescription ( int id ) {
        this.id = id;
    }

    public int compareTo ( Object x ) {
        return ((UserDescription) x).id - id;
    }
    
    public JsonObject getDescription() {
        return description.getAsJsonObject();
    }
}