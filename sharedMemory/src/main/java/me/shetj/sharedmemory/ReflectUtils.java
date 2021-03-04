package me.shetj.sharedmemory;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ReflectUtils {
    private final Class<?> type;
    private final Object object;

    private ReflectUtils(Class<?> type) {
        this(type, type);
    }

    private ReflectUtils(Class<?> type, Object object) {
        this.type = type;
        this.object = object;
    }

    public static ReflectUtils reflect(String className) throws ReflectUtils.ReflectException {
        return reflect(forName(className));
    }

    public static ReflectUtils reflect(String className, ClassLoader classLoader) throws ReflectUtils.ReflectException {
        return reflect(forName(className, classLoader));
    }

    public static ReflectUtils reflect(Class<?> clazz) throws ReflectUtils.ReflectException {
        return new ReflectUtils(clazz);
    }

    public static ReflectUtils reflect(Object object) throws ReflectUtils.ReflectException {
        return new ReflectUtils(object == null ? Object.class : object.getClass(), object);
    }

    private static Class<?> forName(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException var2) {
            throw new ReflectUtils.ReflectException(var2);
        }
    }

    private static Class<?> forName(String name, ClassLoader classLoader) {
        try {
            return Class.forName(name, true, classLoader);
        } catch (ClassNotFoundException var3) {
            throw new ReflectUtils.ReflectException(var3);
        }
    }

    public ReflectUtils newInstance() {
        return this.newInstance();
    }

    public ReflectUtils newInstance(Object... args) {
        Class[] types = this.getArgsType(args);

        try {
            Constructor<?> constructor = this.type().getDeclaredConstructor(types);
            return this.newInstance(constructor, args);
        } catch (NoSuchMethodException var9) {
            List<Constructor<?>> list = new ArrayList();
            Constructor[] var5 = this.type().getDeclaredConstructors();
            int var6 = var5.length;

            for(int var7 = 0; var7 < var6; ++var7) {
                Constructor<?> constructor = var5[var7];
                if (this.match(constructor.getParameterTypes(), types)) {
                    list.add(constructor);
                }
            }

            if (list.isEmpty()) {
                throw new ReflectUtils.ReflectException(var9);
            } else {
                this.sortConstructors(list);
                return this.newInstance((Constructor)list.get(0), args);
            }
        }
    }

    private Class<?>[] getArgsType(Object... args) {
        if (args == null) {
            return new Class[0];
        } else {
            Class<?>[] result = new Class[args.length];

            for(int i = 0; i < args.length; ++i) {
                Object value = args[i];
                result[i] = value == null ? ReflectUtils.NULL.class : value.getClass();
            }

            return result;
        }
    }

    private void sortConstructors(List<Constructor<?>> list) {
        Collections.sort(list, new Comparator<Constructor<?>>() {
            public int compare(Constructor<?> o1, Constructor<?> o2) {
                Class<?>[] types1 = o1.getParameterTypes();
                Class<?>[] types2 = o2.getParameterTypes();
                int len = types1.length;

                for(int i = 0; i < len; ++i) {
                    if (!types1[i].equals(types2[i])) {
                        if (ReflectUtils.this.wrapper(types1[i]).isAssignableFrom(ReflectUtils.this.wrapper(types2[i]))) {
                            return 1;
                        }

                        return -1;
                    }
                }

                return 0;
            }
        });
    }

    private ReflectUtils newInstance(Constructor<?> constructor, Object... args) {
        try {
            return new ReflectUtils(constructor.getDeclaringClass(), ((Constructor)this.accessible(constructor)).newInstance(args));
        } catch (Exception var4) {
            throw new ReflectUtils.ReflectException(var4);
        }
    }

    public ReflectUtils field(String name) {
        try {
            Field field = this.getField(name);
            return new ReflectUtils(field.getType(), field.get(this.object));
        } catch (IllegalAccessException var3) {
            throw new ReflectUtils.ReflectException(var3);
        }
    }

    public ReflectUtils field(String name, Object value) {
        try {
            Field field = this.getField(name);
            field.set(this.object, this.unwrap(value));
            return this;
        } catch (Exception var4) {
            throw new ReflectUtils.ReflectException(var4);
        }
    }

    private Field getField(String name) throws IllegalAccessException {
        Field field = this.getAccessibleField(name);
        if ((field.getModifiers() & 16) == 16) {
            try {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(field, field.getModifiers() & -17);
            } catch (NoSuchFieldException var4) {
            }
        }

        return field;
    }

    private Field getAccessibleField(String name) {
        Class type = this.type();

        try {
            return (Field)this.accessible(type.getField(name));
        } catch (NoSuchFieldException var6) {
            while(true) {
                try {
                    return (Field)this.accessible(type.getDeclaredField(name));
                } catch (NoSuchFieldException var5) {
                    type = type.getSuperclass();
                    if (type == null) {
                        throw new ReflectUtils.ReflectException(var6);
                    }
                }
            }
        }
    }

    private Object unwrap(Object object) {
        return object instanceof ReflectUtils ? ((ReflectUtils)object).get() : object;
    }

    public ReflectUtils method(String name) throws ReflectUtils.ReflectException {
        return this.method(name);
    }

    public ReflectUtils method(String name, Object... args) throws ReflectUtils.ReflectException {
        Class[] types = this.getArgsType(args);

        try {
            Method method = this.exactMethod(name, types);
            return this.method(method, this.object, args);
        } catch (NoSuchMethodException var7) {
            try {
                Method method = this.similarMethod(name, types);
                return this.method(method, this.object, args);
            } catch (NoSuchMethodException var6) {
                throw new ReflectUtils.ReflectException(var6);
            }
        }
    }

    private ReflectUtils method(Method method, Object obj, Object... args) {
        try {
            this.accessible(method);
            if (method.getReturnType() == Void.TYPE) {
                method.invoke(obj, args);
                return reflect(obj);
            } else {
                return reflect(method.invoke(obj, args));
            }
        } catch (Exception var5) {
            throw new ReflectUtils.ReflectException(var5);
        }
    }

    private Method exactMethod(String name, Class<?>[] types) throws NoSuchMethodException {
        Class type = this.type();

        try {
            return type.getMethod(name, types);
        } catch (NoSuchMethodException var7) {
            while(true) {
                try {
                    return type.getDeclaredMethod(name, types);
                } catch (NoSuchMethodException var6) {
                    type = type.getSuperclass();
                    if (type == null) {
                        throw new NoSuchMethodException();
                    }
                }
            }
        }
    }

    private Method similarMethod(String name, Class<?>[] types) throws NoSuchMethodException {
        Class<?> type = this.type();
        List<Method> methods = new ArrayList();
        Method[] var5 = type.getMethods();
        int var6 = var5.length;

        int var7;
        Method method;
        for(var7 = 0; var7 < var6; ++var7) {
            method = var5[var7];
            if (this.isSimilarSignature(method, name, types)) {
                methods.add(method);
            }
        }

        if (!methods.isEmpty()) {
            this.sortMethods(methods);
            return (Method)methods.get(0);
        } else {
            do {
                var5 = type.getDeclaredMethods();
                var6 = var5.length;

                for(var7 = 0; var7 < var6; ++var7) {
                    method = var5[var7];
                    if (this.isSimilarSignature(method, name, types)) {
                        methods.add(method);
                    }
                }

                if (!methods.isEmpty()) {
                    this.sortMethods(methods);
                    return (Method)methods.get(0);
                }

                type = type.getSuperclass();
            } while(type != null);

            throw new NoSuchMethodException("No similar method " + name + " with params " + Arrays.toString(types) + " could be found on type " + this.type() + ".");
        }
    }

    private void sortMethods(List<Method> methods) {
        Collections.sort(methods, new Comparator<Method>() {
            public int compare(Method o1, Method o2) {
                Class<?>[] types1 = o1.getParameterTypes();
                Class<?>[] types2 = o2.getParameterTypes();
                int len = types1.length;

                for(int i = 0; i < len; ++i) {
                    if (!types1[i].equals(types2[i])) {
                        if (ReflectUtils.this.wrapper(types1[i]).isAssignableFrom(ReflectUtils.this.wrapper(types2[i]))) {
                            return 1;
                        }

                        return -1;
                    }
                }

                return 0;
            }
        });
    }

    private boolean isSimilarSignature(Method possiblyMatchingMethod, String desiredMethodName, Class<?>[] desiredParamTypes) {
        return possiblyMatchingMethod.getName().equals(desiredMethodName) && this.match(possiblyMatchingMethod.getParameterTypes(), desiredParamTypes);
    }

    private boolean match(Class<?>[] declaredTypes, Class<?>[] actualTypes) {
        if (declaredTypes.length == actualTypes.length) {
            for(int i = 0; i < actualTypes.length; ++i) {
                if (actualTypes[i] != ReflectUtils.NULL.class && !this.wrapper(declaredTypes[i]).isAssignableFrom(this.wrapper(actualTypes[i]))) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    private <T extends AccessibleObject> T accessible(T accessible) {
        if (accessible == null) {
            return null;
        } else {
            if (accessible instanceof Member) {
                Member member = (Member)accessible;
                if (Modifier.isPublic(member.getModifiers()) && Modifier.isPublic(member.getDeclaringClass().getModifiers())) {
                    return accessible;
                }
            }

            if (!accessible.isAccessible()) {
                accessible.setAccessible(true);
            }

            return accessible;
        }
    }

    private Class<?> type() {
        return this.type;
    }

    private Class<?> wrapper(Class<?> type) {
        if (type == null) {
            return null;
        } else {
            if (type.isPrimitive()) {
                if (Boolean.TYPE == type) {
                    return Boolean.class;
                }

                if (Integer.TYPE == type) {
                    return Integer.class;
                }

                if (Long.TYPE == type) {
                    return Long.class;
                }

                if (Short.TYPE == type) {
                    return Short.class;
                }

                if (Byte.TYPE == type) {
                    return Byte.class;
                }

                if (Double.TYPE == type) {
                    return Double.class;
                }

                if (Float.TYPE == type) {
                    return Float.class;
                }

                if (Character.TYPE == type) {
                    return Character.class;
                }

                if (Void.TYPE == type) {
                    return Void.class;
                }
            }

            return type;
        }
    }

    public <T> T get() {
        return (T) this.object;
    }

    public int hashCode() {
        return this.object.hashCode();
    }

    public boolean equals(Object obj) {
        return obj instanceof ReflectUtils && this.object.equals(((ReflectUtils)obj).get());
    }

    public String toString() {
        return this.object.toString();
    }

    public static Object getInstance(String className, Object... params) {
        if (className != null && !className.equals("")) {
            try {
                Class<?> c = Class.forName(className);
                if (params == null) {
                    Constructor constructor = c.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    return constructor.newInstance();
                } else {
                    int plength = params.length;
                    Class[] paramsTypes = new Class[plength];

                    for(int i = 0; i < plength; ++i) {
                        paramsTypes[i] = params[i].getClass();
                    }

                    Constructor constructor = c.getDeclaredConstructor(paramsTypes);
                    constructor.setAccessible(true);
                    return constructor.newInstance(params);
                }
            } catch (Exception var6) {
                var6.printStackTrace();
                return null;
            }
        } else {
            throw new IllegalArgumentException("className 不能为空");
        }
    }

    public static Object invoke(String className, Object instance, String methodName, Object... params) {
        if (className != null && !className.equals("")) {
            if (methodName != null && !methodName.equals("")) {
                try {
                    Class<?> c = Class.forName(className);
                    if (params == null) {
                        Method method = c.getDeclaredMethod(methodName);
                        method.setAccessible(true);
                        return method.invoke(instance);
                    } else {
                        int plength = params.length;
                        Class[] paramsTypes = new Class[plength];

                        for(int i = 0; i < plength; ++i) {
                            paramsTypes[i] = params[i].getClass();
                        }

                        Method method = c.getDeclaredMethod(methodName, paramsTypes);
                        method.setAccessible(true);
                        return method.invoke(instance, params);
                    }
                } catch (Exception var8) {
                    var8.printStackTrace();
                    return null;
                }
            } else {
                throw new IllegalArgumentException("methodName不能为空");
            }
        } else {
            throw new IllegalArgumentException("className 不能为空");
        }
    }

    public static Object invokeMethod(Object instance, Method m, Object... params) {
        if (m == null) {
            throw new IllegalArgumentException("method 不能为空");
        } else {
            m.setAccessible(true);

            try {
                return m.invoke(instance, params);
            } catch (Exception var4) {
                var4.printStackTrace();
                return null;
            }
        }
    }

    public static Object getField(String className, Object instance, String fieldName) {
        if (className != null && !className.equals("")) {
            if (fieldName != null && !fieldName.equals("")) {
                try {
                    Class c = Class.forName(className);
                    Field field = c.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return field.get(instance);
                } catch (Exception var5) {
                    var5.printStackTrace();
                    return null;
                }
            } else {
                throw new IllegalArgumentException("fieldName 不能为空");
            }
        } else {
            throw new IllegalArgumentException("className 不能为空");
        }
    }

    public static void setField(String className, Object instance, String fieldName, Object value) {
        if (className != null && !className.equals("")) {
            if (fieldName != null && !fieldName.equals("")) {
                try {
                    Class<?> c = Class.forName(className);
                    Field field = c.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(instance, value);
                } catch (Exception var6) {
                    var6.printStackTrace();
                }

            } else {
                throw new IllegalArgumentException("fieldName 不能为空");
            }
        } else {
            throw new IllegalArgumentException("className 不能为空");
        }
    }

    public static Method getMethod(String className, String methodName, Class... paramsType) {
        if (className != null && !className.equals("")) {
            if (methodName != null && !methodName.equals("")) {
                try {
                    Class<?> c = Class.forName(className);
                    return c.getDeclaredMethod(methodName, paramsType);
                } catch (Exception var4) {
                    var4.printStackTrace();
                    return null;
                }
            } else {
                throw new IllegalArgumentException("methodName不能为空");
            }
        } else {
            throw new IllegalArgumentException("className 不能为空");
        }
    }

    public static class ReflectException extends RuntimeException {
        private static final long serialVersionUID = 858774075258496016L;

        public ReflectException(String message) {
            super(message);
        }

        public ReflectException(String message, Throwable cause) {
            super(message, cause);
        }

        public ReflectException(Throwable cause) {
            super(cause);
        }
    }

    private static class NULL {
        private NULL() {
        }
    }
}
