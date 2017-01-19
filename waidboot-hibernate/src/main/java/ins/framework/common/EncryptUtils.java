package ins.framework.common;

import ins.framework.utils.StringUtils;

/**
 * 加密工具类（MD5算法和中科软算法）
 */
public class EncryptUtils {
	private static final MD5 MD5_INSTANCE = new MD5();

	/**
	 * 返回经过MD5加密之后的字符串. <br>
	 * <br>
	 * <b>示例: </b>
	 * EncryptUtils.md5 (&quot;&quot;) 返回 &quot;d41d8cd98f00b204e9800998ecf8427e&quot;
	 * EncryptUtils.md5 (&quot;a&quot;) 返回 &quot;0cc175b9c0f1b6a831c399e269772661&quot;
	 * EncryptUtils.md5 (&quot;abc&quot;) 返回 &quot;900150983cd24fb0d6963f7d28e17f72&quot;
	 * EncryptUtils.md5 (&quot;message digest&quot;) 返回 &quot;f96b697d7cb7938d525a2f31aaf161d0&quot;
	 * EncryptUtils.md5 (&quot;abcdefghijklmnopqrstuvwxyz&quot;) 返回 &quot;c3fcd3d76192e4007dfb496cca67e13b&quot;
	 * @param str
	 *            明文
	 * @return 暗文
	 */
	public static String md5(String str) {
		return MD5_INSTANCE.getMD5ofStr(str);
	}

	/**
	 * 返回经过中科软业务系统的加密算法加密之后的字符串. <br>
	 * <br>
	 * <b>示例: </b>
	 * EncryptUtils.sinosoftEncrypt (&quot;&quot;) 返回 &quot;kkkkkkkk&quot;
	 * EncryptUtils.sinosoftEncrypt (&quot;a&quot;) 返回 &quot;kkakkkkk&quot;
	 * EncryptUtils.sinosoftEncrypt (&quot;abc&quot;) 返回 &quot;kkakkckb&quot;
	 * EncryptUtils.sinosoftEncrypt (&quot;message digest&quot;) 返回 &quot;acmbgckb&quot;
	 * @param str
	 *            明文
	 * @return 暗文
	 */
	public static String sinosoftEncrypt(String str) {
		return SinosoftEncrypt.sinosoftEncrypt(str);
	}
	/**
	 * 返回经过中科软业务系统的加密算法解密之后的字符串. <br>
	 */
	public static String sinosoftDecrypt(String str) {
		return SinosoftEncrypt.sinosoftDecrypt(str);
	}
}

/*******************************************************************************
 * md5 类实现了RSA Data Security, Inc.在提交给IETF 的RFC1321中的MD5 message-digest 算法。
 ******************************************************************************/
final class MD5 {
	/*
	 * 下面这些S11-S44实际上是一个4*4的矩阵，在原始的C实现中是用#define 实现的， 这里把它们实现成为static
	 * final是表示了只读，切能在同一个进程空间内的多个 Instance间共享
	 */
	private static final int S11 = 7;
	private static final int S12 = 12;
	private static final int S13 = 17;
	private static final int S14 = 22;
	private static final int S21 = 5;
	private static final int S22 = 9;
	private static final int S23 = 14;
	private static final int S24 = 20;
	private static final int S31 = 4;
	private static final int S32 = 11;
	private static final int S33 = 16;
	private static final int S34 = 23;
	private static final int S41 = 6;
	private static final int S42 = 10;
	private static final int S43 = 15;
	private static final int S44 = 21;
	private static final byte[] PADDING = { -128, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
			0, 0, 0, 0 };
	/*
	 * 下面的三个成员是MD5计算过程中用到的3个核心数据，在原始的C实现中 被定义到MD5_CTX结构中
	 */
	private long[] state = new long[4]; // state (ABCD)
	private long[] count = new long[2]; // number of bits, modulo 2^64 (lsb
	// first)
	private byte[] buffer = new byte[64]; // input buffer
	/*
	 * digestHexStr是MD5的唯一一个公共成员，是最新一次计算结果的 16进制ASCII表示.
	 */
	private String digestHexStr; // NOPMD by zhouxianli on 08-12-16 下午4:51
	/*
	 * digest,是最新一次计算结果的2进制内部表示，表示128bit的MD5值.
	 */
	private byte[] digest = new byte[16];

