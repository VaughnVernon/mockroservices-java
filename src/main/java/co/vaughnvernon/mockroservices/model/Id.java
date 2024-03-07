package co.vaughnvernon.mockroservices.model;

import java.util.UUID;

public final class Id {
  public final String value;

  public static Id fromExisting(final String referencedId) {
    return new Id(referencedId);
  }
  
  public static Id unique() {
    return new Id();
  }
  
  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public boolean equals(final Object other) {
    if (other == this) {
      return true;
    }
    if (other == null || other.getClass() != Id.class) {
      return false;
    }
    
    final Id otherId = (Id) other;
    
    return this.value.equals(otherId.value);
  }

  @Override
  public String toString() {
    return "Id[value=" + value + "]";
  }
  
  private Id() {
    this.value = UUID.randomUUID().toString();
  }
  
  private Id(final String referencedId) {
    this.value = referencedId;
  }
}
