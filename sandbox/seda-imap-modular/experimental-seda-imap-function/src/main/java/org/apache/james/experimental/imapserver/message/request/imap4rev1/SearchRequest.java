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
package org.apache.james.experimental.imapserver.message.request.imap4rev1;

import javax.mail.search.SearchTerm;

import org.apache.james.api.imap.ImapCommand;
import org.apache.james.experimental.imapserver.message.request.base.AbstractImapRequest;

public class SearchRequest extends AbstractImapRequest {
    private final SearchTerm searchTerm;
    private final boolean useUids;

    public SearchRequest(final ImapCommand command, final SearchTerm searchTerm, final boolean useUids,
            final String tag) {
        super(tag, command);
        this.searchTerm = searchTerm;
        this.useUids = useUids;
    }

    public final SearchTerm getSearchTerm() {
		return searchTerm;
	}

	public final boolean isUseUids() {
		return useUids;
	}
}
