/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.james.core;

import java.io.*;
import java.net.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import org.apache.mailet.*;
import org.apache.avalon.*;

/**
 *
 * @author Serge Knystautas <sergek@lokitech.com>
 */
public class MailetConfigImpl implements MailetConfig {
    private MailetContext mailetContext;
    private String name;
    //This would probably be better.
    //Properties params = new Properties();
    //Instead, we're tied to the Configuration object
    private Configuration configuration;

    public MailetConfigImpl() {

    }

    public String getInitParameter(String name) {
        String result = null;
        for (Enumeration e = configuration.getConfigurations(name); e.hasMoreElements(); ) {
            if (result == null) {
                result = "";
            } else {
                result += ",";
            }
            Configuration conf = (Configuration)e.nextElement();
            result += conf.getValue();
        }
        return result;
        //return params.getProperty(name);
    }

    public Iterator getInitParameterNames() {
        throw new RuntimeException("Not yet implemented");
        //return params.keySet().iterator();
    }

    public MailetContext getMailetContext() {
        return mailetContext;
    }

    public void setMailetContext(MailetContext newContext) {
        mailetContext = newContext;
    }

    public void setConfiguration(Configuration newConfiguration) {
        configuration = newConfiguration;
    }

    public String getMailetName() {
        return name;
    }

    public void setMailetName(String newName) {
        name = newName;
    }
}