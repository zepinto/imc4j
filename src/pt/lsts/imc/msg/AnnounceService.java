package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;
import pt.lsts.imc.util.SerializationUtils;

/**
 * Announcement about the existence of a service.
 */
public class AnnounceService extends Message {
	public static final int ID_STATIC = 152;

	/**
	 * Semicolon separated list of URLs (see :ref:`Announce`).
	 */
	@FieldType(
			type = IMCField.TYPE_PLAINTEXT
	)
	public String service = "";

	/**
	 * Informs about the availability of the service on internal and
	 * external networks.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Bitfield"
	)
	public EnumSet<SERVICE_TYPE> service_type = EnumSet.noneOf(SERVICE_TYPE.class);

	public int mgid() {
		return 152;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			SerializationUtils.serializePlaintext(_out, service);
			long _service_type = 0;
			for (SERVICE_TYPE __service_type : service_type.toArray(new SERVICE_TYPE[0])) {
				_service_type += __service_type.value();
			}
			_out.writeByte((int)_service_type);
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			service = SerializationUtils.deserializePlaintext(buf);
			long service_type_val = buf.get() & 0xFF;
			service_type.clear();
			for (SERVICE_TYPE SERVICE_TYPE_op : SERVICE_TYPE.values()) {
				if ((service_type_val & SERVICE_TYPE_op.value()) == SERVICE_TYPE_op.value()) {
					service_type.add(SERVICE_TYPE_op);
				}
			}
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum SERVICE_TYPE {
		SRV_TYPE_EXTERNAL(0x01l),

		SRV_TYPE_LOCAL(0x02l);

		protected long value;

		SERVICE_TYPE(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static SERVICE_TYPE valueOf(long value) throws IllegalArgumentException {
			for (SERVICE_TYPE v : SERVICE_TYPE.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for SERVICE_TYPE: "+value);
		}
	}
}
