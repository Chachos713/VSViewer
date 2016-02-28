/**
 * Copyright (C) 2014 Kyle and David Diller
 * 
 * This file is part of the VS Viewer 3D.
 * The contents are covered by the terms of the BSD license which
 * is included in the file license.txt, found at the root of the VS Viewer 3D
 * source tree.
 */

import java.awt.event.MouseEvent;

import astex.MoleculeViewer;
import astex.UserInterface;


public class KUserInterface extends UserInterface {
	//Stores the AstexViewer Shown
	private AstexViewer av;

	//Creates the UserInterface
	public KUserInterface(MoleculeViewer arg0, AstexViewer a) {
		super(arg0);
		av = a;
	}

	@Override
	//Calls the AstexViewer's reload to show what molecule are shown
	public void mouseReleased(MouseEvent e){
		super.mouseReleased(e);
		
		av.reload();
	}
}
