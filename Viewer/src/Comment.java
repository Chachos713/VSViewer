/**
 * Copyright (C) 2014 Kyle and David Diller
 * 
 * This file is part of the VS Viewer 3D. The contents are covered by the terms
 * of the BSD license which is included in the file license.txt, found at the
 * root of the VS Viewer 3D source tree.
 */

public class Comment {
	// The comment and person to posted that comment
	private String user, message;

	// Creates a basic comment with no user
	public Comment(String m) {
		user = "";
		message = m;
	}

	// REturns the person who posted and the comment
	public String getUser() {
		return user;
	}

	public String getComment() {
		return message;
	}
}
