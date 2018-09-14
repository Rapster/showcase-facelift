package org.primefaces.showcase.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.component.UIPanel;
import javax.faces.context.FacesContext;

import org.apache.commons.lang3.BooleanUtils;
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

    public static final List<FileContent> getFilesContent(FacesContext fc, UIComponent component, String fullPathToFile) {
        CacheProvider provider = PrimeApplicationContext.getCurrentInstance(fc).getCacheProvider();
        List<FileContent> files = (List<FileContent>) provider.get("contents", fullPathToFile);

        if (files == null) {
            FileContent srcContent = getFileContent(fullPathToFile, true);
            UIComponent tabs = component.getFacet("static-tabs");
            attach(tabs, srcContent);
            files = new ArrayList<>();
            flatFileContent(srcContent, files);

            provider.put("contents", fullPathToFile, files);
        }
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

    private static final void attach(UIComponent component, FileContent file) {
        if (component instanceof Tab && component.isRendered()) {
            Boolean flatten = BooleanUtils.toBooleanDefaultIfNull(
                    Boolean.valueOf((String) component.getAttributes().get("flatten")),
                    false);
            FileContent content = getFileContent(((Tab) component).getTitle(), flatten);
            file.getAttached().add(content);
        }
        else if (component instanceof UIPanel) {
            for (UIComponent child : component.getChildren()) {
                attach((Tab) child, file);
            }
        }
    }

    private static final void flatFileContent(FileContent source, List<FileContent> dest) {
        dest.add(new FileContent(source.getTitle(), source.getValue(), source.getType(), Collections.<FileContent>emptyList()));

        for(FileContent file : source.getAttached()) {
            flatFileContent(file, dest);
        }
    }
}