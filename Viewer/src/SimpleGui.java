/**
 * Copyright (C) 2014 Kyle and David Diller
 * 
 * This file is part of the VS Viewer 3D.
 * The contents are covered by the terms of the BSD license which
 * is included in the file license.txt, found at the root of the VS Viewer 3D
 * source tree.
 */

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.File;

public class SimpleGui {
	// Fields to store the other forms, current file, JMenu's, JButton's, etc.
	// all used throughout the program
	Dialog display;
	JFrame frame, molFrm;
	int plot_width = 700;
	int plot_height = 700;
	int border = 25;
	int xoff = 10, yoff = 50;
	Plot2D plot;
	MolGridViewGui mgvg;
	ArrayList<Point> select_curve = new ArrayList<Point>();
	static CommentGui comments;
	MyDraw drawer;
	boolean startUp;
	AstexViewer av;
	int prevX, prevY;
	File saver;
	JMenu tool;
	JMenuItem sort;
	ArrayList<JCheckBoxMenuItem> checks = new ArrayList<JCheckBoxMenuItem>();
	static boolean changed = false;
	JButton compare, compare2d;
	ArrayList<MenuElement> si, co;
	Box colorBy, sizeBy, logBy;
	int button = 0;
	Split split;
	int type = -1;
	SpreadSheet sheet;
	boolean invert;
	static JCheckBoxMenuItem[] views;
	SimpleGui sg = this;
	JMenu xaxis, yaxis;

	// Method to run the program
	public static void main(String[] args) {
		SimpleGui gui = new SimpleGui();
		gui.go(args);
	}

