package fr.becpg.test.repo.helper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

        LargeTextHelper.elipse(mlText);
        
        assertTrue(mlText.get(Locale.ENGLISH).length() <= LargeTextHelper.TEXT_SIZE_LIMIT / mlText.size() - 20
               );
        assertTrue(mlText.get(Locale.FRENCH).length() <= LargeTextHelper.TEXT_SIZE_LIMIT / mlText.size() - 20
                );
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
}
