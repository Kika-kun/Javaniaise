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
    
    private Lock status;
    
    private final int id;
    Serializable ref;
    
    // XX - YY ; sources ; read me
    /**
     *
     * @param s
     * @param id
     */
    public JvnObjectImpl(Serializable s, int id) {
        super();
        status = Lock.W;
        ref = s;
        this.id = id;
    }    
       
    public synchronized void jvnLockRead() throws JvnException {
        JvnServerImpl.jvnGetServer().jvnLockRead(id);
        status = Lock.R;
    }

    public synchronized void jvnLockWrite() throws JvnException {
        JvnServerImpl.jvnGetServer().jvnLockWrite(id);
        status = Lock.W;
    }

    public synchronized void jvnUnLock() throws JvnException {
        switch (status) {
            case R:
                status = Lock.RC;
                break;
            case W:
                status = Lock.WC;
                break;
            default:
                System.err.println("Can't unlock something already not locked");
        }
        notify();
    }

    public synchronized int jvnGetObjectId() throws JvnException {
        return id;
    }

    public synchronized Serializable jvnGetObjectState() throws JvnException {
        return ref;
    }

    public synchronized void jvnInvalidateReader() throws JvnException {
        status = Lock.RC;
    }

    public synchronized Serializable jvnInvalidateWriter() throws JvnException {
        //wait();
        System.out.println("123456");
        System.out.flush();
//        status = Lock.WC;
//        return ref;
        return null;
    }

    public synchronized Serializable jvnInvalidateWriterForReader() throws JvnException {
        //if canL
        status = Lock.RWC;
        return ref;
    }

    public Lock jvnGetStatus() {
        return status;
    }
    
}
