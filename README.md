# Handwrite Code 手写系列

本项目是一个手写框架核心功能的系列教程，通过手动实现主流框架的核心模块，深入理解其内部机制和设计思想。(不是一比一复刻，而是思想相同)

## 🎯 项目概述

- **项目名称**: handwrit-code
- **开发语言**: Java 17
- **构建工具**: Maven

通过手写实现 Spring 等框架的核心功能，包括 IOC、AOP、线程池等模块，帮助开发者更好地理解框架底层原理。

## 📚 学习内容

### IOC (Inversion of Control) 容器
- Bean 的定义、创建和管理机制
- 依赖注入(DI)的实现原理
- Bean 生命周期管理
- 容器初始化和销毁过程

### AOP (Aspect-Oriented Programming)
- 面向切面编程的设计思想
- 动态代理技术(JDK/CGLIB)应用
- 方法拦截和增强实现
- 切点(Pointcut)和通知(Advice)机制

### 线程池实现
- 自定义线程池设计模式
- 任务调度和执行机制
- 线程安全与并发控制
- 拒绝策略和性能调优

### 集合源码分析
- 常用集合类(ArrayList、HashMap等)内部实现
- 数据结构与算法在集合中的应用
- 并发集合(ConcurrentHashMap等)源码解析
- 性能优化策略和最佳实践

### Tomcat BIO实现
- 基于阻塞I/O的HTTP服务器实现
- 多线程架构：主监听线程 + 线程池处理请求
- 核心组件：
  - Acceptor: 连接监听器
  - Processor: 请求处理器
  - ThreadPoolConfig: 线程池配置
- 支持基本HTTP请求处理和响应返回
- 实现优雅停机机制

### Tomcat NIO实现
- 基于非阻塞I/O的HTTP服务器实现
- Reactor模式：主线程监听连接，线程池处理请求
- 核心组件：
  - Acceptor: 连接监听器和分发器
  - Processor: HTTP请求处理器
  - ThreadPoolConfig: 线程池配置
- 支持基本HTTP请求处理和响应返回
- 实现优雅停机机制

## ✅ 完成清单
- [ ] IOC 容器实现
    - [ ] `BeanFactory` 核心接口设计
    - [x] BeanDefinition 定义与解析
    - [x] 依赖注入功能实现
    - [x] BeanPostProcessor后置处理器支持
    - [ ] 容器生命周期管理

- [ ] AOP 框架实现
    - [ ] 切点表达式解析器
    - [ ] JDK动态代理创建机制
    - [ ] CGLIB代理实现
    - [ ] 通知类型(前置、后置、环绕等)实现
    - [ ] 切面织入(Weaving)机制

- [ ] 线程池模块
    - [x] 基础线程池 `ThreadPoolExecutor` 实现
    - [x] 任务队列(`BlockingQueue`)设计
    - [x] 线程复用和管理机制
    - [x] 拒绝策略(`RejectedExecutionHandler`)实现
    - [ ] 线程池状态监控

- [ ] 集合源码分析
    - [ ] `ArrayList` 核心方法源码解析
    - [ ] `HashMap` 底层数据结构分析
    - [ ] `ConcurrentHashMap` 并发安全实现
    - [ ] `LinkedList` 双向链表结构解析

- [ ] 可靠传输机制实现
    - [x] 停止等待（Stop-and-Wait）协议实现
     - - [x] DataPacket 数据包结构设计
     - - [x] StopAndWait 核心协议处理类实现
     - - [x] 发送端 Sender 实现
     - - [x] 接收端 Receiver 实现
     - - [x] Type 枚举类型定义
     - - [x] CRC校验机制实现
     - - [x] 超时重传机制实现
     - - [x] 序列号管理机制实现
    - [ ] 流水线协议实现
     - -[ ] 滑动窗口机制设计
     - -[ ] Go-Back-N协议实现
     - -[ ] 选择重传（Selective Repeat）协议实现
     - -[ ] 多数据包并发传输支持
     - -[ ] 接收窗口管理机制
     - -[ ] 累计确认机制实现
     - -[ ] 流量控制与拥塞控制
- [x] Tomcat BIO实现
  - [x] 阻塞式Socket通信
  - [x] 多线程请求处理
  - [x] HTTP协议解析
  - [x] 线程池资源管理
  - [x] 优雅停机机制

- [x] Tomcat NIO实现
  - [x] 非阻塞式Socket通信
  - [x] 多线程请求处理
  - [x] HTTP协议解析
  - [x] 线程池资源管理
  - [x] 优雅停机机制

## 🚀 快速开始

```bash
# 克隆项目
git clone <repository-url>

# 进入项目目录
cd handwrit-code

# 使用 Maven 构建项目
mvn clean compile
```


## 📖 学习建议

1. 按模块顺序学习，从基础的 IOC 开始
2. 结合源码阅读和实际调试
3. 每个模块都配有详细注释和测试用例
4. 建议动手实现一遍加深理解

## 🤝 贡献指南

欢迎提交 Issue 和 Pull Request 来帮助完善这个手写框架系列项目。对于任何改进意见或问题反馈，请随时提出。
