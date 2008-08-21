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

package org.apache.james.mailboxmanager.torque;

import java.util.Date;
import java.util.List;

import javax.mail.Flags;
import javax.mail.internet.MimeMessage;

import org.apache.commons.collections.IteratorUtils;
import org.apache.james.mailboxmanager.MessageResult;
import org.apache.james.mailboxmanager.TestUtil;
import org.apache.james.mailboxmanager.impl.FetchGroupImpl;
import org.apache.james.mailboxmanager.impl.MessageRangeImpl;
import org.apache.james.mailboxmanager.impl.MailboxListenerCollector;
import org.apache.james.mailboxmanager.mailbox.Mailbox;
import org.apache.james.mailboxmanager.torque.om.MailboxRow;
import org.apache.james.mailboxmanager.torque.om.MailboxRowPeer;
import org.apache.james.mailboxmanager.torque.om.MessageRow;
import org.apache.james.mailboxmanager.torque.om.MessageRowPeer;
import org.apache.torque.TorqueException;
import org.apache.torque.util.Criteria;

import EDU.oswego.cs.dl.util.concurrent.WriterPreferenceReadWriteLock;

public class TorqueMailboxTest extends AbstractTorqueTestCase {

    public TorqueMailboxTest() throws TorqueException {
        super();
    }

    public void testAppendGetDeleteMessage() throws Exception {
        MailboxRow mr = new MailboxRow("#users.tuser.INBOX", 100);
        mr.save();
        mr=MailboxRowPeer.retrieveByName("#users.tuser.INBOX");
        Mailbox torqueMailbox = new TorqueMailbox(mr, new WriterPreferenceReadWriteLock(),null);
        torqueMailbox.addListener(new MailboxListenerCollector());
        assertEquals(0,torqueMailbox.getMessageCount(session));
        
        long time = System.currentTimeMillis();
        time = time - (time % 1000);
        Date date = new Date(time);
        MimeMessage mm=TestUtil.createMessage();
        Flags flags=new Flags();
        flags.add(Flags.Flag.ANSWERED);
        flags.add(Flags.Flag.SEEN);
        mm.setFlags(flags,true);
        mm.writeTo(System.out);
        torqueMailbox.appendMessage(mm, date, FetchGroupImpl.MINIMAL, session);
        assertEquals(1,torqueMailbox.getMessageCount(session));
        List l = MessageRowPeer.doSelect(new Criteria());
        assertEquals(1, l.size());
        MessageRow msg = (MessageRow) l.get(0);
        assertEquals(mr.getMailboxId(), msg.getMailboxId());
        assertEquals(1, msg.getUid());

        assertEquals(date, msg.getInternalDate());
        assertEquals(flags, msg.getMessageFlags().getFlagsObject());

        mr = MailboxRowPeer.retrieveByPK(mr.getMailboxId());
        assertEquals(1, mr.getLastUid());
        
        Flags f=new Flags();
        f.add(Flags.Flag.DELETED);
        torqueMailbox.setFlags(f,true,false, MessageRangeImpl.oneUid(1l), FetchGroupImpl.MINIMAL, session);
        List messageResults=IteratorUtils.toList(torqueMailbox.expunge(MessageRangeImpl.all(),FetchGroupImpl.MINIMAL, session));
        assertEquals(1,messageResults.size());
        assertEquals(1l,((MessageResult)messageResults.get(0)).getUid());
    }

}