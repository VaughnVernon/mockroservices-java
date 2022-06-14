package co.vaughnvernon.mockroservices.journal;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.UUID;

import org.junit.Test;

import co.vaughnvernon.mockroservices.Product;
import co.vaughnvernon.mockroservices.journal.JournalPublisherTest.TestSubscriber;
import co.vaughnvernon.mockroservices.messagebus.MessageBus;
import co.vaughnvernon.mockroservices.messagebus.Topic;

public class IntegrationTest {

  @Test
  public void writeReadBiTemporalEvents() {
    ProductRepository repository = new ProductRepository("bi-product-journal");
    long y2018 = new Date(1000000l).getTime(); //var y2018 = new DateTimeOffset(2018, 1, 1, 0, 0, 0, TimeSpan.Zero);
    long y2019 = new Date(2000000l).getTime(); //var y2019 = new DateTimeOffset(2019, 1, 1, 0, 0, 0, TimeSpan.Zero);
    long y2020 = new Date(3000000l).getTime(); //var y2020 = new DateTimeOffset(2020, 1, 1, 0, 0, 0, TimeSpan.Zero);

    String id = UUID.randomUUID().toString();
    Product product = new Product(id, "dice-fuz-1", "Fuzzy dice.", 999, y2018);
    product.changeName("dice-fuzzy-1", y2020);
    product.changeDescription("Fuzzy dice, and all.", y2020);
    product.changePrice(995, y2019);
    product.changeName("dice-fizzy-1", y2019);
    repository.save(Product.class, product);

    Product productAt2018 = repository.productOfId(Product.class, id, y2018);
    assertEquals(5, productAt2018.currentVersion);
    assertEquals("dice-fuz-1", productAt2018.name);
    assertEquals("Fuzzy dice.", productAt2018.description);
    assertEquals(999, productAt2018.price);

    Product productAt2019 = repository.productOfId(Product.class, id, y2019);
    assertEquals(5, productAt2019.currentVersion);
    assertEquals("dice-fizzy-1", productAt2019.name);
    assertEquals("Fuzzy dice.", productAt2019.description);
    assertEquals(995, productAt2019.price);

    Product productAt2020 = repository.productOfId(Product.class, id, y2020);
    assertEquals(5, productAt2020.currentVersion);
    assertEquals("dice-fuzzy-1", productAt2020.name);
    assertEquals("Fuzzy dice, and all.", productAt2020.description);
    assertEquals(995, productAt2020.price);
  }

  @Test
  public void fromRepositoryToProjection() throws Exception {
    MessageBus messageBus = MessageBus.start("test-bus-product");
    Topic topic = messageBus.openTopic("cat-product");
    String journalName = "product-journal";
    JournalPublisher journalPublisher = JournalPublisher.using(journalName, messageBus.name, topic.name);
    TestSubscriber subscriber = new JournalPublisherTest.TestSubscriber();
    topic.subscribe(subscriber);

    ProductRepository repository = new ProductRepository(journalName);

    Product product1 = new Product(UUID.randomUUID().toString(), "dice-fuz-1", "Fuzzy dice.", 999);
    product1.changeName("dice-fuzzy-1");
    product1.changeDescription("Fuzzy dice, and all.");
    product1.changePrice(995);
    repository.save(Product.class, product1);

    Product product2 = new Product(UUID.randomUUID().toString(), "dice-fuz-2", "Fuzzy dice.", 999);
    product2.changeName("dice-fuzzy-2");
    product2.changeDescription("Fuzzy dice, and all 2.");
    product2.changePrice(1000);
    repository.save(Product.class, product2);

    subscriber.waitForExpectedMessages(8);
    topic.close();
    journalPublisher.close();

    assertEquals(8, subscriber.handledMessages.size());
  }
}
