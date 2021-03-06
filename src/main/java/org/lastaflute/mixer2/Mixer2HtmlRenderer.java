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

import java.io.IOException;
import java.lang.reflect.Field;

import javax.annotation.Resource;
import javax.servlet.ServletException;

import org.dbflute.helper.message.ExceptionMessageBuilder;
import org.dbflute.util.DfReflectionUtil;
import org.dbflute.util.Srl;
import org.lastaflute.core.util.ContainerUtil;
import org.lastaflute.mixer2.exception.Mixer2DynamicHtmlFailureException;
import org.lastaflute.mixer2.exception.Mixer2ViewInterfaceNotImplementedException;
import org.lastaflute.mixer2.template.Mixer2TemplateReader;
import org.lastaflute.mixer2.template.Mixer2TemplateReader.LoadedHtml;
import org.lastaflute.mixer2.view.Mixer2Supporter;
import org.lastaflute.mixer2.view.Mixer2View;
import org.lastaflute.web.path.ActionPathResolver;
import org.lastaflute.web.ruts.NextJourney;
import org.lastaflute.web.ruts.process.ActionRuntime;
import org.lastaflute.web.ruts.renderer.HtmlRenderer;
import org.lastaflute.web.servlet.request.RequestManager;
import org.mixer2.Mixer2Engine;
import org.mixer2.jaxb.xhtml.Html;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jflute
 */
public class Mixer2HtmlRenderer implements HtmlRenderer {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final Logger logger = LoggerFactory.getLogger(Mixer2HtmlRenderer.class);

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Mixer2Engine engine;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public Mixer2HtmlRenderer(Mixer2Engine engine) {
        this.engine = engine;
    }

    // ===================================================================================
    //                                                                              Redner
    //                                                                              ======
    @Override
    public void render(RequestManager requestManager, ActionRuntime runtime, NextJourney journey) throws IOException, ServletException {
        final Mixer2View view = extractMixer2View(runtime, journey);
        showRendering(journey, view);
        final Mixer2TemplateReader reader = new Mixer2TemplateReader(engine, requestManager, runtime);
        final LoadedHtml loadedHtml = reader.loadHtml(journey.getRoutingPath()).get();
        final Html html = loadedHtml.getHtml();
        beDynamic(requestManager, runtime, journey, view, reader, html);
        final String htmlText = engine.saveToString(html);
        final String realText = reader.resolveHtmlDef(htmlText, loadedHtml);
        write(requestManager, realText);
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
        final Mixer2View view = (Mixer2View) obj;
        injectSimply(view);
        return view;
    }

    protected void throwMixer2ViewInterfaceNotImplementedException(ActionRuntime runtime, Object obj) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Not implemented the Mixer2 view interface.");
        br.addItem("Advice");
        br.addElement("The view should implement Mixer2 view like this:");
        br.addElement("  (x):");
        br.addElement("    public class SeaView { // *Bad");
        br.addElement("        ...");
        br.addElement("    }");
        br.addElement("  (o):");
        br.addElement("    public class SeaView implements Mixer2View { // Good");
        br.addElement("        ...");
        br.addElement("    }");
        br.addElement("  (o):");
        br.addElement("    public abstract class HarborBaseView implements Mixer2View {");
        br.addElement("        ...");
        br.addElement("    }");
        br.addElement("    public class SeaView extends HarborBaseView { // Good");
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

    protected void injectSimply(Mixer2View view) {
        new EverywhereInjector().injectSimply(view);
    }

    public static class EverywhereInjector { // #pending Lasta Di should provide it

        public void injectSimply(Object target) {
            for (Class<?> currentType = target.getClass(); !currentType.equals(Object.class); currentType = currentType.getSuperclass()) {
                final Field[] fields = currentType.getDeclaredFields();
                for (Field field : fields) {
                    if (field.getAnnotation(Resource.class) != null) { // type only #for_now
                        final Object component = ContainerUtil.getComponent(field.getType());
                        DfReflectionUtil.setValueForcedly(field, target, component);
                    }
                }
            }
        }
    }

    // ===================================================================================
    //                                                                        Dynamic HTML
    //                                                                        ============
    protected void beDynamic(RequestManager requestManager, ActionRuntime runtime, NextJourney journey, Mixer2View view,
            Mixer2TemplateReader reader, Html html) {
        try {
            view.beDynamic(html, createMixer2Supporter(requestManager, runtime, journey, reader));
        } catch (RuntimeException e) {
            throwMixer2DynamicHtmlFailureException(runtime, journey, view, html, e);
        }
    }

    protected Mixer2Supporter createMixer2Supporter(RequestManager requestManager, ActionRuntime runtime, NextJourney journey,
            Mixer2TemplateReader reader) {
        final ActionPathResolver actionPathResolver = ContainerUtil.getComponent(ActionPathResolver.class); // #pending from requestManager
        return new Mixer2Supporter(engine, requestManager, reader, actionPathResolver);
    }

    protected void throwMixer2DynamicHtmlFailureException(ActionRuntime runtime, NextJourney journey, Mixer2View view, Html html,
            RuntimeException e) {
        final ExceptionMessageBuilder br = new ExceptionMessageBuilder();
        br.addNotice("Failed to be dynamic HTML by your view.");
        br.addItem("Action");
        br.addElement(runtime);
        br.addItem("Template");
        br.addElement(journey);
        br.addItem("Mixer2 View");
        br.addElement(view);
        br.addItem("HTML Object");
        br.addElement(html);
        final String msg = br.buildExceptionMessage();
        throw new Mixer2DynamicHtmlFailureException(msg, e);
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
