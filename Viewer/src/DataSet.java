/**
 * Copyright (C) 2014 Kyle and David Diller
 * 
 * This file is part of the VS Viewer 3D.
 * The contents are covered by the terms of the BSD license which
 * is included in the file license.txt, found at the root of the VS Viewer 3D
 * source tree.
 */

import java.io.*;
import java.util.*;
import javax.swing.*;

class DataSet {
	// List of all the different descriptors
	String[] header;

	// List of which molecules are selected
	boolean[] selected;

	// Number of molecules and descriptors
	int nData;
	int nFields;

	// List of all the different comments
	ArrayList<String> masterList;

	// List of molecules and the query molecule
	Molecule[] mol;
	Molecule query;

	// Values to calculate the coordinates of each atom
	double hi2d, lo2d, xhi, xlo, yhi, ylo, zhi, zlo;

	// List of files to be opened by the 3D viewer
	ArrayList<File> extensionFiles;

	// Reads a VSV file
	public boolean openVSV(final String vsv) {
		try {
			// Gets and sets the number of molecules
			int nmol = CountMolVSV(vsv), place = 0;
			nData = nmol;
			mol = new Molecule[nmol];

			// Creates the an empty list of comments
			masterList = new ArrayList<String>();

			// Prepares to read the file
			String strLine = "";
			InputStream in = new FileInputStream(vsv);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			// List of all the values to calculate the atom coordinates
			String[] extremities = br.readLine().split(" ");
			lo2d = Double.parseDouble(extremities[0]);
			hi2d = Double.parseDouble(extremities[1]);

			if (extremities.length > 2) {// Makes sure that the file is a 3D vsv
											// file
				xlo = Double.parseDouble(extremities[2]);
				xhi = Double.parseDouble(extremities[3]);
				ylo = Double.parseDouble(extremities[4]);
				yhi = Double.parseDouble(extremities[5]);
				zlo = Double.parseDouble(extremities[6]);
				zhi = Double.parseDouble(extremities[7]);
			} else {
				JOptionPane.showMessageDialog(null,
						"This is a 2D file not a 3D");
				br.close();
				return false;
			}

			extensionFiles = new ArrayList<File>();// Creates the list of
													// extension files for the
													// 3D viewer

			// Temporary molecule and the values for the 3D finger print
			Molecule m = new Molecule();
			m.fpAdd();

			long cur = System.currentTimeMillis();// Used to calculate time for
													// reading the file

			while ((strLine = br.readLine()) != null) {// Reads the file
				if (strLine.equalsIgnoreCase("<EXTENSION>")) {
					while (!(strLine = br.readLine()).contains("</EXTENSION>")) {// reads
																					// all
																					// the
																					// extension
																					// files
						extensionFiles.add(new File(strLine));
						System.out.println(strLine);
					}
					// Sets the files to be read
					AstexViewer.files = extensionFiles;
				} else if (strLine.startsWith("<QUERY>")) {// Reads the Query
															// Molecule if there
															// is one
					m.readVSV(br, lo2d, hi2d, xlo, xhi, ylo, yhi, zlo, zhi,
							strLine.substring(7));

					System.out.println(m);

					query = m.copy();
				} else if (strLine.startsWith("<MOLECULE>")) {// Reads all the
																// normal
																// molecules
					m.readVSV(br, lo2d, hi2d, xlo, xhi, ylo, yhi, zlo, zhi,
							strLine.substring(10));
					m.index = place;
					Molecule mo = m.copy();
					mol[place] = mo;
					mol[place].addComments(masterList);
					place++;
					System.out.println(place + " / " + nmol + " <> "
							+ m.getName());// Outputs the name and current
											// location of the file
				}
			}
			System.out.println(System.currentTimeMillis() - cur);// Time to read
																	// the file

			br.close();// Closes the file

			// Gets a list of all the descriptors and sets header and the number
			// of descriptors
			updateHeader();

			selected = new boolean[nmol];// Creates the array for knowing which
											// molecules are selected
		} catch (Exception e) {
			e.printStackTrace();
		}

		return true;
	}

