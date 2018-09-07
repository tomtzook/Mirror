package com.mirror;

import com.mirror.helper.ReflectionHelper;
import com.mirror.helper.MirrorHelper;
import com.mirror.wrapping.ThrowableWrapper;
import com.mirror.wrapping.Unwrapper;
import com.mirror.wrapping.Wrapper;

import java.lang.reflect.Proxy;

public class MirrorCreator {

    private final ClassLoader mClassLoader;
    private final MirrorHelper mMirrorHelper;
    private final ReflectionHelper mReflectionHelper;
    private final ThrowableWrapper mThrowableWrapper;
    private final MirrorValidator mMirrorValidator;

    public MirrorCreator(ClassLoader classLoader, MirrorHelper mirrorHelper, ReflectionHelper reflectionHelper, ThrowableWrapper throwableWrapper, MirrorValidator mirrorValidator) {
        mClassLoader = classLoader;
        mMirrorHelper = mirrorHelper;
        mReflectionHelper = reflectionHelper;
        mThrowableWrapper = throwableWrapper;
        mMirrorValidator = mirrorValidator;
    }

    private MirrorCreator(ClassLoader classLoader) {
        mClassLoader = classLoader;
        mMirrorHelper = new MirrorHelper();
        mReflectionHelper = new ReflectionHelper(new Wrapper(mMirrorHelper, this), new Unwrapper(mMirrorHelper, mClassLoader));
        mThrowableWrapper = new ThrowableWrapper();
        mMirrorValidator = new MirrorValidator(mMirrorHelper);
    }

    public <T> Mirror<T> createMirror(Class<T> mirrorClass) throws MirrorCreationException {
        try {
            mMirrorValidator.validateMirrorClass(mirrorClass);
            Class<?> targetClass = getTargetType(mirrorClass);
            return new Mirror<T>(mirrorClass, targetClass, mReflectionHelper, mThrowableWrapper);
        } catch (ClassNotFoundException | ClassNotMirrorException | MirrorValidationException e) {
            throw new MirrorCreationException(e);
        }
    }

    public <T> T createMirrorFactory(Class<T> mirrorFactoryClass) throws MirrorFactoryCreationException {
        try {
            mMirrorValidator.validateMirrorFactoryClass(mirrorFactoryClass);

            Class<?> mirrorClass = mMirrorHelper.getMirrorFactoryType(mirrorFactoryClass);
            mMirrorValidator.validateMirrorClass(mirrorClass);

            Class<?> targetClass = getTargetType(mirrorClass);
            return createMirrorFactoryProxy(mirrorFactoryClass, mirrorClass, targetClass);
        } catch (ClassNotFoundException | ClassNotMirrorException | MirrorValidationException | ClassNotMirrorFactoryException e) {
            throw new MirrorFactoryCreationException(e);
        }
    }

    private Class<?> getTargetType(Class<?> mirrorClass) throws ClassNotFoundException {
        String targetTypeName = mMirrorHelper.getMirroredTypeName(mirrorClass);
        return Class.forName(targetTypeName, true, mClassLoader);
    }

    private <T> T createMirrorFactoryProxy(Class<T> mirrorFactoryClass, Class<?> mirrorClass, Class<?> targetClass) {
        return mirrorFactoryClass.cast(Proxy.newProxyInstance(
                mirrorFactoryClass.getClassLoader(),
                new Class[] {mirrorFactoryClass},
                new MirrorFactoryInvocationHandler(mReflectionHelper, mThrowableWrapper, targetClass, mirrorClass)));
    }

    public static MirrorCreator createForClassLoader(ClassLoader classLoader) {
        return new MirrorCreator(classLoader);
    }
}
