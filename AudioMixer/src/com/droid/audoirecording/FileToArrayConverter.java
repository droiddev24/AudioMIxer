package com.droid.audoirecording;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

public class FileToArrayConverter {

	/**
	 * Dealing with big endian streams
	 * @param byte0
	 * @param byte1
	 * @return a short with the two bytes swapped
	 */
	private static short swapBytes(byte byte0, byte byte1){
		return (short)((byte1 & 0xff) << 8 | (byte0 & 0xff));
	}

	/**
	 * From file to byte[] array
	 * @param sample 
	 * @param swap should swap bytes?
	 * @return
	 * @throws IOException
	 */
	public static byte[] sampleToByteArray(FileInputStream sample, boolean swap) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		BufferedInputStream bis = new BufferedInputStream(sample);
		int BUFFERSIZE = 4096;
		byte[] buffer = new byte[BUFFERSIZE];
		while(bis.read(buffer) != - 1){
			baos.write(buffer);
		}
		byte[] outputByteArray = baos.toByteArray();
		bis.close();
		baos.close();

		if(swap){
			for(int i=0; i < outputByteArray.length - 1; i=i+2){
				byte byte0 = outputByteArray[i];
				outputByteArray[i] = outputByteArray[i+1];
				outputByteArray[i+1] = byte0;
			}
		}

		return outputByteArray;
	}

	/**
	 * Read a file and returns its contents as array of shorts
	 * @param sample the sample file
	 * @param swap true if we should swap the bytes of short (reading a little-endian file), false otherwise (reading a big-endian file)
	 * @return
	 * @throws IOException
	 */
	public static short[] sampleToShortArray(FileInputStream sample, boolean swap) throws IOException{
		short[] outputArray = new short[((int)sample.getChannel().size()/2)+1];


		byte[] outputByteArray = sampleToByteArray(sample,false);


		for(int i=0, j=0; i < outputArray.length; i+= 2, j++){
			if(swap){
				outputArray[j] = swapBytes(outputByteArray[i], outputByteArray[i + 1]);
			}
			else{
				outputArray[j] = swapBytes(outputByteArray[i + 1], outputByteArray[i]);
			}
		}
		return outputArray;
	}
}
