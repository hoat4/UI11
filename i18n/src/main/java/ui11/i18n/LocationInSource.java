package ui11.i18n;

import ui11.reflectutil.ReflectionUtil;

import java.lang.reflect.Method;
import java.util.Objects;

public class LocationInSource {

    public final String templateName;
    public final int line;
    public final int column;

    public LocationInSource(String templateName, int line, int column) {
        this.templateName = templateName;
        this.line = line;
        this.column = column;
    }

    /**
     * ha annotáció a forrás (pl. lokalizálás esetén)
     */
    public static LocationInSource of(Class<?> clazz) {
        return new LocationInSource(clazz.getName(), -1, -1);
    }

    /**
     * ha annotáció a forrás (pl. lokalizálás esetén)
     */
    public static LocationInSource of(Method method) {
        return new LocationInSource(ReflectionUtil.memberToShortString(method), -1, -1);
    }

    // TODO ezt törölni kéne, mert hülyeség
    @Deprecated
    public static LocationInSource of(StackTraceElement stackTraceElement) {
        return new LocationInSource(stackTraceElement.getFileName(), stackTraceElement.getLineNumber(), -1);
    }
    
    @Override
    public String toString() {
        if(line == -1)
            return templateName;
        String s = "line " + line;
        if (column != -1)
            s += " column " + column;
        return s + " in " + templateName;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.templateName);
        hash = 59 * hash + this.line;
        hash = 59 * hash + this.column;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final LocationInSource other = (LocationInSource) obj;
        if (this.line != other.line)
            return false;
        if (this.column != other.column)
            return false;
        if (!Objects.equals(this.templateName, other.templateName))
            return false;
        return true;
    }

    public LocationInSource offset(int lines, int columns) {
        return new LocationInSource(templateName, 
                line + lines, lines > 0 ? columns + 1 : column + columns);
    }

    public LocationInSource offset2D(int lines, int columns) {
        return new LocationInSource(templateName, 
                line + lines, column + columns);
    }

}
