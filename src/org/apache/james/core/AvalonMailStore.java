/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included  with this distribution in
 * the LICENSE file.
 */
package org.apache.james.core;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import org.apache.avalon.Initializable;
import org.apache.avalon.component.Component;
import org.apache.avalon.component.ComponentException;
import org.apache.avalon.component.ComponentException;
import org.apache.avalon.component.ComponentException;
import org.apache.avalon.component.ComponentManager;
import org.apache.avalon.component.Composable;
import org.apache.avalon.configuration.Configurable;
import org.apache.avalon.configuration.Configuration;
import org.apache.avalon.configuration.ConfigurationException;
import org.apache.avalon.logger.AbstractLoggable;
import org.apache.james.services.MailRepository;
import org.apache.james.services.MailStore;
import org.apache.log.LogKit;
import org.apache.log.Logger;
import org.apache.phoenix.Block;

/**
 *
 * @author <a href="mailto:fede@apache.org">Federico Barbieri</a>
 */
public class AvalonMailStore
    extends AbstractLoggable
    implements Block, Composable, Configurable, MailStore, Initializable {

    private static final String REPOSITORY_NAME = "Repository";
    private static long id;
    private HashMap repositories;
    private HashMap models;
    private HashMap classes;
    protected Configuration          configuration;
    protected ComponentManager       componentManager;

    public void compose( final ComponentManager componentManager )
        throws ComponentException
    {
        this.componentManager = componentManager;
    }

    public void configure( final Configuration configuration )
        throws ConfigurationException
    {
        this.configuration = configuration;
    }

    public void init()
        throws Exception {

        getLogger().info("JamesMailStore init...");
        repositories = new HashMap();
        models = new HashMap();
        classes = new HashMap();
        Configuration[] registeredClasses
            = configuration.getChild("repositories").getChildren("repository");
        for ( int i = 0; i < registeredClasses.length; i++ )
        {
            registerRepository((Configuration) registeredClasses[i]);
        }
        getLogger().info("James RepositoryManager ...init");
    }

    public void registerRepository(Configuration repConf)
        throws ConfigurationException {
        String className = repConf.getAttribute("class");
        getLogger().info("Registering Repository " + className);
        Configuration[] protocols
            = repConf.getChild("protocols").getChildren("protocol");
        Configuration[] types = repConf.getChild("types").getChildren("type");
        Configuration[] models
            = repConf.getChild("models").getChildren("model");
        for ( int i = 0; i < protocols.length; i++ )
        {
            String protocol = protocols[i].getValue();

            for ( int j = 0; j < types.length; j++ )
            {
                String type = types[j].getValue();

                for ( int k = 0; k < models.length; k++ )
                {
                    String model = models[k].getValue();

                    classes.put(protocol + type + model, className);
                    getLogger().info("   for " + protocol + "," + type + "," + model);
                }
            }
        }
    }

    public void release(Component component)
    {
    }

    public Component select(Object hint) throws ComponentException,
    ComponentException {

        Configuration repConf = null;
        try {
            repConf = (Configuration) hint;
        } catch (ClassCastException cce) {
            throw new ComponentException("hint is of the wrong type. Must be a Configuration", cce);
        }
        URL destination = null;
        try {
            destination = new URL(repConf.getAttribute("destinationURL"));
        } catch (ConfigurationException ce) {
            throw new ComponentException("Malformed configuration has no destinationURL attribute", ce);
        } catch (MalformedURLException mue) {
            throw new ComponentException("destination is malformed. Must be a valid URL", mue);
        }

        try
        {
            String type = repConf.getAttribute("type");
            String repID = destination + type;
            MailRepository reply = (MailRepository) repositories.get(repID);
            String model = (String) repConf.getAttribute("model");
            if (reply != null) {
                if (models.get(repID).equals(model)) {
                    return (Component)reply;
                } else {
                    throw new ComponentException("There is already another repository with the same destination and type but with different model");
                }
            } else {
                String protocol = destination.getProtocol();
                String repClass = (String) classes.get( protocol + type + model );

                getLogger().debug( "Need instance of " + repClass +
                                   " to handle: " + protocol + type + model );

                try {
                    reply = (MailRepository) Class.forName(repClass).newInstance();
                    if (reply instanceof Configurable) {
                        ((Configurable) reply).configure(repConf);
                    }
                    if (reply instanceof Composable) {
                        ((Composable) reply).compose( componentManager );
                    }
/*                if (reply instanceof Contextualizable) {
                  ((Contextualizable) reply).contextualize(context);
                  }*/
                    if (reply instanceof Initializable) {
                        ((Initializable) reply).init();
                    }
                    repositories.put(repID, reply);
                    models.put(repID, model);
                    getLogger().info( "New instance of " + repClass +
                                      " created for " + destination );
                    return (Component)reply;
                } catch (Exception e) {
                    getLogger().warn( "Exception while creating repository:" +
                                      e.getMessage(), e );

                    throw new
                        ComponentException( "Cannot find or init repository", e );
                }
            }
        } catch( final ConfigurationException ce ) {
            throw new ComponentException( "Malformed configuration", ce );
        }
    }

    public static final String getName() {
        return REPOSITORY_NAME + id++;
    }
}
