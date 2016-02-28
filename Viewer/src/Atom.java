/**
 * Copyright (C) 2014 Kyle and David Diller
 * 
 * This file is part of the VS Viewer 3D.
 * The contents are covered by the terms of the BSD license which
 * is included in the file license.txt, found at the root of the VS Viewer 3D
 * source tree.
 */

import java.util.*;
import java.text.DecimalFormat;

/** Saves location and type of atom */
class Atom {
	protected String type;// Element
	protected double x, y, z, x2D, y2D;// Coordinates for drawing
	private ArrayList<Integer> partners;// List of atoms bonded to

	public Atom() {
		partners = new ArrayList<Integer>();
	}// Instantiates partners and Atom

	// Returns Coordinates
	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	public double getX2D() {
		return x2D;
	}

	public double getY2D() {
		return y2D;
	}

	// Returns Element
	public String getType() {
		return type;
	}

	// Returns List of atoms bonded to
	public ArrayList<Integer> getMates() {
		return partners;
	}

	// Sets Coordinates
	public void setX(double X) {
		x = X;
	}

	public void setY(double Y) {
		y = Y;
	}

	public void setZ(double Z) {
		z = Z;
	}

	public void setX2D(double X) {
		x2D = X;
	}

	public void setY2D(double Y) {
		y2D = Y;
	}

	// Sets element
	public void setType(String Type) {
		type = Type;
	}

	// Adds an atom to the list
	public void addPartner(int i) {
		partners.add(i);
	}

	// Reads a SDF line
	public boolean ReadLineSDF(String strLine, boolean is2D) {
		try {
			String strData;
			if (is2D) {// If it is a 2D file
				strData = strLine.substring(0, 10).trim();
				x2D = Double.parseDouble(strData);

				strData = strLine.substring(10, 20).trim();
				y2D = Double.parseDouble(strData);

				strData = strLine.substring(20, 30).trim();
				z = Double.parseDouble(strData);

				if (z != 0)
					return false;

				type = strLine.substring(30, 33).trim();
			} else {// If it is 3D
				strData = strLine.substring(0, 10).trim();
				x = Double.parseDouble(strData);

				strData = strLine.substring(10, 20).trim();
				y = Double.parseDouble(strData);

				strData = strLine.substring(20, 30).trim();
				z = Double.parseDouble(strData);

				type = strLine.substring(30, 33).trim();
			}
		} catch (Exception e) {// Catch exception if any
			System.err.println("Atom Exception Error: " + e.getMessage());
			return false;
		}
		return true;
	}

	// Returns a string for SDF or MOL files
	public String getMolLine(boolean d3) {
		String line;
		String field;
		int ns;
		int i;

		DecimalFormat format = new DecimalFormat("0.0000");

		line = "";
		if (d3) {// Outputs as a 3D file
			field = format.format(x);
			ns = 10 - field.length();
			for (i = 0; i < ns; i++) {
				line += " ";
			}
			line += field;

			field = format.format(y);
			ns = 10 - field.length();
			for (i = 0; i < ns; i++) {
				line += " ";
			}
			line += field;

			field = format.format(z);
			ns = 10 - field.length();
			for (i = 0; i < ns; i++) {
				line += " ";
			}
			line += field;
			line += " ";
			line += type;
			ns = 3 - type.length();
			for (i = 0; i < ns; i++) {
				line += " ";
			}
		} else {// OutPuts as a 2D file
			field = format.format(x2D);
			ns = 10 - field.length();
			for (i = 0; i < ns; i++) {
				line += " ";
			}
			line += field;

			field = format.format(y2D);
			ns = 10 - field.length();
			for (i = 0; i < ns; i++) {
				line += " ";
			}
			line += field;

			field = format.format(0);
			ns = 10 - field.length();
			for (i = 0; i < ns; i++) {
				line += " ";
			}
			line += field;

			line += " ";
			line += type;
			ns = 3 - type.length();
			for (i = 0; i < ns; i++) {
				line += " ";
			}
		}

		line += " 0  0  0  0  0  0  0  0  0  0  0  0";
		return line;
	}

	// Copies the atom to another location in memory
	public Atom copy() {
		Atom a = new Atom();
		a.type = type;
		a.x = x;
		a.y = y;
		a.z = z;
		a.x2D = x2D;
		a.y2D = y2D;

		a.partners = new ArrayList<Integer>();

		for (int i : partners) {
			a.partners.add(i);
		}

		return a;
	}

	// Distance formula between current atom and a point in space for 3D finger
	// print
	public double disSQ(double ax, double ay, double az) {
		double dx = ax - x;
		double dy = ay - y;
		double dz = az - z;

		return (dx * dx) + (dy * dy) + (dz * dz);
	}
}