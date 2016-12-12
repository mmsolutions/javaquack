package javaquack;

public class DuckTypingSingleton {

    private static final DuckTyping DUCK_TYPING = new DuckTyping();

    public static <S, D> D cast(S sourceObject, Class<D> destinationInterface) {
        return DUCK_TYPING.cast(sourceObject, destinationInterface);
    }

}