	/*
	 * getMD5ofStr是类MD5最主要的公共方法，入口参数是你想要进行MD5变换的字符串
	 * 返回的是变换完的结果，这个结果是从公共成员digestHexStr取得的．
	 */
	protected synchronized String getMD5ofStr(String inbuf) {
		md5Init();
		md5Update(inbuf.getBytes(), inbuf.length());
		md5Final();
		digestHexStr = "";
		for (int i = 0; i < 16; i++) {
			digestHexStr += byteHEX(digest[i]);
		}
		return digestHexStr;
	}

	// 这是MD5这个类的标准构造函数，JavaBean要求有一个public的并且没有参数的构造函数
	protected MD5() {
		md5Init();
	}

	/* md5Init是一个初始化函数，初始化核心变量，装入标准的幻数 */
	private void md5Init() {
		count[0] = 0L;
		count[1] = 0L;
		// /* Load magic initialization constants.
		state[0] = 0x67452301L;
		state[1] = 0xefcdab89L;
		state[2] = 0x98badcfeL;
		state[3] = 0x10325476L;
	}

	/*
	 * F, G, H ,I 是4个基本的MD5函数，在原始的MD5的C实现中，由于它们是
	 * 简单的位运算，可能出于效率的考虑把它们实现成了宏，在java中，我们把它们 实现成了private方法，名字保持了原来C中的。
	 */
	private long md5F(long x, long y, long z) {
		return (x & y) | ((~x) & z);
	}

	private long md5G(long x, long y, long z) {
		return (x & z) | (y & (~z));
	}

	private long md5H(long x, long y, long z) {
		return x ^ y ^ z;
	}

	private long md5I(long x, long y, long z) {
		return y ^ (x | (~z));
	}

	/*
	 * FF,GG,HH和II将调用F,G,H,I进行近一步变换 FF, GG, HH, and II transformations for
	 * rounds 1, 2, 3, and 4. Rotation is separate from addition to prevent
	 * recomputation.
	 */
	private long md5FF(long a, long b, long c, long d, long x, long s, long ac) { // NOPMD by zhouxianli on 08-12-1
		// 上午11:29
		a += md5F(b, c, d) + x + ac;
		a = ((int) a << s) | ((int) a >>> (32 - s));
		a += b;
		return a;
	}

	private long md5GG(long a, long b, long c, long d, long x, long s, long ac) { // NOPMD by zhouxianli on 08-12-1
		// 上午11:29
		a += md5G(b, c, d) + x + ac;
		a = ((int) a << s) | ((int) a >>> (32 - s));
		a += b;
		return a;
	}

	private long md5HH(long a, long b, long c, long d, long x, long s, long ac) { // NOPMD by zhouxianli on 08-12-1
		// 上午11:30
		a += md5H(b, c, d) + x + ac;
		a = ((int) a << s) | ((int) a >>> (32 - s));
		a += b;
		return a;
	}

	private long md5II(long a, long b, long c, long d, long x, long s, long ac) { // NOPMD by zhouxianli on 08-12-1
		// 上午11:30
		a += md5I(b, c, d) + x + ac;
		a = ((int) a << s) | ((int) a >>> (32 - s));
		a += b;
		return a;
	}

	/*
	 * md5Update是MD5的主计算过程，inbuf是要变换的字节串，inputlen是长度，这个
	 * 函数由getMD5ofStr调用，调用之前需要调用md5init，因此把它设计成private的
	 */
	private void md5Update(byte[] inbuf, int inputLen) {
		int i, index, partLen;
		byte[] block = new byte[64];
		index = (int) (count[0] >>> 3) & 0x3F;
		// /* Update number of bits */
		if ((count[0] += (inputLen << 3)) < (inputLen << 3)) { // NOPMD by zhouxianli on 08-12-16 下午4:52
			count[1]++;
		}
		count[1] += (inputLen >>> 29);
		partLen = 64 - index;
		// Transform as many times as possible.
		if (inputLen >= partLen) {
			md5Memcpy(buffer, inbuf, index, 0, partLen);
			md5Transform(buffer);
			for (i = partLen; i + 63 < inputLen; i += 64) {
				md5Memcpy(block, inbuf, 0, i, 64);
				md5Transform(block);
			}
			index = 0;
		} else {
			i = 0;
		}
		// /* Buffer remaining input */
		md5Memcpy(buffer, inbuf, index, i, inputLen - i);
	}

	/*
	 * md5Final整理和填写输出结果
	 */
	private void md5Final() {
		byte[] bits = new byte[8];
		int index, padLen;
		// /* Save number of bits */
		encode(bits, count, 8);
		// /* Pad out to 56 mod 64.
		index = (int) (count[0] >>> 3) & 0x3f;
		padLen = (index < 56) ? (56 - index) : (120 - index);
		md5Update(PADDING, padLen);
		// /* Append length (before padding) */
		md5Update(bits, 8);
		// /* Store state in digest */
		encode(digest, state, 16);
	}

