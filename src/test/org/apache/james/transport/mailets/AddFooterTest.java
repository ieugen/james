/***********************************************************************
 * Copyright (c) 1999-2006 The Apache Software Foundation.             *
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
package org.apache.james.transport.mailets;

import org.apache.james.core.MailImpl;
import org.apache.james.core.MailetConfigImpl;
import org.apache.james.test.mock.mailet.MockMailContext;
import org.apache.mailet.Mail;
import org.apache.mailet.Mailet;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import junit.framework.TestCase;

/**
 * Test encoding issues
 * 
 * This test should also be run with the following JVM options to be sure it tests:
 * "-Dfile.encoding=ASCII -Dmail.mime.charset=ANSI_X3.4-1968"
 */
public class AddFooterTest extends TestCase {

    public AddFooterTest(String arg0) throws UnsupportedEncodingException {
        super(arg0);
        
        /*
        
        String encoding = (new InputStreamReader(System.in)).getEncoding();
        System.out.println("System Encoding: "+encoding);
        System.out.println("Default Java Charset:"+MimeUtility.getDefaultJavaCharset());
        System.out.println("---------");
        String a = "\u20AC\u00E0"; // euro char followed by an italian a with an accent System.out.println(debugString(a,"UTF-8"));
        System.out.println(debugString(a,"UTF8"));
        System.out.println(debugString(a,"UTF-16"));
        System.out.println(debugString(a,"UNICODE"));
        System.out.println(debugString(a,"ISO-8859-15"));
        System.out.println(debugString(a,"ISO-8859-1"));
        System.out.println(debugString(a,"CP1252"));
        System.out.println(debugString(a,"ANSI_X3.4-1968"));
         
         */
    }

    private final static char[] hexchars = { '0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    public String debugString(String a, String charset)
            throws UnsupportedEncodingException {
        byte[] bytes = a.getBytes(charset);
        StringBuffer res = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            if (i > 0)
                res.append("-");
            res.append(hexchars[((bytes[i] + 256) % 256) / 16]);
            res.append(hexchars[((bytes[i] + 256) % 256) % 16]);
        }
        res.append(" (");
        res.append(MimeUtility.mimeCharset(charset));
        res.append(" / ");
        res.append(MimeUtility.javaCharset(charset));
        res.append(")");
        return res.toString();
    }

    /*
     * Class under test for String getSubject()
     */
    public void testAddFooterTextPlain() throws MessagingException, IOException {

        // quoted printable mimemessage text/plain
        String asciisource = "Subject: test\r\nContent-Type: text/plain; charset=ISO-8859-15\r\nMIME-Version: 1.0\r\nContent-Transfer-Encoding: quoted-printable\r\n\r\nTest=E0 and one\r\n";

        String iso885915qpheader = "------ my footer =E0/=A4 ------";
        String footer = "------ my footer \u00E0/\u20AC ------";

        String res = processAddFooter(asciisource, footer);

        assertEquals(asciisource + iso885915qpheader, res);

    }

    /*
     * Class under test for String getSubject()
     */
    public void testAddFooterTextPlainCP1252toISO8859() throws MessagingException, IOException {

        // quoted printable mimemessage text/plain
        String asciisource = "Subject: test\r\nContent-Type: text/plain; charset=CP1252\r\nMIME-Version: 1.0\r\nContent-Transfer-Encoding: quoted-printable\r\n\r\nTest=E0 and one =80\r\n";

        String iso885915qpheader = "------ my footer =E0/=80 ------";
        String footer = "------ my footer \u00E0/\u20AC ------";

        String res = processAddFooter(asciisource, footer);

        assertEquals(asciisource + iso885915qpheader, res);

    }

    /*
     * Class under test for String getSubject()
     */
    public void testAddFooterMultipartAlternative() throws MessagingException,
            IOException {

        String sep = "--==--";
        String head = "Subject: test\r\nContent-Type: multipart/alternative;\r\n    boundary=\""
                + sep
                + "\"\r\nMIME-Version: 1.0\r\n";
        String content1 = "Content-Type: text/plain;\r\n    charset=\"ISO-8859-15\"\r\nContent-Transfer-Encoding: quoted-printable\r\n\r\nTest=E0 and @=80";
        String c2h = "Content-Type: text/html;\r\n    charset=\"CP1252\"\r\nContent-Transfer-Encoding: quoted-printable\r\n\r\n";
        String c2pre = "<html><body>test =80 ss";
        String c2post = "</body></html>";

        StringBuffer asciisource = new StringBuffer();
        asciisource.append(head);
        asciisource.append("\r\n--");
        asciisource.append(sep);
        asciisource.append("\r\n");
        asciisource.append(content1);
        asciisource.append("\r\n--");
        asciisource.append(sep);
        asciisource.append("\r\n");
        asciisource.append(c2h);
        asciisource.append(c2pre);
        asciisource.append(c2post);
        asciisource.append("\r\n--");
        asciisource.append(sep);
        asciisource.append("--\r\n");

        String iso885915qpheader = "------ my footer =E0/=A4 ------";
        String cp1252qpfooter = "------ my footer =E0/=80 ------";
        String footer = "------ my footer \u00E0/\u20AC ------";

        StringBuffer expected = new StringBuffer();
        expected.append(head);
        expected.append("\r\n--");
        expected.append(sep);
        expected.append("\r\n");
        expected.append(content1);
        expected.append("\r\n");
        expected.append(iso885915qpheader);
        expected.append("\r\n--");
        expected.append(sep);
        expected.append("\r\n");
        expected.append(c2h);
        expected.append(c2pre);
        expected.append("<br>");
        expected.append(cp1252qpfooter);
        expected.append(c2post);
        expected.append("\r\n--");
        expected.append(sep);
        expected.append("--\r\n");
        
        String res = processAddFooter(asciisource.toString(), footer);

        assertEquals(expected.toString(), res);

    }

    private String processAddFooter(String asciisource, String footer)
            throws MessagingException, IOException {
        Mailet mailet = new AddFooter() {
            private String footer;

            public String getInitParameter(String name) {
                if ("text".equals(name)) {
                    return footer;
                }
                return null;
            }

            public Mailet setFooter(String string) {
                this.footer = string;
                return this;
            };
        }.setFooter(footer);

        MailetConfigImpl mci = new MailetConfigImpl();
        mci.setMailetContext(new MockMailContext());

        mailet.init(mci);

        Mail mail = new MailImpl(new MimeMessage(Session
                .getDefaultInstance(new Properties()),
                new ByteArrayInputStream(asciisource.getBytes())));

        mailet.service(mail);

        ByteArrayOutputStream rawMessage = new ByteArrayOutputStream();
        mail.getMessage().writeTo(
                rawMessage,
                new String[] { "Bcc", "Content-Length", "Message-ID" });
        String res = rawMessage.toString();
        return res;
    }

}
