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

package org.apache.james.imapserver.handler.session;

import java.io.IOException;

import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.Flags.Flag;
import javax.mail.internet.MimeMessage;

import org.apache.james.imapserver.ProtocolException;
import org.apache.james.imapserver.client.FetchCommand;
import org.apache.james.imapserver.client.LoginCommand;
import org.apache.james.imapserver.client.SelectCommand;
import org.apache.james.imapserver.util.MessageGenerator;
import org.apache.james.mailboxmanager.MailboxManagerException;

public class UidFlagSizeFetchSessionTest extends AbstractSessionTest {
    
    String[] folders = {USER_MAILBOX_ROOT + ".INBOX",USER_MAILBOX_ROOT + ".test",USER_MAILBOX_ROOT + ".test1",USER_MAILBOX_ROOT + ".test1.test1a",USER_MAILBOX_ROOT + ".test1.test1b",USER_MAILBOX_ROOT + ".test2.test2a",USER_MAILBOX_ROOT + ".test2.test2b"};
    String[] onlyInbox = {USER_MAILBOX_ROOT + ".INBOX"};
    MimeMessage[] msgs= null;
    long[] uids = null;
    
    public void setUp() throws Exception {
        super.setUp();
        msgs=MessageGenerator.generateSimpleMessages(10);
        createFolders(onlyInbox);
        // increase the uid
        appendMessagesClosed(USER_MAILBOX_ROOT+".INBOX",msgs);
        deleteAll(USER_MAILBOX_ROOT+".INBOX");
        uids=addUIDMessagesOpen(USER_MAILBOX_ROOT+".INBOX",msgs);
    }
    
    public void testEmptyFetchInRange() throws ProtocolException, IOException, MessagingException, MailboxManagerException {
        
        verifyCommand(new LoginCommand(USER_NAME,USER_PASSWORD));
        verifyCommand(new SelectCommand(USER_MAILBOX_ROOT+".INBOX", msgs, getUidValidity(USER_MAILBOX_ROOT+".INBOX")));
        
        verifyCommand(new FetchCommand(msgs,1,-1));
        verifyCommand(new FetchCommand(msgs,1,5));
        verifyCommand(new FetchCommand(msgs,5,-1));
        verifyCommand(new FetchCommand(msgs,2,2));
    }

    public void testEmptyFetchOutOfRange() throws ProtocolException, IOException, MessagingException, MailboxManagerException {
        verifyCommand(new LoginCommand(USER_NAME,USER_PASSWORD));
        verifyCommand(new SelectCommand(USER_MAILBOX_ROOT+".INBOX", msgs, getUidValidity(USER_MAILBOX_ROOT+".INBOX")));

        verifyCommand(new FetchCommand(msgs,0,2)); 
        verifyCommand(new FetchCommand(msgs,3,2));
        verifyCommand(new FetchCommand(msgs,3,12));
    }
    
    public void testFetchUids() throws ProtocolException, IOException, MessagingException, MailboxManagerException {
        verifyCommand(new LoginCommand(USER_NAME,USER_PASSWORD));
        verifyCommand(new SelectCommand(USER_MAILBOX_ROOT+".INBOX", msgs, getUidValidity(USER_MAILBOX_ROOT+".INBOX")));
        
        FetchCommand fc=new FetchCommand(msgs,1,-1);
        fc.setUids(uids);
        verifyCommand(fc);

    }
    
    public void testFetchUidsFlags() throws ProtocolException, IOException, MessagingException, MailboxManagerException {
        // Folder hast to be kept open for RECENT Flags
        useFolder(USER_MAILBOX_ROOT+".INBOX");
        appendMessagesClosed(USER_MAILBOX_ROOT+".INBOX",MessageGenerator.generateSimpleMessages(5));
        setFlags(USER_MAILBOX_ROOT+".INBOX", 12, 12, new Flags(Flags.Flag.SEEN), true, false);
        setFlags(USER_MAILBOX_ROOT+".INBOX", 14, 14, new Flags(Flags.Flag.SEEN), true, false);
        msgs=getMessages(USER_MAILBOX_ROOT+".INBOX");
        uids=getUids(USER_MAILBOX_ROOT+".INBOX");
        

        
        verifyCommand(new LoginCommand(USER_NAME,USER_PASSWORD));
        SelectCommand sc=new SelectCommand(USER_MAILBOX_ROOT+".INBOX", msgs, getUidValidity(USER_MAILBOX_ROOT+".INBOX"));
        assertEquals(sc.getRecentCount(),5);
        verifyCommand(sc);
        
        // Recent flag is reset by select
        for (int i=0;i<msgs.length;i++) {
            msgs[i].setFlag(Flag.RECENT, false);
        }
        
        FetchCommand fc=new FetchCommand(msgs,1,-1);
        fc.setUids(uids);
        fc.setFetchFlags(true);
        verifyCommand(fc);
        freeFolder(USER_MAILBOX_ROOT+".INBOX");

    }
    
    public void testRfc822Size() throws ProtocolException, IOException, MessagingException, MailboxManagerException {
        verifyCommand(new LoginCommand(USER_NAME,USER_PASSWORD));
        verifyCommand(new SelectCommand(USER_MAILBOX_ROOT+".INBOX", msgs, getUidValidity(USER_MAILBOX_ROOT+".INBOX")));
        msgs=getMessages(USER_MAILBOX_ROOT+".INBOX");
        FetchCommand fc=new FetchCommand(msgs,1,-1);
        fc.setFetchRfc822Size(true);
        verifyCommand(fc);
    }
    
}