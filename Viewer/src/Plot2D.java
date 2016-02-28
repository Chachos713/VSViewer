/**
 * Copyright (C) 2014 Kyle and David Diller
 * 
 * This file is part of the VS Viewer 3D.
 * The contents are covered by the terms of the BSD license which
 * is included in the file license.txt, found at the root of the VS Viewer 3D
 * source tree.
 */

import javax.swing.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.*;
import java.io.*;

@SuppressWarnings("serial")
public class Plot2D extends JPanel {
	// Gets whether some values should be the logarithm of the original
	SimpleGui sg;

	// Values to translate/zoom in the plot
	double zoomX = 1, zoomY = 1;
	double xoff = 0, yoff = 0;

	// Values to draw a line of correlation
	boolean drawLine;
	double correlationEdge = .9;

	// Values to determine if to draw or not
	boolean ready = false;
	DataSet data;
	boolean popup_state;

	// Values to drawing the boundaries
	int xlo;
	int xhi;
	int ylo;
	int yhi;
	int border;

	// Values to keep track of which descriptors to use
	int xaxis;
	int yaxis;
	int caxis;
	int saxis;

	// Values for for sizing the points
	double diameter = 15.0;
	double dhi = 15;
	double dlo = 5;

	// Values for the boundaries to draw the points in
	double Xlo;
	double Ylo; // because the window begins in the upper right corner Ylo >=
				// Yhi
	double Xhi;
	double Yhi;

	// List of values and which Molecule they correlate to
	ArrayList<Double> X, Y, S, C;
	ArrayList<Integer> index;

	// Stores which point is clicked
	int molshow = -1;

	// List of points of the selection curve
	ArrayList<Point> select_curve = new ArrayList<Point>();

	// Creates an object with the SimpleGui Object
	public Plot2D(SimpleGui s) {
		sg = s;
	}

	// Reads a file
	public boolean openFile(String vsv, int width, int height, int xa, int ya,
			int ca, int b, JFrame f) {
		open(width, height, b);// Sets Values for drawing

		// Sets the initial values for the axis and selected Molecule
		xaxis = xa;
		yaxis = ya;
		molshow = -1;

		// Sets ready to false and creates a DataSet to read the file
		ready = false;
		data = new DataSet();

		// Gets the extension
		char c = vsv.substring(vsv.indexOf(".")).charAt(1);

		switch (c) {
		case 'v':// VSV
			if (!data.openVSV(vsv))
				return false;// Return if file is unreadable
			break;
		case 'c':// CSV
			data.readCSV(new File(vsv));
			break;
		case 's':// SDF
			if (!data.LoadSDF2D(vsv, true))
				return false;// Return if file is unreadable
			break;
		default:// None of the types
			System.err.println("WRONG FILE TYPE");
		}

		// Sets the values for drawing
		caxis = data.header.length;
		yaxis %= data.header.length;
		xaxis %= data.header.length;
		saxis = caxis;

		// Sets values to allow drawing
		ready = true;
		popup_state = true;
		return true;
	}

	// Sets initial values to drawing the plot
	public void open(int width, int height, int b) {
		border = 0;
		xlo = border;
		ylo = border;
		xhi = border + width;
		yhi = border + height;
		border = b;
	}

