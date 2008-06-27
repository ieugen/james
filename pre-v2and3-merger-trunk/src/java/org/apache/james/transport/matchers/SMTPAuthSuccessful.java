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

package org.apache.james.transport.matchers;

import java.util.Collection;

import org.apache.mailet.GenericMatcher;
import org.apache.mailet.Mail;

/**
 * <P>Matches mails that are sent by an SMTP authenticated user.</P>
 * <P>If the sender was not authenticated it will not match.</P>
 * <PRE><CODE>
 * &lt;mailet match=&quot;SMTPAuthSuccessful&quot; class=&quot;&lt;any-class&gt;&quot;&gt;
 * </CODE></PRE>
 *
 * @version CVS $Revision: 1.4 $ $Date: 2004/01/30 02:22:14 $
 * @since 2.2.0
 */
public class SMTPAuthSuccessful extends GenericMatcher {
    
    /**
     * The mail attribute holding the SMTP AUTH user name, if any.
     */
    private final static String SMTP_AUTH_USER_ATTRIBUTE_NAME = "org.apache.james.SMTPAuthUser";
    
    public Collection match(Mail mail) {
        String authUser = (String) mail.getAttribute(SMTP_AUTH_USER_ATTRIBUTE_NAME);
        if (authUser != null) {
            return mail.getRecipients();
        } else {
            return null;
        }
    }
}