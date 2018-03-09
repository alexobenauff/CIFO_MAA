package project;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.awt.image.PixelGrabber;
import java.util.Random;
import java.util.Arrays;

public class Solution {

	public static final int VALUES_PER_TRIANGLE = 10;
	protected static Problem instance;
	protected int[] values;
	protected double fitness;

	protected Random r;

	public Solution(Problem instance) {
		this.instance = instance;
		r = new Random();
		switch (Main.Initialization) {
		case "initialize":
			initialize();
			break;
		case "initializeFillingBorders":
			initializeFillingBorders();
			break;
		case "initializeFillingBordersOld":
			initializeFillingBordersOld();
			break;
		}
	}

	public void initialize() {
		values = new int[instance.getNumberOfTriangles() * VALUES_PER_TRIANGLE];

		for (int triangleIndex = 0; triangleIndex < instance.getNumberOfTriangles(); triangleIndex++) {
			// initialize HSB and Alpha
			for (int i = 0; i < 4; i++) {
				values[triangleIndex * VALUES_PER_TRIANGLE + i] = r.nextInt(256);
			}
			// initialize vertices
			for (int i = 4; i <= 8; i += 2) {
				values[triangleIndex * VALUES_PER_TRIANGLE + i] = r.nextInt(instance.getImageWidth() + 1);
				values[triangleIndex * VALUES_PER_TRIANGLE + i + 1] = r.nextInt(instance.getImageHeight() + 1);
			}
		}
	}

	public void evaluate() {
		BufferedImage generatedImage = createImage();
		int[] generatedPixels = new int[instance.getImageWidth() * instance.getImageHeight()];
		PixelGrabber pg = new PixelGrabber(generatedImage, 0, 0, instance.getImageWidth(), instance.getImageHeight(),
				generatedPixels, 0, instance.getImageWidth());
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		int[] targetPixels = instance.getTargetPixels();
		long sum = 0;
		for (int i = 0; i < targetPixels.length; i++) {
			int c1 = targetPixels[i];
			int c2 = generatedPixels[i];
			int red = ((c1 >> 16) & 0xff) - ((c2 >> 16) & 0xff);
			int green = ((c1 >> 8) & 0xff) - ((c2 >> 8) & 0xff);
			int blue = (c1 & 0xff) - (c2 & 0xff);
			sum += red * red + green * green + blue * blue;
		}
		fitness = Math.sqrt(sum);
	}

	public double evaluateOffspring() {
		BufferedImage generatedImage = createImage();
		int[] generatedPixels = new int[instance.getImageWidth() * instance.getImageHeight()];
		PixelGrabber pg = new PixelGrabber(generatedImage, 0, 0, instance.getImageWidth(), instance.getImageHeight(),
				generatedPixels, 0, instance.getImageWidth());
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		int[] targetPixels = instance.getTargetPixels();
		long sum = 0;
		for (int i = 0; i < targetPixels.length; i++) {
			int c1 = targetPixels[i];
			int c2 = generatedPixels[i];
			int red = ((c1 >> 16) & 0xff) - ((c2 >> 16) & 0xff);
			int green = ((c1 >> 8) & 0xff) - ((c2 >> 8) & 0xff);
			int blue = (c1 & 0xff) - (c2 & 0xff);
			sum += red * red + green * green + blue * blue;
		}
		fitness = Math.sqrt(sum);
		return fitness;
	}

	public static double[] evaluate2(Solution individual) {
		double[] fitnessoftriangles = new double[Main.NUMBER_OF_TRIANGLES];
		double[] fitnessperpointpertriangle = new double[Main.NUMBER_OF_TRIANGLES];

		BufferedImage generatedImage = createImage2(individual);
		int[] generatedPixels = new int[instance.getImageWidth() * instance.getImageHeight()];
		int[] checkPixels = new int[instance.getImageWidth() * instance.getImageHeight()];

		PixelGrabber pg = new PixelGrabber(generatedImage, 0, 0, instance.getImageWidth(), instance.getImageHeight(),
				generatedPixels, 0, instance.getImageWidth());
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		int[] targetPixels = instance.getTargetPixels();

		long sum = 0;
		for (int i = 0; i < targetPixels.length; i++) {
			int c1 = targetPixels[i];
			int c2 = generatedPixels[i];
			int red = ((c1 >> 16) & 0xff) - ((c2 >> 16) & 0xff);
			int green = ((c1 >> 8) & 0xff) - ((c2 >> 8) & 0xff);
			int blue = (c1 & 0xff) - (c2 & 0xff);
			sum += red * red + green * green + blue * blue;
		}

		// fitness = Math.sqrt(sum);

		int[] triangle = new int[6];
		// calculate the fitness per triangle as well
		for (int p = 0; p < Main.NUMBER_OF_TRIANGLES; p++) {
			// load the coordinates
			for (int j = 0; j < 6; j++) {
				triangle[j] = individual.getValue(p * VALUES_PER_TRIANGLE + j + 4);
			}
			int[] TP = { 0, 0 };
			int sizeoftriangle = 0;

			int[] indices = { loctoindex(triangle[0], triangle[1]), loctoindex(triangle[2], triangle[3]),
					loctoindex(triangle[4], triangle[5]) };
			int min = Arrays.stream(indices).min().getAsInt();
			int max = Arrays.stream(indices).max().getAsInt();

			for (int i = min; i < max + 1; i++) {
				TP = indextoloc(i);
				if (isintriangle(triangle, TP) == true) {
					checkPixels[i] = 1;
					sizeoftriangle++;
				} else {
					checkPixels[i] = 0;
				}
			}

			long total = 0;
			for (int i = 0; i < targetPixels.length; i++) {
				if (checkPixels[i] == 1) {
					int c1 = targetPixels[i];
					int c2 = generatedPixels[i];
					int red = ((c1 >> 16) & 0xff) - ((c2 >> 16) & 0xff);
					int green = ((c1 >> 8) & 0xff) - ((c2 >> 8) & 0xff);
					int blue = (c1 & 0xff) - (c2 & 0xff);
					total += red * red + green * green + blue * blue;
				}

			}
			// penalty if it is not a proper triangle
			if (total == 0) {
				total = 1000000000;
				sizeoftriangle = 1;
			}

			fitnessoftriangles[p] = Math.sqrt(total);
			fitnessperpointpertriangle[p] = Math.sqrt(total) / sizeoftriangle;

		}
		// return fitnesses of triangles
		return fitnessoftriangles;

		// return the fitness per index of the triangles
		// return fitnessperpointpertriangle;
	}

	public Solution applyMutation() {
		Solution temp = this.copy();
		int triangleIndex = r.nextInt(instance.getNumberOfTriangles());
		int valueIndex = r.nextInt(VALUES_PER_TRIANGLE);
		if (valueIndex < 4) {
			temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r.nextInt(256);
		} else {
			if (valueIndex % 2 == 0) {
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r.nextInt(instance.getImageWidth() + 1);
			} else {
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r
						.nextInt(instance.getImageHeight() + 1);
			}
		}
		return temp;
	}