	// Draws the scatter plot
	public void paintComponent(Graphics g) {
		// Values to setup and clear the screen to get ready to draw
		double Ulo;
		double Uhi;

		double Vlo;
		double Vhi;

		double Slo;
		double Shi;

		g.setColor(Color.white);
		g.fillRect(0, 0, 20000, 20000);
		X = new ArrayList<Double>();
		Y = new ArrayList<Double>();
		C = new ArrayList<Double>();
		S = new ArrayList<Double>();
		index = new ArrayList<Integer>();

		// If a file is read
		if (!ready) {
			return;
		}

		int i;
		int j;

		// Values for drawing the axes
		Xlo = xlo + border;
		Ylo = yhi - border;
		Xhi = xhi - border;
		Yhi = ylo + border;

		// Draws the axes
		g.setColor(Color.black);
		g.drawLine((int) Xlo, (int) Ylo, (int) Xhi, (int) Ylo);
		g.drawLine((int) Xlo, (int) Ylo, (int) Xlo, (int) Yhi);

		// Sets the descriptor name of the axes, color by, and size by
		String x = data.header[xaxis];
		String y = data.header[yaxis];
		String c = "None";
		if (caxis != data.header.length)
			c = data.header[caxis];

		String z = "None";
		if (saxis != data.header.length)
			z = data.header[saxis];

		// Goes through the data and determines whether the molecule should be
		// drawn or not
		for (i = 0; i < data.nData; i++) {
			Molecule m = data.mol[i];

			// Check there is a x and y value and that the value can be a log
			if (m.containsLabel(x) != Double.NEGATIVE_INFINITY
					&& m.containsLabel(y) != Double.NEGATIVE_INFINITY
					&& log(m, x, y, c, z)) {
				index.add(i);
				X.add(m.containsLabel(x));
				if (sg.checks.get(xaxis).getState()) {
					X.set(X.size() - 1, Math.log10(X.get(X.size() - 1)));
				}
				Y.add(m.containsLabel(y));
				if (sg.checks.get(yaxis).getState()) {
					Y.set(Y.size() - 1, Math.log10(Y.get(Y.size() - 1)));
				}
				C.add(m.containsLabel(c));
				if (!c.equals("None") && sg.checks.get(caxis).getState()) {
					C.set(C.size() - 1, Math.log10(C.get(C.size() - 1)));
				}
				S.add(m.containsLabel(z));
				if (!z.equals("None") && sg.checks.get(saxis).getState()) {
					S.set(S.size() - 1, Math.log10(S.get(S.size() - 1)));
				}
			}
		}

		double corr = Calculator.correlation(X, Y);// Calculates the correlation
													// coefficient

		if (X.size() == 0)
			return;// Make sure there are molecules being drawn

		// Set the initial values for the low and high of each axes, color by,
		// and size by
		Ulo = X.get(0);
		Uhi = Ulo;
		Vlo = Y.get(0);
		Vhi = Vlo;
		Slo = C.get(0);
		Shi = Slo;

		double zlo = S.get(0);
		double zhi = zlo;

		// Goes through the data and gets the low and high of each
		for (i = 0; i < X.size(); i++) {
			if (X.get(i) > Uhi) {
				Uhi = X.get(i);
			}
			if (X.get(i) < Ulo) {
				Ulo = X.get(i);
			}
			if (Y.get(i) > Vhi) {
				Vhi = Y.get(i);
			}
			if (Y.get(i) < Vlo) {
				Vlo = Y.get(i);
			}
			if (C.get(i) > Shi) {
				Shi = C.get(i);
			}
			if ((C.get(i) < Slo && C.get(i) != Double.NEGATIVE_INFINITY)
					|| Slo == Double.NEGATIVE_INFINITY) {
				Slo = C.get(i);
			}

			if (S.get(i) > zhi) {
				zhi = S.get(i);
			}
			if ((S.get(i) < zlo && S.get(i) != Double.NEGATIVE_INFINITY)
					|| zlo == Double.NEGATIVE_INFINITY) {
				zlo = S.get(i);
			}
		}

		// Used to set the initial bounds of values to draw within
		double s;
		s = diameter * Math.abs((Uhi - Ulo) / (Xhi - Xlo)) / 2.0 + (Uhi - Ulo)
				/ 20;
		Ulo -= s;
		Uhi += s;
		s = diameter * Math.abs((Vhi - Vlo) / (Yhi - Ylo)) / 2.0 + (Vhi - Vlo)
				/ 20;
		Vlo -= s;
		Vhi += s;

		// Draws the axes names
		Font f = new Font("SansSerif", Font.BOLD, 18);
		g.setFont(f);
		g.setColor(Color.black);
		drawVerticalCenteredString(y, xlo + border / 2, (ylo + yhi) / 2, g);
		drawCenteredString(x, (xlo + xhi) / 2, yhi - border / 2, g);

		// Draws the curve
		g.setColor(Color.black);
		if (select_curve.size() > 3) {
			for (j = 1; j < select_curve.size(); j++) {
				g.drawLine(select_curve.get(j - 1).x,
						select_curve.get(j - 1).y, select_curve.get(j).x,
						select_curve.get(j).y);
			}
		}

		// Calculates the values based on the zoom
		for (i = 0; i < X.size(); i++) {
			X.set(i, ((X.get(i) - Ulo) / (Uhi - Ulo) - 0.5) * zoomX + 0.5);
			Y.set(i, ((Y.get(i) - Vlo) / (Vhi - Vlo) - 0.5) * zoomY + 0.5);
		}

		double St = 0;
		// Draws the data points
		for (i = 0; i < X.size(); i++) {
			double d = diameter;

			// Calculates the color
			Color Cr = Color.red;
			if (!c.equals("None")) {
				if (C.get(i) == Double.NEGATIVE_INFINITY)
					Cr = Color.black;
				else {
					St = (C.get(i) - Slo + .025) / (Shi - Slo + .05); // S is
																		// between
																		// 0 and
																		// 1
					Cr = new Color((float) St, (float) 0.0, (float) (1.0 - St));
				}
			}

			// Calculates the diameter
			if (!z.equals("None")) {
				if (S.get(i) == Double.NEGATIVE_INFINITY)
					d = dlo;
				else {
					double p = (S.get(i) - zlo) / (zhi - zlo) * 100;
					d = p / 100 * (dhi - dlo) + dlo;
				}
			}

			// Sets the color
			g.setColor(Cr);
			if (data.selected[index.get(i)]) {
				g.setColor(Color.green);
			}

			// Sets the screen coordinates
			double x1 = (Xhi - Xlo) * X.get(i) + Xlo - d / 2.0 + xoff;
			double y1 = (Yhi - Ylo) * Y.get(i) + Ylo - d / 2.0 + yoff;
			X.set(i, x1);
			Y.set(i, y1);

			// Draws the data point
			if (x1 >= Xlo && x1 <= Xhi && y1 <= Ylo && y1 >= Yhi)
				g.fillOval(((int) x1), ((int) y1), (int) d, (int) d);
		}

		// Draws the high and low values for the axes
		f = new Font("SansSerif", Font.PLAIN, 12);
		g.setFont(f);
		g.setColor(Color.black);
		double lox, hix, loy, hiy;
		hix = Ulo + (Uhi - Ulo)
				* (((Xhi - Xlo - xoff) / (Xhi - Xlo) - .5) * (1 / zoomX) + .5);
		hix = Calculator.round(hix, 2);
		hiy = Vlo + (Vhi - Vlo)
				* (((Yhi - Ylo - yoff) / (Yhi - Ylo) - .5) * (1 / zoomY) + .5);
		hiy = Calculator.round(hiy, 2);

		lox = Ulo + (Uhi - Ulo)
				* (((-xoff) / (Xhi - Xlo) - .5) * (1 / zoomX) + .5);
		lox = Calculator.round(lox, 2);
		loy = Vlo + (Vhi - Vlo)
				* (((-yoff) / (Yhi - Ylo) - .5) * (1 / zoomY) + .5);
		loy = Calculator.round(loy, 2);

		if (sg.checks.get(xaxis).getState()) {
			hix = Math.pow(10, hix);
			lox = Math.pow(10, lox);
		}

		if (sg.checks.get(yaxis).getState()) {
			hiy = Math.pow(10, hiy);
			loy = Math.pow(10, loy);
		}

		g.drawString("" + hix, (int) Xhi - border, (int) Ylo + 15);
		g.drawString("" + lox, (int) Xlo, (int) Ylo + 15);

		Graphics2D g2 = (Graphics2D) g;
		g2.rotate(-Math.PI / 2.0);

		g.drawString("" + hiy, -((int) Yhi) - 25, border - 10);
		g.drawString("" + loy, -((int) Ylo), border - 10);

		g2.rotate(Math.PI / 2.0);

		// Calculates the values for the line of best fit
		double m = Calculator.slopeLineBest(X, Y);
		double b = Calculator.yint(X, Y, m);

		double miy = m * Xlo + b + Yhi / 2;
		double may = m * Xhi + b + Yhi / 2;
		double max = Xhi;
		double mix = Xlo;

		// Draws the line of best fit
		if (drawLine && Math.abs(corr) >= correlationEdge)
			g.drawLine((int) mix, (int) miy, (int) max, (int) may);
	}

