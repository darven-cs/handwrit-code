/**
 * 可靠传输机制 Reliable Transmission Mechanism
 * 我们将在这个包里面简单实现TCP依赖的可靠传输机制，解决以下问题：
 * 1.怎么在IP（不可靠信道）上面实现可靠信道？
 * 2.IP不可靠的问题
 * 3.怎么解决丢包问题？
 * 4.怎么解决乱序问题？
 * 5.怎么解决重传问题？
 * 6.怎么解决重复问题？
 * */
package cn.darven.net.rtm;