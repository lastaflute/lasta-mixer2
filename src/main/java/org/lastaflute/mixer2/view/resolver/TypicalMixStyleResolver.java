/*
 * Copyright 2015-2016 the original author or authors.
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
package org.lastaflute.mixer2.view.resolver;

import java.util.regex.Pattern;

import org.lastaflute.mixer2.view.Mixer2Supporter;
import org.mixer2.jaxb.xhtml.Html;
import org.mixer2.xhtml.PathAdjuster;

/**
 * @author jflute
 */
public class TypicalMixStyleResolver {

    protected static final Pattern CSS_PATH_PATTERN = Pattern.compile("^\\.+/.*css/(.*)$");
    protected static final Pattern CSS_SUFFIX_PATTERN = Pattern.compile("\\.css$");
    protected static final Pattern JS_PATH_PATTERN = Pattern.compile("^\\.+/.*js/(.*)$");
    protected static final Pattern JS_SUFFIX_PATTERN = Pattern.compile("\\.js$");
    protected static final long VQ = System.currentTimeMillis();

    protected boolean versionQueryUsed;

    public TypicalMixStyleResolver useVersionQuery() {
        versionQueryUsed = true;
        return this;
    }

    public void resolveStyle(Html html, Mixer2Supporter supporter) {
        if (html == null) {
            throw new IllegalArgumentException("The argument 'html' should not be null.");
        }
        if (supporter == null) {
            throw new IllegalArgumentException("The supporter 'html' should not be null.");
        }
        adjustCssRelativePath(html, supporter);
        if (versionQueryUsed) {
            adjustCssVersionQuery(html, supporter);
        }
        adjustJsRelativePath(html, supporter);
        if (versionQueryUsed) {
            adjustJsVersionQuery(html, supporter);
        }
    }

    protected void adjustCssRelativePath(Html html, Mixer2Supporter supporter) {
        PathAdjuster.replacePath(html, CSS_PATH_PATTERN, supporter.getRequestManager().getContextPath() + "/css/$1");
    }

    protected void adjustCssVersionQuery(Html html, Mixer2Supporter supporter) {
        PathAdjuster.replacePath(html, CSS_SUFFIX_PATTERN, ".css?v=" + VQ);
    }

    protected void adjustJsRelativePath(Html html, Mixer2Supporter supporter) {
        PathAdjuster.replacePath(html, JS_PATH_PATTERN, supporter.getRequestManager().getContextPath() + "/js/$1");
    }

    protected void adjustJsVersionQuery(Html html, Mixer2Supporter supporter) {
        PathAdjuster.replacePath(html, JS_SUFFIX_PATTERN, ".js?v=" + VQ);
    }
}
