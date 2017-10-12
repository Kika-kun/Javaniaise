/**
 * *
 * JAVANAISE API
 * Contact:
 *
 * Authors:
 */
package jvn;

import java.io.*;

/**
 * Interface of a JVN object. The serializable property is required in order to
 * be able to transfer a reference to a JVN object remotely
 */
public interface JvnObject extends Serializable {

    static enum Lock {
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

    /**
     * Get a Read lock on the object
     *
     * @throws JvnException
	*
     */
    public void jvnLockRead()
            throws jvn.JvnException;

    /**
     * Get a Write lock on the object
     *
     * @throws JvnException
	*
     */
    public void jvnLockWrite()
            throws jvn.JvnException;

    /**
     * Unlock the object
     *
     * @throws JvnException
	*
     */
    public void jvnUnLock()
            throws jvn.JvnException;

    /**
     * Get the object identification
     *
     * @throws JvnException
	*
     */
    public int jvnGetObjectId()
            throws jvn.JvnException;

    /**
     * Get the object state
     *
     * @throws JvnException
	*
     */
    public Serializable jvnGetObjectState()
            throws jvn.JvnException;

    /**
     * Invalidate the Read lock of the JVN object
     *
     * @throws JvnException
	*
     */
    public void jvnInvalidateReader()
            throws jvn.JvnException;

    /**
     * Invalidate the Write lock of the JVN object
     *
     * @return the current JVN object state
     * @throws JvnException
	*
     */
    public Serializable jvnInvalidateWriter()
            throws jvn.JvnException;

    /**
     * Reduce the Write lock of the JVN object
     *
     * @return the current JVN object state
     * @throws JvnException
	*
     */
    public Serializable jvnInvalidateWriterForReader()
            throws jvn.JvnException;

    // Accessible for the classes of the Jvn package
    // Returns the status of the Object
    Lock jvnGetStatus();
}
