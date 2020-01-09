package models;

import java.io.Serializable;

/**
 *      All private data from user go here!
 * 
 * @author aCard0s0
 */
public class SecData implements Serializable {
    
    private final long serialVersionUID = 1;
    
    private PvKeyInfo pvKeyInfo;            // RSA private key

    public SecData(PvKeyInfo pvKeyInfo) {
        this.pvKeyInfo = pvKeyInfo;
    }

    public PvKeyInfo getPvKeyInfo() {
        return pvKeyInfo;
    }
}
