/***********************************************************************
 * Copyright (c) 1999-2006 The Apache Software Foundation.             *
 * All rights reserved.                                                *
 * ------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License"); you *
 * may not use this file except in compliance with the License. You    *
 * may obtain a copy of the License at:                                *
 *                                                                     *
 *     http://www.apache.org/licenses/LICENSE-2.0                      *
 *                                                                     *
 * Unless required by applicable law or agreed to in writing, software *
 * distributed under the License is distributed on an "AS IS" BASIS,   *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or     *
 * implied.  See the License for the specific language governing       *
 * permissions and limitations under the License.                      *
 ***********************************************************************/
package org.apache.james.test.mock.mailet;

import org.apache.mailet.MailetConfig;
import org.apache.mailet.MailetContext;

import java.util.Iterator;
import java.util.Properties;

/**
 * MailetConfig over Properties
 */
public class MockMailetConfig extends Properties implements MailetConfig {

    private String mailetName;
    private MailetContext mc;

    public MockMailetConfig(String mailetName, MailetContext mc) {
        super();
        this.mailetName = mailetName;
        this.mc = mc;
    }

    public MockMailetConfig(String mailetName, MailetContext mc, Properties arg0) {
        super(arg0);
        this.mailetName = mailetName;
        this.mc = mc;
    }

    public String getInitParameter(String name) {
        return getProperty(name);
    }

    public Iterator getInitParameterNames() {
        return keySet().iterator();
    }

    public MailetContext getMailetContext() {
        return mc;
    }

    public String getMailetName() {
        return mailetName;
    }

}