	/*
	 * md5Memcpy是一个内部使用的byte数组的块拷贝函数，从input的inpos开始把len长度的
	 * 字节拷贝到output的outpos位置开始
	 */
	private void md5Memcpy(byte[] output, byte[] input, int outpos, int inpos, int len) {
		for (int i = 0; i < len; i++) {
			output[outpos + i] = input[inpos + i];
		}
	}

	/*
	 * md5Transform是MD5核心变换程序，有md5Update调用，block是分块的原始字节
	 */
	private void md5Transform(byte block[]) {
		long a = state[0], b = state[1], c = state[2], d = state[3];
		long[] x = new long[16];
		decode(x, block, 64);
		/* Round 1 */
		a = md5FF(a, b, c, d, x[0], S11, 0xd76aa478L); /* 1 */
		d = md5FF(d, a, b, c, x[1], S12, 0xe8c7b756L); /* 2 */
		c = md5FF(c, d, a, b, x[2], S13, 0x242070dbL); /* 3 */
		b = md5FF(b, c, d, a, x[3], S14, 0xc1bdceeeL); /* 4 */
		a = md5FF(a, b, c, d, x[4], S11, 0xf57c0fafL); /* 5 */
		d = md5FF(d, a, b, c, x[5], S12, 0x4787c62aL); /* 6 */
		c = md5FF(c, d, a, b, x[6], S13, 0xa8304613L); /* 7 */
		b = md5FF(b, c, d, a, x[7], S14, 0xfd469501L); /* 8 */
		a = md5FF(a, b, c, d, x[8], S11, 0x698098d8L); /* 9 */
		d = md5FF(d, a, b, c, x[9], S12, 0x8b44f7afL); /* 10 */
		c = md5FF(c, d, a, b, x[10], S13, 0xffff5bb1L); /* 11 */
		b = md5FF(b, c, d, a, x[11], S14, 0x895cd7beL); /* 12 */
		a = md5FF(a, b, c, d, x[12], S11, 0x6b901122L); /* 13 */
		d = md5FF(d, a, b, c, x[13], S12, 0xfd987193L); /* 14 */
		c = md5FF(c, d, a, b, x[14], S13, 0xa679438eL); /* 15 */
		b = md5FF(b, c, d, a, x[15], S14, 0x49b40821L); /* 16 */
		/* Round 2 */
		a = md5GG(a, b, c, d, x[1], S21, 0xf61e2562L); /* 17 */
		d = md5GG(d, a, b, c, x[6], S22, 0xc040b340L); /* 18 */
		c = md5GG(c, d, a, b, x[11], S23, 0x265e5a51L); /* 19 */
		b = md5GG(b, c, d, a, x[0], S24, 0xe9b6c7aaL); /* 20 */
		a = md5GG(a, b, c, d, x[5], S21, 0xd62f105dL); /* 21 */
		d = md5GG(d, a, b, c, x[10], S22, 0x2441453L); /* 22 */
		c = md5GG(c, d, a, b, x[15], S23, 0xd8a1e681L); /* 23 */
		b = md5GG(b, c, d, a, x[4], S24, 0xe7d3fbc8L); /* 24 */
		a = md5GG(a, b, c, d, x[9], S21, 0x21e1cde6L); /* 25 */
		d = md5GG(d, a, b, c, x[14], S22, 0xc33707d6L); /* 26 */
		c = md5GG(c, d, a, b, x[3], S23, 0xf4d50d87L); /* 27 */
		b = md5GG(b, c, d, a, x[8], S24, 0x455a14edL); /* 28 */
		a = md5GG(a, b, c, d, x[13], S21, 0xa9e3e905L); /* 29 */
		d = md5GG(d, a, b, c, x[2], S22, 0xfcefa3f8L); /* 30 */
		c = md5GG(c, d, a, b, x[7], S23, 0x676f02d9L); /* 31 */
		b = md5GG(b, c, d, a, x[12], S24, 0x8d2a4c8aL); /* 32 */
		/* Round 3 */
		a = md5HH(a, b, c, d, x[5], S31, 0xfffa3942L); /* 33 */
		d = md5HH(d, a, b, c, x[8], S32, 0x8771f681L); /* 34 */
		c = md5HH(c, d, a, b, x[11], S33, 0x6d9d6122L); /* 35 */
		b = md5HH(b, c, d, a, x[14], S34, 0xfde5380cL); /* 36 */
		a = md5HH(a, b, c, d, x[1], S31, 0xa4beea44L); /* 37 */
		d = md5HH(d, a, b, c, x[4], S32, 0x4bdecfa9L); /* 38 */
		c = md5HH(c, d, a, b, x[7], S33, 0xf6bb4b60L); /* 39 */
		b = md5HH(b, c, d, a, x[10], S34, 0xbebfbc70L); /* 40 */
		a = md5HH(a, b, c, d, x[13], S31, 0x289b7ec6L); /* 41 */
		d = md5HH(d, a, b, c, x[0], S32, 0xeaa127faL); /* 42 */
		c = md5HH(c, d, a, b, x[3], S33, 0xd4ef3085L); /* 43 */
		b = md5HH(b, c, d, a, x[6], S34, 0x4881d05L); /* 44 */
		a = md5HH(a, b, c, d, x[9], S31, 0xd9d4d039L); /* 45 */
		d = md5HH(d, a, b, c, x[12], S32, 0xe6db99e5L); /* 46 */
		c = md5HH(c, d, a, b, x[15], S33, 0x1fa27cf8L); /* 47 */
		b = md5HH(b, c, d, a, x[2], S34, 0xc4ac5665L); /* 48 */
		/* Round 4 */
		a = md5II(a, b, c, d, x[0], S41, 0xf4292244L); /* 49 */
		d = md5II(d, a, b, c, x[7], S42, 0x432aff97L); /* 50 */
		c = md5II(c, d, a, b, x[14], S43, 0xab9423a7L); /* 51 */
		b = md5II(b, c, d, a, x[5], S44, 0xfc93a039L); /* 52 */
		a = md5II(a, b, c, d, x[12], S41, 0x655b59c3L); /* 53 */
		d = md5II(d, a, b, c, x[3], S42, 0x8f0ccc92L); /* 54 */
		c = md5II(c, d, a, b, x[10], S43, 0xffeff47dL); /* 55 */
		b = md5II(b, c, d, a, x[1], S44, 0x85845dd1L); /* 56 */
		a = md5II(a, b, c, d, x[8], S41, 0x6fa87e4fL); /* 57 */
		d = md5II(d, a, b, c, x[15], S42, 0xfe2ce6e0L); /* 58 */
		c = md5II(c, d, a, b, x[6], S43, 0xa3014314L); /* 59 */
		b = md5II(b, c, d, a, x[13], S44, 0x4e0811a1L); /* 60 */
		a = md5II(a, b, c, d, x[4], S41, 0xf7537e82L); /* 61 */
		d = md5II(d, a, b, c, x[11], S42, 0xbd3af235L); /* 62 */
		c = md5II(c, d, a, b, x[2], S43, 0x2ad7d2bbL); /* 63 */
		b = md5II(b, c, d, a, x[9], S44, 0xeb86d391L); /* 64 */
		state[0] += a;
		state[1] += b;
		state[2] += c;
		state[3] += d;
	}

