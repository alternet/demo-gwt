package com.github.alternet.demo.countries.generator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.alternet.demo.countries.client.LatinMapper;
import com.google.gwt.core.ext.Generator;
import com.google.gwt.core.ext.GeneratorContext;
import com.google.gwt.core.ext.TreeLogger;
import com.google.gwt.core.ext.UnableToCompleteException;
import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.rebind.ClassSourceFileComposerFactory;
import com.google.gwt.user.rebind.SourceWriter;
import com.ibm.icu.lang.UCharacter;

/**
 * Generates a mapping table for decomposing diacritics and ligatures.
 *
 * The Unicode format defines a normalization table for decomposing accented characters,
 * however it doesn't decompose some ligatures (AE / OE), so instead the Unicode names
 * are used to generate a table of mappings.
 *
 * @see http://semplicewebsites.com/removing-accents-javascript
 *
 * @author Philippe Poulard
 */
public class LatinMapperGenerator extends Generator {

    @Override
    public String generate(TreeLogger logger, GeneratorContext context,
            String typeName) throws UnableToCompleteException {
        JClassType classType;
        ClassSourceFileComposerFactory composer = null;

        try {
            classType = context.getTypeOracle().getType(typeName);

            String packageName = classType.getPackage().getName();
            String simpleName = classType.getSimpleSourceName() + "Generated";
            composer = new ClassSourceFileComposerFactory(packageName, simpleName);
            composer.setSuperclass(LatinMapper.class.getCanonicalName());
            composer.addImport(JSONValue.class.getCanonicalName());
            PrintWriter printWriter = context.tryCreate(logger, packageName, simpleName);
            if (printWriter == null) {
                // already generated
                return composer.getCreatedClassName();
            }
            SourceWriter src = composer.createSourceWriter(context, printWriter);

//
//            protected native JSONValue map() /*-{
//                var o = {
//                    'Á': 'A', // LATIN CAPITAL LETTER A WITH ACUTE
//                    'Ă': 'A', // LATIN CAPITAL LETTER A WITH BREVE
//                    ...
//                    'ᵥ': 'v', // LATIN SUBSCRIPT SMALL LETTER V
//                    'ₓ': 'x', // LATIN SUBSCRIPT SMALL LETTER X
//                };
//                return @com.google.gwt.json.client.JSONObject::new(Lcom/google/gwt/core/client/JavaScriptObject;)(o);
//            }-*/;
//

            src.println("    protected native JSONValue map() /*-{");
            src.println("        var o = {");

            String name = null;
            BufferedReader names = unicodeLatin();
            while ((name = names.readLine()) != null) {
                int codePoint = UCharacter.getCharFromName(name);
                if (codePoint == -1) {
                    System.err.println("CANNOT FIND CHAR FOR " + name);
                } else {
                    String replace = "";
                    Matcher m = p1.matcher(name);
                    if (m.matches()) {
                        replace = ("CAPITAL".equals(m.group(1))) ?
                            m.group(2).toUpperCase() :
                            m.group(2).toLowerCase();
                    } else {
                        m = p2.matcher(name);
                        if (m.matches()) {
                            replace = ("CAPITAL".equals(m.group(1))) ?
                                    m.group(2).toUpperCase() :
                                    m.group(2).toLowerCase();
                        } else {
                            m = p3.matcher(name);
                            if (m.matches()) {
                                replace = m.group(1);
                            } else {
                                System.err.println("CANNOT FIND APPROXIMATION FOR " + name);
                            }
                        }
                    }
                    String orig = new String(Character.toChars(codePoint));
                    if (replace.length() > 0 && ! orig.equals(replace))
                    {
                        // '\\u{00C1}': 'A', // LATIN CAPITAL LETTER A WITH ACUTE
                        src.println("'" + orig + "': '" + replace + "', // " + name);
                    }
                }
            }
            src.println("\n        };");
            src.println("        return @com.google.gwt.json.client.JSONObject::new(Lcom/google/gwt/core/client/JavaScriptObject;)(o);");
            src.println("    }-*/;");

            src.commit(logger);

            System.out.println("Generating for: " + typeName);
            return typeName + "Generated";

        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return composer.getCreatedClassName();
    }

    Pattern p1 = Pattern.compile("^LATIN (?:SUBSCRIPT )?(CAPITAL|SMALL) LETTER (?:(?:BARRED|DOTLESS|INSULAR|INVERTED|LONG|OPEN|REVERSED|SIDEWAYS|SCRIPT|TURNED) )?([A-Z][A-Za-z]?)( WITH.*|DIGRAPH.*)?$");
    Pattern p2 = Pattern.compile("^LATIN (CAPITAL|SMALL) LIGATURE ([A-Z]+)$");
    Pattern p3 = Pattern.compile("^LATIN LETTER SMALL CAPITAL (?:(?:BARRED|DOTLESS|INSULAR|INVERTED|LONG|OPEN|REVERSED|SIDEWAYS|SCRIPT|TURNED) )?([A-Z][A-Z]?)( WITH.*)?$");

    BufferedReader unicodeLatin() throws IOException {
        return new BufferedReader(
            new InputStreamReader(
                LatinMapperGenerator.class.getResource("unicode-latin.txt").openStream(),
                Charset.forName("UTF-8")
            )
        );
    }

}
