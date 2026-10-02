package com.sbv.linkdroid;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class UtilsTest {
    @Test
    public void normalizeBaseUrl_keepsCleanUrl() {
        assertEquals("https://linkwarden.example.com",
                Utils.normalizeBaseUrl("https://linkwarden.example.com"));
    }

    @Test
    public void normalizeBaseUrl_removesTrailingSlashes() {
        assertEquals("https://linkwarden.example.com",
                Utils.normalizeBaseUrl("https://linkwarden.example.com/"));
        assertEquals("http://192.168.0.15:4000",
                Utils.normalizeBaseUrl("http://192.168.0.15:4000//"));
    }

    @Test
    public void normalizeBaseUrl_keepsSubPath() {
        assertEquals("https://example.com/linkwarden",
                Utils.normalizeBaseUrl("https://example.com/linkwarden/"));
    }

    @Test
    public void normalizeBaseUrl_removesWhitespace() {
        assertEquals("https://linkwarden.example.com",
                Utils.normalizeBaseUrl("  https://linkwarden.example.com/ \n"));
    }

    @Test
    public void normalizeBaseUrl_handlesNull() {
        assertEquals("", Utils.normalizeBaseUrl(null));
    }

    @Test
    public void normalizeToken_removesWhitespace() {
        assertEquals("abc.def-123", Utils.normalizeToken(" abc.def-123 \n"));
    }

    @Test
    public void normalizeToken_handlesNull() {
        assertEquals("", Utils.normalizeToken(null));
    }

    @Test
    public void normalizeToken_whitespaceOnlyGivesEmpty() {
        assertEquals("", Utils.normalizeToken("  \n"));
    }
}
