package fr.becpg.repo.product.report;

/**
 * Levels a multi-level export writes: every level up to a maximum one, or only the maximum one.
 *
 * <p>Whatever the mode, the export goes down to the maximum level: an "only level N" export
 * reaches the rows of level N through the levels above it, it only leaves those levels out of
 * the sheet.</p>
 *
 * @param maxLevel the deepest level to export, or {@link #UNLIMITED}
 * @param onlyMaxLevel whether the rows of the levels above the maximum one are left out
 */
record ExportLevelFilter(int maxLevel, boolean onlyMaxLevel) {

    /** No maximum level. */
    static final int UNLIMITED = -1;

    /**
     * Tells whether a level lies below the deepest exported one, where the export stops.
     *
     * @param level the level, starting at 1
     * @return true when nothing is exported at this level nor below it
     */
    boolean isBeyondMaxLevel(int level) {
        return (maxLevel != UNLIMITED) && (level > maxLevel);
    }

    /**
     * Tells whether the rows of a level are written to the sheet.
     *
     * @param level the level, starting at 1
     * @return true when the rows of this level are exported
     */
    boolean isRowWritten(int level) {
        if (isBeyondMaxLevel(level)) {
            return false;
        }
        return !onlyMaxLevel || (level == maxLevel);
    }
}
