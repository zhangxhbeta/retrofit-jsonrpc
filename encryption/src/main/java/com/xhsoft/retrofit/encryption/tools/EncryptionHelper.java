package com.xhsoft.retrofit.encryption.tools;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * 加解密工具类
 * 
 * @author zhangxiaohui
 * 
 */
public class EncryptionHelper {

	private static byte[] defaultIV = { 1, 2, 3, 4, 5, 6, 7, 8 };

	/**
	 * 加密
	 * 
	 * @param plainText
	 * @param strKey
	 * @param byteIV
	 * @return
	 * @throws Exception
	 */
	public static String encryptText(String plainText, String strKey, byte[] byteIV) throws GeneralSecurityException {

		byte[] plaintext = plainText.getBytes();// input

		byte[] encryptText = encryptBytes(plaintext, strKey, byteIV);

		try {
			String encryptedString = new String(encryptText, "US-ASCII");
			return encryptedString;
		} catch (UnsupportedEncodingException e) {
			throw new AssertionError(e);
		}
	}

	/**
	 * 加密
	 *
	 * @param plaintext
	 * @param strKey
	 * @param byteIV
	 * @return
	 * @throws GeneralSecurityException
	 */
	public static byte[] encryptBytes(byte[] plaintext, String strKey, byte[] byteIV) throws GeneralSecurityException {

		byte[] tdesKeyData = hexStringToByteArray(strKey);
		byte[] myIV = byteIV.length == 0 ? defaultIV : byteIV;

		Cipher c3des = Cipher.getInstance("DESede/CBC/PKCS5Padding");
		SecretKeySpec myKey = new SecretKeySpec(tdesKeyData, "DESede");
		IvParameterSpec ivspec = new IvParameterSpec(myIV);

		c3des.init(Cipher.ENCRYPT_MODE, myKey, ivspec);
		byte[] cipherText = c3des.doFinal(plaintext);

		return Base64.encode(cipherText, Base64.DEFAULT);
	}

	/**
	 * 解密
	 * 
	 * @param plainText
	 * @param strKey
	 * @param byteIV
	 * @return
	 * @throws Exception
	 */
	public static String dencryptText(String plainText, String strKey, byte[] byteIV) throws GeneralSecurityException, UnsupportedEncodingException {
		byte[] inputData = plainText.getBytes();
		byte[] cipherText = dencryptBytes(inputData, strKey, byteIV);

		String dencryptedString = new String(cipherText, "utf-8");
		return dencryptedString;
	}

	/**
	 * 解密
	 *
	 * @param inputBytes
	 * @param strKey
	 * @param byteIV
	 * @return
	 * @throws GeneralSecurityException
	 * @throws UnsupportedEncodingException
	 */
	public static byte[] dencryptBytes(byte[] inputBytes, String strKey, byte[] byteIV) throws GeneralSecurityException, UnsupportedEncodingException {

		byte[] inputData = Base64.decode(inputBytes, Base64.DEFAULT);

		byte[] tdesKeyData = hexStringToByteArray(strKey);
		byte[] myIV = byteIV.length == 0 ? defaultIV : byteIV;

		Cipher c3des = Cipher.getInstance("DESede/CBC/PKCS5Padding");
		SecretKeySpec myKey = new SecretKeySpec(tdesKeyData, "DESede");
		IvParameterSpec ivspec = new IvParameterSpec(myIV);

		c3des.init(Cipher.DECRYPT_MODE, myKey, ivspec);
		byte[] cipherText = c3des.doFinal(inputData);

		return cipherText;
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(
					s.charAt(i + 1), 16));
		}
		return data;
	}
}