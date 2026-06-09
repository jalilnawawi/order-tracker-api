package id.sevenspeed.tracking.service;

import id.sevenspeed.tracking.dto.request.order.CreateOrderRequest;
import id.sevenspeed.tracking.dto.request.order.UpdateOrderRequest;
import id.sevenspeed.tracking.dto.response.order.OrderDetailResponse;
import id.sevenspeed.tracking.dto.response.order.OrderListItemResponse;
import id.sevenspeed.tracking.dto.response.order.OrderResponse;
import id.sevenspeed.tracking.dto.response.order.OrderTimelineStepResponse;
import id.sevenspeed.tracking.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {

    Page<OrderListItemResponse> findAll(String status, Long customerId, Pageable pageable);

    OrderDetailResponse findById(Long id);

    OrderResponse create(CreateOrderRequest request);

    OrderResponse update(Long id, UpdateOrderRequest request);

    void delete(Long id);

    OrderDetailResponse findByIdAndCustomerId(Long id, Long customerId);

    List<OrderTimelineStepResponse> getCustomerTimeline(Long id, Long customerId);

    Order findEntityById(Long id); // dipakai internal service lain
}