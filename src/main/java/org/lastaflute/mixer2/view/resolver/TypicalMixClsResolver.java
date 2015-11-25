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
package org.lastaflute.mixer2.view.resolver;

import java.util.List;

import org.dbflute.jdbc.Classification;
import org.dbflute.jdbc.ClassificationMeta;
import org.dbflute.util.Srl;
import org.lastaflute.core.direction.FwAssistantDirector;
import org.lastaflute.core.util.ContainerUtil;
import org.lastaflute.db.dbflute.classification.ListedClassificationProvider;
import org.lastaflute.db.dbflute.exception.ProvidedClassificationNotFoundException;
import org.lastaflute.mixer2.view.Mixer2Supporter;
import org.mixer2.jaxb.xhtml.Body;
import org.mixer2.jaxb.xhtml.Html;
import org.mixer2.jaxb.xhtml.Option;
import org.mixer2.xhtml.AbstractJaxb;

/**
 * @author jflute
 */
public class TypicalMixClsResolver {

    public void resolveCls(Html html, Mixer2Supporter supporter) {
        if (html == null) {
            throw new IllegalArgumentException("The argument 'html' should not be null.");
        }
        if (supporter == null) {
            throw new IllegalArgumentException("The supporter 'html' should not be null.");
        }
        final Body body = html.getBody();
        List<AbstractJaxb> tagList = supporter.searchTagList(body, tag -> {
            final String id = tag.getId();
            return id != null && id.startsWith("cls-");
        });
        for (AbstractJaxb tag : tagList) {
            if (!(tag instanceof Option)) { // #pending rich message 
                throw new IllegalStateException("Unmatched tag for the ID: " + tag.getClass() + ", " + tag.getId());
            }
            Option option = (Option) tag;
            ListedClassificationProvider provider =
                    ContainerUtil.getComponent(FwAssistantDirector.class).assistDbDirection().assistListedClassificationProvider();
            final String id = tag.getId();
            final String classificationName = Srl.substringFirstRear(id, "cls-");
            final ClassificationMeta meta;
            try {
                meta = provider.provide(classificationName);
            } catch (ProvidedClassificationNotFoundException e) { // #pending rich message
                throw new IllegalStateException("Not found the classification: " + classificationName, e);
            }
            // #pending now making
            final List<Classification> clsList = meta.listAll();
            int index = 0;
            for (Classification cls : clsList) {
                String code = cls.code();
                String alias = cls.alias();
                if (index == 0) {
                    option.setValue(code);
                    option.setContent(alias);
                    option.setId(null);
                } else {
                    final Option copied = option.copy(Option.class);
                    copied.setValue(code);
                    copied.setContent(alias);
                }
                ++index;
            }
        }
    }
}
