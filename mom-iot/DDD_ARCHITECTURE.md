# mom-iot模块DDD架构说明

## 目录结构

```
mom-iot/src/main/java/com/ly/factmesh/iot/
├── IotApplication.java                  # Spring Boot应用入口
├── domain/                              # 领域层：包含核心业务逻辑
│   ├── entity/                          # 实体：具有唯一标识的核心业务对象
│   │   └── Device.java                  # 设备实体，代表物理设备
│   ├── valueobject/                     # 值对象：无唯一标识的不可变对象
│   │   └── DeviceStatus.java            # 设备状态值对象，表示设备的当前状态
│   ├── aggregate/                       # 聚合根：管理多个实体和值对象的边界对象
│   │   └── DeviceAggregate.java         # 设备聚合根，以设备实体为中心的聚合
│   ├── service/                         # 领域服务：封装跨实体的业务逻辑
│   │   └── DeviceDomainService.java     # 设备领域服务，处理设备相关的业务规则
│   ├── repository/                      # 仓储接口：定义聚合根的持久化操作
│   │   └── DeviceRepository.java        # 设备仓储接口，设备聚合根的持久化抽象
│   └── event/                           # 领域事件：领域内发生的重要业务事件
│       ├── DomainEvent.java             # 领域事件抽象类，定义事件的基本结构
│       ├── DeviceRegisteredEvent.java   # 设备注册事件，设备成功注册时发布
│       └── DeviceStatusChangedEvent.java # 设备状态变更事件，设备状态变化时发布
├── application/                         # 应用层：协调领域层和基础设施层
│   ├── service/                         # 应用服务：处理用例，协调领域服务和基础设施
│   │   └── DeviceApplicationService.java # 设备应用服务，为表示层提供功能
│   ├── dto/                             # 数据传输对象：用于层间数据传递
│   │   └── DeviceDTO.java               # 设备数据传输对象，封装设备数据
│   ├── command/                         # 命令对象：表示要执行的操作
│   └── query/                           # 查询对象：表示查询条件
├── infrastructure/                      # 基础设施层：提供技术实现
│   ├── repository/                      # 仓储实现：实现仓储接口
│   │   ├── DeviceRepositoryImpl.java    # 设备仓储实现，实现DeviceRepository接口
│   │   └── JpaDeviceRepository.java     # JPA设备仓储，继承JpaRepository
│   └── database/                        # 数据库相关配置和实现
└── presentation/                        # 表示层：处理用户交互
    └── controller/                      # API控制器：处理HTTP请求
```

## 文件作用说明

### 1. 领域层 (Domain Layer)

#### Device.java (实体)
- 代表物理设备，具有唯一标识(ID)
- 包含设备的核心属性：设备编码、名称、类型、型号等
- 封装设备的基本业务行为：上线、离线、开始运行、停止运行、故障等
- 关联设备状态值对象

#### DeviceStatus.java (值对象)
- 表示设备的当前状态，无唯一标识
- 包含在线状态、运行状态、温度、湿度、电压、电流等
- 是Device实体的组成部分，使用@Embeddable注解
- 具有值相等性，状态变化时自动更新时间戳

#### DeviceAggregate.java (聚合根)
- 以Device实体为中心的聚合，管理设备相关的所有对象
- 定义聚合级别的业务逻辑：注册设备、更新设备信息等
- 维护聚合的一致性边界
- 管理领域事件的发布

#### DeviceDomainService.java (领域服务)
- 封装跨实体或聚合的业务规则
- 处理设备注册、状态更新、运行控制等核心业务逻辑
- 协调多个实体或聚合之间的交互
- 依赖设备仓储接口

#### DeviceRepository.java (仓储接口)
- 定义设备聚合根的持久化操作
- 提供保存、查询、删除等方法
- 抽象底层数据访问技术
- 是领域层与基础设施层的边界

#### DomainEvent.java (领域事件抽象类)
- 定义所有领域事件的基础结构
- 包含事件ID、事件类型、事件发生时间
- 是所有具体领域事件的父类

#### DeviceRegisteredEvent.java (设备注册事件)
- 当设备成功注册时发布
- 包含设备聚合根信息
- 用于通知其他系统或模块设备已注册

#### DeviceStatusChangedEvent.java (设备状态变更事件)
- 当设备状态发生变化时发布
- 包含设备ID、设备编码、在线状态、运行状态等信息
- 用于实时通知设备状态变化

### 2. 应用层 (Application Layer)

#### DeviceApplicationService.java (应用服务)
- 协调领域服务和基础设施，为表示层提供功能
- 处理设备相关的应用用例
- 提供DTO转换功能，隔离领域模型和表示层
- 管理事务边界

#### DeviceDTO.java (数据传输对象)
- 用于在应用层和表示层之间传递设备数据
- 包含设备的所有展示属性
- 提供Getters和Setters方法
- 与领域模型解耦

### 3. 基础设施层 (Infrastructure Layer)

#### DeviceRepositoryImpl.java (仓储实现)
- 实现DeviceRepository接口
- 使用JpaDeviceRepository进行底层数据访问
- 处理聚合根到实体的转换
- 管理聚合根的生命周期

#### JpaDeviceRepository.java (JPA设备仓储)
- 继承Spring Data JPA的JpaRepository
- 提供基本的CRUD操作
- 可以扩展自定义查询方法
- 基于JPA实现数据访问

### 4. 表示层 (Presentation Layer)

表示层目前尚未实现具体的控制器类，后续将添加DeviceController.java来处理HTTP请求，提供设备相关的API接口。

## 架构特点

1. **分层清晰**：严格遵循DDD分层架构，各层职责明确
2. **领域驱动**：核心业务逻辑封装在领域层，确保业务规则的完整性
3. **松耦合**：各层之间通过接口通信，便于替换实现
4. **可扩展**：新的业务功能可以通过添加新的命令、事件或服务来实现
5. **事件驱动**：通过领域事件实现模块间的松耦合通信
6. **标准化**：统一使用Snowflake ID生成器，确保ID的全局唯一性
7. **技术无关**：领域层不依赖具体的技术实现，便于切换底层技术栈

## 核心业务流程

1. **设备注册**：
   - 应用层接收注册请求，创建DeviceApplicationService
   - 领域服务DeviceDomainService处理注册逻辑
   - 设备聚合根DeviceAggregate执行注册操作
   - 发布DeviceRegisteredEvent事件
   - 仓储保存设备聚合根

2. **设备状态更新**：
   - 应用层接收状态更新请求
   - 领域服务调用设备聚合根的状态更新方法
   - 设备聚合根更新设备状态
   - 发布DeviceStatusChangedEvent事件
   - 仓储保存更新后的设备聚合根

3. **设备查询**：
   - 应用层接收查询请求
   - 领域服务通过仓储查询设备聚合根
   - 转换为DeviceDTO返回给表示层

此架构设计确保了系统的可维护性、可扩展性和业务规则的完整性，为后续功能开发奠定了坚实的基础。