package ui11;

import ui11.reflectutil.ReflectionUtil;

import java.util.*;

import static java.util.stream.Collectors.joining;

abstract class IVCombiner<T> {

    abstract void add(T value, long priority);

    abstract T finish();

    @SuppressWarnings("unchecked")
    static <T> IVCombiner<T> forType(Class<T> type) {
        if (type == ResolutionRequestCollection.class)
            return (IVCombiner<T>) new ReqCollIVCombiner();
        else
            return new SelectingIVCombiner<>() {

                @Override
                protected String typeDebugName() {
                    return "inherited value " + ReflectionUtil.simpleName(type);
                }
            };
    }

    private static abstract class SelectingIVCombiner<T> extends IVCombiner<T> {

        private final Map<T, List<Long>> differentValues = new IdentityHashMap<>();

        protected abstract String typeDebugName();

        @Override
        void add(T value, long priority) {
            differentValues.computeIfAbsent(value, __ -> new ArrayList<>()).add(priority);
        }

        @Override
        T finish() {
            assert !differentValues.isEmpty();

            if (differentValues.size() == 1)
                return differentValues.keySet().iterator().next();

            if (differentValues.size() == 2) {
                Object[] arr = differentValues.keySet().toArray(Object[]::new);
                @SuppressWarnings("unchecked")
                T a = (T) arr[0], b = (T) arr[1];
                long aOrigin = singlePriority(a), bOrigin = singlePriority(b);
                return selectMostRecent(a, aOrigin, b, bOrigin);
            }

            StringBuilder sb = new StringBuilder();
            sb.append(differentValues.size()).append(" different values for ");
            sb.append(typeDebugName()).append(":");
            for (Map.Entry<T, List<Long>> e : differentValues.entrySet()) {
                sb.append("\n- ").append(e.getKey()).append(" ").append(e.getKey().hashCode()).append(
                        e.getValue().stream().map(String::valueOf).collect(joining(", ", " (priority: ", ")")));
            }
            throw new RuntimeException(sb.toString());
        }

        private long singlePriority(T t) {
            List<Long> origins = differentValues.get(t);
            if (origins.size() == 1)
                return origins.getFirst();
            else
                throw new RuntimeException("TODO more than one origin for IV value: " + t);
        }

        private T selectMostRecent(T a, long timeA, T b, long timeB) {
            if (timeA < timeB)
                return b;
            if (timeA > timeB)
                return a;

            // TODO itt List.of és Map.of nem jó, mert a és b lehet null
            throw new RuntimeException("Can't decide between two reqs (times: " + timeA + ", " + timeB + ")");
        }
    }

    private static class ReqCollIVCombiner extends IVCombiner<ResolutionRequestCollection> {

        private final Set<ResolutionRequest<?>> combined = new HashSet<>();
        private final Map<Class<? extends ExposeRequest<?>>, SelectingIVCombiner<ResolutionRequest<?>>> combiners =
                new HashMap<>();

        @Override
        void add(ResolutionRequestCollection value, long priority) {
            for (ResolutionRequest<?> req : value.requests()) {
                Class<? extends ExposeRequest<?>> base = req.metadata().baseForAtMostOnce;
                if (base == null) {
                    combined.add(req);
                } else {
                    combiners.computeIfAbsent(base, base2 -> new SelectingIVCombiner<>() {
                        @Override
                        protected String typeDebugName() {
                            return ReflectionUtil.simpleName(base2) +
                                " based " + ExposeRequest.class.getSimpleName() + "s";
                        }
                    }).add(req, priority);
                }
            }
        }

        @Override
        ResolutionRequestCollection finish() {
            combiners.forEach((base, combiner) -> {
                ResolutionRequest<?> req = combiner.finish();
                assert base.isAssignableFrom(req.requestData.getClass());
                boolean added = combined.add(req);
                // duplikált nem lehet, mert akkor a baseForAtMostOnce más lenne
                assert added;
            });
            return new ResolutionRequestCollection(combined);
        }
    }
}
