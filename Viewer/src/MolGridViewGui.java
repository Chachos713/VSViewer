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
import java.io.File;

public class MolGridViewGui {
	// Fields to help make the display of 2D structures
	JFrame frame;
	JButton buttonNext, buttonPrevious;
	JMenuItem sort, save, close;
	int plot_width, plot_height, mol_width = 300, mol_height = 150;
	static int borderx = 10, bordery = 10;

	// Stores all the molecules
	MolGridView mgv;

	// Forms for 3D viewing and call methods for saving
	AstexViewer viewer3d;
	SimpleGui sg;

	// Closes the form when closing the program, opening a new file, etc.
	public void close() {
		frame.setVisible(false);
	}

	// Creates the form and gives values to the MolGridView and the 3D viewer
	public MolGridViewGui(AstexViewer viewer) {
		// Assigns the values to the MolGridView and the 3D viewer
		mgv = new MolGridView();
		viewer3d = viewer;

		// Creates the frame and adds some listeners to it for different
		// functions
		frame = new JFrame("2D Structure Grid Viewer");
		frame.addMouseListener(new myMouseListener());
		frame.addComponentListener(new Resizer());

		// Buttons and Menu Items to save, close, scroll and sort
		buttonNext = new JButton("Next");
		buttonPrevious = new JButton("Previous");
		save = new JMenuItem("Save");
		sort = new JMenuItem("Sort");
		close = new JMenuItem("Close");

		// Adds the actions to the buttons and menu items
		buttonNext.addActionListener(new NextButtonListener());
		buttonPrevious.addActionListener(new PreviousButtonListener());
		sort.addActionListener(new Sort());
		save.addActionListener(new Save());

		close.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});

		// Creates the menu bar and menus
		JMenuBar bar = new JMenuBar();
		JMenu tools = new JMenu("Tools");
		JMenu file = new JMenu("File");

		// Adds the buttons to the panel at the bottom
		JPanel panelButton = new JPanel();
		panelButton.add(buttonPrevious);
		panelButton.add(buttonNext);

		// Adds the menu items to them menus
		file.add(save);
		tools.add(sort);
		file.add(close);

		// Adds the menus to the menu bar
		bar.add(file);
		bar.add(tools);

		// Adds the items to the frame
		frame.getContentPane().add(BorderLayout.SOUTH, panelButton);
		frame.getContentPane().add(BorderLayout.CENTER, mgv);
		frame.getContentPane().add(BorderLayout.NORTH, bar);

		// Calculates and sets the frame size
		frame.setSize(3 * mol_width + 2 * borderx + 10, 5 * mol_height
				+ bordery * 2);

		// Sets-up the MolGridView for drawing the molecules
		plot_width = 900;
		plot_height = 750;

		mgv.Open(plot_width, plot_height, mol_width, mol_height, borderx,
				bordery);
	}

	// Sets the value of the SimpleGui
	public void sg(SimpleGui s) {
		sg = s;
		viewer3d = sg.av;
	}

	// Sets what molecules are to be shown
	public void go(ArrayList<Molecule> mol) {
		mgv.setMol(mol);
		frame.repaint();
		frame.setVisible(true);
	}

	// Goes to the next set of molecules
	class NextButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			mgv.Next();
			frame.repaint();
		}
	}

	// Goes to the previous set of molecules
	class PreviousButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			mgv.Previous();
			frame.repaint();
		}
	}

	// Handles the mouse click to know which molecule is selected
	class myMouseListener implements MouseListener {
		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		public void mouseClicked(MouseEvent e) {
			int selected = mgv.getSelected(e.getX(), e.getY());
			Molecule mol = mgv.mol.get(selected);

			// Displays the comment viewer
			if (e.getClickCount() >= 0 && SimpleGui.views[3].isSelected()) {
				SimpleGui.comments.newMol(mol);
			}

			// Displays the 3D viewer
			if (mol.is3D() && SimpleGui.views[4].isSelected())
				viewer3d.setSubj(mol);
		}
	}

	// Allows for the form to draw molecules that would be viewed on the form as
	// the size changes
	public class Resizer implements ComponentListener {
		public void componentResized(ComponentEvent e) {
			plot_width = (frame.getSize().width) - (2 * borderx);
			plot_height = (frame.getSize().height) - (2 * bordery)
					- buttonNext.getSize().height;
			int n = (plot_width / 300) * (plot_height / 150);
			mgv.resize(n, plot_width / 300, plot_width, plot_height);

			mgv.repaint();
		}

		public void componentHidden(ComponentEvent e) {
		}

		public void componentShown(ComponentEvent e) {
		}

		public void componentMoved(ComponentEvent e) {
		}
	}

	// Allows the user to save the selected set of molecules
	public class Save implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				// Decides what to save the file as
				String choice = "";
				if (!mgv.mol.get(0).is3D()) {// If not a 3D file, only allow the
												// 2D SDF
					choice = "2D sdf";
				} else {// Let the user select what to save as
						// Creates the JList and ListModel
					DefaultListModel<String> adder = new DefaultListModel<String>();
					JList<String> choices = new JList<String>(adder);
					choices.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					choices.setSelectedIndex(0);
					choices.setVisibleRowCount(5);
					choices.setFixedCellHeight(16);

					// Creates the scroll bar
					JScrollPane scroller = new JScrollPane(choices);
					scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
					scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

					// Adds the choices to the list
					adder.add(0, "2D sdf");
					adder.add(1, "3D sdf");
					adder.add(2, "vsv");

					// Displays the list of choices
					int decis = JOptionPane.showConfirmDialog(frame, scroller,
							"Search by Comment", JOptionPane.OK_CANCEL_OPTION);

					if (decis == JOptionPane.CANCEL_OPTION)
						return;// Continue as long as they didn't click CANCEL

					// Gets the selected choice
					choice = (String) choices.getSelectedValue();
				}

				// Creates the file chooser
				JFileChooser fc = new JFileChooser(sg.saver);
				SDFileFilterSDF sdf = new SDFileFilterSDF();
				SDFileFilterVSV vsv = new SDFileFilterVSV();
				fc.setAcceptAllFileFilterUsed(false);

				// Gets what type of file type selected
				char type = choice.charAt(0);

				// Used to check that the right extension is put on the file
				String end = ".sdf";// Assumes SDF because of 2/3 is going to be
									// SDF at worst

				// Sets the filter to use
				if (type == 'v') {
					end = ".vsv";// If VSV is selected, put the extension as
									// .vsv
					fc.setFileFilter(vsv);
				} else {
					fc.setFileFilter(sdf);
				}

				// Shows the file chooser
				int returnVal = fc.showSaveDialog(frame);

				// Checks that it is an OK file
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					// Gets the file and the name
					File saver = fc.getSelectedFile();
					String name = saver.getName();
					name = name.trim();

					// Checks the name is OK
					if (name.equals("")) {
						return;
					}

					// Checks the extension is correct
					if (!name.endsWith(end)) {
						String path = saver.getPath();
						int index = path.lastIndexOf(name);
						name += "." + end;
						path = path.substring(0, index);
						saver = new File(path + name);
					}

					// Tells the MolGridView to save it
					mgv.save(type, saver.toString(), sg);
				}
			} catch (Exception ex) {
			}
		}
	}

	// Allows the user to sort the molecules and view them in the order
	public class Sort implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// Creates JList and ListModel to view categories to sort by
			DefaultListModel<String> adder = new DefaultListModel<String>();
			JList<String> header = new JList<String>(adder);
			header.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			header.setSelectedIndex(0);
			header.setVisibleRowCount(5);
			header.setFixedCellHeight(16);

			// Adds the scroll bar
			JScrollPane scroller = new JScrollPane(header);
			scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

			// Adds the categories to sort by
			for (int i = 0; i < sg.plot.data.header.length; i++) {
				adder.add(i, sg.plot.data.header[i]);
			}

			// Create the layout and panel to display the form
			GridLayout g = new GridLayout(1, 2);
			JPanel grid = new JPanel();
			grid.setLayout(g);

			// Creates the option to sort ascending or descending
			JCheckBox c = new JCheckBox("Descending");

			// Adds items to the panel
			grid.add(c);
			grid.add(scroller);

			// Displays the form
			int choice = JOptionPane.showConfirmDialog(frame, grid, "Sort By",
					JOptionPane.OK_CANCEL_OPTION);

			// Checks the clicked OK
			if (choice != JOptionPane.OK_OPTION)
				return;

			// Gets and sets what to sort by
			String sortBy = (String) header.getSelectedValue();
			Molecule.sortBy = sortBy;

			// Sorts the list
			Collections.sort(mgv.mol);

			// If descending, flip the list
			if (!c.isSelected())
				mgv.flip();

			// Reset and show the order
			mgv.molshow = 0;

			frame.repaint();
		}
	}
}