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
package org.lastaflute.mixer2.view;

import org.dbflute.util.Srl;
import org.lastaflute.mixer2.view.resolver.TypicalMixClsResolver;
import org.lastaflute.mixer2.view.resolver.TypicalMixErrorsResolver;
import org.lastaflute.mixer2.view.resolver.TypicalMixLayoutResolver;
import org.lastaflute.mixer2.view.resolver.TypicalMixStyleResolver;
import org.lastaflute.web.UrlChain;
import org.mixer2.jaxb.xhtml.Html;

/**
 * @author jflute
 */
public abstract class TypicalMixView implements Mixer2View {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    static {
        System.setProperty("com.sun.xml.internal.bind.v2.runtime.JAXBContextImpl.fastBoot", "true");
    }

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final TypicalMixClsResolver clsResolver = createTypicalMixClsResolver();
    protected final TypicalMixErrorsResolver errorsResolver = createTypicalMixErrorsResolver();
    protected final TypicalMixLayoutResolver layoutResolver = createTypicalMixLayoutResolver();
    protected final TypicalMixStyleResolver styleResolver = createTypicalMixStyleResolver();

    // ===================================================================================
    //                                                                        Dynamic HTML
    //                                                                        ============
    @Override
    public void beDynamic(Html html, Mixer2Supporter supporter) {
        // #pending now making
        resolveCls(html, supporter);
        resolveErrors(html, supporter);
        resolveLayout(html, supporter);
        resolveStyle(html, supporter);
        render(html, supporter);
        resolveLinkUrl(html, supporter);
    }

    protected abstract void render(Html html, Mixer2Supporter supporter);

    // ===================================================================================
    //                                                                      Classification
    //                                                                      ==============
    protected void resolveCls(Html html, Mixer2Supporter supporter) {
        clsResolver.resolveCls(html, supporter);
    }

    protected TypicalMixClsResolver createTypicalMixClsResolver() {
        return new TypicalMixClsResolver();
    }

    // ===================================================================================
    //                                                                              Errors
    //                                                                              ======
    protected void resolveErrors(Html html, Mixer2Supporter supporter) {
        errorsResolver.resolveErrors(html, supporter);
    }

    protected TypicalMixErrorsResolver createTypicalMixErrorsResolver() {
        return new TypicalMixErrorsResolver();
    }

    // ===================================================================================
    //                                                                              Layout
    //                                                                              ======
    protected void resolveLayout(Html html, Mixer2Supporter supporter) {
        layoutResolver.resolveLayout(html, supporter);
    }

    protected TypicalMixLayoutResolver createTypicalMixLayoutResolver() {
        final TypicalMixLayoutResolver resolver = new TypicalMixLayoutResolver();
        customizeLayout(resolver);
        return resolver;
    }

    protected void customizeLayout(TypicalMixLayoutResolver resolver) { // may be overridden
    }

    // ===================================================================================
    //                                                                               Style
    //                                                                               =====
    protected void resolveStyle(Html html, Mixer2Supporter supporter) {
        styleResolver.resolveStyle(html, supporter);
    }

    protected TypicalMixStyleResolver createTypicalMixStyleResolver() {
        return new TypicalMixStyleResolver();
    }

    // ===================================================================================
    //                                                                            Link URL
    //                                                                            ========
    protected void resolveLinkUrl(Html html, Mixer2Supporter supporter) {
        supporter.resolveLinkUrl(html);
    }

    // ===================================================================================
    //                                                                        Small Helper
    //                                                                        ============
    // -----------------------------------------------------
    //                                        Empty Handling
    //                                        --------------
    protected boolean isEmpty(String str) {
        return Srl.is_Null_or_Empty(str);
    }

    protected boolean isNotEmpty(String str) {
        return Srl.is_NotNull_and_NotEmpty(str);
    }

    // -----------------------------------------------------
    //                                    UrlChain Following
    //                                    ------------------
    /**
     * Set up more URL parts as URL chain. <br>
     * The name and specification of this method is synchronized with UrlChain#moreUrl().
     * @param urlParts The varying array of URL parts. (NotNull)
     * @return The created instance of URL chain. (NotNull)
     */
    protected UrlChain moreUrl(Object... urlParts) {
        assertArgumentNotNull("urlParts", urlParts);
        return newUrlChain().moreUrl(urlParts);
    }

    /**
     * Set up parameters on GET as URL chain. <br>
     * The name and specification of this method is synchronized with UrlChain#params().
     * @param paramsOnGet The varying array of parameters on GET. (NotNull)
     * @return The created instance of URL chain. (NotNull)
     */
    protected UrlChain params(Object... paramsOnGet) {
        assertArgumentNotNull("paramsOnGet", paramsOnGet);
        return newUrlChain().params(paramsOnGet);
    }

    /**
     * Set up hash on URL as URL chain. <br>
     * The name and specification of this method is synchronized with UrlChain#hash().
     * @param hashOnUrl The value of hash on URL. (NotNull)
     * @return The created instance of URL chain. (NotNull)
     */
    protected UrlChain hash(Object hashOnUrl) {
        assertArgumentNotNull("hashOnUrl", hashOnUrl);
        return newUrlChain().hash(hashOnUrl);
    }

    protected UrlChain newUrlChain() {
        return new UrlChain(this);
    }

    // ===================================================================================
    //                                                                      General Helper
    //                                                                      ==============
    private void assertArgumentNotNull(String variableName, Object value) {
        if (variableName == null) {
            throw new IllegalArgumentException("The argument 'variableName' should not be null.");
        }
        if (value == null) {
            throw new IllegalArgumentException("The argument '" + variableName + "' should not be null.");
        }
    }
}
