package ui11.task;

public sealed interface TaskStatus<T> {

    record InProgress<T>() implements TaskStatus<T> {}

    record Failure<T>(Throwable exception) implements TaskStatus<T> {}

    record Success<T>(T value) implements TaskStatus<T> {}
}
