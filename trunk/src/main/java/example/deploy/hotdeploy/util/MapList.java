
package example.deploy.hotdeploy.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MapList {
    public static <F, T> List<T> map(List<F> from, Mapping<F, T> mapping) {
        ArrayList<T> result = new ArrayList<T>();

        map(from, result, mapping);

        return result;
    }

    public static <F, T> Set<T> map(Set<F> from, Mapping<F, T> mapping) {
        HashSet<T> result = new HashSet<T>();

        map(from, result, mapping);

        return result;
    }

    public static <F, T> Collection<T> map(Iterable<F> from, Collection<T> to, Mapping<F, T> mapping) {
        for (F f : from) {
            to.add(mapping.map(f));
        }

        return to;
    }
}
