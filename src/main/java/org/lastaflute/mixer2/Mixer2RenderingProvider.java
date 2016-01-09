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
package org.lastaflute.mixer2;

import org.lastaflute.web.response.HtmlResponse;
import org.lastaflute.web.ruts.NextJourney;
import org.lastaflute.web.ruts.process.ActionRuntime;
import org.lastaflute.web.ruts.renderer.HtmlRenderer;
import org.lastaflute.web.ruts.renderer.HtmlRenderingProvider;
import org.lastaflute.web.util.LaServletContextUtil;
import org.mixer2.Mixer2Engine;

/**
 * Mixer2 rendering provider of Lastaflute.
 * @author jflute
 */
public class Mixer2RenderingProvider implements HtmlRenderingProvider {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    public static final String DEFAULT_TEMPLATE_MODE = "HTML5";
    public static final String DEFAULT_TEMPLATE_ENCODING = "UTF-8";

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected boolean development;
    private Mixer2Engine cachedTemplateEngine;

    // ===================================================================================
    //                                                                              Option
    //                                                                              ======
    public Mixer2RenderingProvider asDevelopment(boolean development) {
        this.development = development;
        return this;
    }

    // ===================================================================================
    //                                                                             Provide
    //                                                                             =======
    /**
     * @param runtime The runtime of current requested action. (NotNull)
     * @param journey The journey to next stage. (NotNull)
     * @return The renderer to render HTML. (NotNull)
     */
    @Override
    public HtmlRenderer provideRenderer(ActionRuntime runtime, NextJourney journey) {
        if (journey.getRoutingPath().endsWith(".jsp")) {
            return DEFAULT_RENDERER;
        }
        return createMixer2HtmlRenderer();
    }

    protected Mixer2HtmlRenderer createMixer2HtmlRenderer() {
        return new Mixer2HtmlRenderer(getTemplateEngine());
    }

    @Override
    public HtmlResponse provideShowErrorsResponse(ActionRuntime runtime) {
        return HtmlResponse.fromForwardPath("/error/show_errors.html");
    }

    // ===================================================================================
    //                                                                     Template Engine
    //                                                                     ===============
    protected Mixer2Engine getTemplateEngine() {
        if (cachedTemplateEngine != null) {
            return cachedTemplateEngine;
        }
        synchronized (this) {
            if (cachedTemplateEngine != null) {
                return cachedTemplateEngine;
            }
            cachedTemplateEngine = createTemplateEngine();
        }
        return cachedTemplateEngine;
    }

    protected Mixer2Engine createTemplateEngine() {
        final Mixer2Engine engine = newTemplateEngine();
        setupTemplateEngine(engine);
        return engine;
    }

    protected Mixer2Engine newTemplateEngine() {
        return new Mixer2Engine();
    }

    protected void setupTemplateEngine(Mixer2Engine engine) {
    }

    protected String getHtmlViewPrefix() {
        return LaServletContextUtil.getHtmlViewPrefix();
    }

    protected String getTemplateMode() {
        return DEFAULT_TEMPLATE_MODE;
    }

    protected String getEncoding() {
        return DEFAULT_TEMPLATE_ENCODING;
    }

    protected boolean isCacheable() {
        return !development;
    }
}
