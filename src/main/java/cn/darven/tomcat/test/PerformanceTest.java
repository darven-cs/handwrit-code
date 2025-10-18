package cn.darven.tomcat.test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * BIO和NIO服务器性能对比测试类
 */
public class PerformanceTest {

    private static final String SERVER_URL = "http://localhost:8080";
    private static final int CONCURRENT_THREADS = 50;
    private static final int REQUESTS_PER_THREAD = 20;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("开始进行性能测试...\n");

        // 测试BIO版本
        testBioServer();

        // 等待服务器完全关闭
        Thread.sleep(2000);

        // 测试NIO版本
        testNioServer();
    }

    /**
     * 测试BIO服务器性能
     */
    private static void testBioServer() {
        System.out.println("=== 测试BIO服务器 ===");

        long startTime = System.currentTimeMillis();

        // 启动BIO服务器（需要手动运行cn.darven.tomcat.bio.Main）
        System.out.println("请先手动启动BIO服务器(cn.darven.tomcat.bio.Main)");
        waitForUserInput("BIO服务器启动后，按回车键继续测试...");

        // 执行并发测试
        performLoadTest();

        long endTime = System.currentTimeMillis();
        System.out.println("BIO测试总耗时: " + (endTime - startTime) + "ms\n");
    }

    /**
     * 测试NIO服务器性能
     */
    private static void testNioServer() {
        System.out.println("=== 测试NIO服务器 ===");

        long startTime = System.currentTimeMillis();

        // 启动NIO服务器（需要手动运行cn.darven.tomcat.nio.Main）
        System.out.println("请先手动启动NIO服务器(cn.darven.tomcat.nio.Main)");
        waitForUserInput("NIO服务器启动后，按回车键继续测试...");

        // 执行并发测试
        performLoadTest();

        long endTime = System.currentTimeMillis();
        System.out.println("NIO测试总耗时: " + (endTime - startTime) + "ms\n");
    }

    /**
     * 执行并发负载测试
     */
    private static void performLoadTest() {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        List<Future<TestResult>> futures = new ArrayList<>();

        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            final int threadId = i;
            futures.add(executor.submit(() -> performRequests(threadId)));
        }

        // 收集测试结果
        int totalSuccess = 0;
        int totalFailed = 0;
        long totalTime = 0;

        for (Future<TestResult> future : futures) {
            try {
                TestResult result = future.get();
                totalSuccess += result.successCount;
                totalFailed += result.failedCount;
                totalTime += result.totalTime;
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

        System.out.println("测试结果汇总:");
        System.out.println("- 成功请求数: " + totalSuccess);
        System.out.println("- 失败请求数: " + totalFailed);
        System.out.println("- 平均响应时间: " + (totalTime / Math.max(totalSuccess, 1)) + "ms");
    }

    /**
     * 单个线程执行多次HTTP请求
     */
    private static TestResult performRequests(int threadId) {
        int successCount = 0;
        int failedCount = 0;
        long totalTime = 0;

        for (int i = 0; i < REQUESTS_PER_THREAD; i++) {
            try {
                long startTime = System.currentTimeMillis();
                sendHttpRequest();
                long endTime = System.currentTimeMillis();

                successCount++;
                totalTime += (endTime - startTime);

                // 添加小延迟避免过于激烈的请求
                Thread.sleep(10);
            } catch (Exception e) {
                failedCount++;
                System.err.println("线程" + threadId + "第" + i + "次请求失败: " + e.getMessage());
            }
        }

        return new TestResult(successCount, failedCount, totalTime);
    }

    /**
     * 发送HTTP GET请求
     */
    private static void sendHttpRequest() throws IOException {
        URL url = new URL(SERVER_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000); // 5秒连接超时
        connection.setReadTimeout(5000);    // 5秒读取超时

        int responseCode = connection.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("HTTP响应码: " + responseCode);
        }

        // 读取响应内容
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            System.out.println("HTTP响应内容: " + response.toString());
        }


        connection.disconnect();
    }

    /**
     * 等待用户输入确认
     */
    private static void waitForUserInput(String message) {
        System.out.println(message);
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 测试结果数据类
     */
    private static class TestResult {
        final int successCount;
        final int failedCount;
        final long totalTime;

        TestResult(int successCount, int failedCount, long totalTime) {
            this.successCount = successCount;
            this.failedCount = failedCount;
            this.totalTime = totalTime;
        }
    }
}
