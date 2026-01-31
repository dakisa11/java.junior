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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
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
    public String viewHomePage(Model model) {
        model.addAttribute("orderList", this.orderManager.getAllOrders());
        return "order/index";
    }

    @GetMapping("/{buyerId}/orders")
    public String viewBuyerOrder(@PathVariable Long buyerId, Model model) {
        model.addAttribute("orderList", this.orderManager.getAllBuyerOrders(buyerId));
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
            @RequestParam String street,
            @RequestParam String homeNumber,
            @RequestParam String city,
            @RequestParam String orderStatus,
            @RequestParam String paymentOption,
            @RequestParam String notes,
            @RequestParam MultiValueMap<String, String> parameters, ModelMap modelMap)
    {
        if (orderStatus == null || orderStatus.isBlank()) {
            order.setOrderStatus(orderStatus);
        }
        if (paymentOption == null ||paymentOption.isBlank()) {
            order.setPaymentOption(paymentOption);
        }

        BuyerAddress addr = new BuyerAddress();
        addr.setStreet(street);
        addr.setHomeNumber(homeNumber);
        addr.setCity(city);
        BuyerAddress savedAddr = buyerAddressRepository.save(addr);
        order.setDeliveryAddressId(savedAddr.getBuyerAddressId());

        order.setNotes(notes);
        order.setOrderTime(LocalDateTime.now());

        Order savedOrder = orderManager.save(order);

        Map<Integer, MenuItemDto> menuMap =
                menuService.getMenuItems().stream()
                .collect(Collectors.toMap(
                        MenuItemDto::getItemNr
                        ,m -> m
                ));

        for(String key: parameters.keySet()) {
            if(!key.startsWith("qty__"))continue;

            Integer itemNr = Integer.valueOf(key.substring(5));
            int quantity = Integer.parseInt(Objects.requireNonNull(parameters.getFirst(key)));

            if (quantity < 0)continue;

            MenuItemDto menuItemDto = menuMap.get(itemNr);
            if (menuItemDto == null)continue;

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderNr(savedOrder.getOrderNr());
            orderItem.setItemNr(itemNr);
            orderItem.setName(menuItemDto.getItemName());
            orderItem.setQuantity((short) quantity);
            orderItem.setPrice(menuItemDto.getPrice());

            orderItemRepository.save(orderItem);
        }

        return "redirect:/order/";
    }

    @GetMapping("/showFormForUpdate/{id}")
    public String updateForm(@PathVariable(value = "id") long id, Model model) {
        Order order = this.orderManager.getById(id);
        model.addAttribute("order", order);
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
