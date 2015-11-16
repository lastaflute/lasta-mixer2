/*
 * Copyright 2014-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.lastaflute.mixer2.view.typical;

import java.util.regex.Pattern;

import org.lastaflute.mixer2.view.Mixer2Supporter;
import org.mixer2.jaxb.xhtml.Html;
import org.mixer2.xhtml.PathAdjuster;

/**
 * @author jflute
 */
public class TypicalMixStyleResolver {

    protected static final Pattern CSS_PATTERN = Pattern.compile("^\\.+/.*css/(.*)$");

    public void resolveStyle(Html html, Mixer2Supporter supporter) {
        if (html == null) {
            throw new IllegalArgumentException("The argument 'html' should not be null.");
        }
        if (supporter == null) {
            throw new IllegalArgumentException("The supporter 'html' should not be null.");
        }
        replaceCssPath(html, supporter);
    }

    protected void replaceCssPath(Html html, Mixer2Supporter supporter) {
        PathAdjuster.replacePath(html, CSS_PATTERN, supporter.getRequestManager().getContextPath() + "/css/$1");
    }
}
