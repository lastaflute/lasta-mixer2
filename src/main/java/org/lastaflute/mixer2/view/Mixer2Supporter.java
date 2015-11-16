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

import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

import org.dbflute.optional.OptionalThing;
import org.lastaflute.web.servlet.request.RequestManager;
import org.mixer2.Mixer2Engine;
import org.mixer2.jaxb.exception.Mixer2JAXBException;
import org.mixer2.jaxb.xhtml.Html;
import org.mixer2.xhtml.AbstractJaxb;

/**
 * @author jflute
 */
public class Mixer2Supporter {

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final Mixer2Engine engine;
    protected final RequestManager requestManager;
    protected final Function<String, OptionalThing<InputStream>> streamProvider; // #pending original interface

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public Mixer2Supporter(Mixer2Engine engine, RequestManager requestManager,
            Function<String, OptionalThing<InputStream>> streamProvider) {
        this.engine = engine;
        this.requestManager = requestManager;
        this.streamProvider = streamProvider;
    }

    // ===================================================================================
    //                                                                           Supporter
    //                                                                           =========
    public <TAG extends AbstractJaxb> OptionalThing<TAG> loadById(String path, String id, Class<TAG> tagType) {
        return streamProvider.apply(path).map(ins -> {
            return loadHtml(ins, path);
        }).map(html -> {
            return getById(html, id, tagType);
        });
    }

    protected <TAG extends AbstractJaxb> TAG getById(AbstractJaxb html, String id, Class<TAG> tagType) {
        try {
            return html.getById(id, tagType);
        } catch (Exception e) { // #pending rich message
            throw new IllegalStateException("Failed to get by the ID: " + id + ", " + tagType, e);
        }
    }

    protected Html loadHtml(InputStream ins, String path) {
        try {
            return engine.checkAndLoadHtmlTemplate(ins);
        } catch (Mixer2JAXBException | IOException e) { // #pending rich message
            throw new IllegalStateException("Failed to load the template file: " + path, e);
        }
    }

    // ===================================================================================
    //                                                                            Accessor
    //                                                                            ========
    public Mixer2Engine getEngine() {
        return engine;
    }

    public RequestManager getRequestManager() {
        return requestManager;
    }
}
