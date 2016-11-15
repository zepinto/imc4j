package pt.lsts.imc.msg;

import java.io.IOException;
import java.lang.Exception;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.util.SerializationUtils;

public abstract class Message {
	public static final short SYNC_WORD = (short)0xFE54;

	/**
	 * The synchronization number marks the beginning of a packet.
	 * It denotes the packet API version and can be used to deduce
	 * the byte order of the sending host.
	 * It encodes value 0xFE[major][minor] where [major] equals the
	 * major version number of the protocol and [minor] equals the
	 * minor version of the protocol.
	 * The packet recipient is responsible for the correct
	 * interpretation of the synchronization number and byte order
	 * conversions.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public final int sync = 0xFE54;

	/**
	 * The time when the packet was sent, as seen by the packet
	 * dispatcher. The number of seconds is represented in Universal
	 * Coordinated Time (UCT) in seconds since Jan 1, 1970 using IEEE
	 * double precision floating point numbers.
	 */
	@FieldType(
			type = IMCField.TYPE_FP64,
			units = "s"
	)
	public double timestamp = System.currentTimeMillis() / 1000.0;

	/**
	 * The Source IMC system ID.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int src = 0xFFFF;

	/**
	 * The entity generating this message at the source address.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int src_ent = 0xFF;

	/**
	 * The Destination IMC system ID.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT16
	)
	public int dst = 0xFFFF;

	/**
	 * The entity that should process this message at the destination
	 * address.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int dst_ent = 0xFF;

	public int size() {
		return serializeFields().length;
	}

	/**
	 * The identification number of the message */
	public abstract int mgid();

	/**
	 * Serialize this message's payload */
	public abstract byte[] serializeFields();

	/**
	 * Deserialize this message's payload */
	public abstract void deserializeFields(ByteBuffer buf) throws IOException;

	/**
	 * Serialize this message */
	public byte[] serialize() {
		return SerializationUtils.serializeMessage(this);
	}

	/**
	 * Read a message from a byte array */
	public static Message deserialize(byte[] data) throws Exception {
		return SerializationUtils.deserializeMessage(data);
	}
}
