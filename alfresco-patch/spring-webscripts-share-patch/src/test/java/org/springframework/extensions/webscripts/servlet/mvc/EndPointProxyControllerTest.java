package org.springframework.extensions.webscripts.servlet.mvc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for the uri encoding of the patched {@link EndPointProxyController}.
 */
public class EndPointProxyControllerTest {

    @Test
    public void encodesEmojiAsSingleUtf8Sequence() {
        assertEquals("alfresco/slingshot/node/content/test%20%f0%9f%a7%bc.pdf",
                EndPointProxyController.encodeUri("alfresco/slingshot/node/content/test 🧼.pdf"));
    }

    @Test
    public void encodesAccentedCharactersInUtf8() {
        assertEquals("fiche%20%c3%a9t%c3%a9.pdf", EndPointProxyController.encodeUri("fiche été.pdf"));
    }

    @Test
    public void keepsReservedCharactersAndEncodesSingleQuote() {
        assertEquals("api/node;a,b?c=d&e+f/d%27info", EndPointProxyController.encodeUri("api/node;a,b?c=d&e+f/d'info"));
    }
}
