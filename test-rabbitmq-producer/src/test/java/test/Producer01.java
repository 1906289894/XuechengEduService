package test;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class Producer01 {
    //队列名词
    private static final String  QUEUE = "helloword";

    public static void main(String[] args) throws IOException, TimeoutException {
        Connection connection = null;
        Channel channel = null;


        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            factory.setPort(5672);
            factory.setUsername("guest");
            factory.setPassword("guest");
            //rabbitmq默认虚拟机名称为“、”
            factory.setVirtualHost("/");
            //创建与RabbitMQ服务有关的TCP链接
            connection = factory.newConnection();
            //创建与Exchange的通道，链接可以创建多个通道，每个通道代表一个会话
            channel = connection.createChannel();

            /**
             * 声明队列，如果Rabbit中没有此队列将自动创建
             * param1:队列名称
             * param2:是否持久化
             * param3:队列是否独占此连接
             * param4:队列不再使用时是否自动删除此队列
             * param5:队列参数
             */
            channel.queueDeclare(QUEUE,true,false,false,null);

            String message = "helloword王斌"+ System.currentTimeMillis();

            /**
             * 消息发布方法
             * param1：Exchange的名称，如果没有指定，则使用Default Exchange
             * param2:routingKey,消息的路由Key，是用于Exchange（交换机）将消息转发到指定的消息队列
             * param3:消息包含的属性
             * param4：消息体
             */
            channel.basicPublish("",QUEUE,null,message.getBytes());
            System.out.println("Send Message is:"+message);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TimeoutException e) {
            e.printStackTrace();
        }finally {
            if (channel!=null) {
                channel.close();
            }
            if (connection != null) {
                connection.close();
            }
        }

    }
}
