package com.amplifyframework.datastore.generated.model;

import com.amplifyframework.core.model.temporal.Temporal;

import java.util.List;
import java.util.UUID;
import java.util.Objects;

import androidx.core.util.ObjectsCompat;

import com.amplifyframework.core.model.Model;
import com.amplifyframework.core.model.annotations.Index;
import com.amplifyframework.core.model.annotations.ModelConfig;
import com.amplifyframework.core.model.annotations.ModelField;
import com.amplifyframework.core.model.query.predicate.QueryField;

import static com.amplifyframework.core.model.query.predicate.QueryField.field;

/** This is an auto generated class representing the AssetAllocationStores type in your schema. */
@SuppressWarnings("all")
@ModelConfig(pluralName = "AssetAllocationStores")
@Index(name = "storeCodeIndex", fields = {"storeCode"})
@Index(name = "storeNameIndex", fields = {"storeName"})
public final class AssetAllocationStores implements Model {
  public static final QueryField ID = field("AssetAllocationStores", "id");
  public static final QueryField BASE_ALLOCATION_TYPE = field("AssetAllocationStores", "baseAllocationType");
  public static final QueryField STORE_CODE = field("AssetAllocationStores", "storeCode");
  public static final QueryField STORE_NAME = field("AssetAllocationStores", "storeName");
  public static final QueryField DC = field("AssetAllocationStores", "dc");
  public static final QueryField STATUS = field("AssetAllocationStores", "status");
  private final @ModelField(targetType="ID", isRequired = true) String id;
  private final @ModelField(targetType="String", isRequired = true) String baseAllocationType;
  private final @ModelField(targetType="String", isRequired = true) String storeCode;
  private final @ModelField(targetType="String", isRequired = true) String storeName;
  private final @ModelField(targetType="String") String dc;
  private final @ModelField(targetType="String") String status;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime createdAt;
  private @ModelField(targetType="AWSDateTime", isReadOnly = true) Temporal.DateTime updatedAt;
  public String getId() {
      return id;
  }
  
  public String getBaseAllocationType() {
      return baseAllocationType;
  }
  
  public String getStoreCode() {
      return storeCode;
  }
  
  public String getStoreName() {
      return storeName;
  }
  
  public String getDc() {
      return dc;
  }
  
  public String getStatus() {
      return status;
  }
  
  public Temporal.DateTime getCreatedAt() {
      return createdAt;
  }
  
  public Temporal.DateTime getUpdatedAt() {
      return updatedAt;
  }
  
  private AssetAllocationStores(String id, String baseAllocationType, String storeCode, String storeName, String dc, String status) {
    this.id = id;
    this.baseAllocationType = baseAllocationType;
    this.storeCode = storeCode;
    this.storeName = storeName;
    this.dc = dc;
    this.status = status;
  }
  
