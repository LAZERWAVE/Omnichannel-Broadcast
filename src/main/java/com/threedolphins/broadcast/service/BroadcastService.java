package com.threedolphins.broadcast.service;

import com.threedolphins.broadcast.config.ExecutorConfig;
import com.threedolphins.broadcast.model.BroadcastJob;
import com.threedolphins.broadcast.model.Customer;
import com.threedolphins.broadcast.model.CustomerStatus;
import com.threedolphins.broadcast.model.SendResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
public class BroadcastService {

    @Inject
    private MockMessagingService messagingService;

    @Inject
    private ExecutorConfig executorConfig;

    private static final Logger LOGGER =
            Logger.getLogger(BroadcastService.class.getName());

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

               try {

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

               } catch (Exception e) {

                   job.setStatus(
                           customer.getId(),
                           CustomerStatus.FAILED
                   );

                   job.incrementFailed();

                   LOGGER.log(
                           Level.WARNING,
                           "Failed to send message to customer " + customer.getId(),
                           e
                   );
               }
            });
        }

        return job;
    }
}