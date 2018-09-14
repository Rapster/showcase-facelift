package org.primefaces.showcase.util;

/**
 * FileContentSettings
 *
 * @author Sébastien Lepage / last modified by $Author$
 * @version $Revision$
 * @since 6.3
 */
public class FileContentSettings {
    private String[] startMarkers = null;
    private String[] endMarkers = null;
    private String[] excludeMarkers = null;
    private boolean showLineWithMarker = false;
    private boolean includeMarker = false;
    private String type;

    public String[] getStartMarkers() {
        if (startMarkers == null) return new String[0];
        return startMarkers;
    }

    public FileContentSettings setStartMarkers(String... startMarkers) {
        this.startMarkers = startMarkers;
        return this;
    }

    public String[] getExcludeMarkers() {
        if (excludeMarkers == null) return new String[0];
        return excludeMarkers;
    }

    public FileContentSettings setExcludeMarkers(String... excludeMarkers) {
        this.excludeMarkers = excludeMarkers;
        return this;
    }

    public String[] getEndMarkers() {
        if (endMarkers == null) {
            return new String[0];
        }
        return endMarkers;
    }

    public FileContentSettings setEndMarkers(String... endMarkers) {
        this.endMarkers = endMarkers;
        return this;
    }

    public boolean isShowLineWithMarker() {
        return showLineWithMarker;
    }

    public FileContentSettings setShowLineWithMarker(boolean showLineWithMarker) {
        this.showLineWithMarker = showLineWithMarker;
        return this;
    }

    public String getType() {
        return type;
    }

    public FileContentSettings setType(String type) {
        this.type = type;
        return this;
    }

    public boolean isIncludeMarker() {
        return includeMarker;
    }

    public FileContentSettings setIncludeMarker(boolean includeMarker) {
        this.includeMarker = includeMarker;
        return this;
    }
}
