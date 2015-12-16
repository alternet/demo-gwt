package com.github.alternet.demo.countries.client;

import java.util.Iterator;

import com.github.alternet.demo.countries.generator.LatinMapperGenerator;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

/**
 * Map diacritics and ligatures to latin characters.
 *
 * @author Philippe Poulard
 */
public abstract class LatinMapper {

    /**
     * Remove diacritics and ligatures of a string.
     *
     * @param string The string to latinize.
     *
     * @return A new latin string.
     */
    public static String latinize(String string) {
        StringBuilder result = new StringBuilder();
        for (int codePoint : unicodeCodePoints(string)) {
            String alt = new String(Character.toChars(codePoint));
            JSONValue val = MAP.get(alt);
            if (val != null) {
                alt = val.isString().stringValue();
            }
            result.append(alt);
        }
        return result.toString();
    }

    private static JSONObject MAP;

    static {
        LatinMapper mapper = GWT.create(LatinMapper.class);
        MAP = mapper.map().isObject();
    }

    /**
     * The default implementation is just a template
     * and will be replaced by the generator that create
     * a child class with the complete mapping.
     *
     * @return The map of characters to replace.
     *
     * @see LatinMapperGenerator
     */
    protected native JSONValue map() /*-{
        var o = {
              'Á': 'A', // LATIN CAPITAL LETTER A WITH ACUTE
              'Ă': 'A', // LATIN CAPITAL LETTER A WITH BREVE
              // ...
              'ᵥ': 'v', // LATIN SUBSCRIPT SMALL LETTER V
              'ₓ': 'x', // LATIN SUBSCRIPT SMALL LETTER X
        };
        return @com.google.gwt.json.client.JSONObject::new(Lcom/google/gwt/core/client/JavaScriptObject;)(o);
    }-*/;

    /**
     * Iterate on the unicode code points of a string (a code point is made of 1
     * or 2 chars).
     *
     * @param string
     *            The actual non-null string.
     * @return An iterator on its unicode code points.
     */
    private static Iterable<Integer> unicodeCodePoints(final String string) {
        return new Iterable<Integer>() {
            String text = string;
            public Iterator<Integer> iterator() {
                return new Iterator<Integer>() {
                    int nextIndex = 0;

                    public boolean hasNext() {
                        return nextIndex < text.length();
                    }

                    public Integer next() {
                        int result = text.codePointAt(nextIndex);
                        nextIndex += Character.charCount(result);
                        return result;
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

}
