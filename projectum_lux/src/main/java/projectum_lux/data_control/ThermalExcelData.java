package projectum_lux.data_control;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javafx.scene.control.Alert.AlertType;
import projectum_lux.helper.UserMessageHandler;

public class ThermalExcelData {
	
	private UserMessageHandler messageHandler;
	
	private Workbook workbook;
    private Sheet sheet;
    private int currentRow = 0;
    private File excelFile;

    public void createExcelFile(Optional<File> diretory) throws IOException {
        try {

            String fileName = "Thermal_"+ new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".xlsx";
            this.excelFile = new File(diretory.get(), fileName);

            this.workbook = new XSSFWorkbook();
            this.sheet = workbook.createSheet("Thermal data");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            Row headerRow = sheet.createRow(0);

            String[] headers = {"Date", "Temperature(C)"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

        } catch (Exception e) {
        	messageHandler.userHandlerModal("Excel thermal data", e.getCause().toString(), e.getMessage(), AlertType.ERROR);
        }
    }


    public void saveDataOnNewLine(String date, double[][] temperature) {
    	try {
	        Row row = sheet.createRow(currentRow++);
	        int col = 0;
	
	        row.createCell(col++).setCellValue(date);
	
	        StringBuilder matrizSerializada = new StringBuilder();
	        for (double[] linha : temperature) {
	        	for (double valor : linha) {
	        		matrizSerializada.append(String.format("%.1f", valor)).append(" ");
	        	}
	        	matrizSerializada.append("; ");
	        }
	        row.createCell(col++).setCellValue(matrizSerializada.toString().trim());
	
	        for (int i = 0; i < col; i++) {
	            sheet.autoSizeColumn(i);
	        }
    	} catch (Exception e) {
         	messageHandler.userHandlerModal("Excel thermal data", e.getCause().toString(), e.getMessage(), AlertType.ERROR);
        }
    }

    public void saveFile() throws IOException {
        try (FileOutputStream fileOut = new FileOutputStream(excelFile)) {
            workbook.write(fileOut);
            workbook.close();
        } catch (Exception e) {
        	messageHandler.userHandlerModal("Excel thermal data", e.getCause().toString(), e.getMessage(), AlertType.ERROR);
        }
    }

}