	public Solution applyMutationVertex() {
		Solution temp = this.copy();
		int triangleIndex = r.nextInt(instance.getNumberOfTriangles());
		int valueIndex = r.nextInt(VALUES_PER_TRIANGLE);
		while (valueIndex < 4) {
			valueIndex = r.nextInt(VALUES_PER_TRIANGLE);
		}
		if (valueIndex % 2 == 0) {
			temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r.nextInt(instance.getImageWidth() + 1);
		} else {
			temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r.nextInt(instance.getImageHeight() + 1);
		}
		return temp;
	}

	//swapMutation
	public Solution applySwapMutation() {
		Solution temp = this.copy();
		int triangleIndex = r.nextInt(instance.getNumberOfTriangles());
		// get 2 random integers between 0 and the size of the Triangle
		int p1 = r.nextInt(Solution.VALUES_PER_TRIANGLE);
		int p2 = r.nextInt(Solution.VALUES_PER_TRIANGLE);
		// change one of the points if they are the same
		while (p1 < 4 && p2 < 4 || p1 > 3 && p2 > 3) {
			p1 = r.nextInt(Solution.VALUES_PER_TRIANGLE);
			p2 = r.nextInt(Solution.VALUES_PER_TRIANGLE);
		}
		// swap of the array element at those indices
		int p1value = temp.getValue(triangleIndex * VALUES_PER_TRIANGLE + p1);
		temp.setValue(triangleIndex * VALUES_PER_TRIANGLE + p1,
				temp.getValue(triangleIndex * VALUES_PER_TRIANGLE + p2));
		temp.setValue(triangleIndex * VALUES_PER_TRIANGLE + p2, p1value);

		return temp;
	}

	//inversion mutation
	public Solution applyInversionMutation() {
		Solution temp = this.copy();
		int triangleIndex = r.nextInt(instance.getNumberOfTriangles());
		// repeat the mutation 5 times
		for (int k = 0; k < 10; k++) {
			int p1 = r.nextInt(Solution.VALUES_PER_TRIANGLE);
			int p2 = r.nextInt(Solution.VALUES_PER_TRIANGLE);
			while (p1 >= p2 && p1 < 4 && p2 < 4 || p1 >= p2 && p1 > 3 && p2 > 3) {
				p1 = r.nextInt(Solution.VALUES_PER_TRIANGLE);
				p2 = r.nextInt(Solution.VALUES_PER_TRIANGLE);
			}
			int midPoint = p1 + ((p2 + 1) - p1) / 2;
			int endCount = p2;
			for (int i = p1; i < midPoint; i++) {
				int tmp = temp.getValue(triangleIndex * VALUES_PER_TRIANGLE + i);
				temp.setValue(triangleIndex * VALUES_PER_TRIANGLE + i,
						temp.getValue(triangleIndex * VALUES_PER_TRIANGLE + endCount));
				temp.setValue(triangleIndex * VALUES_PER_TRIANGLE + endCount, tmp);
				endCount--;
			}

		}
		return temp;
	}

	public Solution applyRandomTriangleLineColor() {
		Solution temp = this.copy();

		int triangleIndex = r.nextInt(instance.getNumberOfTriangles());
		int[] A = new int[2];
		int[] B = new int[2];
		int[] C = new int[2];

		// vertices
		A[0] = temp.values[triangleIndex * VALUES_PER_TRIANGLE + 4];
		A[1] = temp.values[triangleIndex * VALUES_PER_TRIANGLE + 5];

		B[0] = temp.values[triangleIndex * VALUES_PER_TRIANGLE + 6];
		B[1] = temp.values[triangleIndex * VALUES_PER_TRIANGLE + 7];

		C[0] = temp.values[triangleIndex * VALUES_PER_TRIANGLE + 8];
		C[1] = temp.values[triangleIndex * VALUES_PER_TRIANGLE + 9];

		int[] AB = Coordinates(A, B);
		int[] AC = Coordinates(A, C);
		int[] BC = Coordinates(B, C);

		int[] newArray = new int[AB.length + AC.length + BC.length];
		System.arraycopy(AB, 0, newArray, 0, AB.length);
		System.arraycopy(AC, 0, newArray, AB.length, AC.length);
		System.arraycopy(BC, 0, newArray, AB.length + AC.length, BC.length);

		int[] HSB = GetColorOfPixel(newArray[r.nextInt(newArray.length)]);

		// initialize HSB and Alpha
		for (int i = 0; i < 4; i++) {
			// values[triangleIndex * VALUES_PER_TRIANGLE + i] = r.nextInt(256);
			temp.values[triangleIndex * VALUES_PER_TRIANGLE + 0] = HSB[0];
			temp.values[triangleIndex * VALUES_PER_TRIANGLE + 1] = HSB[1];
			temp.values[triangleIndex * VALUES_PER_TRIANGLE + 2] = HSB[2];
			temp.values[triangleIndex * VALUES_PER_TRIANGLE + 3] = r.nextInt(256);

		}

		return temp;
	}

	public Solution applyMutationTriangleColorTargetPicture() {
		Solution temp = this.copy(); // copy current solution as temp
		int triangleIndex = r.nextInt(instance.getNumberOfTriangles()); // (n excluded) get random number between 0 and
																		// 99 number of triangles
		int valueIndex = r.nextInt(VALUES_PER_TRIANGLE); // get valueindex between 0 and 9
		int[] Triangle = new int[6];

		if (valueIndex < 3) { // to check which array of each triangle it is color and coordinate parts

			for (int i = 4; i <= 8; i += 2) {

				Triangle[i - 4] = temp.values[triangleIndex * VALUES_PER_TRIANGLE + i];
				Triangle[i - 3] = temp.values[triangleIndex * VALUES_PER_TRIANGLE + i + 1];
			}

			int[] TrianglePixels = getImageIndices(Triangle);

			int[] HSB = GetColorOfPixel(r.nextInt(TrianglePixels.length));
			temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = HSB[valueIndex];

		} else {
			if (valueIndex == 3) {
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r.nextInt(256);
			} else {
				if (valueIndex % 2 == 0) { // remainder to check if its Y of vertex, Y at valueIndex 5,7,9
					temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r
							.nextInt(instance.getImageWidth() + 1); // Y value can only be between 0 and ImageWidth
				} else { // else valueIndex is X of vertex
					temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r
							.nextInt(instance.getImageHeight() + 1); // X value can only be between 0 and ImageWidth
				}
			}
		}
		return temp;
	}

	public void draw() {
		BufferedImage generatedImage = createImage();
		Graphics g = Problem.view.getFittestDrawingView().getMainPanel().getGraphics();
		g.drawImage(generatedImage, 0, 0, Problem.view.getFittestDrawingView());
	}

	public void print() {
		System.out.printf("Fitness: %.1f\n", fitness);
	}

	public int getValue(int index) {
		return values[index];
	}

	public void setValue(int index, int value) {
		values[index] = value;
	}

