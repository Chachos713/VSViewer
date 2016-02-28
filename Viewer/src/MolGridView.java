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
import java.io.*;

@SuppressWarnings("serial")
public class MolGridView extends JPanel {
	// Fields used to know which molecules to show, which are showing, and the
	// location and dimensions of the molecule
	boolean ready = false;
	ArrayList<Molecule> mol;
	int xlo;
	int xhi;
	int ylo;
	int yhi;
	int borderx, bordery;
	int mol_width;
	int mol_height;
	int nshow = 15;
	int numx = 3;
	double diameter = 10.0;
	int molshow;

	// Used to give values for location and size of the molecules
	public boolean Open(int width, int height, int mw, int mh, int bx, int by) {
		xlo = borderx;
		ylo = bordery;
		xhi = borderx + width;
		yhi = bordery + height;
		borderx = bx;
		bordery = by;
		mol_width = mw;
		mol_height = mh;
		molshow = -1;
		ready = true;
		molshow = 0;
		return true;

	}

	// Draws a Molecule
	public void DrawMol(Graphics g, Molecule molIn, int mol_num, double mxlo,
			double mxhi, double mylo, double myhi, boolean t) {
		if (molIn == null)
			return;// Make sure molecule to be drawn is not null

		double ulo, uhi, vlo, vhi;
		double U, V;
		double s, S, S1, S2;
		double x1, x2, y1, y2;
		double vx, vy;
		int a1, a2, bt;
		String tmp;
		int i;

		// dy is an arbitrary value used to prevent the molecule from touching
		// the name
		final double dy = 20;

		// Draws box around the molecule
		g.setColor(Color.black);
		g.drawLine((int) mxlo, (int) mylo, (int) mxhi, (int) mylo);
		g.drawLine((int) mxhi, (int) mylo, (int) mxhi, (int) myhi);
		g.drawLine((int) mxhi, (int) myhi, (int) mxlo, (int) myhi);
		g.drawLine((int) mxlo, (int) myhi, (int) mxlo, (int) mylo);

		// Gives boundaries to draw molecule in
		mxlo += borderx;
		myhi += bordery;
		mxhi -= borderx;
		mylo -= bordery;

		// Calculate the minimum and maximum of the x and y coordinates of the
		// molecule
		ulo = molIn.getAtom(0).getX2D();
		uhi = molIn.getAtom(0).getX2D();
		vlo = molIn.getAtom(0).getY2D();
		vhi = molIn.getAtom(0).getY2D();
		for (i = 0; i < molIn.getNAtoms(); i++) {
			U = molIn.getAtom(i).getX2D();
			V = molIn.getAtom(i).getY2D();
			if (U > uhi) {
				uhi = U;
			}
			if (U < ulo) {
				ulo = U;
			}
			if (V > vhi) {
				vhi = V;
			}
			if (V < vlo) {
				vlo = V;
			}
		}

		// Calculated to convert file coordinates to the screen coordinates
		S1 = (mxhi - mxlo) / (uhi - ulo);
		S2 = -(myhi - (mylo - dy)) / (vhi - vlo);
		S = S1;
		if (S2 < S) {
			S = S2;
		}

		//S is the smallest ratio of pixels/unit
		S1 = (mxhi - mxlo - S * (uhi - ulo)) / 2.0;
		S2 = (myhi - (mylo - dy) + S * (vhi - vlo)) / 2.0;

		// Draws the lines for the bonds
		for (i = 0; i < molIn.getNBonds(); i++) {
			// Gets the values to draw the bond (type and atoms)
			a1 = molIn.getBond(i).getA1();
			a2 = molIn.getBond(i).getA2();
			bt = molIn.getBond(i).getType();

			Atom atm1 = molIn.getAtom(a1);
			Atom atm2 = molIn.getAtom(a2);

			// If neither atom is a Hydrogen, then draw the bond
			if (!atm1.getType().equals("H") && !atm2.getType().equals("H")) {
				// Location of the two atoms
				x1 = atm1.getX2D();
				y1 = atm1.getY2D();
				x2 = atm2.getX2D();
				y2 = atm2.getY2D();

				// Calculates coordinates for placement on the form
				x1 = S * (x1 - ulo) + mxlo + S1;
				y1 = -S * (y1 - vlo) + (mylo - dy) + S2;
				x2 = S * (x2 - ulo) + mxlo + S1;
				y2 = -S * (y2 - vlo) + (mylo - dy) + S2;

				// Offset for double or triple bonds to be off center
				vx = y1 - y2;
				vy = x2 - x1;
				s = Math.sqrt(vx * vx + vy * vy);
				vx /= s;
				vy /= s;

				if (bt % 2 == 1) {// Draws a single, or triple bond
					g.drawLine((int) x1, (int) y1, (int) x2, (int) y2);
					if (bt == 3) {// If a triple bond
						g.drawLine((int) (x1 + vx * 5.0),
								(int) (y1 + vy * 5.0), (int) (x2 + vx * 5.0),
								(int) (y2 + vy * 5.0));
						g.drawLine((int) (x1 - vx * 5.0),
								(int) (y1 - vy * 5.0), (int) (x2 - vx * 5.0),
								(int) (y2 - vy * 5.0));
					}
				} else if (bt == 2) {// DRaws a double bond
					g.drawLine((int) (x1 + vx * 2.5), (int) (y1 + vy * 2.5),
							(int) (x2 + vx * 2.5), (int) (y2 + vy * 2.5));
					g.drawLine((int) (x1 - vx * 2.5), (int) (y1 - vy * 2.5),
							(int) (x2 - vx * 2.5), (int) (y2 - vy * 2.5));
				}
			}
		}

		// Font to display the atoms
		Font f = new Font("SansSerif", Font.BOLD, 12);
		g.setFont(f);
		for (i = 0; i < molIn.getNAtoms(); i++) {// Loops through the atoms
			// Location of the atom
			x1 = molIn.getAtom(i).getX2D();
			y1 = molIn.getAtom(i).getY2D();

			// Calculates the screen coordinates
			x1 = S * (x1 - ulo) + mxlo + S1;
			y1 = -S * (y1 - vlo) + (mylo - dy) + S2;

			// Type of atom
			tmp = molIn.getAtom(i).getType();

			// If not a Hydrogen or Carbon, clear the space for the symbol and
			// then draw the symbol
			if (!tmp.equals("H") && !tmp.equals("C")) {
				g.setColor(Color.white);
				g.fillOval((int) (x1 - 8.0), (int) (y1 - 8.0), 16, 16);
				g.setColor(Color.black);
				drawCenteredString(tmp, (int) (x1), (int) (y1), g);
			}
		}

		// Font to draw the name
		f = new Font("SansSerif", Font.BOLD, 12);
		g.setFont(f);
		g.setColor(Color.black);
		if (t)// Put the location in the list
			tmp = mol_num + ":  " + molIn.getName();
		else
			// No location in list
			tmp = molIn.getName();

		// Draws the name of the molecule in the middle of the box
		drawCenteredString(tmp, (int) ((mxhi + mxlo) / 2.0), (int) (mylo), g);

	}

