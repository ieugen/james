
package org.apache.james.mailboxmanager.torque.om;


import javax.mail.Flags;

import org.apache.torque.om.Persistent;

/**
 * The skeleton for this class was autogenerated by Torque on:
 *
 * [Wed Sep 06 19:48:03 CEST 2006]
 *
 * You should add additional methods to this class to meet the
 * application requirements.  This class will only be generated as
 * long as it does not already exist in the output directory.
 */
public  class MessageFlags
    extends org.apache.james.mailboxmanager.torque.om.BaseMessageFlags
    implements Persistent
{
    private static final long serialVersionUID = -7426028860085278304L;


    public void setFlags(Flags flags) {
        setAnswered(flags.contains(Flags.Flag.ANSWERED));
        setDeleted(flags.contains(Flags.Flag.DELETED));
        setDraft(flags.contains(Flags.Flag.DRAFT));
        setFlagged(flags.contains(Flags.Flag.FLAGGED));
        setRecent(flags.contains(Flags.Flag.RECENT));
        setSeen(flags.contains(Flags.Flag.SEEN));
    }
    
    public Flags getFlagsObject() {
        Flags flags=new Flags();

        if (getAnswered()) {
            flags.add(Flags.Flag.ANSWERED);
        }
        if (getDeleted()) {
            flags.add(Flags.Flag.DELETED);
        }
        if (getDraft()) {
            flags.add(Flags.Flag.DRAFT);
        }
        if (getFlagged()) {
            flags.add(Flags.Flag.FLAGGED);
        }
        if (getRecent()) {
            flags.add(Flags.Flag.RECENT);
        }
        if (getSeen()) {
            flags.add(Flags.Flag.SEEN);
        }
        return flags;
    }
}
