/**
 * Copyright (C) 2014 Kyle and David Diller
 * 
 * This file is part of the VS Viewer 3D.
 * The contents are covered by the terms of the BSD license which
 * is included in the file license.txt, found at the root of the VS Viewer 3D
 * source tree.
 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

class Molecule implements Comparable<Molecule> {
	// Fields of the name, atoms, bonds, polarity, comments, data, and
	// fingerprints for comparision
	private String name;
	private Atom[] atm;
	private int[] atmType;
	private static final int maxType = 6;
	private Bond[] bnd;
	private boolean[] polarH;
	private int na;
	private int nb;
	private ArrayList<Comment> comments = new ArrayList<Comment>();
	private ArrayList<DataPt> data = new ArrayList<DataPt>();
	private String d2 = "", d3 = "";
	private double percent, percent2D;
	private static ArrayList<Vector3d> vec = new ArrayList<Vector3d>();
	private static final double dx = 1.5;
	private boolean d22, d33;
	private ArrayList<Integer> fp2d, fp3d;
	public static String sortBy;
	int index;

	// Methods to set or get values from the molecule
	public void setName(String n) {
		name = n;
	}

	public String getName() {
		return name;
	}

	public int getNAtoms() {
		return na;
	}

	public int getNBonds() {
		return nb;
	}

	public Comment getComment(int i) {
		return comments.get(i);
	}

	public Atom getAtom(int i) {
		return atm[i];
	}

	public Bond getBond(int i) {
		return bnd[i];
	}

	public boolean AmIPolarH(int i) {
		return polarH[i];
	}

	public void addComment(String c) {
		comments.add(new Comment(c));
	}

	public void removeComment(int i) {
		comments.remove(i);
	}

	public int getNumCom() {
		return comments.size();
	}

	public Atom[] getAtoms() {
		return atm;
	}

	public Bond[] getBonds() {
		return bnd;
	}

	public boolean[] getPolar() {
		return polarH;
	}

	public boolean is2D() {
		return d22;
	}

	public boolean is3D() {
		return d33;
	}

	public void addData(String name, double val) {
		data.add(new DataPt(val, name));
	}

	public void setPercent(double p) {
		percent = p;
	}

	public void setPercent2D(double p) {
		percent2D = p;
	}

	// Reads a section of a SDF file
	public boolean ReadNextSDF(BufferedReader br, boolean is2D) {
		String strLine = "";
		try {
			String strData;
			na = 0;
			nb = 0;

			if ((strLine = br.readLine()) == null) {
				System.err.println("End of File 1");
				return false;
			}
			name = strLine; // Sets the name
			if ((strLine = br.readLine()) == null) { // skip
				System.err.println("End of File 2");
				return false;
			}

			if ((strLine = br.readLine()) == null) { // skip
				System.err.println("End of File 3");
				return false;
			}

			if ((strLine = br.readLine()) == null) { // counts line
				System.err.println("End of File 4");
				return false;
			}

			// Sets the number of atoms and bonds
			strData = strLine.substring(0, 3).trim();
			na = Integer.parseInt(strData);
			strData = strLine.substring(3, 6).trim();
			nb = Integer.parseInt(strData);

			if (na == 0) {
				return true;
			}

			// Reads the atoms and their location
			int i;
			atm = new Atom[na];
			polarH = new boolean[na];
			for (i = 0; i < na; i++) {
				if ((strLine = br.readLine()) == null) {
					System.err.println("End of File 5");
					return false;
				}
				atm[i] = new Atom();
				if (!atm[i].ReadLineSDF(strLine, is2D))
					return false;
				polarH[i] = false;
			}

			// Reads the bonds
			bnd = new Bond[nb];
			for (i = 0; i < nb; i++) {
				if ((strLine = br.readLine()) == null) {
					System.err.println("End of File 6");
					return false;
				}
				bnd[i] = new Bond();
				bnd[i].ReadLineSDF(strLine);
				atm[bnd[i].getA1()].addPartner(bnd[i].getA2());
				atm[bnd[i].getA2()].addPartner(bnd[i].getA1());
			}

			// Sets fields to have values added to
			comments = new ArrayList<Comment>();
			data = new ArrayList<DataPt>();
			d22 = true;
			strLine = br.readLine();
			createAtomTypes();
			create2Dfp(getPath(4));
		} catch (Exception e) {// Catch exception if any
			System.err.println("Molecule Exception Error: " + name + " : "
					+ strLine);
			e.printStackTrace();
			return false;
		}
		setPolarH();
		return true;
	}

	// Sets what the atom type is for the 2D finger print
	public void createAtomTypes() {
		atmType = new int[atm.length];

		for (int i = 0; i < atm.length; i++) {
			switch (atm[i].getType().charAt(0)) {
			case 'H':
				atmType[i] = -1;
				break;
			case 'O':
				atmType[i] = 3;
				break;
			case 'N':
				atmType[i] = 2;
				break;
			case 'C':
				if (amISingle(i))
					atmType[i] = 0;
				else
					atmType[i] = 1;
				break;
			case 'S':
				atmType[i] = 4;
				break;
			default:
				atmType[i] = 5;
			}
		}
	}

	// Checks if a carbon is bonded to less then four atoms
	public boolean amISingle(int i) {
		for (int j = 0; j < bnd.length; j++) {
			if (bnd[j].getA1() == i || bnd[j].getA2() == i) {
				if (bnd[j].getType() != 1)
					return false;
			}
		}

		return true;
	}

	// Compares the 2D finger prints of molecules
	public static double compare2D(Molecule m1, Molecule m2) {
		int numEqu = 0;
		int i1 = 0, i2 = 0;
		int k1, k2;
		ArrayList<Integer> mol1 = m1.fp2d;
		ArrayList<Integer> mol2 = m2.fp2d;

		while (i1 < mol1.size() && i2 < mol2.size()) {
			k1 = mol1.get(i1);
			k2 = mol2.get(i2);
			if (k1 == k2) {
				i1++;
				i2++;
				numEqu++;
			} else if (k1 > k2) {
				i2++;
			} else {
				i1++;
			}
		}
		return (double) (numEqu)
				/ (double) (mol1.size() + mol2.size() - numEqu);
	}

	// Creates a list of paths for the 2D finger print using a breadth first
	// searching algorithm
	public ArrayList<Path> getPath(int length) {
		ArrayList<Path> paths = new ArrayList<Path>();

		for (int i = 0; i < atm.length; i++) {
			if (atmType[i] != -1) {
				Path p = new Path();
				p.add(i);
				paths.add(p);
			}
		}

		for (int i = 1; i < length; i++) {
			paths = grow(paths);
		}

		return paths;
	}

	// Increases the size of the paths created
	public ArrayList<Path> grow(ArrayList<Path> paths) {
		ArrayList<Path> newPaths = new ArrayList<Path>();

		for (Path p : paths) {
			for (int i : atm[p.get(p.length() - 1)].getMates()) {
				if (atmType[i] < 0)
					continue;

				Path pN = p.copy();

				if (pN.add(i))
					newPaths.add(pN);
			}
		}

		return newPaths;
	}

	// Using the list of, creates a value for each path and adds it to a list
	public void create2Dfp(ArrayList<Path> paths) {
		fp2d = new ArrayList<Integer>();

		for (Path p : paths) {
			int tot = 0;
			int fac = 1;
			for (int i = 0; i < p.length(); i++) {
				int val = atmType[p.get(i)];
				val = val * fac;
				tot += val;
				fac *= maxType;
			}

			fp2d.add(tot);
		}

		Collections.sort(fp2d);
	}

	// Sets which hydrogen is polar
	public void setPolarH() {
		int i;
		int a1;
		int a2;
		for (i = 0; i < na; i++) {
			polarH[i] = false;
		}
		for (i = 0; i < nb; i++) {
			a1 = bnd[i].getA1();
			a2 = bnd[i].getA2();
			if (atm[a1].getType().equals("H")) {
				if (atm[a2].getType().equals("O")
						|| atm[a2].getType().equals("N")
						|| atm[a2].getType().equals("S")) {
					polarH[a1] = true;
				}
			}
			if (atm[a2].getType().equals("H")) {
				if (atm[a1].getType().equals("O")
						|| atm[a1].getType().equals("N")
						|| atm[a1].getType().equals("S")) {
					polarH[a2] = true;
				}
			}
		}
	}

	// Creates a copy of the molecule to another location in memory
	public Molecule copy() {
		int i;
		Molecule m = new Molecule();
		m.index = index;
		m.name = name;
		m.na = na;
		m.nb = nb;
		m.d2 = d2;
		m.d3 = d3;
		if (na > 0) {
			m.atm = new Atom[na];
			m.polarH = new boolean[na];

			for (i = 0; i < na; i++) {
				m.atm[i] = new Atom();
				m.atm[i] = atm[i].copy();
				m.polarH[i] = polarH[i];
			}
			m.bnd = new Bond[nb];
			for (i = 0; i < nb; i++) {
				m.bnd[i] = new Bond();
				m.bnd[i] = bnd[i].copy();
			}
		}

		for (Comment c : comments)
			m.comments.add(c);

		for (DataPt c : data)
			m.data.add(c);

		if (d33) {
			m.fp3d = new ArrayList<Integer>();

			for (int j : fp3d)
				m.fp3d.add(j);
		}

		if (d22) {
			m.fp2d = new ArrayList<Integer>();

			for (int v : fp2d)
				m.fp2d.add(v);

			m.atmType = new int[atmType.length];

			for (i = 0; i < atmType.length; i++) {
				m.atmType[i] = atmType[i];
			}
		}
		m.d22 = this.d22;
		m.d33 = this.d33;

		return m;
	}

	// Creates a 3D finger print
	public void createFP(double xlo, double ylo, double zlo, int Nx, int Ny,
			int Nz) {
		int i, j, k, n, a;
		double x, y, z;
		fp3d = new ArrayList<Integer>();
		double r = MoleculeConvertor.RADIUS;

		for (a = 0; a < na; a++) {
			if (!atm[a].getType().equals("H")) {
				i = (int) ((atm[a].getX() - xlo - .5) / dx);
				j = (int) ((atm[a].getY() - ylo - .5) / dx);
				k = (int) ((atm[a].getZ() - zlo - .5) / dx);

				for (Vector3d v : vec) {
					z = k + v.z;
					y = j + v.y;
					x = i + v.x;
					n = (int) (z + Nz * y + Ny * Nz * x);

					x = xlo + x * dx + dx / 2.0;
					y = ylo + y * dx + dx / 2.0;
					z = zlo + z * dx + dx / 2.0;

					if (atm[a].disSQ(x, y, z) > r * r) {
						continue;
					}

					if (n >= 0) {
						fp3d.add(n);
					}
				}
			}
		}

		Collections.sort(fp3d);
		removeDoubles();
	}

	// Removes all the duplicates from the 3D finger print
	private void removeDoubles() {
		ArrayList<Integer> temp = new ArrayList<Integer>();
		int last = -1;
		int now;

		for (int i = 0; i < fp3d.size(); i++) {
			now = fp3d.get(i);
			if (now != last) {
				temp.add(now);
			}
			last = now;
		}
		fp3d = temp;
	}

	// Creates a list of possible locations occupied by the surface of a atom
	public void fpAdd() {
		int move = (int) (Math.round(MoleculeConvertor.RADIUS / dx));

		for (int i = -move; i <= move; i++) {
			for (int j = -move; j <= move; j++) {
				for (int k = -move; k <= move; k++) {
					if (i * i + j * j + k * k <= move * move) {
						vec.add(new Vector3d(i, j, k));
					}
				}
			}
		}
	}

	// Reads a VSV file
	public void readVSV(BufferedReader br, double lo2D, double hi2D,
			double xlo, double xhi, double ylo, double yhi, double zlo,
			double zhi, String name) {
		String line = "";
		this.name = name;
		comments = new ArrayList<Comment>();
		data = new ArrayList<DataPt>();
		int nH = 0;
		int surfP = 0, surfA = 0;
		d3 = "";
		d2 = "";

		try {
			while ((line = br.readLine()) != null
					&& !(line.equals("</MOLECULE>") || line.equals("</QUERY>"))) {// Go
																					// untile
																					// end
																					// of
																					// molecule
				if (line.startsWith("<ATOM>")) {// Reads the atoms and their
												// types
					String atom = line.substring(line.indexOf(" ") + 1);
					String[] atoms = atom.split(" ");
					na = atoms.length;
					atm = new Atom[atoms.length];

					for (int i = 0; i < atoms.length; i++) {
						atm[i] = new Atom();
						atm[i].setType(atoms[i]);

						if (atoms[i].equals("H")) {
							nH++;
						} else if (atoms[i].equals("O") || atoms[i].equals("N")) {
							surfP += 24;
						} else {
							surfA += 24;
						}
					}
				} else if (line.startsWith("<BOND>")) {// Reads and creates
														// bonds between two
														// atoms
					String bond = line.substring(line.indexOf(" ") + 1);
					String[] bonds = bond.split(" ");
					nb = bonds.length;
					bnd = new Bond[bonds.length];

					for (int i = 0; i < bonds.length; i++) {
						String[] atom = bonds[i].split(":");
						int[] bnds = { Integer.parseInt(atom[0]) - 1,
								Integer.parseInt(atom[1]) - 1,
								Integer.parseInt(atom[2]) };
						bnd[i] = new Bond(bnds[0], bnds[1], bnds[2]);

						atm[bnd[i].getA1()].addPartner(bnd[i].getA2());
						atm[bnd[i].getA2()].addPartner(bnd[i].getA1());

						if (atm[bnd[i].getA1()].getType().equals("H")
								|| atm[bnd[i].getA2()].getType().equals("H"))
							continue;

						if (atm[bnd[i].getA1()].getType().equals("N")
								|| atm[bnd[i].getA1()].getType().equals("O"))
							surfP -= 6;
						else
							surfA -= 6;

						if (atm[bnd[i].getA2()].getType().equals("N")
								|| atm[bnd[i].getA2()].getType().equals("O"))
							surfP -= 6;
						else
							surfA -= 6;
					}

					createAtomTypes();
					create2Dfp(getPath(4));
				} else if (line.startsWith("<2D>")) {// Reads and converts a
														// string to 2D
														// coordinates
					String points = line.substring(4);
					d2 = points;
					int place = 0;
					boolean x = true;

					while (points.length() > 0) {
						if (x)
							for (int i = place; i < atm.length; i++)
								if (atm[i].getType().equals("H"))
									place++;
								else
									break;

						double pos = CharToDouble(lo2D, hi2D, points.charAt(0),
								points.charAt(1));

						if (x)
							atm[place].setX2D(pos);
						else {
							atm[place].setY2D(pos);
							place++;
						}

						x = !x;
						points = points.substring(2);
					}
					d22 = true;
				} else if (line.startsWith("<3D>")) {// Reads and converts a
														// string to 3D
														// coordinates
					String points = line.substring(4);
					d3 = points;
					int place = 0;
					int value = 0;

					while (points.length() > 0) {
						if (value == 0) {
							double pos = CharToDouble(xlo, xhi,
									points.charAt(0), points.charAt(1));
							atm[place].setX(pos);
						} else if (value == 1) {
							double pos = CharToDouble(ylo, yhi,
									points.charAt(0), points.charAt(1));
							atm[place].setY(pos);
						} else {
							double pos = CharToDouble(zlo, zhi,
									points.charAt(0), points.charAt(1));
							atm[place].setZ(pos);
							place++;
						}

						value++;
						value %= 3;
						points = points.substring(2);
					}
					d33 = true;
				} else if (line.startsWith("<COMMENT>")) {// Reads a list of
															// comments
					while (!(line = br.readLine()).equals("</COMMENT>")) {
						Comment c = new Comment(line.toLowerCase());
						comments.add(c);
					}
				} else if (line.startsWith("<DATA>")) {// Reads a list of data
														// points as well as
														// calculate some
					while (!(line = br.readLine()).equals("</DATA>")) {
						DataPt c = new DataPt(Double.parseDouble(line
								.substring(0, line.indexOf(" "))),
								line.substring(line.indexOf(" ") + 1));
						data.add(c);
					}

					if (loc("Total Surface Area") == -1) {
						data.add(new DataPt(
								(24 * (na - nH)) - (12 * (nb - nH)),
								"Total Surface Area"));
						SimpleGui.changed = true;
					}

					if (loc("Polar Surface Area") == -1) {
						data.add(new DataPt(surfP, "Polar Surface Area"));
						SimpleGui.changed = true;
					}

					if (loc("Apolar Surface Area") == -1) {
						data.add(new DataPt(surfA, "Apolar Surface Area"));
						SimpleGui.changed = true;
					}
				}
			}

			polarH = new boolean[na];
			setPolarH();

			createFP(xlo, ylo, zlo, (int) ((xhi - xlo) / dx),
					(int) ((yhi - ylo) / dx), (int) ((zhi - zlo) / dx));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Writes a VSV file
	public void write(FileWriter os, double lo2D, double hi2D, double xlo,
			double xhi, double ylo, double yhi, double zlo, double zhi) {
		try {
			os.write("<MOLECULE>" + name + "\n");
			os.write("<ATOM>");

			for (Atom a : atm) {// Writes the atoms
				os.write(" " + a.getType());
			}
			os.write("\n");

			os.write("<BOND>");

			for (Bond b : bnd) {// Writes the bonds
				os.write(b.writeBond());
			}
			os.write("\n");

			// Writes the strings for the points
			os.write("<2D>" + d2 + "\n");
			os.write("<3D>" + d3 + "\n");

			os.write("<COMMENT>\n");

			for (Comment c : comments) {// Writes the comments
				os.write(c.getComment() + "\n");
			}
			os.write("</COMMENT>\n");

			os.write("<DATA>\n");
			for (DataPt dp : data) {// Writes the data
				os.write("" + dp + "\n");
			}
			os.write("</DATA>\n");
			os.write("</MOLECULE>\n");
		} catch (Exception e) {
		}
	}

	// Changes two chars to a location in a grid
	public double CharToDouble(double xlo, double xhi, char c1, char c2) {
		double x = 0.0;
		int b1 = 0;
		int b2 = 0;
		// String of all the values possible for the location
		final String D2C = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789`~@#$%^&*()_+-=[]{};:,<.>?/|";
		final int D2C_MAX = 90;

		b1 = D2C.indexOf(c1);
		if (b1 == -1) { // make sure c1 is in D2C
			return Double.NEGATIVE_INFINITY;
		}
		b2 = D2C.indexOf(c2);
		if (b2 == -1) { // make sure c2 is in D2C
			return Double.NEGATIVE_INFINITY;
		}

		x = (double) b1 / (double) (D2C_MAX) + (double) (b2)
				/ ((double) (D2C_MAX) * (double) (D2C_MAX));
		x *= (xhi - xlo);
		x += xlo;
		return x;
	}

	// Checks if there is a data point in this molecule
	public double containsLabel(String l) {
		for (DataPt d : data)
			if (d.getLabel().equals(l))
				return d.getValue();

		return Double.NEGATIVE_INFINITY;
	}

	// Adds the headers not in the list
	public void getDataPts(ArrayList<String> dpt) {
		for (DataPt d : data)
			if (!dpt.contains(d.getLabel()))
				dpt.add(d.getLabel());
	}

	// Adds comments not in the list
	public void addComments(ArrayList<String> master) {
		for (Comment d : comments)
			if (!master.contains(d.getComment()))
				master.add(d.getComment());
	}

	// CHecks if the molecule has a specific comment
	public boolean containsComment(String comment) {
		for (Comment c : comments) {
			if (comment.equals(c.getComment())) {
				return true;
			}
		}

		return false;
	}

	// Outputs all the data in the molecule
	public String getData() {
		String data = "Num Atoms: " + na + "\n";

		for (DataPt d : this.data) {
			data += d.getLabel() + ": " + d.getValue() + "\n";
		}

		return data;
	}

	// Writes a SDF file
	public static void WriteSDFFile(ArrayList<Molecule> mols, File file,
			boolean d3) {
		String buf;
		int i;
		int j;
		String field;
		int ns;
		int year;
		int month;
		int day;
		int hour;
		int min;
		int sec;
		String progName = "Viewer";

		try {
			FileWriter output = new FileWriter(file);
			for (Molecule m : mols) {// Writes a molecule
				// A bunch of stuff happens that I can't describe
				output.write(m.name + "\n");
				buf = progName;
				if (progName.length() > 8) {
					buf = progName.substring(0, 8);
				}
				ns = 8 - buf.length();
				for (i = 0; i < ns; i++) {
					buf += " ";
				}
				Calendar c = Calendar.getInstance();
				year = c.get(Calendar.YEAR);
				day = c.get(Calendar.DAY_OF_MONTH);
				month = c.get(Calendar.MONTH);
				hour = c.get(Calendar.HOUR_OF_DAY);
				min = c.get(Calendar.MINUTE);
				sec = c.get(Calendar.SECOND);

				if (month < 10) {
					buf += "0";
				}
				buf += month;
				if (day < 10) {
					buf += "0";
				}
				buf += day;
				year = (year % 100);
				if (year < 1) {
					buf += "0";
				}
				if (year < 10) {
					buf += "0";
				}
				buf += year;

				if (hour < 1) {
					buf += "0";
				}
				if (hour < 10) {
					buf += "0";
				}
				buf += hour;
				if (min < 1) {
					buf += "0";
				}
				if (min < 10) {
					buf += "0";
				}
				buf += min;
				if (sec < 1) {
					buf += "0";
				}
				if (sec < 10) {
					buf += "0";
				}
				buf += sec;

				if (d3)
					buf += "3D";
				else
					buf += "2D";

				output.write(buf + "\n");
				output.write("\n");
				buf = "";
				if (m.na < 10) {
					buf += " ";
				}
				if (m.na < 100) {
					buf += " ";
				}
				buf += m.na;
				if (m.nb < 10) {
					buf += " ";
				}
				if (m.nb < 100) {
					buf += " ";
				}
				buf += m.nb;
				output.write(buf + "  0  0  1  0  0  0  0  0999 V2000\n");
				for (i = 0; i < m.na; i++) {
					buf = m.atm[i].getMolLine(d3);
					output.write(buf + "\n");
				}
				for (i = 0; i < m.nb; i++) {
					buf = "";
					field = "";
					field += (m.bnd[i].getA1() + 1);
					ns = 3 - field.length();
					for (j = 0; j < ns; j++) {
						buf += " ";
					}
					buf += field;

					field = "";
					field += (m.bnd[i].getA2() + 1);
					ns = 3 - field.length();
					for (j = 0; j < ns; j++) {
						buf += " ";
					}
					buf += field;

					field = "";
					field += m.bnd[i].getType();
					ns = 3 - field.length();
					for (j = 0; j < ns; j++) {
						buf += " ";
					}
					buf += field;

					field = "";
					field += "0";
					ns = 3 - field.length();
					for (j = 0; j < ns; j++) {
						buf += " ";
					}
					buf += field;

					buf += "  0  0";
					output.write(buf + "\n");
				}
				output.write("M  END\n$$$$\n");// Ends a molecule
			}

			output.close();
		} catch (Exception ex) {
			return;
		}
	}

	// Returns the location of a data point
	public int loc(String s) {
		for (int i = 0; i < data.size(); i++)
			if (data.get(i).getLabel().equals(s))
				return i;

		return -1;
	}

	// Reads a CSV file
	public void readCSV(String[] title, String[] points) {
		name = points[0];
		for (int i = 1; i < title.length; i++) {
			if (true) {
				if (word(points[i]))
					continue;

				int p = loc(title[i]);

				// Adds a value to a molecule
				if (p == -1)
					data.add(new DataPt(Double.parseDouble(points[i]), title[i]));
				else {
					data.get(p).setPoint(Double.parseDouble(points[i]));
				}
			}
		}
	}

	// Checks if a string is a number or a word
	public boolean word(String w) {
		if (w.length() == 0)
			return true;

		for (int i = 0; i < w.length(); i++) {
			if (Character.isLetter(w.charAt(i)))
				return true;
		}
		return false;
	}

	// Compares the 3D similarity between two molecules
	public static double compare(Molecule m1, Molecule m2) {
		int numEqu = 0;
		int i1 = 0, i2 = 0;
		int k1, k2;
		ArrayList<Integer> mol1 = m1.fp3d;
		ArrayList<Integer> mol2 = m2.fp3d;

		while (i1 < mol1.size() && i2 < mol2.size()) {
			k1 = mol1.get(i1);
			k2 = mol2.get(i2);
			if (k1 == k2) {
				i1++;
				i2++;
				numEqu++;
			} else if (k1 > k2) {
				i2++;
			} else {
				i1++;
			}
		}

		double p = (double) (numEqu)
				/ (double) (mol1.size() + mol2.size() - numEqu);
		return p;
	}

	// Used to sort molecules based on 2D similarity, 3D similarity or a data
	// point
	public int compareTo(Molecule m) {
		if (sortBy.isEmpty())
			return 0;
		else if (sortBy.equals("2D"))
			return Double.compare(m.percent2D, percent2D);
		else if (sortBy.equals("3D"))
			return Double.compare(m.percent, percent);
		else
			return Double.compare(m.containsLabel(sortBy),
					containsLabel(sortBy));
	}

	// Adds the 3D similarity to the list of data
	public void addSim(String mol, double per) {
		if (loc(mol + " - Sim3D") >= 0)
			return;

		data.add(new DataPt(per, mol + " - Sim3D"));
	}

	// Adds the 2D similarity to the list of data
	public void addSim2D(String mol, double per) {
		if (loc(mol + " - Sim2D") >= 0)
			return;

		data.add(new DataPt(per, mol + " - Sim2D"));
	}

	// Sorts the molecule based on the data point's location relative to a value
	// selected in the Split screen
	public void newName(String name, int value, String className) {
		double d = Math.min(containsLabel(name), value) == value ? 1.0 : 0.0;
		data.add(new DataPt(d, className));
	}

	// Writes a VSV files query molecule
	public void writeQuerry(FileWriter os, double lo2d, double hi2d,
			double xlo, double xhi, double ylo, double yhi, double zlo,
			double zhi) {
		try {
			os.write("<QUERY>" + name + "\n");
			os.write("<ATOM>");

			for (Atom a : atm) {
				os.write(" " + a.getType());
			}
			os.write("\n");

			os.write("<BOND>");

			for (Bond b : bnd) {
				os.write(b.writeBond());
			}
			os.write("\n");

			os.write("<2D>" + d2 + "\n");
			os.write("<3D>" + d3 + "\n");

			os.write("<COMMENT>\n");

			for (Comment c : comments) {
				os.write(c.getComment() + "\n");
			}
			os.write("</COMMENT>\n");

			os.write("<DATA>\n");
			for (DataPt dp : data) {
				os.write("" + dp + "\n");
			}
			os.write("</DATA>\n");
			os.write("</QUERY>\n");
		} catch (Exception e) {
		}
	}
}