package fr.becpg.repo.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.Test;

import fr.becpg.repo.helper.ExcelHelper.ExcelCellStyles;

/**
 * Unit tests for {@link ExcelCellStyles}.
 */
public class ExcelHelperTest {

    private static final int EXPORTED_ENTITIES = 70000;

    @Test
    public void instancesOnTheSameWorkbookShareTheirStyles() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            short textStyleIndex = new ExcelCellStyles(workbook).getTextCellStyle().getIndex();
            int styleCount = workbook.getNumCellStyles();

            assertEquals(textStyleIndex, new ExcelCellStyles(workbook).getTextCellStyle().getIndex());
            assertEquals(styleCount, workbook.getNumCellStyles());
        }
    }

    @Test
    public void eachStyleKindHasItsOwnStyle() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            ExcelCellStyles styles = new ExcelCellStyles(workbook);

            assertNotEquals(styles.getFullDateCellStyle().getIndex(), styles.getShortDateCellStyle().getIndex());
            assertEquals(14, new ExcelCellStyles(workbook).getFullDateCellStyle().getDataFormat());
            assertEquals(22, new ExcelCellStyles(workbook).getShortDateCellStyle().getDataFormat());
        }
    }

    @Test
    public void eachWorkbookGetsItsOwnStyles() throws IOException {
        try (XSSFWorkbook first = new XSSFWorkbook(); XSSFWorkbook second = new XSSFWorkbook()) {
            new ExcelCellStyles(first).getHeaderTextStyle();
            int secondStyleCount = second.getNumCellStyles();

            new ExcelCellStyles(second).getHeaderTextStyle();

            assertEquals(secondStyleCount + 1, second.getNumCellStyles());
        }
    }

    @Test
    public void exportFilledEntityByEntityStaysUnderTheStylesLimit() throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            for (int i = 0; i < EXPORTED_ENTITIES; i++) {
                ExcelCellStyles styles = new ExcelCellStyles(workbook);
                styles.getTextCellStyle();
                styles.getFullDateCellStyle();
            }

            assertEquals(3, workbook.getNumCellStyles());
        }
    }
}