	public int getHue(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 0];
	}

	public int getSaturation(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 1];
	}

	public int getBrightness(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 2];
	}

	public int getAlpha(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 3];
	}

	public int getXFromVertex1(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 4];
	}

	public int getYFromVertex1(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 5];
	}

	public int getXFromVertex2(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 6];
	}

	public int getYFromVertex2(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 7];
	}

	public int getXFromVertex3(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 8];
	}

	public int getYFromVertex3(int triangleIndex) {
		return values[triangleIndex * VALUES_PER_TRIANGLE + 9];
	}

	public void setHue(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 0] = value;
	}

	public void setSaturation(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 1] = value;
	}

	public void setBrightness(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 2] = value;
	}

	public void setAlpha(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 3] = value;
	}

	public void setXFromVertex1(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 4] = value;
	}

	public void setYFromVertex1(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 5] = value;
	}

	public void setXFromVertex2(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 6] = value;
	}

	public void setYFromVertex2(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 7] = value;
	}

	public void setXFromVertex3(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 8] = value;
	}

	public void setYFromVertex3(int triangleIndex, int value) {
		values[triangleIndex * VALUES_PER_TRIANGLE + 9] = value;
	}

	public int[] getVertex1(int triangleIndex) {
		return new int[] { getXFromVertex1(triangleIndex), getYFromVertex1(triangleIndex) };
	}

	public int[] getVertex2(int triangleIndex) {
		return new int[] { getXFromVertex2(triangleIndex), getYFromVertex2(triangleIndex) };
	}

	public int[] getVertex3(int triangleIndex) {
		return new int[] { getXFromVertex3(triangleIndex), getYFromVertex3(triangleIndex) };
	}

	public Problem getInstance() {
		return instance;
	}

	public int[] getValues() {
		return values;
	}

	public double getFitness() {
		return fitness;
	}
	/*
	 * public double[] getFitnessOfTriangles() { return fitnessoftriangles; } public
	 * double[] getFitnessPerIndexOfTriangles() { return fitnessperpointpertriangle;
	 * }
	 */

	public Solution copy() {
		Solution temp = new Solution(instance);
		for (int i = 0; i < values.length; i++) {
			temp.values[i] = values[i];
		}
		temp.fitness = fitness;
		return temp;
	}

	private BufferedImage createImage() {
		BufferedImage target = instance.getTargetImage();
		BufferedImage generatedImage = new BufferedImage(target.getWidth(), target.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics generatedGraphics = generatedImage.getGraphics();

		generatedGraphics.setColor(Color.GRAY);
		generatedGraphics.fillRect(0, 0, instance.getImageWidth(), instance.getImageWidth());
		for (int triangleIndex = 0; triangleIndex < instance.getNumberOfTriangles(); triangleIndex++) {
			generatedGraphics.setColor(expressColor(triangleIndex));
			generatedGraphics.fillPolygon(expressPolygon(triangleIndex));
		}
		return generatedImage;
	}

	private static BufferedImage createImage2(Solution individual) {
		BufferedImage target = instance.getTargetImage();
		BufferedImage generatedImage = new BufferedImage(target.getWidth(), target.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		Graphics generatedGraphics = generatedImage.getGraphics();

		generatedGraphics.setColor(Color.GRAY);
		generatedGraphics.fillRect(0, 0, instance.getImageWidth(), instance.getImageWidth());
		for (int triangleIndex = 0; triangleIndex < instance.getNumberOfTriangles(); triangleIndex++) {
			generatedGraphics.setColor(expressColor2(triangleIndex, individual));
			generatedGraphics.fillPolygon(expressPolygon2(triangleIndex, individual));
		}
		return generatedImage;
	}

	private static Color expressColor2(int triangleIndex, Solution individual) {
		int hue = individual.getHue(triangleIndex);
		int saturation = individual.getSaturation(triangleIndex);
		int brightness = individual.getBrightness(triangleIndex);
		int alpha = individual.getAlpha(triangleIndex);
		Color c = Color.getHSBColor(hue / 255.0f, saturation / 255.0f, brightness / 255.0f);
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
	}

	private static Polygon expressPolygon2(int triangleIndex, Solution individual) {
		int[] xs = new int[] { individual.getXFromVertex1(triangleIndex), individual.getXFromVertex2(triangleIndex),
				individual.getXFromVertex3(triangleIndex) };
		int[] ys = new int[] { individual.getYFromVertex1(triangleIndex), individual.getYFromVertex2(triangleIndex),
				individual.getYFromVertex3(triangleIndex) };
		return new Polygon(xs, ys, 3);
	}

	private Color expressColor(int triangleIndex) {
		int hue = getHue(triangleIndex);
		int saturation = getSaturation(triangleIndex);
		int brightness = getBrightness(triangleIndex);
		int alpha = getAlpha(triangleIndex);
		Color c = Color.getHSBColor(hue / 255.0f, saturation / 255.0f, brightness / 255.0f);
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
	}

	private Polygon expressPolygon(int triangleIndex) {
		int[] xs = new int[] { getXFromVertex1(triangleIndex), getXFromVertex2(triangleIndex),
				getXFromVertex3(triangleIndex) };
		int[] ys = new int[] { getYFromVertex1(triangleIndex), getYFromVertex2(triangleIndex),
				getYFromVertex3(triangleIndex) };
		return new Polygon(xs, ys, 3);
	}

	/////// INITIALIZATION
	///// store all Colors from targetPicture into an array
	public int[] TargetPictureColors() {
		int[] cArray = new int[instance.getImageWidth() * instance.getImageHeight()];
		int[] targetPixels = instance.getTargetPixels();
		for (int p = 0; p < cArray.length; p++) {
			// int[] HSB = GetColorOfMidpoint(p);
			cArray[p] = targetPixels[p];
		}
		return cArray;
	}
	//// New initalization method: fill borders

	public void initializeFillingBorders() {
		int[] ColorArray = TargetPictureColors();
		values = new int[instance.getNumberOfTriangles() * VALUES_PER_TRIANGLE];
		int sumX = 0;
		int sumY = 0;

		int[] triangle = new int[6];

		double[] fitnesses = new double[instance.getNumberOfTriangles()];
		double[] fitnessesperindex = new double[instance.getNumberOfTriangles()];

		int[] xborders = { 0, instance.getImageWidth() / 2, instance.getImageWidth() / 4, 0,
				(instance.getImageWidth() / 4) * 3, 0, instance.getImageWidth() };
		int[] yborders = { 0, instance.getImageHeight() / 2, instance.getImageHeight() / 4, 0,
				(instance.getImageHeight() / 4) * 3, 0, instance.getImageHeight() };

		for (int triangleIndex = 0; triangleIndex < instance.getNumberOfTriangles(); triangleIndex++) {

			sumX = 0;
			sumY = 0;

			// first fill the 20 triangles on the outside
			if (triangleIndex < 20) {
				// for (int i = 4; i <= 8; i += 2) {

				// values[triangleIndex * VALUES_PER_TRIANGLE + i] =
				// xborders[r.nextInt(xborders.length-1)];
				// values[triangleIndex * VALUES_PER_TRIANGLE + i + 1] =
				// yborders[r.nextInt(yborders.length-1)];

				// sumX += values[triangleIndex * VALUES_PER_TRIANGLE + i];
				// sumY += values[triangleIndex * VALUES_PER_TRIANGLE + i + 1];

				// }

				while (((values[triangleIndex * VALUES_PER_TRIANGLE + 4] == values[triangleIndex * VALUES_PER_TRIANGLE
						+ 6])
						&& (values[triangleIndex * VALUES_PER_TRIANGLE
								+ 5] == values[triangleIndex * VALUES_PER_TRIANGLE + 7]))
						|| ((values[triangleIndex * VALUES_PER_TRIANGLE
								+ 6] == values[triangleIndex * VALUES_PER_TRIANGLE + 8])
								&& (values[triangleIndex * VALUES_PER_TRIANGLE
										+ 7] == values[triangleIndex * VALUES_PER_TRIANGLE + 9]))
						|| ((values[triangleIndex * VALUES_PER_TRIANGLE
								+ 4] == values[triangleIndex * VALUES_PER_TRIANGLE + 8])
								&& (values[triangleIndex * VALUES_PER_TRIANGLE
										+ 5] == values[triangleIndex * VALUES_PER_TRIANGLE + 9]))) {
					values[triangleIndex * VALUES_PER_TRIANGLE + 4] = xborders[r.nextInt(xborders.length - 1)];
					values[triangleIndex * VALUES_PER_TRIANGLE + 5] = yborders[r.nextInt(yborders.length - 1)];
					values[triangleIndex * VALUES_PER_TRIANGLE + 6] = xborders[r.nextInt(xborders.length - 1)];
					values[triangleIndex * VALUES_PER_TRIANGLE + 7] = yborders[r.nextInt(yborders.length - 1)];
					values[triangleIndex * VALUES_PER_TRIANGLE + 8] = xborders[r.nextInt(xborders.length - 1)];
					values[triangleIndex * VALUES_PER_TRIANGLE + 9] = yborders[r.nextInt(yborders.length - 1)];

					sumX = values[triangleIndex * VALUES_PER_TRIANGLE + 4]
							+ values[triangleIndex * VALUES_PER_TRIANGLE + 6]
							+ values[triangleIndex * VALUES_PER_TRIANGLE + 8];
					sumY = values[triangleIndex * VALUES_PER_TRIANGLE + 5]
							+ values[triangleIndex * VALUES_PER_TRIANGLE + 7]
							+ values[triangleIndex * VALUES_PER_TRIANGLE + 9];
				}

			} else {
				// initialize vertices
				for (int i = 4; i <= 8; i += 2) {
					int width = instance.getImageWidth();
					values[triangleIndex * VALUES_PER_TRIANGLE + i] = r.nextInt(instance.getImageWidth() + 1);
					values[triangleIndex * VALUES_PER_TRIANGLE + i + 1] = r.nextInt(instance.getImageHeight() + 1);

					sumX += values[triangleIndex * VALUES_PER_TRIANGLE + i];
					sumY += values[triangleIndex * VALUES_PER_TRIANGLE + i + 1];
				}
			}

			int[] HSB = null;
			switch (Main.initializeColor) {
			case "Midpoint":
				// apply Midpoint Color
				int MidpointX = (int) (sumX / 3);
				int MidpointY = (int) (sumY / 3);

				HSB = GetColorOfPixel(loctoindex(MidpointX, MidpointY));
				break;

			case "ColortargetPicture":
				HSB = GetColorOfPixel(r.nextInt(ColorArray.length));
				break;
			}

			values[triangleIndex * VALUES_PER_TRIANGLE + 0] = HSB[0];
			values[triangleIndex * VALUES_PER_TRIANGLE + 1] = HSB[1];
			values[triangleIndex * VALUES_PER_TRIANGLE + 2] = HSB[2];

			if (Main.initalizeConstantAlpha) {
				values[triangleIndex * VALUES_PER_TRIANGLE + 3] = Main.Alpha;
			} else {
				values[triangleIndex * VALUES_PER_TRIANGLE + 3] = r.nextInt(256);
			}

		}

	}

	public void initializeFillingBordersOld() {
		int[] ColorArray = TargetPictureColors();
		values = new int[instance.getNumberOfTriangles() * VALUES_PER_TRIANGLE];
		int sumX = 0;
		int sumY = 0;
		int[] xborders = { 0, instance.getImageWidth() / 2, instance.getImageWidth() / 4, 0,
				(instance.getImageWidth() / 4) * 3, 0, instance.getImageWidth() };
		int[] yborders = { 0, instance.getImageHeight() / 2, instance.getImageHeight() / 4, 0,
				(instance.getImageHeight() / 4) * 3, 0, instance.getImageHeight() };

		for (int triangleIndex = 0; triangleIndex < instance.getNumberOfTriangles(); triangleIndex++) {

			sumX = 0;
			sumY = 0;

			// first fill the 20 triangles on the outside
			if (triangleIndex < 20) {
				for (int i = 4; i <= 8; i += 2) {

					values[triangleIndex * VALUES_PER_TRIANGLE + i] = xborders[r.nextInt(xborders.length - 1)];
					values[triangleIndex * VALUES_PER_TRIANGLE + i + 1] = yborders[r.nextInt(yborders.length - 1)];

					sumX += values[triangleIndex * VALUES_PER_TRIANGLE + i];
					sumY += values[triangleIndex * VALUES_PER_TRIANGLE + i + 1];

				}
			} else {
				// initialize vertices
				for (int i = 4; i <= 8; i += 2) {

					values[triangleIndex * VALUES_PER_TRIANGLE + i] = r.nextInt(instance.getImageWidth() + 1);
					values[triangleIndex * VALUES_PER_TRIANGLE + i + 1] = r.nextInt(instance.getImageHeight() + 1);

					sumX += values[triangleIndex * VALUES_PER_TRIANGLE + i];
					sumY += values[triangleIndex * VALUES_PER_TRIANGLE + i + 1];
				}
			}

			// apply Midpoint Color
			int MidpointX = (int) (sumX / 3);
			int MidpointY = (int) (sumY / 3);

			int HSB[] = GetColorOfPixel(loctoindex(MidpointX, MidpointY));

			/*
			 * apply Random Color of TargetPicture int[] HSB =
			 * GetColorOfPixel(r.nextInt(ColorArray.length));
			 */
			// initialize HSB and Alpha
			// values[triangleIndex * VALUES_PER_TRIANGLE + i] = r.nextInt(256);
			values[triangleIndex * VALUES_PER_TRIANGLE + 0] = HSB[0];
			values[triangleIndex * VALUES_PER_TRIANGLE + 1] = HSB[1];
			values[triangleIndex * VALUES_PER_TRIANGLE + 2] = HSB[2];
			values[triangleIndex * VALUES_PER_TRIANGLE + 3] = Main.Alpha;// r.nextInt(256);
		}
	}

	// function to get the color of a location (Pixel) in the targetpicture

	public int[] GetColorOfPixel(int Pixel) {
		Pixel = (Pixel >= 40000 ? 39999 : Pixel);
		int[] targetPixels = instance.getTargetPixels();
		int c1 = targetPixels[Pixel];

		int red = ((c1 >> 16) & 0xff);
		int green = ((c1 >> 8) & 0xff);
		int blue = (c1 & 0xff);

		float[] hsb = Color.RGBtoHSB(red, green, blue, null);
		float hue = hsb[0] * 255;
		float saturation = hsb[1] * 255;
		float brightness = hsb[2] * 255;

		int[] A = { Math.round(hue), Math.round(saturation), Math.round(brightness) };
		return A;
	}

	public Solution applyMidpointColor() {
		Solution temp = this.copy();
		int sumX = 0;
		int sumY = 0;

		for (int triangleIndex = 0; triangleIndex < instance.getNumberOfTriangles(); triangleIndex++) {
			sumX = 0;
			sumY = 0;
			// initialize vertices
			for (int i = 4; i <= 8; i += 2) {

				sumX += temp.values[triangleIndex * VALUES_PER_TRIANGLE + i];
				sumY += temp.values[triangleIndex * VALUES_PER_TRIANGLE + i + 1];
			}

			int MidpointX = sumX / 3;
			int MidpointY = sumY / 3;

			int HSB[] = GetColorOfPixel(loctoindex(MidpointX, MidpointY));

			// initialize HSB and Alpha
			// values[triangleIndex * VALUES_PER_TRIANGLE + i] = r.nextInt(256);
			temp.values[triangleIndex * VALUES_PER_TRIANGLE + 0] = HSB[0];
			temp.values[triangleIndex * VALUES_PER_TRIANGLE + 1] = HSB[1];
			temp.values[triangleIndex * VALUES_PER_TRIANGLE + 2] = HSB[2];
			temp.values[triangleIndex * VALUES_PER_TRIANGLE + 3] = r.nextInt(256);
		}
		return temp;
	}

	////////// MUTATION OPERATORS
	////////// /////////////////////////////////////////////////////////////////////
	/// ---apply Mutation with Amount
	public Solution applyMutationWithAmount(int[] ColorArray) {

		double MutationAmount = Main.MUTATION_AMOUNT;
		Solution temp = this.copy();// copy current solution as temp
		for (int i = 0; i < (instance.numberOfTriangles * VALUES_PER_TRIANGLE * MutationAmount); i++) {

			int triangleIndex = r.nextInt(instance.getNumberOfTriangles()); // (n excluded) get random number between 0
																			// and 99 number of triangles
			int valueIndex = r.nextInt(VALUES_PER_TRIANGLE); // get valueindex between 0 and 9

			if (valueIndex < 3) { // to check which array of each triangle it is color and coordinate parts
				int[] HSB = GetColorOfPixel(r.nextInt(ColorArray.length));
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = HSB[valueIndex];
			} else {
				if (valueIndex == 3) {
					temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r.nextInt(256);
				} else {

					if (valueIndex % 2 == 0) { // remainder to check if its Y of vertex, Y at valueIndex 5,7,9
						temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r
								.nextInt(instance.getImageWidth() + 1); // Y value can only be between 0 and ImageWidth
					} else { // else valueIndex is X of vertex
						temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r
								.nextInt(instance.getImageHeight() + 1); // X value can only be between 0 and ImageWidth
					}
				}
			}
		}
		return temp;
	}

	public Solution applyScrambleMutation() {

		// copy current solution as temp
		Solution temp = this.copy();
		// get random triangle index
		int triangleIndex = r.nextInt(instance.getNumberOfTriangles());

		// get Index (start) of that Triangle, [TriangleValueIndexStart,
		// TriangleValueIndexStart+3] color triangle array
		int TriangleValueIndexStartColor = triangleIndex * VALUES_PER_TRIANGLE;

		int[] xArray = new int[] { 4, 6, 8 };
		int[] yArray = new int[] { 5, 7, 9 };

		// swap now each color index for the random chosen Triangle with values from
		// another Triangle
		for (int i = TriangleValueIndexStartColor + 3; i >= TriangleValueIndexStartColor; i--) {

			int index = (VALUES_PER_TRIANGLE * r.nextInt(instance.getNumberOfTriangles())) + r.nextInt(4);
			// Simple swap
			int a = temp.values[index]; // store value at random color index in a
			temp.values[index] = temp.values[i]; // change the value at that random color index to initial random Color
													// Index
			temp.values[i] = a; // now change the inital color index to the value stored in a
		}
		for (int xy = TriangleValueIndexStartColor + 9; xy >= TriangleValueIndexStartColor + 4; xy--) {
			int valueIndex = (xy - (xy / 10 * 10));

			if (valueIndex % 2 == 0) {

				int xvalueIndex = r.nextInt(3);
				int xindex = (VALUES_PER_TRIANGLE * r.nextInt(instance.getNumberOfTriangles())) + xArray[xvalueIndex];

				// Simple swap
				int a = temp.values[xindex];
				temp.values[xindex] = temp.values[xy];
				temp.values[xy] = a;

			} else {

				int yvalueIndex = r.nextInt(3);
				int yindex = (VALUES_PER_TRIANGLE * r.nextInt(instance.getNumberOfTriangles())) + yArray[yvalueIndex];

				// Simple swap
				int a = temp.values[yindex];
				temp.values[yindex] = temp.values[xy];
				temp.values[xy] = a;
			}
		}
		return temp;
	}

	///
	public Solution applyMutationTargetPictureColor(int[] ColorArray) {
		Solution temp = this.copy(); // copy current solution as temp
		int triangleIndex = r.nextInt(instance.getNumberOfTriangles()); // (n excluded) get random number between 0 and
																		// 99 number of triangles
		int valueIndex = r.nextInt(VALUES_PER_TRIANGLE); // get valueindex between 0 and 9
		if (valueIndex < 3) { // to check which array of each triangle it is color and coordinate parts
			int[] HSB = GetColorOfPixel(r.nextInt(ColorArray.length));
			temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = HSB[valueIndex];
		} else {
			if (valueIndex == 3) {
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r.nextInt(256);
			} else {
				if (valueIndex % 2 == 0) { // remainder to check if its Y of vertex, Y at valueIndex 5,7,9
					temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r
							.nextInt(instance.getImageWidth() + 1); // Y value can only be between 0 and ImageWidth
				} else { // else valueIndex is X of vertex
					temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r
							.nextInt(instance.getImageHeight() + 1); // X value can only be between 0 and ImageWidth
				}
			}
		}
		return temp;
	}

	/// Apply constant Alpha

	public Solution applyMutationConstantAlpha() {
		Solution temp = this.copy(); // copy current solution as temp
		int triangleIndex = r.nextInt(instance.getNumberOfTriangles()); // (n excluded) get random number between 0 and
																		// 99 number of triangles
		int valueIndex = r.nextInt(VALUES_PER_TRIANGLE); // get valueindex between 0 and 9
		if (valueIndex < 3) { // change back to 4 //to check which array of each triangle it is color and
								// coordinate parts
			temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r.nextInt(256); // color between 0 and 256

		} else {
			if (valueIndex == 3) { // to check which array of each triangle it is color and coordinate parts
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + 3] = Main.Alpha;// r.nextInt(256);

			} else {
				if (valueIndex % 2 == 0) { // remainder to check if its Y of vertex, Y at valueIndex 5,7,9
					temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r
							.nextInt(instance.getImageWidth() + 1); // Y value can only be between 0 and ImageWidth
				} else { // else valueIndex is X of vertex
					temp.values[triangleIndex * VALUES_PER_TRIANGLE + valueIndex] = r
							.nextInt(instance.getImageHeight() + 1); // X value can only be between 0 and ImageWidth
				}
			}
		}
		return temp;
	}

	public static boolean isintriangle(int[] triangle, int[] Targetpoint) {

		boolean bOne = false;
		boolean bTwo = false;
		boolean bThree = false;

		// first check
		if (triangle[2] == triangle[0]) {
			if (((triangle[4] > triangle[0]) && (Targetpoint[0] > triangle[0]))
					|| ((triangle[4] < triangle[0]) && (Targetpoint[0] < triangle[0]))) {
				bOne = true;
			}
		} else {
			if (((((triangle[3] - triangle[1]) / (triangle[2] - triangle[0])) * triangle[4] + (triangle[1]
					- ((triangle[3] - triangle[1]) / (triangle[2] - triangle[0])) * triangle[0]) < triangle[5])
					&& (((triangle[3] - triangle[1]) / (triangle[2] - triangle[0])) * Targetpoint[0]
							+ (triangle[1] - ((triangle[3] - triangle[1]) / (triangle[2] - triangle[0]))
									* triangle[0]) < Targetpoint[1]))
					|| ((((triangle[3] - triangle[1]) / (triangle[2] - triangle[0])) * triangle[4] + (triangle[1]
							- ((triangle[3] - triangle[1]) / (triangle[2] - triangle[0])) * triangle[0]) > triangle[5])
							&& (((triangle[3] - triangle[1]) / (triangle[2] - triangle[0])) * Targetpoint[0]
									+ (triangle[1] - ((triangle[3] - triangle[1]) / (triangle[2] - triangle[0]))
											* triangle[0]) > Targetpoint[1]))) {
				bOne = true;
			}
		}

		// second check
		if (triangle[4] == triangle[0]) {
			if (((triangle[2] > triangle[0]) && (Targetpoint[0] > triangle[0]))
					|| ((triangle[2] < triangle[0]) && (Targetpoint[0] < triangle[0]))) {
				bTwo = true;
			}
		} else {
			if (((((triangle[5] - triangle[1]) / (triangle[4] - triangle[0])) * triangle[2] + (triangle[1]
					- ((triangle[5] - triangle[1]) / (triangle[4] - triangle[0])) * triangle[0]) < triangle[3])
					&& (((triangle[5] - triangle[1]) / (triangle[4] - triangle[0])) * Targetpoint[0]
							+ (triangle[1] - ((triangle[5] - triangle[1]) / (triangle[4] - triangle[0]))
									* triangle[0]) < Targetpoint[1]))
					|| ((((triangle[5] - triangle[1]) / (triangle[4] - triangle[0])) * triangle[2] + (triangle[1]
							- ((triangle[5] - triangle[1]) / (triangle[4] - triangle[0])) * triangle[0]) > triangle[3])
							&& (((triangle[5] - triangle[1]) / (triangle[4] - triangle[0])) * Targetpoint[0]
									+ (triangle[1] - ((triangle[5] - triangle[1]) / (triangle[4] - triangle[0]))
											* triangle[0]) > Targetpoint[1]))) {
				bTwo = true;
			}
		}

		// third check
		if (triangle[4] == triangle[2]) {
			if (((triangle[0] > triangle[2]) && (Targetpoint[0] > triangle[2]))
					|| ((triangle[0] < triangle[2]) && (Targetpoint[0] < triangle[2]))) {
				bThree = true;
			}
		} else {
			if (((((triangle[5] - triangle[3]) / (triangle[4] - triangle[2])) * triangle[0] + (triangle[3]
					- ((triangle[5] - triangle[3]) / (triangle[4] - triangle[2])) * triangle[2]) < triangle[1])
					&& (((triangle[5] - triangle[3]) / (triangle[4] - triangle[2])) * Targetpoint[0]
							+ (triangle[3] - ((triangle[5] - triangle[3]) / (triangle[4] - triangle[2]))
									* triangle[2]) < Targetpoint[1]))
					|| ((((triangle[5] - triangle[3]) / (triangle[4] - triangle[2])) * triangle[0] + (triangle[3]
							- ((triangle[5] - triangle[3]) / (triangle[4] - triangle[2])) * triangle[2]) > triangle[1])
							&& (((triangle[5] - triangle[3]) / (triangle[4] - triangle[2])) * Targetpoint[0]
									+ (triangle[3] - ((triangle[5] - triangle[3]) / (triangle[4] - triangle[2]))
											* triangle[2]) > Targetpoint[1]))) {
				bThree = true;
			}
		}
		if ((bOne == true) && (bTwo == true) && (bThree == true)) {
			return true;
		} else {
			return false;
		}
	}

	public Solution applyColorTriangleAmount() {
		Solution temp = this.copy();
		int[] Triangle = new int[6];
		double amount = Main.MUTATION_AMOUNT;
		// BufferedImage generatedImage = createImage();

		for (int index = 0; index < instance.getNumberOfTriangles() * amount; index++) {
			int triangleIndex = r.nextInt(instance.getNumberOfTriangles());
			int sum = 0;
			for (int i = 4; i <= 8; i += 2) {

				Triangle[i - 4] = temp.values[triangleIndex * VALUES_PER_TRIANGLE + i];
				Triangle[i - 3] = temp.values[triangleIndex * VALUES_PER_TRIANGLE + i + 1];
			}

			int[] TrianglePixels = getImageIndices(Triangle);

			for (int i = 0; i < TrianglePixels.length; i++) {
				if (TrianglePixels[i] > 0) {
					sum += 1;
				}
			}
			int[] PixelArray = new int[sum];
			int idx = 0;
			for (int i = 0; i < TrianglePixels.length; i++) {
				if (TrianglePixels[i] > 0) {
					PixelArray[idx] = TrianglePixels[i]; // starts from 0 or 1??? do I have to add one?
					idx++;
				}
			}

			if (sum > 0) {
				int HSB[] = GetColorOfPixel(PixelArray[r.nextInt(PixelArray.length)]);
				// initialize HSB and Alpha
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + 0] = HSB[0];
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + 1] = HSB[1];
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + 2] = HSB[2];
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + 3] = r.nextInt(256);
			} else {
				int HSB[] = GetColorOfPixel(r.nextInt(instance.getImageHeight() * instance.getImageWidth()));
				// initialize HSB and Alpha
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + 0] = HSB[0];
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + 1] = HSB[1];
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + 2] = HSB[2];
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + 3] = r.nextInt(256);

			}
		}
		return temp;

	}

	/*
	 * Loop through each Triangle get Pixels inside Triangle get colors of that
	 * triangle - store in array random resetting of each triangle based on the
	 * colors in the area from targetPicture
	 */

	public Solution applyColorTriangleAll() {
		Solution temp = this.copy();
		int[] Triangle = new int[6];
		// BufferedImage generatedImage = createImage();

		for (int triangleIndex = 0; triangleIndex < instance.getNumberOfTriangles(); triangleIndex++) {
			int sum = 0;
			for (int i = 4; i <= 8; i += 2) {

				Triangle[i - 4] = temp.values[triangleIndex * VALUES_PER_TRIANGLE + i];
				Triangle[i - 3] = temp.values[triangleIndex * VALUES_PER_TRIANGLE + i + 1];
			}

			int[] TrianglePixels = getImageIndices(Triangle);

			for (int i = 0; i < TrianglePixels.length; i++) {
				if (TrianglePixels[i] > 0) {
					sum += 1;
				}
			}
			int[] PixelArray = new int[sum];
			int idx = 0;
			for (int i = 0; i < TrianglePixels.length; i++) {
				if (TrianglePixels[i] > 0) {
					PixelArray[idx] = TrianglePixels[i]; // starts from 0 or 1??? do I have to add one?
					idx++;
				}
			}

			if (sum > 0) {
				int HSB[] = GetColorOfPixel(PixelArray[r.nextInt(PixelArray.length)]);
				// System.out.println(HSB[1]);
				// initialize HSB and Alpha
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + 0] = HSB[0];
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + 1] = HSB[1];
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + 2] = HSB[2];
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + 3] = r.nextInt(256);
			} else {
				int HSB[] = GetColorOfPixel(r.nextInt(instance.getImageHeight() * instance.getImageWidth()));
				// initialize HSB and Alpha
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + 0] = HSB[0];
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + 1] = HSB[1];
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + 2] = HSB[2];
				temp.values[triangleIndex * VALUES_PER_TRIANGLE + 3] = r.nextInt(256);

			}
		}
		return temp;

	}

	//// get Pixels Triangle//////////

	public int[] getImageIndices(int[] triangle) {
		// check which pixels are inside the triangle

		// BufferedImage generatedImage = createImage();
		int[] generatedPixels = new int[instance.getImageWidth() * instance.getImageHeight()];
		int[] checkPixels = new int[instance.getImageWidth() * instance.getImageHeight()];

		/*
		 * PixelGrabber pg = new PixelGrabber(generatedImage, 0, 0,
		 * instance.getImageWidth(), instance.getImageHeight(), generatedPixels, 0,
		 * instance.getImageWidth()); try { pg.grabPixels(); } catch
		 * (InterruptedException e) { e.printStackTrace(); }
		 */

		int[] targetPixels = instance.getTargetPixels();
		int[] TP = { 0, 0 };
		int sizeoftriangle = 0;
		int count = 0;

		int[] indices = { loctoindex(triangle[0], triangle[1]), loctoindex(triangle[2], triangle[3]),
				loctoindex(triangle[4], triangle[5]) };
		int min = Arrays.stream(indices).min().getAsInt();
		int max = Arrays.stream(indices).max().getAsInt();

		// for each row 1 to 200
		for (int i = min; i < max + 1; i++) {
			TP = indextoloc(i);
			if (isintriangle(triangle, TP) == true) {
				checkPixels[count] = loctoindex(TP[0], TP[1]); // first index is 0 !!! WARNING
				sizeoftriangle++;
				count++;
			} else {
				checkPixels[count] = 0; // CHANGED TO 0 (?)
				count++;

			}
		}
		return checkPixels;
	}

	/// Retrieve Index(Pixel) based on (x,y) coordinates

	public static int loctoindex(int x, int y) {
		// BufferedImage generatedImage = createImage();
		int index = (y * instance.getImageWidth()) + x;
		int rt = (index >= 40000 ? 39999 : index);
		// System.out.println("Index" + index);
		// System.out.println("return" + rt);
		return rt;
	}

	/// Retrieve (x,y) coordinates based in index (Pixel)

	public static int[] indextoloc(int index) {
		int[] loc = { 0, 0 };
		// BufferedImage generatedImage = createImage();
		// first the x

		loc[0] = (int) Math.round((index) / instance.getImageWidth());
		loc[1] = (index) - (loc[0] * instance.getImageWidth());
		return loc;
	}

	// order all triangles from small x to big x
	public static Solution ordersolution(Solution inputsolution) {
		Solution output = inputsolution;
		int[] x = { 0, 0, 0 };
		int[] y = { 0, 0, 0 };
		int xtemp = 0;
		int ytemp = 0;

		for (int j = 0; j < Main.NUMBER_OF_TRIANGLES; j++) {
			for (int i = 1; i < 4; i++) {
				// array from 1 until 3
				x[i - 1] = inputsolution.getValue(i * 2 + 2 + (j * VALUES_PER_TRIANGLE));
				y[i - 1] = inputsolution.getValue(i * 2 + 2 + (j * VALUES_PER_TRIANGLE) + 1);
			}

			// check if a change is needed between x1 and x2
			if (x[1] < x[0]) {
				xtemp = x[0];
				ytemp = y[0];
				x[0] = x[1];
				y[0] = y[1];
				x[1] = xtemp;
				y[1] = ytemp;

			}
			// check if a change is needed between x2 and x3
			if (x[2] < x[1]) {
				xtemp = x[1];
				ytemp = y[1];
				x[1] = x[2];
				y[1] = y[2];
				x[2] = xtemp;
				y[2] = ytemp;

			}
			// check again if a change is needed between x1 and x2
			if (x[1] < x[0]) {
				xtemp = x[0];
				ytemp = y[0];
				x[0] = x[1];
				y[0] = y[1];
				x[1] = xtemp;
				y[1] = ytemp;
			}
			// store the sorted array
			for (int i = 1; i < 4; i++) {
				output.setValue(i * 2 + 2 + (j * VALUES_PER_TRIANGLE), x[i - 1]);
				output.setValue(i * 2 + 2 + (j * VALUES_PER_TRIANGLE) + 1, y[i - 1]);
			}
		}

		return output;
	}

	/// Calculate the function of a line
	// SLOPE
	public static float slope(int[] a, int[] b) {
		if (a[0] == b[0]) {
			return 0;
		}
		return (float) ((b[1] - a[1]) / (1.00 * (b[0] - a[0])));
	}

	// INTERCEPT
	public static float intercept(int[] point, float slope) {
		if (slope == 0) {
			// vertical line
			return point[0];
		}
		return point[1] - slope * point[0];
	}

	// COORDINATES
	public static int[] Coordinates(int A[], int B[]) {

		float m = slope(A, B);
		float b = intercept(A, m);

		int[] Xcoordinates = new int[Math.abs(A[0] - B[0]) + 1];
		int[] Ycoordinates = new int[Math.abs(A[0] - B[0]) + 1];
		int[] Pixels = new int[Math.abs(A[0] - B[0]) + 1];
		int i = 0;
		int Min = (A[0] > B[0] ? B[0] : A[0]);
		int Max = (A[0] < B[0] ? B[0] : A[0]);
		for (int x = Min; x <= Max; x++) {
			float y = m * x + b;
			Xcoordinates[i] = x;
			Ycoordinates[i] = Math.round(y);
			Pixels[i] = x * Math.round(y);
			i++;
		}
		return Pixels;
	}

	public static double getdifference(Solution basesolution, Solution comparesolution) {
		double sum = 0;
		int[] coefficients = new int[Main.NUMBER_OF_TRIANGLES * VALUES_PER_TRIANGLE];
		int coefficient = Main.NUMBER_OF_TRIANGLES
				* (4 * 255 + 3 * instance.getImageWidth() + 3 * instance.getImageHeight());

		// loop over the solution to generate the total sharing coefficient

		// the biggest possible difference per individual = 222000
		// 255*4*100 + 200*6*100 = 222000

		// only for the colors
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < Main.NUMBER_OF_TRIANGLES - 1; j++) {
				// coefficients[i+j*VALUES_PER_TRIANGLE] =
				// (255-(basesolution.getValue(i+j*VALUES_PER_TRIANGLE)-comparesolution.getValue(i+j*VALUES_PER_TRIANGLE)))/255;
				coefficients[i + j * VALUES_PER_TRIANGLE] = Math.abs(basesolution.getValue(i + j * VALUES_PER_TRIANGLE)
						- comparesolution.getValue(i + j * VALUES_PER_TRIANGLE)) / coefficient;

			}
		}
		// only for the locations (x,y)
		for (int i = 4; i < 10; i++) {
			for (int j = 0; j < Main.NUMBER_OF_TRIANGLES - 1; j++) {
				// coefficients[i+j*VALUES_PER_TRIANGLE] =
				// (200-(basesolution.getValue(i+j*VALUES_PER_TRIANGLE)-comparesolution.getValue(i+j*VALUES_PER_TRIANGLE)))/200;
				coefficients[i + j * VALUES_PER_TRIANGLE] = Math.abs(basesolution.getValue(i + j * VALUES_PER_TRIANGLE)
						- comparesolution.getValue(i + j * VALUES_PER_TRIANGLE)) / coefficient;
			}
		}

		// after debugging and testing, we can improve the performance of this code
		for (int i = 0; i < coefficients.length; i++) {
			sum = sum + coefficients[i];
		}

		// this sum is a number between 0 and 1
		return sum;

	}

	// order the triangles based on middlepoint (bubble sort)
	public static Solution orderwholesolution(Solution inputsolution) {
		Solution output = inputsolution;
		int[] middlepoints = new int[Main.NUMBER_OF_TRIANGLES];
		int[] triangle = { 0, 0, 0, 0, 0, 0 };
		int[] temp = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		int tempindex = 0;

		// calculate all the middlepoints
		for (int i = 0; i < Main.NUMBER_OF_TRIANGLES; i++) {
			for (int j = 0; j < 6; j++) {
				triangle[j] = output.getValue(i * VALUES_PER_TRIANGLE + 4 + j);
			}
			middlepoints[i] = getmiddlepointindex(triangle);
		}

		// bubble sort to sort the solution based on the middlepointindex
		for (int i = 0; i < Main.NUMBER_OF_TRIANGLES; i++) {
			for (int j = 0; j < Main.NUMBER_OF_TRIANGLES - 1; j++) {
				if (middlepoints[j] > middlepoints[j + 1]) {
					// switch them

					// store everything from j in temp
					for (int k = 0; k < 10; k++) {
						temp[k] = output.getValue(j * VALUES_PER_TRIANGLE + k);
					}

					// store everything form j+1 in j
					for (int k = 0; k < 10; k++) {
						output.setValue(j * VALUES_PER_TRIANGLE + k,
								output.getValue((j + 1) * VALUES_PER_TRIANGLE + k));
					}

					// store everything from temp in j+1
					for (int k = 0; k < 10; k++) {
						output.setValue((j + 1) * VALUES_PER_TRIANGLE + k, temp[k]);
					}

					// switch the middlepoints
					tempindex = middlepoints[j];
					middlepoints[j] = middlepoints[j + 1];
					middlepoints[j + 1] = tempindex;
				}
			}
		}

		return output;
	}

	// calculates all the shared fitnesses of the individuals in the population
	public static double[] getsharedfitnesses(Solution[] solutions) {

		double[] fitnesses = new double[Main.POPULATION_SIZE];
		double dif = 0.0;

		for (int i = 0; i < Main.POPULATION_SIZE; i++) {
			double sumdifs = 0.0;
			for (int j = 0; j < Main.POPULATION_SIZE; j++) {
				if (i != j) {
					// calculate the difference
					dif = getdifference(solutions[i], solutions[j]);
					sumdifs = sumdifs + (1 - dif);
				}
			}
			fitnesses[i] = sumdifs * solutions[i].getFitness();
		}
		return fitnesses;
	}

	// function to receive the index of the middle point, input: XYXYXY
	public static int getmiddlepointindex(int[] triangle) {
		int[] middle = { 0, 0 };
		int centroidindex = 0;
		// the X point of the middle point
		middle[0] = (int) (triangle[0] + triangle[2] + triangle[4]) / 3;

		// the Y
		middle[1] = (int) (triangle[1] + triangle[3] + triangle[5]) / 3;

		centroidindex = loctoindex(middle[0], middle[1]);
		return centroidindex;
	}

	/* NOT USED
	 * ///Function to give back the fitness of the triangle and the fitness per
	 * pixel public static double[] getFitnessofTriangleold(int[] triangle, Solution
	 * individual) { // check which pixels are inside the triangle BufferedImage
	 * generatedImage = createImage();
	 * 
	 * int[] generatedPixels = new int[instance.getImageWidth() *
	 * instance.getImageHeight()]; int[] checkPixels = new
	 * int[instance.getImageWidth() * instance.getImageHeight()];
	 * 
	 * PixelGrabber pg = new PixelGrabber(generatedImage, 0, 0,
	 * instance.getImageWidth(), instance.getImageHeight(), generatedPixels, 0,
	 * instance.getImageWidth()); try { pg.grabPixels(); } catch
	 * (InterruptedException e) { e.printStackTrace(); } int[] targetPixels =
	 * instance.getTargetPixels();
	 * 
	 * int[] TP = {0,0}; int sizeoftriangle = 0;
	 * 
	 * int[] indices =
	 * {loctoindex(triangle[0],triangle[1]),loctoindex(triangle[2],triangle[3]),
	 * loctoindex(triangle[4],triangle[5])}; int min =
	 * Arrays.stream(indices).min().getAsInt(); int max =
	 * Arrays.stream(indices).max().getAsInt();
	 * 
	 * 
	 * for(int i = min; i<max+1;i++) { TP = indextoloc(i);
	 * if(isintriangle(triangle,TP)==true) { checkPixels[i] = 1; sizeoftriangle++;
	 * }else { checkPixels[i] = 0; } }
	 * 
	 * long sum = 0; for (int i = 0; i < targetPixels.length; i++) { if
	 * (checkPixels[i] == 1) { int c1 = targetPixels[i]; int c2 =
	 * generatedPixels[i]; int red = ((c1 >> 16) & 0xff) - ((c2 >> 16) & 0xff); int
	 * green = ((c1 >> 8) & 0xff) - ((c2 >> 8) & 0xff); int blue = (c1 & 0xff) - (c2
	 * & 0xff); sum += red * red + green * green + blue * blue; }
	 * 
	 * } //penalty if it is not a proper triangle if(sum == 0) { sum = 1000000000;
	 * sizeoftriangle = 1; }
	 * 
	 * double[] fitness = {Math.sqrt(sum),Math.sqrt(sum)/sizeoftriangle};
	 * 
	 * //test return fitness; }
	 */

}
