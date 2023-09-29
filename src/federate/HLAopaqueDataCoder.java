package federate;

import java.nio.ByteBuffer;
import java.util.Iterator;

import hla.rti1516e.encoding.DecoderException;
import hla.rti1516e.encoding.EncoderFactory;
import hla.rti1516e.encoding.HLAopaqueData;
import static java.nio.ByteOrder.LITTLE_ENDIAN;

public class HLAopaqueDataCoder {
	private HLAopaqueData coder;
	
	public HLAopaqueDataCoder(EncoderFactory factory) {
		coder = factory.createHLAopaqueData();
	}
	
	public static byte[] byteArray(int[] intArray) {
		int times = Integer.SIZE / Byte.SIZE;
		byte[] bytes = new byte[intArray.length * times];
		for(int i = 0; i < intArray.length; i++) {
			getByteBuffer(bytes, i, times).putInt(intArray[i]);
		}
		return bytes;
	}
	
	public static int[] intArray(byte[] byteArray) {
		int times = Integer.SIZE / Byte.SIZE;
		int[] ints = new int[byteArray.length / times];
		for(int i = 0; i < ints.length; i++) {
			ints[i] = getByteBuffer(byteArray, i, times).getInt();
		}
		return ints;
	}
	
	private static ByteBuffer getByteBuffer(byte[] bytes, int index, int times) {
		return ByteBuffer.wrap(bytes, index * times, times).order(LITTLE_ENDIAN);
	}
	
	public byte[] encode(int[] data) {
		byte[] b = byteArray(data);
		coder.setValue(b);
		return coder.toByteArray();
	}
	
	public byte[] decode(byte[] byteValue) throws DecoderException {
		coder.decode(byteValue);
		return coder.getValue();
	}
	
	

}
