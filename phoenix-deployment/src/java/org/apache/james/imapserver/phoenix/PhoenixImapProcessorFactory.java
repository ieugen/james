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

package org.apache.james.imapserver.phoenix;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.james.api.user.UserMetaDataRespository;
import org.apache.james.imapserver.processor.main.DefaultImapProcessorFactory;
import org.apache.james.mailboxmanager.manager.MailboxManagerProvider;
import org.apache.james.services.UsersRepository;

public class PhoenixImapProcessorFactory extends DefaultImapProcessorFactory implements Serviceable {

    public void service(ServiceManager serviceManager) throws ServiceException {
        UsersRepository usersRepository = ( UsersRepository ) serviceManager.
            lookup( UsersRepository.ROLE );
        MailboxManagerProvider mailboxManagerProvider = 
            (MailboxManagerProvider) serviceManager.lookup( MailboxManagerProvider.ROLE );
        UserMetaDataRespository userMetaDataRepository =
            (UserMetaDataRespository) serviceManager.lookup( UserMetaDataRespository.ROLE );
        configure(usersRepository, mailboxManagerProvider, userMetaDataRepository);
    }

}