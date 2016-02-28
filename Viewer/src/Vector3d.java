/**
 * Copyright (C) 2014 Kyle and David Diller
 * 
 * This file is part of the VS Viewer 3D.
 * The contents are covered by the terms of the BSD license which
 * is included in the file license.txt, found at the root of the VS Viewer 3D
 * source tree.
 */

/** Saves three doubles for later use */
public class Vector3d {
	// The point in the vector
	public double x, y, z;

	// Creates a basic and empty vector
	public Vector3d() {
	}

	public Vector3d(double a, double b, double c) {
		x = a;
		y = b;
		z = c;
	}
}
