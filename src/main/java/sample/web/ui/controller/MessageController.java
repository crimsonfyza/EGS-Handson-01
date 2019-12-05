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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import sample.web.ui.domain.Message;
import sample.web.ui.domain.Order;
import sample.web.ui.domain.Product;
import sample.web.ui.domain.ProductCatalog;
import sample.web.ui.repository.MessageRepository;
import sample.web.ui.repository.OrderRepository;
import sample.web.ui.repository.ProductCatalogRepository;
import sample.web.ui.repository.ProductRepository;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sample.web.ui.repository.ProductRepository;

import java.util.Optional;


@Controller
@RequestMapping("/")
public class MessageController {
	private final MessageRepository messageRepository;
	private final OrderRepository orderRepository;
	private final ProductCatalogRepository productCatalogRepository;

	// constructor dependency injection
	@Autowired
	public MessageController(MessageRepository messageRepository,
							 OrderRepository orderRepository,
							 ProductCatalogRepository productCatalogRepository) {
		this.messageRepository = messageRepository;
		this.orderRepository = orderRepository;
		this.productCatalogRepository = productCatalogRepository;

	}



	public void createProductCatalogAndProducts() {

		// build product catalog and two products

		ProductCatalog productCatalog = new ProductCatalog();

		// right-side productCatalog: without id; left-side productCatalog: with id
		// (needed because of autoincrement)
		productCatalog = productCatalogRepository.save(productCatalog);

		Product prod1 = new Product("schroefje", 2);
		Product prod2 = new Product("moertje", 1);

		// add two products
		productCatalog.add(prod1);
		productCatalog.add(prod2);

	}

    public void createOrder() {
        // get the productCatalog
        Optional<ProductCatalog> productCatalog = productCatalogRepository.findById(1L);

        // "find" a product in the catalog and add it to the order
        Product prod = productCatalog.get().find(2L);

        // make a copy of the product (the copy has no id yet)
        // why a copy is made?
        Product prodCopy = new Product(prod);

        Order order = new Order();
        order = orderRepository.save(order);
        order.add(prodCopy);
    }

    @Transactional
    @GetMapping
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
