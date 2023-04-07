package helper;

import java.util.List;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.mockito.MockedConstruction;

import static org.mockito.Mockito.mockConstruction;

/**
 * Helper class to clean up mocking
 * @param <T>       Type of class that needs a constructor mocking
 */
public class MockConstructor<T> implements AutoCloseable {
    private final MockedConstruction<T> backing;

    private List<?> arguments;
    private T mock;


    public MockConstructor(@Nonnull Class<T> target) {
        this(target, m -> {} );
    }

    public MockConstructor(@Nonnull Class<T> target, @Nonnull Consumer<T> init) {
       this.backing = mockConstruction(target,
            (mock, context) -> {
                this.mock = mock;
                this.arguments = context.arguments();

                init.accept(mock);
            });
    }


    @Nonnull
    public T getMock() {
        if (mock == null) {
            throw new RuntimeException("ERROR: constructor has not been called");
        }

        return mock;
    }


    @Nullable
    public <A> A getArgument(int index, @Nonnull Class<A> type) {
        if (arguments == null) {
            throw new RuntimeException("ERROR: constructor has not been called");
        }

        return type.cast(arguments.get(index));
    }


    @Override
    public void close() {
        backing.close();
    }
}
