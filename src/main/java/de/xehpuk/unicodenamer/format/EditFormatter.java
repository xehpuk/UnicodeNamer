package de.xehpuk.unicodenamer.format;

/**
 * @author xehpuk <xehpuk@netbeans.org>
 */
public class EditFormatter extends CodePointFormatter {
	@Override
	public String valueToString(final Object value) {
		final int currentValue = (int) value;
		if (currentValue == 0) {
			return "";
		}
		return Integer.toHexString(currentValue).toUpperCase();
	}
}