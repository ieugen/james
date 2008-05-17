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

package org.apache.james.mailboxmanager;

/**
 * Indicates that the failure is caused by a reference
 * to a mailbox which does not exist.
 */
public class MailboxNotFoundException extends MailboxManagerException {

    private static final long serialVersionUID = -8493370806722264915L;

    private static String message(String mailboxName) {
        final String result;
        if (mailboxName == null) {
            result = "Mailbox not found";
        } else {
            result = "Mailbox '" + mailboxName + "' not found.";
        }
        return result;
    }
    
    private final String mailboxName;
    
    /**
     * 
     * @param mailboxName name of the mailbox, not null
     */
    public MailboxNotFoundException(String mailboxName) {
        super(message(mailboxName));
        this.mailboxName = mailboxName;
    }

    /**
     * Gets the name of the mailbox which cannot be found.
     * @return name
     */
    public final String getMailboxName() {
        return mailboxName;
    }
}