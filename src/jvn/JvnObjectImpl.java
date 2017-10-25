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

    public void jvnLockRead() throws JvnException {
        ref = JvnServerImpl.jvnGetServer().jvnLockRead(id);

        switch (status) {
            case NL:
                status = Lock.R;
                break;
            case R:
                break;
            case RC:
                status = Lock.R;
                break;
            case W:
                status = Lock.RWC;
                break;
            case WC:
                status = Lock.RWC;
                break;
            case RWC:
                break;
        }

    }

    public void jvnLockWrite() throws JvnException {
        ref = JvnServerImpl.jvnGetServer().jvnLockWrite(id);

        switch (status) {
            case NL:
                status = Lock.W;
                break;
            case R:
                status = Lock.W;
                break;
            case RC:
                status = Lock.W;
                break;
            case W:
                break;
            case WC:
                status = Lock.W;
                break;
            case RWC:
                status = Lock.W;
                break;
        }

    }

    public synchronized void jvnUnLock() throws JvnException {
        switch (status) {
            case R:
                status = Lock.RC;
                notify();
                break;
            case W:
            case RWC:
                status = Lock.WC;
                notify();
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
        status = Lock.WC;
        return ref;
    }

    public Serializable jvnInvalidateWriterForReader() throws JvnException {
        status = Lock.RWC;
        return ref;
    }

    public Lock jvnGetStatus() {
        return status;
    }

    public void jvnSetObjectState(Serializable ref) {
        this.ref = ref;
    }

}
