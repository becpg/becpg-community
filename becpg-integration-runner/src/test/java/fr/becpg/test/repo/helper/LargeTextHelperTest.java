package fr.becpg.test.repo.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Locale;

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
        String plain = "a".repeat(500);
        String truncated = LargeTextHelper.elipseHtml(plain, 100);
        assertTrue(truncated.endsWith("..."));
        assertEquals(103, truncated.length());
    }
}
