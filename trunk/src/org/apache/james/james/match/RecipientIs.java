/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.james.james.match;

import javax.mail.internet.*;
import javax.mail.Session;
import org.apache.mail.MessageContainer;
import java.util.*;

/**
 * @version 1.0.0, 24/04/1999
 * @author  Federico Barbieri <scoobie@pop.systemy.it>
 */
public class RecipientIs implements Match {
    
    public Vector match(MessageContainer mc, String condition) {
        Vector matchingRecipients = new Vector();
        for (Enumeration e = mc.getRecipients().elements(); e.hasMoreElements(); ) {
            String rec = (String) e.nextElement();
            if (condition.indexOf(rec) != -1) {
                matchingRecipients.addElement(rec);
            }
        }
        return matchingRecipients;
    }
}
    
