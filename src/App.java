import java.util.*;

public class App {
    static final int MAX_QUEUE_CAPACITY = 64;
    static int id = 0;

    public static void main(String[] args) throws Exception {
        DataQueue dataQueue = new DataQueue(MAX_QUEUE_CAPACITY);

        Producer producer = new Producer(dataQueue);
        Thread producerThread = new Thread(producer);

        Consumer consumer = new Consumer(dataQueue);
        Thread consumerThread = new Thread(consumer);

        producerThread.start();
        consumerThread.start();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
			e.printStackTrace();
        }

        producer.stop();

        while (dataQueue.isEmpty() == false) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }    
        }
        consumer.stop();

        return;
    }
    
    public static class Message {
        public int id;
        public double data;
    
        // constructors and getter/setters
    }

    public static class DataQueue {
        private final Queue<Message> queue = new LinkedList<>();
        private final int maxSize;
        private final Object FULL_QUEUE = new Object();
        private final Object EMPTY_QUEUE = new Object();
    
        DataQueue(int maxSize) {
            this.maxSize = maxSize;
        }
    
        // other methods
        public void waitOnFull() throws InterruptedException {
            synchronized (FULL_QUEUE) {
                FULL_QUEUE.wait();
            }
        }
        public void notifyAllForFull() {
            synchronized (FULL_QUEUE) {
                FULL_QUEUE.notifyAll();
            }
        }

        public void waitOnEmpty() throws InterruptedException {
            synchronized (EMPTY_QUEUE) {
                EMPTY_QUEUE.wait();
            }
        }
        
        public void notifyAllForEmpty() {
            synchronized (EMPTY_QUEUE) {
                EMPTY_QUEUE.notify();
            }
        }

        public void add(Message message) {
            synchronized (queue) {
                queue.add(message);
                String string = String.format("-> [%d] %f (%d)", message.id, message.data, queue.size());
                System.out.println(string);
            }
        }

        public Message remove() {
            synchronized (queue) {
                return queue.poll();
            }
        }

        public Boolean isFull() {
            synchronized (queue) {
                return (queue.size() == maxSize);
            }
        }

        public Boolean isEmpty() {
            synchronized (queue) {
                return queue.isEmpty();
            }
        }
        public int getCount() {
            synchronized (queue) {
                return queue.size();
            }
        }
    }

    public static class Producer implements Runnable {
        private final DataQueue dataQueue;
        private volatile boolean runFlag;
    
        public Producer(DataQueue dataQueue) {
            this.dataQueue = dataQueue;
            runFlag = true;
        }
    
        @Override
        public void run() {
            produce();
        }
    
        // Other methods
        public void produce() {
            while (runFlag) {
                Message message = generateMessage();
                while (dataQueue.isFull()) {
                    try {
                        dataQueue.waitOnFull();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!runFlag) {
                    break;
                }
                dataQueue.add(message);

                dataQueue.notifyAllForEmpty();
            }
        }

        public void stop() {
            runFlag = false;
            dataQueue.notifyAllForFull();
        }

        public Message generateMessage() {
            Message msg = new Message();
            msg.id = id;
            msg.data = Math.random();
            id++;
            return msg;
        }
    }

    public static class Consumer implements Runnable {
        private final DataQueue dataQueue;
        private volatile boolean runFlag;
    
        public Consumer(DataQueue dataQueue) {
            this.dataQueue = dataQueue;
            runFlag = true;
        }
    
        @Override
        public void run() {
            consume();
        }
    
        // Other methods
        public void consume() {
            Message message;
            while (runFlag) {
                if (dataQueue.isEmpty()) {
                    try {
                        dataQueue.waitOnEmpty();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (!runFlag) {
                    break;
                }
                message = dataQueue.remove();
                dataQueue.notifyAllForFull();
                useMessage(message);
            }
        }

        public void stop() {
            runFlag = false;
            dataQueue.notifyAllForEmpty();
        }

        public void useMessage(Message message) {
            try {
                String string = String.format("<- [%d] %f (%d)", message.id, message.data, dataQueue.getCount());
                System.out.println(string);    
            } catch (Exception e) {

            }
        }
    }

};
