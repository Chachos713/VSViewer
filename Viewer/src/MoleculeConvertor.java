/**
 * Copyright (C) 2014 Kyle and David Diller
 * 
 * This file is part of the VS Viewer 3D.
 * The contents are covered by the terms of the BSD license which
 * is included in the file license.txt, found at the root of the VS Viewer 3D
 * source tree.
 */

import java.util.*;

public final class MoleculeConvertor {
	// List of points for the surface
	static ArrayList<Vector3d> surf;
	// Distance from the molecule for the surface
	static double RADIUS = 1.7;

	// Changes the Molecule used in this project to the ones used by the
	// AstexViewer
	public static astex.Molecule convert(Molecule m) {
		// Gets all the atoms bonds and polarity
		Atom[] atm = m.getAtoms();
		Bond[] bnd = m.getBonds();
		boolean[] polar = m.getPolar();

		// Used to determine what molecule to skip later on
		boolean[] skip = new boolean[atm.length];

		String nme = m.getName();

		// Determines which atoms to remove later on
		ArrayList<astex.Atom> nonpolarH = new ArrayList<astex.Atom>();

		// Creates and sets the name of the new Molecule
		astex.Molecule newM = new astex.Molecule();
		newM.setName(nme);

		for (int i = 0; i < atm.length; i++) {// Adds all the atoms
			// Creates and set location and Atom type
			astex.Atom a = newM.addAtom();
			a.x = atm[i].getX();
			a.y = atm[i].getY();
			a.z = atm[i].getZ();
			a.setElement(element(atm[i].getType()));

			if (!polar[i] && atm[i].getType().equals("H")) {// Adds the
															// non-polar
															// hydrogens to a
															// list to remove
															// later
				nonpolarH.add(a);
				skip[i] = true;
			}
		}

		for (Bond b : bnd) {// Creates the bonds for the new Molecule
			if (!(skip[b.getA1()] || skip[b.getA2()])) {// Only add the bond if
														// neither is connected
														// to a non-polar
														// hydrogen
				astex.Bond bo = newM.addBond(b.getA1(), b.getA2(), b.getType());
				bo.setBondWidth(2);
			}
		}

		for (astex.Atom a : nonpolarH) {// Removes all the non-polar hydrogens
			newM.removeAtom(a);
		}

		return newM;
	}

	// Changes our Atom type(String) to the location in the periodic
	// table(Integer)
	private static int element(String type) {
		if (type.equals("C"))
			return 6;
		if (type.equals("O"))
			return 8;
		if (type.equals("N"))
			return 7;
		if (type.equals("F"))
			return 9;
		if (type.equals("Cl"))
			return 17;
		if (type.equals("S"))
			return 16;
		if (type.equals("H"))
			return 1;

		return 100;
	}

	// Calculates location of all points
	private static void MakeSurfacePoints() {
		// Values used in calculating the points
		final double dx = 0.25;
		Vector3d v;
		double theta, dtheta, phi, dphi, r, z;
		int n, i;
		surf = new ArrayList<Vector3d>();

		dtheta = 2.0 * Math.asin(dx / (2.0 * RADIUS)); // essentially =
														// dx/radius
		n = (int) (Math.PI / dtheta) + 1;
		dtheta = Math.PI / (double) n;
		theta = dtheta / 2.0; // this makes the sampling even from top to bottom
		while (theta < Math.PI) {
			z = Math.cos(theta) * RADIUS;
			r = Math.sin(theta) * RADIUS;
			n = 0;
			if (2.0 * r > dx) {
				dphi = 2.0 * Math.asin(dx / (2.0 * r)); // essentially dx/r
				n = (int) (2.0 * Math.PI / dphi) + 1;
			}
			if (n < 4) {
				n = 4;
			}
			dphi = 2.0 * Math.PI / (double) n;
			phi = 0.0;
			for (i = 0; i < n; i++) {
				v = new Vector3d();
				v.z = z;
				phi = 2.0 * Math.PI * (double) i / (double) n;
				v.x = r * Math.cos(phi);
				v.y = r * Math.sin(phi);
				surf.add(v);
			}
			theta += dtheta;
		}

	}

	// Uses the current Astex Molecule and makes a dot surface for it
	public static astex.Molecule createSurface(astex.Molecule mol) {
		// Values in storing and calculating location
		astex.Molecule S = new astex.Molecule();
		S.setName("surface_" + mol.getName());
		final double Ri = RADIUS * RADIUS;
		double dx, dy, dz;
		int i, j, k, N;
		Vector3d v;

		if (surf == null) {// Calls MakeSurfacePoints() if was never called
			MakeSurfacePoints();
		}

		// Number of surface points and list of which ones to keep
		N = surf.size();
		boolean[] keep = new boolean[N];

		for (i = 0; i < mol.getAtomCount(); i++) {// Goes through each Atom
			for (k = 0; k < N; k++) {// Sets all the values in the list to true
				keep[k] = true;
			}

			for (j = 0; j < mol.getAtomCount(); j++) {// Goes through each of
														// the other atoms
				v = new Vector3d();
				if (j != i && mol.getAtom(j).getElement() != 1) {// If the Atom
																	// is not a
																	// Hydrogen
																	// and not
																	// the
																	// current
																	// one
					// Calculates the difference in x, y, and z of the two
					// molecules
					v.x = mol.getAtom(i).getX() - mol.getAtom(j).getX();
					v.y = mol.getAtom(i).getY() - mol.getAtom(j).getY();
					v.z = mol.getAtom(i).getZ() - mol.getAtom(j).getZ();

					// Checks that the molecules are close enough to affect the
					// surface
					if (v.x * v.x + v.y * v.y + v.z * v.z < 4.0 * Ri) {
						for (k = 0; k < N; k++) {
							if (keep[k]) {// Make sure the atom is not already
											// removed
								// Calculates the new location of where a
								// surface point would be
								dx = v.x + surf.get(k).x;
								dy = v.y + surf.get(k).y;
								dz = v.z + surf.get(k).z;
								if (dx * dx + dy * dy + dz * dz <= Ri) {// Makes
																		// sure
																		// that
																		// it is
																		// outside
																		// of
																		// the
																		// range
																		// of
																		// the
																		// other
																		// molecule
									keep[k] = false;
								}
							}
						}
					}
				}
			}

			for (k = 0; k < N; k++) {// Creates and adds the molecule to the
										// list
				if (keep[k]) {
					astex.Atom a = S.addAtom();
					a.x = mol.getAtom(i).getX() + surf.get(k).x;
					a.y = mol.getAtom(i).getY() + surf.get(k).y;
					a.z = mol.getAtom(i).getZ() + surf.get(k).z;
					a.setElement(mol.getAtom(i).getElement());
				}
			}
		}
		return S;
	}
}