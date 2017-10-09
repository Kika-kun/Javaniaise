/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jvn;

import java.io.Serializable;

/**
 *
 * @author marionsy
 */
public class JvnObjectImpl implements JvnObject {
    

    public static enum Lock implements Serializable {
        NL(true), 
        RC(true), 
        WC(true), 
        R(false), 
        W(false), 
        RWC(false);
        
        private final boolean canLock;
        Lock(boolean canLock) {
            this.canLock = canLock;
        }
        
        public boolean canLock() {
            return canLock;
        }
    };
    public Lock status;
    private final int id;
    private final Serializable ref;

    /**
     *
     * @param s
     */
    public JvnObjectImpl(Serializable s, int id) {
        super();
        status = Lock.WC;
        ref = s;
        this.id = id;
    }    
       
    public synchronized void jvnLockRead() throws JvnException {
        if (status.canLock()) {
            status = Lock.R;
        }
    }

    public synchronized void jvnLockWrite() throws JvnException {
        if (status.canLock) {
            status = Lock.W;
        }
    }

    public synchronized void jvnUnLock() throws JvnException {
        status = Lock.NL;
        notify();
    }

    public synchronized int jvnGetObjectId() throws JvnException {
        return id;
    }

    public synchronized Serializable jvnGetObjectState() throws JvnException {
        return ref;
    }

    public synchronized void jvnInvalidateReader() throws JvnException {
        status = Lock.NL;
    }

    public synchronized Serializable jvnInvalidateWriter() throws JvnException {
        //wait();
        status = Lock.NL;
        return ref;
    }

    public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
        //if canL
        status = Lock.R;
        return ref;
    }
    
}
