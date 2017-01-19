package ins.framework.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

/**
 * 加密工具类。(MD5算法)
 */
public class EncryptUtils {
	private static MessageDigest messageDigest;
	static {
		try {
			messageDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}

	private EncryptUtils() {
	}

	/**
	 * 中科软加密算法
	 * @param planText
	 *            明文
	 * @return 密文
	 */
	public static String sinosoftEncrypt(String planText) {
		byte[] digest = messageDigest.digest(planText.getBytes());
		return new String(Hex.encodeHex(digest)).toUpperCase();
	}

	public static void main(String[] args) throws NoSuchAlgorithmException {
		System.out.println(EncryptUtils.sinosoftEncrypt("0000"));
	}
}
