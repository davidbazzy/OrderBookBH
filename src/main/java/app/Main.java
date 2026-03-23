package app;

import orderbook.OrderProcessor;
import parser.OrderParser;
import model.Order;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {

    /**
     * Main entry point for extracting and processing orders.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        // Filepath defaults to provided sample orders file resource folder if filepath not provided in args
        String filePath = args.length > 0 ? args[0] : "resources/orders";

        List<String> inputOrders = Files.readAllLines(Path.of(filePath));
        List<Order> orders = OrderParser.getOrdersFromInputLines(inputOrders);

        OrderProcessor orderProcessor = new OrderProcessor();
        for (Order order : orders) {
            orderProcessor.processOrder(order);
        }

    }
}
