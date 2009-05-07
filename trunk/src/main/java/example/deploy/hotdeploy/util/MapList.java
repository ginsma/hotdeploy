
package example.deploy.hotdeploy.util;

import java.util.ArrayList;
import java.util.List;

public class MapList {
    public static <F, T> List<T> map(List<F> from, Mapping<F, T> mapping) {
        ArrayList<T> result = new ArrayList<T>();

        for (F f : from) {
            result.add(mapping.map(f));
        }

        return result;
    }
}
