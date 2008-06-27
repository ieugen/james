/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */
package org.apache.james.transport.mailets;

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.james.Constants;
import org.apache.james.services.UsersRepository;
import org.apache.james.services.UsersStore;
import org.apache.mailet.MailAddress;

/**
 * Adds or removes an email address to a listserv.
 *
 * <p>Sample configuration:
 * <br>&lt;mailet match="CommandForListserv=james@list.working-dogs.com" class="AvalonListservManager"&gt;
 * <br>&lt;repositoryName&gt;name of user repository configured in UsersStore block &lt;/repositoryName&gt;
 * <br>&lt;/mailet&gt;
 *
 * @author  <a href="sergek@lokitech.com">Serge Knystautas </a>
 * @version This is $Revision: 1.3 $
 * Committed on $Date: 2002/01/18 02:48:38 $ by: $Author: darrell $ 
 */
public class AvalonListservManager extends GenericListservManager {

    private UsersRepository members;

    public void init() {
        ComponentManager compMgr = (ComponentManager)getMailetContext().getAttribute(Constants.AVALON_COMPONENT_MANAGER);
        try {
            UsersStore usersStore = (UsersStore) compMgr.lookup("org.apache.james.services.UsersStore");
            String repName = getInitParameter("repositoryName");

            members = (UsersRepository) usersStore.getRepository(repName);
        } catch (ComponentException cnfe) {
            log("Failed to retrieve Store component:" + cnfe.getMessage());
        } catch (Exception e) {
            log("Failed to retrieve Store component:" + e.getMessage());
        }
    }

    public boolean addAddress(MailAddress address) {
        members.addUser(address.toString(), "");
        return true;
    }

    public boolean removeAddress(MailAddress address) {
        members.removeUser(address.toString());
        return true;
    }

    public boolean existsAddress(MailAddress address) {
        return members.contains(address.toString());
    }

    public String getMailetInfo() {
        return "AvalonListservManager Mailet";
    }
}
