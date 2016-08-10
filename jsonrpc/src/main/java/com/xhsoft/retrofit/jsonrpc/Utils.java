package com.xhsoft.retrofit.jsonrpc;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

final class Utils {
  private Utils() {
    throw new AssertionError("No instances");
  }

  /**
   * Returns true if {@code annotations} contains an instance of {@code cls}.
   */
  static <T extends Annotation> boolean isAnnotationPresent(Annotation[] annotations,
                                                            Class<T> cls) {
    return findAnnotation(annotations, cls) != null;
  }

  /**
   * Returns an instance of {@code cls} if {@code annotations} contains an instance.
   */
  static <T extends Annotation> T findAnnotation(Annotation[] annotations, Class<T> cls) {
    for (Annotation annotation : annotations) {
      if (cls.isInstance(annotation)) {
        //noinspection unchecked
        return (T) annotation;
      }
    }
    return null;
  }

  public static Class<?> getRawType(Type type) {
    if (type instanceof Class<?>) {
      // Type is a normal class.
      return (Class<?>) type;

    } else if (type instanceof ParameterizedType) {
      ParameterizedType parameterizedType = (ParameterizedType) type;

      // I'm not exactly sure why getRawType() returns Type instead of Class. Neal isn't either but
      // suspects some pathological case related to nested classes exists.
      Type rawType = parameterizedType.getRawType();
      if (!(rawType instanceof Class)) {
        throw new IllegalArgumentException();
      }
      return (Class<?>) rawType;

    } else if (type instanceof GenericArrayType) {
      Type componentType = ((GenericArrayType) type).getGenericComponentType();
      return Array.newInstance(getRawType(componentType), 0).getClass();

    } else if (type instanceof TypeVariable) {
      // We could use the variable's bounds, but that won't work if there are multiple. Having a raw
      // type that's more general than necessary is okay.
      return Object.class;

    } else if (type instanceof WildcardType) {
      return getRawType(((WildcardType) type).getUpperBounds()[0]);

    } else {
      String className = type == null ? "null" : type.getClass().getName();
      throw new IllegalArgumentException("Expected a Class, ParameterizedType, or "
          + "GenericArrayType, but <" + type + "> is of type " + className);
    }
  }
}
