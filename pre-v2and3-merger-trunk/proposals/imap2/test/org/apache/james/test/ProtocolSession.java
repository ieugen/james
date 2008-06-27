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

package org.apache.james.test;

import org.apache.oro.text.perl.Perl5Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A protocol session which can be run against a reader and writer, which checks
 * the server response against the expected values.
 * TODO make ProtocolSession itself be a permissible ProtocolElement,
 * so that we can nest and reuse sessions.
 * @author  Darrell DeBoer <darrell@apache.org>
 *
 * @version $Revision: 1.9 $
 */
public class ProtocolSession
{
    private int maxSessionNumber;
    protected List testElements = new ArrayList();
    private static final Perl5Util perl = new Perl5Util();

    /**
     * Returns the number of sessions required to run this ProtocolSession.
     * If the number of readers and writers provided is less than this number,
     * an exception will occur when running the tests.
     */
    public int getSessionCount() {
        return maxSessionNumber + 1;
    }

    /**
     * Executes the ProtocolSession in real time against the readers and writers
     * supplied, writing client requests and reading server responses in the order
     * that they appear in the test elements. The index of a reader/writer in the array
     * corresponds to the number of the session.
     * If an exception occurs, no more test elements are executed.
     * @param out The client requests are written to here.
     * @param in The server responses are read from here.
     */
    public void runLiveSession(PrintWriter[] out, BufferedReader[] in) throws InvalidServerResponseException {
        for ( Iterator iter = testElements.iterator(); iter.hasNext(); ) {
            Object obj = iter.next();
            if ( obj instanceof ProtocolElement ) {
                ProtocolElement test = ( ProtocolElement ) obj;
                test.testProtocol( out, in );
            }
        }
    }

    /**
     * adds a new Client request line to the test elements
     */
    public void CL( String clientLine )
    {
        testElements.add( new ClientRequest( clientLine ) );
    }

    /**
     * adds a new Server Response line to the test elements, with the specified location.
     */
    public void SL( String serverLine, String location )
    {
        testElements.add( new ServerResponse( serverLine, location ) );
    }

    /**
     * adds a new Server Unordered Block to the test elements.
     */
    public void SUB( List serverLines, String location )
    {
        testElements.add( new ServerUnorderedBlockResponse( serverLines, location ) );
    }

    /**
     * adds a new Client request line to the test elements
     */
    public void CL( int sessionNumber, String clientLine )
    {
        this.maxSessionNumber = Math.max(this.maxSessionNumber, sessionNumber);
        testElements.add( new ClientRequest( sessionNumber, clientLine ) );
    }

    /**
     * adds a new Server Response line to the test elements, with the specified location.
     */
    public void SL( int sessionNumber, String serverLine, String location )
    {
        this.maxSessionNumber = Math.max(this.maxSessionNumber, sessionNumber);
        testElements.add( new ServerResponse( sessionNumber, serverLine, location ) );
    }

    /**
     * adds a new Server Unordered Block to the test elements.
     */
    public void SUB( int sessionNumber, List serverLines, String location )
    {
        this.maxSessionNumber = Math.max(this.maxSessionNumber, sessionNumber);
        testElements.add( new ServerUnorderedBlockResponse( sessionNumber, serverLines, location ) );
    }

    /**
     * A client request, which write the specified message to a Writer.
     */
    private class ClientRequest implements ProtocolElement
    {
        private int sessionNumber;
        private String message;

        /**
         * Initialises the ClientRequest with the supplied message.
         */
        public ClientRequest( String message )
        {
            this(-1, message);
        }

        /**
         * Initialises the ClientRequest, with a message and session number.
         * @param sessionNumber
         * @param message
         */
        public ClientRequest(int sessionNumber, String message) {
            this.sessionNumber = sessionNumber;
            this.message = message;
        }

        /**
         * Writes the request message to the PrintWriters. If the sessionNumber == -1,
         * the request is written to *all* supplied writers, otherwise, only the
         * writer for this session is writted to.
         */
        public void testProtocol( PrintWriter[] out, BufferedReader[] in )
        {
            if (sessionNumber < 0) {
                for (int i = 0; i < out.length; i++) {
                    PrintWriter printWriter = out[i];
                    writeMessage(printWriter);
                }
            }
            else {
                PrintWriter writer = out[sessionNumber];
                writeMessage(writer);
            }
        }

        private void writeMessage(PrintWriter writer) {
            writer.write(message);
            writer.write('\r');
            writer.write('\n');
            writer.flush();
        }
    }

    /**
     * Represents a single-line server response, which reads a line
     * from a reader, and compares it with the defined regular expression
     * definition of this line.
     */
    private class ServerResponse implements ProtocolElement
    {
        private int sessionNumber;
        private String expectedLine;
        protected String location;

        /**
         * Sets up a server response.
         * @param expectedPattern A Perl regular expression pattern used to test
         *                        the line recieved.
         * @param location A descriptive value to use in error messages.
         */
        public ServerResponse( String expectedPattern, String location )
        {
            this(-1, expectedPattern, location);
        }

        /**
         * Sets up a server response.
         * @param sessionNumber The number of session for a multi-session test
         * @param expectedPattern A Perl regular expression pattern used to test
         *                        the line recieved.
         * @param location A descriptive value to use in error messages.
         */
        public ServerResponse( int sessionNumber, String expectedPattern, String location )
        {
            this.sessionNumber = sessionNumber;
            this.expectedLine = expectedPattern;
            this.location = location;
        }

