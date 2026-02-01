package hr.abysalto.hiring.api.junior.controller;

import hr.abysalto.hiring.api.junior.Service.MenuService;
import hr.abysalto.hiring.api.junior.components.DatabaseInitializer;
import hr.abysalto.hiring.api.junior.dto.MenuItemDto;
import hr.abysalto.hiring.api.junior.manager.OrderManager;
import hr.abysalto.hiring.api.junior.manager.BuyerManager;
import hr.abysalto.hiring.api.junior.model.BuyerAddress;
import hr.abysalto.hiring.api.junior.model.Order;
import hr.abysalto.hiring.api.junior.model.OrderItem;
import hr.abysalto.hiring.api.junior.repository.BuyerAddressRepository;
import hr.abysalto.hiring.api.junior.repository.BuyerRepository;
import hr.abysalto.hiring.api.junior.repository.OrderItemRepository;
import hr.abysalto.hiring.api.junior.repository.OrderRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Orders", description = "for handling orders")
@RequestMapping("order")
@Controller
public class OrderController {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderManager orderManager;
    @Autowired
    private BuyerManager buyerManager;
    @Autowired
    private DatabaseInitializer databaseInitializer;
    @Autowired
    private BuyerAddressRepository buyerAddressRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private MenuService menuService;
    @Autowired
    private BuyerRepository buyerRepository;

