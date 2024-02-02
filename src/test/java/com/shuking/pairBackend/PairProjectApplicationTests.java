package com.shuking.pairBackend;

import com.shuking.pairBackend.service.AccountService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.*;
import java.util.concurrent.*;

@SpringBootTest(classes = PairProjectApplication.class)
class PairProjectApplicationTests {

    @Resource
    private AccountService accountService;
    @Test
    void contextLoads() {
    }

    @Test
    void ConCurrentTest() {
        //  自定义线程池 核心数40 最大1000 保持10000分钟 待处理任务数最大为10000
        ExecutorService executorService = new ThreadPoolExecutor(40, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));
        List<Integer> integers = Collections.synchronizedList(new ArrayList<>());
        //  默认ForkJoinPool提供的线程数 为当前电脑的cpu线程数
        ArrayList<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < 1000000; i++) {
            CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
//                System.out.println("thread--" + Thread.currentThread().getName());
                return new Random().nextInt(Integer.MAX_VALUE);
            }).thenAccept(integers::add);
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        System.out.println(integers.size());
    }

    @Test
    void stringTest() {
        long startTime = System.currentTimeMillis();
        final int num = 10000000;
        String str1 = "";
        for (int i = 0; i < num; i++) {
            str1 += 1;
        }
        System.out.println(System.currentTimeMillis()-startTime);

        startTime = System.currentTimeMillis();
        StringBuilder str2 = new StringBuilder();
        for (int i = 0; i < num; i++) {
            str2.append(1);
        }
        System.out.println(System.currentTimeMillis()-startTime);
    }

    @Test
    void mapTest(){
        TreeMap<Integer, Integer> map = new TreeMap<>(Comparator.comparingInt(a -> a));
        map.put(3,111);
        map.put(1,333);
        map.put(2,444);

        System.out.println(map.entrySet());
    }

    @Test
    void queueTest(){
        PriorityQueue<Integer> queue = new PriorityQueue<>(5);
    }
}
