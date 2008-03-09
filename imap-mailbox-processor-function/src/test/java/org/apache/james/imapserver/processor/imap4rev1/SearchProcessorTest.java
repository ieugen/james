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

import java.util.ArrayList;
import java.util.List;

import javax.mail.Flags.Flag;

import org.apache.james.api.imap.ImapCommand;
import org.apache.james.api.imap.display.HumanReadableTextKey;
import org.apache.james.api.imap.message.IdRange;
import org.apache.james.api.imap.message.request.DayMonthYear;
import org.apache.james.api.imap.message.request.SearchKey;
import org.apache.james.api.imap.message.response.imap4rev1.StatusResponse;
import org.apache.james.api.imap.message.response.imap4rev1.StatusResponseFactory;
import org.apache.james.api.imap.process.ImapProcessor;
import org.apache.james.api.imap.process.ImapSession;
import org.apache.james.api.imap.process.SelectedImapMailbox;
import org.apache.james.api.imap.process.ImapProcessor.Responder;
import org.apache.james.imap.message.request.imap4rev1.SearchRequest;
import org.apache.james.imap.message.response.imap4rev1.server.SearchResponse;
import org.apache.james.imapserver.processor.base.ImapSessionUtils;
import org.apache.james.mailboxmanager.MailboxSession;
import org.apache.james.mailboxmanager.SearchQuery;
import org.apache.james.mailboxmanager.impl.FetchGroupImpl;
import org.apache.james.mailboxmanager.mailbox.ImapMailbox;
import org.apache.mailet.RFC2822Headers;
import org.jmock.Mock;
import org.jmock.MockObjectTestCase;

public class SearchProcessorTest extends MockObjectTestCase {
    private static final int DAY = 6;
    private static final int MONTH = 6;
    private static final int YEAR = 1944;
    private static final DayMonthYear DAY_MONTH_YEAR = new DayMonthYear(DAY, MONTH, YEAR);
    private static final long SIZE = 1729;
    private static final String KEYWORD = "BD3";
    private static final long[] EMPTY = {};
    private static final String TAG = "TAG";
    private static final String ADDRESS = "John Smith <john@example.org>";
    private static final String SUBJECT = "Myriad Harbour";
    private static final IdRange[] IDS = {new IdRange(1), new IdRange(42, 1048)};
    private static final SearchQuery.NumericRange[] RANGES = {new SearchQuery.NumericRange(1), new SearchQuery.NumericRange(42, 1048)};
    
    SearchProcessor processor;
    Mock next;
    Mock responder;
    Mock result;
    Mock session;
    Mock command;
    Mock serverResponseFactory;
    Mock statusResponse;
    Mock mailbox;
    ImapCommand imapCommand;
    ImapProcessor.Responder responderImpl;
    
