package pt.lsts.imc.util;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;

import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.msg.Message;

public class FormatConversion {

	private static JsonObject asJsonObject(Message msg, boolean toplevel) {
		JsonObject obj = new JsonObject();

		if (msg == null)
			return obj;

		obj.add("abbrev", msg.getClass().getSimpleName());

		if (toplevel) {
			obj.add("timestamp", msg.timestamp);
			obj.add("src", msg.src);
			obj.add("src_ent", msg.src_ent);
			obj.add("dst", msg.dst);
			obj.add("dst_ent", msg.dst_ent);
		}

		for (Field f : msg.getClass().getDeclaredFields()) {
			FieldType type = f.getAnnotation(FieldType.class);
			if (type == null)
				continue;
			try {
				if (type.units().equals("Enumerated") || type.units().equals("Bitfield")) {
					obj.add(f.getName(), "" + f.get(msg));
					continue;
				}
				switch (type.type()) {
				case TYPE_PLAINTEXT:
					obj.add(f.getName(), "" + f.get(msg));
					break;
				case TYPE_MESSAGE:
					obj.add(f.getName(), asJsonObject((Message) f.get(msg), false));
					break;
				case TYPE_RAWDATA: {
					byte[] data = (byte[]) f.get(msg);
					StringBuilder sb = new StringBuilder(data.length * 2);
					for (byte b : data)
						sb.append(String.format("%02x", b & 0xff));
					obj.add(f.getName(), sb.toString());
					break;
				}
				case TYPE_MESSAGELIST: {
					@SuppressWarnings("unchecked")
					ArrayList<Message> msgs = (ArrayList<Message>) f.get(msg);
					JsonArray array = new JsonArray();
					if (msgs != null)
						for (Message m : msgs)
							array.add(asJsonObject(m, false));
					obj.add(f.getName(), array);
					break;
				}
				case TYPE_FP32:
					obj.add(f.getName(), (float) f.get(msg));
					break;
				case TYPE_FP64:
					obj.add(f.getName(), (double) f.get(msg));
					break;
				case TYPE_INT16:
				case TYPE_INT8:
				case TYPE_INT32:
				case TYPE_UINT16:
				case TYPE_UINT8:
					obj.add(f.getName(), (int) f.get(msg));
					break;
				case TYPE_UINT32:
				case TYPE_INT64:
					obj.add(f.getName(), (long) f.get(msg));
					break;
				default:
					obj.add(f.getName(), "" + f.get(msg));
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return obj;
	}

	public static String asJson(Message msg) {
		return asJsonObject(msg, true).toString();
	}
}