        /**
         * Reads a line from the supplied reader, and tests that it matches
         * the expected regular expression. If the sessionNumber == -1, then all
         * readers are tested, otherwise, only the reader for this session is tested.
         * @param out Is ignored.
         * @param in The server response is read from here.
         * @throws InvalidServerResponseException If the actual server response didn't
         *          match the regular expression expected.
         */
        public void testProtocol( PrintWriter[] out, BufferedReader[] in )
                throws InvalidServerResponseException
        {
            if (sessionNumber < 0) {
                for (int i = 0; i < in.length; i++) {
                    BufferedReader reader = in[i];
                    checkResponse(reader);
                }
            }
            else {
                BufferedReader reader = in[sessionNumber];
                checkResponse(reader);
            }
        }

        protected void checkResponse(BufferedReader reader) throws InvalidServerResponseException {
            String testLine = readLine(reader);
            if ( ! match( expectedLine, testLine ) ) {
                String errMsg = "\nLocation: " + location +
                        "\nExcpected: " + expectedLine +
                        "\nActual   : " + testLine;
                throw new InvalidServerResponseException( errMsg );
            }
        }

        /**
         * A convenience method which returns true if the actual string
         * matches the expected regular expression.
         * @param expected The regular expression used for matching.
         * @param actual The actual message to match.
         * @return <code>true</code> if the actual matches the expected.
         */
        protected boolean match( String expected, String actual )
        {
            String pattern = "m/" + expected + "/";
            return perl.match( pattern, actual );
        }

        /**
         * Grabs a line from the server and throws an error message if it
         * doesn't work out
         * @param in BufferedReader for getting the server response
         * @return String of the line from the server
         */
        protected String readLine( BufferedReader in )
                throws InvalidServerResponseException
        {
            try {
                return in.readLine();
            } catch (IOException e) {
                String errMsg = "\nLocation: " + location +
                                "\nExpected: " + expectedLine +
                                "\nReason: Server Timeout.";
                throw new InvalidServerResponseException(errMsg);
            }
        }
    }

    /**
     * Represents a set of lines which must be recieved from the server,
     * in a non-specified order.
     */
    private class ServerUnorderedBlockResponse extends ServerResponse
    {
        private List expectedLines = new ArrayList();

        /**
         * Sets up a ServerUnorderedBlockResponse with the list of expected lines.
         * @param expectedLines A list containing a reqular expression for each
         *                      expected line.
         * @param location A descriptive location string for error messages.
         */
        public ServerUnorderedBlockResponse( List expectedLines, String location )
        {
            this(-1, expectedLines, location);
        }

        /**
         * Sets up a ServerUnorderedBlockResponse with the list of expected lines.
         * @param sessionNumber The number of the session to expect this block,
         *              for a multi-session test.
         * @param expectedLines A list containing a reqular expression for each
         *                      expected line.
         * @param location A descriptive location string for error messages.
         */
        public ServerUnorderedBlockResponse( int sessionNumber,
                                             List expectedLines, String location )
        {
            super( sessionNumber, "<Unordered Block>", location );
            this.expectedLines = expectedLines;
        }

        /**
         * Reads lines from the server response and matches them against the
         * list of expected regular expressions. Each regular expression in the
         * expected list must be matched by only one server response line.
         * @param reader Server responses are read from here.
         * @throws InvalidServerResponseException If a line is encountered which doesn't
         *              match one of the expected lines.
         */
        protected void checkResponse(BufferedReader reader) throws InvalidServerResponseException {
            List testLines = new ArrayList(expectedLines);
            while (testLines.size() > 0) {
                String actualLine = readLine(reader);

                boolean foundMatch = false;
                for (int i = 0; i < testLines.size(); i++) {
                    String expected = (String) testLines.get(i);
                    if (match(expected, actualLine)) {
                        foundMatch = true;
                        testLines.remove(expected);
                        break;
                    }
                }

                if (!foundMatch) {
                    StringBuffer errMsg = new StringBuffer()
                            .append("\nLocation: ")
                            .append(location)
                            .append("\nExpected one of: ");
                    Iterator iter = expectedLines.iterator();
                    while (iter.hasNext()) {
                        errMsg.append("\n    ");
                        errMsg.append(iter.next());
                    }
                    errMsg.append("\nActual: ")
                            .append(actualLine);

                    throw new InvalidServerResponseException(errMsg.toString());
                }
            }
        }
    }

    /**
     * Represents a generic protocol element, which may write requests to the server,
     * read responses from the server, or both. Implementations should test the server
     * response against an expected response, and throw an exception on mismatch.
     */
    private interface ProtocolElement
    {
        /**
         * Executes the ProtocolElement against the supplied read and writer.
         * @param out Client requests are written to here.
         * @param in Server responses are read from here.
         * @throws InvalidServerResponseException If the actual server response
         *              doesn't match the one expected.
         */
        void testProtocol( PrintWriter[] out, BufferedReader[] in )
                throws InvalidServerResponseException;
    }

    /**
     * An exception which is thrown when the actual response from a server
     * is different from that expected.
     */
    public class InvalidServerResponseException extends Exception
    {
        public InvalidServerResponseException( String message )
        {
            super( message );
        }
    }

}