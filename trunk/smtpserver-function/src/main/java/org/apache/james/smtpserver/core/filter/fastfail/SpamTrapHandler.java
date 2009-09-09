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


package org.apache.james.smtpserver.core.filter.fastfail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.james.smtpserver.Configurable;
import org.apache.james.smtpserver.SMTPSession;
import org.apache.james.smtpserver.hook.HookResult;
import org.apache.james.smtpserver.hook.HookReturnCode;
import org.apache.james.smtpserver.hook.RcptHook;
import org.apache.mailet.MailAddress;

/**
 * This handler can be used for providing a spam trap. IPAddresses which send emails to the configured
 * recipients will get blacklisted for the configured time.
 */
public class SpamTrapHandler extends AbstractLogEnabled implements RcptHook,Configurable{

    // Map which hold blockedIps and blockTime in memory
    private Map<String,Long> blockedIps = new HashMap<String,Long>();
    
    private Collection<String> spamTrapRecips = new ArrayList<String>();
    
    // Default blocktime 12 hours
    private long blockTime = 4320000; 
    
    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    @SuppressWarnings("unchecked")
	public void configure(Configuration config) throws ConfigurationException {
        List<String> rcpts= config.getList("spamTrapRecip");
    
        if (rcpts.isEmpty() == false ) {
            setSpamTrapRecipients(rcpts);
        } else {
            throw new ConfigurationException("Please configure a spamTrapRecip.");
        }
    
        setBlockTime(config.getLong("blockTime",blockTime));
        
    }
    
    public void setSpamTrapRecipients(Collection<String> spamTrapRecips) {
        this.spamTrapRecips = spamTrapRecips;
    }
    
    public void setBlockTime(long blockTime) {
        this.blockTime = blockTime;
    }
    
    /**
     * @see org.apache.james.smtpserver.hook.RcptHook#doRcpt(org.apache.james.smtpserver.SMTPSession, org.apache.mailet.MailAddress, org.apache.mailet.MailAddress)
     */
    public HookResult doRcpt(SMTPSession session, MailAddress sender, MailAddress rcpt) {
        if (isBlocked(session.getRemoteIPAddress())) {
            return new HookResult(HookReturnCode.DENY);
        } else {
         
            if (spamTrapRecips.contains(rcpt.toString().toLowerCase())){
        
                addIp(session.getRemoteIPAddress());
            
                return new HookResult(HookReturnCode.DENY);
            }
        }
        return new HookResult(HookReturnCode.DECLINED);
    }
    
    
    /**
     * Check if ipAddress is in the blockList.
     * 
     * @param ip ipAddress to check
     * @return true or false
     */
    private boolean isBlocked(String ip) {
        Long rawTime = blockedIps.get(ip);
    
        if (rawTime != null) {
            long blockTime = rawTime.longValue();
           
            if (blockTime > System.currentTimeMillis()) {
                getLogger().debug("BlockList contain Ip " + ip);
                return true;
            } else {
                getLogger().debug("Remove ip " + ip + " from blockList");
               
                synchronized(blockedIps) {
                    blockedIps.remove(ip);
                }
            }
        }
        return false;
    }
    
    /**
     * Add ipaddress to blockList
     * 
     * @param ip IpAddress to add
     */
    private void addIp(String ip) {
        long bTime = System.currentTimeMillis() + blockTime;
        
        getLogger().debug("Add ip " + ip + " for " + bTime + " to blockList");
    
        synchronized(blockedIps) {
            blockedIps.put(ip, new Long(bTime));
        }
    
    }
}
