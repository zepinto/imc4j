package pt.lsts.imc.msg;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
import java.lang.String;
import java.nio.ByteBuffer;
import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.annotations.IMCField;

/**
 * Camera Zoom.
 */
public class CameraZoom extends Message {
	public static final int ID_STATIC = 300;

	/**
	 * The identification number of the destination camera.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int id = 0;

	/**
	 * Absolute zoom level.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8
	)
	public int zoom = 0;

	/**
	 * The zoom action to perform.
	 */
	@FieldType(
			type = IMCField.TYPE_UINT8,
			units = "Enumerated"
	)
	public ACTION action = ACTION.values()[0];

	public String abbrev() {
		return "CameraZoom";
	}

	public int mgid() {
		return 300;
	}

	public byte[] serializeFields() {
		try {
			ByteArrayOutputStream _data = new ByteArrayOutputStream();
			DataOutputStream _out = new DataOutputStream(_data);
			_out.writeByte(id);
			_out.writeByte(zoom);
			_out.writeByte((int)(action != null? action.value() : 0));
			return _data.toByteArray();
		}
		catch (IOException e) {
			e.printStackTrace();
			return new byte[0];
		}
	}

	public void deserializeFields(ByteBuffer buf) throws IOException {
		try {
			id = buf.get() & 0xFF;
			zoom = buf.get() & 0xFF;
			action = ACTION.valueOf(buf.get() & 0xFF);
		}
		catch (Exception e) {
			throw new IOException(e);
		}
	}

	public enum ACTION {
		ACTION_ZOOM_RESET(0l),

		ACTION_ZOOM_IN(1l),

		ACTION_ZOOM_OUT(2l),

		ACTION_ZOOM_STOP(3l);

		protected long value;

		ACTION(long value) {
			this.value = value;
		}

		long value() {
			return value;
		}

		public static ACTION valueOf(long value) throws IllegalArgumentException {
			for (ACTION v : ACTION.values()) {
				if (v.value == value) {
					return v;
				}
			}
			throw new IllegalArgumentException("Invalid value for ACTION: "+value);
		}
	}
}
