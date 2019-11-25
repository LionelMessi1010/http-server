package com.duiyi;

/**
 * @time: 2019/11/22 20:21
 * @version: 1.00
 * @author: duiyi
 */
public class Main {
    public static void main(String[] args) {
        NettyHttpServer server = new NettyHttpServer(8080);

        try {
            server.init();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("exception: " + e.getMessage());
        }
        System.out.println("server close!");

    }
}
