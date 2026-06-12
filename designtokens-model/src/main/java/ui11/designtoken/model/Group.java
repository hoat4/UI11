package ui11.designtoken.model;

import org.jspecify.annotations.NonNull;

import java.util.LinkedHashMap;
import java.util.SequencedMap;

public final class Group extends Node {

    public final SequencedMap<String, Node> children = new LinkedHashMap<>();

    public GroupReference extendsFrom;

    public interface GroupReference {}

    public record GroupReferenceByPath(@NonNull String path) implements GroupReference {}

    public record GroupReferenceByJsonPointer(@NonNull String jsonPointer) implements GroupReference {}
}