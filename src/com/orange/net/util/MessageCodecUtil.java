package com.orange.net.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import com.orange.net.interfaces.IMessage;

//TODO: check time consumption of read and write message op to decide if we should 
//do it on separate threads
public class MessageCodecUtil {

	/*
	 * Safely downcast an IMessage object to TypeMsg TypeMsg must be a sub class
	 * of IMessage
	 */
	@SuppressWarnings("unchecked")
	public static <TypeMsg extends IMessage> TypeMsg convert(IMessage msg) {
		assert (msg != null);
		TypeMsg message = null;
		try {
			message = (TypeMsg) msg;
		} catch (Exception e) {

		}
		return message;
	}

	/**
	 * Writes an IMessage object to the bytes stream.
	 * 
	 * @param message
	 *            the IMessage object to write to the target stream.
	 */
	public static byte[] writeMessage(IMessage message) {
		ByteArrayOutputStream bytesOutput = null;
		ObjectOutputStream objectOutput = null;
		byte[] result = null;

		try {
			bytesOutput = new ByteArrayOutputStream();
			objectOutput = new ObjectOutputStream(bytesOutput);

			objectOutput.writeObject(message);
			result = bytesOutput.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != objectOutput) {
				try {
					objectOutput.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (null != bytesOutput) {
				try {
					bytesOutput.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		byte[] tmp = new byte[result.length + 4];
		ByteBuffer byteBuffer = ByteBuffer.wrap(tmp);
		byteBuffer.putInt(result.length);
		byteBuffer.put(result);
		return tmp;
	}

	/**
	 * Reads an IMessage object from the bytes stream.
	 * 
	 * @param byteArray
	 *            the bytes stream to read.
	 */
	public static IMessage readMessage(byte[] byteArray, int offset, int length) {
		ByteArrayInputStream input = null;
		ObjectInputStream in = null;
		IMessage message = null;

		try {
			input = new ByteArrayInputStream(byteArray, offset, length);
			in = new ObjectInputStream(input);
			message = (IMessage) in.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (null != in) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (null != input) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return message;
	}

	public static IMessage readMessage(byte[] byteArray) {
		return readMessage(byteArray, 0, byteArray.length);
	}
}
