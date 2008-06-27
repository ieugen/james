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

import org.apache.james.imapserver.AccessControlException;
import org.apache.james.imapserver.AuthorizationException;
import org.apache.james.imapserver.ACLMailbox;
import org.apache.james.imapserver.ImapRequest;
import org.apache.james.imapserver.ImapSession;

class MyRightsCommand extends AbstractAclCommand
{
    private static final String BAD_MYRIGHTS_MSG
            = "BAD Command should be <tag> <MYRIGHTS> <mailbox>";
        
    protected boolean checkUsage( int arguments, ImapSession session )
    {
        if ( arguments != 3 ) {
            session.taggedResponse( BAD_MYRIGHTS_MSG );
            return false;
        }
        return true;
    }

    protected void doAclCommand( ImapRequest request, ImapSession session,
                                 ACLMailbox target, String folder )
            throws AccessControlException, AuthorizationException
    {
        String command = request.getCommand();
        session.untaggedResponse( command + SP
                                   + target.getName() + SP
                                   + target.getRights( session.getCurrentUser(), session.getCurrentUser() ) );
        session.okResponse( command );
    }

}