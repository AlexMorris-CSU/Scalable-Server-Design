package cs455.scaling.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class hashing {


    public String SHA1FromBytes(byte[] data) {
        MessageDigest digest = null;
        try {
            digest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Could not get instance " + e);
        }
        byte[] hash = digest.digest(data);
        BigInteger hashInt = new BigInteger(1, hash);
        String finalString = hashInt.toString(16);
        while(finalString.length() < 40){
            finalString = '0' + finalString;
        }
        return finalString;
    }

}
