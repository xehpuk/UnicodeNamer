package de.xehpuk.unicodenamer.format;

import de.xehpuk.unicodenamer.Utils;
import java.awt.Toolkit;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * @author xehpuk <xehpuk@netbeans.org>
 */
public class UnicodeFilter extends DocumentFilter {
	private final int maxLength = Integer.toHexString(Utils.MAX_CODE_POINT).length();
	
	@Override
	public void insertString(final FilterBypass fb, final int offset, final String string, final AttributeSet attr) throws BadLocationException {
		replace(fb, offset, 0, string, attr);
	}

	@Override
	public void replace(final FilterBypass fb, final int offset, final int length, final String text, final AttributeSet attrs) throws BadLocationException {
		final String s = text == null ? "" : text.toUpperCase();
		final int sLen = s.length();
		final int diff = sLen - length;
		final int dLen = fb.getDocument().getLength();
		if (offset + sLen > maxLength || !s.matches("\\p{XDigit}*")) {
			Toolkit.getDefaultToolkit().beep();
		} else {
			super.replace(fb, offset, (dLen + diff > maxLength) ? sLen : length, s, attrs);
		}
	}
}