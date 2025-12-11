package ui11.observable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class BindingUtil {

    public static <T> void compareAndCopy(Collection<? extends T> src, Collection<T> target,
                                          Consumer<? super T> add, Consumer<? super T> remove) {
        for (Iterator<T> iterator = target.iterator(); iterator.hasNext(); ) {
            T t = iterator.next();
            if (src.contains(t))
                continue;
            iterator.remove();
            remove.accept(t);
        }
        for (T t : src)
            if (target.add(t))
                add.accept(t);
    }

    public static <SRC, DST> List<DST> mapList(List<SRC> src, Function<SRC, DST> function) {
        return new AbstractList<DST>() {

            @Override
            public DST get(int index) {
                return function.apply(src.get(index));
            }

            @Override
            public int size() {
                return src.size();
            }
        };
    }
}
