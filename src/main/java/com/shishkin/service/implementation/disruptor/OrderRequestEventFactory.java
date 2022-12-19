package com.shishkin.service.implementation.disruptor;

import com.lmax.disruptor.EventFactory;
import com.shishkin.dto.OrderRequestDto;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
public class OrderRequestEventFactory {
    private static final EventFactory<OrderRequestEventFactory> EVENT_FACTORY = OrderRequestEventFactory::new;
    private OrderRequestDto orderRequestDto;

    public static EventFactory<OrderRequestEventFactory> getInstanceFactory() {
        return EVENT_FACTORY;
    }
}
