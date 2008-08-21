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

package org.apache.james.imapserver.client;

import java.io.IOException;
import java.util.List;

import javax.mail.MessagingException;

public interface Command {

    public List getExpectedResponseList() throws MessagingException, IOException;

    /**
     * Untagged, without leading space or trailing newline.<br>
     * e.g. : "OK LOGIN completed."
     */
    public String getExpectedStatusResponse();

    /**
     * Untagged, without leading space with trailing newline. <br>
     *  e.g. : "LOGIN user password\n"
     */
    public String getCommand();

}