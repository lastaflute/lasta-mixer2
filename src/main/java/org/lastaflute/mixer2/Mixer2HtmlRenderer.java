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

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.util.DfResourceUtil;
import org.dbflute.util.Srl;
import org.lastaflute.mixer2.exception.Mixer2DynamicHtmlNofFoundException;
import org.lastaflute.mixer2.exception.Mixer2TemplateHtmlNofFoundException;
import org.lastaflute.mixer2.exception.Mixer2TemplateHtmlParseFailureException;
import org.lastaflute.mixer2.exception.Mixer2ViewInterfaceNotImplementedException;
import org.lastaflute.mixer2.view.Mixer2View;
import org.lastaflute.web.ruts.NextJourney;
import org.lastaflute.web.ruts.process.ActionRuntime;
import org.lastaflute.web.ruts.renderer.HtmlRenderer;
import org.lastaflute.web.servlet.request.RequestManager;
import org.lastaflute.web.util.LaServletContextUtil;
import org.mixer2.Mixer2Engine;
import org.mixer2.jaxb.exception.Mixer2JAXBException;
import org.mixer2.jaxb.xhtml.Html;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 */
public class Mixer2HtmlRenderer implements HtmlRenderer {

    private static final Logger logger = LoggerFactory.getLogger(Mixer2HtmlRenderer.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Mixer2Engine templateEngine;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public Mixer2HtmlRenderer(Mixer2Engine templateEngine) {
        this.templateEngine = templateEngine;
    }

    // ===================================================================================
    //                                                                              Redner
    //                                                                              ======
    @Override
    public void render(RequestManager requestManager, ActionRuntime runtime, NextJourney journey) throws IOException, ServletException {
        final Mixer2Engine engine = prepareTemplateEngine();
        final Mixer2View view = extractMixer2View(runtime, journey);
        showRendering(journey, view);
        final Html staticHtml = loadStaticHtml(requestManager, runtime, journey, engine);
        final Html dynamicHtml = toDynamicHtml(runtime, journey, view, staticHtml);
        final String htmlText = engine.saveToString(dynamicHtml);
        write(requestManager, htmlText);
    }

    protected Mixer2Engine prepareTemplateEngine() {
        return templateEngine;
    }

    protected void showRendering(NextJourney journey, Mixer2View view) {
        if (logger.isDebugEnabled()) {
            final String pureName = Srl.substringLastRear(journey.getRoutingPath(), "/");
            logger.debug("#flow ...Rendering {} by #mixer2 view: {}", pureName, view);
        }
    }

    // ===================================================================================
    //                                                                         Mixer2 View
    //                                                                         ===========
    protected Mixer2View extractMixer2View(ActionRuntime runtime, NextJourney journey) {
        final Object obj = journey.getViewObject().get();
        if (!(obj instanceof Mixer2View)) {
            throwMixer2ViewInterfaceNotImplementedException(runtime, obj);
        }
        return (Mixer2View) obj;
    }

    protected void throwMixer2ViewInterfaceNotImplementedException(ActionRuntime runtime, Object obj) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not implemented the Mixer2 view interface.");
        br.addItem("Advice");
        br.addElement("The view should implement Mixer2 view like this:");
        br.addElement("  (x):");
        br.addElement("    public class SeaView { // *NG");
        br.addElement("        ...");
        br.addElement("    }");
        br.addElement("  (o):");
        br.addElement("    public class SeaView implements Mixer2View { // OK");
        br.addElement("        ...");
        br.addElement("    }");
        br.addElement("  (o):");
        br.addElement("    public abstract class HarborBaseView implements Mixer2View {");
        br.addElement("        ...");
        br.addElement("    }");
        br.addElement("    public class SeaView extends HarborBaseView { // OK");
        br.addElement("        ...");
        br.addElement("    }");
        br.addItem("Action");
        br.addElement(runtime);
        br.addItem("Your View");
        br.addElement(obj.getClass());
        br.addElement(obj);
        final String msg = br.buildExceptionMessage();
        throw new Mixer2ViewInterfaceNotImplementedException(msg);
    }

    // ===================================================================================
    //                                                                         Static HTML
    //                                                                         ===========
    protected Html loadStaticHtml(RequestManager requestManager, ActionRuntime runtime, NextJourney journey, Mixer2Engine engine)
            throws IOException {
        final String routingPath = journey.getRoutingPath();
        final String webPath = buildWebPath(routingPath);
        InputStream ins = requestManager.getServletContext().getResourceAsStream(webPath);
        if (ins == null) {
            ins = DfResourceUtil.getResourceStream(routingPath);
            if (ins == null) {
                throwMixer2TemplateHtmlNotFoundException(runtime, journey, webPath);
            }
        }
        final Html staticHtml;
        try {
            staticHtml = engine.checkAndLoadHtmlTemplate(ins);
        } catch (Mixer2JAXBException e) {
            throwMixer2TemplateHtmlParseFailureException(runtime, journey, e);
            return null; // unreachable
        }
        return staticHtml;
    }

    protected String buildWebPath(String routingPath) {
        return LaServletContextUtil.getHtmlViewPrefix() + routingPath;
    }

    protected void throwMixer2TemplateHtmlNotFoundException(ActionRuntime runtime, NextJourney journey, String webPath) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the Mixer2 template HTML file.");
        br.addItem("Action");
        br.addElement(runtime);
        br.addItem("Template");
        br.addElement(journey);
        br.addItem("Web Path");
        br.addElement(webPath);
        final String msg = br.buildExceptionMessage();
        throw new Mixer2TemplateHtmlNofFoundException(msg);
    }

    protected void throwMixer2TemplateHtmlParseFailureException(ActionRuntime runtime, NextJourney journey, Mixer2JAXBException cause) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to parse the template html.");
        br.addItem("Action");
        br.addElement(runtime);
        br.addItem("Template");
        br.addElement(journey);
        final String saxMsg = cause.getSAXParseExceptionMessage(); // #thinking how to get line number?
        if (saxMsg != null) {
            br.addItem("SAX Message");
            br.addElement(saxMsg);
        }
        final String msg = br.buildExceptionMessage();
        throw new Mixer2TemplateHtmlParseFailureException(msg, cause);
    }

    // ===================================================================================
    //                                                                        Dynamic HTML
    //                                                                        ============
    protected Html toDynamicHtml(ActionRuntime runtime, NextJourney journey, Mixer2View view, Html staticHtml) {
        final Html dynamicHtml = view.toDynamicHtml(staticHtml);
        if (dynamicHtml == null) {
            throwMixer2DynamicHtmlNofFoundException(runtime, journey, view, staticHtml);
        }
        return dynamicHtml;
    }

    protected void throwMixer2DynamicHtmlNofFoundException(ActionRuntime runtime, NextJourney journey, Mixer2View view, Html staticHtml) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not found the dynamic html from your view.");
        br.addItem("Action");
        br.addElement(runtime);
        br.addItem("Template");
        br.addElement(journey);
        br.addItem("Mixer2 View");
        br.addElement(view);
        br.addItem("Static HTML");
        br.addElement(staticHtml);
        final String msg = br.buildExceptionMessage();
        throw new Mixer2DynamicHtmlNofFoundException(msg);
    }

    // ===================================================================================
    //                                                                               Write
    //                                                                               =====
    protected void write(RequestManager requestManager, String htmlText) {
        requestManager.getResponseManager().write(htmlText, "text/html", getEncoding());
    }

    protected String getEncoding() {
        return "UTF-8";
    }
}
