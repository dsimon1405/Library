package com.simon.lib_service.service;

import com.simon.dto.user.OrderDTO;
import com.simon.exception.ExistsException;
import com.simon.name.PathRoles;
import com.simon.utils.HttpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IntegrationService {

    private final HttpRequest httpRequest;

    public static final String user_service_url = "http://user-service";
//    public static final String user_service_url = "http://localhost:8082";

    public void checkOpenOrdersByBookIdsAndThrowExistingException(List<Integer> bookIds, String throwMessageStart) {
        List<OrderDTO> orderDTOs = getOpenOrderDTOsByBookIds(bookIds);
        if (!orderDTOs.isEmpty())
            throw new ExistsException(throwMessageStart + orderDTOs);
    }

    public List<OrderDTO> getOpenOrderDTOsByBookIds(List<Integer> bookIds) {
        return bookIds.isEmpty() ? List.of()
                : httpRequest.request(
                    user_service_url + "/user-service/api/v1/order" + PathRoles.SERVICE + "/get/open",
                    HttpMethod.POST,
                    new HttpEntity<>(bookIds),
                    new ParameterizedTypeReference<List<OrderDTO>>(){},
                    null).getBody();
    }
}
