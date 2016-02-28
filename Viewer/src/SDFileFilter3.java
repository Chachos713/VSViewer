/**
 * Copyright (C) 2014 Kyle and David Diller
 * 
 * This file is part of the VS Viewer 3D.
 * The contents are covered by the terms of the BSD license which
 * is included in the file license.txt, found at the root of the VS Viewer 3D
 * source tree.
 */

import java.io.File;
import javax.swing.filechooser.*;

/**
 * Used to access files
 */
// Same as ImageFileFilter
public class SDFileFilter3 extends FileFilter {
	private final String[] okFileExt = new String[] { ".csv" };

	public boolean accept(File file) {
		if (file.isDirectory()) {
			return true;
		}

		for (String ext : okFileExt) {
			if (file.getName().toLowerCase().endsWith(ext)) {
				return true;
			}
		}

		return false;
	}

	public String getDescription() {
		return "Data(.csv)";
	}
}