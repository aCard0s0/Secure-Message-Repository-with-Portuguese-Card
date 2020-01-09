package managers;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.User;

/**
 *
 * @author aCard0s0
 */
public class DisplayManager {

    public DisplayManager() {
    }
    
   
    /**
     *  TODO: formatar view
     * 
     * @param replyData 
     */
    public void printUserList(JsonObject replyData) {
        
        Type listType = new TypeToken<List<User>>() {}.getType();
        List<User> list = new Gson().fromJson(replyData.get("data"), listType);
        
        System.out.println("============== Users List ====================");
        list.forEach((u) -> {
            System.out.println("|  UUID: "+ u.getUuid() +" - "+ u.getName());
        });
        System.out.println("==============================================");
    }

    public void printNewMessages(JsonObject replyData) {
        
        JsonArray newMsgs = replyData.get("result").getAsJsonArray();
        
        System.out.println("============== New Messages ===================");
        for(JsonElement m : newMsgs){
            System.out.println("|    "+ m.getAsString());
        }
        System.out.println("====================================================");
    }

    public void printAllMessages(JsonObject replyData) {
        
        Type listType = new TypeToken< List<List<String>> >() {}.getType();
        List<List<String>> lists = new Gson().fromJson(replyData.get("result"), listType);
        
        List<String> receivedList = lists.get(0);
        List<String> sendedList = lists.get(1);
        
        System.out.println("============== Message Box ===================");
        System.out.println("=Received:");
        receivedList.forEach((r) -> {
            System.out.println("        "+ r);
        });
        System.out.println("=Sended:");
        sendedList.forEach((s) -> {
            System.out.println("        "+ s);
        });
        System.out.println("===================================");
    }

    public void printStatus(JsonObject replyData) {
        
        DateFormat dFormat = new SimpleDateFormat("dd.MM.yyyy 'at' HH:mm:ss z");
        Calendar calendar = Calendar.getInstance();
        
        JsonObject info = replyData.get("result").getAsJsonObject();
        String msg = info.get("msg").getAsString();
        JsonArray receipts = info.get("receipts").getAsJsonArray();
    
        System.out.println("\n=========== Status ================");
        //System.out.println("=Text:");
        //System.out.println(msg);
        System.out.println("\n=Metadata:");
        if(receipts.size() > 0) {
            JsonObject r;
            for(int i=0; i< receipts.size(); i++){
                r = receipts.get(i).getAsJsonObject();
                System.out.println("| To: "+ r.get("id") );
                calendar.setTimeInMillis(Long.parseLong( r.get("date").getAsString() ));
                System.out.println("| Date: "+ dFormat.format(calendar.getTime()));
                System.out.println("| Receipt: "+ r.get("receipt")); // todo is valid?
                System.out.println("==");
            }
        } else {
            System.out.println("| No receipts yet.");
        }
        System.out.println("===================================");
    }
    
}
