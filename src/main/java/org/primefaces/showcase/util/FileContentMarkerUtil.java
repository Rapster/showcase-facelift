package org.primefaces.showcase.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final FileContentSettings javaFileSettings = new FileContentSettings()
            .setType("java")
            .setStartMarkers(
                    Marker.of("@ManagedBean"),
                    Marker.of("@RequestScoped"),
                    Marker.of("@ViewScoped"),
                    Marker.of("@SessionScoped"),
                    Marker.of("@FacesConverter"),
                    Marker.of("@Target"),
                    Marker.of(" class "),
                    Marker.of(" enum "),
                    Marker.of("EXCLUDE-SOURCE-END").excluded())
            .setEndMarkers(Marker.of("EXCLUDE-SOURCE-START").excluded());

    private static final FileContentSettings xhtmlFileSettings = new FileContentSettings()
            .setType("xml")
            .setStartMarkers(
                    Marker.of("EXAMPLE-SOURCE-START").excluded(),
                    Marker.of("<ui:define name=\"implementation\">").excluded(),
                    Marker.of("<ui:define name=\"head\">").excluded())
            .setEndMarkers(
                    Marker.of("EXAMPLE-SOURCE-END").excluded(),
                    Marker.of("</ui:define>").excluded());


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
        }
        catch (Exception e) {
            throw new IllegalStateException("Internal error: file " + fullPathToFile + " could not be read", e);
        }
    }

    private static FileContent readFileContent(String fileName, InputStream inputStream, FileContentSettings settings, boolean readBeans) throws Exception {
        StringBuilder content = new StringBuilder();
        List<FileContent> javaFiles = new ArrayList<>();

        try (InputStreamReader ir = new InputStreamReader(inputStream);
             BufferedReader br = new BufferedReader(ir)) {
            String line;
            boolean started = false;

            while ((line = br.readLine()) != null) {
                if (!started) {
                    Marker marker = getMatchingMarker(line, settings.getStartMarkers());
                    started = marker != null;
                    if (!started || marker.isExcluded()) {
                        continue;
                    }
                }

                // if is before first end marker
                if (started && getMatchingMarker(line, settings.getEndMarkers()) != null) {
                    started = false;
                    content.append("\n");
                    continue;
                }

                content.append("\n");
                content.append(line);

                if (readBeans && line.contains("#{")) {
                    Matcher m = Pattern.compile("#\\{\\w*?\\s?(\\w+)[\\.\\[].*\\}").matcher(line.trim());
                    while (m.find()) {
                        String group = m.group(1);
                        Object bean = Faces.evaluateExpressionGet("#{" + group + "}");
                        if (bean != null && !bean.getClass().getName().startsWith("java")) {
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
            }
        }

        return new FileContent(fileName, content.toString().trim(), settings.getType(), javaFiles);
    }

    private static Marker getMatchingMarker(String line, Marker[] markers) {
        for (Marker marker : markers) {
            if (line.contains(marker.getName())) {
                return marker;
            }
        }

        return null;
    }
}
