/**
 * Copyright (C) 2014 Kyle and David Diller
 * 
 * This file is part of the VS Viewer 3D.
 * The contents are covered by the terms of the BSD license which
 * is included in the file license.txt, found at the root of the VS Viewer 3D
 * source tree.
 */

import javax.swing.table.AbstractTableModel;

@SuppressWarnings("serial")
public class KTableModel extends AbstractTableModel {
	// List of data and header
	private Object[][] data;
	private String[] header;

	// Creates with data and header
	public KTableModel(Object[][] da, String[] head) {
		data = da;
		header = head;
	}

	@Override
	// Returns number of columns
	public int getColumnCount() {
		return header.length;
	}

	@Override
	// Returns number of rows
	public int getRowCount() {
		return data.length;
	}

	// Returns name of a column
	public String getColumnName(int col) {
		return header[col];
	}

	// Returns the value at a specific location
	public Object getValueAt(int row, int col) {
		return data[row][col];
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	// Returns class at the location
	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}
}
