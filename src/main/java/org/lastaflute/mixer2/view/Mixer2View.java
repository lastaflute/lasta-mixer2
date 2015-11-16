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

import org.mixer2.jaxb.xhtml.Html;
import org.mixer2.xhtml.exception.TagTypeUnmatchException;

/**
 * @author jflute
 */
public interface Mixer2View {

    /**
     * @param html The plain html from template file. (NotNull)
     * @param supporter The supporter of mixer2, has e.g. core engine, request manager. (NotNull)
     * @throws TagTypeUnmatchException When specified tag type (e.g. Span.class) is unmatched with actual type.
     */
    void beDynamic(Html html, Mixer2Supporter supporter) throws TagTypeUnmatchException;
}
