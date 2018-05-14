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
            if (!destinationInterface.isInterface()) {
                throw new IllegalStateException(destinationInterface.getCanonicalName() + " is not interface");
            }

            if (sourceObject == null) {
                return null;
            }

            if (destinationInterface.isInstance(sourceObject)) {
                return destinationInterface.cast(sourceObject);
            }

            Class<S> sourceClass = (Class<S>) sourceObject.getClass();

            Class<? extends D> generatedClass =
                (Class<? extends D>) cache.computeIfAbsent(
                    generateName(sourceClass, destinationInterface),
                    key -> generateClass(sourceClass, destinationInterface, key));

            return generatedClass.getConstructor(sourceClass).newInstance(sourceObject);
        } catch (Exception e) {
            throw new DuckTypingException(e);
        }
    }

    public static <S, D> String generateName(Class<S> sourceClass, Class<D> destinationInterface) {
        StringBuilder stringBuilder =
            new StringBuilder()
                .append(destinationInterface.getCanonicalName())
                .append(GENERATED_CLASS_NAME_DELIMITER);

        for (String sourceClassCanonicalNameToken : sourceClass.getCanonicalName().split(SOURCE_CLASS_CANONICAL_NAME_SPLIT_REGEX)) {
            stringBuilder =
                stringBuilder
                    .append(sourceClassCanonicalNameToken.substring(0, 1).toUpperCase())
                    .append(sourceClassCanonicalNameToken.substring(1));
        }

        return stringBuilder.toString();
    }

    private static <S, D> Class<? extends D> generateClass(Class<S> sourceClass, Class<D> destinationInterface, String name) {
        try {
            DynamicType.Builder<? extends D> dynamicTypeBuilder =
                new ByteBuddy()
                    .subclass(destinationInterface)
                    .name(name)
                    .defineField(QUACK_DELEGATE_FIELD_NAME, sourceClass, Visibility.PRIVATE, FieldManifestation.FINAL)
                    .defineConstructor(Visibility.PUBLIC)
                    .withParameter(sourceClass)
                    .intercept(MethodCall.invoke(Object.class.getConstructor()).onSuper().andThen(FieldAccessor.ofField(QUACK_DELEGATE_FIELD_NAME).setsArgumentAt(0)));

            for (Method method : destinationInterface.getMethods()) {
                dynamicTypeBuilder =
                    dynamicTypeBuilder
                        .method(ElementMatchers.is(method))
                        .intercept(MethodDelegation.toField(QUACK_DELEGATE_FIELD_NAME));
            }

            return dynamicTypeBuilder
                .make()
                .load(destinationInterface.getClassLoader())
                .getLoaded();
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

}
