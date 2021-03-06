package com.mirror.helper;

import com.mirror.wrapping.Unwrapper;
import com.mirror.wrapping.UnwrappingException;
import com.mirror.wrapping.Wrapper;
import com.mirror.wrapping.WrappingException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ReflectionHelper {

    private final Wrapper mWrapper;
    private final Unwrapper mUnwrapper;

    public ReflectionHelper(Wrapper wrapper, Unwrapper unwrapper) {
        mWrapper = wrapper;
        mUnwrapper = unwrapper;
    }

    public Object[] unwrapParameters(Object... parameters) throws UnwrappingException {
        List<Object> unwrappedParameters = new ArrayList<Object>();

        for (Object parameter : parameters) {
            Object unwrapped = mUnwrapper.unwrap(parameter);
            unwrappedParameters.add(unwrapped);
        }

        return unwrappedParameters.toArray();
    }

    public Class<?>[] unwrapParameterTypes(Method method) throws UnwrappingException {
        List<Class<?>> parameterTypes = new ArrayList<Class<?>>();

        for (Class<?> type : method.getParameterTypes()) {
            Class<?> unwrapped = mUnwrapper.unwrapType(type);
            parameterTypes.add(unwrapped);
        }

        Class<?>[] parameterTypesArr = new Class[parameterTypes.size()];
        return parameterTypes.toArray(parameterTypesArr);
    }

    public Method findMirrorMethod(Method method, String methodName, Class<?> targetClass) throws NoSuchMethodException, UnwrappingException {
        Class<?>[] parameterTypes = unwrapParameterTypes(method);
        try {
            return targetClass.getDeclaredMethod(methodName, parameterTypes);
        } catch (NoSuchMethodException e) {
            return targetClass.getMethod(methodName, parameterTypes);
        }
    }

    public Object invokeMirrorMethod(Method method, Object instance, Class<?> returnType, Object... parameters) throws InvocationTargetException, IllegalAccessException, UnwrappingException, WrappingException {
        Object[] unwrappedParameters = unwrapParameters(parameters);

        method.setAccessible(true);
        Object result = method.invoke(instance, unwrappedParameters);

        return mWrapper.wrap(result, returnType);
    }

    public Field findMirrorField(String fieldName, Class<?> targetClass) throws NoSuchFieldException {
        try {
            return targetClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            return targetClass.getField(fieldName);
        }
    }

    public Object getFieldValue(Field field, Object instance, Class<?> returnType) throws IllegalAccessException, WrappingException {
        field.setAccessible(true);
        Object result = field.get(instance);

        return mWrapper.wrap(result, returnType);
    }

    public void setFieldValue(Field field, Object instance, Object value) throws UnwrappingException, IllegalAccessException {
        field.setAccessible(true);

        Object unwrappedValue = mUnwrapper.unwrap(value);
        field.set(instance, unwrappedValue);
    }

    public Constructor<?> findMirrorConstructor(Method method, Class<?> targetClass) throws UnwrappingException, NoSuchMethodException {
        Class<?>[] parameterTypes = unwrapParameterTypes(method);
        try {
            return targetClass.getDeclaredConstructor(parameterTypes);
        } catch (NoSuchMethodException e) {
            return targetClass.getConstructor(parameterTypes);
        }
    }

    public Object invokeMirrorConstructor(Constructor<?> constructor, Class<?> returnType, Object... parameters) throws UnwrappingException, IllegalAccessException, InvocationTargetException, InstantiationException, WrappingException {
        Object[] unwrappedParameters = unwrapParameters(parameters);

        constructor.setAccessible(true);
        Object result = constructor.newInstance(unwrappedParameters);

        return mWrapper.wrap(result, returnType);
    }
}
