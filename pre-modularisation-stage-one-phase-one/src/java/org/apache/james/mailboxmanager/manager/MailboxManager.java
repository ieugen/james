/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.james.mailboxmanager.manager;

import org.apache.james.mailboxmanager.GeneralMessageSet;
import org.apache.james.mailboxmanager.ListResult;
import org.apache.james.mailboxmanager.MailboxManagerException;
import org.apache.james.mailboxmanager.mailbox.GeneralMailbox;
import org.apache.james.mailboxmanager.mailbox.GeneralMailboxSession;
import org.apache.james.mailboxmanager.mailbox.ImapMailboxSession;
import org.apache.james.mailboxmanager.mailbox.MailboxSession;
import org.apache.james.services.User;


/**
 * <p>
 * Central MailboxManager which creates, lists, provides, renames and
 * deletes Mailboxes
 * </p>
 * <p>
 * An important goal is to be JavaMail feature compatible. That means JavaMail
 * could be used in both directions: As a backend for e.g. accessing a Maildir
 * JavaMail store or as a frontend to access a JDBC MailboxManager
 * through JavaMail. This should be possible by not too complicated wrapper
 * classes. Due to the complexity of JavaMail it might be impossible to avoid
 * some limitations.
 * </p>
 * <p>
 * Internally MailboxManager deals with named repositories that could have
 * different implementations. E.g. JDBC connections to different hosts or
 * Maildir / Mbox like stores. This repositories are identified by its names and
 * maybe are configured in config.xml. The names of the mailboxes have to be
 * mapped to the corresponding repository name. For user mailboxes this could be
 * done by a "User.getRepositoryName()" property. It is imaginable that
 * repositories lookup further properties from the user object like a path name
 * for a file based storage method. Until Milestone 6 there is only one named
 * repository: "default".
 * </p>
 * <p>
 * The only operation that requires dealing with the named repositories directly
 * is the quota management. It is probably really difficult to implement a quota
 * system that spans multiple repository implementations. That is why quotas are
 * created for a specific repository. To be able to administer, repositories and
 * theier belonging mailboxes can be listet.
 * </p>
 */

public interface MailboxManager {

    public static final char HIERARCHY_DELIMITER='.';
    
    public static final String USER_NAMESPACE="#mail";

    /**
     * get a session mailbox 
     * 
     * @param mailboxName
     * @throws MailboxManagerException 
     */
    MailboxSession getMailboxSession(String mailboxName, boolean autoCreate)
            throws MailboxManagerException;
    
    /**
     * Supports 
     * 
     * @param mailboxName
     * @throws MailboxManagerException
     */
    
    GeneralMailboxSession getGeneralMailboxSession(String mailboxName) throws MailboxManagerException;
    
    ImapMailboxSession getImapMailboxSession(String mailboxName) throws MailboxManagerException;

    void createMailbox(String mailboxName) throws MailboxManagerException;

    void deleteMailbox(String mailboxName) throws MailboxManagerException;

    void renameMailbox(String from, String to) throws MailboxManagerException;

    /**
     * this is done by the MailboxRepository because maybe this operation could
     * be optimized in the corresponding store.
     * 
     * @param from
     * @param set
     *            messages to copy
     * @param to
     *            name of the destination mailbox
     */
    void copyMessages(GeneralMailbox from, GeneralMessageSet set, String to) throws MailboxManagerException;

    /**
     * 
     * @param base
     *            hierarchy starting point like #mail.user1 to list mailboxes of
     *            user1
     * @param expression
     *            allows the use of wildcards
     * @param subscribed
     *            if true, only list subscribed mailboxes (needs interaction
     *            with the user-object)
     * @throws MailboxManagerException 
     */

    ListResult[] list(String base, String expression, boolean subscribed) throws MailboxManagerException;

    /**
     * could be implemented later. There could be enviroments where
     * subscribtions are stored in the mailbox database. Another possibility is
     * to manage subscribtions in the user repository, e.g. a ldap attribute,
     * 
     * @param mailboxName
     * @param value
     */

    void setSubscription(String mailboxName, boolean value) throws MailboxManagerException;

    boolean existsMailbox(String mailboxName) throws MailboxManagerException;

    void close();

    // TODO maybe move createInbox to provider
    boolean createInbox(User user) throws MailboxManagerException;

}
