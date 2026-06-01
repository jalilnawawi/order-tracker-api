package id.sevenspeed.tracking.service;

import id.sevenspeed.tracking.dto.request.order.CreateOrderRequest;
import id.sevenspeed.tracking.dto.request.order.UpdateOrderRequest;
import id.sevenspeed.tracking.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    Page<Order> findAll(String status, Long customerId, Pageable pageable);

    Order findById(Long id);

    Order create(CreateOrderRequest request);

    Order update(Long id, UpdateOrderRequest request);

    void delete(Long id);
}