    protected void setUp() throws Exception {
        super.setUp();
        serverResponseFactory = mock(StatusResponseFactory.class);
        session = mock(ImapSession.class);
        command = mock(ImapCommand.class);
        imapCommand = (ImapCommand) command.proxy();
        next = mock(ImapProcessor.class);
        responder = mock(ImapProcessor.Responder.class);
        statusResponse = mock(StatusResponse.class);
        responderImpl = (ImapProcessor.Responder) responder.proxy();
        mailbox = mock(ImapMailbox.class);
        processor = new SearchProcessor((ImapProcessor) next.proxy(),
                (StatusResponseFactory) serverResponseFactory.proxy());
        expectOk();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testSequenceSetLowerUnlimited() throws Exception {
        final IdRange[] ids = {new IdRange(Long.MAX_VALUE, 1729)};
        final SearchQuery.NumericRange[] ranges = {new SearchQuery.NumericRange(Long.MAX_VALUE, 1729L)};
        Mock selectedMailbox = mock(SelectedImapMailbox.class);
        selectedMailbox.expects(once()).method("uid").with(eq(1729)).will(returnValue(1729L));
        session.expects(once()).method("getSelected").will(returnValue(selectedMailbox.proxy()));
        check(SearchKey.buildSequenceSet(ids), SearchQuery.uid(ranges));
    }
    
    public void testSequenceSetUpperUnlimited() throws Exception {
        final IdRange[] ids = {new IdRange(1, Long.MAX_VALUE)};
        final SearchQuery.NumericRange[] ranges = {new SearchQuery.NumericRange(42, Long.MAX_VALUE)};
        Mock selectedMailbox = mock(SelectedImapMailbox.class);
        selectedMailbox.expects(once()).method("uid").with(eq(1)).will(returnValue(42L));
        session.expects(once()).method("getSelected").will(returnValue(selectedMailbox.proxy()));
        check(SearchKey.buildSequenceSet(ids), SearchQuery.uid(ranges));
    }

    public void testSequenceSetMsnRange() throws Exception {
        final IdRange[] ids = {new IdRange(1, 5)};
        final SearchQuery.NumericRange[] ranges = {new SearchQuery.NumericRange(42, 1729)};
        Mock selectedMailbox = mock(SelectedImapMailbox.class);
        selectedMailbox.expects(once()).method("uid").with(eq(1)).will(returnValue(42L));
        selectedMailbox.expects(once()).method("uid").with(eq(5)).will(returnValue(1729L));
        session.expects(once()).method("getSelected").will(returnValue(selectedMailbox.proxy()));
        check(SearchKey.buildSequenceSet(ids), SearchQuery.uid(ranges));
    }
    
    public void testSequenceSetSingleMsn() throws Exception {
        final IdRange[] ids = {new IdRange(1)};
        final SearchQuery.NumericRange[] ranges = {new SearchQuery.NumericRange(42)};
        Mock selectedMailbox = mock(SelectedImapMailbox.class);
        selectedMailbox.expects(exactly(2)).method("uid").with(eq(1)).will(returnValue(42L));
        session.expects(once()).method("getSelected").will(returnValue(selectedMailbox.proxy()));
        check(SearchKey.buildSequenceSet(ids), SearchQuery.uid(ranges));
    }

    public void testALL() throws Exception {
        check(SearchKey.buildAll(), SearchQuery.all());
    }

    public void testANSWERED() throws Exception {
        check(SearchKey.buildAnswered(), SearchQuery.flagIsSet(Flag.ANSWERED));
    }

    public void testBCC() throws Exception {
        check(SearchKey.buildBcc(ADDRESS), SearchQuery.headerContains(RFC2822Headers.BCC, ADDRESS));
    }

    public void testBEFORE() throws Exception {
        check(SearchKey.buildBefore(DAY_MONTH_YEAR), SearchQuery.internalDateBefore(DAY, MONTH, YEAR));
    }

    public void testBODY() throws Exception {
        check(SearchKey.buildBody(SUBJECT), SearchQuery.bodyContains(SUBJECT));
    }

    public void testCC() throws Exception {
        check(SearchKey.buildCc(ADDRESS), SearchQuery.headerContains(RFC2822Headers.CC, ADDRESS));
    }

    public void testDELETED() throws Exception {
        check(SearchKey.buildDeleted(), SearchQuery.flagIsSet(Flag.DELETED));
    }

    public void testDRAFT() throws Exception {
        check(SearchKey.buildDraft(), SearchQuery.flagIsSet(Flag.DRAFT));
    }

    public void testFLAGGED() throws Exception {
        check(SearchKey.buildFlagged(), SearchQuery.flagIsSet(Flag.FLAGGED));
    }
    

    public void testFROM() throws Exception {
        check(SearchKey.buildFrom(ADDRESS), SearchQuery.headerContains(RFC2822Headers.FROM, ADDRESS));
    }

    public void testHEADER () throws Exception {
        check(SearchKey.buildHeader(RFC2822Headers.IN_REPLY_TO, ADDRESS), SearchQuery.headerContains(RFC2822Headers.IN_REPLY_TO, ADDRESS));
    }

    public void testKEYWORD() throws Exception {
        check(SearchKey.buildKeyword(KEYWORD), SearchQuery.flagIsSet(KEYWORD));
    }

    public void testLARGER() throws Exception {
        check(SearchKey.buildLarger(SIZE), SearchQuery.sizeGreaterThan(SIZE));
    }

    public void testNEW() throws Exception {
        check(SearchKey.buildNew(), 
                SearchQuery.and(SearchQuery.flagIsSet(Flag.RECENT), SearchQuery.flagIsUnSet(Flag.SEEN)));
    }

    public void testNOT() throws Exception {
        check(SearchKey.buildNot(SearchKey.buildOn(DAY_MONTH_YEAR)), 
                SearchQuery.not(SearchQuery.internalDateOn(DAY, MONTH, YEAR)));   
    }

    public void testOLD() throws Exception {
        check(SearchKey.buildOld(), SearchQuery.flagIsUnSet(Flag.RECENT));
    }

    public void testON() throws Exception {
        check(SearchKey.buildOn(DAY_MONTH_YEAR), SearchQuery.internalDateOn(DAY, MONTH, YEAR));
    }

    public void testAND() throws Exception {
        List keys = new ArrayList();
        keys.add(SearchKey.buildOn(DAY_MONTH_YEAR));
        keys.add(SearchKey.buildOld());
        keys.add(SearchKey.buildLarger(SIZE));
        List criteria = new ArrayList();
        criteria.add(SearchQuery.internalDateOn(DAY, MONTH, YEAR));
        criteria.add(SearchQuery.flagIsUnSet(Flag.RECENT));
        criteria.add(SearchQuery.sizeGreaterThan(SIZE));
        check(SearchKey.buildAnd(keys), SearchQuery.and(criteria));
    }
    
    public void testOR() throws Exception {
        check(SearchKey.buildOr(SearchKey.buildOn(DAY_MONTH_YEAR), SearchKey.buildOld()), 
                SearchQuery.or(SearchQuery.internalDateOn(DAY, MONTH, YEAR), SearchQuery.flagIsUnSet(Flag.RECENT)));
    }

    public void testRECENT() throws Exception {
        check(SearchKey.buildRecent(), SearchQuery.flagIsSet(Flag.RECENT));
    }

    public void testSEEN() throws Exception {
        check(SearchKey.buildSeen(), SearchQuery.flagIsSet(Flag.SEEN));
    }

    public void testSENTBEFORE() throws Exception {
        check(SearchKey.buildSentBefore(DAY_MONTH_YEAR), SearchQuery.headerDateBefore(RFC2822Headers.DATE, DAY, MONTH, YEAR));
    }

    public void testSENTON() throws Exception {
        check(SearchKey.buildSentOn(DAY_MONTH_YEAR), SearchQuery.headerDateOn(RFC2822Headers.DATE, DAY, MONTH, YEAR));
    }

    public void testSENTSINCE() throws Exception {
        check(SearchKey.buildSentSince(DAY_MONTH_YEAR), SearchQuery.headerDateAfter(RFC2822Headers.DATE, DAY, MONTH, YEAR));
    }

    public void testSINCE() throws Exception {
        check(SearchKey.buildSince(DAY_MONTH_YEAR), SearchQuery.internalDateAfter(DAY, MONTH, YEAR));
    }

    public void testSMALLER() throws Exception {
        check(SearchKey.buildSmaller(SIZE), SearchQuery.sizeLessThan(SIZE));        
    }

    public void testSUBJECT() throws Exception {
        check(SearchKey.buildSubject(SUBJECT), SearchQuery.headerContains(RFC2822Headers.SUBJECT, SUBJECT));
    }

    public void testTEXT() throws Exception {
        check(SearchKey.buildText(SUBJECT), SearchQuery.mailContains(SUBJECT));
    }

    public void testTO () throws Exception {
        check(SearchKey.buildTo(ADDRESS), SearchQuery.headerContains(RFC2822Headers.TO, ADDRESS));
    }

    public void testUID() throws Exception {
        check(SearchKey.buildUidSet(IDS), SearchQuery.uid(RANGES));
    }

    public void testUNANSWERED() throws Exception {
        check(SearchKey.buildUnanswered(), SearchQuery.flagIsUnSet(Flag.ANSWERED));
    }

    public void testUNDELETED() throws Exception {
        check(SearchKey.buildUndeleted(), SearchQuery.flagIsUnSet(Flag.DELETED));
    }

    public void testUNDRAFT() throws Exception {
        check(SearchKey.buildUndraft(), SearchQuery.flagIsUnSet(Flag.DRAFT));
    }

    public void testUNFLAGGED() throws Exception {
        check(SearchKey.buildUnflagged(), SearchQuery.flagIsUnSet(Flag.FLAGGED));
    }

    public void testUNKEYWORD() throws Exception {
        check(SearchKey.buildUnkeyword(KEYWORD), SearchQuery.flagIsUnSet(KEYWORD));
    }

    public void testUNSEEN() throws Exception {
        check(SearchKey.buildUnseen(), SearchQuery.flagIsUnSet(Flag.SEEN));
    }
    
    private void check(SearchKey key, SearchQuery.Criterion criterion) throws Exception {
        SearchQuery query = new SearchQuery();
        query.andCriteria(criterion);
        check(key, query);
    }
    
    private void check(SearchKey key, SearchQuery query) throws Exception {
        MailboxSession mailboxSession = (MailboxSession) mock(MailboxSession.class).proxy();
        session.expects(once()).method("getAttribute")
            .with(eq(ImapSessionUtils.MAILBOX_SESSION_ATTRIBUTE_SESSION_KEY))
            .will(returnValue((MailboxSession) mailboxSession));
        session.expects(once()).method("getAttribute")
            .with(eq(ImapSessionUtils.SELECTED_MAILBOX_ATTRIBUTE_SESSION_KEY))
            .will(returnValue((ImapMailbox) mailbox.proxy()));
        session.expects(once()).method("unsolicitedResponses").with(eq(true), eq(false)).will(returnValue(new ArrayList()));
        mailbox.expects(once()).method("search")
            .with(eq(query), eq(FetchGroupImpl.MINIMAL), eq(mailboxSession))
            .will(returnValue(new ArrayList().iterator()));
        responder.expects(once()).method("respond").with(eq(new SearchResponse(EMPTY)));
        SearchRequest message = new SearchRequest(imapCommand, key, false, TAG);
        processor.doProcess(message, (ImapSession) session.proxy(), TAG, (ImapCommand) command.proxy(), 
                (Responder) responder.proxy());
    }
    
    private void expectOk() {
        StatusResponse response = (StatusResponse) statusResponse.proxy();
        serverResponseFactory.expects(once()).method("taggedOk")
            .with(eq(TAG), same(imapCommand), eq(HumanReadableTextKey.COMPLETED))
                .will(returnValue(response));
        responder.expects(once()).method("respond").with(same(response));
    }
}