package hr.abysalto.hiring.api.junior.manager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import hr.abysalto.hiring.api.junior.dto.MenuItemDto;
import hr.abysalto.hiring.api.junior.model.*;
import hr.abysalto.hiring.api.junior.repository.BuyerAddressRepository;
import hr.abysalto.hiring.api.junior.repository.OrderItemRepository;
import hr.abysalto.hiring.api.junior.repository.OrderRepository;
import hr.abysalto.hiring.api.junior.dto.OrderViewDto;
import hr.abysalto.hiring.api.junior.service.MenuService;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

@Component
public class OrderManagerImpl implements OrderManager {
    private final OrderRepository orderRepository;
    private final BuyerAddressRepository buyerAddressRepository;
    private final OrderItemRepository orderItemRepository;
    private final BuyerManager buyerManager;
    private final MenuService menuService;

    public OrderManagerImpl(OrderRepository orderRepository,
                            BuyerAddressRepository buyerAddressRepository,
                            OrderItemRepository orderItemRepository,
                            BuyerManager buyerManager, MenuService menuService) {
        this.orderRepository = orderRepository;
        this.buyerAddressRepository = buyerAddressRepository;
        this.orderItemRepository = orderItemRepository;
        this.buyerManager = buyerManager;
        this.menuService = menuService;
    }

    @Override
    public List<OrderViewDto> getAllOrders() {
        List<Order> orders = (List<Order>)orderRepository.findAll();
        return getOrderViewDtos(orders);
    }

    @Override
    public Order save(Order order) {
        this.orderRepository.save(order);
        return order;
    }

    @Override
    public Order getById(Long id) {
        return this.orderRepository.findById(id).orElse(null);
    }

    @Override
    public void deleteById(Long id) {
        this.orderItemRepository.deleteById(id);
        this.orderRepository.deleteById(id);
    }

    @Override
    public List<OrderViewDto> getAllBuyerOrders(Long buyerId) {
        List<Order> orders = orderRepository.findByBuyerId(buyerId);
        return getOrderViewDtos(orders);
    }

    @Override
    public void updateStatus(Long orderNr, String status) {
        if (status == null || status.isBlank()) return;
        OrderStatus os = OrderStatus.fromString(status);
        if (os == null) return;

        orderRepository.updateStatus(orderNr, os.toString());
    }

    @Override
    @Transactional
    public Order createOrder(Order order, BuyerAddress newAddress, MultiValueMap<String, String> parameters){
        if (order.getOrderStatus() == null){
            order.setOrderStatus("WAITING_FOR_CONFIRMATION");
        }
        if(order.getPaymentOption() == null){
            order.setPaymentOption("CASH");
        }

        order.setOrderTime(LocalDateTime.now());
        order.setCurrency("EUR");

        BuyerAddress savedAddr = buyerAddressRepository.save(newAddress);;
        order.setDeliveryAddressId(savedAddr.getBuyerAddressId());

        Map<Integer, Integer> qtyMap = parseQty(parameters);
        BigDecimal totalEur = calculateTotalEUR(qtyMap);
        order.setTotalPrice(totalEur);

        Order savedOrder = orderRepository.save(order);

        upsertOrderItems(savedOrder.getOrderNr(), qtyMap);

        return savedOrder;
    }


    @Override
    @Transactional
    public Order updateOrder(Long orderNr, Order updatedOrder, Long existingAddressId,
                             BuyerAddress maybeNewAddress, MultiValueMap<String, String> parameters
    ){
        Order db = orderRepository.findById(orderNr).orElse(null);

        db.setBuyerId(updatedOrder.getBuyerId());
        db.setOrderStatus(updatedOrder.getOrderStatus());
        db.setPaymentOption(updatedOrder.getPaymentOption());
        db.setCurrency(updatedOrder.getCurrency());
        db.setNotes(updatedOrder.getNotes());

        db.setCurrency("EUR");

        Long addressId = resolveDeliveryAddressId(existingAddressId, maybeNewAddress);
        db.setDeliveryAddressId(addressId);

        Map<Integer, Integer> qtyMap = parseQty(parameters);
        BigDecimal totalEur = calculateTotalEUR(qtyMap);
        db.setTotalPrice(totalEur);
        db.setCurrency("EUR");

        Order saved = orderRepository.save(db);

        orderItemRepository.deleteByOrderNr(orderNr);
        upsertOrderItems(orderNr, qtyMap);

        return saved;
    }

