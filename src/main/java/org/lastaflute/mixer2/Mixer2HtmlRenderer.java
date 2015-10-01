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

import javax.servlet.ServletException;

import org.lastaflute.web.callback.ActionRuntime;
import org.lastaflute.web.ruts.NextJourney;
import org.lastaflute.web.ruts.renderer.HtmlRenderer;
import org.lastaflute.web.servlet.request.RequestManager;

/**
 * @author jflute
 */
public class Mixer2HtmlRenderer implements HtmlRenderer {

    @Override
    public void render(RequestManager requestManager, ActionRuntime runtime, NextJourney journey) throws IOException, ServletException {
        @SuppressWarnings("unused")
        String routingPath = journey.getRoutingPath();
    }
}
