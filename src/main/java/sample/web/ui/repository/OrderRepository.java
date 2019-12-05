package sample.web.ui.repository;

import org.springframework.data.repository.CrudRepository;

import sample.web.ui.domain.Order;


public interface OrderRepository  extends CrudRepository<Order, Long> {}