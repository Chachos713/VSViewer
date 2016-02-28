/**
 * Copyright (C) 2014 Kyle and David Diller
 * 
 * This file is part of the VS Viewer 3D.
 * The contents are covered by the terms of the BSD license which
 * is included in the file license.txt, found at the root of the VS Viewer 3D
 * source tree.
 */

import astex.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.io.File;
import java.util.*;

public class AstexViewer implements ActionListener {
	// Items to display the molecule and side panels
	JFrame frame;
	MolViewer app;
	Box check, distances;
	Dimension size;
	JScrollPane scroller, scroller2;

	// List of all molecules added to the 3D viewer along with which one is the
	// most recent
	astex.Molecule astex, cyan;
	astex.Molecule queryMolecule, querySurface;
	ArrayList<astex.Molecule> molecules, surfaces;
	ArrayList<Molecule> mols;

	// Lists of the buttons and check boxes added to the side panel
	ArrayList<JCheckBox> butts, butts2;
	ArrayList<JButton> butts1;
	ArrayList<String> molNames;

	// Display more options for the 3D viewer
	UserInterface ui;

	// Draws the 2D molecule in the bottom corner of the form
	Draw d;

	// Counts how many distances have been created
	int distCount = 0;

	// The current directory the program is working in
	SimpleGui sg;

	// List of files to read
	public static ArrayList<File> files;

	// Closes the form
	public void close() {
		frame.setVisible(false);
	}

	// Creates the frame and the AstexViewer
	public AstexViewer(SimpleGui s) {
		// Creates the frame and sets some of the fields
		sg = s;
		frame = new JFrame("Astex Viewer 3D");
		frame.setSize(900, 800);
		app = new MolViewer(this);
		frame.add(app);
		d = new Draw();

		// Creates the MenuItems
		JMenuItem open, save, pop;

		open = new JMenuItem("Open");
		open.addActionListener(this);

		save = new JMenuItem("Save");
		save.addActionListener(this);

		pop = new JMenuItem("Display");
		pop.addActionListener(this);

		// Creates and adds the MenuItems to the MenuBar
		JMenuBar bar = new JMenuBar();
		JMenu menu = new JMenu("File");
		bar.add(menu);

		menu.add(open);
		menu.add(save);
		menu.add(pop);

		// Creates the Boxes and Scroll Bars for the distances and Molecules
		check = Box.createVerticalBox();
		distances = Box.createVerticalBox();

		scroller = new JScrollPane(check);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		size = new Dimension(250, 200);
		scroller.setPreferredSize(size);

		scroller2 = new JScrollPane(distances);
		scroller2
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroller2
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroller2.setPreferredSize(size);

		check.add(new JLabel("Molecules:"));
		distances.add(new JLabel("Distances:"));

		// Adds the side panel for the AstexViewer
		JPanel panel3 = new JPanel();
		panel3.setLayout(new GridLayout(3, 1));

		panel3.add(scroller);
		panel3.add(scroller2);
		panel3.add(d);

		// Creates the lists for adding Molecules
		surfaces = new ArrayList<astex.Molecule>();
		molecules = new ArrayList<astex.Molecule>();
		butts = new ArrayList<JCheckBox>();
		butts1 = new ArrayList<JButton>();
		butts2 = new ArrayList<JCheckBox>();
		molNames = new ArrayList<String>();
		mols = new ArrayList<Molecule>();

		// Adds all the objects to the frame
		frame.getContentPane().add(BorderLayout.CENTER, app);
		frame.getContentPane().add(BorderLayout.EAST, panel3);

		frame.setJMenuBar(bar);
	}