	// Counts the number of molecules in a VSV file
	private int CountMolVSV(String infile) {
		int N;
		String strLine;

		N = 0;
		try {
			// Creates the object needed to read the file
			FileInputStream fstream = new FileInputStream(infile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			while ((strLine = br.readLine()) != null) {// Goes through and
														// counts the number of
														// times that a Molecule
														// is started
				if (strLine.startsWith("<MOLECULE>")) {
					N++;
				}
			}
			br.close();

		} catch (Exception e) {// Catch exception if any
			e.printStackTrace();
			System.err.println("Plot2D::LoadVSV - Error: " + e.getMessage());
			return 0;
		}

		return N;
	}

	// Returns the location of the next molecule in the set
	public int NextMol(int cur) {
		int i;
		int j;
		j = cur;
		for (i = 0; i < nData;) {
			j++;
			if (j >= nData) {
				j -= nData;
			}
			return j;
		}

		return -1;
	}

	// Returns the location of the previous molecule in the set
	public int PreviousMol(int cur) {
		int i;
		int j;
		j = cur;
		for (i = 0; i < nData;) {
			j--;
			if (j < 0) {
				j += nData;
			}
			return j;
		}

		return -1;
	}

	// Saves to a VSV file
	public void save(File f) {
		try {
			// Created to allow program to write the file
			FileWriter os = new FileWriter(f);

			// Written for the program to calculate the coordinates of atoms
			os.write(lo2d + " " + hi2d + " " + xlo + " " + xhi + " " + ylo
					+ " " + yhi + " " + zlo + " " + zhi + "\n");

			// Writes the extension files read by the 3D viewer
			os.write("<EXTENSION>\n");

			for (File ef : extensionFiles) {
				os.write(ef.toString() + "\n");
			}

			// Ends the extension
			os.write("</EXTENSION>\n");

			// Writes the query molecules
			if (query != null)
				query.writeQuerry(os, lo2d, hi2d, xlo, xhi, ylo, yhi, zlo, zhi);

			for (Molecule m : mol) {// Writes all the molecules
				m.write(os, lo2d, hi2d, xlo, xhi, ylo, yhi, zlo, zhi);
			}

			os.close();
		} catch (Exception e) {
		}
	}

	// Reads a CSV file
	public void readCSV(File f) {
		try {
			// Creates all the variables needed to read the file
			String line = "";
			ArrayList<Molecule> ols = new ArrayList<Molecule>();
			int loc = 0;

			FileInputStream fstream = new FileInputStream(f);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			// Gets the descriptors
			header = br.readLine().split(",");

			if (header.length == 0) {// Checks that there are values for the
										// descriptors
				br.close();
				return;
			}

			while ((line = br.readLine()) != null) {// Reads the file
				String[] mole = line.split(",");// Gets the values for the
												// descriptors

				if (mole.length != header.length) {// Checks that there is data
													// for each descriptor
					continue;
				}

				// Creates and set values in the molecule
				Molecule m = new Molecule();
				m.index = loc;
				m.readCSV(header, mole);
				ols.add(m);
				loc++;
			}

			// Adds the molecules to the list
			mol = new Molecule[ols.size()];

			for (int i = 0; i < ols.size(); i++) {
				mol[i] = ols.get(i);
			}

			// Sets the number of molecules and the number of descriptors
			nData = mol.length;
			nFields = header.length;

			// Creates the array for knowing which molecules are selected
			selected = new boolean[nData];

			// Closes the file
			br.close();
		} catch (Exception e) {
		}
	}

	// Counts the number of "molecules" (data points) in the CSV file
	public int countCSV(File f) {
		try {
			int count = 0;

			// Created to allow reading
			FileInputStream fstream = new FileInputStream(f);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			// List of all the descriptors
			String[] t = br.readLine().split(",");
			String line = "";

			while ((line = br.readLine()) != null) {// Counts the number of
													// proper (has all the
													// values) data points
				String[] mole = line.split(",");
				if (mole.length != t.length) {
					continue;
				}
				count++;
			}

			br.close();
			return count;
		} catch (Exception e) {
		}
		return 0;
	}

	// Updates the list of descriptors
	public void updateHeader() {
		ArrayList<String> dpt = new ArrayList<String>();

		for (Molecule m : mol)
			m.getDataPts(dpt);

		header = new String[dpt.size()];
		nFields = header.length;

		for (int i = 0; i < dpt.size(); i++)
			header[i] = dpt.get(i);
	}

	// Checks if there is already a descriptor with a specific name
	public boolean isIn(String s) {
		for (String t : header)
			if (t.equals(s))
				return true;

		return false;
	}

	// Counts then number of molecules in a SDF file
	private int CountMolSDF(String infile) {
		int N;
		String strLine;

		N = 0;
		try {
			// Allows for the program to read the file
			FileInputStream fstream = new FileInputStream(infile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			while ((strLine = br.readLine()) != null) {
				if (strLine.contains("$$$$")) {// Counts the number of times a
												// molecule is finished
					N++;
				}
			}
			br.close();

		} catch (Exception e) {// Catch exception if any
			System.err.println("Plot2D::LodaSDF - Error: " + e.getMessage());// displays
																				// error
			return 0;
		}

		return N;
	}

	// Reads a 2D SDF file
	public boolean LoadSDF2D(String infile, boolean is2D) {
		try {
			// Variables for reading the file
			int i;
			int j;
			int nmol;
			String strLine;
			Molecule mols;
			String d = new String();
			boolean found;
			nmol = CountMolSDF(infile);
			mol = new Molecule[nmol];

			FileInputStream fstream = new FileInputStream(infile);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			// List of all the fields
			ArrayList<String> sdfield = new ArrayList<String>();

			for (i = 0; i < nmol; i++) {// Reads the file
				ArrayList<SDField> mrTemp = new ArrayList<SDField>();
				mols = new Molecule();
				if (!mols.ReadNextSDF(br, is2D)) {// Reads the molecule and if
													// any error, display it
					JOptionPane.showMessageDialog(null, "Can't Read File");
					return false;
				}
				strLine = "";
				while (!(strLine = br.readLine()).contains("$$$$")) {// Goes
																		// till
																		// end
																		// of
																		// the
																		// file
					d = strLine.substring(strLine.indexOf("<") + 1);// Reads the
																	// file for
																	// the data
																	// and
																	// descriptors
					if (d.length() != 0) {
						found = false;
						for (String field : sdfield) {
							if (d.equals(field)) {
								found = true;
							}
						}
						if (!found) {
							sdfield.add(d);
						}
						SDField sdField = new SDField();
						sdField.create(d.substring(0, d.length() - 1));
						strLine = "XXX";
						while (strLine.length() != 0) {
							if ((strLine = br.readLine()) == null) {
								System.out
										.println("Plot2D::LoadSDF - file ended too soon 3 - "
												+ i + " : " + strLine);
								br.close();
								return false;
							}
							strLine.trim();
							if (strLine.length() != 0) {
								sdField.addData(strLine);
							}
						}
						mrTemp.add(sdField);
					}
				}
				for (SDField sd : mrTemp) {// Adds comments and the descriptors
											// to the molecule
					if (sd.getNData() == 1 && sd.getData(0).amINumber())
						mols.addData(sd.getLabel(), sd.getData(0).getNumber());
					else if (sd.getLabel().contains("COMMENT"))
						for (j = 0; j < sd.getNData(); j++)
							mols.addComment(sd.getData(j).getString());
				}

				// Adds the molecules to the list
				mols.index = i;
				mol[i] = mols.copy();
			}
			// Sets the number of molecules
			nData = mol.length;

			// Sets the array for knowing which molecules are selected
			selected = new boolean[nmol];

			// Sets the list of comments
			masterList = new ArrayList<String>();

			for (int place = 0; place < nmol; place++) {
				mol[place].addComments(masterList);
			}

			// Sets the descriptors
			updateHeader();

			br.close();// Closes the file
		} catch (Exception e) {// Catch exception if any
			System.err.println("Plot2D::LodaSDF - Error: " + e.getMessage());// displays
																				// error
			e.printStackTrace();
			return false;
		}

		return true;
	}
}