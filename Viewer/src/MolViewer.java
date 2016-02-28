/**
 * Copyright (C) 2014 Kyle and David Diller
 * 
 * This file is part of the VS Viewer 3D.
 * The contents are covered by the terms of the BSD license which
 * is included in the file license.txt, found at the root of the VS Viewer 3D
 * source tree.
 */

import astex.*;
import java.awt.event.*;

/** Used to add mouse events */
@SuppressWarnings("serial")
public class MolViewer extends MoleculeViewer implements MouseWheelListener {
	// Stores things from the mouse events
	private MouseEvent mousePressedEvent = null;
	private int button;

	// Used to update the AstexViewer
	private AstexViewer av;

	// Creates a basic MolViewer
	public MolViewer(AstexViewer a) {
		super();
		av = a;
		addMouseWheelListener(this);
	}

	// Takes mouse wheel to zoom in and out
	public void mouseWheelMoved(MouseWheelEvent e) {
		int dy = e.getWheelRotation();
		moleculeRenderer.renderer.applyZoom(dy * 0.25);

		dirtyRepaint();
	}

	// Used to do rotation and movement of molecules in the screen
	public void mouseDragged(MouseEvent e) {
		int dy = e.getY() - mousePressedEvent.getY();
		int dx = e.getX() - mousePressedEvent.getX();

		if (button == MouseEvent.BUTTON3)
			moleculeRenderer.translateCenter(dx, dy);
		else
			super.mouseDragged(e);

		mousePressedEvent = e;
		dirtyRepaint();
	}

	// Gives values of mouse event
	public void mousePressed(MouseEvent e) {
		mousePressedEvent = e;
		button = e.getButton();
		super.mousePressed(e);
	}

	// Releases memory of mouse event
	public void mouseReleased(MouseEvent e) {
		mousePressedEvent = null;
		super.mouseReleased(e);
		button = 0;
	}

	// Handles when a distance is added
	// Same algorithm as used by AstexViewer
	public void keyPressed(KeyEvent e) {
		if ((e.getKeyChar() == 'd' || e.getKeyChar() == 'D') && av != null) {
			DynamicArray selectedAtoms = moleculeRenderer.getSelectedAtoms();
			int i = selectedAtoms.size();

			if (i == 2) {
				astex.Atom localAtom1 = (astex.Atom) selectedAtoms.get(0);
				astex.Atom localAtom2 = (astex.Atom) selectedAtoms.get(1);

				Distance d = Distance.createDistanceMonitor(localAtom1,
						localAtom2);
				av.putDistances(d);
				moleculeRenderer.addDistance(d);
			}

			return;
		}
		super.keyPressed(e);
	}
}