	// Sets the newest Molecule
	public void setSubj(Molecule m) {
		if (molNames.contains(m.getName())) {// Checks if the Molecule is
												// already in the list and
												// removes it
			MoleculeRenderer mr = app.getMoleculeRenderer();
			DynamicArray molts = mr.getMolecules();

			int index = molNames.indexOf(m.getName());
			butts.remove(index);
			butts1.remove(index);
			butts2.remove(index);
			check.remove(index + 1);
			molNames.remove(index);
			mols.remove(index);

			int in = molts.getIndex(molecules.remove(index));
			molts.removeElement(in);

			in = molts.getIndex(surfaces.remove(index));

			if (in >= 0)
				molts.removeElement(in);
		}

		for (int i = 0; i < surfaces.size(); i++) {
			if (surfaces.get(i) != null && butts2.get(i).isSelected()
					&& butts.get(i).isSelected())
				surfaces.get(i).setDisplayed(2);

			if (butts.get(i).isSelected())
				molecules.get(i).setDisplayed(2);
			butts.get(i).setSelected(false);
		}

		// Converts and adds the Molecule and surface to the AstexViewer
		astex = MoleculeConvertor.convert(m);
		surfaces.add(createSurface(astex));
		surfaces.get(surfaces.size() - 1).setDisplayed(2);
		mols.add(m);

		molecules.add(astex);
		newMol(astex.getName());

		app.addMolecule(astex);

		// Sets the display options for the molecule
		app.getMoleculeRenderer().execute(
				"select molexact '" + astex.getName() + "';");
		app.getMoleculeRenderer().execute("display cylinders on current;");
		app.getMoleculeRenderer().execute(
				"exclude molexact '" + astex.getName() + "';");

		// Sets the cyan Molecule
		int in = molecules.indexOf(cyan);
		if (in != -1 && surfaces.get(in) != null)
			setColor(surfaces.get(in), Color32.magenta);

		setMolecule(astex);
		setColor(surfaces.get(surfaces.size() - 1), Color32.cyan);

		app.dirtyRepaint();
		frame.setVisible(true);
	}

	// Used to create the dot surface
	public astex.Molecule createSurface(astex.Molecule as) {
		astex.Molecule a = MoleculeConvertor.createSurface(as);// Gets the dot
																// surface
		app.addMolecule(a);// Adds the surface to the AstexViewer

		// Sets values to show the dots
		app.getMoleculeRenderer().execute(
				"select molexact '" + a.getName() + "';");
		app.getMoleculeRenderer().execute("display sticks on current;");
		app.getMoleculeRenderer().execute("display lines off current;");
		app.getMoleculeRenderer().execute("ball_radius 0.04 current;");
		app.getMoleculeRenderer().execute(
				"exclude molexact '" + a.getName() + "';");
		return a;
	}

