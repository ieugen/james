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
package org.apache.james.imapserver.commands;

import org.apache.james.imapserver.ImapRequestLineReader;
import org.apache.james.imapserver.ProtocolException;

class FetchCommandParser extends AbstractUidCommandParser
{
    public FetchCommandParser(ImapCommand command) {
        super(command);
    }

    public FetchRequest fetchRequest( ImapRequestLineReader request )
            throws ProtocolException
    {
        FetchRequest fetch = new FetchRequest();

        char next = nextNonSpaceChar( request );
        if (request.nextChar() == '(') {
            consumeChar( request, '(' );

            next = nextNonSpaceChar( request );
            while ( next != ')' ) {
                addNextElement( request, fetch );
                next = nextNonSpaceChar( request );
            }
            consumeChar(request, ')');
        } else {
            addNextElement( request, fetch );
            
        }

        return fetch;
    }

    private void addNextElement( ImapRequestLineReader command, FetchRequest fetch)
            throws ProtocolException
    {
        /*char next = nextCharInLine( command );
            StringBuffer element = new StringBuffer();
            while ( next != ' ' && next != '[' && next != ')' && next!='\n' && next!='\r' ) {
                element.append(next);
                command.consume();
                next = nextCharInLine( command );
            }*/
         
        
            //String name = element.toString();
            String name = readWord(command, " [)\r\n");
            char next = command.nextChar();
            // Simple elements with no '[]' parameters.
            //if ( next == ' ' || next == ')'  || next == '\n' || next == '\r') {
            if (next != '[') {
                if ( "FAST".equalsIgnoreCase( name ) ) {
                    fetch.flags = true;
                    fetch.internalDate = true;
                    fetch.size = true;
                } else if ("FULL".equalsIgnoreCase(name)) {
                    fetch.flags = true;
                    fetch.internalDate = true;
                    fetch.size = true;
                    fetch.envelope = true;
                    fetch.body = true;
                } else if ("ALL".equalsIgnoreCase(name)) {
                    fetch.flags = true;
                    fetch.internalDate = true;
                    fetch.size = true;
                    fetch.envelope = true;
                } else if ("FLAGS".equalsIgnoreCase(name)) {
                    fetch.flags = true;
                } else if ("RFC822.SIZE".equalsIgnoreCase(name)) {
                    fetch.size = true;
                } else if ("ENVELOPE".equalsIgnoreCase(name)) {
                    fetch.envelope = true;
                } else if ("INTERNALDATE".equalsIgnoreCase(name)) {
                    fetch.internalDate = true;
                } else if ("BODY".equalsIgnoreCase(name)) {
                    fetch.body = true;
                } else if ("BODYSTRUCTURE".equalsIgnoreCase(name)) {
                    fetch.bodyStructure = true;
                } else if ("UID".equalsIgnoreCase(name)) {
                    fetch.uid = true;
                } else if ("RFC822".equalsIgnoreCase(name)) {
                    fetch.add(new BodyFetchElement("RFC822", ""), false);
                } else if ("RFC822.HEADER".equalsIgnoreCase(name)) {
                    fetch.add(new BodyFetchElement("RFC822.HEADER", "HEADER"), true);
                } else if ("RFC822.TEXT".equalsIgnoreCase(name)) {
                    fetch.add(new BodyFetchElement("RFC822.TEXT", "TEXT"), false);
                } else {
                    throw new ProtocolException( "Invalid fetch attribute: " + name );
                }
            }
            else {
                consumeChar( command, '[' );

                
                String parameter = readWord(command, "]");

                consumeChar( command, ']');
                if ( "BODY".equalsIgnoreCase( name ) ) {
                    fetch.add(new BodyFetchElement("BODY[" + parameter + "]", parameter), false);
                } else if ( "BODY.PEEK".equalsIgnoreCase( name ) ) {
                    fetch.add(new BodyFetchElement("BODY[" + parameter + "]", parameter), true);
                } else {
                    throw new ProtocolException( "Invalid fetch attibute: " + name + "[]" );
                }
            }
        }

    private String readWord(ImapRequestLineReader request, String terminator) throws ProtocolException {
        StringBuffer buf = new StringBuffer();
        char next = request.nextChar(); 
        while(terminator.indexOf(next)==-1) {
            buf.append(next);
            request.consume();
            next = request.nextChar();
        }
        return buf.toString();
    }
    
    private char nextCharInLine( ImapRequestLineReader request )
            throws ProtocolException
    {
        char next = request.nextChar();
        if ( next == '\r' || next == '\n' ) {
            throw new ProtocolException( "Unexpected end of line." );
        }
        return next;
    }

    private char nextNonSpaceChar( ImapRequestLineReader request )
            throws ProtocolException
    {
        char next = request.nextChar();
        while ( next == ' ' ) {
            request.consume();
            next = request.nextChar();
        }
        return next;
    }

    protected AbstractImapCommandMessage decode(ImapCommand command, ImapRequestLineReader request, String tag, boolean useUids) throws ProtocolException {
        IdRange[] idSet = parseIdRange( request );
        FetchRequest fetch = fetchRequest( request );
        endLine( request );
        
        final FetchCommandMessage result 
            = new FetchCommandMessage(command, useUids, idSet, fetch, tag);
        return result;
    }

}
