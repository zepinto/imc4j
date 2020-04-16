package pt.lsts.imcwan;

import java.lang.reflect.Field;

/**
 * This interface will expose all class fields in the printObject() method default implementation
 * @author zp
 *
 */
public interface Printable {

	public default String printObject() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append(" {\n");
		for (Field f : getClass().getFields()) {
			try {
				f.setAccessible(true);
				builder.append("  \"").append(f.getName()).append("\": ").append(f.get(this)).append("\n");	
			}
			catch (Exception e) {
			}	
		}
		builder.append("}\n");
		return builder.toString();
	}
}
