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

/**
 * 
 */
package org.apache.james.imapserver.processor.imap4rev1.fetch;

import java.util.Arrays;

import org.apache.james.mailboxmanager.MessageResult;

final class MimePathImpl implements MessageResult.MimePath {
    private final int[] positions;
    
    public MimePathImpl(final int[] positions) {
        super();
        this.positions = positions;
    }

    public int[] getPositions() {
        return positions;
    }

    public int hashCode() {
        return positions.length;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final MimePathImpl other = (MimePathImpl) obj;
        if (!Arrays.equals(positions, other.positions))
            return false;
        return true;
    }
    
    public String toString() {
        final StringBuffer buffer = new StringBuffer("MIMEPath:");
        boolean isFirst = false;
        for (int i = 0; i < positions.length; i++) {
            if (isFirst) {
                isFirst = false;
            } else {
                buffer.append('.');
            }
            buffer.append(positions[i]);
        }
        return buffer.toString();
    }
}