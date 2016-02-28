/**
 * Copyright (C) 2014 Kyle and David Diller
 * 
 * This file is part of the VS Viewer 3D.
 * The contents are covered by the terms of the BSD license which
 * is included in the file license.txt, found at the root of the VS Viewer 3D
 * source tree.
 */

import java.util.*;

public class SDField {
	// Name of data set
	private String label;
	// List of data in set
	private ArrayList<SDDataPt> data;

	// Creates an empty data set
	public SDField() {
		label = new String();
		data = new ArrayList<SDDataPt>();
	}

	// Clears the data set
	public void clear() {
		label = "";
		data.clear();
	}

	// Creates a empty data set
	public void create(String l) {
		clear();
		label = l;
	}

	// Returns name
	public String getLabel() {
		return label;
	}

	// Adds a data point
	public void addData(String d) {
		SDDataPt dp = new SDDataPt(d);
		data.add(dp);
	}

	// Returns the number of data points
	public int getNData() {
		return data.size();
	}

	// Returns data at the specific location
	public SDDataPt getData(int i) {
		return data.get(i);
	}

	// Prints out the data
	public void print(String s) {
		System.out.println(s + label);
		for (SDDataPt d : data) {
			System.out.println(s + "      " + d.getString());
		}
	}

	// Returns all data in set and values
	public String getString(String s) {

		String msg = s + label + ":";
		if (data.size() != 1) {
			for (SDDataPt d : data) {
				msg += "\n" + s + "    " + d.getString();
			}
		} else {
			msg += "   " + data.get(0).getString();
		}
		return msg;
	}

	// Sort the data
	public void sortFields() {
		Collections.sort(data);
	}

	// Returns if any data point is a number
	public Boolean amINumber() {
		for (SDDataPt dp : data) {
			if (dp.amINumber()) {
				return true;
			}
		}
		return false;
	}

	// Returns the median of data set
	public Double getNumber() {
		ArrayList<Double> val = new ArrayList<Double>();
		int k;

		for (SDDataPt dp : data) {
			if (dp.amINumber()) {
				val.add(dp.getNumber());
			}
		}
		Collections.sort(val);
		if (val.size() == 0) {
			return 0.0; // this actually was not a number
		} else if ((val.size() % 2) == 0) {
			k = val.size() / 2; // k is at least 1
			return (val.get(k) + val.get(k - 1)) / 2.0;
		} else {
			k = (val.size() - 1) / 2;
			return val.get(k);
		}

	}

	// Compares against another data set
	public int compareTo(SDField s) {
		if (amINumber()) {
			if (s.amINumber()) {
				if (getNumber() < s.getNumber()) {
					return -1;
				}
				if (getNumber() == s.getNumber()) {
					return 0;
				}
				return 1;
			}
			return -1;
		}
		if (s.amINumber()) {
			return 1;
		}
		return getData(0).compareTo(s.getData(0));
	}
}