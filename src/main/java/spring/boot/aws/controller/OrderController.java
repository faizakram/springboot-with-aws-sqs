package spring.boot.aws.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import spring.boot.aws.service.OrderProcessingService;


@RestController
@RequestMapping(value = "/orders")
public class OrderController {

	@Autowired
    private OrderProcessingService orderCreationService;

   
    @GetMapping(value = "/createOrder")
    public void createOrder(@RequestParam int itemNumber){
        orderCreationService.createOrder(itemNumber);
    }
}