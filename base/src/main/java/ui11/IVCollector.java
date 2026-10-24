package ui11;

import org.jspecify.annotations.NonNull;
import ui11.provide.DynamicProvider;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Objects;

import static ui11.WidgetTree.IV_NOT_PROVIDED;

class IVCollector<T> {

    static final int USED_BY_SELF = 1;
    static final int USED_BY_DESCENDANT = 2;

    final Node<?> widgetState;
    final Class<T> type;

    private Object value;

    int usage;

    // ha ennek a konstruktornak a signaturejét megváltoztatjuk, akkor
    // változtassuk meg WidgetAccessorTeaVMPluginben a hivatkozást és a getVariable-t is
    // (nem fog hibát jelezni ha nem tesszük, csak rejtélyes JS hibák fognak megjelenni)
    IVCollector(Node<?> widgetState, Class<T> type) {
        this.widgetState = widgetState;
        this.type = type;
    }

    boolean retrieveValue() {
        Object newValue;

        if (IVFactory.isApplicable(type)) {
            newValue = retrieveRawValue(ResolutionRequestCollection.class);
            if (newValue != IV_NOT_PROVIDED) {
                ResolutionRequestCollection coll =
                        (ResolutionRequestCollection) newValue;
                newValue = IVFactory.makeUserValue(type, coll);
                if (newValue == null)
                    newValue = IV_NOT_PROVIDED;
            }
        } else if (ExposeRequest.class.isAssignableFrom(type) && ExposeRequest.class != type) {
            newValue = retrieveRawValue(ResolutionRequestCollection.class);
            if (newValue != IV_NOT_PROVIDED) {
                ResolutionRequestCollection coll =
                        (ResolutionRequestCollection) newValue;
                newValue = findResolutionRequest(coll);
            }
        } else if (
            // tömbon isAssignableFrom nem működik TeaVM 0.13.0-on, mindig false-t ad vissza.
            // 0.16.0-ra már javították a hibát, addig workaroundolni kell.
                type.isArray() &&
                        ExposeRequest.class.isAssignableFrom(type.getComponentType()) &&
                        ExposeRequest[].class != type) {

            newValue = retrieveRawValue(ResolutionRequestCollection.class);
            if (newValue != IV_NOT_PROVIDED) {
                ResolutionRequestCollection coll =
                        (ResolutionRequestCollection) newValue;
                newValue = findResolutionRequests(coll);
            }
        } else
            newValue = retrieveRawValue(type);

        if (!Objects.equals(value, newValue)) {
            this.value = newValue;
            return true;
        } else
            return false;
    }

    private <R> Object retrieveRawValue(Class<R> type) {
        IVCombiner<R> combiner = IVCombiner.forType(type);
        boolean hasAnyValues = false;

        for (Edge edge : widgetState.parents) {
            Object value = edge.directIVs().getOrDefault(type, IV_NOT_PROVIDED);

            if (value == IV_NOT_PROVIDED) {
                DynamicProvider dynamicProvider = (DynamicProvider) edge.directIVs().get(DynamicProvider.class);
                if (dynamicProvider != null) {
                    // TODO kéne valamit csinálni hogy DynamicProvider ne kavarhasson subscribe-olásokkal
                    Object dynamicValue = dynamicProvider.provideOrNull(type);
                    if (dynamicValue != null)
                        value = type.cast(dynamicValue);
                }
            }

            if (value != IV_NOT_PROVIDED) {
                hasAnyValues = true;
                combiner.add(type.cast(value), edge.instantiatedAt());
            } else {
                Node<?> parent = edge.parent();
                if (parent == null)
                    continue;

                @SuppressWarnings("unchecked")
                IVCollector<R> ivCollectorInParent = (IVCollector<R>) parent.ivCollectorsByType.get(type);

                if (ivCollectorInParent == null) {
                    ivCollectorInParent = new IVCollector<>(parent, type);
                    parent.ivCollectorsByType.put(type, ivCollectorInParent);
                    ivCollectorInParent.retrieveValue(); // TODO rekurzió eliminálása
                }
                ivCollectorInParent.usage |= IVCollector.USED_BY_DESCENDANT;
                if (ivCollectorInParent.value != IV_NOT_PROVIDED) {
                    combiner.add(type.cast(ivCollectorInParent.value), edge.instantiatedAt());
                    hasAnyValues = true;
                }
            }
        }

        return hasAnyValues ? combiner.finish() : IV_NOT_PROVIDED;
    }

    private @NonNull Object findResolutionRequest(ResolutionRequestCollection coll) {
        Object newValue;
        @SuppressWarnings("unchecked") Class<? extends ExposeRequest<?>> castedType =
                (Class<? extends ExposeRequest<?>>) type.asSubclass(ExposeRequest.class);
        List<? extends ResolutionRequest<?>> reqs = coll.byType(castedType);
        newValue = switch (reqs.size()) {
            case 0 -> IV_NOT_PROVIDED;
            case 1 -> type.cast(reqs.getFirst().requestData);
            default -> {
                // TODO
                throw new RuntimeException("Multiple requests corresponds to " + type.getName());
            }
        };
        return newValue;
    }

    // tömb
    private @NonNull Object findResolutionRequests(ResolutionRequestCollection coll) {
        @SuppressWarnings("unchecked")
        Class<? extends ExposeRequest<?>> pcrType = (Class<? extends ExposeRequest<?>>)
                type.getComponentType().asSubclass(ExposeRequest.class);

        List<? extends ResolutionRequest<?>> reqs = coll.byType(pcrType);
        Object[] result = (Object[]) Array.newInstance(pcrType, reqs.size());
        for (int i = 0; i < reqs.size(); i++) {
            result[i] = pcrType.cast(reqs.get(i).requestData);
        }
        return result;
    }

    /**
     * Csak retrieveValue vagy resume után szabad meghívni, ha a widget előtte
     * FLAG_NEEDS_IV_RETRIEVE vagy !FLAG_ACTIVE állapotban volt.
     * Továbbá csak refresh közben, mivel exception messageben kiírhatja a refresh stack tartalmát is.
     */
    T currentValue(boolean optional, String fieldOrParameterName) {
        if (!widgetState.hasFlag(Node.FLAG_ACTIVE))
            throw new IllegalStateException("can't retrieve value, widget state is inactive: " + this + ", " + widgetState);
        if (value == null)
            throw new RuntimeException("should not reach here (IVC.cV)");

        if (value == IV_NOT_PROVIDED)
            if (optional)
                return null;
            else {
                // TODO hibaüzenet esetén field nevet hogy kéne kiírni? (InjectionFieldInfo::debugName)
                throw new RuntimeException("inherited value for " +
                        type.getName() + " (used by " + fieldOrParameterName + ")" +
                        " not supplied for " + widgetState + "; Ancestors: " +
                        widgetState.tree.refreshStackToString(false));
            }
        else
            return type.cast(value);
    }

    /**
     * The behavior is undefined if {@link #retrieveValue()} has not been called before.
     *
     * @return {@linkplain WidgetTree#IV_NOT_PROVIDED} if not exists
     */
    public Object valueIfExists() {
        return value;
    }
}
