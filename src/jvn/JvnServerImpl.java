/**
 * *
 * JAVANAISE Implementation JvnServerImpl class Contact:
 *
 * Authors:
 */
package jvn;

import java.rmi.server.UnicastRemoteObject;
import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JvnServerImpl
        extends UnicastRemoteObject
        implements JvnLocalServer, JvnRemoteServer {

    // A JVN server is managed as a singleton 
    private static JvnServerImpl js = null;

    private static HashMap<Integer, JvnObject> cachedObjects;

    private JvnRemoteCoord coordinator;
    
    private final int maxCacheSize;

    /**
     * Default constructor
     *
     * @throws JvnException
     *
     */
    private JvnServerImpl(int maxCacheSize) throws Exception {
        super();
        
        this.maxCacheSize = maxCacheSize;
        // Get le registry
        Registry r = LocateRegistry.getRegistry(4321);
        try {
            coordinator = (JvnRemoteCoord) r.lookup("coordinator");
            cachedObjects = new HashMap<Integer, JvnObject>();
        } catch (RemoteException e) {
            System.err.println("Error : registry doesn't exist");
        } catch (NotBoundException e) {
            System.err.println("Error : coordinator doesn't exist");
        }
    }

    /**
     * Static method allowing an application to get a reference to a JVN server
     * instance
     *
     * @return
     *
     */
    public static JvnServerImpl jvnGetServer() {
        if (js == null) {
            try {
                js = new JvnServerImpl(5);
            } catch (Exception e) {
                return null;
            }
        }
        return js;
    }

    /**
     * The JVN service is not used anymore
     *
     * @throws JvnException
     *
     */
    public void jvnTerminate()
            throws jvn.JvnException {
        try {
            coordinator.jvnTerminate(js);
        } catch (RemoteException ex) {
            Logger.getLogger(JvnServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * creation of a JVN object
     *
     * @param o : the JVN object state
     * @throws JvnException
     *
     */
    public JvnObject jvnCreateObject(Serializable o)
            throws jvn.JvnException {
        try {
            Integer idObject = coordinator.jvnGetObjectId();
            JvnObject newObject = new JvnObjectImpl(o, idObject);

            return newObject;
        } catch (RemoteException ex) {
            System.err.println(ex);
            return null;
        }
    }

    /**
     * Associate a symbolic name with a JVN object
     *
     * @param jon : the JVN object name
     * @param jo : the JVN object
     * @throws JvnException
     *
     */
    public void jvnRegisterObject(String jon, JvnObject jo)
            throws jvn.JvnException {
        try {
            coordinator.jvnRegisterObject(jon, jo, (JvnRemoteServer) js);
            if (cachedObjects.size() >= maxCacheSize) {
                throw new jvn.JvnException("Cache full");
            }
            cachedObjects.put(jo.jvnGetObjectId(), jo);
        } catch (JvnException ex) {
            throw ex;
        } catch (RemoteException ex) {
            Logger.getLogger(JvnServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Provide the reference of a JVN object beeing given its symbolic name
     *
     * @param jon : the JVN object name
     * @return the JVN object
     * @throws JvnException
     *
     */
    public JvnObject jvnLookupObject(String jon)
            throws jvn.JvnException {
        try {
            JvnObject lookedUpObj = coordinator.jvnLookupObject(jon, js);
            if (cachedObjects.size() >= maxCacheSize) {
                throw new jvn.JvnException("Cache full");
            }
            cachedObjects.put(lookedUpObj.jvnGetObjectId(), lookedUpObj);
            return lookedUpObj;
        } catch (RemoteException ex) {
            Logger.getLogger(JvnServerImpl.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (NullPointerException ex) {
            return null;
        }
    }

    /**
     * Get a Read lock on a JVN object
     *
     * @param joi : the JVN object identification
     * @return the current JVN object state
     * @throws JvnException
     *
     */
    public  Serializable jvnLockRead(int joi)
            throws JvnException {

        //System.out.println("Server Lock Read");

        try {
            Serializable locked = coordinator.jvnLockRead(joi, js);
            if (locked == null) {
                locked = cachedObjects.get(joi).jvnGetObjectState();
            }
            //System.out.println("locked = "+ (Sentence) locked);
            return locked;
        } catch (RemoteException ex) {
            Logger.getLogger(JvnServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("On ne doit pas passer ici (lock read)");

        return null;

    }

    /**
     * Get a Write lock on a JVN object
     *
     * @param joi : the JVN object identification
     * @return the current JVN object state
     * @throws JvnException
     *
     */
    public  Serializable jvnLockWrite(int joi)
            throws JvnException {
        //System.out.println("Server Lock Write");

        try {
            Serializable locked = coordinator.jvnLockWrite(joi, js);
            if (locked == null) {
                locked = cachedObjects.get(joi).jvnGetObjectState();
            }
            //System.out.println("locked = "+ (Sentence) locked);

            return locked;

        } catch (RemoteException ex) {
            Logger.getLogger(JvnServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.err.println("On ne doit pas passer ici (lock write)");
        return null;
    }

    /**
     * Invalidate the Read lock of the JVN object identified by id called by the
     * JvnCoord
     *
     * @param joi : the JVN object id
     * @throws java.rmi.RemoteException,JvnException
     * @throws jvn.JvnException
     *
     */
    public  void jvnInvalidateReader(int joi)
            throws java.rmi.RemoteException, jvn.JvnException {
        //System.out.println("Server Invalidate reader");

        cachedObjects.get(joi).jvnInvalidateReader();
    }
	    
	/**
	* Invalidate the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
     * @throws jvn.JvnException
	**/
  public  Serializable jvnInvalidateWriter(int joi)
            throws java.rmi.RemoteException, jvn.JvnException {
        //System.out.println("Server Invalidate Writer");
        
        return cachedObjects.get(joi).jvnInvalidateWriter();
    }
	
	/**
	* Reduce the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
   public  Serializable jvnInvalidateWriterForReader(int joi)
            throws java.rmi.RemoteException, jvn.JvnException {
        //System.out.println("Server Invalidate writer for reader");
        return cachedObjects.get(joi).jvnInvalidateWriterForReader();
    }
   
   public void jvnFlush()
           throws jvn.JvnException {
        try {
            coordinator.jvnTerminate(js);
            cachedObjects.clear();
        } catch (RemoteException ex) {
            Logger.getLogger(JvnServerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
   }
}
