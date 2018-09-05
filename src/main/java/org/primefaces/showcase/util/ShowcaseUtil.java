package org.primefaces.showcase.util;

import org.apache.commons.lang3.BooleanUtils;
import org.primefaces.component.tabview.Tab;

import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.context.FacesContext;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ShowcaseUtil
 *
 * @author Sébastien Lepage / last modified by $Author$
 * @version $Revision$
 * @since 6.3
 */
public class ShowcaseUtil {

    public static final List<FileContent> getFilesContent(UIComponent component, String fullPathToFile) {
        FileContent srcContent = getFileContent(fullPathToFile, true);
        UIComponent tabs = component.getFacet("static-tabs");
        if (tabs instanceof Tab) {
            attach((Tab) tabs, srcContent);
        } else if(tabs instanceof UIPanel) {
            for(UIComponent child : tabs.getChildren()) {
                if (child instanceof Tab) {
                    attach((Tab) child, srcContent);
                }
            }
        }

        List<FileContent> files = new ArrayList<>();
        flatFileContent(srcContent, files);
        return files;
    }

    public static final FileContent getFileContent(String fullPathToFile, Boolean readBeans) {
        try {
            // Finding in WEB ...
            FacesContext fc = FacesContext.getCurrentInstance();
            InputStream is = fc.getExternalContext().getResourceAsStream(fullPathToFile);
            if (is != null) {
                return FileContentMarkerUtil.readFileContent(fullPathToFile, is, readBeans);
            }

            // Finding in ClassPath ...
            is = ShowcaseUtil.class.getResourceAsStream(fullPathToFile);
            if (is != null) {
                return FileContentMarkerUtil.readFileContent(fullPathToFile, is, readBeans);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Internal error: file " + fullPathToFile + " could not be read", e);
        }

        return null;
    }

    private static final void attach(Tab tab, FileContent file) {
        if (tab.isRendered()) {
            Boolean flatten = BooleanUtils.toBooleanDefaultIfNull(
                    Boolean.valueOf((String) tab.getAttributes().get("flatten")),
                    false);
            FileContent content = getFileContent(tab.getTitle(), flatten);
            file.getAttached().add(content);
        }
    }

    private static final void flatFileContent(FileContent source, List<FileContent> dest) {
        dest.add(new FileContent(source.getTitle(), source.getValue(), source.getType(), Collections.<FileContent>emptyList()));

        for(FileContent file : source.getAttached()) {
            flatFileContent(file, dest);
        }
    }
}