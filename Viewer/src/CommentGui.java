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
import java.awt.event.*;
import java.util.*;

public class CommentGui implements ActionListener {
	// Fields of molecule, comments, and display objects
	Molecule mol;
	JFrame frmComb;
	JTextField comment;
	JTextArea data;
	JList<String> master, molecule;
	private DefaultListModel<String> masterCom, moleculeCom;
	ArrayList<String> masterList;
	JButton newComment, masterComment, delete, compare3D, compare2d;
	int longMaster;
	MolDraw drawer;

	// Closes the window
	public void close() {
		frmComb.setVisible(false);
	}

	// Creates the form for the CommentGui
	public CommentGui(SimpleGui sg) {
		// Panel to draw 2D of molecule
		drawer = new MolDraw();

		// Creates the frame
		frmComb = new JFrame();
		frmComb.setSize(1100, 210);

		// Panel for the data, comments, and compare buttons
		JPanel comb = new JPanel();
		comb.setLayout(new BorderLayout());

		// Comment JPanel
		JPanel com = new JPanel();
		com.setLayout(new BorderLayout());

		// Text Area to display the data
		data = new JTextArea(9, 15);
		data.setEditable(false);

		// Text box to add a new comment
		comment = new JTextField(15);

		// Buttons for comparison
		compare3D = new JButton("Compare 3D");
		compare3D.addActionListener(new Compare(sg, true, false));
		compare2d = new JButton("Compare 2D");
		compare2d.addActionListener(new Compare(sg, false, false));

		// Buttons for comments (Add and Delete)
		newComment = new JButton("Add");
		newComment.addActionListener(this);
		masterComment = new JButton("Add");
		masterComment.addActionListener(this);
		delete = new JButton("Delete");
		delete.addActionListener(this);

		// JList and List Models for the master list and comments from the
		// molecule
		masterCom = new DefaultListModel<String>();
		moleculeCom = new DefaultListModel<String>();
		master = new JList<String>(masterCom);
		molecule = new JList<String>(moleculeCom);

		// Methods for setting up the JList
		master.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		master.setVisibleRowCount(5);
		master.setFixedCellHeight(16);

		molecule.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		molecule.setVisibleRowCount(5);
		molecule.setFixedCellHeight(16);

		// Scroll bars for the two JList, and data
		JScrollPane scroller = new JScrollPane(master);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroller.setPreferredSize(new Dimension(15 * 15, 16 * 8));

		JScrollPane scroller1 = new JScrollPane(molecule);
		scroller1
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroller1
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroller1.setPreferredSize(new Dimension(15 * 15, 16 * 8));

		JScrollPane scroller2 = new JScrollPane(data);
		scroller2
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroller2
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

		// JPanel for the data side of the form
		JPanel panelData = new JPanel();
		panelData.add(scroller2);

		// Box to hold the two compare buttons
		Box yax = Box.createVerticalBox();

		yax.add(compare3D);
		yax.add(compare2d);

		// Combine the compare buttons with the data
		panelData.add(yax);

		// Panel for adding the comment from the text box
		JPanel newCom = new JPanel();
		newCom.add(comment);
		newCom.add(newComment);

		// Panel for buttons to delete and add from the master list
		JPanel option = new JPanel();
		option.setLayout(new BoxLayout(option, BoxLayout.Y_AXIS));
		option.add(masterComment);
		option.add(delete);

		// Combines all the objects for the comment side of the form
		com.add(BorderLayout.WEST, scroller);
		com.add(BorderLayout.CENTER, option);
		com.add(BorderLayout.EAST, scroller1);
		com.add(BorderLayout.SOUTH, newCom);

		// Combines everything together
		comb.add(BorderLayout.WEST, com);
		comb.add(BorderLayout.CENTER, drawer);
		comb.add(BorderLayout.EAST, panelData);

		// Adds it to the form
		frmComb.add(comb);
	}