	/*
	 * Encode把long数组按顺序拆成byte数组，因为java的long类型是64bit的， 只拆低32bit，以适应原始C实现的用途
	 */
	private void encode(byte[] output, long[] input, int len) {
		int i, j;
		for (i = 0, j = 0; j < len; i++, j += 4) {
			output[j] = (byte) (input[i] & 0xffL);
			output[j + 1] = (byte) ((input[i] >>> 8) & 0xffL);
			output[j + 2] = (byte) ((input[i] >>> 16) & 0xffL);
			output[j + 3] = (byte) ((input[i] >>> 24) & 0xffL);
		}
	}

	/*
	 * decode把byte数组按顺序合成成long数组，因为java的long类型是64bit的，
	 * 只合成低32bit，高32bit清零，以适应原始C实现的用途
	 */
	private void decode(long[] output, byte[] input, int len) {
		int i, j;
		for (i = 0, j = 0; j < len; i++, j += 4) {
			output[i] = b2iu(input[j]) | (b2iu(input[j + 1]) << 8) | (b2iu(input[j + 2]) << 16)
					| (b2iu(input[j + 3]) << 24);
		}
	}

	/*
	 * b2iu是我写的一个把byte按照不考虑正负号的原则的＂升位＂程序，因为java没有unsigned运算
	 */
	private long b2iu(byte b) {
		return b < 0 ? b & 0x7F + 128 : b;
	}

	/*
	 * byteHEX()，用来把一个byte类型的数转换成十六进制的ASCII表示，
	 * 因为java中的byte的toString无法实现这一点，我们又没有C语言中的 sprintf(outbuf,"%02X",ib)
	 */
	private String byteHEX(byte ib) {
		char[] Digit = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		char[] ob = new char[2];
		ob[0] = Digit[(ib >>> 4) & 0X0F];
		ob[1] = Digit[ib & 0X0F];
		return new String(ob);
	}
}

