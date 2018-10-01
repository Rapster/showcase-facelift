package org.primefaces.showcase.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.omnifaces.util.Faces;

/**
 * FileContentMarkerUtil
 *
 * @author Sébastien Lepage / last modified by $Author$
 * @version $Revision$
 * @since 6.3
 */
public class FileContentMarkerUtil {

    private static FileContentSettings javaFileSettings = new FileContentSettings()
            .setType("java")
            .setStartMarkers("@ManagedBean", "@RequestScoped", "@ViewScoped", "@SessionScoped", "@FacesConverter", "@Target", " class ", " enum ")
            .setIncludeMarker(true);

    private static FileContentSettings xhtmlFileSettings = new FileContentSettings()
            .setType("xml")
            .setStartMarkers("EXAMPLE-SOURCE-START", "<ui:define name=\"implementation\">", "<ui:define name=\"head\">")
            .setEndMarkers("EXAMPLE-SOURCE-END", "</ui:define>")
            .setIncludeMarker(false);

    public static FileContent readFileContent(String fullPathToFile, InputStream is, boolean readBeans) {
        try {
            String fileName = StringUtils.substringAfterLast(fullPathToFile, "/");
            if (fullPathToFile.endsWith(".java")) {
                return readFileContent(fileName, is, javaFileSettings, readBeans);
            }

            if (fullPathToFile.endsWith(".xhtml")) {
                return readFileContent(fileName, is, xhtmlFileSettings, readBeans);
            }

            throw new UnsupportedOperationException();
        } catch (Exception e) {
            throw new IllegalStateException("Internal error: file " + fullPathToFile + " could not be read", e);
        }
    }

    private static FileContent readFileContent(String fileName, InputStream inputStream, FileContentSettings settings, boolean readBeans) throws IOException {
        StringBuilder content = new StringBuilder();
        List<FileContent> javaFiles = new ArrayList<>();

        try(BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            boolean started = false;

            while ((line = br.readLine()) != null) {
                if (!started) {
                    started = containMarker(line, settings.getStartMarkers());
                    if (!started || !settings.isIncludeMarker()) {
                        continue;
                    }
                }

                // if is before first end marker
                if (started && containMarker(line, settings.getEndMarkers())) {
                    started = false;
                    continue;
                }

                if (readBeans && line.contains("#{")) {
                    Matcher m = Pattern.compile("#\\{\\w*?\\s?(\\w+)[\\.\\[].*\\}").matcher(line.trim());
                    while(m.find()) {
                        String group = m.group(1);
                        Object bean = Faces.evaluateExpressionGet("#{" + group + "}");
                        if (bean != null && !ClassUtils.isPrimitiveOrWrapper(bean.getClass())) {
                            String javaFileName = StringUtils.substringAfterLast(bean.getClass().getName(), ".") + ".java";
                            if (!javaFiles.contains(new FileContent(javaFileName, null, null, null))) {
                                String path = "/" + StringUtils.replaceAll(bean.getClass().getName(), "\\.", "/") + ".java";
                                FileContent javaContent = readFileContent(javaFileName,
                                                                          FileContentMarkerUtil.class.getResourceAsStream(path),
                                                                          javaFileSettings,
                                                                          false);
                                javaFiles.add(javaContent);
                            }
                        }
                    }
                }

                content.append("\n");
                content.append(line);
            }
        }

        return new FileContent(fileName, content.toString().trim(), settings.getType(), javaFiles);
    }

    private static boolean containMarker(String line, String[] markers) {
        for (String marker : markers) {
            if (line.contains(marker)) {
                return true;
            }
        }

        return false;
    }
}
