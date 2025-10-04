package com.simon.user_service.controller.integration;

import com.simon.dto.lib.BookDTO;
import com.simon.name.Headers;
import com.simon.name.PathRoles;
import com.simon.user_service.model.Account;
import com.simon.user_service.model.Order;
import com.simon.user_service.repository.AccountRepository;
import com.simon.user_service.repository.OrderRepository;
import com.simon.utils.HttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderControllerIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired AccountRepository accountRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired HttpRequest mockHttpRequest;

    @TestConfiguration
    static class TestRestTemplateConfig {
        @Bean
        @Primary
        public HttpRequest restTemplateTest(@Autowired RestTemplate restTemplate) {    //  exclude @BalanceLoader so RestTemplate will use localhost not service name
            return Mockito.mock(HttpRequest.class);
        }
    }

    static final String URI = "/api/v1/order";
    final int user_id = 1;

    @BeforeEach
    void setup(@Autowired RestTemplate restTemplate) {
        orderRepository.deleteAll();
        accountRepository.deleteAll();
    }

    @Test
    void makeOrder_ShouldReturnOk() throws Exception {
        accountRepository.save(new Account(user_id));

        int book_id = 5;
        BookDTO bookDTO = new BookDTO(book_id, "", null, null, BigDecimal.valueOf(6), 0);
        when(mockHttpRequest.request(anyString(), eq(HttpMethod.PUT), eq(HttpEntity.EMPTY), eq(BookDTO.class), any(Map.class)))
                .thenReturn(ResponseEntity.ok(bookDTO));

        mockMvc.perform(post(URI + PathRoles.USER + "/make/" + book_id)
                        .header(Headers.USER_ID, user_id))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.book_id").value(book_id));
    }

    @Test
    void closeOrder_WhenExists_ShouldReturnOk() throws Exception {
        Order order = orderRepository.save(new Order(new Account(user_id), 5, BigDecimal.ZERO));

        mockMvc.perform(put(URI + PathRoles.USER + "/close/" + order.getId())
                        .header(Headers.USER_ID, user_id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()));
    }

    @Test
    void closeOrder_WhenNotExists_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(put(URI + PathRoles.USER + "/close/999")
                        .header(Headers.USER_ID, user_id))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAccountOrders_ShouldReturnList() throws Exception {
        Account account = new Account(1);
        orderRepository.saveAll(List.of(
                new Order(account, 4, new BigDecimal("1.00")),
                new Order(account, 3, new BigDecimal("1.00"))));

        mockMvc.perform(get(URI + PathRoles.USER + "/get")
                        .header(Headers.USER_ID, user_id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].book_id").value(4))
                .andExpect(jsonPath("$[1].book_id").value(3));
    }

    @Test
    void getAccountOrders_WhenNoHeader_ShouldReturnConflict() throws Exception {
        mockMvc.perform(get(URI + PathRoles.USER + "/get"))
                .andExpect(status().isConflict());
    }
}



    //  MockRestServiceServer
//@SpringBootTest
//@AutoConfigureMockMvc
//@ActiveProfiles("test")
//class OrderControllerIntegrationTest {
//
//    @Autowired
//    MockMvc mockMvc;
//    @Autowired
//    AccountRepository accountRepository;
//    @Autowired
//    OrderRepository orderRepository;
//    @Autowired
//    ObjectMapper objectMapper;
//    MockRestServiceServer mockServer;
//
//    @TestConfiguration
//    static class TestRestTemplateConfig {
//
//        @Bean
//        @Primary
//        public RestTemplate restTemplateTest() {    //  exclude @BalanceLoader so RestTemplate will use localhost not service name
//            return new RestTemplate();
//        }
//    }
//
//    static final String URI = "/api/v1/order";
//    static final String service_url = "http://localhost:8081";    //  must be localhost not service name and in OrderService restTemplate (HttpRequest) must call localhost too
//    final int user_id = 1;
//
//    @BeforeEach
//    void setup(@Autowired RestTemplate restTemplate) {
//        orderRepository.deleteAll();
//        accountRepository.deleteAll();
//        mockServer = MockRestServiceServer.createServer(restTemplate);
//    }
//
//    @Test
//    void makeOrder_ShouldReturnOk() throws Exception {
//        accountRepository.save(new Account(user_id));
//
//        int book_id = 5;
//        BookDTO bookDTO = new BookDTO(book_id, "", null, null, BigDecimal.valueOf(6), 0);
//        mockServer.expect(ExpectedCount.once(),
//                        requestTo(service_url + "/lib-service/api/v1/book" + PathRoles.SERVICE
//                                + "/rent/" + book_id + "?change_on=-1"))
//                .andExpect(method(HttpMethod.PUT))
//                .andRespond(withSuccess(objectMapper.writeValueAsString(bookDTO), MediaType.APPLICATION_JSON));
//
//        mockMvc.perform(post(URI + PathRoles.USER + "/make/" + book_id)
//                        .header(Headers.USER_ID, user_id))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.book_id").value(book_id));
//    }
//}