package co.vaughnvernon.mockroservices.journal;

public final class StreamNameBuilder {
    public static <T> String buildStreamNameFor(final Class<T> streamClass, final String value) {
        return streamClass.getSimpleName().toLowerCase() + '_' + value;
    }
    public static <T> String buildStreamNameFor(final Class<T> streamClass) {
      return "cat-" + streamClass.getSimpleName().toLowerCase();
  }
}
