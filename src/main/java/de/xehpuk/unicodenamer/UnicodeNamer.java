package de.xehpuk.unicodenamer;

import de.xehpuk.unicodenamer.format.DisplayFormatter;
import de.xehpuk.unicodenamer.format.EditFormatter;
import de.xehpuk.unicodenamer.format.NullFormatter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class UnicodeNamer {
	private final static BufferedImage ICON_UNICODE = Utils.loadIcon("icon.png");
	private final static BufferedImage ICON_FILE_FORMAT = Utils.loadIcon("fileFormat.png");
	private final static BufferedImage ICON_GOOGLE = Utils.loadIcon("google.png");
	private final static BufferedImage ICON_WIKIPEDIA = Utils.loadIcon("wikipedia.png");
	
	public static void main(final String... args) {
		if (args.length > 0) {
			final boolean unique = args.length > 1 && args[1].equalsIgnoreCase("-u");
			final List<CharName> charNames = charNames(args[0]);
			for (final CharName cn : unique ? new LinkedHashSet<>(charNames) : charNames) {
				System.out.printf("%s: %s, %s%n", cn.getCodePointChars(), cn.getCodePointString(), cn.getName());
			}
		} else {
			SwingUtilities.invokeLater(() -> {
				UIManager.put("swing.boldMetal", Boolean.FALSE);
				new UnicodeNamer(1050, 500).start();
			});
		}
	}

	public static List<CharName> charNames(final String s) {
		final int m = s.length();
		final List<CharName> charNames = new ArrayList<>(m);
		for (int i = 0; i < m;) {
			final int codePoint = s.codePointAt(i);
			charNames.add(CharName.valueOf(codePoint));
			i += Character.charCount(codePoint);
		}
		return charNames;
	}
	
	private final JFrame frame = new JFrame("Unicode-Namen");
	private final JLabel inputLabel = new JLabel("Unicode-String");
	private final JTextComponent inputTextComponent = new JTextField(60);
	private final JLabel codePointLabel = new JLabel("Hex-Codepoint");
	private final JSpinner codePointSpinner = new JSpinner(new SpinnerNumberModel(0, Character.MIN_CODE_POINT, Utils.MAX_CODE_POINT, 1));
	private final JButton insertCodePointButton = new JButton("Einf\u00fcgen");
	private final JCheckBox uniqueCheckBox = new JCheckBox("Duplikate ausblenden");
	private	final JCheckBox filterCheckBox = new JCheckBox("Regex-Filter");
	private final JTextComponent filterTextComponent = new JTextField(20);
	private final Color textComponentForeground = filterTextComponent.getForeground();
	private final CharNameModel model = new CharNameModel();
	private final JTable output = new JTable(model);
	private final RowFilter<TableModel, Integer> uniqueFilter = new UniqueRowFilter();
	private final RowFilter<TableModel, Integer> regexFilter = new RegexRowFilter();

	public UnicodeNamer(final int width, final int height) {
		inputTextComponent.getDocument().addDocumentListener(new DocumentListener() {
			private int oldLength = documentToModelLength();
			
			@Override
			public void insertUpdate(final DocumentEvent e) {
				final int newLength = documentToModelLength();
				inputTextInserted(e.getOffset(), e.getLength());
				oldLength = newLength;
			}

			@Override
			public void removeUpdate(final DocumentEvent e) {
				final int newLength = documentToModelLength();
				inputTextRemoved(e.getOffset(), oldLength - newLength);
				oldLength = newLength;
			}

			@Override
			public void changedUpdate(final DocumentEvent e) {}
		});
		
		final JFormattedTextField textField = ((JSpinner.NumberEditor) codePointSpinner.getEditor()).getTextField();
		textField.setColumns(2 + Utils.MAX_LENGTH);
		final NullFormatter nullFormatter = new NullFormatter();
		final DisplayFormatter displayFormatter = nullFormatter.getFormatter();
		final EditFormatter editFormatter = new EditFormatter();
		textField.setFormatterFactory(new DefaultFormatterFactory(displayFormatter, displayFormatter, editFormatter, nullFormatter));
		
		final ActionListener codePointListener = e -> inputTextComponent.replaceSelection(CharName.valueOf((int) codePointSpinner.getValue()).getCodePointChars());
		textField.addActionListener(codePointListener);
		insertCodePointButton.addActionListener(codePointListener);
		
		uniqueCheckBox.addItemListener(e -> uniqueCheckBoxChanged());
		
		filterCheckBox.addItemListener(e -> filterCheckBoxChanged());
		
		filterTextComponent.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(final DocumentEvent e) {
				filterTextComponentChanged();
			}

			@Override
			public void removeUpdate(final DocumentEvent e) {
				filterTextComponentChanged();
			}

			@Override
			public void changedUpdate(final DocumentEvent e) {
				filterTextComponentChanged();
			}
		});
		
		final JComponent contentPane = new JPanel(new BorderLayout());
		final int padding = 2;
		final JComponent headerComponent = new JPanel(new GridLayout(2, 0, 0, padding));
		headerComponent.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Eingabe"));
		final JComponent inputComponent = Box.createHorizontalBox();
		inputComponent.add(inputLabel);
		inputComponent.add(Box.createHorizontalStrut(padding));
		inputComponent.add(inputTextComponent);
		headerComponent.add(inputComponent);
		final JComponent filterComponent = Box.createHorizontalBox();
		filterComponent.add(codePointLabel);
		filterComponent.add(Box.createHorizontalStrut(padding));
		filterComponent.add(codePointSpinner);
		filterComponent.add(insertCodePointButton);
		filterComponent.add(Box.createHorizontalStrut(padding));
		filterComponent.add(uniqueCheckBox);
		filterComponent.add(Box.createHorizontalStrut(padding));
		filterComponent.add(filterCheckBox);
		filterComponent.add(filterTextComponent);
		filterComponent.add(Box.createHorizontalGlue());
		headerComponent.add(filterComponent);
		contentPane.add(headerComponent, BorderLayout.NORTH);
		
		output.setAutoCreateRowSorter(true);
		final TableColumnModel columnModel = output.getColumnModel();
		final TableColumn indexColumn = columnModel.getColumn(CharNameModel.INDEX_COLUMN);
		indexColumn.setMaxWidth(40);
		indexColumn.setMinWidth(30);
		final TableColumn charColumn = columnModel.getColumn(CharNameModel.CHARACTER_COLUMN);
		charColumn.setMaxWidth(60);
		charColumn.setMinWidth(30);
		DefaultTableCellRenderer charRenderer = new DefaultTableCellRenderer();
		charRenderer.setHorizontalAlignment(JLabel.CENTER);
		charColumn.setCellRenderer(charRenderer);
		final TableColumn unicodeColumn = columnModel.getColumn(CharNameModel.CODE_POINT_COLUMN);
		unicodeColumn.setMaxWidth(75);
		unicodeColumn.setMinWidth(75);
		unicodeColumn.setCellRenderer(new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
				final JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				label.setHorizontalAlignment(JLabel.CENTER);
				label.setText(model.get(table.convertRowIndexToModel(row)).getCodePointString());
				return label;
			}
		});
		final DefaultTableCellRenderer utfCellRenderer = new DefaultTableCellRenderer() {
			@Override
			public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
				final JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				final byte[] bytes = (byte[]) value;
				label.setHorizontalAlignment(JLabel.CENTER);
				label.setText(Utils.hex(bytes));
				return label;
			}
		};
		final TableColumn utf8Column = columnModel.getColumn(CharNameModel.UTF_8_COLUMN);
		utf8Column.setMaxWidth(80);
		utf8Column.setMinWidth(30);
		utf8Column.setCellRenderer(utfCellRenderer);
		final TableColumn utf16Column = columnModel.getColumn(CharNameModel.UTF_16_COLUMN);
		utf16Column.setMaxWidth(80);
		utf16Column.setMinWidth(40);
		utf16Column.setCellRenderer(utfCellRenderer);
		@SuppressWarnings("unchecked")
		final DefaultRowSorter<? extends TableModel, Integer> rowSorter = (DefaultRowSorter<? extends TableModel, Integer>) output.getRowSorter();
		final DefaultRowSorter<CharNameModel, Integer> newRowSorter = new DefaultRowSorter<>() {
			private final Comparator<byte[]> bytesComparator = (bs, bs2) -> {
				final int bsLength = bs.length;
				final int bs2Length = bs2.length;
				for (int i = 0, m = Math.min(bsLength, bs2Length); i < m; i++) {
					final int bc = Integer.compare(bs[i] & 0xFF, bs2[i] & 0xFF);
					if (bc == 0) {
						continue;
					}
					// System.out.printf("%d: %s, %s%n", bc, Utils.hex(bs), Utils.hex(bs2));
					return bc;
				}
				return Integer.compare(bsLength, bs2Length);
			};

			private final Comparator<Character.UnicodeBlock> blockComparator = (b, b2) -> String.CASE_INSENSITIVE_ORDER.compare(b.toString(), b2.toString());

			{
				setModelWrapper(new ModelWrapper<>() {
					@Override
					public CharNameModel getModel() {
						return model;
					}

					@Override
					public int getColumnCount() {
						return model.getColumnCount();
					}

					@Override
					public int getRowCount() {
						return model.getRowCount();
					}

					@Override
					public Object getValueAt(final int row, final int column) {
						return model.getValueAt(row, column);
					}

					@Override
					public Integer getIdentifier(final int row) {
						return row;
					}
				});
			}

			@Override
			public Comparator<?> getComparator(final int column) {
				switch (column) {
					case CharNameModel.UTF_8_COLUMN:
						return bytesComparator;
					case CharNameModel.BLOCK_COLUMN:
						return blockComparator;
					default:
						return rowSorter.getComparator(column);
				}
			}
		};
		newRowSorter.setRowFilter(RowFilter.andFilter(Arrays.asList(uniqueFilter, regexFilter)));
		output.setRowSorter(newRowSorter);
		columnModel.getColumn(CharNameModel.SCRIPT_COLUMN).setPreferredWidth(30);
		columnModel.getColumn(CharNameModel.BLOCK_COLUMN).setPreferredWidth(125);
		columnModel.getColumn(CharNameModel.NAME_COLUMN).setPreferredWidth(215);
		if (Desktop.isDesktopSupported()) {
			final TableUriBrowseButton fileFormatButtonColumn = new TableUriBrowseButton(new ImageIcon(ICON_FILE_FORMAT));
			fileFormatButtonColumn.setMnemonic(KeyEvent.VK_F);
			output.addMouseListener(fileFormatButtonColumn);
			final TableColumn fileFormatColumn = columnModel.getColumn(CharNameModel.FILE_FORMAT_COLUMN);
			fileFormatColumn.setCellEditor(fileFormatButtonColumn);
			fileFormatColumn.setCellRenderer(fileFormatButtonColumn);
			fileFormatColumn.setMaxWidth(60);
			fileFormatColumn.setMinWidth(60);
			final TableUriBrowseButton googleButtonColumn = new TableUriBrowseButton(new ImageIcon(ICON_GOOGLE));
			googleButtonColumn.setMnemonic(KeyEvent.VK_G);
			output.addMouseListener(googleButtonColumn);
			final TableColumn googleColumn = columnModel.getColumn(CharNameModel.GOOGLE_COLUMN);
			googleColumn.setCellEditor(googleButtonColumn);
			googleColumn.setCellRenderer(googleButtonColumn);
			googleColumn.setMaxWidth(60);
			googleColumn.setMinWidth(60);
			final TableUriBrowseButton wikipediaButtonColumn = new TableUriBrowseButton(new ImageIcon(ICON_WIKIPEDIA));
			wikipediaButtonColumn.setMnemonic(KeyEvent.VK_W);
			output.addMouseListener(wikipediaButtonColumn);
			final TableColumn wikipediaColumn = columnModel.getColumn(CharNameModel.WIKIPEDIA_COLUMN);
			wikipediaColumn.setCellEditor(wikipediaButtonColumn);
			wikipediaColumn.setCellRenderer(wikipediaButtonColumn);
			wikipediaColumn.setMaxWidth(60);
			wikipediaColumn.setMinWidth(60);
		}
		final JComponent outputScroller = new JScrollPane(output);
		outputScroller.setPreferredSize(new Dimension(width, 100));
		outputScroller.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLoweredBevelBorder(), "Ausgabe"));
		contentPane.add(outputScroller, BorderLayout.CENTER);
		
		inputLabel.setLabelFor(inputTextComponent);
		codePointLabel.setLabelFor(codePointSpinner);
		
		inputLabel.setDisplayedMnemonic('U');
		codePointLabel.setDisplayedMnemonic('H');
		insertCodePointButton.setMnemonic('E');
		uniqueCheckBox.setMnemonic('D');
		filterCheckBox.setMnemonic('R');
		
		uniqueCheckBoxChanged();
		filterCheckBoxChanged();
		
		final JScrollPane contentPaneScroller = new JScrollPane(contentPane);
		frame.setContentPane(contentPaneScroller);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setIconImage(ICON_UNICODE);
		frame.pack();
		frame.setSize(frame.getWidth(), height);
		frame.setLocationRelativeTo(null);
	}
	
	public void start() {
		frame.setVisible(true);
	}
	
	private int documentToModelLength() {
		final int length = inputTextComponent.getDocument().getLength();
		try {
			return documentToModelLength(length);
		} catch (final BadLocationException ble) {
			throw new IndexOutOfBoundsException(String.format("%s, offset=%d, length=%d", ble, 0, length));
		}
	}
	
	private int documentToModelLength(final int offset) throws BadLocationException {
		return documentToModelLength(0, offset);
	}
	
	private int documentToModelLength(final int offset, final int length) throws BadLocationException {
		final String text = inputTextComponent.getText(offset, length);
		return text.codePointCount(0, length);
	}
	
	/**
	 * @param offset from document
	 * @param length from document
	 */
	private void inputTextInserted(final int offset, final int length) {
		try {
			final String text = inputTextComponent.getText(offset, length);
			final List<CharName> charNames = charNames(text);
			model.addAll(documentToModelLength(offset), charNames);
		} catch (final BadLocationException ble) {
			throw new IndexOutOfBoundsException(String.format("%s, offset=%d, length=%d", ble, offset, length));
		}
		inputTextChanged();
	}
	
	/**
	 * @param offset from document
	 * @param length from model
	 */
	private void inputTextRemoved(final int offset, final int length) {
		try {
			model.remove(documentToModelLength(offset), length);
		} catch (final BadLocationException ble) {
			throw new IndexOutOfBoundsException(String.format("%s, offset=%d, length=%d", ble, offset, length));
		}
		inputTextChanged();
	}
	
	private void inputTextChanged() {
		textComponentChanged();
	}
	
	private void uniqueCheckBoxChanged() {
		model.fireTableDataChanged();
	}
	
	private void filterCheckBoxChanged() {
		filterTextComponent.setEnabled(filterCheckBox.isSelected());
		model.fireTableDataChanged();
	}
	
	private void filterTextComponentChanged() {
		model.fireTableDataChanged();
		textComponentChanged();
	}
	
	private void textComponentChanged() {
		frame.getContentPane().revalidate();
		frame.getContentPane().repaint();
	}
	
	private class UniqueRowFilter extends RowFilter<TableModel, Integer> {
		private final Set<CharName> addedCharNames = new HashSet<>();
		
		@Override
		public boolean include(final Entry<? extends TableModel, ? extends Integer> entry) {
			if (!uniqueCheckBox.isSelected()) {
				return true;
			}
			final int index = entry.getIdentifier();
			if (index == 0) {
				addedCharNames.clear();
			}
			// System.out.printf("%d: %s%n", index, model.get(index).getCodePointChars());
			// return model.isFirstOccurrence(index);
			return addedCharNames.add(model.get(index));
		}
	}
	
	private class RegexRowFilter extends RowFilter<TableModel, Integer> {
		private Pattern pattern;
		
		@Override
		public boolean include(final Entry<? extends TableModel, ? extends Integer> entry) {
			if (!filterCheckBox.isSelected()) {
				return true;
			}
			if (0 == entry.getIdentifier()) {
				filterTextComponent.setForeground(textComponentForeground);
				filterTextComponent.setToolTipText(null);
				if (pattern == null || !pattern.pattern().equals(filterTextComponent.getText())) {
					try {
						pattern = Pattern.compile(filterTextComponent.getText(), Pattern.CASE_INSENSITIVE);
					} catch (final PatternSyntaxException pse) {
						filterTextComponent.setForeground(Color.RED);
						final String lineBreak = "<br>";
						filterTextComponent.setToolTipText("<html>" + Utils.encodeHtml(pse.getLocalizedMessage())
								.replaceFirst(System.lineSeparator(), lineBreak + "<code>")
								.replaceFirst(System.lineSeparator(), lineBreak + "<font color=\"red\"><b>")
								.replaceAll("\\s", "&nbsp;") + "</b></font></code></html>");
					}
				}
			}
			return pattern == null
					|| pattern.matcher(entry.getStringValue(CharNameModel.SCRIPT_COLUMN)).find()
					|| pattern.matcher(entry.getStringValue(CharNameModel.BLOCK_COLUMN)).find()
					|| pattern.matcher(entry.getStringValue(CharNameModel.NAME_COLUMN)).find();
		}
	}
}