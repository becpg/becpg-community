package fr.becpg.test.repo.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.util.Pair;
import org.junit.Test;

import fr.becpg.repo.helper.LargeTextHelper;

public class LargeTextHelperTest {
    @Test
    public void testElipseWithShortText() {
    	
        String input = "Short text";
        String result = LargeTextHelper.elipse(input, 50);
        assertEquals(input, result);
    }

    @Test
    public void testElipseWithLongText() {
        String input = "This is a very long text that needs to be shortened by the elipse function";
        String result = LargeTextHelper.elipse(input, 20);
        assertEquals("This is a very long ...", result);
    }

    @Test
    public void testCreateTextDiffs() {
    	  // Input strings with slight differences
        String string1 = "Hello World. The classic greeting.";
        String string2 = "Hallo Werld. The classic greeting.";

        // Expected results
        String expectedBefore = "Hello Wo";
        String expectedAfter = "Hallo We";

        Pair<String, String> result = LargeTextHelper.createTextDiffs(string1, string2);
        assertEquals(expectedBefore, result.getFirst());
        assertEquals(expectedAfter, result.getSecond());
    }


    @Test
    public void testElipseMLText() {
        MLText mlText = new MLText();
        mlText.put(Locale.ENGLISH, "This is a very long text that needs to be shortened in multiple languages.");
        mlText.put(Locale.FRENCH, "Ceci est un texte très long qui doit être raccourci dans plusieurs langues.");

        MLText result = LargeTextHelper.elipse(mlText);

        // Below the global budget: values are kept as-is and the source is left untouched (no mutation)
        assertEquals(mlText.get(Locale.ENGLISH), result.get(Locale.ENGLISH));
        assertEquals(mlText.get(Locale.FRENCH), result.get(Locale.FRENCH));
    }

    private static String repeat(char c, int length) {
        char[] chars = new char[length];
        Arrays.fill(chars, c);
        return new String(chars);
    }

    /**
     * Ticket #35528: a label written in a single language must not be truncated to ~4000 characters
     * just because the MLText carries empty entries for the other configured locales. The budget must
     * be shared according to the actual content, not the number of locale keys.
     */
    @Test
    public void testSingleContentLanguageAmongManyEmptyLocales() {
        MLText mlText = new MLText();
        mlText.put(Locale.FRENCH, repeat('a', 60000));
        // Other configured locales are present but empty
        for (Locale locale : new Locale[] { Locale.ENGLISH, Locale.GERMAN, Locale.ITALIAN, new Locale("es"),
                new Locale("pt"), new Locale("nl"), new Locale("pl"), new Locale("ru"), new Locale("zh"),
                new Locale("ja"), new Locale("ar") }) {
            mlText.put(locale, "");
        }

        MLText result = LargeTextHelper.elipse(mlText);

        // The single populated language keeps almost the whole budget, not ~4000 (= 50000 / 12)
        assertTrue("French value was over-truncated: " + result.get(Locale.FRENCH).length(),
                result.get(Locale.FRENCH).length() > 40000);
        // Empty locales stay empty
        assertEquals("", result.get(Locale.ENGLISH));
        // Source MLText must NOT be mutated (no leak toward the stored property)
        assertEquals(60000, mlText.get(Locale.FRENCH).length());
    }

    @Test
    public void testMultiLanguageBudgetSharedProportionally() {
        MLText mlText = new MLText();
        mlText.put(Locale.FRENCH, repeat('a', 40000));
        mlText.put(Locale.ENGLISH, repeat('b', 40000));

        MLText result = LargeTextHelper.elipse(mlText);

        // 80000 total > 50000 -> each gets ~25000 (proportional to its size)
        assertTrue(result.get(Locale.FRENCH).length() < 26000);
        assertTrue(result.get(Locale.FRENCH).length() > 24000);
        assertEquals(result.get(Locale.FRENCH).length(), result.get(Locale.ENGLISH).length());
    }

    @Test
    public void testNullMlTextReturnsEmpty() {
        assertTrue(LargeTextHelper.elipse((MLText) null).isEmpty());
    }


    // #35900 : keep the whole under the property size limit by dropping complete languages, never cutting a value

    private static final Function<Locale, String> NOTICE = locale -> "Content too long";

    @Test
    public void testDropOversizedLocalesKeepsEverythingWhenItFits() {
        MLText mlText = new MLText();
        mlText.put(Locale.FRENCH, repeat('a', 20000));
        mlText.put(Locale.ENGLISH, repeat('b', 20000));

        MLText result = LargeTextHelper.dropOversizedLocales(mlText, NOTICE);

        assertEquals(mlText.get(Locale.FRENCH), result.get(Locale.FRENCH));
        assertEquals(mlText.get(Locale.ENGLISH), result.get(Locale.ENGLISH));
    }

