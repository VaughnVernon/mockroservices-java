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

import java.util.Date;
import java.util.List;

import org.junit.Test;

import co.vaughnvernon.mockroservices.model.DomainEvent;
import co.vaughnvernon.mockroservices.model.EventSourcedRootEntity;

public class EventSourcedRootEntityTest {

  @Test
  public void testProductDefinedEventKept() throws Exception {
    final Product product = new Product("dice-fuz-1", "Fuzzy dice.", 999);
    assertEquals(1, product.mutatingEvents().size());
    assertEquals("dice-fuz-1", product.name);
    assertEquals("Fuzzy dice.", product.description);
    assertEquals(999, product.price);
    assertEquals(new ProductDefined("dice-fuz-1", "Fuzzy dice.", 999), product.mutatingEvents().get(0));
  }
  
  @Test
  public void testProductNameChangedEventKept() throws Exception {
    final Product product = new Product("dice-fuz-1", "Fuzzy dice.", 999);
    
    product.mutatingEvents().clear();
    
    product.changeName("dice-fuzzy-1");
    assertEquals(1, product.mutatingEvents().size());
    assertEquals("dice-fuzzy-1", product.name);
    assertEquals(new ProductNameChanged("dice-fuzzy-1"), product.mutatingEvents().get(0));
  }
  
  @Test
  public void testProductDescriptionChangedEventsKept() throws Exception {
    final Product product = new Product("dice-fuz-1", "Fuzzy dice.", 999);
    
    product.mutatingEvents().clear();
    
    product.changeDescription("Fuzzy dice, and all.");
    assertEquals(1, product.mutatingEvents().size());
    assertEquals("Fuzzy dice, and all.", product.description);
    assertEquals(new ProductDescriptionChanged("Fuzzy dice, and all."), product.mutatingEvents().get(0));
  }
  
  @Test
  public void testProductPriceChangedEventKept() throws Exception {
    final Product product = new Product("dice-fuz-1", "Fuzzy dice.", 999);
    
    product.mutatingEvents().clear();
    
    product.changePrice(995);
    assertEquals(1, product.mutatingEvents().size());
    assertEquals(995, product.price);
    assertEquals(new ProductPriceChanged(995), product.mutatingEvents().get(0));
  }
  
  @Test
  public void testReconstitution() throws Exception {
    final Product product = new Product("dice-fuz-1", "Fuzzy dice.", 999);
    product.changeName("dice-fuzzy-1");
    product.changeDescription("Fuzzy dice, and all.");
    product.changePrice(995);

    final Product productAgain = new Product(product.mutatingEvents(), product.mutatedVersion());
    assertEquals(product, productAgain);
  }
  
  public class Product extends EventSourcedRootEntity {
    public String name;
    public String description;
    public long price;

    Product(final String name, final String description, final long price) {
      apply(new ProductDefined(name, description, price));
    }

    Product(final List<DomainEvent> eventStream, final int streamVersion) {
      super(eventStream, streamVersion);
    }
    
    public void changeDescription(final String description) {
      apply(new ProductDescriptionChanged(description));
    }

    public void changeName(final String name) {
      apply(new ProductNameChanged(name));
    }

    public void changePrice(final long price) {
      apply(new ProductPriceChanged(price));
    }

    @Override
    public boolean equals(Object other) {
      if (other == null || other.getClass() != Product.class) {
        return false;
      }

      final Product otherProduct = (Product) other;
      
      return this.name.equals(otherProduct.name) &&
          this.description.equals(otherProduct.description) &&
          this.price == otherProduct.price;
    }

    public void when(final ProductDefined event) {
      this.name = event.name;
      this.description = event.description;
      this.price = event.price;
    }
    
    public void when(final ProductDescriptionChanged event) {
      this.description = event.description;
    }
    
    public void when(final ProductNameChanged event) {
      this.name = event.name;
    }
    
    public void when(final ProductPriceChanged event) {
      this.price = event.price;
    }
  }
  
  public final class ProductDefined extends DomainEvent {
    public final String description;
    public final String name;
    public final Date occurredOn;
    public final long price;
    public final int version;
    
    ProductDefined(final String name, final String description, final long price) {
      this.name = name;
      this.description = description;
      this.price = price;
      this.occurredOn = new Date();
      this.version = 1;
    }
    
    public Date occurredOn() {
      return occurredOn;
    }

    public int eventVersion() {
      return version;
    }
    
    @Override
    public boolean equals(Object other) {
      if (other == null || other.getClass() != ProductDefined.class) {
        return false;
      }

      final ProductDefined otherProductDefined = (ProductDefined) other;
      
      return this.name.equals(otherProductDefined.name) &&
          this.description.equals(otherProductDefined.description) &&
          this.price == otherProductDefined.price &&
          this.version == otherProductDefined.version;
    }
  }
  
  public final class ProductDescriptionChanged extends DomainEvent {
    public final String description;
    public final Date occurredOn;
    public final int version;
    
    ProductDescriptionChanged(final String description) {
      this.description = description;
      this.occurredOn = new Date();
      this.version = 1;
    }
    
    public Date occurredOn() {
      return occurredOn;
    }

    public int eventVersion() {
      return version;
    }
    
    @Override
    public boolean equals(Object other) {
      if (other == null || other.getClass() != ProductDescriptionChanged.class) {
        return false;
      }

      final ProductDescriptionChanged otherProductDescriptionChanged = (ProductDescriptionChanged) other;
      
      return this.description.equals(otherProductDescriptionChanged.description) &&
          this.version == otherProductDescriptionChanged.version;
    }
  }
  
  public final class ProductNameChanged extends DomainEvent {
    public final String name;
    public final Date occurredOn;
    public final int version;
    
    ProductNameChanged(final String name) {
      this.name = name;
      this.occurredOn = new Date();
      this.version = 1;
    }
    
    public Date occurredOn() {
      return occurredOn;
    }

    public int eventVersion() {
      return version;
    }
    
    @Override
    public boolean equals(Object other) {
      if (other == null || other.getClass() != ProductNameChanged.class) {
        return false;
      }

      final ProductNameChanged otherProductNameChanged = (ProductNameChanged) other;
      
      return this.name.equals(otherProductNameChanged.name) &&
          this.version == otherProductNameChanged.version;
    }
  }
  
  public final class ProductPriceChanged extends DomainEvent {
    public final long price;
    public final Date occurredOn;
    public final int version;
    
    ProductPriceChanged(final long price) {
      this.price = price;
      this.occurredOn = new Date();
      this.version = 1;
    }
    
    public Date occurredOn() {
      return occurredOn;
    }

    public int eventVersion() {
      return version;
    }
    
    @Override
    public boolean equals(Object other) {
      if (other == null || other.getClass() != ProductPriceChanged.class) {
        return false;
      }

      final ProductPriceChanged otherProductPriceChanged = (ProductPriceChanged) other;
      
      return this.price == otherProductPriceChanged.price &&
          this.version == otherProductPriceChanged.version;
    }
  }
}
