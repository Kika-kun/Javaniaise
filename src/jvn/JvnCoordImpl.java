/**
 * *
 * JAVANAISE Implementation JvnServerImpl class Contact:
 *
 * Authors:
 */
package jvn;

import java.rmi.server.UnicastRemoteObject;
import java.io.Serializable;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import jvn.JvnObject.Lock;

public class JvnCoordImpl
        extends UnicastRemoteObject
        implements JvnRemoteCoord {

    private static Integer idServ = 0;
    private static Integer objectId = 0;

    private final HashMap<JvnRemoteServer, Integer> listServerToServerId;
    private final HashMap<Integer, JvnObjectState> listObjects;

    private static Registry r;

    /**
     * Default constructor
     *
     * @throws JvnException
     *
     */
    public JvnCoordImpl() throws Exception {
        // Create registry
        r = LocateRegistry.createRegistry(4321);
        r.bind("coordinator", this);

        listServerToServerId = new HashMap<JvnRemoteServer, Integer>();
        listObjects = new HashMap<Integer, JvnObjectState>();
    }

    /**
     * Allocate a NEW JVN object id (usually allocated to a newly created JVN
     * object)
     *
     * @return
     * @throws java.rmi.RemoteException,JvnException
     * @throws jvn.JvnException
     *
     */
    public int jvnGetObjectId()
            throws java.rmi.RemoteException, jvn.JvnException {
        int ret = objectId;
        objectId++;
        return ret;
    }

    /**
     * Associate a symbolic name with a JVN object
     *
     * @param jon : the JVN object name
     * @param jo : the JVN object
     * @param js : the remote reference of the JVNServer
     * @throws java.rmi.RemoteException,JvnException
     * @throws jvn.JvnException
     *
     */
    public void jvnRegisterObject(String jon, JvnObject jo, JvnRemoteServer js)
            throws java.rmi.RemoteException, jvn.JvnException {
        // Check if the name is already registered
        for (JvnObjectState object : listObjects.values()) {
            if (object.getName().equals(jon)) {
                System.err.println("Error : that object name is already registered");
                throw new JvnException("Trying to register an object under a name already existing.");
            }
        }
        // Create a new object to register
        JvnObjectState object = new JvnObjectState(jo, jon, jo.jvnGetObjectId());
        object.setStatus(jo.jvnGetStatus());
        object.addWriter(js);
        listObjects.put(jo.jvnGetObjectId(), object);

        System.out.println("Registered '" + jon + "'");

    }

    /**
     * Get the reference of a JVN object managed by a given JVN server
     *
     * @param jon : the JVN object name
     * @param js : the remote reference of the JVNServer
     * @return
     * @throws java.rmi.RemoteException,JvnException
     * @throws jvn.JvnException
     *
     */
    public JvnObject jvnLookupObject(String jon, JvnRemoteServer js)
            throws java.rmi.RemoteException, jvn.JvnException {
        for (JvnObjectState object : listObjects.values()) {
            if (object.getName().equals(jon)) {
                System.out.println("orig = "+object.orig);
                return object.orig;
            }
        }
        return null;
    }

    /**
     * Get a Read lock on a JVN object managed by a given JVN server
     *
     * @param joi : the JVN object identification
     * @param js : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException, JvnException
     *
     */
    public Serializable jvnLockRead(int joi, JvnRemoteServer js)
            throws java.rmi.RemoteException, JvnException {
        JvnObjectState object = listObjects.get(joi);
        if (object == null) {
            throw new JvnException("Error : No JvnObject with this ID");
        }
        Serializable ret;
        switch (object.getStatus()) {
            case W:
            case WC:
                ret = object.getWriter().jvnInvalidateWriterForReader(object.getId());
                object.orig.ref = ret;
                JvnRemoteServer demotedWriter = object.getWriter();
                object.removeWriter();
                object.setStatus(Lock.R);
                object.addReader(js);
                object.addReader(demotedWriter);
                break;
            default:
                object.setStatus(Lock.R);
                object.addReader(js);
                ret = object.orig.jvnGetObjectState();
        }
        return ret;
    }

    /**
     * Get a Write lock on a JVN object managed by a given JVN server
     *
     * @param joi : the JVN object identification
     * @param js : the remote reference of the server
     * @return the current JVN object state
     * @throws java.rmi.RemoteException, JvnException
     *
     */
    public Serializable jvnLockWrite(int joi, JvnRemoteServer js)
            throws java.rmi.RemoteException, JvnException {
        // check si qqn a le lock
        JvnObjectState object = listObjects.get(joi);
        if (object == null) {
            throw new JvnException("Error : No JvnObject with this ID");
        }
        Serializable ret;
        switch (object.getStatus()) {
            case W:
            case WC:
                ret = object.getWriter().jvnInvalidateWriter(object.getId());
                object.orig.ref = ret;
                object.removeWriter();
                object.setStatus(Lock.W);
                object.addWriter(js);
                break;
            case R:
            case RC:
            case RWC:
                List<JvnRemoteServer> listReaders = new ArrayList<JvnRemoteServer>();
                for (JvnRemoteServer reader : object.getReaders()) {
                    reader.jvnInvalidateReader(object.getId());
                    listReaders.add(reader);
                }
                for (JvnRemoteServer reader : listReaders) {
                    object.removeReader(reader);
                }
                // Essentiellement on a unlock l'object donc on peut le laisser passer de la partie unlock
            default:
                object.setStatus(Lock.W);
                object.addWriter(js);
                ret = object.orig.jvnGetObjectState();
        }
        return ret;
    }

    /**
     * A JVN server terminates
     *
     * @param js : the remote reference of the server
     * @throws java.rmi.RemoteException, JvnException
     *
     */
    public void jvnTerminate(JvnRemoteServer js)
            throws java.rmi.RemoteException, JvnException {
        try {
            Integer idToRemove = listServerToServerId.get(js);
            r.unbind(idToRemove.toString());
            listServerToServerId.remove(js);

        } catch (NotBoundException ex) {
            Logger.getLogger(JvnCoordImpl.class
                    .getName()).log(Level.SEVERE, null, ex);

        } catch (AccessException ex) {
            Logger.getLogger(JvnCoordImpl.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void JvnRegisterServer(JvnRemoteServer server)
            throws RemoteException, JvnException {
        try {
            r.bind(idServ.toString(), server);
            listServerToServerId.put(server, idServ);
            idServ++;
        } catch (AlreadyBoundException ex) {
            System.err.println("Error : server name already registered");
        } catch (AccessException ex) {
            System.err.println(ex);
        }
    }
}
