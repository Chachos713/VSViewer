/**
 * Copyright (C) 2014 Kyle and David Diller
 * 
 * This file is part of the VS Viewer 3D.
 * The contents are covered by the terms of the BSD license which
 * is included in the file license.txt, found at the root of the VS Viewer 3D
 * source tree.
 */

import java.util.*;

public class Calculator {
	// Rounds a decimal to a certain number of places
	public static double round(double num, int n) {
		double magnitude = Math.pow(10, n);
		long shifted = Math.round(num * magnitude);
		return shifted / magnitude;
	}

	// Calculates Standard Deviation of a list of numbers
	public static double standardDeviation(ArrayList<Double> x) {
		double mean = 0;
		double x2 = 0;

		for (double d : x) {
			x2 += d * d;
			mean += d;
		}

		mean /= x.size();
		x2 /= x.size();
		return Math.sqrt(x2 - mean * mean);
	}

	// Calculates average of a list of numbers
	public static double mean(ArrayList<Double> list) {
		return sum(list) / list.size();
	}

	// Returns the minimum/maximum of a set of numbers
	public static double minmax(ArrayList<Double> values, boolean min) {
		double val = values.get(0);

		for (Double d : values) {
			if (min) {
				val = Math.min(d, val);
			} else {
				val = Math.max(d, val);
			}
		}

		return val;
	}

	// Returns all the numbers in a list added together
	public static double sum(ArrayList<Double> list) {
		double tot = 0.0;
		for (double x : list) {
			if (x != Double.NEGATIVE_INFINITY)
				tot += x;
		}

		return tot;
	}

	// Calculates the correlation coefficient of a data set
	public static double correlation(ArrayList<Double> xList,
			ArrayList<Double> yList) {
		double stdX = standardDeviation(xList);
		double stdY = standardDeviation(yList);

		double meanx = mean(xList);
		double meany = mean(yList);

		double xy = 0;

		for (int i = 0; i < xList.size(); i++) {
			xy += (xList.get(i) * yList.get(i));
		}
		xy /= xList.size();

		return (xy - meanx * meany) / (stdX * stdY);
	}

	// Calculates the slope of the line of best fit
	public static double slopeLineBest(ArrayList<Double> xList,
			ArrayList<Double> yList) {
		if (yList.size() != xList.size())
			return Double.NEGATIVE_INFINITY;

		double stdX = standardDeviation(xList);

		double meanx = mean(xList);
		double meany = mean(yList);

		double xy = 0;

		for (int i = 0; i < xList.size(); i++) {
			xy += (xList.get(i) * yList.get(i));
		}
		xy /= xList.size();

		return (xy - meanx * meany) / (stdX * stdX);
	}

	// Gives the y-intercept of a line
	public static double yint(ArrayList<Double> xList, ArrayList<Double> yList,
			double slope) {
		double meanx = mean(xList);
		double meany = mean(yList);
		return meany - (slope * meanx);
	}
}
