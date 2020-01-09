package managers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.User;

/**
 *  TODO: identificar o OS. win: \\ unix: / for paths
 * @author aCard0s0
 */
public class FileManager {

    private FileInputStream reader;
    private FileOutputStream writer;

    public FileManager() {
    }
    
    /**
     * @param path
     * @return 
     */
    public byte[] readFile(String path) {
        
        byte[] data = null;
        
        try {
            reader = new FileInputStream(path);
            data = new byte[reader.available()];
            reader.read(data);
            
        } catch (IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                reader.close();
            } catch (IOException ex) {
            }
        }
        
        return data;
    }
    
    /* write file
    */
    public void writeFile(byte[] data, String name) {
        
        try {
            writer = new FileOutputStream(name);
            writer.write(data);
        } catch (IOException ex) {
            Logger.getLogger(FileManager.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                writer.close();
                writer.flush();
            } catch (IOException ex) {
            }
        }
    }
    
    /*  Check if some file with "name" exist on some directory "path"
    *   @return true if exist, false ortherwise
    */
    public boolean existsFile(String path, String name) {
        
        File folder = new File(path);
        File[] listRoot = folder.listFiles(); 
        for (File file : listRoot) {
            if (file.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

}
