package com.simon.lib_service.service.unit;

import com.simon.lib_service.service.IntegrationService;
import com.simon.dto.user.OrderDTO;
import com.simon.exception.ExistsException;
import com.simon.name.PathRoles;
import com.simon.utils.HttpRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.simon.lib_service.service.IntegrationService.user_service_url;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class IntegrationServiceUnitTest {

    @Mock
    HttpRequest httpRequest;
    @InjectMocks
    IntegrationService integrationService;

    @Test
    void checkOpenOrdersByBookIdsAndThrowExistingException_BookInOrders_ThrowExistsException() {
        List<Integer> bookIds = List.of(1);
        prepareRequest(bookIds, List.of(Mockito.mock(OrderDTO.class)));

        String msg = "err msg";

        assertThatThrownBy(() -> integrationService.checkOpenOrdersByBookIdsAndThrowExistingException(bookIds, msg))
                .isInstanceOf(ExistsException.class)
                .hasMessageContaining(msg);
    }

    @Test
    void checkOpenOrdersByBookIdsAndThrowExistingException_NoBookInOrders_Success() {
        prepareRequest(List.of(1), List.of());

        String msg = "err msg";

        assertDoesNotThrow(() -> integrationService.checkOpenOrdersByBookIdsAndThrowExistingException(List.of(1), msg));
    }

    @Test
    void getOpenOrderDTOsByBookIds_BookIdsEmpty_ReturnsEmptyList() {
        List<OrderDTO> result = integrationService.getOpenOrderDTOsByBookIds(List.of());

        assertThat(result).isEmpty();
    }

    @Test
    void getOpenOrderDTOsByBookIds_BookIds_MakeHttpRequest() {
        List<Integer> bookIds = List.of(1);
        prepareRequest(bookIds, List.of(Mockito.mock(OrderDTO.class)));

        List<OrderDTO> result = integrationService.getOpenOrderDTOsByBookIds(bookIds);

        assertThat(result).hasSize(1);
        verify(httpRequest, times(1)).request(
                user_service_url + "/user-service/api/v1/order" + PathRoles.SERVICE + "/get/open",
                HttpMethod.POST,
                new HttpEntity<>(bookIds),
                new ParameterizedTypeReference<List<OrderDTO>>(){},
                null);
    }

    private void prepareRequest(List<Integer> request_List, List<OrderDTO> return_list) {
        Mockito.when(httpRequest.request(
                user_service_url + "/user-service/api/v1/order" + PathRoles.SERVICE + "/get/open",
                HttpMethod.POST,
                new HttpEntity<>(request_List),
                new ParameterizedTypeReference<List<OrderDTO>>(){},
                null
        )).thenReturn(ResponseEntity.ok(return_list));
    }
}
