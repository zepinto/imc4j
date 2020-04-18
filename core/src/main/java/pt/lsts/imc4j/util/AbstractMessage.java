package pt.lsts.imc4j.util;

import java.util.ArrayList;

import pt.lsts.imc4j.annotations.FieldType;
import pt.lsts.imc4j.annotations.IMCField;
import pt.lsts.imc4j.msg.Message;

public class AbstractMessage {

	public IMCField getTypeOf(String field) {
		try {
			return getClass().getField(field).getAnnotation(FieldType.class).type();
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public String getString(String field) {
		try {
			return ""+getClass().getField(field).get(this);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public Double getDouble(String field) {
		try {
			return Double.parseDouble(getString(field));
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public Float getFloat(String field) {
		try {
			return getDouble(field).floatValue();
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public Long getLong(String field) {
		try {
			return getDouble(field).longValue();
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public Integer getInteger(String field) {
		try {
			return getDouble(field).intValue();
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public byte[] getRawData(String field) {
		try {
			return (byte[]) getClass().getField(field).get(this);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Message> T getMessage(String field) {
		try {
			return (T) getClass().getField(field).get(this);
		}
		catch (Exception e) {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Message> ArrayList<T> getMsgList(String field) {
		try {
			return (ArrayList<T>) getClass().getField(field).get(this);
		}
		catch (Exception e) {
			return null;
		}
	}	
}
