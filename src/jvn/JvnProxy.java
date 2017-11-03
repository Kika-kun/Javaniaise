/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jvn;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 *
 * @author Kikaha
 */
public class JvnProxy implements InvocationHandler {

    private JvnObject jo;
    
    public JvnProxy(Serializable o, String name) throws JvnException {
        JvnServerImpl js = JvnServerImpl.jvnGetServer();
        if (js.jvnLookupObject(name) == null) {
            jo = js.jvnCreateObject(o);
            jo.jvnUnLock();
            js.jvnRegisterObject(name, jo);
        } else {
            jo = js.jvnLookupObject(name);
        }
    }
    
    public static Object createObject(Serializable obj, String name) throws JvnException {
        return java.lang.reflect.Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(), new JvnProxy(obj, name));
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // On récupère le type de la methode (read/write)
        LockType annotation = method.getAnnotation(LockType.class);
        switch (annotation.type()) {
            case READ:
                jo.jvnLockRead();
                break;
            case WRITE:
                jo.jvnLockWrite();
                break;
            default:
                System.err.println("Probleme lors de la lecture de l'annotation");
        }
        
        Object r = method.invoke(jo.jvnGetObjectState(), args);
        
        jo.jvnUnLock();
        return r;
    }
    
    
}
