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
package org.apache.james.vut.api;

import java.util.Collection;
import java.util.Map;

/**
 * Interface which should be implemented of classes which map recipients.
 */
public interface VirtualUserTable {

    /**
     * The prefix which is used for error mappings
     */
    static final String ERROR_PREFIX = "error:";
    
    /**
     * The prefix which is used for regex mappings
     */
    static final String REGEX_PREFIX = "regex:";
    
    /**
     * The prefix which is used for alias domain mappings
     */
    static final String ALIASDOMAIN_PREFIX = "domain:";
    
    /**
     * The wildcard used for alias domain mappings
     */
    final static String WILDCARD = "*";

    /**
     * Return the mapped MailAddress for the given address. Return null if no 
     * matched mapping was found
     * 
     * @param user the MailAddress
     * @return the mapped mailAddress
     * @throws ErrorMappingException get thrown if an error mapping was found
     * @throws VirtualUserTableException 
     */
    Collection<String> getMappings(String user, String domain) throws ErrorMappingException, VirtualUserTableException;
    
    
    /**
     * Add regex mapping
     * 
     * @param user the username. Null if no username should be used
     * @param domain the domain. Null if no domain should be used
     * @param regex the regex.
     * @throws VirtualUserTableException 
     */
    void addRegexMapping(String user, String domain, String regex) throws VirtualUserTableException;
    
    /**
     * Remove regex mapping
     * 
     * @param user the username. Null if no username should be used
     * @param domain the domain. Null if no domain should be used
     * @param regex the regex.
     * @throws VirtualUserTableException 
     */
    void removeRegexMapping(String user,String domain, String regex) throws VirtualUserTableException 
;
    
    /***
     * Add address mapping
     * 
     * @param user the username. Null if no username should be used
     * @param domain the domain. Null if no domain should be used
     * @param address 
     * @throws VirtualUserTableException 
     */
    void addAddressMapping(String user, String domain, String address) throws VirtualUserTableException;
    
    /**
     * Remove address mapping
     * 
     * @param user the username. Null if no username should be used
     * @param domain the domain. Null if no domain should be used
     * @param address 
     * @throws VirtualUserTableException 
     */
    void removeAddressMapping(String user,String domain, String address) throws VirtualUserTableException;
    
    /**
     * Add error mapping
     * 
     * @param user the username. Null if no username should be used
     * @param domain the domain. Null if no domain should be used
     * @param error the regex.
     * @throws VirtualUserTableException 
     */
    void addErrorMapping(String user, String domain, String error) throws VirtualUserTableException;

    /**
     * Remove error mapping
     * 
     * @param user the username. Null if no username should be used
     * @param domain the domain. Null if no domain should be used
     * @param error
     * @return true if successfully
     * @throws VirtualUserTableException 
     */
    void removeErrorMapping(String user,String domain, String error) throws VirtualUserTableException;
    
    /**
     * Return the explicit mapping stored for the given user and domain. Return null
     * if no mapping was found
     * 
     * @param user the username
     * @param domain the domain
     * @return the collection which holds the mappings. 
     * @throws VirtualUserTableException 
     */
    Collection<String> getUserDomainMappings(String user, String domain) throws VirtualUserTableException;
    
    /**
     * Add mapping
     * 
     * @param user the username. Null if no username should be used
     * @param domain the domain. Null if no domain should be used
     * @param mapping the mapping
     * @throws VirtualUserTableException 
     */
    void addMapping(String user, String domain, String mapping)throws VirtualUserTableException;
    
    /**
     * Remove mapping
     * 
     * @param user the username. Null if no username should be used
     * @param domain the domain. Null if no domain should be used
     * @param mapping the mapping
     * @throws VirtualUserTableException 
     */
    void removeMapping(String user, String domain, String mapping) throws VirtualUserTableException;


    /**
     * Return a Map which holds all mappings. The key is the user@domain and the value is a Collection 
     * which holds all mappings
     * 
     * @return Map which holds all mappings
     * @throws VirtualUserTableException 
     */
    Map<String,Collection<String>> getAllMappings() throws VirtualUserTableException;
    
    /**
     * Add aliasDomain mapping
     * 
     * @param aliasDomain the aliasdomain which should be mapped to the realDomain
     * @param realDomain the realDomain
     * @throws VirtualUserTableException 
     */
    void addAliasDomainMapping(String aliasDomain, String realDomain) throws VirtualUserTableException;
    
    /**
     * Remove aliasDomain mapping
     * 
     * @param aliasDomain the aliasdomain which should be mapped to the realDomain
     * @param realDomain the realDomain
     * @return true if successfully
     */
    void removeAliasDomainMapping(String aliasDomain, String realDomain) throws VirtualUserTableException;

    class ErrorMappingException extends Exception {

        private static final long serialVersionUID = 2348752938798L;

        public ErrorMappingException(String string) {
            super(string);
        }

    }
}
