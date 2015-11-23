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
package org.lastaflute.mixer2.view.resolver;

import org.lastaflute.mixer2.view.Mixer2Supporter;
import org.mixer2.jaxb.xhtml.Footer;
import org.mixer2.jaxb.xhtml.Header;
import org.mixer2.jaxb.xhtml.Html;

/**
 * @author jflute
 */
public class TypicalMixLayoutResolver {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    protected static final String DEFAULT_HEADER_ID = "header";
    protected static final String DEFAULT_FOOTER_ID = "footer";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected String headerId;
    protected String footerId;
    protected LayoutHeaderResolver headerResolver;
    protected LayoutFooterResolver footerResolver;
    protected LayoutPartsResolver partsResolver;
    protected boolean suppressedHeader;
    protected boolean suppressedFooter;

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    // -----------------------------------------------------
    //                                                   ID
    //                                                  ----
    public TypicalMixLayoutResolver headerId(String headerId) {
        this.headerId = headerId;
        return this;
    }

    public TypicalMixLayoutResolver footerId(String footerId) {
        this.footerId = footerId;
        return this;
    }

    // -----------------------------------------------------
    //                                              Resolver
    //                                              --------
    public TypicalMixLayoutResolver resolveHeader(LayoutHeaderResolver headerResolver) {
        this.headerResolver = headerResolver;
        return this;
    }

    public static interface LayoutHeaderResolver {

        void resolve(Header header, Mixer2Supporter supporter);
    }

    public TypicalMixLayoutResolver resolveFooter(LayoutFooterResolver footerResolver) {
        this.footerResolver = footerResolver;
        return this;
    }

    public static interface LayoutFooterResolver {

        void resolve(Footer footer, Mixer2Supporter supporter);
    }

    public TypicalMixLayoutResolver resolveParts(LayoutPartsResolver partsHandler) {
        this.partsResolver = partsHandler;
        return this;
    }

    public static interface LayoutPartsResolver {

        void resolve(Html html, Mixer2Supporter supporter, Html loaded);
    }

    // -----------------------------------------------------
    //                                              Suppress
    //                                              --------
    public TypicalMixLayoutResolver suppressHeader(boolean headerSuppressed) {
        this.suppressedHeader = headerSuppressed;
        return this;
    }

    public TypicalMixLayoutResolver suppressFooter(boolean footerSuppressed) {
        this.suppressedFooter = footerSuppressed;
        return this;
    }

    // ===================================================================================
    //                                                                            Resolver
    //                                                                            ========
    public void resolveLayout(Html html, Mixer2Supporter supporter) {
        if (html == null) {
            throw new IllegalArgumentException("The argument 'html' should not be null.");
        }
        if (supporter == null) {
            throw new IllegalArgumentException("The supporter 'html' should not be null.");
        }
        // TODO jflute mixer2: resolve head? merge? (2015/11/23)
        supporter.loadPartsHtml(getLayoutHtmlPath()).alwaysPresent(loaded -> {
            if (isSuppressReplacingHeader()) {
                replaceHeader(html, supporter, loaded);
            }
            if (isSuppressReplacingFooter()) {
                replaceFooter(html, supporter, loaded);
            }
            asYouLikeIt(html, supporter, loaded);
        });
    }

    protected String getLayoutHtmlPath() {
        return "/common/layout.html";
    }

    protected boolean isSuppressReplacingHeader() {
        return suppressedHeader;
    }

    protected boolean isSuppressReplacingFooter() {
        return suppressedFooter;
    }

    protected void asYouLikeIt(Html html, Mixer2Supporter supporter, Html loaded) {
        if (partsResolver != null) {
            partsResolver.resolve(html, supporter, loaded);
        }
    }

    // ===================================================================================
    //                                                                              Header
    //                                                                              ======
    protected void replaceHeader(Html html, Mixer2Supporter supporter, Html loaded) {
        final Header header = supporter.findById(loaded, getHeaderId(), Header.class).get();
        supporter.resolveUrlLink(header);
        asYouLikeHeader(header, supporter);
        supporter.replaceById(html, getHeaderId(), header);
    }

    protected String getHeaderId() {
        return headerId != null ? headerId : DEFAULT_HEADER_ID;
    }

    protected void asYouLikeHeader(Header header, Mixer2Supporter supporter) {
        if (headerResolver != null) {
            headerResolver.resolve(header, supporter);
        }
    }

    // ===================================================================================
    //                                                                              Footer
    //                                                                              ======
    protected void replaceFooter(Html html, Mixer2Supporter supporter, Html loaded) {
        final Footer footer = supporter.findById(loaded, getFooterId(), Footer.class).get();
        supporter.resolveUrlLink(footer);
        asYouLikeFooter(footer, supporter);
        supporter.replaceById(html, getFooterId(), footer);
    }

    protected String getFooterId() {
        return footerId != null ? footerId : DEFAULT_FOOTER_ID;
    }

    protected void asYouLikeFooter(Footer footer, Mixer2Supporter supporter) {
        if (footerResolver != null) {
            footerResolver.resolve(footer, supporter);
        }
    }
}