	// Checks if a data point can be drawn based on the value and if it needs to
	// take the log or not
	public boolean log(Molecule m, String x, String y, String c, String z) {
		return !(sg.checks.get(xaxis).getState() && m.containsLabel(x) <= 0)
				&& !(sg.checks.get(yaxis).getState() && m.containsLabel(y) <= 0)
				&& (c.equals("None") || !(sg.checks.get(caxis).getState() && m
						.containsLabel(c) <= 0))
				&& (z.equals("None") || !(sg.checks.get(saxis).getState() && m
						.containsLabel(z) <= 0));
	}

	/**
	 * Uses the idea that a straight line in one direction from a point will
	 * pass through the sides of the object an odd number times if it is inside
	 * and an even number of times if it is outside
	 * 
	 * @param curve
	 * @param invert
	 */
	// Calculates which molecules are selected
	public void WhoIsSelected(ArrayList<Point> curve, boolean invert) {
		int i;
		int j;
		double t;
		double X1;
		double Y1;
		double X2;
		double Y2;

		Arrays.fill(data.selected, invert);

		// Goes through the plot values
		if (curve.size() > 3) {
			for (i = 0; i < X.size(); i++) {
				for (j = 1; j < curve.size(); j++) {
					// Gets the x and y values of the two points of the curve
					Y1 = (double) curve.get(j - 1).y;
					Y2 = (double) curve.get(j).y;
					X1 = (double) curve.get(j - 1).x;
					X2 = (double) curve.get(j).x;

					if ((Y.get(i) < Y1 && Y.get(i) >= Y2)
							|| (Y.get(i) > Y1 && Y.get(i) <= Y2)) {// Checks
																	// that the
																	// data
																	// point is
																	// within
																	// the the
																	// two
																	// points
						t = (Y.get(i) - Y2) / (Y1 - Y2);
						if (t * X1 + (1.0 - t) * X2 <= X.get(i)) {// Checks that
																	// the point
																	// is to one
																	// side of
																	// the line
							data.selected[index.get(i)] = !data.selected[index
									.get(i)];
						}
					}
				}
			}
		}
	}

