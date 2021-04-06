package javaquack;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.FieldManifestation;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.FieldAccessor;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

public class DuckTyping {

    private static final String DELEGATE_FIELD_NAME = "duckTypingDelegate";

    private static final String GENERATED_CLASS_NAME_DELIMITER = "DelegatedTo";

    private static final ConcurrentMap<String, Class<?>> cache = new ConcurrentHashMap<>();

    public static void warmUp(Class<?> sourceClass, Class<?> destinationInterface) {
        try {
            validateDestinationInterface(destinationInterface);
            getOrComputeEntryForCacheIfAbsent(sourceClass, destinationInterface);
        } catch (Exception e) {
            throw new DuckTypingException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <S, D> D cast(S sourceObject, Class<D> destinationInterface) {
        try {
            validateDestinationInterface(destinationInterface);

            if (sourceObject == null) {
                return null;
            }
            if (destinationInterface.isInstance(sourceObject)) {
                return destinationInterface.cast(sourceObject);
            }

            Class<S> sourceClass = (Class<S>) sourceObject.getClass();

            return getOrComputeEntryForCacheIfAbsent(sourceClass, destinationInterface)
                .getConstructor(sourceClass)
                .newInstance(sourceObject);
        } catch (Exception e) {
            throw new DuckTypingException(e);
        }
    }

    public static <S, D> String generateName(Class<S> sourceClass, Class<D> destinationInterface) {
        StringBuilder stringBuilder =
            new StringBuilder()
                .append(destinationInterface.getCanonicalName())
                .append(GENERATED_CLASS_NAME_DELIMITER);

        boolean makeUppercase = true;
        for (char c : sourceClass.getCanonicalName().toCharArray()) {
            if (makeUppercase) {
                stringBuilder.append(Character.toUpperCase(c));
                makeUppercase = false;
            } else if (c == '.') {
                makeUppercase = true;
            } else {
                stringBuilder.append(c);
            }
        }

        return stringBuilder.toString();
    }

    public static Map<String, Class<?>> getCache() {
        return Collections.unmodifiableMap(cache);
    }

    private static void validateDestinationInterface(Class<?> destinationInterface) {
        if (destinationInterface == null) {
            throw new IllegalStateException("Destination interface cannot be null");
        }
        if (!destinationInterface.isInterface()) {
            throw new IllegalStateException(destinationInterface.getCanonicalName() + " is not interface");
        }
    }

    @SuppressWarnings("unchecked")
    private static <S, D> Class<? extends D> getOrComputeEntryForCacheIfAbsent(Class<S> sourceClass, Class<D> destinationInterface) {
        return (Class<? extends D>) cache.computeIfAbsent(
            generateName(sourceClass, destinationInterface),
            key -> generateClass(sourceClass, destinationInterface, key));
    }

    private static <S, D> Class<? extends D> generateClass(Class<S> sourceClass, Class<D> destinationInterface, String name) {
        try {
            DynamicType.Builder<? extends D> dynamicTypeBuilder =
                new ByteBuddy()
                    .subclass(destinationInterface)
                    .name(name)
                    .defineField(DELEGATE_FIELD_NAME, sourceClass, Visibility.PRIVATE, FieldManifestation.FINAL)
                    .defineConstructor(Visibility.PUBLIC)
                    .withParameter(sourceClass)
                    .intercept(
                        MethodCall
                            .invoke(Object.class.getConstructor())
                            .onSuper()
                            .andThen(FieldAccessor.ofField(DELEGATE_FIELD_NAME).setsArgumentAt(0)));

            for (Method method : collectAllNonDefaultDeclaredMethods(destinationInterface)) {
                dynamicTypeBuilder =
                    dynamicTypeBuilder
                        .method(ElementMatchers.is(method))
                        .intercept(
                            MethodDelegation
                                .withDefaultConfiguration()
                                .filter(
                                    ElementMatchers
                                        .hasMethodName(method.getName())
                                        .and(ElementMatchers.returns(method.getReturnType()))
                                        .and(ElementMatchers.takesArguments(method.getParameterTypes())))
                                .toField(DELEGATE_FIELD_NAME));
            }

            return dynamicTypeBuilder
                .make()
                .load(destinationInterface.getClassLoader())
                .getLoaded();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private static List<Method> collectAllNonDefaultDeclaredMethods(Class<?> parentInterface) {
        List<Method> nonDefaultDeclaredMethods =
            Arrays.stream(parentInterface.getDeclaredMethods())
                .filter(method -> !method.isDefault())
                .collect(Collectors.toList());

        for (Class<?> childInterface : parentInterface.getInterfaces()) {
            nonDefaultDeclaredMethods.addAll(collectAllNonDefaultDeclaredMethods(childInterface));
        }

        return nonDefaultDeclaredMethods;
    }

}
