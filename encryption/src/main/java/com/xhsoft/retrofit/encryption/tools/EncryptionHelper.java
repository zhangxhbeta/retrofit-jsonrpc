package com.xhsoft.retrofit.encryption.tools;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * 加解密工具类.
 *
 * @author zhangxhbeta
 *
 */
public class EncryptionHelper {

  private static byte[] defaultIV = {1, 2, 3, 4, 5, 6, 7, 8};

  /**
   * 加密.
   *
   * @param plainText 原始字符串
   * @param strKey 加密key
   * @param byteIv 向量
   * @return 密文
   * @throws GeneralSecurityException 加密过程中抛出的异常
   */
  public static String encryptText(String plainText, String strKey, byte[] byteIv)
      throws GeneralSecurityException {

    byte[] plaintext = plainText.getBytes();// input

    byte[] encryptText = encryptBytes(plaintext, strKey, byteIv);

    try {
      String encryptedString = new String(encryptText, "US-ASCII");
      return encryptedString;
    } catch (UnsupportedEncodingException ex) {
      throw new AssertionError(ex);
    }
  }

  /**
   * 加密.
   *
   * @param plaintext 原始字符串字节
   * @param strKey 加密key
   * @param byteIv 向量
   * @return 加密的字符串数组
   * @throws GeneralSecurityException 加密过程中的异常
   */
  public static byte[] encryptBytes(byte[] plaintext, String strKey, byte[] byteIv)
      throws GeneralSecurityException {

    byte[] tdesKeyData = hexStringToByteArray(strKey);
    byte[] myIv = byteIv.length == 0 ? defaultIV : byteIv;

    Cipher c3des = Cipher.getInstance("DESede/CBC/PKCS5Padding");
    SecretKeySpec myKey = new SecretKeySpec(tdesKeyData, "DESede");
    IvParameterSpec ivspec = new IvParameterSpec(myIv);

    c3des.init(Cipher.ENCRYPT_MODE, myKey, ivspec);
    byte[] cipherText = c3des.doFinal(plaintext);

    return Base64.encode(cipherText, Base64.NO_WRAP);
  }

  /**
   * 解密.
   */
  public static String dencryptText(String encryptText, String strKey, byte[] byteIv)
      throws GeneralSecurityException, UnsupportedEncodingException {
    byte[] inputData = encryptText.getBytes();
    byte[] cipherText = dencryptBytes(inputData, strKey, byteIv);

    String dencryptedString = new String(cipherText, "utf-8");
    return dencryptedString;
  }

  /**
   * 解密.
   */
  public static byte[] dencryptBytes(byte[] inputBytes, String strKey, byte[] byteIv)
      throws GeneralSecurityException, UnsupportedEncodingException {

    byte[] inputData = Base64.decode(inputBytes, Base64.DEFAULT);

    byte[] tdesKeyData = hexStringToByteArray(strKey);
    byte[] myIv = byteIv.length == 0 ? defaultIV : byteIv;

    Cipher c3des = Cipher.getInstance("DESede/CBC/PKCS5Padding");
    SecretKeySpec myKey = new SecretKeySpec(tdesKeyData, "DESede");
    IvParameterSpec ivspec = new IvParameterSpec(myIv);

    c3des.init(Cipher.DECRYPT_MODE, myKey, ivspec);
    byte[] cipherText = c3des.doFinal(inputData);

    return cipherText;
  }

  /**
   * 十六进制字符串转字节数组.
   *
   * @param text 字符串
   */
  public static byte[] hexStringToByteArray(String text) {
    int len = text.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(text.charAt(i), 16) << 4) + Character.digit(
          text.charAt(i + 1), 16));
    }
    return data;
  }
}
