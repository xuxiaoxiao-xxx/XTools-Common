package me.xuxiaoxiao.xtools.common.reflect;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.function.Function;

@SuppressWarnings("ALL")
public class XReflectTools {

    @Nullable
    public <T> T accessField(@Nonnull Class<?> clazz, @Nonnull String field, Function<Field, T> function) {
        for (Class<?> c = clazz; c != null && !c.equals(Object.class); c = c.getSuperclass()) {
            Field[] fields = c.getDeclaredFields();
            for (Field f : fields) {
                if (f.getName().equals(field)) {
                    if ((!Modifier.isPublic(f.getModifiers()) || !Modifier.isPublic(f.getDeclaringClass().getModifiers()) || Modifier.isFinal(f.getModifiers())) && !f.isAccessible()) {
                        f.setAccessible(true);
                    }
                    return function.apply(f);
                }
            }
        }
        throw new IllegalArgumentException(String.format("Cannot find field:[%s] in class:<%s>", field, clazz.getSimpleName()));
    }

    @Nullable
    public <T> T accessMethod(@Nonnull Class<?> clazz, @Nonnull String field, Function<Method, T> function) {
        for (Class<?> c = clazz; c != null && !c.equals(Object.class); c = c.getSuperclass()) {
            Method[] methods = c.getDeclaredMethods();
            for (Method m : methods) {
                if (m.getName().equals(field)) {
                    if ((!Modifier.isPublic(m.getModifiers()) || !Modifier.isPublic(m.getDeclaringClass().getModifiers()) || Modifier.isFinal(m.getModifiers())) && !m.isAccessible()) {
                        m.setAccessible(true);
                    }
                    return function.apply(m);
                }
            }
        }
        throw new IllegalArgumentException(String.format("Cannot find method:[%s] in class:<%s>", field, clazz.getSimpleName()));
    }

    @Nullable
    public <T> T getFieldValue(@Nonnull Object obj, @Nonnull String field) {
        Objects.requireNonNull(obj, "Parameter 'obj' cannot be null");
        Objects.requireNonNull(field, "Parameter 'field' cannot be null");

        return accessField(obj.getClass(), field, new Function<Field, T>() {

            @Override
            public T apply(Field field) {
                try {
                    return (T) field.get(obj);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Nullable
    public <T> T getFieldValue(@Nonnull Class<?> clazz, @Nonnull String field) {
        Objects.requireNonNull(clazz, "Parameter 'clazz' cannot be null");
        Objects.requireNonNull(field, "Parameter 'field' cannot be null");

        return accessField(clazz, field, new Function<Field, T>() {

            @Override
            public T apply(Field field) {
                try {
                    return (T) field.get(null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void setFieldValue(@Nonnull Object obj, @Nonnull String field, Object value) {
        Objects.requireNonNull(obj, "Parameter 'clazz' cannot be null");
        Objects.requireNonNull(field, "Parameter 'field' cannot be null");

        accessField(obj.getClass(), field, new Function<Field, Void>() {
            @Override
            public Void apply(Field field) {
                try {
                    field.set(null, value);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });
    }

    public void setFieldValue(@Nonnull Class<?> clazz, @Nonnull String field, Object value) {
        Objects.requireNonNull(clazz, "Parameter 'clazz' cannot be null");
        Objects.requireNonNull(field, "Parameter 'field' cannot be null");

        accessField(clazz, field, new Function<Field, Void>() {
            @Override
            public Void apply(Field field) {
                try {
                    field.set(null, value);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            }
        });
    }

    public <T> T invokeMethod(@Nonnull Object obj, @Nonnull String method, @Nullable Object... args) {
        Objects.requireNonNull(obj, "Parameter 'obj' cannot be null");
        Objects.requireNonNull(method, "Parameter 'method' cannot be null");

        return accessMethod(obj.getClass(), "method", new Function<Method, T>() {
            @Override
            public T apply(Method method) {
                try {
                    return (T) method.invoke(obj, args);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public <T> T invokeMethod(@Nonnull Class<?> clazz, @Nonnull String method, @Nullable Object... args) {
        Objects.requireNonNull(clazz, "Parameter 'clazz cannot be null");
        Objects.requireNonNull(method, "Parameter 'method' cannot be null");

        return accessMethod(clazz, "method", new Function<Method, T>() {
            @Override
            public T apply(Method method) {
                try {
                    return (T) method.invoke(null, args);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}