	// Creates the frame, and all the values for the start of the program
	public void go(String[] args) {
		// Assigns starting values to some of the other forms
		startUp = true;
		drawer = new MyDraw();
		av = new AstexViewer(this);
		plot = new Plot2D(this);
		comments = new CommentGui(this);
		mgvg = new MolGridViewGui(av);

		// Creates a menu bar and adds menu items to it
		JMenuBar bar = new JMenuBar();
		JMenu menu = new JMenu("File");
		bar.add(menu);

		JMenuItem open = new JMenuItem("Open");
		open.addActionListener(new Open());
		JMenuItem save = new JMenuItem("Save As");
		save.addActionListener(new SaveAs());
		JMenuItem save1 = new JMenuItem("Save");
		save1.addActionListener(new Save());
		JMenuItem close = new JMenuItem("Close");
		close.addActionListener(new Close());
		JMenuItem help = new JMenuItem("Help");
		help.addActionListener(new Help());
		JMenuItem exit = new JMenuItem("Exit");
		exit.addActionListener(new Exit());
		JMenuItem stats = new JMenuItem("Stats");
		stats.addActionListener(new Stats());
		sheet = new SpreadSheet();

		menu.add(open);
		menu.add(save);
		menu.add(save1);
		menu.add(close);
		menu.add(help);
		menu.add(exit);

		sort = new JMenuItem("Display");
		sort.setEnabled(false);
		sort.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				display.setVisible(true);
			}
		});

		split = new Split(this);

		tool = new JMenu("Tools");
		tool.setEnabled(false);
		JMenuItem swort = new JMenuItem("Split");
		swort.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				split.show();
			}
		});
		tool.add(swort);

		tool.add(sort);

		JMenuItem searchCom = new JMenuItem("Search by Comment");
		searchCom.addActionListener(new ComSearch());
		tool.add(searchCom);

		JMenuItem inverter = new JMenuItem("Invert Selection");
		inverter.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				invert = !invert;
			}
		});
		tool.add(inverter);
		tool.add(stats);

		JMenu screens = new JMenu("Turn On/Off Screens");
		createScreens(screens);
		tool.add(screens);

		bar.add(tool);

		xaxis = new JMenu("X-Axis");
		xaxis.setEnabled(false);

		yaxis = new JMenu("Y-Axis");
		yaxis.setEnabled(false);

		bar.add(xaxis);
		bar.add(yaxis);

		// Creates the frame and calculates the size based on the screen
		// resolution
		frame = new JFrame("Scatter Plot");

		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension dim = toolkit.getScreenSize();
		int w = dim.width;

		plot_width = 4 * w / 10;
		plot_height = plot_width;
		int mol_width = 15 * plot_width / 31;

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().add(BorderLayout.CENTER, plot);
		frame.setJMenuBar(bar);
		frame.setSize(plot_width + mol_width + 15 - 370, plot_height + border
				* 2 - 10);// The minuses are removing the extra space
		frame.setVisible(true);

		// Adds listeners for functions on this form
		frame.addMouseListener(new myMouseListener());
		frame.addMouseMotionListener(new myMouseMotionListener());
		frame.addComponentListener(new Resizer());
		frame.addMouseWheelListener(new Rotate());
		frame.addWindowListener(new Closing());
		frame.addKeyListener(new myKeyListener());

		// Calculates the size for the 2D Molecule pop-up
		Dimension d = frame.getSize();

		plot_height = d.height + 10 - (border * 2);
		plot_width = d.width + 10 - border;

		plot.open(plot_width, plot_height - border, border);

		molFrm = new JFrame("Molecule");
		molFrm.setBounds(d.width, 0, 310, 220);

		// Adds the compare buttons to the frame
		JPanel panel = new JPanel();
		compare = new JButton("Compare 3D");
		compare.addActionListener(new Compare(this, true, true));
		compare.setEnabled(false);
		panel.add(compare);

		compare2d = new JButton("Compare 2D");
		compare2d.addActionListener(new Compare(this, false, true));
		compare2d.setEnabled(false);
		panel.add(compare2d);

		molFrm.getContentPane().add(BorderLayout.SOUTH, panel);
		molFrm.getContentPane().add(BorderLayout.CENTER, drawer);

		// Creates and sets the dimensions to a display panel
		display = new Dialog(frame);
		display.setVisible(false);

		// Boxes to store a list of all the descriptors to change plotting
		// values
		logBy = Box.createVerticalBox();
		sizeBy = Box.createVerticalBox();
		colorBy = Box.createVerticalBox();

		JScrollPane scroller = new JScrollPane(logBy);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroller.setPreferredSize(new Dimension(150, 150));

		JScrollPane scroller1 = new JScrollPane(sizeBy);
		scroller1
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroller1
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroller1.setPreferredSize(new Dimension(150, 150));

		JScrollPane scroller2 = new JScrollPane(colorBy);
		scroller2
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroller2
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroller2.setPreferredSize(new Dimension(150, 150));

		Box b = Box.createVerticalBox();

		// Allows the user to change the minimum/maximum sizes of the points
		JButton change = new JButton("Change Size");
		change.setActionCommand("size: 10");
		change.addActionListener(new Sort());

		b.add(new JLabel("Color By:"));
		b.add(scroller2);
		b.add(new JLabel("Size By:"));
		b.add(scroller1);
		b.add(change);
		b.add(new JLabel("Log:"));
		b.add(scroller);

		display.add(b);
		display.setSize(200, frame.getSize().height);
		display.addWindowListener(new DClosing());

		if (args.length > 0) {// If a file is already presented
			saver = new File(args[0]);
			open(saver);
		}
	}

	// Opens a file
	public void open(File f) {
		// Resets all the forms
		new Close().actionPerformed(null);

		char c = f.getName().substring(f.getName().indexOf(".")).charAt(1);

		// Determines the type to show forms, and allow certain buttons to be
		// enabled
		switch (c) {
		case 'v':
			type = 2;
			break;
		case 'c':
			type = 0;
			break;
		case 's':
			type = 1;
			break;
		default:
			System.err.println("WRONG FILE TYPE");
		}

		// Reads a file
		if (!plot.openFile("" + f, plot_width, plot_height - border, 1, 2, 0,
				border, frame)) {
			type = -1;
			return;
		}

		// Adds a list of comments if the file wasn't a CSV
		if (type >= 1) {
			comments.setMaster(plot.data.masterList);
		}

		// Allows for the pop-up if not CSV
		if (type == 2)
			molFrm.setVisible(true);

		// Enable all the menus
		sort.setEnabled(true);
		tool.setEnabled(true);
		xaxis.setEnabled(true);
		yaxis.setEnabled(true);

		// Adds all the descriptors to the x axis, y axis, and boxes
		addItems();

		// Disable setting the AstexViewer to be seen if not a VSV
		if (type < 2)
			views[4].setEnabled(false);

		// If CSV file disable most of the forms
		if (type < 1) {
			for (int i = 1; i < 4; i++) {
				views[i].setEnabled(false);
			}
		}

		// Draws the frame and opens file and sets the SimpleGui in
		// MolGridViewGui
		frame.repaint();
		av.openFiles();
		mgvg.sg(this);

		// Sets current file
		saver = f;

		// Sets the Query
		if (plot.data.query != null)
			av.setQuerry(plot.data.query);
	}

	public class myKeyListener implements KeyListener {
		public void keyTyped(KeyEvent e) {
		}

		public void keyReleased(KeyEvent e) {
		}

		// Deals with a key pressed
		public void keyPressed(KeyEvent e) {
			char keyChar = e.getKeyChar();

			// Reset a axis based on which key is pressed
			if (keyChar == 'x')
				plot.resetX();
			if (keyChar == 'y')
				plot.resetY();

			plot.repaint();
		}
	}

	class myMouseListener implements MouseListener {
		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}

		// Takes a mouse pressed
		public void mousePressed(MouseEvent e) {
			if (e.getButton() == MouseEvent.BUTTON3) {// Right mouse button for
														// translating the
														// scatter plot
				prevX = e.getX() - xoff;
				prevY = e.getY() - yoff;
			} else {// Left mouse button for selection
				select_curve.clear();
				plot.select_curve.clear();
			}

			button = e.getButton();
		}

		// Handles the mouse button released
		public void mouseReleased(MouseEvent e) {
			if ((select_curve.size() > 3) && plot.ready && button != 3) {// If
																			// the
																			// curve
																			// is
																			// long
																			// enough
																			// and
																			// selection
																			// is
																			// enabled
				select_curve.add(select_curve.get(0)); // Sets the beginning as
														// the end
				plot.select_curve.clear();// Clears the set for drawing

				// Calculates which data points are selected
				plot.WhoIsSelected(select_curve, invert);

				// Redraws
				frame.repaint();

				// Counts the number selected
				int n = plot.selected();

				if (n == 0)
					return;

				// Gets the selected molecules
				ArrayList<Molecule> mol = new ArrayList<Molecule>();

				for (int j = 0; j < plot.data.mol.length; j++) {
					if (plot.data.selected[j]) {
						mol.add(plot.data.mol[j]);
					}
				}

				// Sets the header for the spreadsheet
				String[] head = new String[plot.data.header.length + 2];
				head[0] = "Names";
				head[1] = "Selected";

				int i = 2;

				for (String g : plot.data.header) {
					head[i] = g;
					i++;
				}

				// Displays the GridView
				if (type >= 1 && views[1].isSelected())
					mgvg.go(mol);

				// Displays the spreadsheet
				if (views[0].isSelected())
					sheet.newMol(head, plot.data.selected, plot.data.mol);

				button = 0;
			}
		}

		// Takes a mouse click
		public void mouseClicked(MouseEvent e) {
			if (!plot.ready)
				return;

			int x, y;
			x = e.getX() - xoff;
			y = e.getY() - yoff;

			int maxy = frame.getSize().height - 35;

			// Checks the mouse clicks location
			if (x >= border && x <= border + plot_width && y >= border
					&& y <= maxy - yoff && type >= 1) {
				if (plot.MouseMove(x, y)) {
					if (plot.molshow >= 0) {

						// If a data point was clicked show the:

						// Comment viewer
						if (e.getClickCount() >= 0 && views[3].isSelected()) {
							comments.newMol(plot.data.mol[plot.molshow]);
						}

						// AstexViewer
						if (type >= 2 && views[4].isSelected())
							av.setSubj(plot.data.mol[plot.molshow]);
					}
				}
			} else {
				plot.MouseClick(x, y, e);// Checks the borders
			}
			frame.repaint();
		}
	}

	public class myMouseMotionListener implements MouseMotionListener {
		// Changes which Molecule is shown in the pop-up window
		public void mouseMoved(MouseEvent e) {
			int x, y;
			x = e.getX() - xoff;
			y = e.getY() - yoff;
			int maxy = frame.getSize().height - 35;
			if (x >= border && x <= border + plot_width && y >= border
					&& y <= maxy - yoff) {
				if (plot.MouseMove(x, y)) {
					startUp = false;
					drawer.repaint();
				}
			}

			compare.setEnabled(plot.ready && plot.molshow >= 0 && type >= 2);
			compare2d.setEnabled(plot.ready && plot.molshow >= 0 && type >= 1);
			if (!molFrm.isVisible()) {
				molFrm.setVisible(type >= 1 && views[2].isSelected());
			}
		}

		// Takes the mouse drag and:
		public void mouseDragged(MouseEvent e) {
			if (button == MouseEvent.BUTTON3) {// Translate the view
				int x = e.getX() - xoff;
				int y = e.getY() - yoff;

				plot.translate(x - prevX, y - prevY);

				prevX = x;
				prevY = y;
			} else {// or adds points to the curve
				Point p = e.getPoint();
				p.x -= xoff;
				p.y -= yoff;
				select_curve.add(p);
				plot.select_curve.add(p);
			}

			frame.repaint();
			compare.setEnabled(plot.molshow >= 0 && !startUp && plot.ready);
		}
	}

	@SuppressWarnings("serial")
	// Draws the Molecule on the pop-up
	public class MyDraw extends JPanel {
		public void paintComponent(Graphics g) {
			// Clears screen
			g.setColor(Color.white);
			g.fillRect(0, 0, 10000, 10000);

			// Draws the Molecule using the MolGridView's method
			if (!startUp && plot.ready) {
				MolGridView mgv = new MolGridView();
				mgv.Open(900, 900, 300, 150, MolGridViewGui.borderx,
						MolGridViewGui.bordery);

				mgv.DrawMol(g, plot.data.mol[plot.molshow], 0, 0.0, 300.0,
						150.0, 0.0, false);
			}
		}
	}

	public class Resizer implements ComponentListener {
		// Takes the changing screen size and tells the plot method the new
		// drawing dimensions
		public void componentResized(ComponentEvent e) {
			Dimension d = frame.getSize();

			plot_height = d.height + 10 - (border * 2);
			plot_width = d.width + 10 - border;

			plot.open(plot_width, plot_height - border, border);
			frame.repaint();
		}

		public void componentHidden(ComponentEvent e) {
		}

		public void componentShown(ComponentEvent e) {
		}

		public void componentMoved(ComponentEvent e) {
		}
	}

	public class Rotate implements MouseWheelListener {
		// Zooms the view in and out based on the wheel rotation
		public void mouseWheelMoved(MouseWheelEvent e) {
			int sign = (int) Math.signum(e.getWheelRotation());

			for (int i = Math.abs(e.getWheelRotation()); i > 0; i--) {
				if (sign > 0) {
					plot.decreaX();
					plot.decreaY();
				} else if (sign < 0) {
					plot.increaX();
					plot.increaY();
				}
			}

			plot.repaint();
		}
	}

	// Displays a file chooser to open a new file
	public class Open implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			try {
				JFileChooser fc = new JFileChooser(saver);

				SDFileFilter2 sdf = new SDFileFilter2();
				SDFileFilter3 sdf2 = new SDFileFilter3();
				SDFileFilter1 sdf1 = new SDFileFilter1();
				fc.setFileFilter(sdf2);
				fc.addChoosableFileFilter(sdf1);
				fc.addChoosableFileFilter(sdf);
				fc.setAcceptAllFileFilterUsed(false);

				int returnVal = fc.showOpenDialog(frame);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File f = fc.getSelectedFile();

					// Gives the file to the method open
					open(f);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	// Allows the user to save the file to a different location if it was read
	// from a VSV file
	public class SaveAs implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (!plot.ready && type != 2)
				return;

			try {
				JFileChooser fc = new JFileChooser(saver);
				SDFileFilter2 sdf2 = new SDFileFilter2();
				fc.setFileFilter(sdf2);
				fc.setAcceptAllFileFilterUsed(false);

				int returnVal = fc.showSaveDialog(frame);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					saver = fc.getSelectedFile();
					String name = saver.getName();
					name = name.trim();

					if (name.equals("")) {
						return;
					}

					// Checks that it has the right extension
					if (!name.endsWith(".vsv")) {
						String path = saver.getPath();
						int index = path.lastIndexOf(name);
						name += ".vsv";
						path = path.substring(0, index);
						saver = new File(path + name);
					}

					plot.save(saver);
					changed = false;
				}
			} catch (Exception ex) {
			}
		}
	}

	// Saves a file to the same location
	public class Save implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (saver == null)
				return;

			plot.save(saver);
			SimpleGui.changed = false;
		}
	}

	// Closes all the forms and resets to opening state
	public class Close implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (SimpleGui.changed) {
				int choice = JOptionPane.showConfirmDialog(frame,
						"Would you like to save your work?", "Save",
						JOptionPane.YES_NO_CANCEL_OPTION);

				if (choice == JOptionPane.YES_OPTION)
					new Save().actionPerformed(e);
				else if (choice == JOptionPane.CANCEL_OPTION)
					return;
			}

			SimpleGui.changed = false;
			tool.setEnabled(false);
			yaxis.setEnabled(false);
			xaxis.setEnabled(false);
			plot.close();
			frame.repaint();
			drawer.repaint();
			sort.setEnabled(false);
			saver = null;
			molFrm.setVisible(false);
			mgvg.close();
			av.close();
			sheet.close();
			comments.close();

			System.gc();

			startUp = true;
			type = 0;
			av = new AstexViewer(sg);
		}
	}

	// A simple description of the program
	public class Help implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JOptionPane
					.showMessageDialog(frame,
							"This is used to plot molecules based on data writen by Kyle Diller");
		}
	}

	// Closes the program
	public class Exit implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			new Close().actionPerformed(e);
			System.exit(0);
		}
	}

	public class Closing implements WindowListener {
		public void windowOpened(WindowEvent e) {
		}

		public void windowClosed(WindowEvent e) {
		}

		public void windowIconified(WindowEvent e) {
		}

		public void windowDeiconified(WindowEvent e) {
		}

		public void windowActivated(WindowEvent e) {
		}

		public void windowDeactivated(WindowEvent e) {
		}

		public void windowStateChanged(WindowEvent e) {
		}

		public void windowGainedFocus(WindowEvent e) {
		}

		public void windowLostFocus(WindowEvent e) {
		}

		// Handles the frame being closed
		public void windowClosing(WindowEvent e) {
			boolean changed = SimpleGui.changed;
			if (changed) {
				int choice = JOptionPane.showConfirmDialog(frame,
						"Would you like to save your work?", "Save",
						JOptionPane.YES_NO_OPTION);

				if (choice == JOptionPane.CANCEL_OPTION) {
					return;
				} else if (choice == JOptionPane.YES_OPTION) {
					plot.save(saver);
				}
			}
			frame.dispose();
		}
	}

	public class DClosing implements WindowListener {
		public void windowOpened(WindowEvent e) {
		}

		public void windowClosed(WindowEvent e) {
		}

		public void windowIconified(WindowEvent e) {
		}

		public void windowDeiconified(WindowEvent e) {
		}

		public void windowActivated(WindowEvent e) {
		}

		public void windowDeactivated(WindowEvent e) {
		}

		public void windowStateChanged(WindowEvent e) {
		}

		public void windowGainedFocus(WindowEvent e) {
		}

		public void windowLostFocus(WindowEvent e) {
		}

		// Closes the dialog for selection
		public void windowClosing(WindowEvent e) {
			Window localWindow = e.getWindow();
			localWindow.setVisible(false);
		}
	}

	// Adds the headers to the x axis, y axis, and boxes
	public void addItems() {
		// Clears everything
		co = new ArrayList<MenuElement>();
		si = new ArrayList<MenuElement>();
		int i = 0;
		int j = 0;

		xaxis.removeAll();
		yaxis.removeAll();

		if (checks.size() > 0) {
			colorBy.remove(0);
			sizeBy.remove(0);
			colorBy.remove(0);
			sizeBy.remove(0);
		}

		for (j = checks.size() - 1; j > 0; j--) {
			colorBy.remove(0);
			sizeBy.remove(0);
			logBy.remove(0);
		}

		checks.clear();

		// Adds the descriptors
		for (String s : plot.data.header) {
			JRadioButtonMenuItem z = new JRadioButtonMenuItem(s);
			z.addActionListener(new Sort());
			z.setActionCommand("size: " + i);
			si.add(z);
			sizeBy.add(z);

			JRadioButtonMenuItem c = new JRadioButtonMenuItem(s);
			c.addActionListener(new Sort());
			c.setActionCommand("colr: " + i);
			co.add(c);
			colorBy.add(c);

			JCheckBoxMenuItem k = new JCheckBoxMenuItem(s);
			k.addActionListener(new Sort());
			k.setActionCommand("null1234567890");
			checks.add(k);
			logBy.add(k);

			if (!plot.pos(s))
				k.setEnabled(false);

			JMenuItem x = new JMenuItem(s);
			x.addActionListener(new ChangeAxis('x', i));
			xaxis.add(x);

			JMenuItem y = new JMenuItem(s);
			y.addActionListener(new ChangeAxis('y', i));
			yaxis.add(y);
			i++;
		}

		JRadioButtonMenuItem z = new JRadioButtonMenuItem("None", true);
		z.addActionListener(new Sort());
		z.setActionCommand("size: " + i);
		si.add(z);
		sizeBy.add(z);

		JMenuItem z1 = new JMenuItem("Set Size");
		z1.addActionListener(new Sort());
		z1.setActionCommand("size: " + i);
		si.add(z1);

		JRadioButtonMenuItem c = new JRadioButtonMenuItem("None", true);
		c.addActionListener(new Sort());
		c.setActionCommand("colr: " + i);
		colorBy.add(c);
		co.add(c);

		plot.set();
	}

	// Used to set the new descriptors for color and size and change the plot if
	// a log is changed
	public class Sort implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String c = e.getActionCommand();
			int place = Integer.parseInt(c.substring(6));

			if (c.startsWith("size: ")) {
				if (e.getSource() instanceof JRadioButtonMenuItem) {
					plot.setS(place);

					for (MenuElement item : si) {
						if (item instanceof JRadioButtonMenuItem)
							((JRadioButtonMenuItem) item).setSelected(false);
					}

					((JRadioButtonMenuItem) e.getSource()).setSelected(true);
				} else {
					JTextField min = new JTextField(10);
					JTextField max = new JTextField(10);
					JPanel minP = new JPanel();
					minP.add(new JLabel("Min: "));
					minP.add(min);

					JPanel maxP = new JPanel();
					maxP.add(new JLabel("Max: "));
					maxP.add(max);
					Object[] params = { minP, maxP };

					int choice = JOptionPane.showConfirmDialog(frame, params,
							"Change Size", JOptionPane.YES_NO_OPTION);

					if (choice == JOptionPane.YES_OPTION) {
						double mini = Double.parseDouble(min.getText());
						double maxi = Double.parseDouble(max.getText());

						if (mini < 0)
							mini = 0;
						if (maxi < 0)
							maxi = 0;

						plot.setSize(mini, maxi);
					}
				}
			} else if (c.startsWith("colr: ")) {
				plot.setC(place);
				for (MenuElement item : co) {
					if (item instanceof JRadioButtonMenuItem)
						((JRadioButtonMenuItem) item).setSelected(false);
				}

				((JRadioButtonMenuItem) e.getSource()).setSelected(true);
			}

			plot.resetX();
			plot.resetY();
			frame.repaint();
		}
	}

	// Takes the change in which screens are to be displayed or not
	public class Flip implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// Checks 4th char(different in all) to see which is selected
			switch (((JCheckBoxMenuItem) e.getSource()).getText().charAt(3)) {
			case 'a':
				sheet.close();
				break;
			case 'G':
				mgvg.close();
				break;
			case 'm':
				comments.close();
				break;
			case 'V':
				av.close();
			}
		}
	}

	// Creates the JMenu for turning screens on/off
	public void createScreens(JMenu screen) {
		views = new JCheckBoxMenuItem[5];
		views[0] = new JCheckBoxMenuItem("Data Table");
		views[1] = new JCheckBoxMenuItem("2D GridView");
		views[2] = new JCheckBoxMenuItem("2D Popup View");
		views[3] = new JCheckBoxMenuItem("Comment View");
		views[4] = new JCheckBoxMenuItem("3D Viewer");

		for (JCheckBoxMenuItem i : views) {
			i.setSelected(true);
			screen.add(i);
		}
	}

	// Searches the comments for a list of Molecules
	public class ComSearch implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// Creates and shows the JList
			DefaultListModel<String> adder = new DefaultListModel<String>();
			JList<String> comments = new JList<String>(adder);
			comments.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			comments.setSelectedIndex(0);
			comments.setVisibleRowCount(5);
			comments.setFixedCellHeight(16);

			JScrollPane scroller = new JScrollPane(comments);
			scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
			scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

			for (int i = 0; i < plot.data.masterList.size(); i++) {
				adder.add(i, plot.data.masterList.get(i));
			}

			int choice = JOptionPane.showConfirmDialog(frame, scroller,
					"Search by Comment", JOptionPane.OK_CANCEL_OPTION);

			int loc = comments.getSelectedIndex();

			if (choice == JOptionPane.CANCEL_OPTION || loc < 0)
				return;

			// Searches the Molecules
			ArrayList<Molecule> mols = new ArrayList<Molecule>();

			for (Molecule m : plot.data.mol) {
				if (m.containsComment(plot.data.masterList.get(loc)))
					mols.add(m);
			}

			if (type >= 1 && views[1].isSelected())
				mgvg.go(mols);
		}
	}

	// Enables drawing the line of best fit and the minimum correlation
	// coefficient
	public class Stats implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JCheckBox draw = new JCheckBox("Draw Line of Best Fit");
			draw.setSelected(plot.drawLine);

			final JLabel value = new JLabel("Value: " + 100
					* plot.correlationEdge);

			final JSlider slide = new JSlider();
			slide.setMaximum(100);
			slide.setMinimum(0);
			slide.setValue((int) Math.abs(100 * plot.correlationEdge));
			slide.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					value.setText("Value: " + slide.getValue());
				}
			});

			JPanel panel = new JPanel();
			GridLayout g = new GridLayout(4, 1);
			panel.setLayout(g);

			panel.add(draw);
			panel.add(new JLabel("Correlation Minimum:"));
			panel.add(slide);
			panel.add(value);

			int choice = JOptionPane.showConfirmDialog(frame, panel, "Stats",
					JOptionPane.OK_CANCEL_OPTION);

			if (choice == JOptionPane.OK_OPTION) {
				plot.setLine(draw.isSelected());
				plot.setCorr((double) slide.getValue() / 100.0);
			}
		}
	}

	// Changes the x axis and y axis value
	public class ChangeAxis implements ActionListener {
		char axis;
		int pos;

		public ChangeAxis(char c, int i) {
			axis = c;
			pos = i;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			switch (axis) {
			case 'x':
				plot.xaxis = pos;
				plot.resetX();
				break;
			case 'y':
				plot.yaxis = pos;
				plot.resetY();
			}

			frame.repaint();
		}

	}
}