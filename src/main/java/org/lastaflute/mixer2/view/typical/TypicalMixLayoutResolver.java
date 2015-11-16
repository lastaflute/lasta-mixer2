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

import org.lastaflute.mixer2.view.Mixer2Supporter;
import org.mixer2.jaxb.xhtml.Footer;
import org.mixer2.jaxb.xhtml.Header;
import org.mixer2.jaxb.xhtml.Html;

/**
 * @author jflute
 */
public class TypicalMixLayoutResolver {

    public void resolveLayout(Html html, Mixer2Supporter supporter) {
        if (html == null) {
            throw new IllegalArgumentException("The argument 'html' should not be null.");
        }
        if (supporter == null) {
            throw new IllegalArgumentException("The supporter 'html' should not be null.");
        }
        supporter.loadHtml(getLayoutHtmlPath()).alwaysPresent(loaded -> {
            replaceHeader(html, supporter, loaded);
            replaceFooter(html, supporter, loaded);
        });
    }

    protected String getLayoutHtmlPath() {
        return "/common/mix_layout.html"; // #pending rename to layout.html
    }

    protected void replaceHeader(Html html, Mixer2Supporter supporter, Html loaded) {
        final Header header = supporter.getById(loaded, "header", Header.class).get();
        supporter.resolveUrlLink(header);
        asYouLikeHeader(header, supporter);
        supporter.replaceById(html, "header", header);
    }

    protected void asYouLikeHeader(Header header, Mixer2Supporter supporter) { // overridden by application
    }

    protected void replaceFooter(Html html, Mixer2Supporter supporter, Html loaded) {
        final Footer footer = supporter.getById(loaded, "footer", Footer.class).get();
        supporter.resolveUrlLink(footer);
        asYouLikeFooter(footer, supporter);
        supporter.replaceById(html, "footer", footer);
    }

    protected void asYouLikeFooter(Footer header, Mixer2Supporter supporter) { // overridden by application
    }
}