	// Goes to the next set of molecules
	public void Next() {
		molshow += nshow;
		while (molshow >= mol.size()) {
			molshow -= mol.size();
		}
	}

	// Goes to the previous set of molecules
	public void Previous() {
		molshow -= nshow;
		while (molshow < 0) {
			molshow += mol.size();
		}
	}

	// Method to do all the drawing
	public void paintComponent(Graphics g) {

		if (!ready) {
			return;
		}

		// Clear the screen
		g.setColor(Color.white);
		g.fillRect(0, 0, 20000, 20000);
		g.setColor(Color.black);

		int j;
		int k;
		double mxlo = 0.0;
		double mylo = (double) (mol_height);
		double mxhi = (double) (mol_width);
		double myhi = 0.0;

		j = molshow;// Starting point for Molecules to draw
		for (k = 0; k < nshow; k++) {// Go through the molecules that could be
										// displayed
			if (k >= mol.size())
				break;// Stop if already passed size of screen

			if (mxhi > xhi + .1) {// When reached end of row, start a new one
				mxlo = 0.0;
				mxhi = (double) (mol_width);
				mylo += (double) (mol_height);
				myhi += (double) (mol_height);
			}
			if (j >= mol.size()) {// When reached end of list, start back at 1
									// (or to a computer 0)
				j -= mol.size();
			}
			DrawMol(g, mol.get(j), j + 1, mxlo + borderx, mxhi + borderx, mylo
					+ bordery, myhi + bordery, true);// Draws the molecule
			j++;
			mxlo += (double) (mol_width);// Increments the x coordinates
			mxhi += (double) (mol_width);
		}

	}

	// Draws a string centered around the point (u, v)
	public void drawCenteredString(String s, int u, int v, Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		int x = u - fm.stringWidth(s) / 2;
		int y = v + fm.getAscent() / 2;
		g.drawString(s, x, y);
	}

	// Sets the list of molecules
	public void setMol(ArrayList<Molecule> m) {
		mol = m;
		molshow = 0;
	}

	// Gets which molecules was selected with the mouse click
	public int getSelected(int x, int y) {
		y -= 50;
		y = y / mol_height;
		x = x / mol_width;

		int i = y * numx + x;

		return (molshow + i) % mol.size();
	}

	// Used to set the new values for displaying the molecules when the form
	// size changes
	public void resize(int n, int x, int w, int h) {
		nshow = n;
		numx = x;

		xhi = borderx + w;
		yhi = bordery + h;
	}

	// Used to save the set of molecules
	public void save(char type, String file, SimpleGui sg) {
		File f = new File(file);

		switch (type) {
		case '2':// 2D SDF
			Molecule.WriteSDFFile(mol, f, false);
			break;
		case '3':// 3D SDF
			Molecule.WriteSDFFile(mol, f, true);
			break;
		case 'v':// VSV
			try {
				FileWriter os = new FileWriter(f);

				DataSet d = sg.plot.data;

				os.write(d.lo2d + " " + d.hi2d + " " + d.xlo + " " + d.xhi
						+ " " + d.ylo + " " + d.yhi + " " + d.zlo + " " + d.zhi
						+ "\n");// Used to calculate coordinates of atoms

				for (Molecule m : mol) {
					m.write(os, d.lo2d, d.hi2d, d.xlo, d.xhi, d.ylo, d.yhi,
							d.zlo, d.zhi);
				}

				os.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	// Flips the list of molecules
	public void flip() {
		ArrayList<Molecule> m = new ArrayList<Molecule>();

		for (int i = mol.size() - 1; i >= 0; i--) {
			m.add(mol.get(i));
		}

		mol = m;
	}
}