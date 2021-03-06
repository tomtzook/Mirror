package com.mirror.invocation;

import com.mirror.MirrorCreator;
import com.mirror.validation.MirrorValidator;
import com.mirror.helper.MirrorHelper;
import com.mirror.helper.ReflectionHelper;
import com.mirror.wrapping.ThrowableWrapper;
import com.mirror.wrapping.UnwrappingException;
import com.mirror.wrapping.WrappingException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

public class MirrorCreatorInvocationHandler implements InvocationHandler {

    private final ReflectionHelper mReflectionHelper;
    private final ThrowableWrapper mThrowableWrapper;
    private final MirrorHelper mMirrorHelper;
    private final MirrorValidator mMirrorValidator;
    private final ClassLoader mClassLoader;

    public MirrorCreatorInvocationHandler(ReflectionHelper reflectionHelper, ThrowableWrapper throwableWrapper, MirrorHelper mirrorHelper, MirrorValidator mirrorValidator, ClassLoader classLoader) {
        mReflectionHelper = reflectionHelper;
        mThrowableWrapper = throwableWrapper;
        mMirrorHelper = mirrorHelper;
        mMirrorValidator = mirrorValidator;
        mClassLoader = classLoader;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (args == null) {
            args = new Object[0];
        }

        if (method.isAnnotationPresent(MirrorCreator.class)) {
            return createMirror(method.getReturnType(), method, args);
        }

        throw new MirrorCreationByProxyException(String.format("method %s has no invocation", method.getName()));
    }

    private Object createMirror(Class<?> mirrorClass, Method method, Object[] args) throws Throwable {
        try {
            mMirrorValidator.validateMirrorClass(mirrorClass);
            Class<?> targetClass = mMirrorHelper.getMirrorTargetType(mirrorClass, mClassLoader);

            return invokeConstructor(mirrorClass, targetClass, method, args);
        } catch (ClassNotFoundException e) {
            throw new MirrorCreationByProxyException(e);
        }
    }

    private Object invokeConstructor(Class<?> mirrorClass, Class<?> targetClass, Method method, Object[] args) throws Throwable {
        try {
            Constructor<?> constructor = mReflectionHelper.findMirrorConstructor(method, targetClass);
            return mReflectionHelper.invokeMirrorConstructor(constructor, mirrorClass, args);
        } catch (UnwrappingException | WrappingException | IllegalAccessException | InstantiationException | NoSuchMethodException e) {
            throw new MirrorCreationByProxyException(e);
        } catch (InvocationTargetException e) {
            Optional<Throwable> optionalThrowable = mThrowableWrapper.tryWrapThrowable(e.getCause(), method.getExceptionTypes(), mClassLoader);
            if (optionalThrowable.isPresent()) {
                throw optionalThrowable.get();
            }

            throw e.getCause();
        }
    }
}
