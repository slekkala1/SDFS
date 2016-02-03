package tempest.services;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by swapnalekkala on 11/2/15.
 */
public class HashKey {

    public static String hashKey(String key) {
        StringBuffer sb = new StringBuffer();

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(key.getBytes());

            byte byteData[] = md.digest();

            for (int i = 0; i < byteData.length; i++) {
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
            }

        } catch (NoSuchAlgorithmException e) {
        }
        return sb.toString();
    }

    public static int hexToKey(String hex) {
        String bin = new BigInteger(hex, 16).toString(2);
        int m = 7;
        String bitsAfterTrucating = bin.substring(bin.length() - m);
        int foo = Integer.parseInt(bitsAfterTrucating, 2);
        return foo;
    }
}