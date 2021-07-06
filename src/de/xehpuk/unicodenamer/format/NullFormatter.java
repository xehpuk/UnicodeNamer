package de.xehpuk.unicodenamer.format;

import java.text.ParseException;

/**
 * @author xehpuk <xehpuk@netbeans.org>
 */
public class NullFormatter extends CodePointFormatter {
	private final DisplayFormatter formatter = new DisplayFormatter();
	
	@Override
	public String valueToString(final Object value) throws ParseException {
		return formatter.valueToString(0);
	}

	public DisplayFormatter getFormatter() {
		return formatter;
	}
}