package de.xehpuk.unicodenamer.format;

import de.xehpuk.unicodenamer.Utils;

import javax.swing.*;
import java.text.ParseException;

/**
 * @author xehpuk <xehpuk@netbeans.org>
 */
public abstract class CodePointFormatter extends JFormattedTextField.AbstractFormatter {
	private final UnicodeFilter filter = new UnicodeFilter();
	
	@Override
	public Object stringToValue(final String text) throws ParseException {
		try {
			if (text.isEmpty()) {
				return 0;
			}
			return Math.min(Utils.MAX_CODE_POINT, Integer.parseInt(text, 16));
		} catch (final NumberFormatException nfe) {
			throw new ParseException(nfe.getLocalizedMessage(), 0);
		}
	}
	
	@Override
	protected UnicodeFilter getDocumentFilter() {
		return filter;
	}
}