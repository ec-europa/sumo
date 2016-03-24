/*
 * 
 */
package org.geoimage.utils;

import java.io.UnsupportedEncodingException;

/**
 * <p>
 * Byte-Array Conversion Utility Functions.
 * </p>
 *
 */
public class ByteUtils {
	/**
	 * Allow derivatives.
	 */
	protected ByteUtils() {
	}

	/**
	 * Converts a byte array to short value. Equivalent to
	 * byteArrayToShort(paRawBytes, 0, pbBigEndian);
	 *
	 * @param paRawBytes
	 *            the byte array
	 * @param pbBigEndian
	 *            true if the bytes are in Big-endian order; false otherwise
	 *
	 * @return short representation of the bytes
	 */
	public static short byteArrayToShort(byte[] paRawBytes, boolean pbBigEndian) {
		return byteArrayToShort(paRawBytes, 0, pbBigEndian);
	}

	/**
	 * Converts a portion of a byte array with given offset to short value.
	 *
	 * @param paRawBytes
	 *            the byte array
	 * @param piOffset
	 *            offset in the original array to start reading bytes from
	 * @param pbBigEndian
	 *            true if the bytes are in Big-endian order; false otherwise
	 *
	 * @return short representation of the bytes
	 */
	public static short byteArrayToShort(byte[] paRawBytes, int piOffset,
			boolean pbBigEndian) {
		int iRetVal = -1;

		// TODO: revisit this: should we silently add missing byte and should
		// we ingnore excessing bytes?
		if (paRawBytes.length < piOffset + 2)
			return -1;

		int iLow;
		int iHigh;

		if (pbBigEndian) {
			iLow = paRawBytes[piOffset + 1];
			iHigh = paRawBytes[piOffset + 0];
		} else {
			iLow = paRawBytes[piOffset + 0];
			iHigh = paRawBytes[piOffset + 1];
		}

		// Merge high-order and low-order byte to form a 16-bit double value.
		iRetVal = (iHigh << 8) | (0xFF & iLow);

		return (short) iRetVal;
	}

	/**
	 * Converts a byte array to int value. Equivalent to
	 * intArrayToShort(paRawBytes, 0, pbBigEndian);
	 *
	 * @param paRawBytes
	 *            the byte array
	 * @param pbBigEndian
	 *            true if the bytes are in Big-endian order; false otherwise
	 *
	 * @return int representation of the bytes
	 */
	public static int byteArrayToInt(byte[] paRawBytes, boolean pbBigEndian) {
		return byteArrayToInt(paRawBytes, 0, pbBigEndian);
	}

	/**
	 * Converts a portion of a byte array with given offset to int value.
	 *
	 * @param paRawBytes
	 *            the byte array
	 * @param piOffset
	 *            offset in the original array to start reading bytes from
	 * @param pbBigEndian
	 *            true if the bytes are in Big-endian order; false otherwise
	 *
	 * @return int representation of the bytes
	 */
	public static int byteArrayToInt(byte[] paRawBytes, int piOffset,
			boolean pbBigEndian) {
		int iRetVal = -1;

		if (paRawBytes.length < piOffset + 4)
			return iRetVal;

		int iLowest;
		int iLow;
		int iMid;
		int iHigh;

		if (pbBigEndian) {
			iLowest = paRawBytes[piOffset + 3];
			iLow = paRawBytes[piOffset + 2];
			iMid = paRawBytes[piOffset + 1];
			iHigh = paRawBytes[piOffset + 0];
		} else {
			iLowest = paRawBytes[piOffset + 0];
			iLow = paRawBytes[piOffset + 1];
			iMid = paRawBytes[piOffset + 2];
			iHigh = paRawBytes[piOffset + 3];
		}

		// Merge four bytes to form a 32-bit int value.
		iRetVal = (iHigh << 24) | (iMid << 16) | (iLow << 8) | (0xFF & iLowest);

		return iRetVal;
	}

	/**
	 * Converts an int value to a byte array.
	 *
	 * @param piValueToConvert
	 *            the original integer
	 * @param pbBigEndian
	 *            true if the bytes are in Big-endian order; false otherwise
	 *
	 * @return byte[] representation of the int
	 */
	public static byte[] intToByteArray(int piValueToConvert,
			boolean pbBigEndian) {
		byte[] aRetVal = new byte[4];

		byte iLowest;
		byte iLow;
		byte iMid;
		byte iHigh;

		iLowest = (byte) (piValueToConvert & 0xFF);
		iLow = (byte) ((piValueToConvert >> 8) & 0xFF);
		iMid = (byte) ((piValueToConvert >> 16) & 0xFF);
		iHigh = (byte) ((piValueToConvert >> 24) & 0xFF);

		if (pbBigEndian) {
			aRetVal[3] = iLowest;
			aRetVal[2] = iLow;
			aRetVal[1] = iMid;
			aRetVal[0] = iHigh;
		} else {
			aRetVal[0] = iLowest;
			aRetVal[1] = iLow;
			aRetVal[2] = iMid;
			aRetVal[3] = iHigh;
		}

		return aRetVal;
	}

