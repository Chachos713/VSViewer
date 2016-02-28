/**
 * Copyright (C) 2014 Kyle and David Diller
 * 
 * This file is part of the VS Viewer 3D.
 * The contents are covered by the terms of the BSD license which
 * is included in the file license.txt, found at the root of the VS Viewer 3D
 * source tree.
 */

import java.util.*;

public class Path {
	// List of atoms in path
	private ArrayList<Integer> path;

	// Creates an empty path
	public Path() {
		path = new ArrayList<Integer>();
	}

	// Basic methods used by ArrayList to get size and a value in a location
	public int length() {
		return path.size();
	}

	public int get(int i) {
		return path.get(i);
	}

	// Adds a value to the path but if it is already in it tells Molecule to
	// remove this
	public boolean add(int i) {
		int loc = path.indexOf(i);

		if (loc == -1) {
			path.add(i);
			return true;
		}

		return false;
	}

	// Creates a copy of current path
	public Path copy() {
		Path p = new Path();

		for (Integer i : path) {
			p.path.add(i);
		}

		return p;
	}

	// Prints out values in path
	public void print() {
		for (int i : path) {
			System.out.print(i + " ");
		}
		System.out.println();
	}
}