    /**
     * Ticket #35900: the languages that fit must be kept complete. Only the largest ones are dropped,
     * and only as many as needed to get back under the limit.
     */
    @Test
    public void testDropOversizedLocalesDropsTheFewestLargestLanguages() {
        MLText mlText = new MLText();
        mlText.put(Locale.FRENCH, repeat('a', 20000));
        mlText.put(Locale.ENGLISH, repeat('b', 20000));
        mlText.put(Locale.GERMAN, repeat('c', 30000));
        mlText.put(Locale.ITALIAN, repeat('d', 40000));

        MLText result = LargeTextHelper.dropOversizedLocales(mlText, NOTICE);

        // 110000 total: dropping it/de is enough to get the remaining 40000 under the limit
        assertEquals("Content too long", result.get(Locale.ITALIAN));
        assertEquals("Content too long", result.get(Locale.GERMAN));
        // The languages that fit keep their full content, untouched
        assertEquals(mlText.get(Locale.FRENCH), result.get(Locale.FRENCH));
        assertEquals(mlText.get(Locale.ENGLISH), result.get(Locale.ENGLISH));
        assertTrue(totalLength(result) <= LargeTextHelper.TEXT_SIZE_LIMIT);
    }

    @Test
    public void testDropOversizedLocalesNeverCutsAValue() {
        MLText mlText = new MLText();
        for (int i = 0; i < 12; i++) {
            mlText.put(new Locale("l" + i), htmlTable(200));
        }

        MLText result = LargeTextHelper.dropOversizedLocales(mlText, NOTICE);

        assertTrue(totalLength(result) <= LargeTextHelper.TEXT_SIZE_LIMIT);
        for (String value : result.values()) {
            // Each value is either the intact table or the notice, never a cut fragment
            assertTrue("Value was cut: " + value, "Content too long".equals(value) || htmlTable(200).equals(value));
        }
    }

    /**
     * A single language that alone busts the limit must be replaced by the notice, otherwise the
     * property write still fails.
     */
    @Test
    public void testDropOversizedLocalesSingleOverLimitLanguage() {
        MLText mlText = new MLText();
        mlText.put(Locale.FRENCH, repeat('a', 60000));

        MLText result = LargeTextHelper.dropOversizedLocales(mlText, NOTICE);

        assertEquals("Content too long", result.get(Locale.FRENCH));
        assertEquals(60000, mlText.get(Locale.FRENCH).length());
    }

    @Test
    public void testDropOversizedLocalesKeepsSinglePopulatedLanguageWhenItFits() {
        MLText mlText = new MLText();
        mlText.put(Locale.FRENCH, repeat('a', 40000));
        for (Locale locale : new Locale[] { Locale.ENGLISH, Locale.GERMAN, Locale.ITALIAN }) {
            mlText.put(locale, "");
        }

        MLText result = LargeTextHelper.dropOversizedLocales(mlText, NOTICE);

        // Empty locales cost nothing, so the only populated language stays complete
        assertEquals(mlText.get(Locale.FRENCH), result.get(Locale.FRENCH));
        assertEquals("", result.get(Locale.ENGLISH));
    }

    /**
     * Empty locales must never be turned into a notice: that would add length instead of freeing any,
     * and would tell the user content was dropped when there was none.
     */
    @Test
    public void testDropOversizedLocalesNeverReplacesEmptyLocales() {
        MLText mlText = new MLText();
        mlText.put(Locale.FRENCH, repeat('a', 40000));
        mlText.put(Locale.ENGLISH, repeat('b', 20000));
        for (Locale locale : new Locale[] { Locale.GERMAN, Locale.ITALIAN }) {
            mlText.put(locale, "");
        }

        MLText result = LargeTextHelper.dropOversizedLocales(mlText, NOTICE);

        // 60000 total: the largest (fr) is dropped, en keeps its full content
        assertEquals("Content too long", result.get(Locale.FRENCH));
        assertEquals(mlText.get(Locale.ENGLISH), result.get(Locale.ENGLISH));
        assertEquals("", result.get(Locale.GERMAN));
        assertEquals("", result.get(Locale.ITALIAN));
        assertTrue(totalLength(result) <= LargeTextHelper.TEXT_SIZE_LIMIT);
    }

    @Test
    public void testDropOversizedLocalesNullReturnsEmpty() {
        assertTrue(LargeTextHelper.dropOversizedLocales(null, NOTICE).isEmpty());
    }

    private static int totalLength(MLText mlText) {
        int total = 0;
        for (String value : mlText.values()) {
            if (value != null) {
                total += value.length();
            }
        }
        return total;
    }


    @Test
    public void testHtmlDiff_NoDifferences() {
        String text1 = "Hello, World!";
        String text2 = "Hello, World!";
        
        String expectedHtml = "<span>Hello, World!</span>"; // No changes should yield same text in a span
        String actualHtml = LargeTextHelper.htmlDiff(text1, text2);
        
        assertEquals(expectedHtml, actualHtml);
    }

