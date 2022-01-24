package de.xehpuk.unicodenamer;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;

/**
 * The ButtonColumn class provides a renderer and an editor that looks like a
 * JButton. The renderer and editor will then be used for a specified column in
 * the table.
 *
 * The button can be invoked by a mouse click or by pressing the space bar when
 * the cell has focus. Optionally a mnemonic can be set to invoke the button.
 * When the button is invoked the underlying URI will be browsed.
 * @author Rob Camick
 */
public class TableUriBrowseButton extends AbstractCellEditor implements TableCellRenderer, TableCellEditor, MouseListener {
	private JTable table;
	private int column;
	private final Icon icon;
	private int mnemonic;
	private final JButton renderButton;
	private final JButton editButton;
	private Object editorValue;
	private boolean isButtonColumnEditor;

	/**
	 * Create the TableUriBrowseButton to be used as a renderer and editor.
	 */
	public TableUriBrowseButton(final Icon icon) {
		this.icon = icon;
		renderButton = new JButton();
		renderButton.setOpaque(false);
		editButton = new JButton();
		editButton.setOpaque(false);
		editButton.setFocusPainted(false);
		editButton.addActionListener(e -> {
			final int editingRow = table.getEditingRow();
			if (editingRow >= 0) {
				fireEditingStopped();
				try {
					Desktop.getDesktop().browse((URI) table.getValueAt(editingRow, column));
				} catch (final IOException ieo) {
					Toolkit.getDefaultToolkit().beep();
				}
			}
		});
	}

	public int getMnemonic() {
		return mnemonic;
	}

	/**
	 * The mnemonic to activate the button when the cell has focus
	 *
	 * @param mnemonic the mnemonic
	 */
	public void setMnemonic(final int mnemonic) {
		this.mnemonic = mnemonic;
		renderButton.setMnemonic(mnemonic);
		editButton.setMnemonic(mnemonic);
	}

	@Override
	public JButton getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row, final int column) {
		this.table = table;
		this.column = column;
		editButton.setBackground(table.getSelectionBackground());
		editButton.setText(null);
		editButton.setIcon(icon);
		editButton.setEnabled(value != null);
		this.editorValue = value;
		return editButton;
	}

	@Override
	public Object getCellEditorValue() {
		return editorValue;
	}

	@Override
	public JButton getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
		this.table = table;
		this.column = column;
		renderButton.setBackground(isSelected ? table.getSelectionBackground() : UIManager.getColor("Button.background"));
		renderButton.setText(null);
		renderButton.setIcon(icon);
		renderButton.setEnabled(value != null);
		return renderButton;
	}

	@Override
	public void mouseClicked(MouseEvent e) {}
	
	/*
	 *  When the mouse is pressed the editor is invoked. If you then drag
	 *  the mouse to another cell before releasing it, the editor is still
	 *  active. Make sure editing is stopped when the mouse is released.
	 */
	@Override
	public void mousePressed(final MouseEvent e) {
		if (table.isEditing() && table.getCellEditor() == TableUriBrowseButton.this) {
			isButtonColumnEditor = true;
		}
	}

	@Override
	public void mouseReleased(final MouseEvent e) {
		if (isButtonColumnEditor && table.isEditing()) {
			table.getCellEditor().stopCellEditing();
		}
		isButtonColumnEditor = false;
	}

	@Override
	public void mouseEntered(MouseEvent e) {}

	@Override
	public void mouseExited(MouseEvent e) {}
}