package fr.becpg.repo.helper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Encodes a value, typically a file name, as a URL path segment or an RFC 5987 header parameter.
 *
 * Unlike {@code org.springframework.extensions.surf.util.URLEncoder}, which encodes each UTF-16 char on its
 * own and therefore breaks characters outside the BMP (emojis) into two invalid sequences, this encoder
 * works on code points and always produces valid UTF-8 percent-encoding.
 *
 * @author valentin
 */
public final class UrlPathEncoder {

    private static final String FORM_ENCODED_SPACE = "+";

    private static final String PERCENT_ENCODED_SPACE = "%20";

    private UrlPathEncoder() {
    }

    /**
     * Percent-encodes a value in UTF-8, spaces included as {@code %20}.
     *
     * @param value the value to encode
     * @return the encoded value
     */
    public static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8).replace(FORM_ENCODED_SPACE, PERCENT_ENCODED_SPACE);
    }
}
