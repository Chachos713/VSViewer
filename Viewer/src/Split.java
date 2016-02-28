/**
 * Copyright (C) 2014 Kyle and David Diller
 * 
 * This file is part of the VS Viewer 3D.
 * The contents are covered by the terms of the BSD license which
 * is included in the file license.txt, found at the root of the VS Viewer 3D
 * source tree.
 */

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Split implements ActionListener, ListSelectionListener,
		ChangeListener {
	// Frame of the view
	private Dialog frame;

	// Allows to visual see all the possible headers and add/remove them
	private DefaultListModel<String> headerCom;
	private JList<String> header;

	// Change point of splitting and the value
	private JSlider slide;
	private JLabel value;

	// Name of the new header
	private JTextField name;

	// Allows to access and add new value to molecules
	private SimpleGui sg;

	// Allows to scroll on the JList
	private JScrollPane scroller;

	// Creates a basic dialog for the split maker
	public Split(SimpleGui s) {
		sg = s;
		frame = new Dialog((JFrame) null);
		slide = new JSlider();
		value = new JLabel();
		name = new JTextField(10);
		headerCom = new DefaultListModel<String>();

		header = new JList<String>(headerCom);
		header.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		header.setSelectedIndex(-1);
		header.setVisibleRowCount(5);
		header.setFixedCellHeight(16);
		header.addListSelectionListener(this);

		slide.setPaintTicks(true);
		slide.setPaintLabels(true);
		slide.setPaintTrack(true);
		slide.addChangeListener(this);

		scroller = new JScrollPane(header);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		JPanel panel1 = new JPanel();
		panel1.add(scroller);

		Box east = Box.createVerticalBox();
		east.add(new JLabel("Set value:"));
		east.add(slide);
		east.add(value);

		JPanel panel = new JPanel();
		panel.add(new JLabel("Class Name:"));
		panel.add(name);

		east.add(panel);

		JButton go = new JButton("Sort");
		go.addActionListener(this);

		east.add(go);
		JPanel pan = new JPanel();
		pan.add(panel1);
		pan.add(east);

		frame.add(pan);

		frame.pack();
		frame.setResizable(false);
		frame.addWindowListener(new DClosing());
	}

	// Shows the dialog window and sets possible headers to select
	public void show() {
		frame.setVisible(true);
		update();

		int i = 0;

		try {
			i = header.getSelectedIndex();
		} catch (Exception e) {
			i = 0;
		}

		if (i <= 0) {
			i = 0;
			header.setSelectedIndex(0);
		} else if (i >= sg.plot.data.header.length) {
			i %= sg.plot.data.header.length;
		}

		set(sg.plot.data.header[i]);
	}

	// Sets the minimum/maximum of the slider
	public void set(String s) {
		ArrayList<Double> vals = new ArrayList<Double>();

		// Gets the list of all the data for that header
		for (Molecule m : sg.plot.data.mol) {
			double v = m.containsLabel(s);

			if (v != Double.NEGATIVE_INFINITY) {
				vals.add(v);
			}
		}

		// Calls methods to get the minimum and maximum of the data set
		double min = Calculator.minmax(vals, true);
		double max = Calculator.minmax(vals, false);

		// Sets the values in the slider
		slide.setMinimum((int) min);
		slide.setMaximum((int) max);

		value.setText("Value: " + slide.getValue());
	}

	// Updates possible header to select
	public void update() {
		for (int i = 0; i < sg.plot.data.header.length; i++) {
			if (!headerCom.contains(sg.plot.data.header[i])) {
				headerCom.add(i, sg.plot.data.header[i]);
			}
		}
	}

	@SuppressWarnings("static-access")
	// Runs the method of calculation for the splitting
	public void actionPerformed(ActionEvent e) {
		if (name.getText().trim().equals(""))
			return;// Simple check for if the name works as a header
		frame.setVisible(false);// Closes the window

		String label = (String) header.getSelectedValue();// Gets the name for
															// the new header

		// Parses through the molecules and sets the new header and values
		for (Molecule m : sg.plot.data.mol) {
			m.newName(label, slide.getValue(), name.getText());
		}

		// Updates all the things that need the header
		sg.plot.data.updateHeader();
		sg.addItems();

		// Clears the name text box
		name.setText("");

		// Tells the SimpleGui that the file has been changed
		sg.changed = true;
	}

	// Calls method to set minimum/maximum of slider
	public void valueChanged(ListSelectionEvent lse) {
		set((String) header.getSelectedValue());
	}

	// Allows for the window to be closed
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

		public void windowClosing(WindowEvent e) {
			Window localWindow = e.getWindow();
			localWindow.setVisible(false);
		}
	}

	// Handles the slider being changed
	public void stateChanged(ChangeEvent arg0) {
		value.setText("Value: " + slide.getValue());
	}
}