	// Counts the number of selected molecules
	public int selected() {
		int n = 0;
		for (int i = 0; i < data.nData; i++) {
			if (data.selected[i])
				n++;
		}

		return n;
	}

	// Draws a string centered around a point
	public void drawCenteredString(String s, int u, int v, Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		int x = u - fm.stringWidth(s) / 2;
		int y = v + fm.getAscent() / 2;
		g.drawString(s, x, y);
	}

	// Draws a string centered around a point
	public void drawVerticalCenteredString(String s, int u, int v, Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		Graphics2D g2 = (Graphics2D) g;
		g2.rotate(-Math.PI / 2.0);
		int x = u + fm.getAscent() / 2;
		int y = v + fm.stringWidth(s) / 2;
		g2.drawString(s, -y, x);
		g2.rotate(Math.PI / 2.0);
	}

	// Takes a mouse click and determines what it does
	public boolean MouseClick(int x, int y, MouseEvent e) {
		double X0 = (double) x;
		double Y0 = (double) y;
		int button = e.getButton();

		// Changes the y-axis
		if (X0 >= Xlo - border && X0 <= Xlo
				&& Math.abs(Y0 - (Ylo + Yhi) / 2.0) < 50.0) {
			if (button == 1) { // left button
				yaxis++;
				if (yaxis >= data.nFields) {
					yaxis = 0;
				}
			} else {
				yaxis--;
				yaxis += data.header.length;
				yaxis %= data.header.length;
			}

			resetY();
			return true;
		}
		// Changes the x-axis
		else if (Y0 >= Ylo && Y0 <= Ylo + border
				&& Math.abs(X0 - (Xlo + Xhi) / 2.0) < 50.0) {
			if (button == 1) { // left button
				xaxis++;
				if (xaxis >= data.nFields) {
					xaxis = 0;
				}
			} else {
				xaxis--;
				xaxis += data.header.length;
				xaxis %= data.header.length;
			}

			resetX();
			return true;
		}

		// Calculates based on which molecule (if any) is selected
		boolean found = ShowMol(x, y);
		if (found) {
			popup_state = false;
		}

		repaint();
		return found;
	}

