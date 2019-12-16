/*
 * Copyright 2012-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package sample.web.ui.controller;

import javax.validation.Valid;
import java.util.*;

import org.springframework.transaction.annotation.Transactional;
import sample.web.ui.crosscutting.MyExecutionTime;
import sample.web.ui.domain.*;
import sample.web.ui.repository.MessageRepository;
import sample.web.ui.repository.BaseOrderRepository;
import sample.web.ui.repository.ProductCatalogRepository;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sample.web.ui.repository.ProductRepository;

/**
 * @author Kevin Meeuwessen
 */
@Controller
@RequestMapping("/")
public class MessageController {

    private final MessageRepository messageRepository;
    private final BaseOrderRepository<Order> orderRepository;
    private final BaseOrderRepository<OrderOption> orderOptionRepository;
    private final ProductCatalogRepository productCatalogRepository;
    private final ProductRepository productRepository;


    // constructor dependency injection
    public MessageController(MessageRepository messageRepository,
                             BaseOrderRepository<Order> orderRepository,
                             BaseOrderRepository<OrderOption> orderOptionRepository,
                             ProductCatalogRepository productCatalogRepository,
                             ProductRepository productRepository) {
        this.messageRepository = messageRepository;
        this.orderRepository = orderRepository;
        this.orderOptionRepository = orderOptionRepository;
        this.productCatalogRepository = productCatalogRepository;
        this.productRepository = productRepository;
    }

    private void createProductCatalogAndProducts() {

        // build product catalog and two products

        ProductCatalog productCatalog = new ProductCatalog();

        // right productCatalog: without id; left productCatalog: with id
        // (needed because of auto increment)
        productCatalog = productCatalogRepository.save(productCatalog);

        System.out.println("#product in product catalog: " +
                productCatalog.getProducts().size());

        Product prod1 = new Product("schroefje", 2);
        Product prod2 = new Product("moertje", 1);
        // a product must have an id to be stored in product catalog, so save
        // explicitly
        prod1 = productRepository.save(prod1);
        prod2 = productRepository.save(prod2);

        // add two products
        productCatalog.add(prod1, 5);
        productCatalog.add(prod2, 5);

        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {}
    }



    private void createOrder() {
        // get the productCatalog
        Optional<ProductCatalog> productCatalog = productCatalogRepository.findById(1L);

        // "find" a product in the catalog and add it to the order
        Product prod = productCatalog.get().decrementStock(2L);

        // make a copy of the product (the copy has no id yet)
        // why a copy is made?
        Product prodCopy = new Product(prod);

        Order order = new Order();
        order = orderRepository.save(order);
        order.add(prodCopy);
//
//        System.exit(0);
    }


    private void decorateOrder() {
        Optional<Order> concreteOrder  = orderRepository.findById(4L);
        OrderOption decoratedOrder1 = new OrderOption("wrapping paper", 7, concreteOrder.get());
        orderOptionRepository.save(decoratedOrder1);
        OrderOption decoratedOrder2 = new OrderOption("nice box", 5, decoratedOrder1);
        orderOptionRepository.save(decoratedOrder2);
        OrderOption decoratedOrder3 = new OrderOption("fast delivery", 12, decoratedOrder2);
        orderOptionRepository.save(decoratedOrder3);
        System.out.println("***** content of the order: " + decoratedOrder3);
        System.out.println("***** price of the order: " + decoratedOrder3.price());
    }

    @Transactional
    @GetMapping
    @MyExecutionTime
    public ModelAndView list() {

        createProductCatalogAndProducts();

        Iterable<Message> messages = messageRepository.findAll();
        return new ModelAndView("messages/list", "messages", messages);
    }

    @GetMapping("{id}")
    public ModelAndView view(@PathVariable("id") Message message) {
        return new ModelAndView("messages/view", "message", message);
    }


    @Transactional
    @GetMapping(params = "form")
    public String createForm(@ModelAttribute Message message) {

        createOrder();
//        decorateOrder();

        return "messages/form";
    }

    @PostMapping
    public ModelAndView create(@Valid Message message, BindingResult result,
                               RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return new ModelAndView("messages/form", "formErrors", result.getAllErrors());
        }
        message = this.messageRepository.save(message);
        redirect.addFlashAttribute("globalMessage", "Successfully created a new message");
        return new ModelAndView("redirect:/{message.id}", "message.id", message.getId());
    }

    @RequestMapping("foo")
    public String foo() {
        throw new RuntimeException("Expected exception in controller");
    }

    @GetMapping(value = "delete/{id}")
    public ModelAndView delete(@PathVariable("id") Long id) {
        this.messageRepository.deleteById(id);
        Iterable<Message> messages = this.messageRepository.findAll();
        return new ModelAndView("messages/list", "messages", messages);
    }

    @GetMapping(value = "modify/{id}")
    public ModelAndView modifyForm(@PathVariable("id") Message message) {
        return new ModelAndView("messages/form", "message", message);
    }

}
