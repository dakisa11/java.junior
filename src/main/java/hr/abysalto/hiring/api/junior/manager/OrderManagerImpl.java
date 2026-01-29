package hr.abysalto.hiring.api.junior.manager;

import hr.abysalto.hiring.api.junior.model.Order;
import hr.abysalto.hiring.api.junior.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderManagerImpl implements OrderManager {
    @Autowired
    private OrderRepository orderRepository;

    @Override
    public Iterable<Order> getAllOrders() {
        return this.orderRepository.findAll();
    }

    @Override
    public void save(Order order) {
        this.orderRepository.save(order);
    }

    @Override
    public Order getById(Long id) {
        return this.orderRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteById(Long id) {
        this.orderRepository.deleteById(id);
    }
}
