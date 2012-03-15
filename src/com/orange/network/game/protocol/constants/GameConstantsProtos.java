// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: GameConstants.proto

package com.orange.network.game.protocol.constants;

public final class GameConstantsProtos {
  private GameConstantsProtos() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public enum GameCommandType
      implements com.google.protobuf.ProtocolMessageEnum {
    JOIN_GAME_REQUEST(0, 1),
    JOIN_GAME_RESPONSE(1, 2),
    START_GAME_REQUEST(2, 3),
    START_GAME_RESPONSE(3, 4),
    USER_JOIN_NOTIFICATION_REQUEST(4, 51),
    USER_JOIN_NOTIFICATION_RESPONSE(5, 52),
    HOST_CHANGE_NOTIFICATION_REQUEST(6, 53),
    HOST_CHANGE_NOTIFICATION_RESPONSE(7, 54),
    GAME_START_NOTIFICATION_REQUEST(8, 55),
    GAME_START_NOTIFICATION_RESPONSE(9, 56),
    ;
    
    public static final int JOIN_GAME_REQUEST_VALUE = 1;
    public static final int JOIN_GAME_RESPONSE_VALUE = 2;
    public static final int START_GAME_REQUEST_VALUE = 3;
    public static final int START_GAME_RESPONSE_VALUE = 4;
    public static final int USER_JOIN_NOTIFICATION_REQUEST_VALUE = 51;
    public static final int USER_JOIN_NOTIFICATION_RESPONSE_VALUE = 52;
    public static final int HOST_CHANGE_NOTIFICATION_REQUEST_VALUE = 53;
    public static final int HOST_CHANGE_NOTIFICATION_RESPONSE_VALUE = 54;
    public static final int GAME_START_NOTIFICATION_REQUEST_VALUE = 55;
    public static final int GAME_START_NOTIFICATION_RESPONSE_VALUE = 56;
    
    
    public final int getNumber() { return value; }
    
    public static GameCommandType valueOf(int value) {
      switch (value) {
        case 1: return JOIN_GAME_REQUEST;
        case 2: return JOIN_GAME_RESPONSE;
        case 3: return START_GAME_REQUEST;
        case 4: return START_GAME_RESPONSE;
        case 51: return USER_JOIN_NOTIFICATION_REQUEST;
        case 52: return USER_JOIN_NOTIFICATION_RESPONSE;
        case 53: return HOST_CHANGE_NOTIFICATION_REQUEST;
        case 54: return HOST_CHANGE_NOTIFICATION_RESPONSE;
        case 55: return GAME_START_NOTIFICATION_REQUEST;
        case 56: return GAME_START_NOTIFICATION_RESPONSE;
        default: return null;
      }
    }
    
    public static com.google.protobuf.Internal.EnumLiteMap<GameCommandType>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static com.google.protobuf.Internal.EnumLiteMap<GameCommandType>
        internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<GameCommandType>() {
            public GameCommandType findValueByNumber(int number) {
              return GameCommandType.valueOf(number);
            }
          };
    
    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      return getDescriptor().getValues().get(index);
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return com.orange.network.game.protocol.constants.GameConstantsProtos.getDescriptor().getEnumTypes().get(0);
    }
    
    private static final GameCommandType[] VALUES = {
      JOIN_GAME_REQUEST, JOIN_GAME_RESPONSE, START_GAME_REQUEST, START_GAME_RESPONSE, USER_JOIN_NOTIFICATION_REQUEST, USER_JOIN_NOTIFICATION_RESPONSE, HOST_CHANGE_NOTIFICATION_REQUEST, HOST_CHANGE_NOTIFICATION_RESPONSE, GAME_START_NOTIFICATION_REQUEST, GAME_START_NOTIFICATION_RESPONSE, 
    };
    
