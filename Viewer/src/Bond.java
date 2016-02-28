/**
 * Copyright (C) 2014 Kyle and David Diller
 * 
 * This file is part of the VS Viewer 3D. The contents are covered by the terms
 * of the BSD license which is included in the file license.txt, found at the
 * root of the VS Viewer 3D source tree.
 */

class Bond {
	// Atoms connected by this bond
	private int a1;
	private int a2;

	// Number of bonds (single, double, triple, etc.)
	private int type;

	// Creates bond with the two atoms and type
	public Bond(int A1, int A2, int bt) {
		a1 = A1;
		a2 = A2;
		type = bt;
	}

	// Creates a basic bond
	public Bond() {
		a1 = -1;
		a2 = -1;
		type = -1;
	}

	// Returns the two atoms
	public int getA1() {
		return a1;
	}

	public int getA2() {
		return a2;
	}

	// Returns bond type
	public int getType() {
		return type;
	}

	// Sets atoms
	public void setA1(int A1) {
		a1 = A1;
	}

	public void setA2(int A2) {
		a2 = A2;
	}

	// Sets bond type
	public void setType(int bt) {
		type = bt;
	}

	// Creates a new bond with the same atoms and bond type
	public Bond copy() {
		Bond b = new Bond(a1, a2, type);
		return b;
	}

	// Creates String for VSV bonds
	public String writeBond() {
		return " " + (a1 + 1) + ":" + (1 + a2) + ":" + type;
	}

	// Creates a bond after reading a SDF/MOL file line
	public boolean ReadLineSDF(String strLine) {
		try {
			String strData;

			strData = strLine.substring(0, 3).trim();
			a1 = Integer.parseInt(strData);
			a1--; // index is 0 based rather than 1 based

			strData = strLine.substring(3, 6).trim();
			a2 = Integer.parseInt(strData);
			a2--; // index is 0 based rather than 1 based

			strData = strLine.substring(6, 9).trim();
			type = Integer.parseInt(strData);
		} catch (Exception e) {// Catch exception if any
			System.err.println("Bond Exception Error: " + e.getMessage());
			return false;
		}
		return true;
	}
}