/**
 * 中科软业务系统的加密算法类
 */
final class SinosoftEncrypt {
	private static final int PWD_LENGTH = 8;
	private static final String KEY1 = "54176382";
	private static final String KEY2 = "1e4s2dj6l38 5097vw.";
	private static final String KEY3 = "ab cdefghijklmn102p";
	private static final String EMPTY8 = "        ";

	/**
	 * 中科软业务系统的加密算法
	 * #############################################
	 * ## (13)
	 * ## 函数功能：字符串加密函数 ##
	 * ## 输入参数：lv_pass 需要操作的字符串 ##
	 * ## 输出参数：返回加密后的字串 lv_pass ##
	 * #############################################
	 * FUNCTION fencode(lv_pass)
	 * DEFINE lv_pass,lv_pass1 CHAR(8)
	 * DEFINE lv_key1,lv_key2,lv_key3,lv_key4 CHAR(19)
	 * DEFINE lv_i,lv_j SMALLINT
	 * INITIALIZE lv_pass1,lv_key1,lv_key2,lv_key3,lv_key4 TO NULL
	 * LET lv_i = 0
	 * LET lv_j = 0
	 * LET lv_key1=&quot;54176382&quot;
	 * LET lv_key2=&quot;1e4s2dj6l38 5097vw.&quot;
	 * LET lv_key3=&quot;ab cdefghijklmn102p&quot;
	 * LET lv_key4=57
	 * FOR lv_i=1 TO 8
	 * LET lv_j=lv_key1[lv_i]
	 * LET lv_pass1[lv_i]=lv_pass[lv_j]
	 * END FOR
	 * LET lv_pass=lv_pass1
	 * FOR lv_i=1 TO 8
	 * FOR lv_j=1 TO 19
	 * IF lv_pass[lv_i]=lv_key2[lv_j] THEN
	 * LET lv_pass1[lv_i]=lv_key3[lv_j]
	 * END IF
	 * END FOR
	 * END FOR
	 * LET lv_pass=lv_pass1
	 * RETURN lv_pass1 CLIPPED
	 * END FUNCTION
	 * @param password
	 *            明文
	 * @return 密文
	 */
	public static String sinosoftEncrypt(String password) {
		String encpwd = password;
		char[] encpwds = new char[PWD_LENGTH];
		if (encpwd.length() < PWD_LENGTH) {
			encpwd = new StringBuffer(PWD_LENGTH).append(encpwd).append(
					StringUtils.newString(" ", PWD_LENGTH - encpwd.length())).toString();
		}
		for (int i = 0; i < KEY1.length(); i++) {
			int j = Integer.parseInt(String.valueOf(KEY1.charAt(i)));
			encpwds[i] = encpwd.charAt(j - 1);
		}
		encpwd = new String(encpwds);
		for (int i = 0; i < encpwd.length(); i++) {
			for (int j = 0; j < KEY2.length(); j++) {
				if (encpwd.charAt(i) == KEY2.charAt(j)) {
					encpwds[i] = KEY3.charAt(j);
				}
			}
		}
		return new String(encpwds);
	}

	/**
	 * 密文转成明文
	 */
	public static String sinosoftDecrypt(String password) {
		String decpwd = password;
		char[] decpwds = new char[PWD_LENGTH];
		if (decpwd.length() < PWD_LENGTH) {
			decpwd = new StringBuffer(PWD_LENGTH).append(decpwd).append(
					StringUtils.newString(" ", PWD_LENGTH - decpwd.length())).toString();
		}
		for (int i = 0; i < decpwd.length(); i++) {
			boolean modifyFlag = false;
			for (int j = 0; j < KEY3.length(); j++) {
				if (decpwd.charAt(i) == KEY3.charAt(j)) {
					decpwds[i] = KEY2.charAt(j);
					modifyFlag = true;
				}
			}
			if (!modifyFlag) {
				decpwds[i] = decpwd.charAt(i);
			}
		}
		decpwd = new String(decpwds);
		decpwds = EMPTY8.toCharArray();
		for (int i = 0; i < decpwd.length(); i++) {
			if (decpwd.charAt(i) != ' ') {
				int j = Integer.parseInt(String.valueOf(KEY1.charAt(i)));
				decpwds[j - 1] = decpwd.charAt(i);
			}
		}
		decpwd = new String(decpwds);
		return decpwd;
	}
}