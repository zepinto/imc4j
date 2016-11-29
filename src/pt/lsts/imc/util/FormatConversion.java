package pt.lsts.imc.util;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.EnumSet;

import javax.xml.bind.DatatypeConverter;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import pt.lsts.imc.annotations.FieldType;
import pt.lsts.imc.msg.Message;
import pt.lsts.imc.msg.MessageFactory;

public class FormatConversion {

	public static Message fromJson(String json) throws ParseException {
		return fromJson(Json.parse(json).asObject());
	}
	
	public static String asJson(Message msg) {
		return asJsonObject(msg, true).toString();
	}

	
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Message fromJson(JsonObject obj) throws ParseException {
		String abbrev = obj.getString("abbrev", "");
		if (abbrev.isEmpty())
			throw new ParseException("Object doesn't have 'abbrev' field", 0);
		Message msg = MessageFactory.create(abbrev);
		if (msg == null)
			throw new ParseException("No message named '"+abbrev+"'", 0);
		
		msg.timestamp = obj.getDouble("timestamp", msg.timestamp);
		msg.src = obj.getInt("src", msg.src);
		msg.src_ent = obj.getInt("src_ent", msg.src_ent);
		msg.dst = obj.getInt("dst", msg.dst);
		msg.dst_ent = obj.getInt("dst_ent", msg.dst_ent);

		for (Field f : msg.getClass().getDeclaredFields()) {
			FieldType type = f.getAnnotation(FieldType.class);
			
			if (type == null)
				continue;
			try {
				if (type.units().equals("Enumerated")) {
					String val = obj.getString(f.getName(),""+f.get(msg));
					Object set = f.getType().getMethod("valueOf", String.class).invoke(null, val);
					f.set(msg, set);
					continue;
				}
				else if (type.units().equals("Bitfield")) {
					String tmp = obj.getString(f.getName(), "[]");
					
					Class enumType = Class.forName(((ParameterizedType)f.getGenericType()).getActualTypeArguments()[0].getTypeName());
					EnumSet<?> enumSet = parseEnumSet(tmp, enumType);
					f.set(msg, enumSet);
					continue;
				}
				else if (type.units().equals("TupleList")) {
					f.set(msg, new TupleList(obj.getString(f.getName(), "")));
					continue;
				} 
				switch (type.type()) {
					case TYPE_PLAINTEXT:
						f.set(msg, obj.getString(f.getName(), ""));
						break;
					case TYPE_FP32:
						f.set(msg, obj.getFloat(f.getName(), 0f));
						break;
					case TYPE_FP64:
						f.set(msg, obj.getDouble(f.getName(), 0d));
						break;
					case TYPE_MESSAGE:
						f.set(msg, fromJson(obj.get(f.getName()).asObject()));
						break;
					case TYPE_RAWDATA:
					{
						String data = obj.getString(f.getName(), "");
						f.set(msg, DatatypeConverter.parseHexBinary(data));
						break;
					}
					case TYPE_MESSAGELIST:
					{
						ArrayList msgs = new ArrayList<>();
						JsonArray array = obj.get(f.getName()).asArray();
						for (JsonValue v : array.values()) {
							msgs.add(fromJson(v.asObject()));
						}
						f.set(msg, msgs);
						break;
					}
					case TYPE_INT16:
					case TYPE_INT32:
					case TYPE_UINT16:
					case TYPE_INT8:
					case TYPE_UINT8:
						f.set(msg, obj.getInt(f.getName(), 0));
						break;
					case TYPE_INT64:
					case TYPE_UINT32:
						f.set(msg, obj.getLong(f.getName(), 0l));
						break;
					default:
						break;
				}
				
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return msg;
	}
	
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
					obj.add(f.getName(), DatatypeConverter.printHexBinary(data));
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
	
	private static <E extends Enum<E>> EnumSet<E> parseEnumSet(String string, Class<E> clazz) {
		EnumSet<E> set = EnumSet.noneOf(clazz);
		if (string == null || string.length() <= 2) {
			return set;
		}
		
		string = string.substring(1);
		string = string.substring(0, string.length()-1);
		
		String[] elements = string.split(",");

		for (String element : elements) {
			element = element.trim();
			for (E type : EnumSet.allOf(clazz)) {
				if (type.name().equals(element)) {
					set.add(type);
					break;
				}
			}
		}
		return set;
	}

}