	// Calculates which molecule is being hovered over
	public boolean MouseMove(int x, int y) {
		if (!popup_state) {
			return false;
		}
		return ShowMol(x, y);
	}

	// Calculates the data point within the coordinates
	public boolean ShowMol(int x, int y) {
		double X0 = (double) x;
		double Y0 = (double) y;
		double minD;
		double d;
		int k;
		int i;

		if (X0 >= Xlo && X0 <= Xhi && Y0 >= Yhi && Y0 <= Ylo) {
			minD = diameter * diameter;// Maximum distance it can be away
			k = -1;
			for (i = 0; i < X.size(); i++) {// Goes through the data and
											// calculates which point is the
											// closest
				d = (X.get(i) - X0) * (X.get(i) - X0) + (Y.get(i) - Y0)
						* (Y.get(i) - Y0);
				if (d <= minD) {
					minD = d;
					k = i;
				}
			}
			if (k >= 0) {
				molshow = index.get(k);
				return true;
			}
		}
		return false;
	}

	// Calculates the zoom
	public void increaX() {
		zoomX *= 1.10;
	}

	public void increaY() {
		zoomY *= 1.10;
	}

	public void decreaX() {
		zoomX *= .95;
	}

	public void decreaY() {
		zoomY *= .95;
	}

	// Resets the zoom and translation
	public void resetX() {
		zoomX = 1;
		xoff = 0;
	}

	public void resetY() {
		zoomY = 1;
		yoff = 0;
	}

	// Saves a file
	public void save(File f) {
		data.save(f);
	}

	// Clears the DataSet
	public void close() {
		data = new DataSet();
		ready = false;
	}

	// Sets the color by and size by
	public void setC(int c) {
		caxis = c;
	}

	public void setS(int s) {
		saxis = s;
	}

	// Sets the minimum and maximum diameter of the points
	public void setSize(double m1, double m2) {
		dlo = m1;
		dhi = m2;
	}

	// Values for the line of best fir
	public void setLine(boolean d) {
		drawLine = d;
	}

	public void setCorr(double c) {
		correlationEdge = c;
	}

	// Translates the plot
	public void translate(int x, int y) {
		xoff += x;
		yoff += y;
	}

	// Checks that the descriptor has at least one positive value
	public boolean pos(String s) {
		for (Molecule m : data.mol) {
			if (m.containsLabel(s) > 0)
				return true;
		}

		return false;
	}

	// Sets the color by and the size by to none if it was when the new values
	// where added
	public void set() {
		if (caxis == data.header.length - 1) {
			caxis++;
		}

		if (saxis == data.header.length - 1) {
			saxis++;
		}
	}
}