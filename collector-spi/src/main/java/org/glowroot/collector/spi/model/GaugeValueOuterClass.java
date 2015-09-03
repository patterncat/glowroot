// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: GaugeValue.proto

package org.glowroot.collector.spi.model;

public final class GaugeValueOuterClass {
  private GaugeValueOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface GaugeValueOrBuilder extends
      // @@protoc_insertion_point(interface_extends:org_glowroot_collector_spi_model.GaugeValue)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>optional int64 capture_time = 1;</code>
     */
    long getCaptureTime();

    /**
     * <code>optional double value = 2;</code>
     */
    double getValue();
  }
  /**
   * Protobuf type {@code org_glowroot_collector_spi_model.GaugeValue}
   */
  public  static final class GaugeValue extends
      com.google.protobuf.GeneratedMessage implements
      // @@protoc_insertion_point(message_implements:org_glowroot_collector_spi_model.GaugeValue)
      GaugeValueOrBuilder {
    // Use GaugeValue.newBuilder() to construct.
    private GaugeValue(com.google.protobuf.GeneratedMessage.Builder<?> builder) {
      super(builder);
    }
    private GaugeValue() {
      captureTime_ = 0L;
      value_ = 0D;
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return com.google.protobuf.UnknownFieldSet.getDefaultInstance();
    }
    private GaugeValue(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry) {
      this();
      int mutable_bitField0_ = 0;
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            default: {
              if (!input.skipField(tag)) {
                done = true;
              }
              break;
            }
            case 8: {

              captureTime_ = input.readInt64();
              break;
            }
            case 17: {

              value_ = input.readDouble();
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw new RuntimeException(e.setUnfinishedMessage(this));
      } catch (java.io.IOException e) {
        throw new RuntimeException(
            new com.google.protobuf.InvalidProtocolBufferException(
                e.getMessage()).setUnfinishedMessage(this));
      } finally {
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return org.glowroot.collector.spi.model.GaugeValueOuterClass.internal_static_org_glowroot_collector_spi_model_GaugeValue_descriptor;
    }

    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return org.glowroot.collector.spi.model.GaugeValueOuterClass.internal_static_org_glowroot_collector_spi_model_GaugeValue_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue.class, org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue.Builder.class);
    }

    public static final int CAPTURE_TIME_FIELD_NUMBER = 1;
    private long captureTime_;
    /**
     * <code>optional int64 capture_time = 1;</code>
     */
    public long getCaptureTime() {
      return captureTime_;
    }

    public static final int VALUE_FIELD_NUMBER = 2;
    private double value_;
    /**
     * <code>optional double value = 2;</code>
     */
    public double getValue() {
      return value_;
    }

    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (captureTime_ != 0L) {
        output.writeInt64(1, captureTime_);
      }
      if (value_ != 0D) {
        output.writeDouble(2, value_);
      }
    }

    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (captureTime_ != 0L) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt64Size(1, captureTime_);
      }
      if (value_ != 0D) {
        size += com.google.protobuf.CodedOutputStream
          .computeDoubleSize(2, value_);
      }
      memoizedSize = size;
      return size;
    }

    private static final long serialVersionUID = 0L;
    public static org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }
    public static org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input);
    }
    public static org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseDelimitedFrom(input, extensionRegistry);
    }
    public static org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return PARSER.parseFrom(input);
    }
    public static org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return PARSER.parseFrom(input, extensionRegistry);
    }

    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * Protobuf type {@code org_glowroot_collector_spi_model.GaugeValue}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:org_glowroot_collector_spi_model.GaugeValue)
        org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValueOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return org.glowroot.collector.spi.model.GaugeValueOuterClass.internal_static_org_glowroot_collector_spi_model_GaugeValue_descriptor;
      }

      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return org.glowroot.collector.spi.model.GaugeValueOuterClass.internal_static_org_glowroot_collector_spi_model_GaugeValue_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue.class, org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue.Builder.class);
      }

      // Construct using org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessage.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      public Builder clear() {
        super.clear();
        captureTime_ = 0L;

        value_ = 0D;

        return this;
      }

      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return org.glowroot.collector.spi.model.GaugeValueOuterClass.internal_static_org_glowroot_collector_spi_model_GaugeValue_descriptor;
      }

      public org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue getDefaultInstanceForType() {
        return org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue.getDefaultInstance();
      }

      public org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue build() {
        org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      public org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue buildPartial() {
        org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue result = new org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue(this);
        result.captureTime_ = captureTime_;
        result.value_ = value_;
        onBuilt();
        return result;
      }

      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue) {
          return mergeFrom((org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue other) {
        if (other == org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue.getDefaultInstance()) return this;
        if (other.getCaptureTime() != 0L) {
          setCaptureTime(other.getCaptureTime());
        }
        if (other.getValue() != 0D) {
          setValue(other.getValue());
        }
        onChanged();
        return this;
      }

      public final boolean isInitialized() {
        return true;
      }

      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue) e.getUnfinishedMessage();
          throw e;
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }

      private long captureTime_ ;
      /**
       * <code>optional int64 capture_time = 1;</code>
       */
      public long getCaptureTime() {
        return captureTime_;
      }
      /**
       * <code>optional int64 capture_time = 1;</code>
       */
      public Builder setCaptureTime(long value) {
        
        captureTime_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional int64 capture_time = 1;</code>
       */
      public Builder clearCaptureTime() {
        
        captureTime_ = 0L;
        onChanged();
        return this;
      }

      private double value_ ;
      /**
       * <code>optional double value = 2;</code>
       */
      public double getValue() {
        return value_;
      }
      /**
       * <code>optional double value = 2;</code>
       */
      public Builder setValue(double value) {
        
        value_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>optional double value = 2;</code>
       */
      public Builder clearValue() {
        
        value_ = 0D;
        onChanged();
        return this;
      }
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return this;
      }

      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return this;
      }


      // @@protoc_insertion_point(builder_scope:org_glowroot_collector_spi_model.GaugeValue)
    }

    // @@protoc_insertion_point(class_scope:org_glowroot_collector_spi_model.GaugeValue)
    private static final org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue();
    }

    public static org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<GaugeValue>
        PARSER = new com.google.protobuf.AbstractParser<GaugeValue>() {
      public GaugeValue parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        try {
          return new GaugeValue(input, extensionRegistry);
        } catch (RuntimeException e) {
          if (e.getCause() instanceof
              com.google.protobuf.InvalidProtocolBufferException) {
            throw (com.google.protobuf.InvalidProtocolBufferException)
                e.getCause();
          }
          throw e;
        }
      }
    };

    public static com.google.protobuf.Parser<GaugeValue> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<GaugeValue> getParserForType() {
      return PARSER;
    }

    public org.glowroot.collector.spi.model.GaugeValueOuterClass.GaugeValue getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_org_glowroot_collector_spi_model_GaugeValue_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_org_glowroot_collector_spi_model_GaugeValue_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\020GaugeValue.proto\022 org_glowroot_collect" +
      "or_spi_model\"1\n\nGaugeValue\022\024\n\014capture_ti" +
      "me\030\001 \001(\003\022\r\n\005value\030\002 \001(\001B\"\n org.glowroot." +
      "collector.spi.modelb\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_org_glowroot_collector_spi_model_GaugeValue_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_org_glowroot_collector_spi_model_GaugeValue_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessage.FieldAccessorTable(
        internal_static_org_glowroot_collector_spi_model_GaugeValue_descriptor,
        new java.lang.String[] { "CaptureTime", "Value", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}