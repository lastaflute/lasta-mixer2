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
package org.lastaflute.mixer2;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;

import org.dbflute.util.DfResourceUtil;
import org.lastaflute.web.callback.ActionRuntime;
import org.lastaflute.web.ruts.NextJourney;
import org.lastaflute.web.ruts.renderer.HtmlRenderer;
import org.lastaflute.web.servlet.request.RequestManager;
import org.mixer2.Mixer2Engine;
import org.mixer2.jaxb.xhtml.Html;

/**
 * @author jflute
 */
public class Mixer2HtmlRenderer implements HtmlRenderer {

    // #thinking mapping design
    // _/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/_/
    // src/main/java
    //  |-org.docksidestage.app.web
    //  |  |-product
    //  |  |  |-ProductListAction.java
    //  |  |  |-ProductListView.java
    // src/main/resources
    //  |-m2mockup
    //  |  |-m2static
    //  |  |  |-css
    //  |  |  |-image
    //  |  |-m2template
    //  |     |-product
    //  |     |  |-product_list.html => product.ProductListView
    //
    // or
    //
    // return asHtml(path_Sea_SeaLandHtml).withRenderer(() -> {
    //     ...???
    // });
    //
    // or
    //
    // return asHtml(path_Sea_SeaLandHtml).withRenderer(ProductListView.class);
    // _/_/_/_/_/_/_/_/_/_/
    @Override
    public void render(RequestManager requestManager, ActionRuntime runtime, NextJourney journey) throws IOException, ServletException {
        final Mixer2Engine engine = createMixer2Engine();
        final Html html = loadHtml(engine, journey);
        // #thinking view here?
        write(requestManager, engine.saveToString(html));
    }

    protected Mixer2Engine createMixer2Engine() {
        return new Mixer2Engine(); // #thinking cache?
    }

    protected Html loadHtml(Mixer2Engine engine, NextJourney journey) throws IOException {
        final InputStream ins = DfResourceUtil.getResourceStream(journey.getRoutingPath()); // #thinking how?
        return engine.loadHtmlTemplate(ins);
    }

    protected void write(RequestManager requestManager, String htmlText) {
        requestManager.getResponseManager().write(htmlText, "text/html", getEncoding());
    }

    protected String getEncoding() {
        return "UTF-8";
    }
}
