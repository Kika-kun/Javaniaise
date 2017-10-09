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
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jvn.JvnObjectImpl.Lock;

public class JvnCoordImpl
        extends UnicastRemoteObject
        implements JvnRemoteCoord {

    private static Integer idServ = 0;
    private static Integer objectId = 0;

    private final HashMap<JvnRemoteServer, Integer> listServerToServerId;
    private final HashMap<String, JvnObject> listObjectNameToObjectRef;
    private final HashMap<JvnObject, JvnRemoteServer> listObjectRefToServer;
    private final HashMap<JvnObject, Tuple<JvnRemoteServer, Lock>> listObjectRefServerUser;

    Registry r;

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
        listObjectNameToObjectRef = new HashMap<String, JvnObject>();
        listObjectRefToServer = new HashMap<JvnObject, JvnRemoteServer>();
        listObjectRefServerUser = new HashMap<JvnObject, Tuple<JvnRemoteServer, Lock>>();
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
        // TODO: Do smth if jon already existed
        System.out.println("Registered '" + jon + "'");
        //System.out.flush();
        listObjectNameToObjectRef.put(jon, jo);
        listObjectRefToServer.put(jo, js);
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
        return listObjectNameToObjectRef.get(jon);
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
        // check si qqn a le lock
        for (JvnObject o : listObjectRefServerUser.keySet()) {
            // si l'objet est locked
            if (o.jvnGetObjectId() == joi) {
                Tuple<JvnRemoteServer, Lock> t = listObjectRefServerUser.get(o);
                Serializable ref;

                //Si celui qui lockait l'objet n'est pas celui qui redemande un lock
                if (!(t.a == js)) {
                    switch (t.b) {
                        case W:
                        case WC:
                            t.a.jvnInvalidateWriterForReader(joi);
                            ref = o.jvnInvalidateWriterForReader();
                            t = listObjectRefServerUser.remove(o);
                            t.a = js;
                            t.b = Lock.R;
                            listObjectRefServerUser.put(o, t);
                            break;
                        default:
                            ref = o.jvnGetObjectState();
                    }
                    return ref;
                } else {
                    System.err.println("Error : trying to lock something you already lock");
                }
            }
        }

        // OMG personne avait le lock
        Tuple<JvnRemoteServer, Lock> t = new Tuple<JvnRemoteServer, Lock>(js, Lock.R);
        for (JvnObject o : listObjectRefToServer.keySet()) {
            if (o.jvnGetObjectId() == joi) {
                listObjectRefServerUser.put(o, t);
                return o.jvnGetObjectState();
            }
        }
        throw new JvnException("Error : No JvnObject with this ID");
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
        for (JvnObject o : listObjectRefServerUser.keySet()) {
            // si l'objet est locked
            if (o.jvnGetObjectId() == joi) {
                Tuple<JvnRemoteServer, Lock> t = listObjectRefServerUser.get(o);
                Serializable ref;

                //Si celui qui lockait l'objet n'est pas celui qui redemande un lock
                if (!(t.a == js)) {
                    switch (t.b) {
                        case R:
                        case RC:
                            t.a.jvnInvalidateReader(joi);
                            ref = o.jvnGetObjectState();
                            t = listObjectRefServerUser.remove(o);
                            t.a = js;
                            t.b = Lock.W;     
                            listObjectRefServerUser.put(o, t);
                            break;
                        case W:
                        case WC:
                            t.a.jvnInvalidateWriter(joi);
                            ref = o.jvnInvalidateWriter();
                            t = listObjectRefServerUser.remove(o);
                            t.a = js;
                            t.b = Lock.W;
                            listObjectRefServerUser.put(o, t);
                            break;
                        default:
                            ref = o.jvnGetObjectState();
                    }
                    return ref;
                } else {
                    System.err.println("Error : trying to lock something you already lock");
                }
            }
        }

        // OMG personne avait le lock
        Tuple<JvnRemoteServer, Lock> t = new Tuple<JvnRemoteServer, Lock>(js, Lock.R);
        for (JvnObject o : listObjectRefToServer.keySet()) {
            if (o.jvnGetObjectId() == joi) {
                listObjectRefServerUser.put(o, t);
                return o.jvnGetObjectState();
            }
        }
        throw new JvnException("Error : No JvnObject with this ID");
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
            Logger.getLogger(JvnCoordImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccessException ex) {
            Logger.getLogger(JvnCoordImpl.class.getName()).log(Level.SEVERE, null, ex);
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

class Tuple<X, Y> {

    public X a;
    public Y b;

    public Tuple(X a, Y b) {
        this.a = a;
        this.b = b;
    }
}
