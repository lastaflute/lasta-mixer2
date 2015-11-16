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

import org.dbflute.util.Srl;
import org.lastaflute.mixer2.view.Mixer2Supporter;
import org.lastaflute.mixer2.view.Mixer2View;
import org.mixer2.jaxb.xhtml.Html;
import org.mixer2.xhtml.exception.TagTypeUnmatchException;

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
    protected final TypicalMixErrorsResolver errorsResolver = createTypicalMixErrorsResolver();
    protected final TypicalMixLayoutResolver layoutResolver = createTypicalMixLayoutResolver();
    protected final TypicalMixStyleResolver styleResolver = createTypicalMixCssResolver();

    // ===================================================================================
    //                                                                        Dynamic HTML
    //                                                                        ============
    @Override
    public void beDynamic(Html html, Mixer2Supporter supporter) throws TagTypeUnmatchException {
        resolveErrors(html, supporter);
        resolveLayout(html, supporter);
        resolveStyle(html, supporter);
        resolveUrlLink(html, supporter);
        render(html, supporter);
    }

    protected abstract void render(Html html, Mixer2Supporter supporter) throws TagTypeUnmatchException;

    // ===================================================================================
    //                                                                              Errors
    //                                                                              ======
    protected void resolveErrors(Html html, Mixer2Supporter supporter) throws TagTypeUnmatchException {
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

    private TypicalMixLayoutResolver createTypicalMixLayoutResolver() {
        return new TypicalMixLayoutResolver();
    }

    // ===================================================================================
    //                                                                               Style
    //                                                                               =====
    protected void resolveStyle(Html html, Mixer2Supporter supporter) {
        styleResolver.resolveStyle(html, supporter);
    }

    protected TypicalMixStyleResolver createTypicalMixCssResolver() {
        return new TypicalMixStyleResolver();
    }

    // ===================================================================================
    //                                                                            URL Link
    //                                                                            ========
    protected void resolveUrlLink(Html html, Mixer2Supporter supporter) {
        supporter.resolveUrlLink(html);
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
}
