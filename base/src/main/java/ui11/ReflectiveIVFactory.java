package ui11;

import org.jspecify.annotations.Nullable;
import ui11.provide.FromPeerRequests;
import ui11.reflectutil.ReflectionUtil;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

// TODO TeaVM-en is működjön
class ReflectiveIVFactory {

    private final Method m;
    final Class<? extends ExposeRequest<?>> peerReqType;

    private ReflectiveIVFactory(Method m) {
        this.m = m;
        m.setAccessible(true);
        if (m.getParameterCount() != 1 || !m.getParameterTypes()[0].isArray())
            throw new RuntimeException("@" + FromPeerRequests.class.getSimpleName() + " method " + ReflectionUtil.memberToShortString(m) + " invalid: " +
                    "parameter count is not 1 or parameter type not array");
        if (!Modifier.isStatic(m.getModifiers()))
            throw new RuntimeException("@" + FromPeerRequests.class.getSimpleName() + " method " + ReflectionUtil.memberToShortString(m) + " invalid: " +
                    "method not static");
        Class<?> peerReqType = m.getParameterTypes()[0].getComponentType();
        assert peerReqType != null;
        if (!ExposeRequest.class.isAssignableFrom(peerReqType) || peerReqType == ExposeRequest.class)
            throw new RuntimeException("@" + FromPeerRequests.class.getSimpleName() + " method " + ReflectionUtil.memberToShortString(m) + " invalid: " +
                    "component of parameter type is not subtype of " + ExposeRequest.class.getSimpleName());

        @SuppressWarnings("unchecked")
        Class<? extends ExposeRequest<?>> casted = (Class<? extends ExposeRequest<?>>) peerReqType;
        this.peerReqType = casted;
    }

    static final ClassValue<ReflectiveIVFactory> CV = new ClassValue<ReflectiveIVFactory>() {
        @Override
        protected ReflectiveIVFactory computeValue(Class<?> type) {
            Method m1 = null;
            for (Method m : type.getDeclaredMethods()) {
                if (m.isAnnotationPresent(FromPeerRequests.class)) {
                    if (m1 == null)
                        m1 = m;
                    else
                        throw new RuntimeException("Multiple methods annotated with " +
                                "@" + FromPeerRequests.class.getSimpleName() + " in " + type.getName() + ": " +
                                ReflectionUtil.memberToShortString(m1) + ", " + ReflectionUtil.memberToShortString(m));
                }
            }
            if (m1 == null)
                return null;
            else
                return new ReflectiveIVFactory(m1);
        }
    };

    @Nullable
    Object makeValue(ResolutionRequestCollection resolutionRequestCollection) {
        List<? extends ResolutionRequest<?>> reqs = resolutionRequestCollection.byType(peerReqType);
        ExposeRequest<?>[] result = (ExposeRequest<?>[]) Array.newInstance(peerReqType, reqs.size());
        for (int i = 0; i < reqs.size(); i++)
            result[i] = peerReqType.cast(reqs.get(i).requestData);

        try {
            return m.invoke(null, (Object) result);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e); // should not happen
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException e2)
                throw e2;
            if (e.getCause() instanceof Error e2)
                throw e2;
            throw new RuntimeException(e); // TODO exception message
        }
    }
}
