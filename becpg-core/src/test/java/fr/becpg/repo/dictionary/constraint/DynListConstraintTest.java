package fr.becpg.repo.dictionary.constraint;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Unit tests for {@link DynListConstraint#putSystemListEntry(Map, DynListEntry)}.
 */
public class DynListConstraintTest {

    private static final String CODE = "QUAL01";

    @Test
    public void keepsActiveEntryWhenDuplicateCodeIsDeleted() {
        Map<String, DynListEntry> entries = new LinkedHashMap<>();
        DynListEntry activeEntry = createEntry(false);

        DynListConstraint.putSystemListEntry(entries, activeEntry);
        DynListConstraint.putSystemListEntry(entries, createEntry(true));

        assertSame(activeEntry, entries.get(CODE));
    }

    @Test
    public void replacesDeletedEntryWithActiveDuplicate() {
        Map<String, DynListEntry> entries = new LinkedHashMap<>();
        DynListEntry activeEntry = createEntry(null);

        DynListConstraint.putSystemListEntry(entries, createEntry(true));
        DynListConstraint.putSystemListEntry(entries, activeEntry);

        assertSame(activeEntry, entries.get(CODE));
    }

    @Test
    public void keepsLastEntryWhenDuplicatesAreActive() {
        Map<String, DynListEntry> entries = new LinkedHashMap<>();
        DynListEntry lastEntry = createEntry(false);

        DynListConstraint.putSystemListEntry(entries, createEntry(false));
        DynListConstraint.putSystemListEntry(entries, lastEntry);

        assertSame(lastEntry, entries.get(CODE));
    }

    @Test
    public void keepsDeletedEntryWhenNoActiveDuplicateExists() {
        Map<String, DynListEntry> entries = new LinkedHashMap<>();

        DynListConstraint.putSystemListEntry(entries, createEntry(true));

        assertFalse(entries.isEmpty());
    }

    private static DynListEntry createEntry(Boolean isDeleted) {
        DynListEntry entry = new DynListEntry();
        entry.setCode(CODE);
        entry.setIsDeleted(isDeleted);
        return entry;
    }
}
