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

package org.apache.james.imapserver.processor.imap4rev1;

import org.apache.james.api.imap.ImapCommand;
import org.apache.james.api.imap.ImapMessage;
import org.apache.james.api.imap.message.request.ImapRequest;
import org.apache.james.api.imap.message.response.imap4rev1.StatusResponseFactory;
import org.apache.james.api.imap.process.ImapProcessor;
import org.apache.james.api.imap.process.ImapSession;
import org.apache.james.imap.message.request.imap4rev1.CloseRequest;
import org.apache.james.imapserver.processor.base.AbstractImapRequestProcessor;
import org.apache.james.imapserver.processor.base.ImapSessionUtils;
import org.apache.james.mailboxmanager.MailboxManagerException;
import org.apache.james.mailboxmanager.MailboxSession;
import org.apache.james.mailboxmanager.impl.FetchGroupImpl;
import org.apache.james.mailboxmanager.impl.MessageRangeImpl;
import org.apache.james.mailboxmanager.mailbox.Mailbox;

public class CloseProcessor extends AbstractImapRequestProcessor {

    public CloseProcessor(final ImapProcessor next, final StatusResponseFactory factory) {
        super(next, factory);
    }

    protected boolean isAcceptable(ImapMessage message) {
        return (message instanceof CloseRequest);
    }

    protected void doProcess(ImapRequest message,
            ImapSession session, String tag, ImapCommand command, Responder responder) {
        Mailbox mailbox = ImapSessionUtils.getMailbox(session);
        if (mailbox.isWriteable()) {
            try {
                final MailboxSession mailboxSession = ImapSessionUtils.getMailboxSession(session);
                mailbox.expunge(MessageRangeImpl.all(),
                        FetchGroupImpl.MINIMAL, mailboxSession);
                session.deselect();
                // TODO: the following comment was present in the code before
                // refactoring
                // TODO: doesn't seem to match the implementation
                // TODO: check that implementation is correct
                // Don't send unsolicited responses on close.
                unsolicitedResponses(session, responder, false);
                okComplete(command, tag, responder);
            } catch (MailboxManagerException e) {
                no(command, tag, responder, e);
            }
        }
    }
}