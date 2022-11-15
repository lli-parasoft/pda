package com.parasoft.demoapp.graphql;

import com.parasoft.demoapp.config.WebConfig;
import com.parasoft.demoapp.controller.ResponseResult;
import com.parasoft.demoapp.model.industry.CartItemEntity;
import graphql.schema.DataFetcher;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.List;
import java.util.Objects;

import static com.parasoft.demoapp.service.GlobalPreferencesDefaultSettingsService.HOST;

@RequiredArgsConstructor
@Component
public class CartItemGraphQLDataFetcher {
    private String cartItemBaseUrl;

    private final RestTemplate restTemplate;

    private final WebConfig webConfig;
    private final HttpServletRequest httpServletRequest;

    @PostConstruct
    private void init() {
        cartItemBaseUrl = HOST + webConfig.getServerPort() + "/v1/cartItems";
    }
//    public CartItemGraphQLDataFetcher(RestTemplate restTemplate, HttpServletRequest httpServletRequest, WebConfig webConfig) {
//        this.restTemplate = restTemplate;
//        this.httpServletRequest = httpServletRequest;
//        this.cartItemBaseUrl = HOST + webConfig.getServerPort() + "/v1/cartItems";
//    }

    public DataFetcher<List<CartItemEntity>> getCartItems() {
        return dataFetchingEnvironment -> {
            try {
                ResponseEntity<ResponseResult<List<CartItemEntity>>> entity =
                        restTemplate.exchange(cartItemBaseUrl,
                            HttpMethod.GET,
                            new HttpEntity<Void>(RestTemplateUtil.createHeaders(httpServletRequest)),
                            new ParameterizedTypeReference<ResponseResult<List<CartItemEntity>>>() {});
                return Objects.requireNonNull(entity.getBody()).getData();
            } catch (Exception e) {
                throw RestTemplateUtil.convertException(e);
            }
        };
    }
    public DataFetcher<CartItemEntity> addItemInCart() {
        System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        return dataFetchingEnvironment -> {
            try {
                ResponseEntity<ResponseResult<CartItemEntity>> entity =
                        restTemplate.exchange(cartItemBaseUrl,
                                HttpMethod.POST,
                                new HttpEntity<Void>(dataFetchingEnvironment.getArgument("shoppingCartDTO"),
                                        RestTemplateUtil.createHeaders(httpServletRequest)),
                                new ParameterizedTypeReference<ResponseResult<CartItemEntity>>() {});
                System.out.println(Objects.requireNonNull(entity.getBody()).getData());
                return Objects.requireNonNull(entity.getBody()).getData();
            } catch (Exception e) {
                throw RestTemplateUtil.convertException(e);
            }
        };
    }
}
