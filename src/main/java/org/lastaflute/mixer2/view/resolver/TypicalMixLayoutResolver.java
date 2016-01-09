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

import java.util.List;

import org.lastaflute.mixer2.view.Mixer2Supporter;
import org.mixer2.jaxb.xhtml.Body;
import org.mixer2.jaxb.xhtml.Footer;
import org.mixer2.jaxb.xhtml.Head;
import org.mixer2.jaxb.xhtml.Header;
import org.mixer2.jaxb.xhtml.Html;
import org.mixer2.jaxb.xhtml.Script;

/**
 * @author jflute
 */
public class TypicalMixLayoutResolver {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected LayoutHeaderResolver headerResolver;
    protected LayoutFooterResolver footerResolver;
    protected LayoutPartsResolver partsResolver;
    protected boolean includingHeadSuppressed;
    protected boolean replacingHeaderSuppressed;
    protected boolean replacingFooterSuppressed;
    protected boolean includingScriptSuppressed;

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
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
    public TypicalMixLayoutResolver suppressIncludingHeader(boolean includingHeadSuppressed) {
        this.includingHeadSuppressed = includingHeadSuppressed;
        return this;
    }

    public TypicalMixLayoutResolver suppressReplacingHeader(boolean replacingHeaderSuppressed) {
        this.replacingHeaderSuppressed = replacingHeaderSuppressed;
        return this;
    }

    public TypicalMixLayoutResolver suppressReplacingFooter(boolean replacingFooterSuppressed) {
        this.replacingFooterSuppressed = replacingFooterSuppressed;
        return this;
    }

    public TypicalMixLayoutResolver suppressIncludingScript(boolean includingScriptSuppressed) {
        this.includingScriptSuppressed = includingScriptSuppressed;
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
            throw new IllegalArgumentException("The supporter 'supporter' should not be null.");
        }
        supporter.loadPartsHtml(getLayoutHtmlPath()).alwaysPresent(loaded -> {
            if (isIncludingHead()) {
                includeHead(html, supporter, loaded);
            }
            if (isReplacingHeader()) {
                replaceHeader(html, supporter, loaded);
            }
            if (isReplacingFooter()) {
                replaceFooter(html, supporter, loaded);
            }
            if (isIncludingScript()) {
                includeScript(html, supporter, loaded);
            }
            asYouLikeIt(html, supporter, loaded);
        });
    }

    protected String getLayoutHtmlPath() {
        return "/common/layout.html";
    }

    protected boolean isIncludingHead() {
        return !includingHeadSuppressed;
    }

    protected boolean isReplacingHeader() {
        return !replacingHeaderSuppressed;
    }

    protected boolean isReplacingFooter() {
        return !replacingFooterSuppressed;
    }

    protected boolean isIncludingScript() {
        return !includingScriptSuppressed;
    }

    protected void asYouLikeIt(Html html, Mixer2Supporter supporter, Html loaded) {
        if (partsResolver != null) {
            partsResolver.resolve(html, supporter, loaded);
        }
    }

    // ===================================================================================
    //                                                                               Head
    //                                                                              ======
    protected void includeHead(Html html, Mixer2Supporter supporter, Html loaded) {
        final Head realHead = loaded.getHead();
        final Head existingHead = html.getHead();
        existingHead.getContent().addAll(realHead.getContent());
    }

    // ===================================================================================
    //                                                                              Header
    //                                                                              ======
    protected void replaceHeader(Html html, Mixer2Supporter supporter, Html loaded) {
        final Header realHeader = supporter.findHeader(loaded).get();
        asYouLikeHeader(realHeader, supporter);
        supporter.findHeader(html).alwaysPresent(existingHeader -> {
            existingHeader.replaceInner(realHeader.getContent());
        });
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
        final Footer realFooter = supporter.findFooter(loaded).get();
        asYouLikeFooter(realFooter, supporter);
        supporter.findFooter(html).alwaysPresent(existingHeader -> {
            existingHeader.replaceInner(realFooter.getContent());
        });
    }

    protected void asYouLikeFooter(Footer footer, Mixer2Supporter supporter) {
        if (footerResolver != null) {
            footerResolver.resolve(footer, supporter);
        }
    }

    // ===================================================================================
    //                                                                              Script
    //                                                                              ======
    protected void includeScript(Html html, Mixer2Supporter supporter, Html loaded) {
        final List<Script> scriptList = loaded.getDescendants(Script.class);
        final Body body = html.getBody();
        final List<Object> contentList = body.getContent();
        if (!scriptList.isEmpty()) {
            for (Script script : scriptList) {
                contentList.add(script);
            }
            contentList.add("\n"); // format
        }
    }
}
