package javaquack;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.description.modifier.Visibility;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DuckTyping {

    public static final String QUACK_DELEGATE_FIELD_NAME = "quackDelegate";

    public static final String GENERATED_CLASS_NAME_DELIMITER = "DelegationTo";

    public static final String SOURCE_CLASS_CANONICAL_NAME_SPLIT_REGEX = "\\.";

    protected static final Map<String, Class<?>> cache = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <S, D> D cast(S sourceObject, Class<D> destinationInterface) {
        try {
            if (sourceObject == null) {
                return null;
            }

            if (destinationInterface.isInstance(sourceObject)) {
                return destinationInterface.cast(sourceObject);
            }

            Class<? extends D> generatedClass =
                (Class<? extends D>) cache.computeIfAbsent(
                    generateName(sourceObject.getClass(), destinationInterface),
                    key -> generateClass(sourceObject.getClass(), destinationInterface, key));

            D generatedClassInstance = generatedClass.newInstance();

            generatedClassInstance.getClass().getField(QUACK_DELEGATE_FIELD_NAME).set(generatedClassInstance, sourceObject);

            return destinationInterface.cast(generatedClassInstance);
        } catch (Exception e) {
            throw new DuckTypingException(e);
        }
    }

    public static <S, D> String generateName(Class<S> sourceClass, Class<D> destinationInterface) {
        return destinationInterface.getCanonicalName() +
            GENERATED_CLASS_NAME_DELIMITER +
            Arrays.stream(sourceClass.getCanonicalName().split(SOURCE_CLASS_CANONICAL_NAME_SPLIT_REGEX))
                .map(token -> token.substring(0, 1).toUpperCase() + token.substring(1))
                .reduce("", String::concat);
    }

    private static <S, D> Class<? extends D> generateClass(Class<S> sourceClass, Class<D> destinationInterface, String name) {
        DynamicType.Builder<? extends D> dynamicTypeBuilder =
            new ByteBuddy()
                .subclass(destinationInterface)
                .defineField(QUACK_DELEGATE_FIELD_NAME, sourceClass, Visibility.PUBLIC)
                .name(name);

        for (Method method : destinationInterface.getMethods()) {
            dynamicTypeBuilder =
                dynamicTypeBuilder
                    .method(ElementMatchers.isDeclaredBy(destinationInterface).and(ElementMatchers.is(method)))
                    .intercept(MethodDelegation.toField(QUACK_DELEGATE_FIELD_NAME));
        }

        return dynamicTypeBuilder
            .make()
            .load(destinationInterface.getClassLoader())
            .getLoaded();
    }

}
