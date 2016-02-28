/**
 * Copyright (C) 2014 Kyle and David Diller
 * 
 * This file is part of the VS Viewer 3D. The contents are covered by the terms
 * of the BSD license which is included in the file license.txt, found at the
 * root of the VS Viewer 3D source tree.
 */

public class DataPt {
	// Name of data
	private String label;
	// Value of data
	private double value;

	// Creates a simple data point
	public DataPt(double v, String l) {
		label = l;
		value = v;
	}

	// Returns value of data
	public double getValue() {
		return value;
	}

	// Returns name of data
	public String getLabel() {
		return label;
	}

	// Returns data point in string form
	public String toString() {
		return value + " " + label;
	}

	// Sets value of data
	public void setPoint(double d) {
		value = d;
	}
}
