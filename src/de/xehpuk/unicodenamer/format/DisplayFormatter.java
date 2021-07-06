package de.xehpuk.unicodenamer.format;

import de.xehpuk.unicodenamer.CharName;
import java.text.ParseException;

/**
 * @author xehpuk <xehpuk@netbeans.org>
 */
public class DisplayFormatter extends CodePointFormatter {
	@Override
	public String valueToString(final Object value) throws ParseException {
		try {
			return CharName.valueOf((int) value).getCodePointString();
		} catch (final IllegalArgumentException iae) {
			throw new ParseException(iae.getLocalizedMessage(), 0);
		}
	}
}