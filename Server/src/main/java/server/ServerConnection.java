/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import utilities.Configurations;
import static main.Main.LOGGER;

/**
 *
 * @author acard0s0
 * @author nC0sta
 */
public class ServerConnection {

    
    public ServerConnection() {
    }
    
    public void startConnection() {

        try {
            ServerSocket s = new ServerSocket( 
                Configurations.PORT, 
                Configurations.BACKLOG, 
                InetAddress.getByName(Configurations.ADDRESS ) 
            );
            //System.out.print( "\nStarted server on port " + Configurations.Port + "\n" );
            LOGGER.info(
                    "Started server on " + Configurations.ADDRESS +":" + Configurations.PORT
            );
            waitForClients( s );
        
        } catch (UnknownHostException e) {
            //System.err.print( "Unknown address ("+ Configurations.Address +"): " + e );
            LOGGER.error("Unknown address: " + Configurations.ADDRESS);
            System.exit( 1 );
        
        } catch (IOException e) {
            //System.err.print( "Cannot open socket: " + e );
            LOGGER.error("Cannot open socket: " + e);
            System.exit( 1 );
        }
    }
    
    private void waitForClients ( ServerSocket s ) {
        
        ServerControl registry = new ServerControl();   // por causa da concorrencia

        try {
            while (true) {
                Socket c = s.accept();
                ServerActions handler = new ServerActions( c, registry );
                new Thread( handler ).start();
                LOGGER.info("Socket accepted");
                LOGGER.info("Thread started");
            }
        } catch ( IOException e ) {
            LOGGER.error("Cannot use socket: " + e );
        }
    }
}
