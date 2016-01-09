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
package org.lastaflute.mixer2.messages;

import java.io.Serializable;
import java.util.Locale;

import org.lastaflute.core.message.MessageManager;
import org.lastaflute.web.ruts.message.ActionMessage;
import org.lastaflute.web.servlet.request.RequestManager;

/**
 * @author Toshi504
 * @author jflute
 */
public class ResolvedMessage implements Serializable {

    // ===================================================================================
    //                                                                          Definition
    //                                                                          ==========
    private static final long serialVersionUID = 1L;

    // ===================================================================================
    //                                                                           Attribute
    //                                                                           =========
    protected final ActionMessage message;
    protected final RequestManager requestManager;

    // ===================================================================================
    //                                                                         Constructor
    //                                                                         ===========
    public ResolvedMessage(ActionMessage origin, RequestManager requestManager) {
        this.message = origin;
        this.requestManager = requestManager;
    }

    // ===================================================================================
    //                                                                    Resolved Message
    //                                                                    ================
    /**
     * @return The resolved message about message resources. (NotNull)
     */
    public String getMessage() { // called by thymeleaf templates e.g. th:text="${er.message}"
        if (message.isResource()) {
            final Locale locale = requestManager.getUserLocale();
            final MessageManager messageManager = requestManager.getMessageManager();
            return messageManager.getMessage(locale, message.getKey());
        } else {
            return message.getKey();
        }
    }
}
