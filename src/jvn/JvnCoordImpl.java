/**
 * *
 * JAVANAISE Implementation JvnServerImpl class Contact:
 *
 * Authors:
 */
package jvn;

import irc.Sentence;
import java.rmi.server.UnicastRemoteObject;
import java.io.Serializable;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JvnCoordImpl
        extends UnicastRemoteObject
        implements JvnRemoteCoord {

    private static Integer idServ = 0;
    private static Integer objectId = 0;

    private static HashMap<JvnObject, JvnRemoteServer> writtenObjects;
    private static HashMap<JvnObject, List<JvnRemoteServer>> readObjects;
    private static HashMap<String, JvnObject> objectNames;

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

        writtenObjects = new HashMap<JvnObject, JvnRemoteServer>();
        readObjects = new HashMap<JvnObject, List<JvnRemoteServer>>();
        objectNames = new HashMap<String, JvnObject>();
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
        for (String name : objectNames.keySet()) {
            if (name.equals(jon)) {
                System.err.println("Error : that object name is already registered");
                throw new JvnException("Trying to register an object under a name already existing.");
            }
        }
        objectNames.put(jon, jo);
        writtenObjects.put(jo, js);

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
        return objectNames.get(jon);
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
    public synchronized Serializable jvnLockRead(int joi, JvnRemoteServer js)
            throws java.rmi.RemoteException, JvnException {

        System.out.println("Coord Lock Read");

        for (Map.Entry<String, JvnObject> entrySet : objectNames.entrySet()) {
            if (entrySet.getValue().jvnGetObjectId() == joi) {

                // Object exists
                System.out.println("S avant   = " + (Sentence) entrySet.getValue().jvnGetObjectState());
                // Check if it's lock read
                if (readObjects.get(entrySet.getValue()) != null) {

                    if (readObjects.get(entrySet.getValue()).contains(js)) {
                        return null;
                    }
                    readObjects.get(entrySet.getValue()).add(js);
                    return entrySet.getValue().jvnGetObjectState();
                }

                // Check if it's lock write
                if (writtenObjects.get(entrySet.getValue()) != null) {
                    Serializable s;

                    if (writtenObjects.get(entrySet.getValue()).equals(js)) {
                        //System.out.println("avant iwfr ; js = " + js + " ; l'autre : " + writtenObjects.get(o));
                        s = writtenObjects.get(entrySet.getValue()).jvnInvalidateWriterForReader(joi);
                    } else {
                        s = writtenObjects.get(entrySet.getValue()).jvnInvalidateWriter(joi);
                    }
                    System.out.println("S après   = " + (Sentence) s);

                    writtenObjects.remove(entrySet.getValue());
                    JvnObject no = entrySet.getValue();
                    no.jvnSetObjectState(s);
                    objectNames.put(entrySet.getKey(), no);
                    List<JvnRemoteServer> readers = new ArrayList<JvnRemoteServer>();
                    readers.add(js);
                    readObjects.put(no, readers);
                    return s;
                }
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
    public synchronized  Serializable jvnLockWrite(int joi, JvnRemoteServer js)
            throws java.rmi.RemoteException, JvnException {

        System.out.println("Coord Lock Write");

        for (Map.Entry<String, JvnObject> entrySet : objectNames.entrySet()) {
            JvnObject o = entrySet.getValue();
            if (o.jvnGetObjectId() == joi) {
                // Object exists
                // Check if it's lock read
                if (readObjects.get(o) != null) {
                    if (readObjects.get(o).contains(js)) {
                        List<JvnRemoteServer> readers = readObjects.get(o);
                        for (JvnRemoteServer reader : readers) {
                            if (reader != js) {
                                reader.jvnInvalidateReader(joi);
                            }
                        }
                        readObjects.remove(o);
                        writtenObjects.put(o, js);

                        return null;
                    }
                    List<JvnRemoteServer> readers = readObjects.get(o);
                    for (JvnRemoteServer reader : readers) {
                        reader.jvnInvalidateReader(joi);
                    }
                    readObjects.remove(o);
                    writtenObjects.put(o, js);
                    return o.jvnGetObjectState();
                }

                // Check if it's lock write
                if (writtenObjects.get(o) != null) {
                    if (writtenObjects.get(o).equals(js)) {
                        return null;
                    }
                    Serializable s = writtenObjects.get(o).jvnInvalidateWriter(joi);
                    writtenObjects.remove(o);
                    JvnObject no = o;
                    no.jvnSetObjectState(s);
                    objectNames.put(entrySet.getKey(), no);
                    writtenObjects.put(no, js);
                    return s;
                }
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
        for (JvnObject o : readObjects.keySet()) {
            if (readObjects.get(o).contains(js)) {
                readObjects.get(o).remove(js);
            }
        }
        for (JvnObject o : writtenObjects.keySet()) {
            if (writtenObjects.get(o).equals(js)) {
                writtenObjects.remove(o);
            }
        }
    }

}