	/**
	 * Converts a byte array to String value. Cleans up non-word characters
	 * along the way.
	 *
	 * Equivalent to byteArrayToString(paRawBytes, 0, paRawBytes.length);
	 *
	 * @param paRawBytes
	 *            the byte array, non-UNICODE
	 *
	 * @return UNICODE String representation of the bytes
	 */
	public static String byteArrayToString(byte[] paRawBytes) {
		return byteArrayToString(paRawBytes, 0, paRawBytes.length);
	}

	/**
	 * Converts a portion of a byte array to String value. Cleans up non-word
	 * characters along the way.
	 *
	 * @param paRawBytes
	 *            the byte array, non-UNICODE
	 * @param piOffset
	 *            offset in the original array to start reading bytes from
	 * @param piLength
	 *            how many bytes of the array paramter to interpret as String
	 *
	 * @return UNICODE String representation of the bytes with trailing garbage
	 *         stripped; "" if array length is less than piOffset + piLength; ""
	 *         if the generatied string begins with garbage
	 */
	public static String byteArrayToString(byte[] paRawBytes, int piOffset,
			int piLength) {
		if (paRawBytes.length < piOffset + piLength)
			return "";

		String oBeautifulString = new String(paRawBytes, piOffset, piLength);
		int i = 0;

		if (oBeautifulString.matches("^\\W") == true)
			oBeautifulString = "";
		else {
			for (i = piOffset; i < piOffset + piLength; i++) {
				if (paRawBytes[i] < 32 || paRawBytes[i] > 128)
					break;
			}

			oBeautifulString = oBeautifulString.substring(0, i - piOffset);
		}

		return oBeautifulString;
	}

	/**
	 * Converts a String value to a byte array in US-ASCII charset.
	 *
	 * Equivalent to stringToByteArray(pstrStringToConvert, "US-ASCII");
	 *
	 * @param pstrStringToConvert
	 *            the original string
	 *
	 * @return null-terminated byte[] representation of the String
	 */
	public static byte[] stringToByteArray(String pstrStringToConvert) {
		return stringToByteArray(pstrStringToConvert, "US-ASCII");
	}

	/**
	 * Attempts to convert a String value to a byte array in specified charset.
	 * If the charset is invalid, returns plain byte-representation of the host
	 * environment.
	 *
	 * @param pstrStringToConvert
	 *            the original string
	 * @param pstrCharSet
	 *            characted set to assume for the original string
	 *
	 * @return null-terminated byte[] representation of the String
	 */
	public static byte[] stringToByteArray(String pstrStringToConvert,
			String pstrCharSet) {
		byte[] aRecordData = null;

		try {
			aRecordData = (pstrStringToConvert + '\0').getBytes(pstrCharSet);
		} catch (UnsupportedEncodingException e) {
			System.err.println("WARNING: " + e);
			aRecordData = (pstrStringToConvert + '\0').getBytes();
		}

		return aRecordData;
	}

	/**
	 * Returns source code revision information.
	 *
	 * @return revision string
	 */
	public static String getMARFSourceCodeRevision() {
		return "$Revision: 1.6 $";
	}

	/**
	 * byte2Int method - extracts ints from byte array
	 *
	 * @param inData
	 * @param byteSwap
	 * @return
	 */
	public static final int[] byte2Int(byte[] inData, boolean byteSwap) {
		int j = 0, value;
		int length = inData.length / 4;
		int[] outData = new int[length];
		if (!byteSwap)
			for (int i = 0; i < length; i++) {
				j = i * 4;
				outData[i] = (((inData[j] & 0xff) << 24)
						+ ((inData[j + 1] & 0xff) << 16)
						+ ((inData[j + 2] & 0xff) << 8) + ((inData[j + 3] & 0xff) << 0));
			}
		else
			for (int i = 0; i < length; i++) {
				j = i * 4;
				outData[i] = (((inData[j + 3] & 0xff) << 24)
						+ ((inData[j + 2] & 0xff) << 16)
						+ ((inData[j + 1] & 0xff) << 8) + ((inData[j] & 0xff) << 0));
			}

		return outData;
	}

}

// EOF
