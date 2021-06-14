package com.tagsgroup.simulators.learn;

import net.openhft.chronicle.bytes.MethodReader;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueue;
import net.openhft.chronicle.queue.impl.single.SingleChronicleQueueBuilder;

public class OutputMain {
    public static void main(String[] args) throws InterruptedException {
        String path = "queue-fr";
        SingleChronicleQueue queue = SingleChronicleQueueBuilder.binary(path).build();
        MessageConsumer messagePrinter = System.out::println;
        MethodReader methodReader = queue.createTailer().methodReader(messagePrinter);

        while (true) {
            if (!methodReader.readOne())
                Thread.sleep(10);
        }
    }
}