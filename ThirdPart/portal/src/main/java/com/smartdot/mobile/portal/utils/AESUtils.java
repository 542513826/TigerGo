package com.smartdot.mobile.portal.utils;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import Decoder.BASE64Encoder;


/**
 * AES加密类 128位秘钥，CBC模式
 * 目的：单点登录传值加密
 */

public class AESUtils {

    public static String SMARTID = "";

    public static void main(String[] args) throws Exception {

    }

    public static String encrypt128(String userid, String password) {
        int max = 999999;
        int min = 100000;
        Random random = new Random();
        int s = random.nextInt(max) % (max - min + 1) + min;
        String mi = userid + "/-/" + password + "/-/" + s;
        System.out.println(mi);
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");// 创建密码
            SecretKeySpec key = new SecretKeySpec("ret78935jgdsu2l0".getBytes(), "AES");
            IvParameterSpec iv = new IvParameterSpec("ji213jfaljf98w4w".getBytes());//使用CBC模式，需要一个向量iv，可增加加密算法的强度
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            byte[] result = cipher.doFinal(mi.getBytes());
            return new BASE64Encoder().encode(result); // 加密
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return null;
    }


}


