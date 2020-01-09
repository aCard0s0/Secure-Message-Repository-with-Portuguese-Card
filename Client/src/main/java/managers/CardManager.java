/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package managers;

import cryptotools.ProviderWrapper;
import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.Map;
import javax.smartcardio.Card;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import models.User;

/**
 *      Simply implementation for this demo.
 * 
 * @author aCard0s0
 */
public class CardManager {
    
    private TerminalFactory factory;
    private CardTerminals terminals;
    private CardTerminal activeTerm;
    private Card card;
    
    public CardManager() {
        factory = TerminalFactory.getDefault();
        terminals = factory.terminals();
        activeTerm = null;
    }
    
    /**
     * @return True if detected a SmartCard reader, False otherwise. 
     */
    public boolean isCardReaderConnected() {
        
        boolean result = false;
        
        try {
            if (!terminals.list().isEmpty()) {
                print("Smart card reader found.");
                result = true;
            }
        } catch (CardException ex) {
            error("No smart card readers found.");
            result = false;
            
        } finally {
            return result;
        }
    }
    
    /**
     *  After start app, if Smart Card Reader not found we can rescan.
     */
    public void rescanCardReader() {
        terminals = factory.terminals();
    }
    
    /**
     *  Setting the terminal use and the card.
     */
    public void waitForCard() {
        
        while (true) { 
            try {
                for (CardTerminal ct : terminals
                        .list(CardTerminals.State.CARD_INSERTION)) {
                    activeTerm = ct;
                    //print("\nTerminal: "+ activeTerm);
                    card = ct.connect("*");
                    //print("\nCard: "+ card);
                    return ;
                } 
                terminals.waitForChange();
            } catch (CardException ex) {
                error("\nCard operation failed.\n");
                error(ex.getMessage());
                System.exit(1);
            }
        } 
    }
    
    /**
     *  True if smart card found, False otherwise.
     * @return 
     */
    public boolean isCardPresent() {
        try {
            return this.activeTerm.isCardPresent();
        } catch (CardException ex) {
            error("Smart Card not found!");
        }
        return false;
    }
    
    
    private void print(Object data) {
        System.out.print(data);
    }
    
    private void error(Object data) {
        System.err.print(data);
    }
}