	// Catches Button Clicks or change in selection of a Check Box or a MenuItem
	// is clicked
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JCheckBox) {// Checks for a check box
			JCheckBox source = (JCheckBox) e.getSource();

			if (source.getText().equals("Surface")) {// If the check box is
														// correlated to the
														// surface
				int index = butts2.indexOf(source);// Index of the check box
													// selected

				if (source.isSelected()) {
					if (surfaces.get(index) == null) {// Checks to make sure
														// that the surface is
														// already created
						createSurface(molecules.get(index));

						if (cyan == molecules.get(index)) {// If it's the
															// surface of the
															// last added
															// molecule, set the
															// carbons to the
															// color cyan
							setColor(surfaces.get(index), Color32.cyan);
						}

						app.dirtyRepaint();
						return;
					}
				}

				surfaces.get(index).setDisplayed(2);// Switch visibility

				app.dirtyRepaint();
				return;
			}

			// SWitches a Molecules visibility
			draw(source);
			return;
		} else if (e.getSource() instanceof JButton) {// Checks that it is a
														// Button click
			JButton source = (JButton) e.getSource();

			if (e.getActionCommand().equals("Distance")) {// Check if the button
															// is correlated to
															// the distance
				String name = (source).getText();
				app.getMoleculeRenderer().execute(
						"distance -delete " + name + ";");// Deletes the
															// distance

				source.setVisible(false);
				distances.remove(source);
				app.dirtyRepaint();
			} else {
				// Removes a Molecule from the AstexViewer
				delete(source);
				return;
			}
		} else {
			switch (((JMenuItem) e.getSource()).getText().charAt(0)) {// Checks
																		// which
																		// MenuItem
																		// was
																		// selected
																		// based
																		// on
																		// the
																		// first
																		// letter
			case 'O':// Open MenuItem
				open();
				break;
			case 'S':// Save MenuItem
				save();
				break;
			case 'D':// Display MenuItem
				if (ui == null)// Creates the display options
					ui = new UserInterface(app);
				else
					ui.userInterfaceFrame.setVisible(true);
			}
		}
	}

	// Opens a Astex Script or a SDF file
	private void open() {
		try {
			// Creates the FileChooser
			JFileChooser fc = new JFileChooser(sg.saver);
			SDFileFilter sdf = new SDFileFilter();
			TXTFileFilter txt = new TXTFileFilter();
			fc.setFileFilter(sdf);
			fc.addChoosableFileFilter(txt);

			fc.setAcceptAllFileFilterUsed(false);

			int returnVal = fc.showOpenDialog(frame);// Shows the FileChooser
			if (returnVal == JFileChooser.APPROVE_OPTION) {// Approves the
															// selection
				File file = fc.getSelectedFile();

				if (fc.getFileFilter().equals(sdf)) {// If it's a SDF file, let
														// AstexViewer read the
														// file
					app.loadMolecule(file.toString());
				} else if (fc.getFileFilter().equals(txt)) {// Runs the script
															// created by the
															// AstexViewer
					app.executeScript(file.toString());
				}

				app.dirtyRepaint();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Saves the image or set of molecules
	private void save() {
		try {
			// Creates the FileChooser
			JFileChooser fc = new JFileChooser(sg.saver);
			SDFileFilterSDF sdf = new SDFileFilterSDF();
			ImageFileFilter imf = new ImageFileFilter();
			fc.setFileFilter(sdf);
			fc.addChoosableFileFilter(imf);
			fc.setAcceptAllFileFilterUsed(false);

			int returnVal = fc.showSaveDialog(frame);// Shows the FileChooser

			if (returnVal == JFileChooser.APPROVE_OPTION) {// Approves the
															// selection
				File file = fc.getSelectedFile();
				String name = file.getName();
				name = name.trim();

				if (name.equals("")) {
					return;
				}

				if (fc.getFileFilter().equals(imf)) {// Saves as a PNG
					savePNG(name, file);
				} else if (fc.getFileFilter().equals(sdf)) {// Saves as a SDF
					// Checks to make sure that there is the correct extension
					// of the file
					if (!name.endsWith(".sdf")) {
						String path = file.getPath();
						int index = path.lastIndexOf(name);
						name += ".sdf";
						path = path.substring(0, index);
						file = new File(path + name);
					}

					Molecule.WriteSDFFile(mols, file, true);// Calls the method
															// to save the
															// molecules as a
															// SDF file
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Saves an image of the current stae of the AstexViewer
	private void savePNG(String name, File file) {
		// Checks to make sure that there is the correct extension of the file
		if (!name.endsWith(".png")) {
			String path = file.getPath();
			int index = path.lastIndexOf(name);
			name += ".png";
			path = path.substring(0, index);
			file = new File(path + name);
		}

		Dimension d = getSize();// Gets the size of the image
		try {
			app.getMoleculeRenderer().execute(
					"view -width " + d.width + " -height " + d.height
							+ " -writeimage \'" + file + "\';");// Tells the
																// AstexViewer
																// to save the
																// picture
		} catch (Exception e) {
			e.printStackTrace();
			System.gc();
		}
	}

	// Used to get the dimensions of the image
	private Dimension getSize() {
		// Labels for the height, width, and title
		JLabel dir = new JLabel("Size (px):");
		JLabel wid = new JLabel("Width:");
		JLabel hid = new JLabel("Height:");

		// Creates a Spinner to select the sizes
		JSpinner width = new JSpinner(new SpinnerNumberModel(600, 300, 8000, 1));
		JSpinner height = new JSpinner(
				new SpinnerNumberModel(600, 300, 8000, 1));

		// Creates the panel with a grid layout
		GridLayout grid = new GridLayout(2, 2);
		JPanel values = new JPanel();
		values.setLayout(grid);

		// Adds the labels and spinners
		values.add(wid);
		values.add(width);
		values.add(hid);
		values.add(height);

		// Adds the title and the spinners to a single panel
		Box vert = Box.createVerticalBox();
		vert.add(dir);
		vert.add(values);

		// Displays the Dialog Box to set the size
		JOptionPane.showConfirmDialog(frame, vert, "Size",
				JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE);

		// Returns the dimensions of the picture
		return new Dimension((Integer) width.getValue(),
				(Integer) height.getValue());
	}

	// Creates the panel for the new Molecule
	private void newMol(String name) {
		// Creates and adds the Check Box for displaying the molecule
		JCheckBox c = new JCheckBox(name, true);
		c.addActionListener(this);
		butts.add(c);

		// Creates and adds the Check Box for the surface
		JCheckBox c2 = new JCheckBox("Surface");
		c2.addActionListener(this);
		butts2.add(c2);

		// Creates and adds the Button to delete the molecule
		JButton b = new JButton("X");
		b.addActionListener(this);
		butts1.add(b);

		// Creates the panel and adds all the component
		JPanel p = new JPanel();
		p.add(c);
		p.add(b);
		p.add(c2);

		// Adds the panel to be displayed
		check.add(p);
		molNames.add(name);// Adds the name of the molecule

		frame.setVisible(true);
	}

	// Used to make a Molecule seen or hidden
	public void draw(JCheckBox c) {
		// Gets the Molecule and switches whether it's seen or not
		int index = butts.indexOf(c);
		astex.Molecule mol = molecules.get(index);
		mol.setDisplayed(2);

		// Switches the surface of the molecule
		if (butts2.get(index).isSelected()) {
			surfaces.get(index).setDisplayed(0);
			butts2.get(index).setSelected(false);
		}

		app.dirtyRepaint();
		frame.setVisible(true);
	}

	// Removes a molecule from the AstexViewer
	public void delete(JButton b) {
		// Gets the list of all the Molecules
		MoleculeRenderer mr = app.getMoleculeRenderer();
		DynamicArray mols1 = mr.getMolecules();

		// Gets the index of which Button is clicked
		int index = butts1.indexOf(b);

		// Sets all the components invisible
		butts.get(index).setVisible(false);
		butts1.get(index).setVisible(false);
		butts2.get(index).setVisible(false);
		check.getComponent(index + 1).setVisible(false);// The +1 is because of
														// the title at the top
														// of the box

		// Removes all the components
		butts.remove(index);
		butts1.remove(index);
		butts2.remove(index);
		check.remove(index + 1);
		molNames.remove(index);

		astex.Molecule mol, sur;

		// Gets the location of the Molecule to be removed
		int in = mols1.getIndex(molecules.remove(index));

		// Removes the Molecule from the list
		if (in >= 0) {
			mol = (astex.Molecule) mols1.get(in);
			mols1.removeElement(in);

			if (ui != null) {
				ui.moleculeRemoved(mr, mol);
			}
		}

		// Gets the location of the surface
		in = mols1.getIndex(surfaces.remove(index));

		// Removes the surface from the list
		if (in >= 0) {
			sur = (astex.Molecule) mols1.get(in);
			mols1.removeElement(in);

			if (ui != null) {
				ui.moleculeRemoved(mr, sur);
			}
		}

		mols.remove(index);

		// Reloads all the distances stored by the AstexViewer
		reloadDistances();

		app.dirtyRepaint();
		frame.setVisible(true);
	}

	// Reloads all the distances stored by the AstexViewer
	private void reloadDistances() {
		MoleculeRenderer mr = app.getMoleculeRenderer();
		distCount = 0;

		// Removes all the distances from the list
		for (int i = distances.getComponentCount() - 1; i >= 0; i--) {
			distances.remove(i);
		}

		// Adds the title to the JPanel
		distances.add(new JLabel("Distances:"));

		// Adds all the distances back
		for (int i = 0; i < mr.getDistanceCount(); i++) {
			putDistances(mr.getDistance(i));
		}
	}

	// Sets the cyan Molecule
	private void setMolecule(astex.Molecule m) {
		if (cyan != null) {// Switches the current Molecule to being magenta
			setColor(cyan, Color32.magenta);
		}

		// Sets the color to the Molecule to be cyan
		setColor(m, Color32.cyan);

		cyan = m;
	}

	// Sets the color of all the carbon atoms
	private void setColor(astex.Molecule m, int c) {
		// Goes through each atom and sets the color to 'c' if the atom is a
		// carbon
		for (int i = 0; i < m.getAtomCount(); i++) {
			if (m.getAtom(i).getElement() == 6)
				m.getAtom(i).setColor(c);
		}
	}

	@SuppressWarnings("serial")
	// Draws the Molecule in the bottom corner
	public class Draw extends JPanel {
		int index;

		public void redraw(int i) {
			index = i;
			repaint();
		}

		public void paintComponent(Graphics g) {
			// Clears the corner
			g.setColor(Color.white);
			g.fillRect(0, 0, 10000, 10000);

			// Creates the MolGridView with the bounds
			MolGridView mgv = new MolGridView();
			mgv.Open(900, 900, 300, 150, MolGridViewGui.borderx,
					MolGridViewGui.bordery);

			// Draws the Molecule
			if (index >= 0 && index < mols.size())
				mgv.DrawMol(g, mols.get(index), 0, 0.0, 250.0, 125.0, 0.0,
						false);
		}
	}

	// Opens the list of extension files
	public void openFiles() {
		if (files == null)
			return;

		for (File f : files) {
			if (f.exists()) {
				String exten = f.getName().substring(
						f.getName().lastIndexOf("."));// Gets the extension

				if (exten.equals(".astx")) {// Runs the Astex script
					app.executeScript(f.toString());
				} else {// Reads a any other format
					app.loadMolecule(f.toString());
				}
			}
		}
	}

	// Used to reset all the Check Boxes if visibility is switched
	public void reload() {
		// Sets the Check Boxes state based on the molecules visibility
		for (int i = 0; i < molecules.size(); i++) {
			astex.Molecule m = molecules.get(i);

			butts.get(i).setSelected(m.getDisplayed());
		}

		// Sets the surfaces visibility
		for (int i = 0; i < surfaces.size(); i++) {
			astex.Molecule s = surfaces.get(i);
			astex.Molecule m = molecules.get(i);

			if (!m.getDisplayed()) {
				surfaces.get(i).setDisplayed(0);
				butts2.get(i).setSelected(false);
			} else {
				butts2.get(i).setSelected(s.getDisplayed());
			}
		}

		// Sets the visibility of the Query Molecule
		querySurface.setDisplayed(queryMolecule.getDisplayed() ? 1 : 0);
	}

	// Sets the Query Molecule
	public void setQuerry(Molecule m) {
		// Creates the Query Molecule used by the AstexViewer
		queryMolecule = MoleculeConvertor.convert(m);
		queryMolecule.setName("QuerryMol");

		// Creates the surface of the Query Molecule
		querySurface = createSurface(queryMolecule);
		queryMolecule.setDisplayed(2);

		app.addMolecule(queryMolecule);// Adds the molecule to the AstexViewer

		// Sets the values to draw the Molecule
		app.getMoleculeRenderer().execute(
				"select molexact '" + queryMolecule.getName() + "';");
		app.getMoleculeRenderer().execute("display cylinders on current;");
		app.getMoleculeRenderer().execute(
				"exclude molexact '" + queryMolecule.getName() + "';");

		// Displays the Molecule
		queryMolecule.setDisplayed(1);

		app.dirtyRepaint();
	}

	// Adds a distance to the list
	public void putDistances(Distance d) {
		distCount++;
		d.setString("name", "Distance" + distCount);// Sets the name of the
													// distance

		// Creates the button for the distance
		JButton b = new JButton(d.getString("name", null));
		b.setActionCommand("Distance");
		b.addActionListener(this);

		// Adds the distance to be displayed
		distances.add(b);

		app.dirtyRepaint();
		frame.setVisible(true);
	}
}
