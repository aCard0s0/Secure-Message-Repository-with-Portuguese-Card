package cryptotools;

import java.security.SecureRandom;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import models.HashKeyInfo;

/*
 * PBKDF2 salted password hashing.
 * Author: havoc AT defuse.ca
 * www: http://crackstation.net/hashing-security.htm
 *
 * Modified by aCard0s0
 */
public class PBKDF2 {

    public static final String PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA1";

    // The following constants may be changed without breaking existing hashes.
    public static final int SALT_BYTES = 24;
    public static final int HASH_BYTES = 16;
    public static final int PBKDF2_ITERATIONS = 1000;

    public static final int ITERATION_INDEX = 0;
    public static final int SALT_INDEX = 1;
    public static final int PBKDF2_INDEX = 2;
    
    private String FILE_NAME = "PB.KDF2";
    private HashKeyInfo hashInfo;
    
    public PBKDF2() {
        hashInfo = null;
    }
    
    /**
    * Returns a salted PBKDF2 hash of the password.
    *
    * @param   password    the password to hash
    * @return              a salted PBKDF2 hash of the password
    */
    public String createHash(String password)
    {
        try {
            return createHash(password.toCharArray());
            
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(PBKDF2.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    /**
     * Returns a salted PBKDF2 hash of the password.
     *  And creates a HashKeyInfo object to save on the server
     * 
     * @param   password    the password to hash
     * @return              a salted PBKDF2 hash of the password encoded base64
     */
    private String createHash(char[] password)
        throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        // Generate a random salt
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);

        // Hash the password
        byte[] hash = pbkdf2(password, salt, PBKDF2_ITERATIONS, HASH_BYTES);
        // format iterations:salt:hash
        hashInfo = new HashKeyInfo(
                Base64.getEncoder().encodeToString(salt),
                PBKDF2_ITERATIONS);
        return Base64.getEncoder().encodeToString(hash);
    }

    /**
     *  Just after create a hashInfo this will return the information needed
     * @return 
     */
    public HashKeyInfo getHashInfo() {
        return hashInfo;
    }
    
    /**
     *  TODO.
     * 
     * @param password
     * @param salt
     * @return
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException 
     */
    public String reCreateHash(char[] password, byte[] salt)
        throws NoSuchAlgorithmException, InvalidKeySpecException 
    {
        // Hash the password
        byte[] hash = pbkdf2(password, salt, PBKDF2_ITERATIONS, HASH_BYTES);
        // format iterations:salt:hash
        //hashInfo = PBKDF2_ITERATIONS + ":" + toHex(salt);
        return Base64.getEncoder().encodeToString(hash);
    }
    
    /**
     * Validates a password using a hash.
     *
     * @param   password    the password to check
     * @param   goodHash    the hash of the valid password
     * @return              true if the password is correct, false if not
     */
    public boolean validatePassword(String password, String goodHash)
    {
        try {
            return validatePassword(password.toCharArray(), goodHash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(PBKDF2.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Validates a password using a hash.
     *
     * @param   password    the password to check
     * @param   goodHash    the hash of the valid password
     * @return              true if the password is correct, false if not
     */
    public static boolean validatePassword(char[] password, String goodHash)
        throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        // Decode the hash into its parameters
        String[] params = goodHash.split(":");
        int iterations = Integer.parseInt(params[ITERATION_INDEX]);
        byte[] salt = fromHex(params[SALT_INDEX]);
        byte[] hash = fromHex(params[PBKDF2_INDEX]);
        // Compute the hash of the provided password, using the same salt, 
        // iteration count, and hash length
        byte[] testHash = pbkdf2(password, salt, iterations, hash.length);
        // Compare the hashes in constant time. The password is correct if
        // both hashes match.
        return slowEquals(hash, testHash);
    }

    /**
     * Compares two byte arrays in length-constant time. This comparison method
     * is used so that password hashes cannot be extracted from an on-line 
     * system using a timing attack and then attacked off-line.
     * 
     * @param   a       the first byte array
     * @param   b       the second byte array 
     * @return          true if both byte arrays are the same, false if not
     */
    private static boolean slowEquals(byte[] a, byte[] b)
    {
        int diff = a.length ^ b.length;
        for(int i = 0; i < a.length && i < b.length; i++)
            diff |= a[i] ^ b[i];
        return diff == 0;
    }

    /**
     *  Computes the PBKDF2 hash of a password.
     *
     * @param   password    the password to hash.
     * @param   salt        the salt
     * @param   iterations  the iteration count (slowness factor)
     * @param   bytes       the length of the hash to compute in bytes
     * @return              the PBDKF2 hash of the password
     */
    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int bytes)
        throws NoSuchAlgorithmException, InvalidKeySpecException
    {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, bytes * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM);
        return skf.generateSecret(spec).getEncoded();
    }

    /**
     * Converts a string of hexadecimal characters into a byte array.
     *
     * @param   hex         the hex string
     * @return              the hex string decoded into a byte array
     */
    private static byte[] fromHex(String hex)
    {
        byte[] binary = new byte[hex.length() / 2];
        for(int i = 0; i < binary.length; i++)
        {
            binary[i] = (byte)Integer.parseInt(hex.substring(2*i, 2*i+2), 16);
        }
        return binary;
    }

    /**
     * Converts a byte array into a hexadecimal string.
     *
     * @param   array       the byte array to convert
     * @return              a length*2 character string encoding the byte array
     */
    private static String toHex(byte[] array)
    {
        BigInteger bi = new BigInteger(1, array);
        String hex = bi.toString(16);
        int paddingLength = (array.length * 2) - hex.length();
        if(paddingLength > 0) 
            return String.format("%0" + paddingLength + "d", 0) + hex;
        else
            return hex;
    }
}
