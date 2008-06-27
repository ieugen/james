package org.apache.james.mailboxmanager.wrapper;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

public class UidToMsnBidiMap {

    protected SortedMap msnToUid;

    protected SortedMap uidToMsn;

    protected long highestUid = 0;
    
    protected int highestMsn = 0;

    public UidToMsnBidiMap() {
        msnToUid = new TreeMap();
        uidToMsn = new TreeMap();
    }

    public synchronized long getUid(int msn) {
        if (msn == -1) {
            return -1;
        }
        Long uid = (Long) msnToUid.get(new Integer(msn));
        if (uid != null) {
            return uid.longValue();
        } else {
            if (msn > 0) {
                return highestUid;
            } else {
                return 0;
            }
        }
    }

    public synchronized int getMsn(long uid) {
        Integer msn = (Integer) uidToMsn.get(new Long(uid));
        if (msn != null) {
            return msn.intValue();
        } else {
            return -1;
        }

    }

    protected synchronized void add(int msn, long uid) {
        if (uid > highestUid) {
            highestUid = uid;
        }
        msnToUid.put(new Integer(msn), new Long(uid));
        uidToMsn.put(new Long(uid), new Integer(msn));
    }

    
    
    public synchronized void expunge(long uid) {
        int msn=getMsn(uid);
        remove(msn,uid);
        List renumberMsns=new ArrayList(msnToUid.tailMap(new Integer(msn+1)).keySet());
        for (Iterator iter = renumberMsns.iterator(); iter.hasNext();) {
            int aMsn = ((Integer) iter.next()).intValue();
            long aUid= getUid(aMsn);
            remove(aMsn,aUid);
            add(aMsn-1,aUid);
        }
        highestMsn--;
        assertValidity();
    }
    
    protected void remove(int msn,long uid) {
        uidToMsn.remove(new Long(uid));
        msnToUid.remove(new Integer(msn));
    }
    
    synchronized void assertValidity() {
        Integer[] msns=(Integer[])msnToUid.keySet().toArray(new Integer[0]);
        for (int i = 0; i < msns.length; i++) {
            if (msns[i].intValue()!=(i+1)) {
                throw new AssertionError("Msn at position "+(i+1)+" was "+msns[i].intValue());
            }
        }
        if (msns.length > 0) {
            if (msns[msns.length - 1].intValue() != highestMsn) {
                throw new AssertionError("highestMsn " + highestMsn
                        + " msns[msns.length-1] " + msns[msns.length - 1]);
            }
        } else {
            if (highestMsn != 0) {
                throw new AssertionError(
                        "highestMsn in empty map has to be 0 but is"
                                + highestMsn);
            }
        }
        if (!msnToUid.keySet().equals(new TreeSet(uidToMsn.values()))) {
            System.out.println(msnToUid.keySet());
            System.out.println(uidToMsn.values());
            throw new AssertionError("msnToUid.keySet() does not equal uidToMsn.values()");
        }
        if (!uidToMsn.keySet().equals(new TreeSet(msnToUid.values()))) {
            System.out.println(uidToMsn.keySet());
            System.out.println(msnToUid.values());
            throw new AssertionError("uidToMsn.keySet() does not equal msnToUid.values()");
        }

    }

    public synchronized void add(long uid) {
        if (!uidToMsn.containsKey(new Long(uid))) {
            highestMsn++;
            add(highestMsn, uid);
        }
    }

    int size() {
        return uidToMsn.size();
    }

}