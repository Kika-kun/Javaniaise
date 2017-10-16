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
import java.rmi.Remote;
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

    private static HashMap<Integer, JvnObject> listObjects;

    private JvnRemoteCoord coordinator;

    /**
     * Default constructor
     *
     * @throws JvnException
     *
     */
    private JvnServerImpl() throws Exception {
        super();

        // Get le registry
        Registry r = LocateRegistry.getRegistry(4321);
        try {
            coordinator = (JvnRemoteCoord) r.lookup("coordinator");
            coordinator.JvnRegisterServer(this);
            listObjects = new HashMap<Integer, JvnObject>();
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
                js = new JvnServerImpl();
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
            // to be completed
            coordinator.jvnRegisterObject(jon, jo, (JvnRemoteServer) js);
            listObjects.put(jo.jvnGetObjectId(), jo);
        } catch (JvnException ex) {
            //System.out.println("Hello");
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
            listObjects.put(lookedUpObj.jvnGetObjectId(), lookedUpObj);
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
    public Serializable jvnLockRead(int joi)
            throws JvnException {

        try {
            Serializable locked = coordinator.jvnLockRead(joi, js);
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
    public Serializable jvnLockWrite(int joi)
            throws JvnException {
        try {
            // to be completed
            return coordinator.jvnLockWrite(joi, js);

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
     * @return void
     * @throws java.rmi.RemoteException,JvnException
     *
     */
    public void jvnInvalidateReader(int joi)
            throws java.rmi.RemoteException, jvn.JvnException {
        listObjects.get(joi).jvnInvalidateReader();
    }

    ;
	    
	/**
	* Invalidate the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
  public Serializable jvnInvalidateWriter(int joi)
            throws java.rmi.RemoteException, jvn.JvnException {
      System.out.print("obj = "+listObjects.get(joi));
        return listObjects.get(joi).jvnInvalidateWriter();
    }

    ;
	
	/**
	* Reduce the Write lock of the JVN object identified by id 
	* @param joi : the JVN object id
	* @return the current JVN object state
	* @throws java.rmi.RemoteException,JvnException
	**/
   public Serializable jvnInvalidateWriterForReader(int joi)
            throws java.rmi.RemoteException, jvn.JvnException {
        // to be completed 
        return listObjects.get(joi).jvnInvalidateWriterForReader();
    }
}
