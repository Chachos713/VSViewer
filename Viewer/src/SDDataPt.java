/**
 * Copyright (C) 2014 Kyle and David Diller
 * 
 * This file is part of the VS Viewer 3D. The contents are covered by the terms
 * of the BSD license which is included in the file license.txt, found at the
 * root of the VS Viewer 3D source tree.
 */

public class SDDataPt implements Comparable<SDDataPt> {

	// Used when the value is an approximation
	final String qualifier = "~<>";
	// Value of data
	private String value;

	// Creates a empty data point
	public SDDataPt() {
		value = new String();
	}

	// Creates one with a value
	public SDDataPt(String s) {
		value = s;
	}

	// clears the data
	public void clear() {
		value = "";
	}

	// Adds a values to the data
	public void create(String l) {
		value = l;
	}

	// Returns the data
	public String getString() {
		return value;
	}

	// Returns the number from the data
	public Double getNumber() {
		try {
			String val = value;
			if (getQualifier() >= 0) {
				val = value.substring(1);
			}
			Double d = Double.valueOf(val);
			return d;
		} catch (NumberFormatException err) {
			return 0.0;
		}
	}

	// Returns the approximation in reference to 10
	public int getQualifier() {
		if (value.length() > 0) {
			return qualifier.indexOf(value.substring(0, 1));
		}
		return -1;
	}

	@Override
	// Compares this data with another
	public int compareTo(SDDataPt s) {
		if (amINumber()) {
			if (s.amINumber()) {
				if (getNumber() < s.getNumber()) {
					return -1;
				}
				return 1;
			} else {
				return -1;
			}
		}
		if (s.amINumber()) {
			return 1;
		}
		return getString().compareTo(s.getString());
	}

	// Returns whether the data is a number or not
	public boolean amINumber() {
		try {
			Double.parseDouble(value);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

}