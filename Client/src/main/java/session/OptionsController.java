package session;

import managers.MessageManager;
import com.google.gson.JsonObject;
import java.util.Scanner;
import managers.DisplayManager;
import managers.CardManager;
import utilities.Menus;

/**
 *
 * @author aCard0s0
 */
public class OptionsController {
    
    final static Scanner sca = new Scanner(System.in); // to read user options
    
    private final CardManager cardManager;          // responsable for smart cards operations
    private final ClientConnection conn;            // responsable for server communication
    private final MessageManager msgs;              // responsable for create messages to send to server
    private final DisplayManager ui;                // responsable to formart data to display
    private final Session session;                  // responsable to manager user informations
    
    public OptionsController() {
        cardManager = new CardManager();
        conn = new ClientConnection();
        msgs = new MessageManager();
        ui = new DisplayManager();
        session = new Session(sca, cardManager, conn, msgs);
    }
    
    /**
     *  Start the Application.
     *  Note if smart card reader detected option with smart card will appear.
     *  Otherwise only option without using smart card will appear.
     * 
     *  TODO: implementação sem CC
     */
    public void start() {
        
        if(!conn.startConnection()) {
            return;
        }
        
        while(true){
            if(!session.isLogin()) {
                System.out.print( Menus.MainMenu_Demo );
                mainMenuDemoOptions( sca.nextInt() );
            } else {
                System.out.print( Menus.SecondaryMenu );
                waitForAction( sca.nextInt() );
            }
        }
    }
    
    private void mainMenuDemoOptions(int option) {
        
        switch (option) {
            case 1:             // Create Acc   (smartcard)
                session.scCreateAcc();
                break;
            case 2:             // Login        (smartcard)
                session.scLogin();  //session.fakeLogin();
                break;
            case 3:             // Login        ( without smartcard)
                session.login();
                break;
            case 0:             // Quit
                conn.closeConnection();
                System.exit(0);
                break;
            default: System.out.println("Opção inválida."); 
                break;
        }
    }
    
    /**
     *  After successful login this options will appear.
     * 
     * @param option 
     */
    private void waitForAction(int option) {
        
        JsonObject replyData;
        String msg;
                
        switch (option) {
            case 1:             // List
                msg = msgs.getUsersList();
                replyData = conn.sendAndReply( msg );
                ui.printUserList(replyData);
                break;
                
            case 2:             // New
                msg = msgs.getNewMessages( session.getUser() );
                replyData = conn.sendAndReply(msg);
                ui.printNewMessages(replyData);
                break;
                
            case 3:             // All
                msg = msgs.getAllMessages( session.getUser() );
                replyData = conn.sendAndReply(msg);
                ui.printAllMessages(replyData);
                break;
                
            case 4:             // Send
                session.sendMessage();
                break;
                
            case 5:             // Recv
                session.receiveMessage();
                break;
                
            case 6:             // Status
                replyData = session.status();
                ui.printStatus(replyData);
                break;
                
            case 9:             // Logout
                conn.closeConnection();
                session.logout();
                break;
                
            case 0:             // Quit
                conn.closeConnection();
                System.exit(0);
                break;
                
            default: System.err.println("Invalid option."); 
                break;
        }
    }
}