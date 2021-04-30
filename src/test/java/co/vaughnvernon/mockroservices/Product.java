package co.vaughnvernon.mockroservices;

import java.util.List;

import co.vaughnvernon.mockroservices.model.DomainEvent;
import co.vaughnvernon.mockroservices.model.SourcedEntity;

public class Product extends SourcedEntity<DomainEvent>  {
  public String id;
  public String name;
  public String description;
  public long price;

  public Product(final String id, final String name, final String description, final long price) {
    apply(new ProductDefined(id, name, description, price));
  }

  public Product(final String id, final String name, final String description, final long price, final long validOn) {
    apply(new ProductDefined(id, name, description, price, validOn));
  }

  public Product(final List<DomainEvent> eventStream, final int streamVersion) {
    super(eventStream, streamVersion);
  }

  public void changeDescription(final String description) {
    apply(new ProductDescriptionChanged(description));
  }

  public void changeDescription(final String description, final long validOn) {
    apply(new ProductDescriptionChanged(description, validOn));
  }

  public void changeName(final String name) {
    apply(new ProductNameChanged(name));
  }

  public void changeName(final String name, final long validOn) {
    apply(new ProductNameChanged(name, validOn));
  }

  public void changePrice(final long price) {
    apply(new ProductPriceChanged(price));
  }

  public void changePrice(final long price, final long validOn) {
    apply(new ProductPriceChanged(price, validOn));
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
    this.id = event.id;
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

  public static class ProductDefined extends DomainEvent {
    public final String id;
    public final String description;
    public final String name;
    public final long price;

    public ProductDefined(final String id, final String name, final String description, final long price) {
      this(id, name, description, price, System.currentTimeMillis());
    }
    ProductDefined(final String id, final String name, final String description, final long price, final long validOn) {
      super(validOn, 0);
      this.id = id;
      this.name = name;
      this.description = description;
      this.price = price;
    }

    @Override
    public boolean equals(Object other) {
      if (other == null || other.getClass() != ProductDefined.class) {
        return false;
      }

      final ProductDefined otherProductDefined = (ProductDefined) other;

      return this.id.equals(otherProductDefined.id) &&
          this.name.equals(otherProductDefined.name) &&
          this.description.equals(otherProductDefined.description) &&
          this.price == otherProductDefined.price &&
          eventVersion == otherProductDefined.eventVersion;
    }
  }

  public static class ProductDescriptionChanged extends DomainEvent {
    public final String description;

    public ProductDescriptionChanged(final String description) {
      this(description, System.currentTimeMillis());
    }

    ProductDescriptionChanged(final String description, final long validOn) {
      super(validOn, 0);
      this.description = description;
    }

    @Override
    public boolean equals(Object other) {
      if (other == null || other.getClass() != ProductDescriptionChanged.class) {
        return false;
      }

      final ProductDescriptionChanged otherProductDescriptionChanged = (ProductDescriptionChanged) other;

      return this.description.equals(otherProductDescriptionChanged.description) &&
          eventVersion == otherProductDescriptionChanged.eventVersion;
    }
  }

  public static class ProductNameChanged extends DomainEvent {
    public final String name;

    public ProductNameChanged(final String name) {
      this(name, System.currentTimeMillis());
    }

    ProductNameChanged(final String name, final long validOn) {
      super(validOn, 0);
      this.name = name;
    }

    @Override
    public boolean equals(Object other) {
      if (other == null || other.getClass() != ProductNameChanged.class) {
        return false;
      }

      final ProductNameChanged otherProductNameChanged = (ProductNameChanged) other;

      return this.name.equals(otherProductNameChanged.name) &&
          eventVersion == otherProductNameChanged.eventVersion;
    }
  }

  public static class ProductPriceChanged extends DomainEvent {
    public final long price;

    public ProductPriceChanged(final long price) {
      this(price, System.currentTimeMillis());
    }

    ProductPriceChanged(final long price, final long validOn) {
      super(validOn, 0);
      this.price = price;
    }

    @Override
    public boolean equals(Object other) {
      if (other == null || other.getClass() != ProductPriceChanged.class) {
        return false;
      }

      final ProductPriceChanged otherProductPriceChanged = (ProductPriceChanged) other;

      return this.price == otherProductPriceChanged.price &&
          eventVersion == otherProductPriceChanged.eventVersion;
    }
  }
}
