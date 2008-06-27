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

package org.apache.james.nntpserver;

import org.apache.james.nntpserver.repository.NNTPRepository;
import org.apache.mailet.UsersRepository;

/**
 * Provides a number of server-wide constant values to the
 * NNTPHandlers
 *
 */
public interface NNTPHandlerConfigurationData {

    /**
     * Returns the service wide hello name
     *
     * @return the hello name
     */
    String getHelloName();

    /**
     * Returns whether NNTP auth is active for this server.
     *
     * @return whether NNTP authentication is on
     */
    boolean isAuthRequired();

    /**
     * Returns the NNTPRepository used by this service.
     *
     * @return the NNTPRepository used by this service
     */
    NNTPRepository getNNTPRepository();

    /**
     * Returns the UsersRepository for this service.
     *
     * @return the local users repository
     */
    UsersRepository getUsersRepository();

}