    @Override
    public List<OrderViewDto> getAllOrdersSortedByTotal(String direction){
        List<Order> orders;

        if("desc".equalsIgnoreCase(direction)){
            orders = orderRepository.findAllOrdersByTotalPriceDesc();
        }else{
            orders = orderRepository.findAllOrdersByTotalPriceAsc();
        }
        return getOrderViewDtos(orders);
    }

    private Map<Integer, Integer> parseQty(MultiValueMap<String, String> parameters){
        Map<Integer, Integer> result = new HashMap<>();
        for(String key : parameters.keySet()){
            if(!key.startsWith("qty__"))continue;

            String v = parameters.getFirst(key);

            int itemNr = Integer.parseInt(key.substring(5));
            int qty = Integer.parseInt(v);

            if (qty > 0)result.put(itemNr, qty);
        }
        return result;
    }

    private BigDecimal calculateTotalEUR(Map<Integer, Integer> qtyMap){
        Map<Integer, MenuItemDto> menuMap = menuService.getMenuItems().stream()
                .collect(Collectors.toMap(MenuItemDto::getItemNr, m -> m));

        BigDecimal total = BigDecimal.ZERO;

        for(var e : qtyMap.entrySet()){
            MenuItemDto item = menuMap.get(e.getKey());
            if(item == null) continue;

            BigDecimal line = item.getPrice().multiply(BigDecimal.valueOf(e.getValue()));
            total = total.add(line);
        }
        return total;
    }

    private Long resolveDeliveryAddressId(Long existingAddressId, BuyerAddress maybeNewAddress){
        if(maybeNewAddress != null &&
                maybeNewAddress.getStreet() != null && !maybeNewAddress.getStreet().isBlank() &&
                maybeNewAddress.getCity() != null && !maybeNewAddress.getCity().isBlank()){
            BuyerAddress saved = buyerAddressRepository.save(maybeNewAddress);
            return saved.getBuyerAddressId();
        }
        return existingAddressId;
    }

    private void upsertOrderItems(Long orderNr, Map<Integer, Integer> qtyMap){
        if(qtyMap.isEmpty())return;

        Map<Integer, MenuItemDto> menuMap = menuService.getMenuItems().stream()
                .collect(Collectors.toMap(MenuItemDto::getItemNr, m -> m));

        for(var e : qtyMap.entrySet()){
            int itemNr = e.getKey();
            int qty = e.getValue();

            MenuItemDto item = menuMap.get(itemNr);
            if(item == null) continue;

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderNr(orderNr);
            orderItem.setItemNr(itemNr);
            orderItem.setName(item.getItemName());
            orderItem.setQuantity((short)qty);
            orderItem.setPrice(item.getPrice());

            orderItemRepository.save(orderItem);
        }
    }

    private List<OrderViewDto> getOrderViewDtos(List<Order> orders){
        List<OrderViewDto> result = new ArrayList<>();

        for (Order order : orders) {
            OrderViewDto orderViewDto = new OrderViewDto();

            Buyer buyer = buyerManager.getById(order.getBuyerId());
            if (buyer.getTitle() != null) {
                orderViewDto.setBuyerName(buyer.getTitle() + ". " + buyer.getFirstName() + ' ' + buyer.getLastName());
            }else{
                orderViewDto.setBuyerName(buyer.getFirstName() + ' ' + buyer.getLastName());
            }
            orderViewDto.setOrderNr(order.getOrderNr());
            orderViewDto.setOrderId(order.getBuyerId());
            orderViewDto.setOrderStatus(OrderStatus.fromString(order.getOrderStatus()));
            orderViewDto.setOrderTime(order.getOrderTime());
            orderViewDto.setPaymentOption(PaymentOption.fromString(order.getPaymentOption()));
            orderViewDto.setContactNumber(order.getContactNumber());
            orderViewDto.setCurrency("EUR");
            orderViewDto.setTotalPrice(order.getTotalPrice());
            orderViewDto.setNotes(order.getNotes());

            buyerAddressRepository
                    .findById(order.getDeliveryAddressId())
                    .ifPresent(orderViewDto::setDeliveryAddress);

            List<OrderItem> orderItems =
                    orderItemRepository.findByOrderNr(order.getOrderNr())
                            .stream().filter(i -> i.getQuantity() > 0)
                            .toList();
            orderViewDto.setOrderItems(orderItems);

            result.add(orderViewDto);
        }
        return result;
    }
}
