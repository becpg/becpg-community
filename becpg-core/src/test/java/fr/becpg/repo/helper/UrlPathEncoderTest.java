package fr.becpg.repo.helper;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for {@link UrlPathEncoder}.
 */
public class UrlPathEncoderTest {

    @Test
    public void encodesEmojiAsSingleUtf8Sequence() {
        assertEquals("test%20%F0%9F%98%80.pdf", UrlPathEncoder.encode("test 😀.pdf"));
    }

    @Test
    public void encodesAccentedCharactersInUtf8() {
        assertEquals("fiche%20%C3%A9t%C3%A9.pdf", UrlPathEncoder.encode("fiche été.pdf"));
    }

    @Test
    public void keepsLiteralPlusDistinctFromSpace() {
        assertEquals("a%2Bb%20c.pdf", UrlPathEncoder.encode("a+b c.pdf"));
    }

    @Test
    public void encodesPathAndQueryDelimiters() {
        assertEquals("a%2Fb%3Bc%3Fd%23e%26f.pdf", UrlPathEncoder.encode("a/b;c?d#e&f.pdf"));
    }
}
