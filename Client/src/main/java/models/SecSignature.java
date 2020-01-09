package models;

import com.google.gson.annotations.Expose;
import java.math.BigInteger;

/**
 *
 * @author aCard0s0
 */
public class SecSignature {
    
    @Expose private byte[] signature;
    @Expose private BigInteger serial;

    public SecSignature() {
    }

    public SecSignature(byte[] signature, BigInteger serial) {
        this.signature = signature;
        this.serial = serial;
    }

    public BigInteger getSerial() {
        return serial;
    }

    public byte[] getSignature() {
        return signature;
    }
}