    public static GameCommandType valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      return VALUES[desc.getIndex()];
    }
    
    private final int index;
    private final int value;
    
    private GameCommandType(int index, int value) {
      this.index = index;
      this.value = value;
    }
    
    // @@protoc_insertion_point(enum_scope:game.GameCommandType)
  }
  
  public enum GameResultCode
      implements com.google.protobuf.ProtocolMessageEnum {
    SUCCESS(0, 0),
    ERROR_JOIN_GAME(1, 1),
    ERROR_USERID_NULL(2, 100),
    ERROR_USER_CANNOT_START_GAME(3, 101),
    ERROR_NO_SESSION_ID(4, 200),
    ERROR_NEXT_STATE_NOT_FOUND(5, 201),
    ERROR_SESSIONID_NULL(6, 202),
    ERROR_SESSION_ALREADY_START(7, 203),
    ERROR_SYSTEM_HANDLER_NOT_FOUND(8, 910),
    ERROR_SYSTEM_EXCEPTION(9, 911),
    ;
    
    public static final int SUCCESS_VALUE = 0;
    public static final int ERROR_JOIN_GAME_VALUE = 1;
    public static final int ERROR_USERID_NULL_VALUE = 100;
    public static final int ERROR_USER_CANNOT_START_GAME_VALUE = 101;
    public static final int ERROR_NO_SESSION_ID_VALUE = 200;
    public static final int ERROR_NEXT_STATE_NOT_FOUND_VALUE = 201;
    public static final int ERROR_SESSIONID_NULL_VALUE = 202;
    public static final int ERROR_SESSION_ALREADY_START_VALUE = 203;
    public static final int ERROR_SYSTEM_HANDLER_NOT_FOUND_VALUE = 910;
    public static final int ERROR_SYSTEM_EXCEPTION_VALUE = 911;
    
    
    public final int getNumber() { return value; }
    
    public static GameResultCode valueOf(int value) {
      switch (value) {
        case 0: return SUCCESS;
        case 1: return ERROR_JOIN_GAME;
        case 100: return ERROR_USERID_NULL;
        case 101: return ERROR_USER_CANNOT_START_GAME;
        case 200: return ERROR_NO_SESSION_ID;
        case 201: return ERROR_NEXT_STATE_NOT_FOUND;
        case 202: return ERROR_SESSIONID_NULL;
        case 203: return ERROR_SESSION_ALREADY_START;
        case 910: return ERROR_SYSTEM_HANDLER_NOT_FOUND;
        case 911: return ERROR_SYSTEM_EXCEPTION;
        default: return null;
      }
    }
    
    public static com.google.protobuf.Internal.EnumLiteMap<GameResultCode>
        internalGetValueMap() {
      return internalValueMap;
    }
    private static com.google.protobuf.Internal.EnumLiteMap<GameResultCode>
        internalValueMap =
          new com.google.protobuf.Internal.EnumLiteMap<GameResultCode>() {
            public GameResultCode findValueByNumber(int number) {
              return GameResultCode.valueOf(number);
            }
          };
    
    public final com.google.protobuf.Descriptors.EnumValueDescriptor
        getValueDescriptor() {
      return getDescriptor().getValues().get(index);
    }
    public final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public static final com.google.protobuf.Descriptors.EnumDescriptor
        getDescriptor() {
      return com.orange.network.game.protocol.constants.GameConstantsProtos.getDescriptor().getEnumTypes().get(1);
    }
    
    private static final GameResultCode[] VALUES = {
      SUCCESS, ERROR_JOIN_GAME, ERROR_USERID_NULL, ERROR_USER_CANNOT_START_GAME, ERROR_NO_SESSION_ID, ERROR_NEXT_STATE_NOT_FOUND, ERROR_SESSIONID_NULL, ERROR_SESSION_ALREADY_START, ERROR_SYSTEM_HANDLER_NOT_FOUND, ERROR_SYSTEM_EXCEPTION, 
    };
    
    public static GameResultCode valueOf(
        com.google.protobuf.Descriptors.EnumValueDescriptor desc) {
      if (desc.getType() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "EnumValueDescriptor is not for this type.");
      }
      return VALUES[desc.getIndex()];
    }
    
    private final int index;
    private final int value;
    
    private GameResultCode(int index, int value) {
      this.index = index;
      this.value = value;
    }
    
    // @@protoc_insertion_point(enum_scope:game.GameResultCode)
  }
  
  
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\023GameConstants.proto\022\004game*\322\002\n\017GameComm" +
      "andType\022\025\n\021JOIN_GAME_REQUEST\020\001\022\026\n\022JOIN_G" +
      "AME_RESPONSE\020\002\022\026\n\022START_GAME_REQUEST\020\003\022\027" +
      "\n\023START_GAME_RESPONSE\020\004\022\"\n\036USER_JOIN_NOT" +
      "IFICATION_REQUEST\0203\022#\n\037USER_JOIN_NOTIFIC" +
      "ATION_RESPONSE\0204\022$\n HOST_CHANGE_NOTIFICA" +
      "TION_REQUEST\0205\022%\n!HOST_CHANGE_NOTIFICATI" +
      "ON_RESPONSE\0206\022#\n\037GAME_START_NOTIFICATION" +
      "_REQUEST\0207\022$\n GAME_START_NOTIFICATION_RE" +
      "SPONSE\0208*\245\002\n\016GameResultCode\022\013\n\007SUCCESS\020\000",
      "\022\023\n\017ERROR_JOIN_GAME\020\001\022\025\n\021ERROR_USERID_NU" +
      "LL\020d\022 \n\034ERROR_USER_CANNOT_START_GAME\020e\022\030" +
      "\n\023ERROR_NO_SESSION_ID\020\310\001\022\037\n\032ERROR_NEXT_S" +
      "TATE_NOT_FOUND\020\311\001\022\031\n\024ERROR_SESSIONID_NUL" +
      "L\020\312\001\022 \n\033ERROR_SESSION_ALREADY_START\020\313\001\022#" +
      "\n\036ERROR_SYSTEM_HANDLER_NOT_FOUND\020\216\007\022\033\n\026E" +
      "RROR_SYSTEM_EXCEPTION\020\217\007BA\n*com.orange.n" +
      "etwork.game.protocol.constantsB\023GameCons" +
      "tantsProtos"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
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
  }
  
  // @@protoc_insertion_point(outer_class_scope)
}
