/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */
package org.apache.james.transport.matchers;

import org.apache.mailet.*;
import org.apache.oro.text.regex.*;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;

/**
 * This is based on a sample filter.cfg for a Netscape Mail Server to stop
 * spam.
 *
 * @author  Serge Knystautas <sergek@lokitech.com>
 */
public class NESSpamCheck extends GenericMatcher {

    protected Perl5Matcher matcher;
    protected Object patterns[][] = {{"Received", "GAA.*-0600.*EST"},
            {"Received", "XAA.*-0700.*EDT"},
            {"Received", "xxxxxxxxxxxxxxxxxxxxx"},
            {"Received", "untrace?able"},
            {"Received", "from (baby|bewellnet|kllklk) "},
            {"To", "Friend@public\\.com"},
            {"To", "user@the[-_]internet"},
            {"Date", "/[0-9]+/.+[AP]M.+Time"},
            {"Subject", "^\\(?ADV?[:;)]"},
            {"Message-ID", "<>"},
            {"Message-Id", "<>"},
            {"Message-Id", "<(419\\.43|989\\.28)"},
            {"X-MimeOLE", "MimeOLE V[^0-9]"},
            //Added 20-Jun-1999.  Appears to be broken spamware.
            {"MIME-Version", "1.0From"},
            //Added 28-July-1999.  Check X-Mailer for spamware.
            {"X-Mailer", "DiffondiCool"},
            {"X-Mailer", "Emailer Platinum"},
            {"X-Mailer", "eMerge"},
            {"X-Mailer", "Crescent Internet Tool"},
            //Added 4-Apr-2000.  Check X-Mailer for Cybercreek Avalanche
            {"X-Mailer", "Avalanche"},
            //Added 21-Oct-1999.  Subject contains 20 or more consecutive spaces
            {"Subject", "                    "},
            //Added 31-Mar-2000.  Invalid headers from MyGuestBook.exe CGI spamware
            {"MessageID", "<.+>"},
            {"X-References", "0[A-Z0-9]+, 0[A-Z0-9]+$"},
            {"X-Other-References", "0[A-Z0-9]+$"},
            {"X-See-Also", "0[A-Z0-9]+$"},
            //Updated 28-Apr-1999.  Check for "Sender", "Resent-From", or "Resent-By"
            // before "X-UIDL".  If found, then exit.
            {"Sender", ".+"},
            {"Resent-From", ".+"},
            {"Resent-By", ".+"},
            //Updated 19-May-1999.  Check for "X-Mozilla-Status" before "X-UIDL".
            {"X-Mozilla-Status", ".+"},
            //Updated 20-Jul-1999.  Check for "X-Mailer: Internet Mail Service"
            // before "X-UIDL".
            {"X-Mailer", "Internet Mail Service"},
            //Updated 25-Oct-1999.  Check for "X-ID" before "X-UIDL".
            {"X-ID", ".+"},
            //X-UIDL is a POP3 header that should normally not be seen
            {"X-UIDL", ".*"},
            //Some headers are valid only for the Pegasus Mail client.  So first check
            //for Pegasus header and exit if found.  If not found, check for
            //invalid headers: "Comments: Authenticated sender", "X-PMFLAGS" and "X-pmrqc".
            {"X-mailer", "Pegasus"},
            //Added 27-Aug-1999.  Pegasus now uses X-Mailer instead of X-mailer.
            {"X-Mailer", "Pegasus"},
            //Added 25-Oct-1999.  Check for X-Confirm-Reading-To.
            {"X-Confirm-Reading-To", ".+"},
            //Check for invalid Pegasus headers
            {"Comments", "Authenticated sender"},
            {"X-PMFLAGS", ".*"},
            {"X-Pmflags", ".*"},
            {"X-pmrqc", ".*"},
            {"Host-From:envonly", ".*"}};

    public void init() {
        //No condition passed... just compile a bunch of regular expressions
        try {
            matcher = new Perl5Matcher();
            for (int i = 0; i < patterns.length; i++) {
                String pattern = (String)patterns[i][1];
                patterns[i][1] = new Perl5Compiler().compile(pattern);
            }
        } catch(MalformedPatternException mp) {
            log(mp.getMessage());
        }
    }

    public Collection match(Mail mail) throws MessagingException {
        MimeMessage message = mail.getMessage();

        //Loop through all the patterns
        for (int i = 0; i < patterns.length; i++) {
            //Get the header name
            String headerName = (String)patterns[i][0];
            //Get the patterns ro that header
            Pattern pattern = (Pattern)patterns[i][1];
            //Get the array of header values that match that
            String headers[] = message.getHeader(headerName);
            //Loop through the header values
            for (int j = 0; j < headers.length; j++) {
                if (matcher.matches(headers[j], pattern)) {
                    return mail.getRecipients();
                }

            }
        }
        return null;
    }
}
