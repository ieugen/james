/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software License
 * version 1.1, a copy of which has been included with this distribution in
 * the LICENSE file.
 */
package org.apache.james.nntpserver.repository;

import org.apache.james.nntpserver.NNTPException;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * reads and translates client data. After this translation, 
 * the data can be streamed into server repository.
 * Handles Dot Stuffing.
 *
 * @author Harmeet Bedi <harmeet@kodemuse.com>
 */
public class NNTPLineReaderImpl implements NNTPLineReader {
    private final BufferedReader reader;
    public NNTPLineReaderImpl(BufferedReader reader) {
        this.reader = reader;
    }
    public String readLine() {
        try {
            String line = reader.readLine();
            // check for end of article.
            if ( line.equals(".") )
                line = null;
            else if ( line.startsWith(".") )
                 line = line.substring(1,line.length());
            return line;
        } catch(IOException ioe) {
            throw new NNTPException("could not create article",ioe);
        }
    }
}