    @Test
    public void testHtmlDiff_AddedText() {
        String text1 = "Hello, World!";
        String text2 = "Hello, Beautiful World!";
        
        String expectedHtml = "<span>Hello, </span><ins style=\"background:#e6ffe6;\">Beautiful </ins><span>World!</span>";
        String actualHtml = LargeTextHelper.htmlDiff(text1, text2);
        
        assertEquals(expectedHtml, actualHtml);
    }

    @Test
    public void testHtmlDiff_RemovedText() {
        String text1 = "Goodbye, World!";
        String text2 = "Goodbye!";
        
        String expectedHtml = "<span>Goodbye</span><del style=\"background:#ffe6e6;\">, World</del><span>!</span>";
        String actualHtml = LargeTextHelper.htmlDiff(text1, text2);
        
        assertEquals(expectedHtml, actualHtml);
    }

    @Test
    public void testHtmlDiff_ChangedText() {
        String text1 = "The quick brown fox jumps over the lazy dog.";
        String text2 = "The swift brown fox leaps over the lazy dog.";
        
        String expectedHtml = "<span>The </span><del style=\"background:#ffe6e6;\">qu</del><ins style=\"background:#e6ffe6;\">sw</ins><span>i</span><del style=\"background:#ffe6e6;\">ck</del><ins style=\"background:#e6ffe6;\">ft</ins><span> brown fox </span><del style=\"background:#ffe6e6;\">jum</del><ins style=\"background:#e6ffe6;\">lea</ins><span>ps over the lazy dog.</span>";
        String actualHtml = LargeTextHelper.htmlDiff(text1, text2);

        assertEquals(expectedHtml, actualHtml);
    }

    // #34846 : non-blocking HTML truncation of the labeling table (must not cut mid-tag)

    private static String htmlTable(int rows) {
        StringBuilder sb = new StringBuilder("<table class=\"labelingTable\"><tbody>");
        for (int i = 0; i < rows; i++) {
            sb.append("<tr><td>ing</td><td>10%</td></tr>");
        }
        sb.append("</tbody></table>");
        return sb.toString();
    }

    private static int count(String haystack, String needle) {
        int total = 0;
        int idx = haystack.indexOf(needle);
        while (idx != -1) {
            total++;
            idx = haystack.indexOf(needle, idx + needle.length());
        }
        return total;
    }

    @Test
    public void testElipseHtmlShortValueUnchanged() {
        String html = htmlTable(2);
        assertEquals(html, LargeTextHelper.elipseHtml(html, LargeTextHelper.TEXT_SIZE_LIMIT));
    }

    @Test
    public void testElipseHtmlNullUnchanged() {
        assertEquals(null, LargeTextHelper.elipseHtml(null, 100));
    }

    @Test
    public void testElipseHtmlTruncatedOnRowBoundaryAndClosed() {
        String html = htmlTable(500);
        String truncated = LargeTextHelper.elipseHtml(html, 200);

        assertTrue("Output must stay within budget + closing tag", truncated.length() <= 200 + "</table>".length());
        assertTrue("Must end on a complete row + closing table", truncated.endsWith("</tr></table>"));
        assertTrue("No tag must be cut mid-way", truncated.lastIndexOf('<') <= truncated.lastIndexOf('>'));
        assertEquals(count(truncated, "<tr>"), count(truncated, "</tr>"));
        assertEquals(1, count(truncated, "<table"));
        assertEquals(1, count(truncated, "</table>"));
    }

    @Test
    public void testElipseHtmlNonHtmlFallsBackToPlainElipse() {
        String plain = repeat('a', 500);
        String truncated = LargeTextHelper.elipseHtml(plain, 100);
        assertTrue(truncated.endsWith("..."));
        assertEquals(103, truncated.length());
    }

    /**
     * Ticket #35900: an over-sized labeling table must be cut on a complete row so the rendered
     * table stays readable, instead of being broken mid-tag.
     */
    @Test
    public void testElipseMLTextKeepsLabelingTableValid() {
        MLText mlText = new MLText();
        mlText.put(Locale.FRENCH, htmlTable(1500));
        mlText.put(Locale.ENGLISH, htmlTable(1500));

        MLText result = LargeTextHelper.elipse(mlText);

        for (Locale locale : new Locale[] { Locale.FRENCH, Locale.ENGLISH }) {
            String value = result.get(locale);
            assertTrue("Value must have been truncated for " + locale, value.length() < htmlTable(1500).length());
            assertTrue("Table must stay closed for " + locale, value.endsWith("</tr></table>"));
            assertEquals(count(value, "<tr>"), count(value, "</tr>"));
        }
    }
}
