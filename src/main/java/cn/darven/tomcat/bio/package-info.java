/**
 * 实现tomcat的多线程模型
 * 这里我们先用bio实现，后面再用nio实现，这样可以学到更多
 * 下面是多线程模型需要的组件
 * - Acceptor 监听器，监听tcp连接
 * - Executor 线程池，这里用来处理请求
 * - Processor 处理器，处理请求
 * 流程：Acceptor监听到tcp连接，封装成Processor，然后放到线程池执行
 * */
package cn.darven.tomcat.bio;