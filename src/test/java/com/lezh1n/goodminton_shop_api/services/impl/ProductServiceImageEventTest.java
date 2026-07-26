package com.lezh1n.goodminton_shop_api.services.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.multipart.MultipartFile;

import com.lezh1n.goodminton_shop_api.dtos.response.ResourceResponse;
import com.lezh1n.goodminton_shop_api.entities.Resources;
import com.lezh1n.goodminton_shop_api.enums.ResourceOwner;
import com.lezh1n.goodminton_shop_api.events.ProductChangedEvent;
import com.lezh1n.goodminton_shop_api.repositories.ProductRepository;
import com.lezh1n.goodminton_shop_api.repositories.ResourceRepository;
import com.lezh1n.goodminton_shop_api.services.ResourceService;

@ExtendWith(MockitoExtension.class)
class ProductServiceImageEventTest {

    @Mock ProductRepository productRepository;
    @Mock ResourceRepository resourceRepository;
    @Mock ResourceService resourceService;
    @Mock ApplicationEventPublisher events;
    @Mock MultipartFile file;

    @InjectMocks ProductServiceImpl service;

    @Test
    void uploadProductImage_publishesImagesUpdatedEvent() {
        when(productRepository.existsById(7)).thenReturn(true);
        when(resourceService.upload(ResourceOwner.PRODUCT_THUMBNAIL, 7, file))
                .thenReturn(ResourceResponse.builder().id(99).build());

        service.uploadProductImage(7, file);

        ArgumentCaptor<ProductChangedEvent> captor = ArgumentCaptor.forClass(ProductChangedEvent.class);
        verify(events).publishEvent(captor.capture());
        assertThat(captor.getValue().action()).isEqualTo("updated");
        assertThat(captor.getValue().productId()).isEqualTo(7);
        assertThat(captor.getValue().fieldsChanged()).containsExactly("images");
    }

    @Test
    void deleteProductImage_publishesImagesUpdatedEventForOwningProduct() {
        lenient().when(resourceRepository.findById(99)).thenReturn(Optional.of(
                Resources.builder().id(99).ownerId(42)
                        .ownerType(ResourceOwner.PRODUCT_THUMBNAIL).build()));

        service.deleteProductImage(99);

        verify(resourceService).delete(99);
        ArgumentCaptor<ProductChangedEvent> captor = ArgumentCaptor.forClass(ProductChangedEvent.class);
        verify(events).publishEvent(captor.capture());
        assertThat(captor.getValue().action()).isEqualTo("updated");
        assertThat(captor.getValue().productId()).isEqualTo(42);
        assertThat(captor.getValue().fieldsChanged()).containsExactly("images");
    }
}
