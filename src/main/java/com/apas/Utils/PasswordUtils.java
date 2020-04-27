package com.apas.Utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
 
public class PasswordUtils {
 
	final static String secretKeyString = "ssshhhhhhhhhhh!!!!";
    private static SecretKeySpec secretKey;
    private static byte[] key;
 
	/**
	 * This function will set the secret key passed in the variable secretKeyString
	 * 
	 * @param myKey
	 *            the secret key to be set
	 */
    public static void setKey(String myKey) {
        MessageDigest sha = null;
        try {
            key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16); 
            secretKey = new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
 
	/**
	 * This function will encrypt he String using the secret key
	 * 
	 * @param strToEncrypt
	 *            String to be encrypted
	 *            @param secret
	 *            		secret key to be used to encryp the string
	 */
    public static String encrypt(String strToEncrypt, String secret) {
    	secret = secretKeyString;
        try{
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } 
        catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }
 
	/**
	 * This function will decrypt he String using the secret key
	 * 
	 * @param strToDecrypt
	 *            String to be decrypted
	 *            @param secret
	 *            		secret key to be used to encryp the string
	 */
    public static String decrypt(String strToDecrypt, String secret) {
    	secret = secretKeyString;
        try{
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } 
        catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }
}
