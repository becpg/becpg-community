package fr.becpg.repo.product.data.productList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;

import fr.becpg.repo.product.data.constraints.IngListFlag;

public class IngListDataItemTest {

    @Test
    public void aNewLineCarriesNoFlag() {
        IngListDataItem line = new IngListDataItem();

        assertTrue(line.getFlags().isEmpty());
        assertFalse(line.getIsProcessingAid());
    }

    @Test
    public void legacySettersStoreFlags() {
        IngListDataItem line = new IngListDataItem();
        line.setIsSupport(true);
        line.setIsGMO(true);

        assertEquals(Arrays.asList("SUPPORT", "GMO"), line.getFlags());
    }

    @Test
    public void settingAFlagToFalseRemovesIt() {
        IngListDataItem line = new IngListDataItem();
        line.setIsNano(true);
        line.setIsNano(false);

        assertFalse(line.hasFlag(IngListFlag.NANO));
    }

    @Test
    public void aNullValueLeavesTheFlagUnchanged() {
        IngListDataItem line = new IngListDataItem();
        line.setIsImpurity(true);
        line.setIsImpurity(null);

        assertTrue(line.getIsImpurity());
    }

    @Test
    public void flagsReadFromTheRepositoryDriveTheLegacyGetters() {
        IngListDataItem line = new IngListDataItem();
        line.setFlags(Arrays.asList("IONIZED", "PROCESSING_AID"));

        assertTrue(line.getIsIonized());
        assertTrue(line.getIsProcessingAid());
        assertFalse(line.getIsSupport());
    }

    @Test
    public void linesWithTheSameFlagsInADifferentOrderAreEqual() {
        IngListDataItem first = new IngListDataItem();
        first.setFlags(Arrays.asList("NANO", "GMO"));
        IngListDataItem second = new IngListDataItem();
        second.setFlags(Arrays.asList("GMO", "NANO"));

        assertEquals(first, second);
    }

    @Test
    public void theCopyDoesNotShareItsFlags() {
        IngListDataItem line = new IngListDataItem();
        line.setIsGMO(true);
        IngListDataItem copy = line.copy();
        copy.setIsGMO(false);

        assertTrue(line.getIsGMO());
    }
}
