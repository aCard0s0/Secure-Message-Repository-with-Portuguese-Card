package main;

import server.ServerConnection;

//log4j
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 *      Security Project - Server Side
 * 
 *  Author @aCard0s0, André Cardoso, Mec: 65069, email: marquescardoso@ua.pt
 *  Author @nC0sta, Nelson Costa, Mec: 42983, email: nelson.costa@ua.pt
 */
public class Main 
{
    public static final Logger LOGGER = LogManager.getLogger(Main.class.getName());
    
    public static void main( String[] args )
    {       
        
        LOGGER.info("Server app started");
        ServerConnection server = new ServerConnection();
        server.startConnection();
    }
}
