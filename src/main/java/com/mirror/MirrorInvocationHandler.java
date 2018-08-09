package com.mirror;

import com.mirror.helper.InvocationHelper;
import com.mirror.wrapping.UnwrappingException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class MirrorInvocationHandler implements InvocationHandler {

    private final InvocationHelper mInvocationHelper;
    private final Class<?> mTargetClass;
    private final Object mTargetInstance;

    public MirrorInvocationHandler(InvocationHelper invocationHelper, Class<?> targetClass, Object targetInstance) {
        mInvocationHelper = invocationHelper;
        mTargetClass = targetClass;
        mTargetInstance = targetInstance;
    }

    public Object getTargetInstance() {
        return mTargetInstance;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return invokeMethod(method, args);
    }

    public Object invokeMethod(Method method, Object[] args) throws Throwable {
        try {
            String mirroredMethodName = getMirroredMethodName(method);
            Method mirroredMethod = mInvocationHelper.findMirrorMethod(method, mirroredMethodName, mTargetClass);

            Object instance = Modifier.isStatic(mirroredMethod.getModifiers()) ? null : mTargetInstance;
            return mInvocationHelper.invokeMirrorMethod(mirroredMethod, instance, args);
        } catch (NoSuchMethodException | IllegalAccessException | UnwrappingException e) {
            throw new MirrorInvocationException(e);
        } catch (InvocationTargetException e) {
            // TODO: THROW INNER EXCEPTION IF DEFINED IN THROWS
            throw e.getCause();
        }
    }

    private String getMirroredMethodName(Method method) {
        return method.getName();
    }
}