    @Operation(summary = "Get all buyers", responses = {
            @ApiResponse(description = "Success", responseCode = "200", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Order.class)))),
            @ApiResponse(description = "Precondition failed", responseCode = "412", content = @Content(mediaType = "text/plain", schema = @Schema(implementation = String.class))),
            @ApiResponse(description = "Error", responseCode = "500", content = @Content(mediaType = "application/json")) })
    @GetMapping("/list")
    public ResponseEntity list() {
        if (!this.databaseInitializer.isDataInitialized()) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).contentType(MediaType.TEXT_PLAIN).body("Data not initialized");
        }
        try {
            return ResponseEntity.ok(this.orderManager.getAllOrders());
        } catch (Exception ex) {
            return ResponseEntity.internalServerError().body(ex);
        }
    }

    @GetMapping("/")
    public String viewHomePage(
            @RequestParam(value = "sort", required = false, defaultValue = "asc") String sort,
            Model model
    ){
        model.addAttribute("orderList", this.orderManager.getAllOrdersSortedByTotal(sort));
        model.addAttribute("sort", sort);

        return "order/index";
    }

    @GetMapping("/{buyerId}/orders")
    public String viewBuyerOrder(@PathVariable Long buyerId, Model model){
        model.addAttribute("orderList", this.orderManager.getAllBuyerOrders(buyerId));
        model.addAttribute("buyerId", buyerId);
        return "order/orders";
    }

    @GetMapping("/addnew")
    public String addNewOrder(Model model) {
        Order order = new Order();
        model.addAttribute("order", order);
        model.addAttribute("buyerList", buyerManager.getAllBuyers());
        model.addAttribute("menuItems", menuService.getMenuItems());
        return "order/neworder";
    }

    @PostMapping("/save")
    public String saveOrder(
            @ModelAttribute("order") Order order,

            //create
            @RequestParam(required = false) String street,
            @RequestParam(required = false) String homeNumber,
            @RequestParam(required = false) String city,

            //update
            @RequestParam(required = false) Long selectedAddressId,
            @RequestParam(required = false) String newStreet,
            @RequestParam(required = false) String newHomeNumber,
            @RequestParam(required = false) String newCity,

            @RequestParam(required = false) String orderStatus,
            @RequestParam(required = false) String paymentOption,
            @RequestParam(required = false) String notes,

            @RequestParam(required = false) MultiValueMap<String, String> parameters
    ){
        if (orderStatus != null && !orderStatus.isBlank()) {
            order.setOrderStatus(orderStatus);
        }
        if (paymentOption != null && !paymentOption.isBlank()) {
            order.setPaymentOption(paymentOption);
        }
        order.setNotes(notes);

        if (order.getOrderNr() == null) {
            order.setOrderTime(LocalDateTime.now());
        }

        order.setCurrency("EUR");

        Long deliveryAddressId = resolveDeliveryAddressId(
                selectedAddressId,
                newStreet, newHomeNumber, newCity,
                street, homeNumber, city
        );
        order.setDeliveryAddressId(deliveryAddressId);

        Map<Integer, MenuItemDto> menuMap =
                menuService.getMenuItems().stream()
                .collect(Collectors.toMap(
                        MenuItemDto::getItemNr
                        ,m -> m
                ));

        List<OrderItem> newItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for(String key: parameters.keySet()) {
            if(!key.startsWith("qty__"))continue;

            Integer itemNr = Integer.valueOf(key.substring(5));
            String qty = parameters.getFirst(key);
            int quantity = qty == null ? 0 : Integer.parseInt(qty);

            if (quantity < 0)continue;

            MenuItemDto menuItemDto = menuMap.get(itemNr);
            if (menuItemDto == null)continue;

            OrderItem orderItem = new OrderItem();

            orderItem.setItemNr(itemNr);
            orderItem.setName(menuItemDto.getItemName());
            orderItem.setQuantity((short) quantity);
            orderItem.setPrice(menuItemDto.getPrice());

            newItems.add(orderItem);

            total = total.add(orderItem.getPrice().multiply(BigDecimal.valueOf(quantity)));
        }

        order.setTotalPrice(total);

        Order savedOrder = orderManager.save(order);

        orderItemRepository.deleteByOrderNr(savedOrder.getOrderNr());
        for(OrderItem orderItem : newItems){
            orderItem.setOrderNr(savedOrder.getOrderNr());
            orderItemRepository.save(orderItem);
        }

        return "redirect:/order/";
    }

    private Long resolveDeliveryAddressId(
            Long selectedAddressId,
            String newStreet, String newHomeNumber, String newCity,
            String street, String homeNumber, String city
    ){
        boolean hasNew =
                newStreet != null && !newStreet.isBlank() &&
                newHomeNumber != null && !newHomeNumber.isBlank() &&
                newCity != null && !newCity.isBlank();

        if (hasNew) {
            BuyerAddress buyerAddress = new BuyerAddress();
            buyerAddress.setStreet(newStreet.trim());
            buyerAddress.setHomeNumber(newHomeNumber.trim());
            buyerAddress.setCity(newCity.trim());
            return buyerAddressRepository.save(buyerAddress).getBuyerAddressId();
        }

        if(selectedAddressId != null) {
            return selectedAddressId;
        }

        boolean hasCreatedAddress =
                street != null && !street.isBlank() &&
                homeNumber != null && !homeNumber.isBlank() &&
                city != null && !city.isBlank();

        if (hasCreatedAddress) {
            BuyerAddress buyerAddress = new BuyerAddress();
            buyerAddress.setStreet(street.trim());
            buyerAddress.setHomeNumber(homeNumber.trim());
            buyerAddress.setCity(city.trim());
            return buyerAddressRepository.save(buyerAddress).getBuyerAddressId();
        }

        throw new IllegalArgumentException("Delivery address is required.");
    }

    @GetMapping("/showFormForUpdate/{id}")
    public String updateForm(@PathVariable(value = "id") long id, Model model) {
        Order order = this.orderManager.getById(id);
        model.addAttribute("order", order);

        model.addAttribute("buyerList", buyerManager.getAllBuyers());
        model.addAttribute("addressList", buyerAddressRepository.findAll());
        model.addAttribute("menuItems", menuService.getMenuItems());

        Map<Integer, Short> qtyMap = new HashMap<>();
        for (OrderItem orderItem: orderItemRepository.findByOrderNr(order.getOrderNr())) {
            qtyMap.put(orderItem.getItemNr(), orderItem.getQuantity());
        }
        model.addAttribute("qtyMap", qtyMap);

        return "order/updateorder";
    }

    @GetMapping("/deleteOrder/{id}")
    public String deleteById ( @PathVariable(value = "id") long id){
        orderItemRepository.deleteByOrderNr(id);  // prvo child
        orderRepository.deleteById(id);           // onda parent
        return "redirect:/order/";
    }

    @PostMapping("/{orderNr}/status")
    public String updateOrderStatus(
            @PathVariable Long orderNr,
            @RequestParam("status") String status,
            @RequestParam(value = "redirect", required = false, defaultValue = "/order/") String redirect
    ) {
        orderManager.updateStatus(orderNr, status);
        return "redirect:" + redirect;
    }

}
