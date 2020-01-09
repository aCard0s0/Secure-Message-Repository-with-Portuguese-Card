package utilities;

/**
 *
 * @author acard0s0
 */
public class Menus {
    
    public static final String welcome(String name) {
        return "\nWelcome,"
                + "\n   "+ name;
    }
   
    public static String MainMenu_Demo = 
             "\n|===================================|"
            +"\n|              MENU                 |"
            +"\n|===================================|"
            +"\n| Options:                          |"
            +"\n|    1. Create Account (Smart Card) |" 
            +"\n|    2. Login          (Smart Card) |"
            +"\n|    3. Login                       |"
            +"\n|                                   |"
            +"\n| 0. Quit                           |"
            +"\n|===================================|"
            +"\nOption: ";
    
    public static final String SecondaryMenu =
             "\n|===========================|"
            +"\n|           MENU            |"
            +"\n|===========================|"
            +"\n| Options:                  |"
            +"\n|    1. List All Users      |"
            +"\n|    2. Show News Messages  |"
            +"\n|    3. Show All Messages   |"
            +"\n|    4. Send New Message    |"
            +"\n|    5. Receive Message     |"
            +"\n|    6. Message Status      |"
            +"\n|                           |"
            +"\n| 9. Logout                 |"
            +"\n| 0. Quit                   |"
            +"\n|===========================|"
            +"\nOption: ";
}
