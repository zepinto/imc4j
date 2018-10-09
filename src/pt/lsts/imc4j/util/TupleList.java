package pt.lsts.imc4j.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Set;

public class TupleList {

	private LinkedHashMap<String, String> list = new LinkedHashMap<>();
	
	public TupleList() {		
	}
	
	public TupleList(String value) {
		reset(value);
	}
	
	public void reset(String values) {
		list.clear();
		if (values == null || values.isEmpty())
			return;		
		
		for (String s : values.split(";")) {
			String[] parts = s.split("=");
			list.put(parts[0].trim(), parts[1].trim());
		}
	}
	
	public Set<String> keys() {
		return list.keySet();
	}
	
	@Override
	public String toString() {
		ArrayList<String> entries = new ArrayList<>();
		for (Entry<String, String> e : list.entrySet())
			entries.add(e.getKey()+"="+e.getValue());				
		return String.join(";", entries);
	}

	public void set(String name, Object value) {
		if (value != null)
			list.put(name, ""+value);
		else
			list.put(name, null);
	}
	
	public String keyFor(Object value) {
		value = ""+value;
		for (Entry<String, String> e : list.entrySet())
			if (e.getValue().equals(value))
				return e.getKey();
		
		return null;
	}
	
	public String get(Object key) {
		return get(""+key);
	}
	
	public String get(String name) {
		return list.get(name);
	}
	
	public Integer getInt(String name) {
		try {
			return Integer.parseInt(get(name));
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public Double getDouble(String name) {
		try {
			return Double.parseDouble(get(name));
		}
		catch (Exception e) {
			return null;
		}
	}
	
	public int size() {
		return list.size();
	}
	
	public boolean hasKey(String key) {
	    return list.containsKey(key);
	}
	
	public TupleList diff(TupleList other) {
	      TupleList result = new TupleList();
	      for (Entry<String, String> e : list.entrySet())
	      {
	          if(other.hasKey(e.getKey()) 
	                  && !other.get(e.getKey()).equals(e.getValue())) {
	              result.set(e.getKey(), other.get(e.getKey()));
	          }
//	          else {
//	              System.err.println(e.getKey()+" with no changes");
//	          }
	      }
        return result;
	    
	}
}
