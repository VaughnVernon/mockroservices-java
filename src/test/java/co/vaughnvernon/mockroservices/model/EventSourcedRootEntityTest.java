//   Copyright © 2017 Vaughn Vernon. All rights reserved.
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package co.vaughnvernon.mockroservices.model;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Test;

import co.vaughnvernon.mockroservices.Product;
import co.vaughnvernon.mockroservices.Product.ProductDefined;
import co.vaughnvernon.mockroservices.Product.ProductNameChanged;

public class EventSourcedRootEntityTest {

  @Test
  public void testProductDefinedEventKept() throws Exception {
    String id = UUID.randomUUID().toString();
    final Product product = new Product(id, "dice-fuz-1", "Fuzzy dice.", 999);
    assertEquals(1, product.applied().size());
    assertEquals("dice-fuz-1", product.name);
    assertEquals("Fuzzy dice.", product.description);
    assertEquals(999, product.price);
    assertEquals(new ProductDefined(id, "dice-fuz-1", "Fuzzy dice.", 999), product.applied().get(0));
  }

  @Test
  public void testProductNameChangedEventKept() throws Exception {
    final Product product = new Product(UUID.randomUUID().toString(), "dice-fuz-1", "Fuzzy dice.", 999);

    product.applied().clear();

    product.changeName("dice-fuzzy-1");
    assertEquals(1, product.applied().size());
    assertEquals("dice-fuzzy-1", product.name);
    assertEquals(new ProductNameChanged("dice-fuzzy-1"), product.applied().get(0));
  }

  @Test
  public void testProductDescriptionChangedEventsKept() throws Exception {
    final Product product = new Product(UUID.randomUUID().toString(), "dice-fuz-1", "Fuzzy dice.", 999);

    product.applied().clear();

    product.changeDescription("Fuzzy dice, and all.");
    assertEquals(1, product.applied().size());
    assertEquals("Fuzzy dice, and all.", product.description);
    assertEquals(new Product.ProductDescriptionChanged("Fuzzy dice, and all."), product.applied().get(0));
  }

  @Test
  public void testProductPriceChangedEventKept() throws Exception {
    final Product product = new Product(UUID.randomUUID().toString(), "dice-fuz-1", "Fuzzy dice.", 999);

    product.applied().clear();

    product.changePrice(995);
    assertEquals(1, product.applied().size());
    assertEquals(995, product.price);
    assertEquals(new Product.ProductPriceChanged(995), product.applied().get(0));
  }

  @Test
  public void testReconstitution() throws Exception {
    final Product product = new Product(UUID.randomUUID().toString(), "dice-fuz-1", "Fuzzy dice.", 999);
    product.changeName("dice-fuzzy-1");
    product.changeDescription("Fuzzy dice, and all.");
    product.changePrice(995);

    final Product productAgain = new Product(product.applied(), product.nextVersion());
    assertEquals(product, productAgain);
  }
}
