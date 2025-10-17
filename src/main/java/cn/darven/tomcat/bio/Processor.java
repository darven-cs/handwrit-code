package cn.darven.tomcat.bio;

import java.io.*;
import java.net.Socket;

/**
 * @author darven
 * @date 2025/10/17
 * @description TODO
 */
public class Processor implements Runnable{
    private final Socket socket;

    public Processor(Socket socket){
        this.socket=socket;
    }

    @Override
    public void run() {
        try (
                InputStream inputStream=socket.getInputStream();
                OutputStream outputStream=socket.getOutputStream();
                BufferedReader reader=new BufferedReader(new InputStreamReader(inputStream));
                PrintWriter writer=new PrintWriter(outputStream,true);
        ){
            String line;
            StringBuffer sb=new StringBuffer();
            while ((line=reader.readLine())!=null&&!line.isEmpty()){
                sb.append(line).append("\r\n");
            }
            System.out.println("线程"+Thread.currentThread().getName()+"+“接收数据="+ sb);

            writer.println("HTTP/1.1 200 OK");
            writer.println("Content-Type:text/html");
            writer.println("Content-Length:"+"<h1>Hello World</h1>".length());
            writer.println();
            writer.println("<h1>Hello World</h1>");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }finally {
            try{
                if(!socket.isClosed()) {
                    socket.close();
                }
            }catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("线程"+Thread.currentThread().getName()+"+“处理完毕");
        }
    }
}
