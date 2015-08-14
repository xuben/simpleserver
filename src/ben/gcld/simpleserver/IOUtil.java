package ben.gcld.simpleserver;

import java.io.IOException;
import java.io.InputStream;

public class IOUtil {
	
	/**
	 * read a given length string from the input stream
	 * 
	 * @param in
	 * @param length
	 * @return
	 * @throws IOException
	 */
	public static String readString(InputStream in, int length) throws IOException {
		byte[] byteArray = new byte[length];
		in.read(byteArray);
		return new String(byteArray);
	}
	
	/**
	 * read an integer value from the input stream
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static int readInt(InputStream in) throws IOException {
		byte[] byteArray = new byte[4];
		in.read(byteArray);
		return (byteArray[0]&0xff) << 24 
				| (byteArray[1]&0xff) << 16 
				| (byteArray[2]&0xff) << 8 
				| (byteArray[3]&0xff) << 0;
	}
	
	/**
	 * write integer value to byte array
	 * @param value
	 * @return
	 */
	public static byte[] writeInt(int value) {
		byte[] byteArray = new byte[4];
		return writeInt(value, byteArray, 0);
	}
	
	/**
	 * write integer value to byte array
	 * @param value
	 * @param byteArray
	 * @param begin
	 * @return
	 */
	public static byte[] writeInt(int value, byte[] byteArray, int begin) {
		byteArray[begin++] = (byte) (value >>> 24);
		byteArray[begin++] = (byte) (value >>> 16);
		byteArray[begin++] = (byte) (value >>> 8);
		byteArray[begin] = (byte) (value >>> 0);
		return byteArray;
	}
}
