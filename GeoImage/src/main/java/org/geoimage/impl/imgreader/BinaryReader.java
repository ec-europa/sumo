/*
 * 
 */
package org.geoimage.impl.imgreader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class BinaryReader {
	private File binaryFile = null;

	private RandomAccessFile inputStream;
	private static final int MASK = 0xff;

	/**
	 * 
	 * @param binary
	 * @throws FileNotFoundException
	 */
	public BinaryReader(File binary) throws FileNotFoundException {
		binaryFile = binary;
		inputStream = new RandomAccessFile(binaryFile, "r");
	}

	/**
	 * 
	 * @param pathBinary
	 * @throws FileNotFoundException
	 */
	public BinaryReader(String pathBinary) throws FileNotFoundException {
		binaryFile = new File(pathBinary);
		inputStream = new RandomAccessFile(binaryFile, "r");
	}

	/**
	 * 
	 * @param pos
	 * @param len
	 * @param bigEndian
	 * @return
	 * @throws IOException
	 */
	public int readB4(int pos, int len, boolean bigEndian) throws IOException {
		byte[] b = readBytes(pos, len);
		int i = 0;
		/*
		 * int i= (b[0]<<24)&0xff000000| (b[1]<<16)&0x00ff0000| (b[2]<<
		 * 8)&0x0000ff00| (b[3]<< 0)&0x000000ff;
		 */
		if (bigEndian) {
			i = (((b[0] & 0xFF) << 24) + ((b[1] & 0xFF) << 16) + ((b[2] & 0xFF) << 8) + ((b[3] & 0xFF)));
		} else {
			i = (((b[3] & 0xFF) << 24) + ((b[2] & 0xFF) << 16) + ((b[1] & 0xFF) << 8) + ((b[0] & 0xFF)));
		}

		return i;
	}

	/**
	 * 
	 * @param pos
	 * @param len
	 * @return
	 * @throws IOException
	 */
	public float bytearray2float(int pos, int len, boolean bigEndian) throws IOException {
		byte[] b = readBytes(pos, len);
		ByteBuffer buf = ByteBuffer.wrap(b);
		if(bigEndian)
			return buf.order(ByteOrder.BIG_ENDIAN).getFloat();
		else
			return buf.order(ByteOrder.LITTLE_ENDIAN).getFloat();
	}

	/**
	 * convert byte array (of size 4) to float
	 * 
	 * @param test
	 * @return
	 * @throws IOException
	 */
	public float byteArrayToFloat(int pos, int len) throws IOException {
		byte[] b = readBytes(pos, len);
		int bits = 0;
		int i = 0;
		for (int shifter = 3; shifter >= 0; shifter--) {
			bits |= ((int) b[i] & MASK) << (shifter * 8);
			i++;
		}
		return Float.intBitsToFloat(bits);
	}
	
	

	/**
	 * 
	 * @param pos
	 * @param len
	 * @return
	 * @throws IOException
	 */
	public int readB6(int pos, int len) throws IOException {
		byte[] array = readBytes(pos, len);
		return ((int) (array[0] & 0xff) << 40) | ((int) (array[1] & 0xff) << 32) | ((int) (array[2] & 0xff) << 24)
				| ((int) (array[3] & 0xff) << 16) | ((int) (array[4] & 0xff) << 8) | ((int) (array[5] & 0xff));
	}

	/**
	 * 
	 * @param pos
	 * @param len
	 * @return
	 * @throws IOException
	 */
	public int readB3(int pos, int len) throws IOException {
		byte[] array = readBytes(pos, len);
		return ((int) (array[0] & 0xff) << 16) | ((int) (array[1] & 0xff) << 8) | ((int) (array[2] & 0xff));
	}

	/**
	 * 
	 * @param position
	 * @param numBytes
	 * @return
	 * @throws IOException
	 */
	public byte[] readBytes(int position, int numBytes) throws IOException {
		inputStream.seek(position);
		byte[] buff = new byte[numBytes];
		inputStream.read(buff, 0, numBytes);
		return buff;
	}

	/**
	 * 
	 * @param position
	 * @param numBytes
	 * @return
	 * @throws IOException
	 */
	public byte[] readBytes(int position, int numBytes, int offset) throws IOException {
		return readBytes(position + offset, numBytes);
	}

	/**
	 * 
	 * @param position
	 * @param numBytes
	 * @return
	 * @throws IOException
	 */
	public short[] readShort(int startPosition, int x, int y, int offsetx, int offsety) throws IOException {
		int buf_size = offsetx * offsety;
		short[] dd = new short[buf_size];

		inputStream.seek(startPosition);

		for (int i = 0; i < dd.length; i++) {
			dd[i] = (short) inputStream.readUnsignedShort();
		}
		// inputStream.seek(startPosition);
		// int val=inputStream.readUnsignedShort();
		// inputStream.read(buff, 0, numBytes);
		return dd;
	}

	/**
	 * 
	 * @param position
	 * @param numBytes
	 * @return
	 * @throws IOException
	 */
	public void readCompleteImg() throws IOException {
		inputStream.seek(720);
		int val = inputStream.readUnsignedShort();

		while (val >= 0) {
			System.out.println(val);
			val = inputStream.readUnsignedShort();
		}
		// return buff;
	}

	/**
	 * 
	 * @param position
	 * @param numBytes
	 * @return
	 * @throws IOException
	 */
	public byte[] readBytes(int numBytes) throws IOException {
		byte[] buff = new byte[numBytes];
		inputStream.read(buff, 0, numBytes);
		return buff;
	}

	public File getBinaryFile() {
		return binaryFile;
	}

	public void setBinaryFile(File binFile) {
		this.binaryFile = binFile;
	}

	public void dispose() {
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				inputStream = null;
			}
		}
	}

}
