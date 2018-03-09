package project;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;

public class CreateExcel {

	protected static int rownumber;
	protected static double[] fitnessrun1 = new double[Main.NUMBER_OF_GENERATIONS + 1];
	protected static double[] fitnessrun2 = new double[Main.NUMBER_OF_GENERATIONS + 1];
	protected static double[] fitnessrun3 = new double[Main.NUMBER_OF_GENERATIONS + 1];

	public static void createHeader() {

	}

	// after each run
	public static void storedata(int run, int rownumber, double fitness) {
		if (run == 1) {
			fitnessrun1[rownumber] = fitness;
		} else {
			if (run == 2) {
				fitnessrun2[rownumber] = fitness;
			} else {
				fitnessrun3[rownumber] = fitness;
			}
		}
	}

	// at the very last moment
	public static void writeExcel() {

		HSSFWorkbook workbook = new HSSFWorkbook();
		HSSFSheet sheet = workbook.createSheet("FitnessGeneration");

		// create the header
		try {
			Row rowheading = sheet.createRow(0);
			rowheading.createCell(0).setCellValue("Fitness_1");
			rowheading.createCell(1).setCellValue("Generation_1");
			rowheading.createCell(2).setCellValue("Fitness_2");
			rowheading.createCell(3).setCellValue("Generation_2");
			rowheading.createCell(4).setCellValue("Fitness_3");
			rowheading.createCell(5).setCellValue("Generation_3");
			for (int i = 0; i < 6; i++) {
				CellStyle stylerowHeading = workbook.createCellStyle();
				Font font = workbook.createFont();
				font.setBold(true);
				font.setFontName(HSSFFont.FONT_ARIAL);
				font.setFontHeightInPoints((short) 11);
				stylerowHeading.setFont(font);
				rowheading.getCell(i).setCellStyle(stylerowHeading);
			}
			// Autofit
			for (int i = 0; i < 6; i++)
				sheet.autoSizeColumn(i);

		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		// loop over the length of arrays
		for (int i = 0; i < fitnessrun1.length; i++) {
			Row row = sheet.createRow(i + 1);

			row.createCell(0).setCellValue(fitnessrun1[i]);
			row.createCell(2).setCellValue(fitnessrun2[i]);
			row.createCell(4).setCellValue(fitnessrun3[i]);

			row.createCell(1).setCellValue(i);
			row.createCell(3).setCellValue(i);
			row.createCell(5).setCellValue(i);
		}
		// Save to an excel file
		try {
			FileOutputStream out = new FileOutputStream(new File("C:/Users/Alex/FitnessGeneration.xls"));
			workbook.write(out);
			out.close();
			workbook.close();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

	}

}