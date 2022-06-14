package co.vaughnvernon.mockroservices.journal;

import co.vaughnvernon.mockroservices.Product;

public class ProductRepository extends Repository {
  // readonly
  private Journal journal;
  // readonly
  private EntryStreamReader reader;

  public Product productOfId(String id) {
      EntryStream stream = reader.streamFor(id);
      return new Product(toSourceStream(stream.stream), stream.streamVersion);
  }

  // public <T> Product productOfId(final Class<T> streamClass, String id) {
  //   EntryStream stream = reader.streamFor(streamClass, id);
  //   return new Product(toSourceStream(stream.stream), stream.streamVersion);
  // }

  public <T> Product productOfId(final Class<T> streamClass, String id, long validOn) {
    EntryStream stream = reader.streamFor(streamClass, id);
    return new Product(toSourceStream(stream.stream, validOn), stream.streamVersion);
  }

  public void save(Product product) {
    journal.write(product.id, product.nextVersion(), toBatch(product.applied));
  }

  public <T extends Product> void save(final Class<T> streamClass, T product) {
    journal.write(streamClass, product.id, product.nextVersion(), toBatch(product.applied));
  }

  //internal
  public ProductRepository(String journalName)
  {
      journal = Journal.open(journalName);
      reader = journal.streamReader();
  }
}