	// Sets the list of all comments already added
	public void setMaster(ArrayList<String> c) {
		masterList = c;

		longMaster = 0;
		masterCom.clear();
		for (String s : c) {// Adds the comments from the list to the JList
			masterCom.add(0, s);

			if (s.length() > longMaster)
				longMaster = s.length();
		}
		SimpleGui.changed = false;
	}

	// Changes what Molecule is shown
	public void newMol(Molecule m) {
		int longMol = 0;
		mol = m;

		// Used to get all the comments of the molecule
		ArrayList<String> empty = new ArrayList<String>();
		m.addComments(empty);

		// Clears the JList of comments from previous molecule's comments
		moleculeCom.clear();

		int i = 0;

		// Add the comments to the JList
		for (String s : empty) {
			moleculeCom.add(i, s);
			i++;

			if (s.length() > longMol)
				longMol = s.length();
		}

		// Display all the data
		data.setText(m.getData());

		// Display everything
		drawer.repaint();

		frmComb.setTitle(m.getName());
		frmComb.setVisible(true);
		compare3D.setEnabled(m.is3D());
	}

	// Button clicks for adding and removing comments
	public void actionPerformed(ActionEvent e) {
		switch (((JButton) (e.getSource())).getText().charAt(0)) {// Gets the
																	// first
																	// Character
																	// in the
																	// text of
																	// the
																	// button
		case 'A':// Add a comment from either the master list or the text box
			if (master.getSelectedIndex() < 0 && e.getSource() == masterComment)// End
																				// method
																				// if
																				// from
																				// master
																				// list
																				// and
																				// no
																				// comment
																				// is
																				// selected
				return;

			if (e.getSource() == masterComment) {// From master list
				ArrayList<String> empty = new ArrayList<String>();
				mol.addComments(empty);// Gets all the comments

				if (!empty.contains((String) master.getSelectedValue())) {// Check
																			// if
																			// it's
																			// not
																			// already
																			// there
					mol.addComment((String) master.getSelectedValue());// Adds
																		// the
																		// comment
																		// to
																		// the
																		// molecule
																		// and
																		// the
																		// JList
					moleculeCom.add(mol.getNumCom() - 1,
							(String) master.getSelectedValue());
				}
			} else {// From text box
				ArrayList<String> empty = new ArrayList<String>();
				mol.addComments(empty);// Gets all the comments

				if (comment.getText().trim().equals(""))
					return;// End if there is nothing there

				if (!empty.contains(comment.getText().toLowerCase())) {// Checks
																		// if
																		// it's
																		// not
																		// already
																		// there
					mol.addComment(comment.getText().toLowerCase());// Adds the
																	// comment
																	// to the
																	// JList and
																	// molecule
					moleculeCom.add(mol.getNumCom() - 1, comment.getText()
							.toLowerCase());

					if (!masterList.contains(comment.getText().toLowerCase())) {// Checks
																				// if
																				// it's
																				// in
																				// the
																				// master
																				// lsit
						masterList.add(comment.getText().toLowerCase());// Adds
																		// it to
																		// the
																		// master
																		// list
																		// and
																		// JList
						masterCom.add(0, comment.getText().toLowerCase());
					}
				}
			}

			break;
		case 'D':// Delete a comment
			if (molecule.getSelectedIndex() < 0)// Check if there is a comment
												// selected
				return;

			mol.removeComment(molecule.getSelectedIndex());// Remove comment
			newMol(mol);// Refresh the form
		}

		SimpleGui.changed = true;
	}

	@SuppressWarnings("serial")
	// Draws the Molecule on the CommentGui
	public class MolDraw extends JPanel {
		public void paintComponent(Graphics g) {
			g.setColor(Color.white);
			g.fillRect(0, 0, 10000, 10000);

			MolGridView mgv = new MolGridView();
			mgv.Open(900, 900, 200, 150, MolGridViewGui.borderx,
					MolGridViewGui.bordery);

			mgv.DrawMol(g, mol, 0, 0.0, 200.0, 150.0, 0.0, false);
		}
	}
}
