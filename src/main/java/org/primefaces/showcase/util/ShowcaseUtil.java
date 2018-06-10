/*
 * Copyright 2011-2015 PrimeFaces Extensions
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * $Id$
 */

package org.primefaces.showcase.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.faces.context.FacesContext;

import org.omnifaces.util.Faces;

/**
 * ShowcaseUtil
 *
 * @author Pavol Slany / last modified by $Author$
 * @version $Revision$
 */
public class ShowcaseUtil {

    public static FileContent getFileContent(final String fullPathToFile, Boolean readBeans) {
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

    public static final List<FileContent> getCurrentFlatFilesContent() {
        return getFlatFilesContent(Faces.getRequestServletPath());
    }

    public static final List<FileContent> getFlatFilesContent(String fullPathToFile) {
        FileContent content = getFileContent(fullPathToFile, true);
        List<FileContent> flatFiles = new ArrayList<>();

        if (content != null) {
            flatFiles.add(new FileContent(content.getTitle(), content.getValue(), content.getType(), Collections.<FileContent>emptyList()));
            for (FileContent c : content.getAttached()) {
                flatFiles.add(c);
            }
        }
        return flatFiles;
    }
}