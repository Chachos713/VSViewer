/**
 * Copyright (C) 2014 Kyle and David Diller
 * 
 * This file is part of the VS Viewer 3D.
 * The contents are covered by the terms of the BSD license which
 * is included in the file license.txt, found at the root of the VS Viewer 3D
 * source tree.
 */

import javax.swing.*;
import java.awt.event.*;
import java.util.*;

public class Compare implements ActionListener {
	// Values for 3D, molecules to be used, and SimpleGui
	private SimpleGui sg;
	private boolean d3;
	private boolean molPop;

	// Gives values to the fields
	public Compare(SimpleGui s, boolean d3, boolean sp) {
		sg = s;
		this.d3 = d3;
		molPop = sp;
	}

	@SuppressWarnings("static-access")
	// Handles the button click
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JButton) {// Make sure it's a button
			// Gets the Plot2D and MolGridView to show the molecules in
			// selection later
			Plot2D plot = sg.plot;
			MolGridViewGui mvg = sg.mgvg;

			// Gets the molecule in the pop-up window
			int m = plot.molshow;

			// If called from the comment viewer use this molecule
			if (!molPop)
				m = sg.comments.mol.index;

			// List of all the molecules to be used for sorting
			ArrayList<Molecule> mols = new ArrayList<Molecule>();

			// Sets sorting for later
			if (d3)
				Molecule.sortBy = "3D";
			else
				Molecule.sortBy = "2D";

			// Goes through the molecules
			for (int i = 0; i < plot.data.mol.length; i++) {
				double p = 0;// Percent

				// Makes the calculation of percent similar
				if (d3)
					p = Molecule.compare(plot.data.mol[m], plot.data.mol[i]) * 100;
				else
					p = Molecule.compare2D(plot.data.mol[m], plot.data.mol[i]) * 100;

				// Rounds the percent to two decimals
				p = Calculator.round(p, 2);

				// Sets the percent that will be compared
				if (d3)
					plot.data.mol[i].setPercent(p);
				else
					plot.data.mol[i].setPercent2D(p);

				// Adds the molecule to mols
				mols.add(plot.data.mol[i]);

				// Adds the data
				if (d3)
					plot.data.mol[i].addSim(plot.data.mol[m].getName(), p);
				else
					plot.data.mol[i].addSim2D(plot.data.mol[m].getName(), p);
			}

			// Sorts the list of molecules
			Collections.sort(mols);

			// List of molecules to use
			ArrayList<Molecule> mole = new ArrayList<Molecule>();

			// Gets the top 100 similar
			for (int i = 0; i < 100 && i < mols.size(); i++) {
				mole.add(mols.get(i));
				int ind = mols.get(i).index;
				sg.plot.data.selected[ind] = true;
			}

			// Does the stuff for the spreadsheet
			String[] head = new String[sg.plot.data.header.length + 2];
			head[0] = "Names";
			head[1] = "Selected";

			int i = 2;

			for (String g : sg.plot.data.header) {
				head[i] = g;
				i++;
			}

			// Displays on MolGridViewGui
			if (sg.views[1].isSelected())
				mvg.go(mole);

			// Displays on the Plot2D
			if (sg.views[0].isSelected())
				sg.sheet.newMol(head, sg.plot.data.selected, sg.plot.data.mol);

			// Redraws the Plot2D
			sg.frame.repaint();

			// Update things to do with the header
			plot.data.updateHeader();
			sg.addItems();

			// Says the file has changed
			sg.changed = true;
		}
	}
}
