package com.plociennik.vestal.encryption;

import com.plociennik.vestal.common.VestalException;
import org.cryptomator.siv.SivMode;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

// todo: pure LLM
// todo: methods should not be here

public class EncryptionUtils {

    private static final String HMAC_ALG = "HmacSHA256";
    private static final SivMode SIV = new SivMode();
    private static final int OUTPUT_LENGTH = 32;
    private static final String AES = "AES";
    private static final String SIV_MAC = "siv-mac";
    private static final String SIV_ENC = "siv-enc";

    public static String encrypt(String plaintext, SecretKey key) {
        byte[] macKeyBytes = hkdfExpand(key.getEncoded(), SIV_MAC);
        byte[] ctrKeyBytes = hkdfExpand(key.getEncoded(), SIV_ENC);
        byte[] ciphertext = SIV.encrypt(
                new SecretKeySpec(ctrKeyBytes, AES),
                new SecretKeySpec(macKeyBytes, AES),
                plaintext.getBytes(StandardCharsets.UTF_8)
        );
        return Base64.getUrlEncoder().withoutPadding().encodeToString(ciphertext);
    }

    public static String decrypt(String encoded, SecretKey key) {
        try {
            byte[] macKeyBytes = hkdfExpand(key.getEncoded(), SIV_MAC);
            byte[] ctrKeyBytes = hkdfExpand(key.getEncoded(), SIV_ENC);
            byte[] ciphertext = Base64.getUrlDecoder().decode(encoded);
            byte[] plaintext = SIV.decrypt(
                    new SecretKeySpec(ctrKeyBytes, AES),
                    new SecretKeySpec(macKeyBytes, AES),
                    ciphertext
            );
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new VestalException("1338_18082026", "Something happened while decrypting value.", e);
        }
    }

    private static byte[] hkdfExpand(byte[] key, String info) {
        Mac mac;
        try {
            mac = Mac.getInstance(HMAC_ALG);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        try {
            mac.init(new SecretKeySpec(key, HMAC_ALG));
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        byte[] result = new byte[OUTPUT_LENGTH];
        byte[] t = new byte[0];
        int pos = 0;
        byte counter = 1;
        while (pos < OUTPUT_LENGTH) {
            mac.reset();
            mac.update(t);
            mac.update(info.getBytes(StandardCharsets.UTF_8));
            mac.update(counter);
            t = mac.doFinal();
            int toCopy = Math.min(t.length, OUTPUT_LENGTH - pos);
            System.arraycopy(t, 0, result, pos, toCopy);
            pos += toCopy;
            counter++;
        }
        return result;
    }
}
