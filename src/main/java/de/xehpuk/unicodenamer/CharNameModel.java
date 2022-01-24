package de.xehpuk.unicodenamer;

import java.awt.Desktop;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 * @author xehpuk <xehpuk@netbeans.org>
 */
/*
 * fireTableDataChanged() as a workaround for hiding of duplicates.
 */
public class CharNameModel extends AbstractTableModel {
	private static final String CHARACTER_ENCODING = "UTF-8";
	public static final int INDEX_COLUMN = 0;
	public static final int CHARACTER_COLUMN = 1;
	public static final int CODE_POINT_COLUMN = 2;
	public static final int UTF_8_COLUMN = 3;
	public static final int UTF_16_COLUMN = 4;
	public static final int SCRIPT_COLUMN = 5;
	public static final int BLOCK_COLUMN = 6;
	public static final int NAME_COLUMN = 7;
	public static final int FILE_FORMAT_COLUMN = 8;
	public static final int GOOGLE_COLUMN = 9;
	public static final int WIKIPEDIA_COLUMN = 10;
	
	private static URI fileFormat(final CharName charName) {
		return URI.create(String.format("http://www.fileformat.info/info/unicode/char/%x/index.htm", charName.getCodePoint()));
	}
	
	private static URI google(final CharName charName) {
		final String name = charName.getName();
		if (name == null) {
			return null;
		}
		try {
			return URI.create(String.format("https://www.google.com/search?q=%%22%s%%22", URLEncoder.encode(name, CHARACTER_ENCODING)));
		} catch (final UnsupportedEncodingException uee) {
			throw new RuntimeException(uee);
		}
	}
	
	private static URI wikipedia(final CharName charName) {
		try {
			return URI.create(String.format("https://de.wikipedia.org/w/index.php?search=%s", URLEncoder.encode(charName.getCodePointChars(), CHARACTER_ENCODING)));
		} catch (final UnsupportedEncodingException uee) {
			throw new RuntimeException(uee);
		}
	}
	
	private static int firstUriColumn() {
		return Math.min(Math.min(FILE_FORMAT_COLUMN, GOOGLE_COLUMN), WIKIPEDIA_COLUMN);
	}
	
	private final List<CharName> charNames = new ArrayList<>();
	private final String[] columnNames = {"#", "Zeichen", "Codepoint", CHARACTER_ENCODING, "UTF-16", "Script", "Block", "Name", "FileFormat", "Google", "Wikipedia"};

	@Override
	public int getRowCount() {
		return charNames.size();
	}

	@Override
	public int getColumnCount() {
		return Desktop.isDesktopSupported() ? columnNames.length : firstUriColumn();
	}

	@Override
	public String getColumnName(final int column) {
		return columnNames[column];
	}

	@Override
	public Class<?> getColumnClass(final int columnIndex) {
		switch (columnIndex) {
			case INDEX_COLUMN:
			case CODE_POINT_COLUMN:
				return Integer.class; // int.class does not implement Comparable
			case UTF_8_COLUMN:
				return byte[].class;
			case UTF_16_COLUMN:
				return byte[].class;
			case CHARACTER_COLUMN:
			case NAME_COLUMN:
				return String.class;
			case SCRIPT_COLUMN:
				return Character.UnicodeScript.class;
			case BLOCK_COLUMN:
				return Character.UnicodeBlock.class;
			case FILE_FORMAT_COLUMN:
			case GOOGLE_COLUMN:
			case WIKIPEDIA_COLUMN:
				return URI.class;
			default:
				throw new IndexOutOfBoundsException("columnIndex == " + columnIndex);
		}
	}

	@Override
	public boolean isCellEditable(final int rowIndex, final int columnIndex) {
		return columnIndex >= firstUriColumn();
	}

	@Override
	public Object getValueAt(final int rowIndex, final int columnIndex) {
		final CharName charName = charNames.get(rowIndex);
		switch (columnIndex) {
			case INDEX_COLUMN:
				return rowIndex + CHARACTER_COLUMN;
			case CHARACTER_COLUMN:
				return charName.getCodePointChars();
			case UTF_8_COLUMN:
				return charName.getUtf8Bytes();
			case UTF_16_COLUMN:
				return charName.getUtf16Bytes();
			case CODE_POINT_COLUMN:
				return charName.getCodePoint();
			case SCRIPT_COLUMN:
				return charName.getUnicodeScript();
			case BLOCK_COLUMN:
				return charName.getUnicodeBlock();
			case NAME_COLUMN:
				return charName.getName();
			case FILE_FORMAT_COLUMN:
				return fileFormat(charName);
			case GOOGLE_COLUMN:
				return google(charName);
			case WIKIPEDIA_COLUMN:
				return wikipedia(charName);
			default:
				throw new IndexOutOfBoundsException("columnIndex == " + columnIndex);
		}
	}
	
	public void add(final CharName charName) {
		add(charNames.size(), charName);
	}
	
	public void add(final int index, final CharName charName) {
		charNames.add(index, charName);
//		fireTableRowsInserted(index, index);
		fireTableDataChanged();
	}
	
	public boolean addAll(final Collection<? extends CharName> charNames) {
		return addAll(this.charNames.size(), charNames);
	}

	public boolean addAll(final int index, final Collection<? extends CharName> charNames) {
		final boolean changed = this.charNames.addAll(index, charNames);
		if (changed) {
//			fireTableRowsInserted(index, index + charNames.size() - 1);
			fireTableDataChanged();
		}
		return changed;
	}
	
	public void clear() {
		final int row = charNames.size();
		if (row > 0) {
			charNames.clear();
			fireTableRowsDeleted(0, row - 1);
		}
	}

	public CharName get(final int index) {
		return charNames.get(index);
	}
	
	public CharName remove(final int index) {
		final CharName charName = charNames.remove(index);
//		fireTableRowsDeleted(index, index);
		fireTableDataChanged();
		return charName;
	}
	
	public List<CharName> remove(final int index, final int length) {
		final List<CharName> removed = new ArrayList<>(length);
		for (int i = 0; i < length; i++) {
			removed.add(charNames.remove(index));
		}
//		fireTableRowsDeleted(index, index + length - 1);
		fireTableDataChanged();
		return removed;
	}
	
	public boolean isFirstOccurrence(final int index) {
		final CharName charName = get(index);
		for (int i = 0; i < index; i++) {
			if (charNames.get(i).equals(charName)) {
				return false;
			}
		}
		return true;
	}
}