package org.primefaces.showcase.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.BooleanUtils;
import org.omnifaces.util.Components;
import org.omnifaces.util.Faces;
import org.primefaces.cache.CacheProvider;
import org.primefaces.component.tabview.Tab;
import org.primefaces.context.PrimeApplicationContext;

/**
 * ShowcaseUtil
 *
 * @author Sébastien Lepage / last modified by $Author$
 * @version $Revision$
 * @since 6.3
 */
public class ShowcaseUtil {

    public static final List<FileContent> getFilesContent(String fullPath, Boolean readBeans) {
        CacheProvider provider = PrimeApplicationContext.getCurrentInstance(Faces.getContext()).getCacheProvider();
        List<FileContent> files = (List<FileContent>) provider.get("contents", fullPath);

        if (files == null) {
            FileContent srcContent = getFileContent(fullPath, readBeans);
            UIComponent tabs = Components.getCurrentComponent().getFacet("static-tabs");
            if (tabs != null) {
                attach(tabs, srcContent);
            }
            files = new ArrayList<>();
            flatFileContent(srcContent, files);

            provider.put("contents", fullPath, files);
        }

        return files;
    }

    // EXCLUDE-SOURCE-START
    private static final FileContent getFileContent(String fullPath, Boolean readBeans) {
        try {
            // Finding in WEB ...
            FacesContext fc = FacesContext.getCurrentInstance();
            InputStream is = fc.getExternalContext().getResourceAsStream(fullPath);
            if (is != null) {
                return FileContentMarkerUtil.readFileContent(fullPath, is, readBeans);
            }

            // Finding in ClassPath ...
            is = ShowcaseUtil.class.getResourceAsStream(fullPath);
            if (is != null) {
                return FileContentMarkerUtil.readFileContent(fullPath, is, readBeans);
            }
        } catch (Exception e) {
            throw new IllegalStateException("Internal error: file " + fullPath + " could not be read", e);
        }

        return null;
    }

    private static final void attach(UIComponent component, FileContent file) {
        if (component.isRendered()) {
            if (component instanceof Tab) {
                Boolean flatten = BooleanUtils.toBooleanDefaultIfNull(
                        Boolean.valueOf((String) component.getAttributes().get("flatten")),
                        false);
                FileContent content = getFileContent(((Tab) component).getTitle(), flatten);
                file.getAttached().add(content);
            }
            else if (component instanceof UIPanel) {
                for (UIComponent child : component.getChildren()) {
                    attach(child, file);
                }
            }
        }
    }

    private static final void flatFileContent(FileContent source, List<FileContent> dest) {
        dest.add(new FileContent(source.getTitle(), source.getValue(), source.getType(), Collections.<FileContent>emptyList()));

        for(FileContent file : source.getAttached()) {
            flatFileContent(file, dest);
        }
    }
    // EXCLUDE-SOURCE-END
}