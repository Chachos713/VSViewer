/**
 * Copyright (C) 2014 Kyle and David Diller
 * 
 * This file is part of the VS Viewer 3D.
 * The contents are covered by the terms of the BSD license which
 * is included in the file license.txt, found at the root of the VS Viewer 3D
 * source tree.
 */

import javax.swing.*;
import java.awt.*;
import java.util.*;
import javax.swing.table.*;

public class SpreadSheet {
	// Makes objects to display the data
	private JTable table;
	private JFrame frame;

	// Creates a basic version of the form
	public SpreadSheet() {
		frame = new JFrame();
		frame.setSize(700, 500);
	}

	// Closes the form
	public void close() {
		frame.dispose();
	}

	// Shows the data of all the molecules
	public void newMol(String[] header, boolean[] selected, Molecule[] mols) {
		// Gets what the frame look like before the method
		Point p = frame.getLocation();
		Dimension d = frame.getSize();

		// Clears the frame from memory and recreates it in the location and
		// size as before
		frame.dispose();
		frame = new JFrame("Data");
		frame.setLocation(p);
		frame.setSize(d);

		// Gets the data to display
		Object[][] da = createData(mols, header, selected);

		// Creates the table to show all the data
		table = new JTable(new KTableModel(da, header));
		table.setEnabled(false);
		table.setDragEnabled(false);
		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(
				table.getModel());
		table.setRowSorter(sorter);

		// Allows for the data to be scrolled through
		JScrollPane sp = new JScrollPane(table);
		sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

		// Displays the form and data
		frame.getContentPane()
				.add(BorderLayout.SOUTH,
						new JLabel(
								Double.NEGATIVE_INFINITY
										+ " = The Molecule doesn't have a value for that descriptor"));
		frame.getContentPane().add(BorderLayout.CENTER, sp);
		frame.setVisible(true);
	}

	// Creates the data to show
	public Object[][] createData(Molecule[] mols, String[] header,
			boolean[] selected) {
		// Creates the memory
		Object[][] da = new Object[mols.length + 4][header.length];// +4 is for
																	// the extra
																	// calculated
																	// things

		// Parses through the headers
		for (int j = 0; j < header.length; j++) {
			// Lists for what is selected and non-selected for calculation later
			// on
			ArrayList<Double> select = new ArrayList<Double>();
			ArrayList<Double> nonselect = new ArrayList<Double>();

			// Used for calculations later on
			double ts = 0, tns = 0, ss = 0, sn = 0;
			int nn = 0, ns = 0;
			int i = 0;

			// Goes through each molecule
			for (i = 0; i < mols.length; i++) {
				if (j == 0) {// Gets the name first
					da[i][j] = mols[i].getName();
				} else if (j == 1) {// Tells selected or not
					da[i][j] = selected[i] ? "1" : "0";
				} else {// Other data
					double val = mols[i].containsLabel(header[j]);// Data from
																	// molecule
					da[i][j] = val != Double.NEGATIVE_INFINITY ? Calculator
							.round(val, 2) : Double.NEGATIVE_INFINITY;// Displays
																		// NAN
																		// for
																		// not
																		// available
																		// or
																		// the
																		// value
					if (selected[i]) {// Based on selection
						if (val != Double.NEGATIVE_INFINITY) {// Adds value to
																// selected list
							select.add(val);
							ts += val;
							ns++;
							ss += val * val;
						}
					} else {
						if (val != Double.NEGATIVE_INFINITY) {// Adds to the
																// non-selected
																// list
							nonselect.add(val);
							tns += val;
							nn++;
							sn += val * val;
						}
					}
				}
				System.out.println(header[j] + " : " + da[i][j]);
			}

			if (j <= 1)
				continue;// No Calculations if on the name or selection

			// Does the calculations of the mean and standard deviation of the
			// data
			double meanS = ts / ns;
			double meanN = tns / nn;
			double stdS = ss / ns - meanS * meanS;
			double stdN = sn / nn - meanN * meanN;

			// Rounds the data to two decimal places
			meanN = Calculator.round(meanN, 2);
			meanS = Calculator.round(meanS, 2);
			stdS = Calculator.round(stdS, 2);
			stdN = Calculator.round(stdN, 2);

			// Adds the data to the list
			da[mols.length][j] = meanS;
			da[mols.length + 1][j] = meanN;
			da[mols.length + 2][j] = stdS;
			da[mols.length + 3][j] = stdN;
		}

		// Adds the data for selection and non-selected
		da[mols.length][0] = "Mean - Selected";
		da[mols.length + 1][0] = "Mean - Non-Selected";
		da[mols.length + 2][0] = "Standard Deviation - Selected";
		da[mols.length + 3][0] = "Standard Deviation - Non-Seleced";

		da[mols.length][1] = "1";
		da[mols.length + 1][1] = "0";
		da[mols.length + 2][1] = "0";
		da[mols.length + 3][1] = "0";

		return da;
	}
}