  @Override
   public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      } else if(obj == null || getClass() != obj.getClass()) {
        return false;
      } else {
      AssetAllocationStores assetAllocationStores = (AssetAllocationStores) obj;
      return ObjectsCompat.equals(getId(), assetAllocationStores.getId()) &&
              ObjectsCompat.equals(getBaseAllocationType(), assetAllocationStores.getBaseAllocationType()) &&
              ObjectsCompat.equals(getStoreCode(), assetAllocationStores.getStoreCode()) &&
              ObjectsCompat.equals(getStoreName(), assetAllocationStores.getStoreName()) &&
              ObjectsCompat.equals(getDc(), assetAllocationStores.getDc()) &&
              ObjectsCompat.equals(getStatus(), assetAllocationStores.getStatus()) &&
              ObjectsCompat.equals(getCreatedAt(), assetAllocationStores.getCreatedAt()) &&
              ObjectsCompat.equals(getUpdatedAt(), assetAllocationStores.getUpdatedAt());
      }
  }
  
  @Override
   public int hashCode() {
    return new StringBuilder()
      .append(getId())
      .append(getBaseAllocationType())
      .append(getStoreCode())
      .append(getStoreName())
      .append(getDc())
      .append(getStatus())
      .append(getCreatedAt())
      .append(getUpdatedAt())
      .toString()
      .hashCode();
  }
  
  @Override
   public String toString() {
    return new StringBuilder()
      .append("AssetAllocationStores {")
      .append("id=" + String.valueOf(getId()) + ", ")
      .append("baseAllocationType=" + String.valueOf(getBaseAllocationType()) + ", ")
      .append("storeCode=" + String.valueOf(getStoreCode()) + ", ")
      .append("storeName=" + String.valueOf(getStoreName()) + ", ")
      .append("dc=" + String.valueOf(getDc()) + ", ")
      .append("status=" + String.valueOf(getStatus()) + ", ")
      .append("createdAt=" + String.valueOf(getCreatedAt()) + ", ")
      .append("updatedAt=" + String.valueOf(getUpdatedAt()))
      .append("}")
      .toString();
  }
  
  public static BaseAllocationTypeStep builder() {
      return new Builder();
  }
  
  /** 
   * WARNING: This method should not be used to build an instance of this object for a CREATE mutation.
   * This is a convenience method to return an instance of the object with only its ID populated
   * to be used in the context of a parameter in a delete mutation or referencing a foreign key
   * in a relationship.
   * @param id the id of the existing item this instance will represent
   * @return an instance of this model with only ID populated
   */
  public static AssetAllocationStores justId(String id) {
    return new AssetAllocationStores(
      id,
      null,
      null,
      null,
      null,
      null
    );
  }
  
  public CopyOfBuilder copyOfBuilder() {
    return new CopyOfBuilder(id,
      baseAllocationType,
      storeCode,
      storeName,
      dc,
      status);
  }
  public interface BaseAllocationTypeStep {
    StoreCodeStep baseAllocationType(String baseAllocationType);
  }
  

  public interface StoreCodeStep {
    StoreNameStep storeCode(String storeCode);
  }
  

  public interface StoreNameStep {
    BuildStep storeName(String storeName);
  }
  

  public interface BuildStep {
    AssetAllocationStores build();
    BuildStep id(String id);
    BuildStep dc(String dc);
    BuildStep status(String status);
  }
  

  public static class Builder implements BaseAllocationTypeStep, StoreCodeStep, StoreNameStep, BuildStep {
    private String id;
    private String baseAllocationType;
    private String storeCode;
    private String storeName;
    private String dc;
    private String status;
    @Override
     public AssetAllocationStores build() {
        String id = this.id != null ? this.id : UUID.randomUUID().toString();
        
        return new AssetAllocationStores(
          id,
          baseAllocationType,
          storeCode,
          storeName,
          dc,
          status);
    }
    
    @Override
     public StoreCodeStep baseAllocationType(String baseAllocationType) {
        Objects.requireNonNull(baseAllocationType);
        this.baseAllocationType = baseAllocationType;
        return this;
    }
    
    @Override
     public StoreNameStep storeCode(String storeCode) {
        Objects.requireNonNull(storeCode);
        this.storeCode = storeCode;
        return this;
    }
    
    @Override
     public BuildStep storeName(String storeName) {
        Objects.requireNonNull(storeName);
        this.storeName = storeName;
        return this;
    }
    
    @Override
     public BuildStep dc(String dc) {
        this.dc = dc;
        return this;
    }
    
    @Override
     public BuildStep status(String status) {
        this.status = status;
        return this;
    }
    
    /** 
     * @param id id
     * @return Current Builder instance, for fluent method chaining
     */
    public BuildStep id(String id) {
        this.id = id;
        return this;
    }
  }
  

  public final class CopyOfBuilder extends Builder {
    private CopyOfBuilder(String id, String baseAllocationType, String storeCode, String storeName, String dc, String status) {
      super.id(id);
      super.baseAllocationType(baseAllocationType)
        .storeCode(storeCode)
        .storeName(storeName)
        .dc(dc)
        .status(status);
    }
    
    @Override
     public CopyOfBuilder baseAllocationType(String baseAllocationType) {
      return (CopyOfBuilder) super.baseAllocationType(baseAllocationType);
    }
    
    @Override
     public CopyOfBuilder storeCode(String storeCode) {
      return (CopyOfBuilder) super.storeCode(storeCode);
    }
    
    @Override
     public CopyOfBuilder storeName(String storeName) {
      return (CopyOfBuilder) super.storeName(storeName);
    }
    
    @Override
     public CopyOfBuilder dc(String dc) {
      return (CopyOfBuilder) super.dc(dc);
    }
    
    @Override
     public CopyOfBuilder status(String status) {
      return (CopyOfBuilder) super.status(status);
    }
  }
  
}
