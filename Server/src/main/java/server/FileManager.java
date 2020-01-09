/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import description.UserDescription;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentSkipListSet;
import static utilities.Configurations.MBOXES_PATH;
import static utilities.Configurations.RECEIPTS_PATH;
import static utilities.Configurations.DESC_FILE_NAME;
import static main.Main.LOGGER;

/**
 *
 *      @author acard0s0
 *      @author nC0sta
 */
public class FileManager {
    
    File mboxesDir = null;
    File receiptsDir = null;
    
    public FileManager() {
        
        intiServer();
    }
    
    /**
     *      Init Server, explore alternatives here...
     *   Criação de directorios, se não existirem;
     *   Indexação dos user que já existem;
     */
    private void intiServer() {

        // Create mboxes directory, if not found
        mboxesDir = new File( MBOXES_PATH );
        if (mboxesDir.exists() == false) {
            try {
                mboxesDir.mkdir();
            } catch (Exception e) {
                LOGGER.error( "Cannot create directory " + MBOXES_PATH + ": " + e );
                System.exit ( 1 );
            }
        }

        // Create receipts directory, if not found
        receiptsDir = new File( RECEIPTS_PATH );
        if (receiptsDir.exists() == false) {
            try {
                receiptsDir.mkdir();
            } catch (Exception e) {
                LOGGER.error( "Cannot create directory " + RECEIPTS_PATH + ": " + e );
                System.exit ( 1 );
            }
        }
    }
    
    public ConcurrentSkipListSet getUsersOnServer() {
        
        ConcurrentSkipListSet<UserDescription> users = new ConcurrentSkipListSet<UserDescription>();

        // Load data for each and every user
        for (File file: mboxesDir.listFiles()) {

            if (file.isDirectory()) { // Users have a directory of their own
                int id;
                JsonElement description = null;

                try {
                    id = Integer.parseUnsignedInt( file.getName() );
                } catch (NumberFormatException e ) {
                    continue; // Not a user directory
                }

                // Read JSON description from file
                String path = MBOXES_PATH + "/" + file.getName() + "/" + DESC_FILE_NAME;
                try {
                    description = new JsonParser().parse( readFromFile( path ) );
                } catch (Exception e) {
                    LOGGER.error( "Cannot load user description from " + path + ": " + e );
                    System.exit ( 1 );
                }

                // Add user to the internal structure
                users.add( new UserDescription ( id, description ) );
            }
        }
        
        return users;
    }
    
    public void saveOnFile ( String path, String data ) throws Exception {

        FileWriter f = new FileWriter( path );
        f.write( data );
        f.flush();
        f.close();
    }

    public String readFromFile ( String path ) throws Exception {

        FileInputStream f = new FileInputStream( path );
        byte [] buffer = new byte[f.available()];
        f.read( buffer );
        f.close();

        return new String( buffer, StandardCharsets.UTF_8 );
    }
    
    public int newFile ( String path, String basename ) {

        for (int i = 1;; i++) {
            File file1 = new File( path + basename + i );
            File file2 = new File( path + "_" + basename + i );
            if (file1.exists() == false && file2.exists() == false) {
                return i;
            }
        }
    }
}
