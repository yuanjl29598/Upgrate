package com.yjl.hotupdate.utils;

import android.text.TextUtils;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
	private static final int HIGHT_F = 0xf0;
	private static final int LOW_F = 0xf;
	private static final int HALF_BYTE = 4;
	
	protected static char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
	
	protected static MessageDigest messageDigest = null;
	
	static {
		try {
			messageDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			Log.e("StringUtil", "md5 初始化失败");
			e.printStackTrace();
		}
	}

	public static boolean emailFormat(String email) {
		boolean tag = true;
		String pattern1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		Pattern pattern = Pattern.compile(pattern1);
		Matcher mat = pattern.matcher(email);
		if (!mat.find()) {
			tag = false;
		}
		return tag;
	}
	
	 /**
     * 检查字符参数是否为空。
     * 
     * @param args 字符串参数列表
     * @return boolean
     */
    public static boolean isStringParamEmpty(String...args) {
        if (null == args) {
            return true;
        }

        int length = args.length;

        for (int i = 0; i < length; i++) {
            if (TextUtils.isEmpty(args[i])) {
                return true;
            }
        }

        return false;
    }

	public static boolean isBlank(String str) {
		if (str == null || str.equals("")) {
			return true;
		} else if (str.trim().equals("")) {
			return true;
		}

		return false;
	}

	public static boolean isNumericByAscii(String str) {
		for (int i = str.length(); --i >= 0;) {
			int chr = str.charAt(i);
			if (chr < 48 || chr > 57)
				return false;
		}
		return true;
	}

	public static boolean isNumericByCharDigit(String str) {
		for (int i = str.length(); --i >= 0;) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public static boolean isNumericByPattern(String str) {
		if (str.matches("\\d*")) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isNumericByPattern2(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		return pattern.matcher(str).matches();
	}

	public static int stringToInt(String str) {
		int num = 0;

		try {
			num = Integer.parseInt(str);
		} catch (NumberFormatException e) {
			num = 0;
		}

		return num;
	}

	public static long stringToLong(String str) {
		long num = 0;

		try {
			num = Long.parseLong(str);
		} catch (NumberFormatException e) {
			num = 0;
		}

		return num;
	}

	public static String strReplace(String inputStr, String findStr,
			String replaceStr) {
		int n = 0;
		String outputStr = inputStr;
		do {
			n = inputStr.indexOf(findStr, n);
			if (n == -1)
				break;
			outputStr = inputStr.substring(0, n) + replaceStr
					+ inputStr.substring(n + findStr.length());
			n += replaceStr.length();
			inputStr = outputStr;
		} while (true);
		return outputStr.substring(0, outputStr.length());
	}

	/**
	 * 对字符串做MD5操作。
	 * 
	 * @param plainText
	 *            明文
	 * @return hash后的密文
	 */
	public static String MD5(String plainText) {
		try {
			byte[] bytes = plainText.getBytes();
			messageDigest.update(bytes);
			return bufferToHex(messageDigest.digest());
		} catch (Exception e) {
			Log.e("Md5", e.getMessage(), e);
		}
		return "";
	}

	private static String bufferToHex(byte[] bytes) {
		return bufferToHex(bytes, 0, bytes.length);
	}

	private static String bufferToHex(byte[] bytes, int m, int n) {
		StringBuffer stringbuffer = new StringBuffer(2 * n);
		int k = m + n;
		for (int l = m; l < k; l++) {
			appendHexPair(bytes[l], stringbuffer);
		}
		return stringbuffer.toString();
	}

	private static void appendHexPair(byte bt, StringBuffer stringbuffer) {
		char c0 = hexDigits[(bt & HIGHT_F) >> HALF_BYTE];
		char c1 = hexDigits[bt & LOW_F];
		stringbuffer.append(c0);
		stringbuffer.append(c1);
	}
	
	public static boolean contains(String[] arr, String key) {
		if(arr == null || arr.length == 0
				|| key == null || key.trim().length() == 0)
			return false;
		for(int i=0;i<arr.length;i++)
			if(key.equals(arr[i]))
				return true;
		return false;
	}
}
