package de.xehpuk.unicodenamer.format;

import java.text.ParseException;

/**
 * @author xehpuk <xehpuk@netbeans.org>
 */
public class EditFormatter extends CodePointFormatter {
	@Override
	public String valueToString(final Object value) throws ParseException {
		final int currentValue = (int) value;
		if (currentValue == 0) {
			return "";
		}
		return Integer.toHexString(currentValue).toUpperCase();
	}
}