package pt.lsts.imc4j.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.xml.bind.DatatypeConverter;

import pt.lsts.imc4j.annotations.Parameter;

/**
 * @author zp
 *
 */
public class PojoConfig {

	@Parameter
	double test = 50.1;
	
	@Parameter
	double test3 = 50.4;
	
	@Parameter
	String[] tests = {"a", "b"};
	
	private static final String[] validTypes = new String[] { "boolean", "Boolean", "byte", "Byte", "short", "Short",
			"int", "Integer", "long", "Long", "float", "Float", "double", "Double", "String", "String[]", "byte[]" };	
	
	public static void cliParams(Object pojo, String[] arguments) throws Exception {
		setProperties(pojo, asProperties(arguments));
	}
	
	public static Properties asProperties(String[] arguments) throws Exception {
		Properties p = new Properties();
		for (String arg : arguments) {
			if (!arg.startsWith("--") && !arg.toUpperCase().startsWith("-D"))
				throw new Exception("Unrecognized argument: "+arg);

			arg = arg.substring(2);
			if (!arg.contains("=")) {
				p.put(arg, "true");
			}
			else {
				p.put(arg.substring(0, arg.indexOf("=")), arg.substring(arg.indexOf("=")+1));
			}
		}
		return p;
	}
	
	public static <T> T create(Class<T> pojoClass, Properties props) throws Exception {
		T pojo = pojoClass.newInstance();
		setProperties(pojo, props);
		return pojo;
	}
	
	public static <T> T create(Class<T> pojoClass, String[] args) throws Exception {
		T pojo = pojoClass.newInstance();
		setProperties(pojo, asProperties(args));
		return pojo;
	}
	
	public static void setProperties(Object pojo, Properties props) throws Exception {
		validate(pojo);
		ArrayList<Field> fields = loadFields(pojo);
		
		for (Field f : fields) {
			f.setAccessible(true);
			String value = props.getProperty(f.getName());
			try {
				if (value != null)
					setValue(pojo, value, f);
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new Exception("Value for '"+f.getName()+"' ("+f.getType().getSimpleName()+") is invalid: "+value); 
			}
		}		
	}
	
	public static Properties getProperties(Object pojo) throws Exception {
		validate(pojo);
		ArrayList<Field> fields = loadFields(pojo);
		Properties props = new Properties();
		for (Field f : fields) {
			String key = f.getName();
			Object value = f.get(pojo);
			
			if (value == null)
				continue;
			
			if (value instanceof byte[]) {
				value = DatatypeConverter.printHexBinary((byte[])value);
			}
			else if (value instanceof String[]) {
				value = String.join(", ", (String[])value);
			}
			
			props.setProperty(key, String.valueOf(value));
		}
		return props;
	}
	
	private static void validate(Object pojo) throws Exception {
		ArrayList<Field> fields = loadFields(pojo);
		List<String> valid = Arrays.asList(validTypes);

		for (Field f : fields) {
			if (!valid.contains(f.getType().getSimpleName()))
				throw new Exception(
						"Type of parameter '" + f.getName() + "' (" + f.getType().getSimpleName() + ") is not valid.");
		}
	}
	
	private static void setValue(Object pojo, String value, Field f) throws Exception {
		switch (f.getType().getSimpleName()) {
		case "Double":
		case "double":
			f.setDouble(pojo, Double.parseDouble(value));
			break;
		case "Integer":
		case "int":
			f.setInt(pojo, Integer.parseInt(value));
			break;
		case "Float":
		case "float":
			f.setFloat(pojo, Float.parseFloat(value));
			break;
		case "Short":
		case "short":
			f.setShort(pojo, Short.parseShort(value));
			break;
		case "Byte":
		case "byte":
			f.setByte(pojo, Byte.parseByte(value));
		case "Long":
		case "long":
			f.setLong(pojo, Long.parseLong(value));
			break;
		case "Boolean":
		case "boolean":
			f.setBoolean(pojo, Boolean.parseBoolean(value));
			break;
		case "String":
			f.set(pojo, value);
			break;
		case "String[]":
			f.set(pojo, value.split("[, ]+"));
			break;
		case "byte[]":
			f.set(pojo, DatatypeConverter.parseHexBinary(value));
			break;
		default:
			throw new Exception("Invalid parameter type: '"+f.getType().getSimpleName()+"'");
		}
	}
	
	private static ArrayList<Field> loadFields(Object pojo) {
		ArrayList<Field> result = new ArrayList<Field>();
		
		for (Field f : pojo.getClass().getDeclaredFields()) {
			if (f.getAnnotation(Parameter.class) != null) {
				f.setAccessible(true);
				result.add(f);
			}
		}
		
		return result;
	}
	
	public static void writeProperties(Object pojo, File file) {
	    try(BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
	        String nl = System.lineSeparator();
	        String name = pojo.getClass().getSimpleName();
	        name = name.replaceAll("([a-z0-9])([A-Z])", "$1 $2");
	        writer.write("#" + name + " Settings" + nl + nl);
	        for (Field f : pojo.getClass().getDeclaredFields()) {
	            f.setAccessible(true);
	            Parameter p = f.getAnnotation(Parameter.class);
	            if (p != null) {
	                writer.write("#" + p.description() + nl);
	                writer.write(f.getName() + "=" + f.get(pojo) + nl + nl);                    
	            }
	        }
	        System.out.println("Wrote default properties to " + file.getAbsolutePath());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public static void main(String[] args) throws Exception {
		PojoConfig pojo = PojoConfig.create(PojoConfig.class, new String[] {"-Dtest=40"});
		System.out.println(pojo.test);
		System.out.println(PojoConfig.getProperties(pojo));
	}	
}
