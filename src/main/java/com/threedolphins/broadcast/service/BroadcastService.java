package com.threedolphins.broadcast.service;

import com.threedolphins.broadcast.config.ExecutorConfig;
import com.threedolphins.broadcast.model.BroadcastJob;
import com.threedolphins.broadcast.model.Customer;
import com.threedolphins.broadcast.model.CustomerStatus;
import com.threedolphins.broadcast.model.SendResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class BroadcastService {

    @Inject
    private MockMessagingService messagingService;

    @Inject
    private ExecutorConfig executorConfig;

    public BroadcastJob startBroadcast(
            List<Customer> customers,
            String message) {

        BroadcastJob job = new BroadcastJob(customers);

        for (Customer customer : customers) {

            executorConfig.getExecutorService().submit(() -> {

                job.setStatus(
                        customer.getId(),
                        CustomerStatus.PROCESSING
                );

                SendResult result =
                        messagingService.send(customer, message);

                if (result.isSuccess()) {

                    job.setStatus(
                            customer.getId(),
                            CustomerStatus.SENT
                    );

                    job.incrementSent();

                } else {

                    job.setStatus(
                            customer.getId(),
                            CustomerStatus.FAILED
                    );

                    job.incrementFailed();
                }
            });
        }

        return job;
    }
}