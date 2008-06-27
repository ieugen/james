/***********************************************************************
 * Copyright (c) 2000-2004 The Apache Software Foundation.             *
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

package org.apache.james.imapserver.commands;

import org.apache.james.imapserver.ImapRequestLineReader;
import org.apache.james.imapserver.ImapResponse;
import org.apache.james.imapserver.ImapSession;
import org.apache.james.imapserver.ImapSessionMailbox;
import org.apache.james.imapserver.ProtocolException;
import org.apache.james.imapserver.store.MailboxException;
import org.apache.james.imapserver.store.MailboxListener;
import org.apache.james.imapserver.store.MessageFlags;
import org.apache.james.imapserver.store.SimpleImapMessage;

import javax.mail.Flags;

/**
 * Handles processeing for the STORE imap command.
 *
 * @author  Darrell DeBoer <darrell@apache.org>
 *
 * @version $Revision: 1.9 $
 */
class StoreCommand extends SelectedStateCommand implements UidEnabledCommand
{
    public static final String NAME = "STORE";
    public static final String ARGS = "<Message-set> ['+'|'-']FLAG[.SILENT] <flag-list>";

    private final StoreCommandParser parser = new StoreCommandParser();

    /** @see CommandTemplate#doProcess */
    protected void doProcess( ImapRequestLineReader request,
                              ImapResponse response,
                              ImapSession session )
            throws ProtocolException, MailboxException
    {
        doProcess( request, response, session , false );
    }

    public void doProcess( ImapRequestLineReader request,
                              ImapResponse response,
                              ImapSession session,
                              boolean useUids )
            throws ProtocolException, MailboxException
    {
        IdRange[] idSet = parser.parseIdRange( request );
        StoreDirective directive = parser.storeDirective( request );
        Flags flags = parser.flagList( request );
        parser.endLine( request );

        ImapSessionMailbox mailbox = session.getSelected();
//        IdRange[] uidSet;
//        if (useUids) {
//           uidSet = idSet;
//        } else {
//            uidSet = mailbox.msnsToUids(idSet);
//        }
//        if (directive.getSign() < 0) {
//            mailbox.setFlags(flags, false, uidSet, directive.isSilent());
//        }
//        else if (directive.getSign() > 0) {
//            mailbox.setFlags(flags, true, uidSet, directive.isSilent());
//        }
//        else {
//            mailbox.replaceFlags(flags, uidSet, directive.isSilent());
//        }

        MailboxListener silentListener = null;
        if (directive.isSilent()) {
            silentListener = mailbox;
        }
        
        // TODO do this in one hit.
        long[] uids = mailbox.getMessageUids();
        for ( int i = 0; i < uids.length; i++ ) {
            long uid = uids[i];
            int msn = mailbox.getMsn( uid );

            if ( ( useUids && includes(idSet, uid) ) ||
                 ( !useUids && includes( idSet, msn ) ) )
            {
                if (directive.getSign() < 0) {
                    mailbox.setFlags(flags, false, uid, silentListener, useUids);
                }
                else if (directive.getSign() > 0) {
                    mailbox.setFlags(flags, true, uid, silentListener, useUids);
                }
                else {
                    mailbox.replaceFlags(flags, uid, silentListener, useUids);
                }
            }
        }
        
        boolean omitExpunged = (!useUids);
        session.unsolicitedResponses( response, omitExpunged );
        response.commandComplete( this );
    }

    private void storeFlags( SimpleImapMessage imapMessage, StoreDirective directive, Flags newFlags )
    {
        Flags messageFlags = imapMessage.getFlags();
        if ( directive.getSign() == 0 ) {
            messageFlags.remove(MessageFlags.ALL_FLAGS);
            messageFlags.add(newFlags);
        }
        else if ( directive.getSign() < 0 ) {
            messageFlags.remove(newFlags);
        }
        else if ( directive.getSign() > 0 ) {
            messageFlags.add(newFlags);
        }
    }

    /** @see ImapCommand#getName */
    public String getName()
    {
        return NAME;
    }

    /** @see CommandTemplate#getArgSyntax */
    public String getArgSyntax()
    {
        return ARGS;
    }

    private class StoreCommandParser extends CommandParser
    {
        StoreDirective storeDirective( ImapRequestLineReader request ) throws ProtocolException
        {
            int sign = 0;
            boolean silent = false;

            char next = request.nextWordChar();
            if ( next == '+' ) {
                sign = 1;
                request.consume();
            }
            else if ( next == '-' ) {
                sign = -1;
                request.consume();
            }
            else {
                sign = 0;
            }

            String directive = consumeWord( request, new NoopCharValidator() );
            if ( "FLAGS".equalsIgnoreCase( directive ) ) {
                silent = false;
            }
            else if ( "FLAGS.SILENT".equalsIgnoreCase( directive ) ) {
                silent = true;
            }
            else {
                throw new ProtocolException( "Invalid Store Directive: '" + directive + "'" );
            }
            return new StoreDirective( sign, silent );
        }
    }

    private class StoreDirective
    {
        private int sign;
        private boolean silent;

        public StoreDirective( int sign, boolean silent )
        {
            this.sign = sign;
            this.silent = silent;
        }

        public int getSign()
        {
            return sign;
        }

        public boolean isSilent()
        {
            return silent;
        }
    }
}
/*
6.4.6.  STORE Command

   Arguments:  message set
               message data item name
               value for message data item

   Responses:  untagged responses: FETCH

   Result:     OK - store completed
               NO - store error: can't store that data
               BAD - command unknown or arguments invalid

      The STORE command alters data associated with a message in the
      mailbox.  Normally, STORE will return the updated value of the
      data with an untagged FETCH response.  A suffix of ".SILENT" in
      the data item name prevents the untagged FETCH, and the server
      SHOULD assume that the client has determined the updated value
      itself or does not care about the updated value.

         Note: regardless of whether or not the ".SILENT" suffix was
         used, the server SHOULD send an untagged FETCH response if a
         change to a message's flags from an external source is
         observed.  The intent is that the status of the flags is
         determinate without a race condition.

      The currently defined data items that can be stored are:

      FLAGS <flag list>
                     Replace the flags for the message with the
                     argument.  The new value of the flags are returned
                     as if a FETCH of those flags was done.

      FLAGS.SILENT <flag list>
                     Equivalent to FLAGS, but without returning a new
                     value.

      +FLAGS <flag list>
                     Add the argument to the flags for the message.  The
                     new value of the flags are returned as if a FETCH
                     of those flags was done.

      +FLAGS.SILENT <flag list>
                     Equivalent to +FLAGS, but without returning a new
                     value.

      -FLAGS <flag list>
                     Remove the argument from the flags for the message.
                     The new value of the flags are returned as if a
                     FETCH of those flags was done.

      -FLAGS.SILENT <flag list>
                     Equivalent to -FLAGS, but without returning a new
                     value.

   Example:    C: A003 STORE 2:4 +FLAGS (\Deleted)
               S: * 2 FETCH FLAGS (\Deleted \Seen)
               S: * 3 FETCH FLAGS (\Deleted)
               S: * 4 FETCH FLAGS (\Deleted \Flagged \Seen)
               S: A003 OK STORE completed

*/